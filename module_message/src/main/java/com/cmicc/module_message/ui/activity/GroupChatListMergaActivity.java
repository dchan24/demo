package com.cmicc.module_message.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity;
import com.cmicc.module_message.ui.fragment.GroupChatListMergaFragment;
import com.cmicc.module_message.ui.presenter.GroupChatListPresenter;
import com.cmcc.cmrcs.android.ui.utils.ActivityUtils;
import com.cmcc.cmrcs.android.ui.utils.UmengUtil;
import com.cmicc.module_message.R;
import com.constvalue.MessageModuleConst;

import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.IS_MULTI_FORWARD;
import static com.cmcc.cmrcs.android.ui.utils.ContactSelectorUtil.SOURCE_NEW_GROUP_NO_GROUP;
import static com.constvalue.MessageModuleConst.GroupChatListMergaActivityConst.GROUP_CHAT_LIST_SEARCH;


/**
 * Created by yangshengfu on 2018/11/22.
 *
 * <br/>
 *  转发->联系人选择器-> 选择一个群界面
 */

public class GroupChatListMergaActivity extends BaseActivity {


    private TextView mTitleTv;
    private ImageView mCreatGroupImg;
    private EditText mSearchEt ;
    private GroupChatListMergaFragment mGroupFragment;
    private GroupChatListPresenter mPresenter ;
    private RelativeLayout mSelectPictureCustomToolbar ;
    private int mCmd = 0 ; // 跳转到这个界面的类型（从什么地方跳转过来的）
    private Bundle mBundle ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().setBackgroundDrawableResource(R.color.color_fffffe);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat_list_merga);
    }

    @Override
    protected void findViews() {
        mSelectPictureCustomToolbar = findViewById(R.id.select_picture_custom_toolbar);
        mTitleTv = findViewById(R.id.select_picture_custom_toolbar_title_text);
        mTitleTv.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        mCreatGroupImg = findViewById(R.id.menu_add_btn);
        mCreatGroupImg.setVisibility(View.GONE);
        mCreatGroupImg.setImageResource(R.drawable.cc_chat_create_group);


        if (getIntent().getIntExtra(ContactSelectorActivity.KEY_GROUP_CHAT,0)== MessageModuleConst.GroupChatListMergaActivityConst.GROUP_CHAT_LIST){
            mCreatGroupImg.setVisibility(View.VISIBLE);
        }

        mSearchEt = findViewById(R.id.et_search);
        mSearchEt.setVisibility(View.VISIBLE);
//        mSearchEt.setFocusable(true);
//        mSearchEt.setFocusableInTouchMode(true);
        mSearchEt.setHint(R.string.search_groupchat);

        mTitleTv.setOnClickListener(this);
        findViewById(R.id.left_back).setOnClickListener(this);

        mCreatGroupImg.setOnClickListener(this);
        mSearchEt.setOnClickListener(this);
    }


    @Override
    protected void init() {
        if (mGroupFragment == null) {
            mGroupFragment = new GroupChatListMergaFragment();
            mBundle = getIntent().getExtras();
            if (mBundle != null) {
                mGroupFragment.setArguments(mBundle);
            }
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), mGroupFragment, R.id.contentFrame);
        }
        mCmd = getIntent().getIntExtra(ContactSelectorActivity.KEY_GROUP_CHAT , 1);
        if(mBundle.getInt(ContactSelectorActivity.KEY_GROUP_CHAT_TITLE) == 1){
            mTitleTv.setText(R.string.group_chat_list);
        }else{
            mTitleTv.setText(R.string.recent_selector_a_new_group);
        }
        mPresenter = new GroupChatListPresenter(this , mGroupFragment);
        mGroupFragment.setPresenter(mPresenter);
        // 获取状态栏，Title，搜索栏的高度
        mSearchEt.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mGroupFragment.setmExpIndexViewHeight(mSearchEt.getMeasuredHeight()+
                        mSelectPictureCustomToolbar.getMeasuredHeight()+
                        getStatusBarHeight(GroupChatListMergaActivity.this));
                mSearchEt.getViewTreeObserver().removeOnPreDrawListener(this);
                return false;
            }
        });
    }


    @Override
    public void onClick(View v){
        int viewID = v.getId();
        if(viewID == R.id.select_picture_custom_toolbar_title_text ||
           viewID == R.id.left_back ) { // 关闭界面
            finish();
        }else if(viewID == R.id.menu_add_btn){ // 创建群聊
            UmengUtil.buryPoint(getApplicationContext(), "contacts_groupmessage_add", "通讯录-群聊-选人建群", 0);
//            Intent intent = ContactsSelectActivity.createIntentForCreateGroupNoChoose(this);
            Intent intent = ContactSelectorActivity.creatIntent(this, SOURCE_NEW_GROUP_NO_GROUP,500);
            startActivity(intent);
        }else if(viewID == R.id.et_search){ // 群搜索
            Intent intent = new Intent(this , GroupChatSearchActivity.class);
            intent.putExtras(mBundle);
            startActivityForResult(intent,GROUP_CHAT_LIST_SEARCH);
        }
    }

    /**
     * 获取状态栏的高度
     * @param context
     * @return
     */
    private int getStatusBarHeight(Context context) {
        int result = 0;
        int resId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) {
            result = context.getResources().getDimensionPixelOffset(resId);
        }
        return result;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GROUP_CHAT_LIST_SEARCH && resultCode == Activity.RESULT_OK) {
            //多选消息选择群聊
            if (mBundle.getBoolean(IS_MULTI_FORWARD)) {
                setResult(Activity.RESULT_OK, data);
                finish();
                return;
            }
            if (mCmd != MessageModuleConst.GroupChatListMergaActivityConst.GROUP_CHAT_LIST) {// 通讯录 - 群聊
                setResult(Activity.RESULT_OK, data);
                finish();
            }else{
                finish();
            }
        }
    }

}
