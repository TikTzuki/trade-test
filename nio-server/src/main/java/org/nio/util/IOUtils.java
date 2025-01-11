package org.nio.util;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.netty.ByteBufFlux;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Slf4j
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

    public  static  Flux<GeneratedMessageV3> deserializeMessages(ByteBufFlux byteBuf, GeneratedMessageV3.Builder message) {
        return byteBuf.map(buff -> {
            int size = getSize(buff);
            ByteBuf data = buff.readBytes(size);
            return data;
        }).map(it -> {
            byte[] bytes = new byte[it.readableBytes()];
            it.readBytes(bytes);
            try {
                return (GeneratedMessageV3) message
                        .mergeFrom(bytes)
                        .build();
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
    }

    public static <T extends GeneratedMessageV3> List<T> parseByteArray(ByteBuf byteBuf, Function<ByteArrayInputStream, T> f) {
        T resp;
        List<T> result = new ArrayList<>();
        byte[] bytes;
        log.info("Parsing byte array {}", IOUtils.getSize(byteBuf));
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

    public static int getSize(ByteBuf input) {
        int firstByte = input.readByte();
        if (firstByte == -1) {
            return -1;
        }
        try {
            return readRawVarint32(firstByte, input);
        } catch (IOException e) {
            return -1;
        }
    }

    public static int readRawVarint32(final int firstByte, final ByteBuf input) throws IOException {
        if ((firstByte & 128) == 0) {
            return firstByte;
        } else {
            int result = firstByte & 127;

            int offset;
            for (offset = 7; offset < 32; offset += 7) {
                int b = input.readByte();
                if (b == -1) {
                    throw new RuntimeException("Truncated message");
                }

                result |= (b & 127) << offset;
                if ((b & 128) == 0) {
                    return result;
                }
            }

            while (offset < 64) {
                int b = input.readByte();
                if (b == -1) {
                    throw new RuntimeException("Truncated message");
                }

                if ((b & 128) == 0) {
                    return result;
                }

                offset += 7;
            }

            throw new RuntimeException("Truncated message");
        }
    }
}
