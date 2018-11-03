package com.vitaviva.commongallery.serializable_callback.base;


import android.annotation.SuppressLint;

import java.io.Serializable;

public interface ICallbackBase extends Serializable {
    void onGetRemover(ICallbackRemover remover);

    interface ICallbackRemover {
        void removeCallback();
    }

    @SuppressLint("NewApi")
    default ICallbackBase setKey(int key){
        return null;
    }

}
