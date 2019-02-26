package com.cmicc.module_message.ui.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.yuliao_business.logic.common.LogicActions;
import com.chinamobile.app.yuliao_business.model.GroupMember;
import com.chinamobile.app.yuliao_business.util.ConversationUtils;
import com.chinamobile.app.yuliao_business.util.GroupChatUtils;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.utils.BroadcastActions;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity;
import com.cmcc.cmrcs.android.ui.adapter.BaseCustomCursorAdapter;
import com.cmicc.module_message.ui.adapter.GroupSettingMemberAdapter;
import com.cmicc.module_message.ui.constract.GroupSettingContract;
import com.cmcc.cmrcs.android.ui.dialogs.CommomDialog;
import com.cmcc.cmrcs.android.ui.dialogs.CommomDialog.OnClickListener;
import com.cmicc.module_message.ui.presenter.GroupSettingPresenter;
import com.cmcc.cmrcs.android.ui.utils.ConvCache;
import com.cmcc.cmrcs.android.ui.utils.LoginUtils;
import com.cmcc.cmrcs.android.ui.utils.UmengUtil;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmicc.module_message.R;
import com.juphoon.cmcc.app.lemon.MtcCliConstants;

import java.util.ArrayList;
import java.util.List;

import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.CONV_ADDRESS;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.GROUP_CARD;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.GROUP_NAME;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.GROUP_OWNER;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.GROUP_TYPE;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.IS_EP_GROUP;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.GROUP_CHAT_ID;
import static com.cmcc.cmrcs.android.ui.utils.ContactSelectorUtil.SOURCE_GROUP_MEMBER_LIST;
import static com.constvalue.MessageModuleConst.GroupSettingActivity.BUNDLE_KEY_ADDRESS;
import static com.constvalue.MessageModuleConst.GroupSettingActivity.BUNDLE_KEY_EXIT_GROUP;
import static com.constvalue.MessageModuleConst.GroupSettingActivity.REQUEST_CODE_ADD_MEMBER;
import static com.constvalue.MessageModuleConst.GroupSettingActivity.REQUEST_CODE_DELETE_MEMBER;
import static com.constvalue.MessageModuleConst.GroupSettingActivity.REQUEST_CODE_NOTIFY_GROUP_CARD;
import static com.constvalue.MessageModuleConst.GroupSettingActivity.REQUEST_CODE_NOTIFY_GROUP_NAME;

/**
 * @anthor situ
 * @time 2017/5/17 11:36
 * @description 群聊设置界面
 */

public class GroupSettingActivity extends BaseActivity implements GroupSettingContract.IView, BaseCustomCursorAdapter.OnRecyclerViewItemClickListener
        ,CompoundButton.OnCheckedChangeListener {




    private static final String TAG = "GroupSettingActivity";

//    Toolbar mToolbar;
    TextView mTvMemberCount;
    TextView mGroupName;
    View mGroupNameLine;
    TextView mMemberGroupName;
    View mMemberGroupNameLine;
    RecyclerView mMemberList;
    SwitchCompat mSwitchUndisturb;
    View mGroupManageLine;
//    ImageView mThemeThumb;

    private GroupSettingContract.IPresenter mPresenter;

    private GroupSettingMemberAdapter mAdapter;

    public boolean isDestroy = false;

    private boolean isEPgroup = false;//是否为企业群

    private TextView leftGroupChatNameTv ; // 左边群聊昵称
    private TextView groupCodeTv ;      // 群二维码
    private TextView meGroupNameTv ; // 我在本群的昵称
    private TextView groupManageTv ; // 群管理
    private TextView messageInterruptionTv ;  // 消息免打扰
    private TextView mChatSet2TopTv; // 聊天置顶
    private SwitchCompat mChatSet2TopSwitch; //聊天置顶的 开关
    private TextView findChatRecordTv ; // 查找聊天记录
    private TextView chatFileTv ; // 聊天文件
    private TextView emptyCahtRecordTv ;// 清空聊天记录
    private TextView deleteContactsTv ; // 删除联系人
    private TextView text_title ;

    private boolean isClearAllMsg = false;//是否清除群消息

    private ProgressBar mNoMessageProgress;

    private String address ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().setBackgroundDrawableResource(R.color.color_f5f5f5);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_setting);
    }

    @Override
    protected void findViews() {
        text_title = (TextView) findViewById(R.id.text_title); //群聊设置，顶部title

        mTvMemberCount = (TextView)findViewById(R.id.member_count); //群成员
        mMemberList = (RecyclerView)findViewById(R.id.member_list);  //群成员列表

        mGroupNameLine = findViewById(R.id.group_name_line);
        leftGroupChatNameTv = (TextView)findViewById(R.id.left_group_chat_name_tv); //群聊名称
        mGroupName = (TextView)findViewById(R.id.group_name);  // 群的具体名称

        groupCodeTv = (TextView) findViewById(R.id.left_group_code_tv); // 群二维码

        mMemberGroupNameLine = findViewById(R.id.my_group_name_line);
        meGroupNameTv = (TextView)findViewById(R.id.left_me_group_name_tv); // 我在本群的昵称
        mMemberGroupName = (TextView)findViewById(R.id.my_group_name);        //我在本群的昵称

        mGroupManageLine =  findViewById(R.id.group_manage); //群管理的容器
        groupManageTv = (TextView) findViewById(R.id.left_group_manage_tv);     // 群管理

        messageInterruptionTv = (TextView) findViewById(R.id.left_message_interruption_tv);  // 消息免打扰
        mSwitchUndisturb = (SwitchCompat)findViewById(R.id.switch_undisturb);           //消息免打扰的开关
        mNoMessageProgress = (ProgressBar) findViewById(R.id.no_message_progress);  //消息免打扰的进度条

        mChatSet2TopTv = (TextView) findViewById(R.id.chat_set_to_top_tv);          // 消息置顶
        mChatSet2TopSwitch = (SwitchCompat) findViewById(R.id.chat_set_to_top_switch); //消息置顶的开关

        findChatRecordTv = (TextView) findViewById(R.id.left_find_chat_record_tv); // 查找聊天记录
        chatFileTv = (TextView) findViewById(R.id.left_chat_file_tv);        // 聊天文件
        emptyCahtRecordTv = (TextView) findViewById(R.id.left_empty_chat_tv);       // 清空聊天记录
        deleteContactsTv = (TextView) findViewById(R.id.delete_and_exit);          // 删除联系人

        mMemberGroupNameLine.setOnClickListener(this);
        mGroupManageLine.setOnClickListener(this);
        deleteContactsTv.setOnClickListener(this);

        findViewById(R.id.show_more_member).setOnClickListener(this);
        findViewById(R.id.group_name_line).setOnClickListener(this);
        findViewById(R.id.rl_group_avatars).setOnClickListener(this);
        findViewById(R.id.tv_serarch_chat_record).setOnClickListener(this);
        findViewById(R.id.tv_chat_file).setOnClickListener(this);
        findViewById(R.id.tv_chat_empty).setOnClickListener(this);

        text_title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL)); //修改顶部title的字体

    }

    @Override
    protected void init() {
        initToolBar();

        mMemberList.setLayoutManager(new GridLayoutManager(this, 4));
        mAdapter = new GroupSettingMemberAdapter(this);
        mAdapter.setRecyclerViewItemClickListener(this);
        mMemberList.setAdapter(mAdapter);

        Bundle bundle = getIntent().getExtras();
        isEPgroup = bundle.getBoolean("isEPgroup" ,false);
        address = bundle.getString(BUNDLE_KEY_ADDRESS);
        mAdapter.setmGroupType(GroupChatUtils.getGroupType(mContext, address));
        mPresenter = new GroupSettingPresenter(this, this, getSupportLoaderManager(), bundle);
        //chairmanRefresh();
        mPresenter.start();
        mPresenter.getUndisturbSetting(mPresenter.getAddress());

        //先设置状态再监听变化
        if (ConversationUtils.isSlient(this, mPresenter.getAddress())) {
            LogF.w(TAG, "isSlient : addr=" + mPresenter.getAddress());
            mSwitchUndisturb.setChecked(true);
        } else {
            mSwitchUndisturb.setChecked(false);
        }
        mSwitchUndisturb.setOnCheckedChangeListener(mCheckListener);
        mSwitchUndisturb.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP){
                    UmengUtil.buryPoint(getApplicationContext(), "message_groupmessage_setup_notdisturb","消息免打扰",0);

                    //先判断联网状态
                    if (!AndroidUtil.isNetworkConnected(mContext)) {
                        Toast.makeText(mContext, R.string.contact_list_no_net_hint, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    //再判断是否已经登录
                    int loginState = LoginUtils.getInstance().getLoginState();
                    if (loginState != MtcCliConstants.MTC_REG_STATE_REGED) {
                        Toast.makeText(mContext, R.string.login_no_logins, Toast.LENGTH_SHORT).show();
                        return true;
                    }

                    //防止重复操作，将按钮设置为不可用，将在服务器返回后将之恢复
                    mSwitchUndisturb.setEnabled(false);
                    mSwitchUndisturb.setVisibility(View.GONE);
                    mNoMessageProgress.setVisibility(View.VISIBLE);

                    //因为我们拦截了checkBox的点击操作，当前状态尚未更改（其实也不会更改了，需要我们收到服务器响应后编码更改）。
                    //所以当前状态已打开的时候要发给服务器off，当前状态未打开时要发给服务器on。注意这里是反的！！！
                    String status = mSwitchUndisturb.isChecked()?"off":"on";
                    mPresenter.setUndisturbSettingServer(status);
                }
                return true;
            }
        });
        // 置顶状态 设置

        boolean isSet2Top = ConversationUtils.isTop(this, mPresenter.getAddress());
        mChatSet2TopSwitch.setChecked(isSet2Top);
        mChatSet2TopSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean success = false;
                if(isChecked){
                    long time = System.currentTimeMillis();
                    success = ConversationUtils.setTop(mContext, mPresenter.getAddress(), time);
                    if(success){
                        UmengUtil.buryPoint(getApplicationContext(),"groupmessage_setup_top","消息-群聊-群聊设置-置顶聊天",0);
                        ConvCache.getInstance().updateToTop( mPresenter.getAddress(), ConvCache.CacheType.CT_ALL, time);
                    }else{//设置置顶失败， 关闭开关
                        mChatSet2TopSwitch.setChecked(false);
                    }
                }else{
                    success = ConversationUtils.setTop(mContext,  mPresenter.getAddress(), -1);
                    if(success){
                        ConvCache.getInstance().updateToTop( mPresenter.getAddress(), ConvCache.CacheType.CT_ALL, -1);
                    }else{//关闭置顶失败， 打开开关
                        mChatSet2TopSwitch.setChecked(true);
                    }
                }
            }
        });
        registerGorupStatusReceiver();
    }

    private void initToolBar() {
        findViewById(R.id.left_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void finish() {
        Intent intent = new Intent();
        if (mSwitchUndisturb != null) {
            intent.putExtra("slient", mSwitchUndisturb.isChecked());
        }

        if(isClearAllMsg){
            intent.putExtra("clear_all_msg" ,true);
        }
        setResult(Activity.RESULT_OK, intent);
        super.finish();
    }

    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.show_more_member) {
//            Intent intent = ContactsSelectActivity.createIntentForShowGroupMemberList(this,mPresenter.getAddress(),isEPgroup
//                    ,mPresenter.getGroupName(),mPresenter.getGroupName(),mPresenter.getAddress() , mPresenter.isChairman(),getIntent().getIntExtra(ContactsSelectActivity.GROUP_TYPE,1));

            Intent intent = ContactSelectorActivity.creatIntent(this, SOURCE_GROUP_MEMBER_LIST,1);
            intent.putExtra(GROUP_TYPE,getIntent().getIntExtra(ContactSelectorActivity.GROUP_TYPE,1));
            intent.putExtra(GROUP_CHAT_ID, mPresenter.getAddress());
            intent.putExtra(IS_EP_GROUP,isEPgroup);
            intent.putExtra(GROUP_CARD ,mPresenter.getGroupName());
            intent.putExtra(GROUP_NAME,mPresenter.getGroupName());
            intent.putExtra(CONV_ADDRESS,mPresenter.getAddress());
            intent.putExtra(GROUP_OWNER,mPresenter.isChairman()); // 群主
            startActivity(intent);

        } else if (i == R.id.group_name_line) {
            UmengUtil.buryPoint(getApplicationContext(), "message_groupmessage_setup_groupcard","群名片",0);
            GroupNameActivity.startForResult(this, REQUEST_CODE_NOTIFY_GROUP_NAME, mPresenter.getAddress(), mPresenter.getGroupName());

        } else if (i == R.id.rl_group_avatars) {
            UmengUtil.buryPoint(getApplicationContext(), "message_groupmessage_setup_QR","群二维码",0);
            Bundle bundle = new Bundle();
            bundle.putString("groupName", mPresenter.getGroupName());
            bundle.putString("address", mPresenter.getAddress());
            Intent intent = new Intent(this, GroupQRActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);

        } else if (i == R.id.my_group_name_line) {
            GroupCardActivity.startForResult(this, REQUEST_CODE_NOTIFY_GROUP_CARD, mPresenter.getAddress(), mPresenter.getGroupCard());

        } else if (i == R.id.tv_serarch_chat_record) {
            UmengUtil.buryPoint(getApplicationContext(), "message_groupmessage_search","查找聊天记录",0);
            MessageSearchActivity.start(this, mPresenter.getAddress(), Type.TYPE_BOX_GROUP);

        } else if (i == R.id.tv_chat_file) {
            UmengUtil.buryPoint(getApplicationContext(), "message_groupmessage_setup_file","聊天文件",0);
//            ChatFileActivity.start(this, mPresenter.getAddress(), Type.TYPE_BOX_GROUP);

        } else if (i == R.id.group_manage) {
            GroupManageActivity.start(this, mPresenter.getAddress(), mPresenter.getGroupMemberList(), isEPgroup);

        } else if (i == R.id.delete_and_exit) {
            UmengUtil.buryPoint(getApplicationContext(), "message_groupmessage_setup_delete","删除退出",0);
            int size = mAdapter.getData().size() ;
            LogF.d(TAG , " size = "+ size);
            if (mPresenter.isChairman() && mAdapter.getData().size() > 3) {
                if(mAdapter.getData().size() == 4 ){ // 群里只有两个人
                    showDissolveConfirmDialog();
                }else{
                    chairmanExitDialog();
                }
            } else {
                memberExitDialog();
            }

        } else if(i == R.id.tv_chat_empty){
            final CommomDialog commomDialog = new CommomDialog(this ,null ,getString(R.string.clear_chat_history));
            commomDialog.setOnNegativeClickListener(new OnClickListener() {
                @Override
                public void onClick() {
                    commomDialog.dismiss();
                }
            });
            commomDialog.setOnPositiveClickListener(new OnClickListener() {
                @Override
                public void onClick() {
                    UmengUtil.buryPoint(getApplicationContext(),"groupmessage_setup_empty","消息-群聊-群聊设置-清空聊天记录",0);
                    commomDialog.dismiss();
                    mPresenter.clearAllMsg();
                    isClearAllMsg = true;
                    Toast.makeText(mContext, R.string.clear_success,Toast.LENGTH_SHORT).show();
                }
            });
            commomDialog.show();

        }else {
        }
    }


    @Override
    public void updateUndisturbFinish(boolean isOk) {
        //将按钮恢复可用
        mSwitchUndisturb.setEnabled(true);
        mSwitchUndisturb.setVisibility(View.VISIBLE);
        mNoMessageProgress.setVisibility(View.GONE);

        if (isOk) {
            //设置上传服务器成功，这时再更改checked状态
            //状态置反
            mSwitchUndisturb.setChecked(!mSwitchUndisturb.isChecked());
            LogF.e(TAG, "上传成功 address=" + mPresenter.getAddress());
        } else {
            Toast.makeText(getApplication(), R.string.update_settings_failed, Toast.LENGTH_SHORT).show();
            LogF.e(TAG, "上传失败 address=" + mPresenter.getAddress());
        }
    }

    @Override
    public void updateGroupName(String name) {
        mGroupName.setText(name);
    }

    @Override
    public void updateGroupMemberList(List<GroupMember> groupMembers) {
        int count = groupMembers.size();
        updateGroupMemberCount(count);
        List<GroupMember> data = new ArrayList<GroupMember>();
        int j = 10;
        if (!mPresenter.isChairman() && !isEPgroup) {
            j = 11;
        }else if(!mPresenter.isChairman()){
            j = 12;
        }
        for (int i = 0; i < j && i < groupMembers.size(); i++) {
            data.add(groupMembers.get(i));
        }
        if(mPresenter.isChairman() || !isEPgroup){
            GroupMember add = new GroupMember();
            add.setAddress("+");
            data.add(add);
        }

        if (mPresenter.isChairman()) {
            GroupMember delete = new GroupMember();
            delete.setAddress("-");
            data.add(delete);
        }
        mAdapter.setData(data);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateGroupCard(String name) {
        mMemberGroupName.setText(name);
    }

    public static void start(Context context, String address) {
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY_ADDRESS, address);
        Intent intent = new Intent();
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public static void start(Activity activity, Bundle bundle) {
        Intent intent = new Intent(activity, GroupSettingActivity.class);
        intent.putExtras(bundle);
        activity.startActivityForResult(intent ,100);
    }

    public static void startForResult(Activity activity, int requestCode, Bundle bundle) {
        Intent intent = new Intent(activity, GroupSettingActivity.class);
        intent.putExtras(bundle);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onItemClick(View view, int position) {
        if(position >= mAdapter.getItemCount() || position < 0){
            return;
        }
        GroupMember groupMember = mAdapter.getItem(position);
        mPresenter.itemClick(groupMember);
    }

    @Override
    public boolean onItemLongCLickListener(View v, int position) {
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogF.d(TAG, "onActivityResult requestCode = " + requestCode + " , resulteCode = " + resultCode);
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQUEST_CODE_ADD_MEMBER:
                mPresenter.addMemberToGroup(data, true); // 添加成员
                break;
            case REQUEST_CODE_NOTIFY_GROUP_NAME:
                String groupName = data.getStringExtra(LogicActions.GROUP_CHAT_SUBJECT);
                mPresenter.updateGroupName(groupName);
                break;
            case REQUEST_CODE_DELETE_MEMBER:
                mPresenter.addMemberToGroup(data, false); // 踢出成员
                break;
            case REQUEST_CODE_NOTIFY_GROUP_CARD:
                UmengUtil.buryPoint(getApplicationContext(),"groupmessage_setup_nickname","消息-群聊-群聊设置-我在本群的昵称",0);
                String groupCard = data.getStringExtra(LogicActions.GROUP_CHAT_DISPLAY_NAME);
                mPresenter.updateGroupCard(groupCard);
                break;
            default:

        }

    }

    @Override
    public void toast(final CharSequence sequence) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                BaseToast.makeText(GroupSettingActivity.this, sequence, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //是否隐藏二维码
    private void hideQRcode(boolean isHide){
        if(isHide){
            findViewById(R.id.rl_group_avatars).setVisibility(View.GONE);
        }
    }

    private void setGroupNameClickable(boolean clickable) {
        mGroupNameLine.setClickable(clickable);
        findViewById(R.id.group_name_right_arrow).setVisibility(clickable ? View.VISIBLE : View.GONE);
    }

    private void updateGroupMemberCount(int i) {
//        if(mGroupMemberCount > 0){
//            i = mGroupMemberCount;
//        }
        String text = getString(R.string.group_setting_member_count);
        mTvMemberCount.setText(String.format(text, String.valueOf(i)));
    }

    @Override
    public void setUndisturbSwitch(boolean checked) {
        mSwitchUndisturb.setChecked(checked);
    }

    @Override
    public void chairmanRefresh() {
        if (mPresenter.isChairman() && !isEPgroup) {
            setGroupNameClickable(true);
            mGroupManageLine.setVisibility(View.VISIBLE);
        } else {
            setGroupNameClickable(false);
            mGroupManageLine.setVisibility(View.GONE);
        }

        if(isEPgroup){
            hideQRcode(true);
            mMemberGroupNameLine.setClickable(false);
            findViewById(R.id.my_group_name_right_arrow).setVisibility(View.GONE);

            findViewById(R.id.delete_and_exit).setVisibility(View.GONE);
        }
    }

    @Override
    public void updateThemeThumb(Drawable drawable) {
//        mThemeThumb.setImageDrawable(drawable);
    }

    // 群主退出弹窗
    private void chairmanExitDialog() {
        String message = this.getString(R.string.chairman_exit_group_hint);
        CommomDialog commomDialog = new CommomDialog(this, null, message);
        commomDialog.setOnPositiveClickListener(new CommomDialog.OnClickListener() {
            @Override
            public void onClick() {
                mPresenter.chairmanExitGroup();
            }
        });
        commomDialog.show();
    }

    // 普通群成员退出弹窗
    private void memberExitDialog() {
        String message = this.getString(R.string.member_exit_group_hint);
        CommomDialog commomDialog = new CommomDialog(this, null, message);
        commomDialog.setOnPositiveClickListener(new CommomDialog.OnClickListener() {
            @Override
            public void onClick() {
                mPresenter.memberExitGroup();
            }
        });
        commomDialog.show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        boolean exit = intent.getBooleanExtra(BUNDLE_KEY_EXIT_GROUP, false);
        mPresenter.setExitAfterTransfer(exit);
        if (exit) {
            if (!mPresenter.isChairman()) {
                // 已转让
                mPresenter.memberExitGroup();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPresenter.setupThemeThumb();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDestroy = true;
        unRegisterGorupStatusReceiver();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mPresenter.setUndisturbSettingLocal(mPresenter.getAddress(), isChecked);
    }


    private final float SETTING_ITEM_FONT_SIZE_FOURTEEN = 14f;
    private final float SETTING_ITEM_FONT_SIZE = 18f;
    private float TV_CONTENT_NAME_MAX_EMS = 8;

    @Override
    protected void onAppFontSizeChanged(float scale){
        super.onAppFontSizeChanged(scale);
        mTvMemberCount.setTextSize(SETTING_ITEM_FONT_SIZE_FOURTEEN*scale); // 群成员
        leftGroupChatNameTv.setTextSize(SETTING_ITEM_FONT_SIZE*scale); // 左边群聊昵称
        mGroupName.setTextSize(SETTING_ITEM_FONT_SIZE*scale);        // 群昵称
        groupCodeTv.setTextSize(SETTING_ITEM_FONT_SIZE*scale) ;      // 群二维码
        meGroupNameTv.setTextSize(SETTING_ITEM_FONT_SIZE*scale) ;    // 我在本群的昵称
        groupManageTv.setTextSize(SETTING_ITEM_FONT_SIZE*scale) ;    // 群管理
        messageInterruptionTv.setTextSize(SETTING_ITEM_FONT_SIZE*scale) ;  // 消息免打扰
        mChatSet2TopTv.setTextSize(SETTING_ITEM_FONT_SIZE*scale);    //置顶聊天
        findChatRecordTv.setTextSize(SETTING_ITEM_FONT_SIZE*scale) ; // 查找聊天记录
        chatFileTv.setTextSize(SETTING_ITEM_FONT_SIZE*scale) ;       // 聊天文件
        emptyCahtRecordTv.setTextSize(SETTING_ITEM_FONT_SIZE*scale) ;// 清空聊天记录
        deleteContactsTv.setTextSize(SETTING_ITEM_FONT_SIZE*scale) ; // 删除联系人
    }

    /**
     * 消息免打扰选项
     */
    private CompoundButton.OnCheckedChangeListener mCheckListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (mPresenter.getAddress() != null && !"".equals(mPresenter.getAddress())) {
                LogF.e(TAG, "上传成功后 更新按钮和数据库 address=" + mPresenter.getAddress() + (isChecked ? "开" : "关"));
                mPresenter.setUndisturbSettingLocal(mPresenter.getAddress(), isChecked);
            }
        }
    };


    /**
     * 直接解散群
     */
    public void showDissolveConfirmDialog() {
        String message = this.getString(R.string.dissolve_group_confirm);
        CommomDialog commomDialog = new CommomDialog(this, null, message);
        commomDialog.setOnPositiveClickListener(new CommomDialog.OnClickListener() {
            @Override
            public void onClick() {
                if (!AndroidUtil.isNetworkConnected(mContext)) {
                    BaseToast.makeText(mContext, mContext.getString(R.string.network_disconnect), Toast.LENGTH_SHORT).show();
                    return;
                }
                mPresenter.rcsImSessDissolve();

            }
        });
        commomDialog.show();
    }


    /**
     * 广播
     */
    class GorupStatusReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if( BroadcastActions.MESSAGE_GROUP_STATUS_UPDATE_MTC_IM_ERR_FORBIDDEN_ACTION.equals(intent.getAction())){
                Toast.makeText( GroupSettingActivity.this , GroupSettingActivity.this.getResources().getString(R.string.activation_group_failure) , Toast.LENGTH_SHORT).show();
            }
        }
    }

    private GorupStatusReceiver mGorupStatusReceiver ;

    private void registerGorupStatusReceiver(){
        if(mContext == null){
            return;
        }
        if(mGorupStatusReceiver == null ){
            mGorupStatusReceiver = new GorupStatusReceiver();
            IntentFilter intentFilter = new IntentFilter(BroadcastActions.MESSAGE_GROUP_STATUS_UPDATE_MTC_IM_ERR_FORBIDDEN_ACTION);
            mContext.registerReceiver(mGorupStatusReceiver ,intentFilter);
        }
    }

    private void unRegisterGorupStatusReceiver(){
        if(mGorupStatusReceiver!=null){
            mContext.unregisterReceiver(mGorupStatusReceiver);
            mGorupStatusReceiver = null ;
        }
    }

}
