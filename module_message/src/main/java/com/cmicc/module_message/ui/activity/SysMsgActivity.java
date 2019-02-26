package com.cmicc.module_message.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmicc.module_message.ui.fragment.SysMsgFragment;
import com.cmcc.cmrcs.android.ui.utils.ActivityUtils;
import com.cmicc.module_message.R;

/**
 * 系统消息界面 SFYang
 * 
 * 1.YYY(群名)
 *   XXX 邀请你加入群
 *
 * 2.YYY(群名)
 *   你已成为群主
 *
 * 3.YYY(群名)
 *   该群已解散
 *
 * 4.YYY(群名)
 *   你已被请该出群
 *
 * 5.你已退出群
 *   来自群聊YYY（群名）
 *
 * 6.XXX (申请人的名字)
 *   XXX来自群聊YYY申请与你交换名片
 *
 */

public class SysMsgActivity extends BaseActivity {

    private SysMsgFragment mSysMsgFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sys_msg_list);
    }

    @Override
    protected void findViews() {
        initToolBar();
    }

    private void initToolBar() {
        View mBackBtn = findViewById(R.id.select_picture_custom_toolbar_back_btn);
        TextView title  = findViewById(R.id.select_picture_custom_toolbar_title_text);
        title.setText(getString(R.string.system_news));
        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        findViewById(R.id.left_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void init() {
        if(mSysMsgFragment == null){
            mSysMsgFragment = SysMsgFragment.newInstantce();
        }
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), mSysMsgFragment, R.id.contentFrame);
    }

    @Override
    protected void onDestroy() {
        if(mSysMsgFragment!=null){
            mSysMsgFragment.dismissProgressDialog();
        }
        super.onDestroy();
    }


    @Override
    protected void onActivityResult( int requestCode , int resultCode , Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == mSysMsgFragment.CARD_REQUESTCODE || requestCode == mSysMsgFragment.GROUP_REQUESTCODE){
            finish(); // 关闭界面
        }
    }

    public static void show(Context context){
        Intent intent = new Intent(context, SysMsgActivity.class);
        context.startActivity(intent);
    }

}
