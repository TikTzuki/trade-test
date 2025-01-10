package org.nio.util;

import com.google.protobuf.GeneratedMessageV3;
import io.netty.buffer.ByteBuf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class IOUtils {
    public static byte[] toByteArrayWithoutConsuming(ByteBuf byteBuf) {
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
