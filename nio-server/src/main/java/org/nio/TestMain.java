package org.nio;

public class TestMain {
    public static void main(String[] args) {
        byte[] bytes = {(byte) 0xFF, (byte) 0xFF, (byte) 0xCF, (byte) 0x02}; // 0xCF, 0x02

//        ByteBuffer wrapped = ByteBuffer.wrap(bytes); // big-endian by default
//        short num = wrapped.getShort(); // 1

        System.out.println("Result: " + fromByteArray(bytes));  // Output: 335
    }

    static int fromByteArray(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8) |
                ((bytes[3] & 0xFF));
    }
}
