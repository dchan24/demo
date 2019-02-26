package com.cmicc.module_message.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.chinamobile.app.yuliao_business.logic.common.LogicActions;
import com.cmcc.cmrcs.android.ui.fragments.BaseFragment;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.activity.ChooseFileSendActivity;
import com.cmicc.module_message.ui.activity.ChooseLocalFileActivity;

/**
 * Created by tigger on 2017/7/3.
 */

public class ChooseFileTypeFragment extends BaseFragment implements OnClickListener {
    protected static final String TAG = "ChooseFileTypeFragment";

    TextView mTvMobileLocal;

    private Context mContext;

    @Override
    public void initData() {
        mContext = getActivity();

        initView();
    }

    public void initViews(View rootView){
        super.initViews(rootView);
        mTvMobileLocal = (TextView) rootView.findViewById(R.id.tv_mobile_local);
    }

    public void initView(){
        mTvMobileLocal.setOnClickListener(this);
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_choose_file_type_layout;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_mobile_local) {
            ChooseFileSendActivity activity = (ChooseFileSendActivity) getActivity();
            Intent i = new Intent(mContext, ChooseLocalFileActivity.class);
            i.putExtra(LogicActions.PHONE_NUMBER, activity.getAddress());
            i.putExtra(LogicActions.NOTIFICATION_GOTO_SEND_FILE_ACTION, activity.getSendFileAction());
            activity.startActivityForResult(i, ChooseFileSendActivity.REQUEST_CODE);
        }
    }
}
