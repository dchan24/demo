package com.cmicc.module_message.ui.model.impls;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.chinamobile.app.yuliao_business.provider.Conversations;
import com.chinamobile.app.yuliao_common.utils.Log;
import com.chinamobile.app.yuliao_common.utils.SharePreferenceUtils;
import com.cmcc.cmrcs.android.ui.model.ConvListModel;
import com.cmcc.cmrcs.android.ui.utils.GlobalConfig;
import com.cmcc.cmrcs.android.ui.utils.MulitCursorLoader;

import java.util.Random;

/**
 * Created by tigger on 2017/3/16.
 */

public class ConvListModelImpl {
    /*
     弃用

    implements ConvListModel, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "ConvListModelImpl";

    private Context mContext;
    private ConvListLoadFinishCallback mConvListLoadFinishCallback;

    private long beginTime;
    private static final int ID = new Random(System.currentTimeMillis()).nextInt();
    @Override
    public void loadCoversations(Context context, LoaderManager loaderManager, ConvListLoadFinishCallback convListLoadFinishCallback) {
        mContext = context;
        mConvListLoadFinishCallback = convListLoadFinishCallback;

        loaderManager.initLoader(ID, null, this);


    }

    @Override
    public void restartLoader(LoaderManager loaderManager) {
        loaderManager.restartLoader(ID, null, this);
    }

    //    long begin = 0;
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        beginTime = System.currentTimeMillis();

        String where = "box_type='1' OR box_type='8' OR box_type='16' OR box_type='4'";
//        CursorLoader loader = new CursorLoader(mContext, Conversations.Conversation.CONTENT_URI,
// new String[] { "type", "status", "date", "body", "send_address",
// "address", "unread_count", "box_type", "person" }, where, null, "date DESC");
        MulitCursorLoader loader = new MulitCursorLoader(mContext);

        //消息
        String[] msg_projection = new String[]{
                "type",
                "status",
                "box_type",
                "date",
                "body",
                "send_address",
                "thread_id",
                "address",
                "unread_count",
                "read",
                "person"
        };
        loader.addData(new MulitCursorLoader.Data(Conversations.Conversation.CONTENT_URI,
                msg_projection, where, null, "date DESC"));

        // platform 公众号
        String[] platfrom_projection = new String[] {
                "type",
                "status",
                "box_type",
                "date",
                "body",
                "send_address",
                "-1 AS thread_id",
                "address",
                "unread_count",
                "read",
                "person" };
        loader.addData(new MulitCursorLoader.Data(Conversations.PlatFormConversation.CONTENT_URI,
                platfrom_projection, null, null, "date DESC"));

        //短彩信接管开关
        if((boolean) SharePreferenceUtils.getDBParam(mContext, GlobalConfig.OPEN_SMS_STATUS, false)) {

            //短彩信（threads）
            String[] sms_projection = new String[]{
                    "type",
                    "snippet_cs AS status",//彩信：UTF-8为106，短信为0
                    "1024 AS box_type",
                    "date",
                    "snippet AS body",//最新更新的消息的内容（彩信为主题，短信为正文）
                    "recipient_ids AS send_address",//接收者（canonical_addresses表的id）列表，所有接收者以空格隔开
                    "_id AS thread_id",
                    "(SELECT address FROM canonical_addresses WHERE _id=recipient_ids) AS address",
                    "-1 AS unread_count",
                    "read",
                    "NULL as person"};
            // sms.threadId
        loader.addData(new MulitCursorLoader.Data(Conversations.SMS.CONTENT_URI_CONVERSATION,
                sms_projection, null, null, "date DESC"));

            //短信（sms）
            String[] projection = new String[]{

                    "type AS type",
                    "status AS status",
                    "1024 AS box_type",
                    "date AS date",
                    "body AS body",
                    "null AS send_address",
                    "thread_id AS thread_id",
                    "address AS address",
                    "-1 AS unread_count",
                    "read AS read",
                    "NULL AS person"
            };
//            Uri URI_SMS_CONVERSATION = Uri.parse("content://sms//conversations");
//            loader.addData(new MulitCursorLoader.Data(URI_SMS_CONVERSATION,
//                    projection, null, null, "date DESC"));
        }
        return loader;
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished time : " + (System.currentTimeMillis() - beginTime));
        mConvListLoadFinishCallback.onLoadFinished(data);
        Log.d(TAG, "onLoadFinished over time : " + (System.currentTimeMillis() - beginTime));
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        loader.reset();
    }

    */

}
