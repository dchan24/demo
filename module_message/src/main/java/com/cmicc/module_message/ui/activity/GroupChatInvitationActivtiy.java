package com.cmicc.module_message.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmicc.module_message.ui.fragment.GroupChatInvitationFragment;
import com.cmcc.cmrcs.android.ui.utils.ActivityUtils;
import com.cmicc.module_message.R;

/**
 * Created by LY on 2018/5/7. YSF
 * 群聊邀请
 */

public class GroupChatInvitationActivtiy extends BaseActivity {

    private static final String TAG="GroupChatInvitationActivtiy";
    private TextView titleText ;
    private GroupChatInvitationFragment mView;
    public static String groupaddress = "groupaddress"; // 群id
    public static String invitationAddress = "invitationAddress" ; // 发情邀请人的地址
    public static String invitationStip = "invitationStip" ; // 发情邀请人的提示语
    public static String body = "body";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat_invitation_layout);
    }

    @Override
    protected void findViews() {
        findViewById(R.id.left_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.select_picture_custom_toolbar_back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        titleText = findViewById(R.id.select_picture_custom_toolbar_title_text);
        titleText.setText(R.string.group_chat_invitation);
    }

    @Override
    protected void init() {
        mView =(GroupChatInvitationFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (mView == null) {
            mView = new GroupChatInvitationFragment();
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                mView.setArguments(bundle);
            }
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), mView, R.id.contentFrame);
        }
    }

    public static void goToGroupChatInvitationActivtiy(Activity context , String address , String sendAddress ,  String stip , int requestCode ){
        if(context == null || TextUtils.isEmpty(address) || TextUtils.isEmpty(sendAddress)){
            return;
        }
        Intent intent = new Intent(context , GroupChatInvitationActivtiy.class ) ;
        intent.putExtra(groupaddress , address ); // 群ID
        intent.putExtra(invitationAddress , sendAddress) ;
        intent.putExtra(invitationStip , stip) ;
        context.startActivityForResult(intent , requestCode );
    }
}
