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
import com.cmicc.module_message.ui.activity.GroupNameActivity;
import com.cmicc.module_message.ui.constract.GroupNameContract;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @anthor situ
 * @time 2017/5/23 10:02
 * @description 修改群聊名称
 */

public class GroupNamePresenter implements GroupNameContract.IPresenter, TextWatcher {

    private static final String TAG = "GroupNamePresenter";

    private GroupNameContract.IView mView;

    private UIObserverManager mUIObserverManager;
    private static List<Integer> sActions;

    private String mAddress = "";//groupId

    private String mName = "";
    private CharSequence mText;
    private static int mMaxLength = 30;
    private int iStart = 0;
    private int iCount = 0;
    Context mContext;

    public GroupNamePresenter(Context context, GroupNameContract.IView view, Bundle bundle) {
        mView = view;
        init(bundle);
        mContext = context;
        ArrayList<Integer> actions = new ArrayList<Integer>();

        actions.add(LogicActions.GROUP_CHAT_MODIFY_SUBJECT_SUCCESS_CB);
        actions.add(LogicActions.GROUP_CHAT_MODIFY_SUBJECT_FAIL_CB);
        actions.add(LogicActions.GROUP_CHAT_MODIFY_SUBJECT);
        actions.add(LogicActions.GROUP_CHAT_MODIFY_SUBJECT_FAIL);
        actions.add(LogicActions.GROUP_CHAT_ERROR_NO);
        actions.add(LogicActions.GROUP_CHAT_ERROR_NETWORK);
        actions.add(LogicActions.GROUP_CHAT_ERROR_OTHER);

        UIObserverManager.getInstance().registerObserver(mUIObserver, actions);
    }

    private void init(Bundle bundle) {
        mAddress = bundle.getString(GroupNameActivity.BUNDLE_KEY_ADDRESS);
        mName = bundle.getString(GroupNameActivity.BUNDLE_KEY_NAME);
    }

    private UIObserver mUIObserver = new UIObserver() {
        @Override
        protected void onReceiveAction(int action, Intent intent) {
            Log.d(TAG, "onReceiveAction " + action);
            mView.toggleProgressDialog(false);
            switch (action) {
                case LogicActions.GROUP_CHAT_MODIFY_SUBJECT:
                    break;
                case LogicActions.GROUP_CHAT_MODIFY_SUBJECT_SUCCESS_CB:
                    mView.toast(mContext.getString(R.string.change_success));
                    Intent in = new Intent();
                    in.putExtra(LogicActions.GROUP_CHAT_SUBJECT, mText.toString());
                    mView.finish(in);
                    break;
                case LogicActions.GROUP_CHAT_MODIFY_SUBJECT_FAIL_CB:
                case LogicActions.GROUP_CHAT_MODIFY_SUBJECT_FAIL:
                    mView.toast(mContext.getString(R.string.change_defail_));
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
    public void start() {
        mView.setEditText(mName);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
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

    @Override
    public void afterTextChanged(Editable s) {
        String temp = s.toString().trim();
        int num = temp.getBytes(Charset.forName("utf-8")).length;
        while (num > mMaxLength) {
            int end = iStart + iCount;
            s.delete(end -1, end);
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


    @Override
    public void save() {
        Log.d(TAG, "notify group name : old = " + mName + " new = " + mText.toString());
        if (!TextUtils.isEmpty(mAddress) && !TextUtils.isEmpty(mText.toString().trim()) && !mText.toString().equals(mName)) {
            mView.toggleProgressDialog(true);
            GroupChatControl.rcsImSessMdfySubject(mAddress, mText.toString().trim());
        } else {
            mView.finish(null);
        }
    }
}
