package com.cmicc.module_message.ui.presenter;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.app.module.proxys.modulemessage.MessageProxy;
import com.chinamobile.app.yuliao_business.model.ConvSearch;
import com.chinamobile.app.yuliao_business.util.Type;
import com.cmcc.cmrcs.android.data.GlobalSearch;

import com.cmcc.cmrcs.android.ui.contracts.GlobalSearchContract;
import com.cmcc.cmrcs.android.ui.utils.NickNameUtils;
import com.cmicc.module_message.ui.activity.MessageSearchActivity;
import com.constvalue.MessageModuleConst;

import java.util.ArrayList;

/**
 * Created by situ on 2017/4/4.
 */

public class GlobalSearchPresenter implements GlobalSearchContract.IPresenter {

    private Context mContext;

    private GlobalSearch mGlobalSearch;

    private GlobalSearchContract.IView mView;

    public GlobalSearchPresenter(Context context, GlobalSearchContract.IView view) {

        mContext = context;
        mView = view;

        mGlobalSearch = new GlobalSearch();

        mGlobalSearch.setSearchCallBack(new GlobalSearch.SearchCallBack() {
            @Override
            public void notifySearchListChanged(ArrayList<ConvSearch> list, String searchKey) {
                if (list == null || list.size() == 0) {
                    mView.showTextHint(false);
                    if (TextUtils.isEmpty(searchKey)) {
                        mView.showEmptyView(false);
                    } else {
                        mView.showEmptyView(true);
                    }
                } else {
                    mView.showEmptyView(false);
                    mView.showTextHint(true);
                }
                mView.notifyList(list, searchKey);
            }
        });
    }

    @Override
    public void search(String keyword) {
        mGlobalSearch.search(mContext, keyword);

    }

    @Override
    public void itemClick(ConvSearch convSearch, String searchKey) {
        if (convSearch == null) {
            return;
        }
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
            }
//            MessageProxy.g.getUiInterface().goMessageDetailActivity(mContext,bundle);;
            MessageProxy.g.getUiInterface().goMessageDetailActivity(mContext,bundle);
        }
    }
}
