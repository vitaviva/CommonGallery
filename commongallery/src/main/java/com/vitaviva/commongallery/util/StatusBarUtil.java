package com.vitaviva.commongallery.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class StatusBarUtil {
    private static final String STATUS_BAR_HEIGHT_RES_NAME = "status_bar_height";
    private static final String NAVIGATION_BAR_HEIGHT_RES_NAME = "navigation_bar_height";
    private final Window window;

    public StatusBarUtil(Window window) {
        this.window = window;
    }

    public void fullScreen() {
        fullScreenOnBehindJellyBean();
        fullScreenOnOverJellyBean();
    }


    //    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void fullScreenOnOverJellyBean() {
        if (OsVersionUtils.hasJellyBean()) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    private void fullScreenOnBehindJellyBean() {
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }


    public static void setWindowImmersive(Window window) {
        if (OsVersionUtils.hasLollipop()) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.setStatusBarColor(Color.TRANSPARENT);
            }
        } else if (OsVersionUtils.hasKitKat()) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    public static void setStatusBarColor(Window window,@ColorInt int color) {
        if (OsVersionUtils.hasLollipop()) {
            window.setStatusBarColor(color);
        }
    }


    public static void fullScreen(Window window) {
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (OsVersionUtils.hasJellyBean()) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    public static void unfullScreen(Window window) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (OsVersionUtils.hasJellyBean()) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

    /**
     * 获取状态栏高度，如果获取失败返回-1
     */
    public static int getStatusBarHeight(Context context) {
        return getInternalDimensionSize(context, STATUS_BAR_HEIGHT_RES_NAME);
    }

    public static int getNavigationBarHeight(Context context) {
        return getInternalDimensionSize(context, NAVIGATION_BAR_HEIGHT_RES_NAME);
    }

    private static int getInternalDimensionSize(Context context, String key) {
        Resources resources = context.getResources();
        int result = -1;
        int resourceId = resources.getIdentifier(key, "dimen", "android");
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static boolean isTranslucentStatus(Activity activity) {
        boolean translucentStatus = false;
        if (OsVersionUtils.hasKitKat()) {
            int[] attrs = {android.R.attr.windowTranslucentStatus};
            TypedArray a = activity.obtainStyledAttributes(attrs);
            try {
                translucentStatus = a.getBoolean(0, false);
            } finally {
                a.recycle();
            }

            Window win = activity.getWindow();
            WindowManager.LayoutParams winParams = win.getAttributes();
            int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            if (OsVersionUtils.hasLollipop()) {
                if ((winParams.flags & bits) != 0 || (winParams.flags & bits) == 0 && win.getStatusBarColor() == Color.TRANSPARENT) {
                    translucentStatus = true;
                }
            } else {
                if ((winParams.flags & bits) != 0) {
                    translucentStatus = true;
                }
            }
        }
        return translucentStatus;
    }
}
