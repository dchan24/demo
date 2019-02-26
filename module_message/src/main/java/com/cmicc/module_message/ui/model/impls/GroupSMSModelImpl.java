package com.cmicc.module_message.ui.model.impls;


import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;

import com.chinamobile.app.yuliao_business.model.GroupNotify;
import com.chinamobile.app.yuliao_business.provider.Conversations;
import com.chinamobile.app.yuliao_business.util.Status;
import com.chinamobile.app.yuliao_business.util.Type;
import com.cmicc.module_message.ui.model.GroupSMSModel;

import java.util.Random;

/**
 * Created by yangshaowei on 2017/7/13.
 */

public class GroupSMSModelImpl implements GroupSMSModel, LoaderManager.LoaderCallbacks<Cursor> {

    private Context mContext;
    private GroupSMSModel.GroupSMSLoadFinishCallback mGroupSMSLoadFinishCallback;
    private int ID = new Random(System.currentTimeMillis()).nextInt();
    private String mAddress;


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String where = String.format("address = '%s' AND status = %s",mAddress,Status.STATUS_OK);
        return new CursorLoader(mContext, Conversations.GroupNotify.CONTENT_URI, new String[] { "_id", Type.TYPE_BOX_GROUP + " AS box_type", "address","body","date", "status" , GroupNotify.COLUMN_SENDEENAME}, where, null,  Conversations.DATE_ASC);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mGroupSMSLoadFinishCallback.onLoadFinished(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void loadGroupSMS(Context context, String address, LoaderManager loaderManager, GroupSMSLoadFinishCallback groupSMSLoadFinishCallback) {
        mContext = context;
        mGroupSMSLoadFinishCallback = groupSMSLoadFinishCallback;
        mAddress = address;
        loaderManager.initLoader(ID, null, this);
    }

    public void reloadGroupSMS(LoaderManager loaderManager){
        loaderManager.restartLoader(ID, null, this);
    }
}
