package com.cmicc.module_message.ui.presenter;

import android.content.Context;
import android.os.Bundle;

import com.app.module.proxys.modulemessage.MessageProxy;
import com.chinamobile.app.yuliao_business.model.ConvSearch;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.utils.RxAsyncHelper;
import com.cmcc.cmrcs.android.data.GlobalSearch;

import com.cmcc.cmrcs.android.ui.utils.NickNameUtils;
import com.cmicc.module_message.ui.activity.MessageSearchActivity;
import com.cmcc.cmrcs.android.ui.contracts.GlobalSearchBaseContract;
import com.constvalue.MessageModuleConst;

import java.util.ArrayList;

import rx.Subscription;
import rx.functions.Func1;

/**
 * @anthor situ
 * @time 2017/10/16 14:41
 * @description
 */

public class GlobalSearchMessagePresenter implements GlobalSearchBaseContract.IPresenter {

    private Context mContext;
    private GlobalSearch mGlobalSearch;
    private GlobalSearchBaseContract.IView mView;

    private Subscription mSubscription;

    public GlobalSearchMessagePresenter(Context context, GlobalSearchBaseContract.IView view) {
        mContext = context;
        mView = view;
        mGlobalSearch = new GlobalSearch();
    }

    @Override
    public void search(final String keyword) {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
        mSubscription = new RxAsyncHelper<>("").runInSingleFixThread(new Func1<Object, ArrayList>() {
            @Override
            public ArrayList call(Object s) {
                ArrayList<ConvSearch> list = mGlobalSearch.searchMessage(mContext, keyword);
                return list;
            }
        }).runOnMainThread(new Func1<ArrayList, Object>() {
            @Override
            public Object call(ArrayList list) {
                mView.notifyList(list, keyword);
                return null;
            }
        }).subscribe();
    }

    @Override
    public void itemClick(Object object, String searchKey) {
        if (object == null || !(object instanceof ConvSearch)) {
            return;
        }
        ConvSearch convSearch = (ConvSearch) object;
        int count = convSearch.count;
        String address = convSearch.address;
        String title = NickNameUtils.getPerson(mContext, convSearch.boxType, address);
        if (count > 1) {
            MessageSearchActivity.start(mContext, convSearch.boxType,address, searchKey, count, title);
        } else {

            String clzName = null;
            long loadTime = convSearch.date;

            Bundle bundle = new Bundle();
            if ((convSearch.boxType & Type.TYPE_BOX_MESSAGE) > 0) {
                clzName = MessageProxy.g.getServiceInterface().getClassName(MessageModuleConst.ClassType.MESSAGE_EDITOR__FRAGMENT_CLASS);
                bundle.putString("address", address);
                bundle.putString("person", title);
                bundle.putLong("loadtime", loadTime);
                bundle.putString("clzName", clzName);
            } else if ((convSearch.boxType & Type.TYPE_BOX_GROUP) > 0) {
                clzName = MessageProxy.g.getServiceInterface().getClassName(MessageModuleConst.ClassType.GROUP_CHAT_MESSAGE_FRAGMENT_CLASS);
                bundle.putString("address", address);
                bundle.putString("person", title);
                bundle.putLong("loadtime", loadTime);
                bundle.putString("clzName", clzName);
            }else if ((convSearch.boxType & Type.TYPE_BOX_SMS) > 0) {
                clzName = MessageProxy.g.getServiceInterface().getClassName(MessageModuleConst.ClassType.MSM_SMS_FRAGMENT_CLASS);
                bundle.putString("address", address);
                bundle.putString("person", title);
                bundle.putLong("loadtime", loadTime);
                bundle.putString("clzName", clzName);
            } else if ((convSearch.boxType & Type.TYPE_BOX_PC) > 0) {
                clzName = MessageProxy.g.getServiceInterface().getClassName(MessageModuleConst.ClassType.PC_MESSAGE_FRAGMENT_CLASS);
                bundle.putString("address", address);
                bundle.putString("person", title);
                bundle.putLong("loadtime", loadTime);
                bundle.putString("clzName", clzName);
            }
//            MessageProxy.g.getUiInterface().goMessageDetailActivity(mContext,bundle);;
            MessageProxy.g.getUiInterface().goMessageDetailActivity(mContext,bundle);
        }

    }
}
