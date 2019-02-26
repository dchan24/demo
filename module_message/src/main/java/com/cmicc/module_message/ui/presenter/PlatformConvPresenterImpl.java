package com.cmicc.module_message.ui.presenter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;

import com.app.module.proxys.modulemessage.MessageProxy;
import com.chinamobile.app.yuliao_business.model.Conversation;
import com.chinamobile.app.yuliao_business.util.PlatformUtils;
import com.cmicc.module_message.ui.activity.MessageDetailActivity;
import com.cmcc.cmrcs.android.ui.utils.ConvCache;
import com.cmicc.module_message.ui.constract.NotifySmsContract;
import com.constvalue.MessageModuleConst;

/**
 * Created by KSBK on 2018/5/9.
 */

public class PlatformConvPresenterImpl implements NotifySmsContract.Presenter, ConvCache.ConvCacheFinishCallback {
    private NotifySmsContract.View mView;
    private Context mContext;
    private LoaderManager mLoaderManager;

    public PlatformConvPresenterImpl(Context context, NotifySmsContract.View view, LoaderManager loaderManager){
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
        clzName =  MessageProxy.g.getServiceInterface().getClassName(MessageModuleConst.ClassType.PUBLIC_ACCOUNT_CHAT_FRAGMENT_CLASS);
        bundle.putString("name", conversation.getPerson());
        bundle.putString("address", conversation.getAddress());
        bundle.putString("clzName", clzName);
        bundle.putString("iconpath", PlatformUtils.getPlatformIcon(mContext, conversation.getAddress()));
        MessageProxy.g.getUiInterface().goMessageDetailActivity(context,bundle);;
    }

    @Override
    public ConvCache.CacheType getCacheType() {
        return ConvCache.CacheType.CT_PLATFORM;
    }

    @Override
    public void onLoadFinished(Cursor cursor) {

    }

    @Override
    public void notifyDatasetChanged() {
        mView.notifyDataSetChanged();
    }
}
