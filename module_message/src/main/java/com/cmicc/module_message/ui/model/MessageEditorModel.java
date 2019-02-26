package com.cmicc.module_message.ui.model;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;

import com.chinamobile.app.yuliao_business.model.Message;
import com.cmcc.cmrcs.android.ui.model.BaseModel;

import java.util.ArrayList;

/**
 * Created by ksk on 2017/3/22.
 */

public interface MessageEditorModel extends BaseModel {
    public interface MessageEditorLoadFinishCallback{
        public void onLoadFinished(int loadType, int searchPos, Bundle bundle);
    }

    void loadMessages(Context context, String address, long loadStartTime, long updateTime, LoaderManager loaderManager, ArrayList<Message> list, MessageEditorModel.MessageEditorLoadFinishCallback messageChatListLoadFinishCallback);
    void loadMessages(Context context, int firstLoadNum, String address, long loadTime, LoaderManager loaderManager, MessageEditorModel.MessageEditorLoadFinishCallback messageChatListLoadFinishCallback);
    void loadMoreMessages(LoaderManager loaderManager);
}
