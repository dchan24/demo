package com.cmicc.module_message.ui.presenter;


import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;

import com.app.module.proxys.modulemessage.MessageProxy;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_business.provider.Conversations;
import com.chinamobile.app.yuliao_business.util.ConversationUtils;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.utils.StringUtil;
import com.chinamobile.app.yuliao_core.util.NumberUtils;

import com.cmcc.cmrcs.android.ui.utils.MulitCursorLoader;
import com.cmcc.cmrcs.android.ui.utils.NickNameUtils;
import com.cmicc.module_message.ui.activity.MessageSearchActivity;
import com.cmicc.module_message.ui.constract.MessageSearchContract;
import com.constvalue.MessageModuleConst;

import java.util.Random;

/**
 * Created by situ on 2017/3/30.
 */

public class MessageSearchPresenter implements MessageSearchContract.IPresenter, LoaderManager.LoaderCallbacks<Cursor> {

    public static final int LOADER_ID = new Random(System.currentTimeMillis()).nextInt();

    private MessageSearchContract.IView mView;
    private Context mContext;
    private LoaderManager mLoaderManager;

    private String mAddress;
    private int mBoxType;

    private String mKeyword;
    private String mSearchContent;

    public MessageSearchPresenter(Context context, MessageSearchContract.IView view, LoaderManager loaderManager, Bundle bundle) {
        mContext = context;
        mLoaderManager = loaderManager;
        mView = view;
        mAddress = bundle.getString(MessageSearchActivity.BUNDLE_KEY_ADDRESS);
        mBoxType = bundle.getInt(MessageSearchActivity.BUNDLE_KEY_BOXTYPE);

        String keyword = bundle.getString(MessageSearchActivity.BUNDLE_KEY_KEYWORD);
        if (keyword != null) {
            int count = bundle.getInt(MessageSearchActivity.BUNDLE_KEY_COUNT);
            String title = bundle.getString(MessageSearchActivity.BUNDLE_KEY_TITLE);
            mView.switchToStaticMode(count, keyword, title);
            searchKeyword(keyword);
        }
    }

    @Override
    public void start() {
        mLoaderManager.initLoader(LOADER_ID, null, this);
    }

    @Override
    public void searchKeyword(String keyword) {
        mKeyword = keyword;
        mLoaderManager.restartLoader(LOADER_ID, null, this);
    }

    @Override
    public void openItem(Message message) {
        String address = message.getAddress();
        String title = NickNameUtils.getPerson(mContext, mBoxType, address);
        long loadTime = message.getDate();
        String clzName = null;
        Bundle bundle = new Bundle();
        if ((mBoxType & Type.TYPE_BOX_MESSAGE) > 0) {
            clzName = MessageProxy.g.getServiceInterface().getClassName(MessageModuleConst.ClassType.MESSAGE_EDITOR__FRAGMENT_CLASS);
            if(address.equals(ConversationUtils.addressPc)){
                clzName = MessageProxy.g.getServiceInterface().getClassName(MessageModuleConst.ClassType.PC_MESSAGE_FRAGMENT_CLASS);
            }
            bundle.putString("address", address);
            bundle.putString("person", title);
            bundle.putLong("loadtime", loadTime);
            bundle.putString("clzName", clzName);
        } else if ((mBoxType & Type.TYPE_BOX_GROUP) > 0) {
            clzName = MessageProxy.g.getServiceInterface().getClassName(MessageModuleConst.ClassType.GROUP_CHAT_MESSAGE_FRAGMENT_CLASS);
            bundle.putString("address", address);
            bundle.putString("person", title);
            bundle.putLong("loadtime", loadTime);
            bundle.putString("clzName", clzName);
        }else if ((mBoxType & Type.TYPE_BOX_SMS) > 0 || (mBoxType & Type.TYPE_BOX_MMS) > 0){
            clzName = MessageProxy.g.getServiceInterface().getClassName(MessageModuleConst.ClassType.MSM_SMS_FRAGMENT_CLASS);
            bundle.putString("address", address);
            bundle.putString("person", title);
            bundle.putLong("loadtime", loadTime);
            bundle.putString("clzName", clzName);
        } else if ((mBoxType & Type.TYPE_BOX_PC) > 0) {
            clzName = MessageProxy.g.getServiceInterface().getClassName(MessageModuleConst.ClassType.PC_MESSAGE_FRAGMENT_CLASS);
            bundle.putString("address", address);
            bundle.putString("person", title);
            bundle.putLong("loadtime", loadTime);
            bundle.putString("clzName", clzName);
        }
//        MessageProxy.g.getUiInterface().goMessageDetailActivity(mContext,bundle);;
        MessageProxy.g.getUiInterface().goMessageDetailActivity(mContext,bundle);
    }

    /**
     * 转义特殊符号：例如%,不转义会导致查询到所有的结果
     * @param keyWord String
     * @return String
     */
    private static String sqliteEscape(CharSequence keyWord){
        if(TextUtils.isEmpty(keyWord) && !(keyWord instanceof String)){
            return "";
        }
        String key = ((String) keyWord);
        key = key.replace("/", "//");
        key = key.replace("'", "''");
        key = key.replace("[", "/[");
        key = key.replace("]", "/]");
        key = key.replace("%", "/%");
        key = key.replace("&","/&");
        key = key.replace("_", "/_");
        key = key.replace("(", "/(");
        key = key.replace(")", "/)");
        return key;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        mSearchContent = mKeyword;
        String where = buildSearch(mSearchContent);
        String msgWhere = "";
        if (!StringUtil.isEmpty(mAddress)) {
            if (mBoxType == Type.TYPE_BOX_GROUP) {
                msgWhere = String.format(Conversations.WHERE_ADDRESS_GROUP, mAddress);
            } else {
                msgWhere = Conversations.buildWhereAddress(NumberUtils.getPhone(mAddress));
            }
            msgWhere = msgWhere + " AND " + where;
        }

        String order = Conversations.DATE_DESC;
        MulitCursorLoader loader = new MulitCursorLoader(mContext, true);
        // 加载消息
        if (mBoxType == Type.TYPE_BOX_GROUP) {
            return new CursorLoader(mContext, Conversations.Group.CONTENT_URI, new String[] { "_id", Type.TYPE_BOX_GROUP + " AS box_type", "msg_id", "address", "send_address", "person", "body",
                    "date", "type", "status", "locked", "error_code", "ext_url", "ext_title", "ext_file_name", "ext_file_path", "ext_size_descript", "ext_file_size", "ext_down_size", "ext_thumb_path",
                    "thread_id", "read", "ext_file_size", "ext_down_size", "ext_short_url", "seen" }, msgWhere, null, order);

        } else {
            loader.addData(new MulitCursorLoader.Data(Conversations.Message.CONTENT_URI, new String[]{"_id", Type.TYPE_BOX_MESSAGE + " AS box_type", "msg_id", "thread_id", "address", "person",
                    "body", "date", "type", "status", "read", "locked", "error_code", "ext_url", "ext_short_url", "ext_title", "ext_file_name", "ext_file_path", "ext_thumb_path", "ext_size_descript",
                    "ext_file_size", "ext_down_size", "seen", "send_address"}, msgWhere, null, order));
        }

        return loader;
    }



    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() == 0) {
            mView.showTextHint(false);
            if (!TextUtils.isEmpty(mSearchContent)) {
                mView.showEmptyView(true);
                mView.showOhterFileSearchView(false);
            } else {
                mView.showEmptyView(false);
                mView.showOhterFileSearchView(true);
            }
        } else {
            mView.showOhterFileSearchView(false);
            mView.showTextHint(true);
            mView.showEmptyView(false);
        }

        mView.updateResultListView(data, mSearchContent, mBoxType);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        loader.reset();
    }

    /**
     * 生成查找的where
     *
     * @param body
     * @return
     */
    public String buildSearch(String body) {
        if (TextUtils.isEmpty(mSearchContent)) {
            return "1=2";
        }
        body = body.replace("'", "");
        //短信:type = 210 ,通知类短信:type = 321, pc抄送消息：type = 147456
        return String.format(Conversations.WHERE_BODY, sqliteEscape(body)) + " AND ( ( type<>3 AND type>0 AND type<10 ) OR type = 210 OR type = 321 OR type = 147456) ";

    }

}
