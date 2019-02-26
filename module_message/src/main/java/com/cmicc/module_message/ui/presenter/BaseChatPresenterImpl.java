package com.cmicc.module_message.ui.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.SparseBooleanArray;

import com.chinamobile.app.yuliao_business.logic.common.LogicActions;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_common.utils.Log;
import com.cmicc.module_message.ui.adapter.MessageChatListAdapter;
import com.cmcc.cmrcs.android.ui.logic.common.UIObserver;
import com.cmcc.cmrcs.android.ui.logic.common.UIObserverManager;
import com.cmcc.cmrcs.android.ui.model.MediaItem;
import com.cmicc.module_message.ui.constract.BaseChatContract;
import com.cmicc.module_message.ui.fragment.BaseChatFragment;
import com.constvalue.MessageModuleConst;
import com.juphoon.cmcc.app.lemon.MtcImConstants;

import java.util.ArrayList;

/**
 * Created by tigger on 2017/5/5.
 */

public class BaseChatPresenterImpl implements BaseChatContract.Presenter {

    private static final String TAG = BaseChatPresenterImpl.class.getSimpleName();
    private Context mContext;
    BaseChatContract.View mBaseChatView;

    public BaseChatPresenterImpl(Context context, BaseChatContract.View baseChatView){
        mContext = context;
        mBaseChatView = baseChatView;

        ArrayList<Integer> actions = new ArrayList<>();
        actions.add(LogicActions.GROUP_CHAT_REJECTED_CB);
        UIObserverManager.getInstance().registerObserver(mUIObserver, actions);
    }

    @Override
    public void start() {

    }

    @Override
    public void reSend(Message msg) {
        mBaseChatView.reSend(msg);
    }

    @Override
    public void sendWithdrawnMessage(Message msg) {

        mBaseChatView.sendWithdrawnMessage(msg);
    }

    @Override
    public void deleteMessage(Message msg) {
        mBaseChatView.deleteMessage(msg);
    }

    @Override
    public void deleteMultiMessage(SparseBooleanArray selectList) {
        mBaseChatView.deleteMultiMessage(selectList);
    }

    @Override
    public void forwardMultiMessage(SparseBooleanArray selectList) {
        mBaseChatView.forwardMultiMessage(selectList);
    }

    @Override
    public void addToFavorite(Message msg, int chatType, String address) {
        Log.e(TAG, "mBaseChatView:" + mBaseChatView.getClass().getSimpleName());
        mBaseChatView.addToFavorite(msg, chatType, address);
    }

    @Override
    public void sendSuperMessage(String msg) {
        Log.e(TAG, "mBaseChatView:" + mBaseChatView.getClass().getSimpleName());
        mBaseChatView.sendSuperMessage(msg);
    }

    /**
     * 系统消息
     * @param
     */
    @Override
    public void sysMessage( int type ) {
        mBaseChatView.sysMessage( type );
    }


    private UIObserver mUIObserver = new UIObserver() {
        @Override
        protected void onReceiveAction(int action, Intent intent) {
            switch (action){
                case LogicActions.GROUP_CHAT_REJECTED_CB:
                    int errorCode = intent.getIntExtra(LogicActions.GROUP_CHAT_ERROR_CODE, -1);
                    String groupID = intent.getStringExtra(LogicActions.GROUP_CHAT_ID );
                    if (errorCode == MtcImConstants.MTC_IM_ERR_LEAVED) {

                        if(mBaseChatView instanceof BaseChatFragment){
                            BaseChatFragment baseChatFragment = ((BaseChatFragment)mBaseChatView);
                            if(baseChatFragment.getChatType() == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT){
                                String adderss = baseChatFragment.getAddress();
                                if(!TextUtils.isEmpty(adderss) && !TextUtils.isEmpty(groupID) && adderss.equals(groupID)){
                                    Activity activity = baseChatFragment.getActivity();
                                    if(activity == null)
                                        break;
                                    activity.finish();
                                }
                            }
                        }
                    }
                    break;
            }
        }
    };

    @Override
    public void reSendImgAndVideo(ArrayList<MediaItem> list) {

    }

    @Override
    public void moveToOffsetEnd() {
        mBaseChatView.moveToOffsetEnd();
    }


}