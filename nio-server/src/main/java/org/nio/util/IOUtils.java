package org.nio.util;

import com.google.protobuf.GeneratedMessageV3;
import io.netty.buffer.ByteBuf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class IOUtils {
    public static byte[] readUntilNewline(ByteBuf byteBuf) {
        int initialReaderIndex = byteBuf.readerIndex();
        int length = 0;
        while (byteBuf.isReadable()) {
            byte b = byteBuf.readByte();
            if (b == '\n') {
                break;
            }
            length++;
        }

        byteBuf.readerIndex(initialReaderIndex);
        byte[] result = new byte[length];
        byteBuf.readBytes(result);
        return result;
    }

    public static long bytesToLong(byte[] b) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(new byte[Long.BYTES - b.length]);
        buffer.put(b);
        buffer.flip();
        return buffer.getLong();
    }

    public static byte[] toByteArrayWithoutConsuming(ByteBuf byteBuf) {
        long len = bytesToLong(readUntilNewline(byteBuf));
        System.out.println("=========================> " + len);
        if (byteBuf.readableBytes() < 4) {
            return null;
        }
        byteBuf.markReaderIndex();

        int length = byteBuf.readInt();
        if (byteBuf.readableBytes() < length) {
            byteBuf.resetReaderIndex();
            return null;
        }

        byte[] array = new byte[length];
        byteBuf.readBytes(array);
        return array;
    }

    public static <T extends GeneratedMessageV3> byte[] serializeMessages(T message) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        message.writeDelimitedTo(outputStream);
        return outputStream.toByteArray();
    }

    public static <T extends GeneratedMessageV3> List<T> parseByteArray(ByteBuf byteBuf, Function<ByteArrayInputStream, T> f) {
        T resp;
        List<T> result = new ArrayList<>();
        byte[] bytes;
        while ((bytes = IOUtils.toByteArrayWithoutConsuming(byteBuf)) != null) {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            resp = f.apply(inputStream);
            if (resp == null) {
                break;
            }
            result.add(resp);
        }
        return result;
    }
}
