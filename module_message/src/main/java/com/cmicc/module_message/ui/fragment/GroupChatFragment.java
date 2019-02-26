package com.cmicc.module_message.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.app.module.WebConfig;
import com.app.module.proxys.modulecontact.ContactProxy;
import com.app.module.proxys.moduleenterprise.EnterPriseProxy;
import com.app.module.proxys.moduleimgeditor.ImgEditorProxy;
import com.chinamobile.app.utils.RxAsyncHelper;
import com.chinamobile.app.yuliao_business.model.BaseModel;
import com.chinamobile.app.yuliao_business.model.GroupInfo;
import com.chinamobile.app.yuliao_business.model.GroupNotifyUtils;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_business.provider.Conversations;
import com.chinamobile.app.yuliao_business.util.ConversationUtils;
import com.chinamobile.app.yuliao_business.util.GroupChatUtils;
import com.chinamobile.app.yuliao_business.util.Status;
import com.chinamobile.app.yuliao_common.application.MyApplication;
import com.chinamobile.app.yuliao_common.utils.FileUtil;
import com.chinamobile.app.yuliao_common.utils.Log;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_common.utils.SharePreferenceUtils;
import com.cmcc.cmrcs.android.osutils.EnterpriseContactNameCompatHelper;
import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity;
import com.cmicc.module_message.ui.activity.GroupSMSActivity;
import com.cmicc.module_message.ui.activity.GroupSMSEditActivity;
import com.cmicc.module_message.ui.activity.GroupSettingActivity;
import com.cmicc.module_message.ui.activity.MessageBgSetActivity;
import com.cmicc.module_message.ui.activity.MessageDetailActivity;
import com.cmicc.module_message.ui.activity.NoRcsGroupMemberActivity;
import com.cmicc.module_message.ui.activity.NonEntryGroupAtivity;
import com.cmcc.cmrcs.android.ui.control.GroupChatControl;
import com.cmcc.cmrcs.android.ui.dialogs.PermissionDeniedDialog;
import com.cmcc.cmrcs.android.ui.model.MediaItem;
import com.cmicc.module_message.ui.model.impls.MessageEditorModelImpl;
import com.cmcc.cmrcs.android.ui.utils.ContactSelectorUtil;
import com.cmicc.module_message.utils.GroupChatCache;
import com.cmcc.cmrcs.android.ui.utils.LoginUtils;
import com.cmcc.cmrcs.android.ui.utils.MessageForwardUtil;
import com.cmcc.cmrcs.android.ui.utils.UmengUtil;
import com.cmcc.cmrcs.android.ui.view.MessageOprationDialog;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.constract.GroupChatContracts;
import com.cmicc.module_message.ui.presenter.GroupChatPresenterImpl;
import com.constvalue.MessageModuleConst;
import com.juphoon.cmcc.app.lemon.MtcImConstants;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import rx.functions.Func1;

import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.GROUP_TYPE;



/**
 * Created by tigger on 2017/5/3.
 */

public class GroupChatFragment extends BaseChatFragment {

    private static final String TAG = "GroupChatFragment";

    protected static String RM_MORE_NOT_TIP_KEY_GORUP = "rm_more_not_tip_key_group";

    private GroupChatContracts.Presenter mPresenter;

    private MenuItem setItem;
    private MenuItem multiCallItem;

    private int themeType = 0;

    private TextView tvTitle;

    private static final int REQUEST_CODE = 200;

    private String mOaArgs;//工作台args, 一期只包含groupId.

    private MessageForwardUtil messageForwardUtil;

    public static GroupChatFragment newInstance(Bundle bundle) {
        GroupChatFragment fragment = new GroupChatFragment();
        fragment.setArguments(bundle);
        fragment.isEPGroup = bundle.getBoolean("isEPgroup", false);
        fragment.isPartyGroup = bundle.getBoolean("isPartyGroup", false);
        return fragment;
    }


    @Override
    public void initData() {
        Log.d(TAG, "GroupChatFragment init");
        super.initData();

        handleSuperMsmVisible(true);

        sysSubInfos();

        resendExactReadState();


        boolean isNotTip = (boolean) SharePreferenceUtils.getParam(MyApplication.getAppContext(), RM_MORE_NOT_TIP_KEY_GORUP, false);
        if(!isNotTip){
            moreRedDot.setVisibility(View.VISIBLE);
        }else{
            moreRedDot.setVisibility(View.GONE);
        }
    }

    private void sysSubInfos() {
        new RxAsyncHelper("").runInThread(new Func1() {
            @Override
            public Object call(Object o) {
                //订阅群信息,防止进入多方电话选择界面数据错误问题
                String identity = GroupChatUtils.getIdentify(mContext, mAddress);
                GroupChatControl.rcsImConfSubsInfo(mAddress, identity);
                return null;
            }
        });
    }

    private void resendExactReadState() {
        new RxAsyncHelper("").runInThread(new Func1() {
            @Override
            public Object call(Object o) {
                ContentResolver cr = getActivity().getContentResolver();
                String where = String.format(Conversations.WHERE_ADDRESS_GROUP, mAddress);
                long currentTime = System.currentTimeMillis();
                long loadTime = currentTime - 7 * 24 * 3600 * 1000l;
                where = where + " AND date>" + loadTime;
                Cursor cursor = cr.query(Conversations.Group.CONTENT_URI,
                        new String[]{BaseModel.COLUMN_NAME_ID, BaseModel.COLUMN_NAME_ADDRESS, BaseModel.COLUMN_NAME_SEND_ADDRESS, BaseModel.COLUMN_NAME_IDENTIFY, BaseModel.COLUMN_NAME_MSG_ID}, where + " AND exact_read=1 AND exd_send_status=3", null,
                        null);
                if (cursor != null) {
                    try {
                        while (cursor.moveToNext()) {
                            int id = cursor.getInt(cursor.getColumnIndex(BaseModel.COLUMN_NAME_ID));
                            String address = cursor.getString(cursor.getColumnIndex(BaseModel.COLUMN_NAME_ADDRESS));
                            String sendAddress = cursor.getString(cursor.getColumnIndex(BaseModel.COLUMN_NAME_SEND_ADDRESS));
                            String identify = cursor.getString(cursor.getColumnIndex(BaseModel.COLUMN_NAME_IDENTIFY));
                            String msgId = cursor.getString(cursor.getColumnIndex(BaseModel.COLUMN_NAME_MSG_ID));
                            GroupChatControl.rcsImSendDispG((int) id, msgId, identify, "tel:" + sendAddress, address);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        cursor.close();
                    }
                }
                return null;
            }
        }).subscribe();
    }

    private void handleSuperMsmVisible(final boolean isFirst) {
        if (getActivity() == null) {
            return;
        }
        new RxAsyncHelper<>(mAddress).runInThread(new Func1<String, String>() {
            @Override
            public String call(String s) {
                if (!isFirst) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                GroupInfo groupInfo = GroupChatUtils.getGroupInfo(MyApplication.getAppContext(), mAddress);
                if (groupInfo == null) {
                    return "";
                }

                String idr = groupInfo.getIdentify();//获取群id.
                idr = idr.replace("sip:1252000199", "");
                String[] ss = idr.split("@");
                mOaArgs = ss[0];

                String master = groupInfo.getOwner();
                if (groupInfo.getType() == MtcImConstants.EN_MTC_GROUP_TYPE_ENTERPRISE) {
                    isEPGroup = true;
                } else if (groupInfo.getType() == MtcImConstants.EN_MTC_GROUP_TYPE_PARTY) {
                    isPartyGroup = true;
                }
                // 当前登录的用户手机号自己是否群主,没有国家码
                LogF.d(TAG, "mAccountUserPhone = " + mAccountUserPhone + ", group master = " + master);
                isOwner = !TextUtils.isEmpty(master) && (master.equals(mAccountUserPhone) || master.endsWith(mAccountUserPhone));
                return master;
            }
        }).runOnMainThread(new Func1<String, Object>() {
            @Override
            public Object call(String master) {
                if (getActivity() == null) {
                    return null;
                }
                mMessageChatListAdapter.setIsGroupChat(true);
                mMessageChatListAdapter.isEPGroup(isEPGroup);
                mMessageChatListAdapter.isPartyGroup(isPartyGroup);

                if (mPreCache) {
                    GroupChatCache.getInstance().getGroupChatCache(new GroupChatCache.OnLoadFinishListener() {
                        @Override
                        public void onLoadFinished(int loadType, int searchPos, long updateTime, long loadStartTime, Bundle bundle) {
                            ArrayList<Message> list = (ArrayList<Message>) bundle.getSerializable("extra_result_data");
                            if (list != null && list.size() > 0) {
                                updateChatListView(loadType, searchPos, bundle);
                            }
                            mPresenter.startInitLoader(list, loadStartTime, updateTime);
                        }
                    }, mPid);
                } else {
                    mPresenter.start();
                }

                //党群标识
                if (isPartyGroup) {
                    ((MessageDetailActivity) getActivity()).getGroupType().setVisibility(View.VISIBLE);
                } else {
                    ((MessageDetailActivity) getActivity()).getGroupType().setVisibility(View.GONE);
                }

                String mLoginUserAddress = LoginUtils.getInstance().getLoginUserName(); // 当前登录的用户手机号 自己是群主才显示 并且是非异网卡
                Log.d(TAG, "login=" + mLoginUserAddress + ", group master=" + master);

                // 判断是不是群主
                if (!TextUtils.isEmpty(master) && master.contains(mLoginUserAddress)) { //
                    isOwner = true;
                    Log.d(TAG, "login=" + mLoginUserAddress + ", group master=" + master);

                } else {
                    isOwner = false;
                }
                return null;
            }
        }).subscribe();
    }

    public void showGroupMsgTip() {
        UmengUtil.buryPoint(mContext, "groupmessage_sms_welcome", "消息-群聊-加号-群短信-欢迎页", 0);

        SpannableStringBuilder sb = new SpannableStringBuilder(getString(R.string.send_group_tip2));
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(mContext.getResources().getColor(R.color.color_0D6CF9));
        sb.setSpan(colorSpan, 2, 16, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        final PopupWindow popupWindow = new PopupWindow(mContext);
        View view = LayoutInflater.from(mContext).inflate(R.layout.activity_group_super_msg, null);
        popupWindow.setContentView(view);

        TextView message = view.findViewById(R.id.tv_tip);
        TextView message2 = view.findViewById(R.id.tv_tip2);
        TextView message3 = view.findViewById(R.id.tv_tip3);
    //    TextView tv_show = view.findViewById(R.id.tv_show);
        TextView group_tip = view.findViewById(R.id.group_tip);
        TextView free_message = view.findViewById(R.id.free_message);


        message3.setVisibility(View.GONE);
    //    tv_show.setVisibility(View.INVISIBLE);
        free_message.setText(getString(R.string.welcome_to_use_group_msg));
        group_tip.setText(getString(R.string.send_group_tip1));
        message.setText(getString(R.string.send_group_tip3));
        message2.setText(sb);
        TextView cancal = view.findViewById(R.id.cancle_btn);
        TextView ok = view.findViewById(R.id.sure_btn);
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        popupWindow.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setWidth(metrics.widthPixels);

        cancal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UmengUtil.buryPoint(mContext, "groupmessage_sms_welcome_later", "消息-群聊-加号-群短信-欢迎页-以后再说", 0);
                popupWindow.dismiss();
            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UmengUtil.buryPoint(mContext, "groupmessage_sms_welcome_done", "消息-群聊-加号-群短信-欢迎页-确定", 0);
                getActivity().getSharedPreferences("config", Context.MODE_PRIVATE).edit().putBoolean("is_first_time_send_group_msgs", false).commit();
                if (GroupNotifyUtils.rawQueryGroupNotifyUnbmer(getActivity(), mAddress) == 0) {
                    GroupSMSEditActivity.start(getActivity(), mAddress); //没有发过群短信的直接去到编辑界面
                } else {
                    GroupSMSActivity.start(getActivity(), mAddress, tvTitle.getText().toString()); //群短信界面
                }
                popupWindow.dismiss();
            }
        });
        ColorDrawable drawable = new ColorDrawable(mContext.getResources().getColor(R.color.white));
        popupWindow.setBackgroundDrawable(drawable);
        popupWindow.showAtLocation(getView(), Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    @Override
    public void onResume() {
        Log.e("time debug", "time onResume ---" + System.currentTimeMillis());
        super.onResume();
    }


    @Override
    protected void onSendClickReport(int type) {
//        HashMap<String, String> map  = new HashMap<String,String>();
//        if(isEPGroup){
//            map.put("message_type","企业群");
//        }else{
//            map.put("message_type","普通群");
//        }
//        switch (type){
//            case SEND_TXT_TYPE:
//                map.put("send_mode","文字");
//                break;
//            case SEND_PIC_TYPE:
//                map.put("send_mode","图片");
//                break;
//            case SEND_AUDIO_TYPE:
//                map.put("send_mode","语音");
//                break;
//            case SEND_CARD_TYPE:
//                map.put("send_mode","名片");
//                break;
//            case SEND_LOCATION_TYPE:
//                map.put("send_mode","定位");
//                break;
//            case SEND_FILE_TYPE:
//                map.put("send_mode","文件");
//                break;
//            case SEND_GIF_TYPE:
//                map.put("send_mode","gif动图");
//                break;
//            case SEND_RED_PACKAGE_TYPE:
//                map.put("send_mode","红包");
//                break;
//            case SEND_APPROV_TYPE:
//                map.put("send_mode","审批");
//                break;
//            case SEND_LOG_TYPE:
//                map.put("send_mode","日志");
//                break;
//            case SEND_SMS_TYPE:
//                map.put("send_mode","短信");
//                break;
//        }
//
//        map.put("click_name","发送类型");
//
//
//        MobclickAgent.onEvent(mContext, "Message_mode", map);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_message_group_detail, menu);
        setItem = menu.findItem(R.id.action_setting);
        multiCallItem = menu.findItem(R.id.action_multicall);
        changeMenu(themeType);
        if(getArguments().getBoolean("sweepcode")){
            showToolBarMenu();
        }else{
            obtainGroupState();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Bundle bundle;
        bundle = getArguments();
        int i1 = item.getItemId();
        if (i1 == R.id.action_multicall) {
            UmengUtil.buryPoint(getActivity(), "message_groupmessage_call", "电话图标", 0);
            hideKeyboardAndTryHideGifView();
            String[] itemList = getResources().getStringArray(R.array.group_msg_call_choose_click);
            MessageOprationDialog messageOprationDialog = new MessageOprationDialog(getContext(), "", itemList, null);
            messageOprationDialog.setOnMessageItemClickListener(new MessageOprationDialog.OnOprationItemClickListener() {
                @Override
                public void onClick(String item, int which, String address) {
                    if (which == 0/*item.equals(getString(R.string.multiparty_call))*/) {
                        //11月29日平台上线boss解耦业务，受限用户能用多方电话
//                        UmengUtil.buryPoint(getActivity(), "message_groupmessage_call_multipartyphone", "多方电话", 0);
//
//                        HashMap<String, String> map  = new HashMap<String,String>();
//                        map.put("source","群聊顶部-多方电话");
//                        map.put("click_name","多方电话调起选择器");
//                        MobclickAgent.onEvent(mContext, "Multipartyphone_click", map);
//
//                        Intent i = ContactProxy.g.getUiInterface().getContactSelectActivityUI().createIntentForMultiCall(getActivity(), mAddress, isEPGroup || isPartyGroup, getArguments().getInt(ContactsSelectActivity.GROUP_TYPE));
//                        startActivity(i);
                        dialMultipartyCall(ContactSelectorUtil.SOURCE_GROUP_TOP_MULTI_CALL);
                    } else if (which == 1/*item.equals(getString(R.string.multi_video_call_toolbar_title))*/) {
//                        UmengUtil.buryPoint(getActivity(), "message_groupmessage_call_multipartyvideo", "多方视频", 0);
//                        String[] permissions;
//                        permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
//                        ((BaseActivity) getActivity()).requestPermissions(new BaseActivity.OnPermissionResultListener() {
//
//                            @Override
//                            public void onAllGranted() {
//                                super.onAllGranted();
//                                LogF.d(TAG, "onAllGranted: ");
//                                Intent i = ContactProxy.g.getUiInterface().getContactSelectActivityUI().createIntentForMultiVideoCall(getActivity(), mAddress, isEPGroup || isPartyGroup, getArguments().getInt(ContactSelectorActivity.GROUP_TYPE));
//                                startActivity(i);
//                            }
//
//                            @Override
//                            public void onAnyDenied(String[] permissions) {
//                                super.onAnyDenied(permissions);
//                                LogF.d(TAG, "onAnyDenied: ");
//                            }
//
//                            @Override
//                            public void onAlwaysDenied(String[] permissions) {
//                                LogF.d(TAG, "onAlwaysDenied: ");
//                                String message = getActivity().getString(R.string.need_video_permission_setting_it);
//                                PermissionDeniedDialog permissionDeniedDialog = new PermissionDeniedDialog(getActivity(), message);
//                                permissionDeniedDialog.show();
//                            }
//                        }, permissions);
                        dialMultipartyVideo();
                    }
                }
            });
            messageOprationDialog.show();
        } else if (i1 == R.id.action_setting) {
            UmengUtil.buryPoint(getActivity(), "message_groupmessage_setup", "设置", 0);
            hideKeyboardAndTryHideGifView();
            // startForResult与singletast冲突
            bundle.putBoolean("isEPgroup", isEPGroup || isPartyGroup);
            //群类型,之后都用这种吧,不要传上面那个了
            bundle.putInt(GROUP_TYPE, getArguments().getInt(GROUP_TYPE, 0));
            Intent intent = new Intent(this.getActivity(), GroupSettingActivity.class);
            intent.putExtras(bundle);
            startActivityForResult(intent, REQUEST_CODE);
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE) {
                if (data != null && data.getBooleanExtra("clear_all_msg", false)) {
                    mBtMsgATTip.setVisibility(View.GONE);
                    mChatRemindHelper.hideMessageCountTip();
                    mPresenter.clearAllMsg();
                    mMessageChatListAdapter.setCanNotLoadMore();
                    mMessageChatListAdapter.setSourceDataForMessageOnly(null, new ArrayList<Message>());
                    mMessageChatListAdapter.notifyDataSetChanged();
//                    onNormalLoadDone(null, false);
                }
            }
        }
        //从MessageDetailActivity-->GalleryActivity 路径
        if (requestCode == OPEN_GALLERY_REQUEST && resultCode == ImgEditorProxy.g.getServiceInterface().getFinalRequestEditPictureStatus()) {
            int imgEditorStatus = data.getIntExtra(ImgEditorProxy.g.getServiceInterface().getFinalImageStatus(), -1);
            if (imgEditorStatus != -1) {
                String imageSavePath = data.getStringExtra(ImgEditorProxy.g.getServiceInterface().getFinalExtraImageSavePath());
                mPresenter.sendEditImage(imageSavePath);
            }
        }

        //从MessageDetailActivity -> PreviewImageActivity 路径
        if (requestCode == PREVIEW_IMAGE_REQUEST && resultCode == ImgEditorProxy.g.getServiceInterface().getFinalRequestEditPictureStatus()) {
            int imgEditorStatus = data.getIntExtra(ImgEditorProxy.g.getServiceInterface().getFinalImageStatus(), -1);
            if (imgEditorStatus != -1) {
                String imageSavePath = data.getStringExtra(ImgEditorProxy.g.getServiceInterface().getFinalExtraImageSavePath());
                mPresenter.sendEditImage(imageSavePath);
            }
        }

        // 多选功能， 转发
        if(requestCode == FORWARD_REQUEST_CODE && resultCode == RESULT_OK){
            if(data != null){
                ((MessageDetailActivity)getActivity()).changeMode(MessageDetailActivity.OUT_MULTI_SELECT_MODE);
                boolean isGroup = data.getBooleanExtra("isGroup",false);
                String number = data.getStringExtra("number");
                messageForwardUtil.handleMessageForward(number,isGroup);
            }
        }
    }

    @Override
    public void updateChatListView(int loadType, int searchPos, Bundle bundle) {
        Log.e("time debug", "time update ---" + System.currentTimeMillis());

        boolean fromMoreMsg = loadType == MessageEditorModelImpl.LOAD_TYPE_MORE;
        boolean hasMore = false;
        int addNum = 0;
        ArrayList<? extends BaseModel> list = null;
        if (bundle != null) {
            hasMore = bundle.getBoolean("extra_has_more", false);
            addNum = bundle.getInt("extra_add_num", 0);
            list = (ArrayList<? extends BaseModel>) bundle.getSerializable("extra_result_data");
        }

        Log.e(TAG, "time update --- fromMoreMsg ：" + fromMoreMsg + " loadType ："+ loadType );
        if (fromMoreMsg) {
            onLoadMoreDone(list, addNum);
        } else {
            if (loadType == MessageEditorModelImpl.LOAD_TYPE_FIRST) {
                onFirstLoadDone(searchPos, list, hasMore);
            } else {
                onNormalLoadDone(list, loadType == MessageEditorModelImpl.LOAD_TYPE_ADD);
            }
        }
    }

    @Override
    public void showTitleName(CharSequence person) {
        Activity activity = getActivity();
        if(activity != null && activity instanceof MessageDetailActivity) {
            tvTitle = ((MessageDetailActivity) activity).getmTvTitle();
            tvTitle.setText(person);
        }
    }


    @Override
    public void reSend(Message msg) {
        mPresenter.resend(msg);
    }

    @Override
    public void sendWithdrawnMessage(Message msg) {
        UmengUtil.buryPoint(mContext, "message_groupmessage_press_recall", "撤回", 0);
        mPresenter.sendWithdrawnMessage(msg);
    }

    @Override
    public void deleteMessage(Message msg) {
        UmengUtil.buryPoint(mContext, "message_groupmessage_press_delete", "删除", 0);
        mPresenter.deleteMessage(msg);
    }

    @Override
    public void deleteMultiMessage(SparseBooleanArray selectList) {
        mPresenter.deleteMultiMessage(selectList);
    }

    @Override
    public void forwardMultiMessage(SparseBooleanArray selectList) {
        mPresenter.forwardMultiMessage(selectList);
    }

    @Override
    public void addToFavorite(Message msg, int chatType, String address) {
        mPresenter.addToFavorite(msg, chatType, address);
    }

    /**
     * 系统消息
     */
    @Override
    public void sysMessage(int type) {
        if (type == 1) { //普通群 （群主才可以操作）
            if (isOwner && !isEPGroup && !isPartyGroup) {
                String identify = GroupChatUtils.getIdentify(getActivity(), mAddress);
                String person = GroupChatUtils.getPerson(getActivity(), mAddress);
                if (TextUtils.isEmpty(identify) || TextUtils.isEmpty(person)) {
                    return;
                }
                Intent intent = new Intent(getActivity(), NonEntryGroupAtivity.class); //
                intent.putExtra(NonEntryGroupAtivity.GROUPID, mAddress);
                intent.putExtra(NonEntryGroupAtivity.GROUPURI, identify);
                intent.putExtra(NonEntryGroupAtivity.GROUPNAME, person);
                getActivity().startActivity(intent);
            } else {
                Toast.makeText(mContext, mContext.getResources().getString(R.string.you_have_no_authority), Toast.LENGTH_SHORT).show();
            }
        } else if (type == 2) {
            if (isEPGroup || isPartyGroup) {
                String identify = GroupChatUtils.getIdentify(getActivity(), mAddress);
                String person = GroupChatUtils.getPerson(getActivity(), mAddress);
                if (TextUtils.isEmpty(identify) || TextUtils.isEmpty(person)) {
                    return;
                }
                Intent intent = new Intent(getActivity(), NoRcsGroupMemberActivity.class); //
                intent.putExtra(NoRcsGroupMemberActivity.GROUPID, mAddress);
                intent.putExtra(NoRcsGroupMemberActivity.GROUPURI, identify);
                intent.putExtra(NoRcsGroupMemberActivity.GROUPNAME, person);
                getActivity().startActivity(intent);
            }
        }
    }

    @Override
    public void showToast(final String toast) {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (getActivity() == null) {
                    return;
                }
                BaseToast.show(MyApplication.getAppContext(), toast);
            }
        });
    }

    @Override
    protected void loadMoreMessages() {
        mPresenter.loadMoreMessages();
    }

    @Override
    public void sendMessage() {
        UmengUtil.buryPoint(MyApplication.getAppContext(), "message_groupmessage_send", "消息-群聊-发送按钮", 0);
        if (TextUtils.isEmpty(mAddress)) {// 群没有创建成功时不能发消息
            return;
        }

        ArrayList<AtMemberLength> memberList = getAtMemberLengthList();
        if (mIsAtAll) {
            mPresenter.sendMessageAtAll(mEtMessage.getText().toString(), messageSize);
            mIsAtAll = false;
            mAtAllSelectionStart = 0;
            mAtAllSelectionEnd = 0;
        } else if (memberList != null && memberList.size() > 0) {
            // 群 @ 消息
            StringBuilder sb = new StringBuilder();
            for (AtMemberLength atMemberLength : memberList) {
                sb.append(';').append(atMemberLength.groupMember.getAddress());
            }
            if (sb.length() > 0)
                sb.deleteCharAt(0);
            mPresenter.sendMessageAt(mEtMessage.getText().toString(), messageSize, sb.toString());
            memberList.clear();
        } else {
            if (TextUtils.isEmpty(mEtMessage.getText().toString().trim())) {
                return;
            }

            if (mNeedSendAudio) {
                File file = new File(FileUtil.DIR_PUBLLIC_XUN_FEI_AUDIO_PATH);
                mNeedSendAudio = false;
                if (file.exists()) {
                    try {
                        MediaPlayer mediaPlayer = new MediaPlayer();
                        mediaPlayer.setDataSource(FileUtil.DIR_PUBLLIC_XUN_FEI_AUDIO_PATH);
                        mediaPlayer.prepare();
                        mPresenter.sendMessage(mEtMessage.getText().toString(), messageSize);
                    } catch (Exception e) {
                        LogF.e(TAG, "get recoder duration exception");
                    }
                }
            } else {
                // 正常消息
                mPresenter.sendMessage(mEtMessage.getText().toString(), messageSize);
            }
        }
    }

    public void onEvent(int event) {
        if (event == MessageModuleConst.EventType.GROUP_CHAT_FRAGMENT_OPEN_RICH_MEDIA_GROUP_SMS_ITME) {
            openRichMediaGroupSmsItme();
        }

    }

    @Override
    protected void sendImgAndVideo(ArrayList<MediaItem> items) {
        mPresenter.sendImgAndVideo(items, false);
    }

    @Override
    protected void sendImgAndVideo(ArrayList<MediaItem> items, boolean isOriginPhoto) {
        mPresenter.sendImgAndVideo(items, isOriginPhoto);
    }

    @Override
    protected void sendLocation(double dLatitude, double dLongitude, float fRadius, String pcLabel, String detailAddress) {
        mPresenter.sendLocation(dLatitude, dLongitude, fRadius, pcLabel, detailAddress);
    }

    @Override
    protected void sendFileMsg(Intent data) {
        mPresenter.sendFileMsg(data);
    }

    @Override
    protected void initPresenter(Bundle bundle) {
        mPresenter = new GroupChatPresenterImpl(this.getActivity(), this, getLoaderManager(), bundle);
        messageForwardUtil = new MessageForwardUtil(this.getActivity());
    }

    public MessageForwardUtil getMessageForwardUtil(){
        return this.messageForwardUtil;
    }

    @Override
    protected Message getDraftMessage() {
        return mPresenter.getDraftMessage();
    }

    @Override
    protected void saveDraftMessage(boolean save, Message msg) {
        mPresenter.saveDraftMessage(save, msg);
    }

    @Override
    public int getChatType() {
        return MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT;
    }

    @Override
    protected void clearUnreadCount() {
        mPresenter.updateUnreadCount();
    }

    @Override
    protected void changeMenu(int themeOption) {
        themeType = themeOption;//menu的加载在主题初始化之后，所以用themeType先保存一下。
        if (setItem == null) {
            return;
        }
        int setDrawable = 0;
        int multiCallDrawable = 0;
        switch (themeOption) {
            case MessageBgSetActivity.THEME_0NE:
                setDrawable = R.drawable.message_setting_selector;
                multiCallDrawable = R.drawable.selector_tool_bar_multiparty_call;
                break;
            case MessageBgSetActivity.THEME_TWO:
                setDrawable = R.drawable.selector_message_menu_setting_two;
                multiCallDrawable = R.drawable.group_multicall_selector_two;
                break;
            case MessageBgSetActivity.THEME_THREE:
                setDrawable = R.drawable.selector_message_menu_setting_two;
                multiCallDrawable = R.drawable.group_multicall_selector_two;
                break;
            case MessageBgSetActivity.THEME_FOUR:
                setDrawable = R.drawable.selector_message_set_theme_three;
                multiCallDrawable = R.drawable.group_multicall_selector_three;
                break;
            case MessageBgSetActivity.THEME_FIVE:
                setDrawable = R.drawable.selector_message_set_theme_four;
                multiCallDrawable = R.drawable.group_multicall_selector_four;
                break;
            case MessageBgSetActivity.THEME_SIX:
                setDrawable = R.drawable.selector_message_set_theme_five;
                multiCallDrawable = R.drawable.group_multicall_selector_five;
                break;
            case MessageBgSetActivity.THEME_SEVEN:
                setDrawable = R.drawable.selector_message_set_theme_seven;
                multiCallDrawable = R.drawable.group_multicall_selector_seven;
                break;
            case MessageBgSetActivity.THEME_EIGHT:
                setDrawable = R.drawable.selector_message_set_theme_eight;
                multiCallDrawable = R.drawable.group_multicall_selector_eight;
                break;
        }
        setItem.setIcon(setDrawable);
        multiCallItem.setIcon(multiCallDrawable);
    }

    //发送名片
    public void sendVcard(String pcUri, String pcSubject, String pcFileName, long duration) {
        mPresenter.sendVcard(pcUri, pcSubject, pcFileName, duration);
    }

    @Override
    public void senAudioMessage(String path, long lon) {
        Map<String, String> map = new HashMap();
        map.put("time", String.valueOf(lon / 1000));
        UmengUtil.buryPoint(mContext, "message_groupmessage_talktime", map);
        mPresenter.sendAudio(path, lon);
    }

    @Override
    public void senAudioMessage(String path, long lon, String detail) {
        Map<String, String> map = new HashMap();
        map.put("time", String.valueOf(lon / 1000));
        UmengUtil.buryPoint(mContext, "message_groupmessage_talktime", map);
        mPresenter.sendAudio(path, lon, detail);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return super.onTouch(v, event);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        ConversationUtils.setNotify(getActivity(), mAddress, -1);
        super.onDestroy();
    }

    public String getAddress() {
        return mPresenter.getAddress();
    }

    @Override
    public void hideSuperMsg() {
        handleSuperMsmVisible(false);
    }

    /**
     * 获取群的状态
     */
    private void obtainGroupState() {
        final Activity activity = getActivity();
        new Thread(new Runnable() {
            @Override
            public void run() {
                GroupInfo groupInfo = GroupChatUtils.getGroupInfo(activity, mAddress);
                if (groupInfo != null) {
                    LogF.i(TAG, "rcsImCbSessReleased 群设置不显示 dwSessId = 5 grouID = " + groupInfo.getId() + " status = " + groupInfo.getStatus());
                }
                if (groupInfo == null || groupInfo.getStatus() == -1 ||groupInfo.getStatus() == Status.STATUS_BE_KICKED) { // 只有在被踢出群的（状态）下隐藏这两个菜单
                    if(activity !=null){
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (setItem != null) {
                                    setItem.setVisible(false);
                                }
                                if (multiCallItem != null) {
                                    multiCallItem.setVisible(false);
                                }
                            }
                        });
                    }
                }
            }
        }).start();
    }

    @Override
    public void sendSuperMessage(String msg) { }

    @Override
    public void hideToolBarMenu() {
        setItem.setVisible(false);
        multiCallItem.setVisible(false);
    }

    @Override
    public void showToolBarMenu() {
        setItem.setVisible(true);
        multiCallItem.setVisible(true);
    }

    /**
     * 打开群短信
     */
    public void openRichMediaGroupSmsItme() {
        UmengUtil.buryPoint(mContext, "message_groupmessage_more_sms", "消息-群聊-加号-群短信（移动群主）", 0);
        boolean isFirstTime = getActivity().getSharedPreferences("config", Context.MODE_PRIVATE).getBoolean("is_first_time_send_group_msgs", true);
        if (isFirstTime) {
            showGroupMsgTip();
        } else {
            if (GroupNotifyUtils.rawQueryGroupNotifyUnbmer(getActivity(), mAddress) == 0) {
                GroupSMSEditActivity.start(getActivity(), mAddress); //没有发过群短信的直接去到编辑界面
            } else {
                GroupSMSActivity.start(getActivity(), mAddress, tvTitle.getText().toString()); //群短信界面
            }
        }
    }

    @Override
    protected void richMediaApprovalItme() {
        String url = "https://117.136.240.96/app.heApproveH5/index.html?groupid=";//正式服
        //String url = "http://112.13.96.207:18082/app.heApproveH5/index.html?groupid=";//测试服
        url += mOaArgs;
        WebConfig config = new WebConfig.Builder().enableRequestToken(true).build(url);
        config.mGroupAddress = mAddress;
        //设置标志位， 标志目标企业的类型， 主要功能是来设置企业联系人选择器的“面包屑”的字段为"企业通讯录"
        config.mEnterpriseType = EnterpriseContactNameCompatHelper.getTypeByCreateChannelInWorkPlatform(EnterpriseContactNameCompatHelper.CHANNEL_TYPE_ANDFECTION);
        EnterPriseProxy.g.getUiInterface().gotoEnterpriseH5Activity(mContext, config);
    }

    @Override
    protected void richMediaLogItme() {
        String url = "https://117.136.240.96/heReport/views/formList.html?groupid=";//正式服
        //String url = "http://112.13.96.207:18082/app.reportH5/index.html?groupid=";//测试服
        url += mOaArgs;
        WebConfig config = new WebConfig.Builder().enableRequestToken(true).build(url);
        config.mGroupAddress = mAddress;
        config.mEnterpriseType = EnterpriseContactNameCompatHelper.getTypeByCreateChannelInWorkPlatform(EnterpriseContactNameCompatHelper.CHANNEL_TYPE_ANDFECTION);
        EnterPriseProxy.g.getUiInterface().gotoEnterpriseH5Activity(mContext, config);
    }

    /**
     * 多方电话
     */
    protected void dialMultipartyCall(){

        HashMap<String, String> map  = new HashMap<String,String>();
        map.put("source","群聊-加号-多方电话");
        map.put("click_name","多方电话调起选择器");
        MobclickAgent.onEvent(mContext, "Multipartyphone_click", map);

//        Intent i = ContactProxy.g.getUiInterface().getContactSelectActivityUI().createIntentForMultiCall(getActivity(), mAddress, isEPGroup || isPartyGroup, getArguments().getInt(ContactsSelectActivity.GROUP_TYPE));
        Intent i = ContactProxy.g.getUiInterface().getContactSelectActivityUI().createIntentForMultiCall(getActivity(), mAddress, isEPGroup || isPartyGroup, getArguments().getInt(GROUP_TYPE));
        startActivity(i);
    }

    protected void dialMultipartyCall(int source){

        HashMap<String, String> map  = new HashMap<String,String>();
        map.put("source","群聊-加号-多方电话");
        map.put("click_name","多方电话调起选择器");
        MobclickAgent.onEvent(mContext, "Multipartyphone_click", map);

    //    UmengUtil.buryPoint(getActivity(), "message_groupmessage_call_multipartyphone", "多方电话", 0);
        Intent i = ContactProxy.g.getUiInterface().getContactSelectActivityUI().createIntentForMultiCall(getActivity(), mAddress, isEPGroup || isPartyGroup, getArguments().getInt(ContactSelectorActivity.GROUP_TYPE),source);
        startActivity(i);
    }

    /**
     * 多方视频
     */
    protected void dialMultipartyVideo(){
        UmengUtil.buryPoint(getActivity(), "message_groupmessage_more_multipartyvideo", "群聊-加号-多方视频", 0);
        String[] permissions;
        permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        ((BaseActivity) getActivity()).requestPermissions(new BaseActivity.OnPermissionResultListener() {

            @Override
            public void onAllGranted() {
                super.onAllGranted();
                LogF.d(TAG, "onAllGranted: ");
                Intent i = ContactProxy.g.getUiInterface().getContactSelectActivityUI().createIntentForMultiVideoCall(getActivity(), mAddress, isEPGroup || isPartyGroup, getArguments().getInt(GROUP_TYPE));
                startActivity(i);
            }

            @Override
            public void onAnyDenied(String[] permissions) {
                super.onAnyDenied(permissions);
                LogF.d(TAG, "onAnyDenied: ");
            }

            @Override
            public void onAlwaysDenied(String[] permissions) {
                LogF.d(TAG, "onAlwaysDenied: ");
                String message = getActivity().getString(R.string.need_video_permission_setting_it);
                PermissionDeniedDialog permissionDeniedDialog = new PermissionDeniedDialog(getActivity(), message);
                permissionDeniedDialog.show();
            }
        }, permissions);
    }

    /**
     * 消息富媒体红点提示
     */
    protected void rmRedTipDisappear(){
        super.rmRedTipDisappear();
        SharePreferenceUtils.setParam(MyApplication.getAppContext(), RM_MORE_NOT_TIP_KEY_GORUP, true);

    }

    public void addressCleared(){
        if(mPresenter!=null && mPresenter instanceof GroupChatPresenterImpl){
            ((GroupChatPresenterImpl)mPresenter).isShowTaost();
        }
    }


}