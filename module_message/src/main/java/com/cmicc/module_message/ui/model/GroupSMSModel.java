package com.cmicc.module_message.ui.model;

import android.app.LoaderManager;
import android.content.Context;
import android.database.Cursor;

import com.cmcc.cmrcs.android.ui.model.BaseModel;


/**
 * Created by yangshaowei on 2017/7/13.
 */

public interface GroupSMSModel extends BaseModel {

    public interface GroupSMSLoadFinishCallback{
        public void onLoadFinished(Cursor cursor);
    }

    void loadGroupSMS(Context context, String address, LoaderManager loaderManager, GroupSMSModel.GroupSMSLoadFinishCallback groupSMSLoadFinishCallback);
}
