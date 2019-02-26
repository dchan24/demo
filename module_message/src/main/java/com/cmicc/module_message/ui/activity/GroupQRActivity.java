package com.cmicc.module_message.ui.activity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmicc.module_message.ui.fragment.GroupQRFragment;
import com.cmicc.module_message.ui.presenter.GroupQRPresenter;
import com.cmcc.cmrcs.android.ui.utils.ActivityUtils;
import com.cmicc.module_message.R;

/**
 * Created by hwb on 2017/7/10.
 * 群二维码界面
 */

public class GroupQRActivity extends BaseActivity {

    TextView mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_with_toolbar_layout);
    }

    @Override
    protected void findViews() {
        mToolbar = (TextView)findViewById(R.id.text_title);
        mToolbar.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
    }

    private void initToolBar() {
        mToolbar.setText(getResources().getString(R.string.title_group_qr_code));
//        setSupportActionBar(mToolbar);
        findViewById(R.id.left_back).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
//        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });
    }

    public View getToolBar() {
        return mToolbar;
    }

    @Override
    protected void init() {
        initToolBar();
        GroupQRFragment fragment =
                (GroupQRFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (fragment == null) {
            fragment = GroupQRFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    fragment, R.id.contentFrame);
        }
        GroupQRPresenter presenter = new GroupQRPresenter(fragment, this.getApplicationContext(), getIntent().getExtras());
        fragment.setPresenter(presenter);
    }

}
