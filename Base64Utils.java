package com.yigit.chat;

/**
 * Created by Mahmut on 27.06.2018.
 */
import android.support.annotation.VisibleForTesting;
import android.util.Base64;

public final class Base64Utils {
    public static byte[] decode(String encodedData) {
        if (encodedData == null) {
            return null;
        }
        return Base64.decode(encodedData, 0);
    }

    public static byte[] decodeUrlSafe(String encodedData) {
        if (encodedData == null) {
            return null;
        }
        return Base64.decode(encodedData, 10);
    }

    public static byte[] decodeUrlSafeNoPadding(String encodedData) {
        if (encodedData == null) {
            return null;
        }
        return Base64.decode(encodedData, 11);
    }

    public static byte[] decodeUrlSafeNoPadding(byte[] encodedData) {
        if (encodedData == null) {
            return null;
        }
        return Base64.decode(encodedData, 11);
    }

    @VisibleForTesting
    public static String encode(byte[] data) {
        if (data == null) {
            return null;
        }
        return Base64.encodeToString(data, 0);
    }

    public static String encodeUrlSafe(byte[] data) {
        if (data == null) {
            return null;
        }
        return Base64.encodeToString(data, 10);
    }

    public static String encodeUrlSafeNoPadding(byte[] data) {
        if (data == null) {
            return null;
        }
        return Base64.encodeToString(data, 11);
    }
}
