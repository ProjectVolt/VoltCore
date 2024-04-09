package io.github.capure.voltcore.util;

import java.util.Base64;

public abstract class Base64Helper {
    public static String toBase64(String in) {
        if (in == null) return null;
        return Base64.getEncoder().encodeToString(in.getBytes());
    }

    public static String fromBase64(String in) {
        if (in == null) return null;
        return new String(Base64.getDecoder().decode(in));
    }
}
