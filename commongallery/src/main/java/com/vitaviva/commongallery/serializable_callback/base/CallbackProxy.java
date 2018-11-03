package com.vitaviva.commongallery.serializable_callback.base;

import android.os.Build;

import com.google.common.reflect.Reflection;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;


public class CallbackProxy {
    public static <T extends ICallbackBase> T newProxy(Class<T> t, T o) {
        ProxyStore.sCallBack.put(o.hashCode(), o);
        final int key = o.hashCode();
        o.onGetRemover(() -> {
            if (ProxyStore.sCallBack.get(key) != null) {
                ProxyStore.sCallBack.remove(key);

            }
        });
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            return Reflection.newProxy(t, new MyHandle(o.hashCode()));
        }
        return null;
    }

    private static class MyHandle implements InvocationHandler, Serializable {
        private Integer key;

        MyHandle(Integer key) {
            this.key = key;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (ProxyStore.sCallBack.get(key) == null) {
                return null;
            }
            return method.invoke(ProxyStore.sCallBack.get(key), args);
        }
    }

    private static class ProxyStore {
        private static ConcurrentHashMap<Integer, Object> sCallBack = new ConcurrentHashMap<>();
    }
}
