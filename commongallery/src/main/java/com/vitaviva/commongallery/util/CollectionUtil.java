package com.vitaviva.commongallery.util;


import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CollectionUtil {
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return null == map || map.isEmpty();
    }

    public static boolean isEmpty(Object[] array) {
        return null == array || array.length == 0;
    }

    public static boolean isEmpty(int[] array) {
        return null == array || array.length == 0;
    }

    public static boolean isEmpty(short[] array) {
        return null == array || array.length == 0;
    }

    public static boolean isEmpty(long[] array) {
        return null == array || array.length == 0;
    }

    public static boolean isEmpty(float[] array) {
        return null == array || array.length == 0;
    }

    public static boolean isEmpty(double[] array) {
        return null == array || array.length == 0;
    }

    public static boolean isEmpty(byte[] array) {
        return null == array || array.length == 0;
    }

    public static boolean isEmpty(char[] array) {
        return null == array || array.length == 0;
    }

    public static boolean isEmpty(boolean[] array) {
        return null == array || array.length == 0;
    }

    public static int getSize(Collection<?> collection) {
        return collection == null ? 0 : collection.size();
    }

    public static <T> int getSize(T[] array) {
        return array == null ? 0 : array.length;
    }

    public static <T> T getObj(List<T> list, int index) {
        return getSize(list) > index ? list.get(index) : null;
    }

    public static <T> T getObj(T[] array, int index) {
        return getSize(array) > index ? array[index] : null;
    }

    public static <T> boolean isInArray(T[] objects, T token) {
        if (null == objects || 0 == objects.length || null == token) {
            return false;
        }
        for (T item : objects) {
            if (item.equals(token)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isInArray(int[] objects, int token) {
        if (null == objects || 0 == objects.length) {
            return false;
        }
        for (int item : objects) {
            if (token == item) {
                return true;
            }
        }
        return false;
    }

    public static <T> int indexOf(T[] objects, T token) {
        if (null == objects || 0 == objects.length || null == token) {
            return -1;
        }
        for (int i = 0; i < objects.length; ++i) {
            if (objects[i].equals(token)) {
                return i;
            }
        }
        return -1;
    }

    public static <T> int lastIndexOf(T[] objects, T token) {
        if (null == objects || 0 == objects.length || null == token) {
            return -1;
        }
        for (int i = objects.length - 1; i >= 0; --i) {
            if (objects[i].equals(token)) {
                return i;
            }
        }
        return -1;
    }

    public static boolean isInArray(long[] objects, long token) {
        if (null == objects || 0 == objects.length) {
            return false;
        }
        for (long item : objects) {
            if (token == item) {
                return true;
            }
        }
        return false;
    }

}