package com.cmicc.module_message.ui.presenter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;

import com.chinamobile.app.yuliao_business.logic.common.LogicActions;
import com.chinamobile.app.yuliao_common.utils.Log;
import com.cmcc.cmrcs.android.ui.control.GroupChatControl;
import com.cmcc.cmrcs.android.ui.logic.common.UIObserver;
import com.cmcc.cmrcs.android.ui.logic.common.UIObserverManager;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.activity.GroupCardActivity;
import com.cmicc.module_message.ui.constract.GroupCardContract;

import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * @anthor situ
 * @time 2017/6/5 11:21
 * @description 修改群名片界面
 */

public class GroupCardPresenter implements GroupCardContract.IPresenter, TextWatcher {

    private static final String TAG = "GroupCardPresenter";

    private Context mContext;
    private GroupCardContract.IView mView;

    private String mAddress = "";//groupId

    private String mName = "";
    private CharSequence mText;
    private static int mMaxLength = 30;
    private int iStart = 0;
    private int iCount = 0;

    public GroupCardPresenter(Context context, GroupCardContract.IView view, Bundle bundle) {
        mContext = context;
        mView = view;
        init(bundle);

        ArrayList<Integer> actions = new ArrayList<Integer>();
        actions.add(LogicActions.GROUP_CHAT_MODIFY_CARD_SUCCESS_CB);
        actions.add(LogicActions.GROUP_CHAT_MODIFY_CARD_FAIL_CB);

        actions.add(LogicActions.GROUP_CHAT_MODIFY_CARD);
        actions.add(LogicActions.GROUP_CHAT_MODIFY_CARD_FAIL);

        actions.add(LogicActions.GROUP_CHAT_ERROR_NO);
        actions.add(LogicActions.GROUP_CHAT_ERROR_NETWORK);
        actions.add(LogicActions.GROUP_CHAT_ERROR_OTHER);
        UIObserverManager.getInstance().registerObserver(mUIObserver, actions);
    }

    @Override
    public void start() {
        mView.setEditText(mName);
    }

    private void init(Bundle bundle) {
        mAddress = bundle.getString(GroupCardActivity.BUNDLE_KEY_ADDRESS);
        mName = bundle.getString(GroupCardActivity.BUNDLE_KEY_CARD);
        if(mName == null){
            mName = "";
        }
    }

    @Override
    public void save() {
        Log.d(TAG, "notify group card : old = " + mName + " new = " + mText.toString());
        if (!TextUtils.isEmpty(mAddress) && !TextUtils.isEmpty(mText.toString().trim()) && !mText.toString().equals(mName)) {
            mView.toggleProgressDialog(true);
            GroupChatControl.rcsImSessMdfyDispName(mAddress, mText.toString().trim());
        } else {
            mView.finish(null);
        }
    }

    private UIObserver mUIObserver = new UIObserver() {
        @Override
        protected void onReceiveAction(int action, Intent intent) {
            Log.d(TAG, "onReceiveAction " + action);
            mView.toggleProgressDialog(false);
            switch (action) {
                case LogicActions.GROUP_CHAT_MODIFY_CARD_SUCCESS_CB:
                    mView.toast(mContext.getString(R.string.change_success));
                    Intent in = new Intent();
                    in.putExtra(LogicActions.GROUP_CHAT_DISPLAY_NAME, mText.toString());
                    mView.finish(in);
                    break;
                case LogicActions.GROUP_CHAT_MODIFY_CARD_FAIL_CB:
                case LogicActions.GROUP_CHAT_MODIFY_CARD_FAIL:
                    mView.toast(mContext.getString(R.string.change_defail));
                    break;
                case LogicActions.GROUP_CHAT_ERROR_NETWORK:
                    mView.toast(mContext.getString(R.string.public_net_exception));
                    break;
                case LogicActions.GROUP_CHAT_ERROR_NO:
                    mView.toast(mContext.getString(R.string.group_timeout));
                    break;
                case LogicActions.GROUP_CHAT_ERROR_OTHER:
                    mView.toast(mContext.getString(R.string.unknow_error));
                    break;
                default:

            }

        }
    };

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        Log.d(TAG, "beforeTextChanged: ");

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        android.util.Log.d(TAG, "onTextChanged: ");
        if (s.length() == 0) {
            mView.showDelete(false);
            mView.setSaveClickable(false);
        } else {
            mView.showDelete(true);
            mView.setSaveClickable(true);
        }
        if (s.toString().trim().isEmpty()) {
            mView.setSaveClickable(false);
        }
        iStart = start;
        iCount = count;
    }

//    private boolean firstTag = true; // 标记进入界面首次显示文本
    @Override
    public void afterTextChanged(Editable s) {
        String temp = s.toString().trim();
        int num = temp.getBytes(Charset.forName("utf-8")).length;
        android.util.Log.d(TAG, "afterTextChanged: " +temp+ " num"+num);
        while (num > mMaxLength) {
            int end = iStart + iCount;
            s.delete(end - 1, end);
            temp = s.toString().trim();
            num = temp.getBytes(Charset.forName("utf-8")).length;
        }
        mText = temp;
        if (TextUtils.isEmpty(temp)) {
            mView.setTextColor(mContext.getResources().getColor(R.color.color_a6a6a6));
        } else {
            mView.setTextColor(mContext.getResources().getColor(R.color.color_4991fb));
        }
    }
}
