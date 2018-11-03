package com.vitaviva.commongallery.util;

import android.app.ActivityManager;
import android.content.Context;
import android.support.annotation.StringRes;
import android.widget.Toast;

import com.google.common.base.Strings;

import java.util.List;


public class ToastUtil {

    public static void toast(Context context, @StringRes int resId) {
        toastIgnoreEmpty(context, context.getString(resId), true);
    }

    public static void toast(Context context, String msg) {
        toastIgnoreEmpty(context, msg, true);
    }

    public static void toastLong(Context context, int msgId) {
        toastIgnoreEmpty(context, context.getString(msgId), false);
    }

    public static void toastLong(Context context, String msg) {
        toastIgnoreEmpty(context, msg, false);
    }

    private static void toastIgnoreEmpty(Context ctx, String msg, boolean isShort) {
        if (!Strings.isNullOrEmpty(msg)) {
            toast(ctx, msg, isShort);
        }
    }

    private static void toast(Context ctx, String msg, boolean isShort) {
        if (isAppOnForeground(ctx)) {
            Toast.makeText(ctx, msg, isShort ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG).show();
        }
    }

    private static boolean isAppOnForeground(Context applicationContext) {
        if (null == applicationContext) {
            return false;
        }
        ActivityManager activityManager = (ActivityManager) applicationContext.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null) {
            return false;
        }
        String packageName = applicationContext.getPackageName();

        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (CollectionUtil.isEmpty(appProcesses)) {
            return false;
        }

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }
}
