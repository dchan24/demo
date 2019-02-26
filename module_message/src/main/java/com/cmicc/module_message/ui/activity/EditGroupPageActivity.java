package com.cmicc.module_message.ui.activity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmicc.module_message.ui.fragment.EditGroupPageFragment;
import com.cmicc.module_message.ui.presenter.EditGroupPagePresenterImpl;
import com.cmcc.cmrcs.android.ui.utils.ActivityUtils;
import com.cmicc.module_message.R;

/**
 * Created by tigger on 2017/5/17.
 * 编辑群聊名称界面
 */

public class EditGroupPageActivity extends BaseActivity {

    private EditGroupPageFragment mEditGroupPageFragment;
    private EditGroupPagePresenterImpl mPresenter;
    private TextView mTitle;
    private RelativeLayout mBack;
    TextView mTvSure;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group_page);
    }

    @Override
    protected void findViews() {
        mTvSure = (TextView)findViewById(R.id.tv_sure);
        mTitle = (TextView) findViewById(R.id.title);
        mTitle.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        mBack = (RelativeLayout) findViewById(R.id.back);
    }

    @Override
    protected void init() {

        initToolBar();

        if(mEditGroupPageFragment == null){
            mEditGroupPageFragment = EditGroupPageFragment.newInstantce();
            mEditGroupPageFragment.setTvSure(mTvSure);
        }
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), mEditGroupPageFragment, R.id.contentFrame);
        mPresenter = new EditGroupPagePresenterImpl(mEditGroupPageFragment, this);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.id_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
//        mPresenter.setAlive(false);
        super.onDestroy();
        mEditGroupPageFragment.dismiss();
    }
}
