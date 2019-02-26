package com.cmicc.module_message.ui.presenter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;

import com.app.module.proxys.modulemessage.MessageProxy;
import com.chinamobile.app.yuliao_business.model.Conversation;
import com.chinamobile.app.yuliao_business.util.Type;
import com.cmicc.module_message.ui.activity.MessageDetailActivity;
import com.cmcc.cmrcs.android.ui.utils.ConvCache;
import com.cmicc.module_message.ui.constract.NotifySmsContract;
import com.constvalue.MessageModuleConst;

/**
 * Created by tigger on 2017/7/27.
 */

public class NotifySmsPresenterImpl implements NotifySmsContract.Presenter, ConvCache.ConvCacheFinishCallback {
    private NotifySmsContract.View mView;
    private Context mContext;
    private LoaderManager mLoaderManager;

    public NotifySmsPresenterImpl(Context context, NotifySmsContract.View view, LoaderManager loaderManager){
        mContext = context;
        mView = view;
        mLoaderManager = loaderManager;
//        ConvCache.getInstance().setConvCacheFinishCallback2(this);
    }

    @Override
    public void start() {
        ConvCache.getInstance().initSecondLoader(mContext, mLoaderManager, this);
    }

    @Override
    public void openItem(Context context, Conversation conversation) {


        String clzName = null;
        Bundle bundle = new Bundle();

        clzName = MessageProxy.g.getServiceInterface().getClassName(MessageModuleConst.ClassType.MESSAGE_EDITOR__FRAGMENT_CLASS);
        bundle.putString("address", conversation.getAddress());
        bundle.putString("thread_id", conversation.getThreadId());
        bundle.putString("person", conversation.getPerson());
        boolean isSlient = conversation.getSlientDate() > 0;
        bundle.putBoolean("slient", isSlient);
        bundle.putLong("loadtime", 0);
        bundle.putString("clzName", clzName);
        if (conversation.getType() == Type.TYPE_MSG_TEXT_DRAFT) {
            bundle.putString("draft", conversation.getBody());
        }
        bundle.putInt("unread", conversation.getUnReadCount());
        MessageProxy.g.getUiInterface().goMessageDetailActivity(context,bundle);;
    }

    @Override
    public ConvCache.CacheType getCacheType() {
        return ConvCache.CacheType.CT_NOTIFY;
    }

    @Override
    public void onLoadFinished(Cursor cursor) {

    }

    @Override
    public void notifyDatasetChanged() {
        mView.notifyDataSetChanged();
    }

}
