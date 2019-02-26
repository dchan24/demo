package com.cmicc.module_message.utils;

import com.cmicc.module_message.ui.listener.UpdateCallingViewListener;

public class CallViewListenerUtil {
    private static CallViewListenerUtil mInstance;

    public synchronized static CallViewListenerUtil getInstance(){
        if(mInstance == null){
            mInstance = new CallViewListenerUtil();
        }
        return mInstance;
    }

    private UpdateCallingViewListener mListener;

    public void setListener(UpdateCallingViewListener listener) {
        mListener = listener;
    }

    public UpdateCallingViewListener getmListener(){
        return mListener;
    }



}
