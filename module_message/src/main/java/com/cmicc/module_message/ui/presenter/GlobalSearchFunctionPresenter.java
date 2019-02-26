package com.cmicc.module_message.ui.presenter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.app.module.proxys.modulemessage.MessageProxy;
import com.chinamobile.app.utils.RxAsyncHelper;
import com.chinamobile.app.yuliao_business.model.OAList;
import com.chinamobile.app.yuliao_business.util.Type;
import com.cmcc.cmrcs.android.data.GlobalSearch;
import com.cmcc.cmrcs.android.ui.contracts.GlobalSearchBaseContract;
import com.cmicc.module_message.ui.activity.MailMsgListActivity;
import com.cmicc.module_message.ui.activity.MailOAMsgListActivity;
import com.cmicc.module_message.ui.activity.MailOASummaryActivity;
import com.constvalue.MessageModuleConst;

import java.util.ArrayList;

import rx.Subscription;
import rx.functions.Func1;

/**
 * @anthor Long
 * @time 2018/10/17 16:20
 * @description
 */

public class GlobalSearchFunctionPresenter implements GlobalSearchBaseContract.IPresenter {

    private Context mContext;
    private GlobalSearch mGlobalSearch;
    private GlobalSearchBaseContract.IView mView;

    private Subscription mSubscription;

    public GlobalSearchFunctionPresenter(Context context, GlobalSearchBaseContract.IView view) {
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
                ArrayList<OAList> list = mGlobalSearch.searchFunction(mContext, keyword);
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
    public void itemClick(Object item, String searchKey) {
        if (item == null || !(item instanceof OAList)) {
            return;
        }
        OAList oaList = (OAList) item;
        int type = oaList.getType();
        if(type == Type.TYPE_BOX_PC) {
            //我的电脑
            String clzName = MessageProxy.g.getServiceInterface().getClassName(MessageModuleConst.ClassType.PC_MESSAGE_FRAGMENT_CLASS);
            Bundle bundle = new Bundle();
            bundle.putString("address", oaList.getAddress());
            bundle.putString("person", oaList.getPerson());
            bundle.putLong("loadtime", 0);
            bundle.putString("clzName", clzName);
//            MessageProxy.g.getUiInterface().goMessageDetailActivity(mContext,bundle);;
            MessageProxy.g.getUiInterface().goMessageDetailActivity(mContext,bundle);

        }else if(type == Type.TYPE_BOX_MAILASSISTANT){
            //139邮箱助手
            MailMsgListActivity.startMailMsgListActivity(mContext);

        }else if(type == Type.TYPE_BOX_OA  || type == Type.TYPE_BOX_MAIL_OA){
            //二级oa
            MailOAMsgListActivity.startActivity(mContext, oaList.getAddress(), oaList.getType());
        }else if(type == Type.TYPE_BOX_ONLY_ONE_LEVEL_OA){
            //一级oa
            Intent intent = new Intent(mContext ,MailOASummaryActivity.class);
            intent.putExtra("address" ,oaList.getAddress());
            intent.putExtra("send_address", oaList.getSendAddress());
            intent.putExtra("box_type", oaList.getBoxType());
            intent.putExtra("boxtype", oaList.getBoxType());
            mContext.startActivity(intent);
        }
    }
}
