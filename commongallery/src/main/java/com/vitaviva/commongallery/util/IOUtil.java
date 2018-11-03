package com.vitaviva.commongallery.util;

import com.orhanobut.logger.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class IOUtil {
    public static final String TAG = "IOUtil";

    public static boolean copyStream(InputStream is, OutputStream os, CopyListener listener, int bufferSize) throws IOException {
        int total = is.available();
        if (total <= 0) {
            total = 512000;
        }

        int current = 0;
        if (shouldStopCopy(listener, current, total)) {
            return false;
        } else {
            byte[] bytes = new byte[bufferSize];
            do {
                int count;
                if ((count = is.read(bytes, 0, bufferSize)) == -1) {
                    os.flush();
                    return true;
                }

                os.write(bytes, 0, count);
                current += count;
            } while (!shouldStopCopy(listener, current, total));

            return false;
        }
    }

    private static boolean shouldStopCopy(CopyListener listener, int current, int total) {
        if (listener != null) {
            boolean shouldContinue = listener.onBytesCopied(current, total);
            if (!shouldContinue && 100 * current / total < 75) {
                return true;
            }
        }
        return false;
    }

    public static void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                Logger.e(TAG, e);
            }
        }
    }

    public interface CopyListener {
        boolean onBytesCopied(int var1, int var2);
    }

}
