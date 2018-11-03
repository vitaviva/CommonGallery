package com.vitaviva.commongallery.util;

import android.support.annotation.Nullable;
import android.text.TextUtils;


import com.orhanobut.logger.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class Reflector {
    private static final String TAG = "Reflector";

    public static final class TypedObject {
        private final Object   object;
        private final Class<?> type;

        public TypedObject(Object object, Class<?> type) {
            this.object = object;
            this.type = type;
        }

        Object getObject() {
            return object;
        }

        Class<?> getType() {
            return type;
        }
    }

    @Nullable
    public static Class<?> forNameSafe(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            Logger.e(TAG, e);
        }
        return null;
    }

    @Nullable
    public static Object constructorSafe(Object obj) {
        try {
            if (obj instanceof Class) {
                return ((Class<?>) obj).getConstructor().newInstance();
            } else if (obj instanceof String) {
                Class<?> clazz = Class.forName((String) obj);
                return clazz.getConstructor().newInstance();
            }
        } catch (Exception e) {
            Logger.e(TAG, e);
        }
        return null;
    }

    @Nullable
    public static Object constructorSafe(Object obj, TypedObject... arguments) {
        if (CollectionUtil.isEmpty(arguments)) {
            return constructorSafe(obj);
        }
        Class<?>[] classes = new Class[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            classes[i] = arguments[i].getType();
        }
        Object[] objects = new Object[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            objects[i] = arguments[i].getObject();
        }
        try {
            if (obj instanceof Class) {
                Constructor<?> constructor = ((Class<?>) obj).getDeclaredConstructor(classes);
                if (!constructor.isAccessible()) {
                    constructor.setAccessible(true);
                }
                return constructor.newInstance(objects);
            } else if (obj instanceof String) {
                Constructor<?> constructor = Class.forName((String) obj)
                        .getDeclaredConstructor(classes);
                if (!constructor.isAccessible()) {
                    constructor.setAccessible(true);
                }
                return constructor.newInstance(objects);
            }
        } catch (Exception e) {
            Logger.e(TAG, e);
        }
        return null;
    }

    @Nullable
    public static Object constructorSafe(Object obj, Object... constructParam) {
        if (CollectionUtil.isEmpty(constructParam)) {
            return constructorSafe(obj);
        }
        Class<?>[] classes = new Class[constructParam.length];
        for (int i = 0; i < constructParam.length; i++) {
            classes[i] = constructParam[i].getClass();
        }
        try {
            if (obj instanceof Class) {
                Constructor<?> constructor = ((Class<?>) obj).getDeclaredConstructor(classes);
                if (!constructor.isAccessible()) {
                    constructor.setAccessible(true);
                }
                return constructor.newInstance(constructParam);
            } else if (obj instanceof String) {
                Constructor<?> constructor = Class.forName((String) obj)
                        .getDeclaredConstructor(classes);
                if (!constructor.isAccessible()) {
                    constructor.setAccessible(true);
                }
                return constructor.newInstance(constructParam);
            }
        } catch (Exception e) {
            Logger.e(TAG, e);
        }
        return null;
    }

    @Nullable
    public static Object invokeMethodExceptionSafe(Object methodOwner, String method,
                                                   TypedObject... arguments) {
        if (null == methodOwner) {
            return null;
        }

        try {
            Class<?>[] types = null == arguments ? new Class<?>[0] : new Class[arguments.length];
            Object[] objects =
                    null == arguments ? new Object[0] : new Object[arguments.length];

            if (null != arguments) {
                for (int i = 0, limit = types.length; i < limit; i++) {
                    types[i] = arguments[i].getType();
                    objects[i] = arguments[i].getObject();
                }
            }

            Method declaredMethod = methodOwner.getClass().getDeclaredMethod(method, types);
            if (!declaredMethod.isAccessible()) {
                declaredMethod.setAccessible(true);
            }
            return declaredMethod.invoke(methodOwner, objects);
        } catch (Throwable e) {
            Logger.e(TAG, e);
        }
        return null;
    }

    @Nullable
    public static Object invokeStaticMethodExceptionSafe(
            String ownName, String invokeMethod,
            TypedObject... arguments) {
        if (TextUtils.isEmpty(ownName) || TextUtils.isEmpty(invokeMethod)) {
            return null;
        }
        try {
            Class<?>[] types = null == arguments ? new Class[0] : new Class[arguments.length];
            Object[] objects = null == arguments ? new Object[0] : new Object[arguments.length];

            if (null != arguments) {
                for (int i = 0, limit = types.length; i < limit; i++) {
                    types[i] = arguments[i].getType();
                    objects[i] = arguments[i].getObject();
                }
            }
            Class<?> clazz = Class.forName(ownName);
            Method method = clazz.getMethod(invokeMethod, types);
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            return method.invoke(null, objects);
        } catch (Throwable e) {
            Logger.e(TAG, e);
        }
        return null;
    }

    @Nullable
    public static Object getFieldSafe(Class<?> clazz, String fieldName, Object target) {
        try {
            return getField(clazz, fieldName, target);
        } catch (Exception e) {
            Logger.e(TAG, e);
        }
        return null;
    }

    private static Object getField(Class<?> clazz, String fieldName, Object target)
            throws NoSuchFieldException, IllegalAccessException {
        if (TextUtils.isEmpty(fieldName)) {
            return null;
        }
        Field field = clazz.getDeclaredField(fieldName);
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        return field.get(target);
    }

    @Nullable
    public static Field getFieldSafe(Class<?> clazz, String fieldName) {
        if (TextUtils.isEmpty(fieldName)) {
            return null;
        }
        try {
            Field field = clazz.getDeclaredField(fieldName);
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            return field;
        } catch (NoSuchFieldException e) {
            Logger.e(TAG, e);
        }
        return null;
    }

    @Nullable
    public static Method getMethodSafe(Class<?> clazz, String methodName, Class<?>... objects) {
        try {
            Method method = clazz.getDeclaredMethod(methodName, objects);
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            return method;
        } catch (NoSuchMethodException e) {
            Logger.e(TAG, e);
        }
        return null;
    }

    public static void setFieldSafe(Class<?> fieldClass, String fieldName, Object instance,
                                    Object value) {
        try {
            setField(fieldClass, fieldName, instance, value);
        } catch (Exception e) {
            Logger.e(TAG, e);
        }
    }

    private static void setField(Class<?> fieldClass, String fieldName, Object instance,
                                 Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = fieldClass.getDeclaredField(fieldName);
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        field.set(instance, value);
    }

    public static void setStaticFieldSafe(Class<?> fieldClass, String fieldName, Object value) {
        try {
            Field field = fieldClass.getDeclaredField(fieldName);
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            field.set(fieldClass, value);
        } catch (Exception e) {
            Logger.e(TAG, e);
        }
    }

    public static <T> Class<T> getGenericClassSafe(Class<?> clazz, int index) {
        Type superType = clazz.getGenericSuperclass();
        ParameterizedType paramType = (ParameterizedType) superType;
        return (Class<T>) paramType.getActualTypeArguments()[index];
    }
}
