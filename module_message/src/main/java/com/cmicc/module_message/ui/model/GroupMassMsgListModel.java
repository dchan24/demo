package com.cmicc.module_message.ui.model;

import android.app.LoaderManager;
import android.content.Context;
import android.database.Cursor;

import com.cmcc.cmrcs.android.ui.model.BaseModel;

public interface GroupMassMsgListModel extends BaseModel {

    public interface GroupMassMsgListLoadFinishCallback{
        public void onLoadFinished(Cursor cursor);
    }

    void loadGroupMassMsgList(Context context, LoaderManager loaderManager, GroupMassMsgListModel.GroupMassMsgListLoadFinishCallback groupMassMsgListLoadFinishCallback);
}
