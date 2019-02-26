package com.cmicc.module_message.ui.model.impls;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.chinamobile.app.yuliao_business.provider.Conversations;
import com.chinamobile.app.yuliao_business.util.Type;
import com.cmicc.module_message.ui.model.GroupMassMsgListModel;

import java.util.Random;

public class GroupMassMsgListModelImpl implements GroupMassMsgListModel, LoaderManager.LoaderCallbacks<Cursor> {

    private Context context;
    private GroupMassMsgListModel.GroupMassMsgListLoadFinishCallback groupMassMsgListLoadFinishCallback;
    private int ID = new Random(System.currentTimeMillis()).nextInt();

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
//        String where = String.format("box_type = '%d' AND status = %s", Type.TYPE_BOX_GROUP_MASS, Status.STATUS_OK);
        String where = "box_type = " + Type.TYPE_BOX_GROUP_MASS + " and status = 2";
        return new CursorLoader(context, Conversations.Message.CONTENT_URI, new String[] { "_id", "send_address", "person", "body", "date", "status"},
                where, null,  Conversations.DATE_ASC);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        groupMassMsgListLoadFinishCallback.onLoadFinished(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    @Override
    public void loadGroupMassMsgList(Context context, LoaderManager loaderManager, GroupMassMsgListLoadFinishCallback groupMassMsgListLoadFinishCallback) {
        this.context = context;
        this.groupMassMsgListLoadFinishCallback = groupMassMsgListLoadFinishCallback;
        loaderManager.initLoader(ID, null, this);
    }

    public void reloadGroupSMS(LoaderManager loaderManager) {
        loaderManager.restartLoader(ID, null, this);
    }
}
