package org.nio;

public class Test {
    public static void main(String[] args) {
        byte[] bytes = {(byte) 0xCF, (byte) 0x02}; // 0xCF, 0x02

        // Combine bytes and convert to integer
        int result = ((bytes[0] & 0xFF) << 8) | (bytes[1] & 0xFF);

        System.out.println("Result: " + result);  // Output: 335
    }
}
