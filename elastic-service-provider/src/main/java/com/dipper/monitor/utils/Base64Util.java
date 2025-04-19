package com.dipper.monitor.utils;

import java.io.IOException;
import org.apache.commons.codec.binary.Base64;

public class Base64Util {
    private static Base64 base64 = new Base64();

    public Base64Util() {
    }

    public static String encode(byte[] bytes) {
        return new String(base64.encode(bytes));
    }

    public static byte[] decode(String text) throws IOException {
        return base64.decode(text.getBytes("UTF-8"));
    }
}
