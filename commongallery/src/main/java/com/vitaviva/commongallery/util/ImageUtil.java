package com.vitaviva.commongallery.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.opengl.GLES10;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;

import com.orhanobut.logger.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

/**
 * [feature][change-localpath][F] 更改存储路径，增加域ID和服务器ID
 */
public class ImageUtil {
    private static final String TAG = "ImageUtil";
    private static final int UNCONSTRAINED = -1;

    public enum Compress {
        MAX("max", 1080, 1080 << 11),
        MEDIUM("medium", 200, 768 << 9),
        SMALL("small", 96, 768 << 9);

        int minSideLength;
        int maxNumOfPixels;
        String name;

        Compress(String name, int minSideLength, int maxNumOfPixels) {
            this.minSideLength = minSideLength;
            this.maxNumOfPixels = maxNumOfPixels;
            this.name = name;
        }
    }

    /**
     * 存放拷贝图片后，生成resid 与文件名对应的一个缓存
     */
    private static final Map<String, String> resMap = new HashMap<>();

    private static void putRes(String name, String res) {
        resMap.put(name, res);
    }

    public static void putRes(String file, boolean isCompress, String resId) {
        putRes(file + isCompress, resId);
    }

    private static String getRes(String name) {
        return resMap.get(name);
    }

    public static String getRes(String filePath, boolean isCompress) {
        return getRes(filePath + isCompress);
    }


    /**
     * 限制高度并旋转
     *
     * @param bmap      原图片
     * @param maxHeight 最大高度
     * @param degrees   旋转角度
     */
    public static Bitmap limitHeightRotate(Bitmap bmap, int maxHeight,
                                           float degrees) {
        boolean change = false;
        int h = bmap.getHeight();
        int w = bmap.getWidth();
        Matrix matrix = new Matrix();
        if (h > maxHeight) {
            float scale = (float) maxHeight / h;
            if (degrees % 180 != 0) {
                scale = (float) maxHeight / w;
            }
            matrix.postScale(scale, scale);
            change = true;
        }
        if (degrees != 0) {
            matrix.postRotate(degrees);
            change = true;
        }
        if (change) {
            bmap = Bitmap.createBitmap(bmap, 0, 0, w, h, matrix, true);
        }
        return bmap;
    }

    public static Bitmap rotate(Bitmap bmap, float degrees) {
        boolean change = false;
        int h = bmap.getHeight();
        int w = bmap.getWidth();
        Matrix matrix = new Matrix();
        if (degrees != 0) {
            matrix.postRotate(degrees);
            change = true;
        }
        if (change) {
            bmap = Bitmap.createBitmap(bmap, 0, 0, w, h, matrix, true);
        }
        return bmap;
    }

    /**
     * 获取图片的旋转角度
     *
     * @param imagePath 图片路径
     */
    public static float getRotateDegrees(Context context, Uri imagePath) {
        float degrees = 0.0f;
        if (TextUtils.equals("content", imagePath.getScheme())) {
            String[] projection = {Images.ImageColumns.ORIENTATION};
            android.database.Cursor c = context.getContentResolver().query(imagePath,
                    projection, null, null, null);
            if (c != null) {
                try {
                    if (c.moveToFirst()) {
                        degrees = c.getInt(0);
                    }
                } finally {
                    c.close();
                }
            }
        } else if (TextUtils.equals("file", imagePath.getScheme())) {
            try {
                ExifInterface exif = new ExifInterface(imagePath.getPath());
                int orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);
                switch (orientation) {
                    case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                        break;
                    case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                        break;
                    case ExifInterface.ORIENTATION_NORMAL:
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degrees = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degrees = 270;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degrees = 90;
                        break;
                    case ExifInterface.ORIENTATION_TRANSPOSE:
                        break;
                    case ExifInterface.ORIENTATION_TRANSVERSE:
                        break;
                    default:
                        break;
                }
            } catch (IOException ignored) {
            }
        }
        return degrees;
    }

    /**
     * 把drawable转换为bitmap
     *
     * @param drawable drawable格式文件
     */
    @Nullable
    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        Bitmap bitmap = Bitmap
                .createBitmap(
                        drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(),
                        drawable.getOpacity() == PixelFormat.OPAQUE ? Bitmap.Config.RGB_565
                                : Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable
                .getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static byte[] drawable2Bytes(Drawable drawable) {

        if (drawable == null) {
            return null;
        }
        Bitmap bitmap = compressImage(drawableToBitmap(drawable));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    public static Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 200) { // 循环判断如果压缩后图片是否大于200kb,大于继续压缩
            baos.reset();// 重置baos即清空baos
            image.compress(CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;// 每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    /**
     * 保存图片到本地，并获得路径
     */
    @Nullable
    public static File saveimgfile(Bitmap bmp, File PHOTO_DIR, String filename) {
        if (bmp == null) {
            return null;
        }
        File file = null;
        OutputStream raf = null;
        try {
            if (!PHOTO_DIR.exists()) {
                PHOTO_DIR.mkdirs();// 创建照片的存储目录
            }
            file = new File(PHOTO_DIR, filename);
            file.createNewFile();

            raf = new BufferedOutputStream(new FileOutputStream(file));
            bmp.compress(CompressFormat.JPEG, 100, raf);
            raf.flush();
        } catch (Exception e) {
            file = null;
            Logger.e(TAG, e);
        } finally {
            IOUtil.closeSilently(raf);
        }
        return file;
    }

    public static File copyFile(File src, File PHOTO_DIR, String filename) {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            if (src.isFile()) {
                if (!PHOTO_DIR.exists()) {
                    PHOTO_DIR.mkdirs();// 创建照片的存储目录
                }
                File tar = new File(PHOTO_DIR, filename);
                if (tar.getAbsolutePath().equals(src.getAbsolutePath())) {
                    return src;
                }
                InputStream is = new FileInputStream(src);
                bis = new BufferedInputStream(is);
                OutputStream op = new FileOutputStream(tar);
                bos = new BufferedOutputStream(op);
                byte[] bt = new byte[8192];
                int len = bis.read(bt);
                while (len != -1) {
                    bos.write(bt, 0, len);
                    len = bis.read(bt);
                }
                bos.flush();
                return tar;
            }
        } catch (Exception e) {
            Logger.e(TAG, e);
        } finally {
            IOUtil.closeSilently(bis);
            IOUtil.closeSilently(bos);
        }
        return null;
    }


    public static StringBuilder getExportFileName(){
        return new StringBuilder("export").append(System.currentTimeMillis());
    }

    /**
     * 用当前时间给取得的图片命名
     */
    private static String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "'IMG'_yyyyMMdd_HHmmss");
        return dateFormat.format(date) + ".jpg";
    }

    public static String getImageFilePath(Context context) {
        String dir = getSDPath();
        String element = "image";
        if (null == dir) {
            dir = context.getFilesDir() + "/sd_no_found";
            element = "";
        } else {
            dir += "/img";
        }
        return dir + File.separator + element;
    }

    private static String getPhotoFileNameWithUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid + ".jpg";
    }

    private static String getSDPath() {
        return Environment.getExternalStorageDirectory().toString();
    }

    public static int computeSampleSize(BitmapFactory.Options options,
                                        int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options,
                                                int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = maxNumOfPixels == UNCONSTRAINED ? 1 :
                (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = minSideLength == UNCONSTRAINED ? 128 :
                (int) Math.min(Math.floor(w / minSideLength),
                        Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            return lowerBound;
        }

        if (maxNumOfPixels == UNCONSTRAINED &&
                minSideLength == UNCONSTRAINED) {
            return 1;
        } else if (minSideLength == UNCONSTRAINED) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    public static Bitmap getBmpFromFile(File file) {
        try {
            return getBmpFromByteDealOOM(is2byte(new FileInputStream(file)));
        } catch (Exception e) {
            Logger.e(TAG, e);
        }
        return null;
    }

    @Nullable
    private static byte[] is2byte(InputStream inStream) {
        if (inStream == null) {
            return null;
        }
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = null;
        try {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            data = outStream.toByteArray();
        } catch (IOException e) {
            Logger.e(TAG, e);
        } finally {
            IOUtil.closeSilently(outStream);
            IOUtil.closeSilently(inStream);
        }
        return data;
    }

    @Nullable
    public static Bitmap getBmpFromByteDealOOM(byte[] data) {
        if (data == null) {
            return null;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        Bitmap bmp;
        while (true) {
            try {
                bmp = BitmapFactory.decodeByteArray(data, 0, data.length,
                        options);
                break;
            } catch (OutOfMemoryError e) {
                Logger.e(TAG, e);
                options.inSampleSize <<= 1;
            }
        }
        return bmp;
    }


    @Nullable
    public static Pair<Double, Double> getLatLngFromImage(Uri uri) {
        if (uri == null) {
            return null;
        }
        try {
            ExifInterface exifInterface = new ExifInterface(uri.getPath());
            String latValue = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            String latRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
            String lngValue = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            String lngRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);

            if (latValue != null && latRef != null && lngValue != null && lngRef != null) {
                Double lat = convertRationalLatLonToFloat(latValue, latRef);
                Double lng = convertRationalLatLonToFloat(lngValue, lngRef);
                if (lat != null && lng != null) {
                    return new Pair<>(lat, lng);
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, e);
        }
        return null;
    }

    public static Pair<Integer, Integer> getImageWidthHeight(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        return new Pair<>(options.outWidth, options.outHeight);
    }


    private static Double convertRationalLatLonToFloat(String rationalString, String ref) {
        try {
            String[] parts = rationalString.split(",");

            String[] pair = parts[0].split("/");
            int degrees = (int) (Double.parseDouble(pair[0].trim())
                    / Double.parseDouble(pair[1].trim()));


            pair = parts[1].split("/");
            int minutes = (int) (Double.parseDouble(pair[0].trim())
                    / Double.parseDouble(pair[1].trim()));


            pair = parts[2].split("/");
            double seconds = Double.parseDouble(pair[0].trim())
                    / Double.parseDouble(pair[1].trim());


            double result = degrees + minutes / 60.0d + seconds / (60.0F * 60.0F);
            if ("S".equals(ref) || "W".equals(ref)) {
                return -result;
            }
            return result;
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    public static String getTimeFromImage(Uri uri) {
        if (uri == null) {
            return null;
        }
        try {
            ExifInterface exifInterface = new ExifInterface(uri.getPath());
            return exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
        } catch (IOException e) {
            Logger.e(TAG, e);
        }
        return null;
    }

    /**
     * 获取硬件加速最大质量（超过该值关闭硬件加速）
     */
    public static int getGLESTextureLimit() {
        if (OsVersionUtils.hasLollipop()) {
            EGL10 egl = (EGL10) EGLContext.getEGL();
            EGLDisplay dpy = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
            int[] vers = new int[2];
            egl.eglInitialize(dpy, vers);
            int[] configAttr = {
                    EGL10.EGL_COLOR_BUFFER_TYPE, EGL10.EGL_RGB_BUFFER,
                    EGL10.EGL_LEVEL, 0,
                    EGL10.EGL_SURFACE_TYPE, EGL10.EGL_PBUFFER_BIT,
                    EGL10.EGL_NONE
            };
            EGLConfig[] configs = new EGLConfig[1];
            int[] numConfig = new int[1];
            egl.eglChooseConfig(dpy, configAttr, configs, 1, numConfig);
            EGLConfig config = configs[0];
            int[] surfAttr = {
                    EGL10.EGL_WIDTH, 64,
                    EGL10.EGL_HEIGHT, 64,
                    EGL10.EGL_NONE
            };
            EGLSurface surf = egl.eglCreatePbufferSurface(dpy, config, surfAttr);
            int EGL_CONTEXT_CLIENT_VERSION = 0x3098;  // missing in EGL10
            int[] ctxAttrib = {
                    EGL_CONTEXT_CLIENT_VERSION, 1,
                    EGL10.EGL_NONE
            };
            EGLContext ctx = egl.eglCreateContext(dpy, config, EGL10.EGL_NO_CONTEXT, ctxAttrib);
            egl.eglMakeCurrent(dpy, surf, surf, ctx);
            int[] maxSize = new int[1];
            GLES10.glGetIntegerv(GLES10.GL_MAX_TEXTURE_SIZE, maxSize, 0);
            egl.eglMakeCurrent(dpy, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE,
                    EGL10.EGL_NO_CONTEXT);
            egl.eglDestroySurface(dpy, surf);
            egl.eglDestroyContext(dpy, ctx);
            egl.eglTerminate(dpy);
            return maxSize[0];
        } else {
            int[] maxSize = new int[1];
            GLES10.glGetIntegerv(GLES10.GL_MAX_TEXTURE_SIZE, maxSize, 0);
            return maxSize[0];
        }
    }
}