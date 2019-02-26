package com.cmicc.module_message.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.app.module.proxys.moduleimgeditor.ImgEditorProxy;
import com.app.util.StrangerEnterpriseUtil;
import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.utils.StringUtil;
import com.chinamobile.app.yuliao_business.model.BaseModel;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.application.MyApplication;
import com.chinamobile.app.yuliao_common.utils.Log;
import com.chinamobile.app.yuliao_common.utils.PopWindowFor10GUtil;
import com.chinamobile.app.yuliao_common.utils.SharePreferenceUtils;
import com.chinamobile.app.yuliao_common.view.PopWindowFor10G;
import com.chinamobile.app.yuliao_core.db.LoginDaoImpl;
import com.chinamobile.app.yuliao_core.util.NumberUtils;
import com.cmicc.module_message.ui.activity.MessageBgSetActivity;
import com.cmicc.module_message.ui.activity.MessageDetailActivity;
import com.cmicc.module_message.ui.activity.OneToOneSettingActivity;
import com.cmcc.cmrcs.android.ui.dialogs.CheckBoxDialog;
import com.cmcc.cmrcs.android.ui.dialogs.IKnowDialog;
import com.cmcc.cmrcs.android.ui.model.MediaItem;
import com.cmicc.module_message.ui.model.impls.MessageEditorModelImpl;
import com.cmcc.cmrcs.android.ui.utils.CallRecordsUtils;
import com.cmcc.cmrcs.android.ui.utils.ConvCache;
import com.cmcc.cmrcs.android.ui.utils.LoginUtils;
import com.cmicc.module_message.utils.MessageCache;
import com.cmcc.cmrcs.android.ui.utils.MessageForwardUtil;
import com.cmcc.cmrcs.android.ui.utils.PhoneUtils;
import com.cmcc.cmrcs.android.ui.utils.UmengUtil;
import com.cmcc.cmrcs.android.ui.view.MessageOprationDialog;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.constract.MessageEditorContracts;
import com.cmicc.module_message.ui.presenter.MessageEditorPresenterImpl;
import com.constvalue.MessageModuleConst;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by GuoXietao on 2017/3/21.
 */

public class MessageEditorFragment extends BaseChatFragment {

    private static final String TAG = "MessageEditorFragment";

    protected static String RM_MORE_NOT_TIP_KEY_SING= "rm_more_not_tip_key_sing";
    private MessageEditorContracts.Presenter mPresenter;
    private Bundle bundle;
    private MenuItem setItem;
    private MenuItem callItem;
    private int themeType = 0;
    private PopWindowFor10G m10GPopWindow;
    private TextView tvTitle;
    private View mPop10GDropView;
    private MessageForwardUtil messageForwardUtil;

    public static final int LOGIN_USERNAME_REQUEST = 200;

    public static MessageEditorFragment newInstance(Bundle bundle) {
        MessageEditorFragment fragment = new MessageEditorFragment();
        fragment.setArguments(bundle);
        return fragment;
    }
    public void initViews(View rootView){
        super.initViews(rootView);

    }
    @Override
    public void initData() {
        Log.d(TAG, "MessageEditorFragment init");
        bundle = getArguments();
        mStrangerEnterPriseStr = bundle.getString(MessageModuleConst.INTENT_KEY_FOR_STRANGER_ENTERPRISE);
        super.initData();

        mAddress = NumberUtils.getNumForStore(mAddress);
        if(mPreCache){
            MessageCache.getInstance().getMessageCache(new MessageCache.OnLoadFinishListener() {
                @Override
                public void onLoadFinished(int loadType, int searchPos, long updateTime, long loadStartTime, Bundle bundle) {
                    ArrayList<Message> list = (ArrayList<Message>) bundle.getSerializable("extra_result_data");
                    if(list.size()>0) {
                        updateChatListView(loadType, searchPos, bundle);
                    }
                    mPresenter.startInitLoader(list, loadStartTime, updateTime);
                }
            }, mPid);
        }else {
            mPresenter.start();
        }
        showSmsTipsDialog();
        boolean isNotTip = (boolean) SharePreferenceUtils.getParam(MyApplication.getAppContext(), RM_MORE_NOT_TIP_KEY_SING, false);
        if(!isNotTip){
            moreRedDot.setVisibility(View.VISIBLE);
        }else{
            moreRedDot.setVisibility(View.GONE);
        }
  //      boolean isNotShowStrangerTip = StrangerEnterpriseUtil.getNotTipState(mAddress);
//        if(!TextUtils.isEmpty(mStrangerEnterPriseStr) && isNotShowStrangerTip == false){
//            mStrangerSaveTipViewStub.inflate().findViewById(R.id.save_stranger_contact).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Bundle bundle = new Bundle();
//                    bundle.putString(CONTACT_NUMBER, mAddress);
//                    bundle.putString(CONTACT_NAME,mPerson);
////                            bundle.putString(CONTACT_RAWID,mRawId+"");
//                    bundle.putBoolean(IS_NEW_NOT_EDIT, true);
//                    String[] info =  new String[]{mStrangerEnterPriseStr,"",""};
//                    bundle.putStringArray(CONTACT_DETAIL, info);
//                    ContactProxy.g.getUiInterface().startNewContactActivity(mContext,bundle);
//                }
//            });
//        }
    }

    @Override
    protected boolean isNeedShowStrangerTip(){
        if(!TextUtils.isEmpty(mStrangerEnterPriseStr) && StrangerEnterpriseUtil.getNotTipState(mAddress) == false){
            Message msg = new Message();
            msg.setType(Type.TYPE_MSG_STRANGERPTIP);
            msg.setAddress(mAddress);
            msg.setPerson(mPerson);
            msg.setId(mRawId);
            msg.setBody(mStrangerEnterPriseStr);
            ArrayList<Message> msgList = new ArrayList<>();
            msgList.add(msg);
            msgList.addAll(mMessageChatListAdapter.getDataList());
            mMessageChatListAdapter.setDataList(msgList);

            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        Log.e("time debug", "time onResume ---" + System.currentTimeMillis());
        super.onResume();

    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        StrangerEnterpriseUtil.setNotTipState(mAddress,true);
    }

    @Override
    protected void onSendClickReport(int type) {
//        HashMap<String, String> map  = new HashMap<String,String>();
//        map.put("message_type","点对点");
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

    private void showSmsTipsDialog() {
        boolean isFirstShow = (boolean) SharePreferenceUtils.getDBParam(mContext, "first_show_sms_tips", true);
        Log.d(TAG, "showSmsTipsDialog: isFirstShow "+ isFirstShow);
        String address = LoginUtils.getInstance().getLoginUserName();
        if (!PhoneUtils.isHongKongNumber(address) && isFirstShow &&
                !(ConvCache.getInstance().SMS_STATUS_CACHE.get(mAddress) == Boolean.TRUE || isSpeticalNum(bundle) || mIsFromSms)) {
            CheckBoxDialog dialog = new CheckBoxDialog(mContext, mContext.getString(R.string.sms_tips_title), mContext.getString(R.string.sms_content));
            dialog.setContentUseHtml(mContext.getString(R.string.sms_content));
            dialog.setOnConfirmClickListener(new CheckBoxDialog.OnConfirmClickListener() {
                @Override
                public void onClick() {
                    SharePreferenceUtils.setDBParam(mContext, "first_show_sms_tips", false);
                }
            });
            dialog.show();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(TAG, "onNewIntent: "+ menu.size());
        menu.clear();
        inflater.inflate(R.menu.menu_message_detail, menu);
        setItem = menu.findItem(R.id.action_setting);
        callItem = menu.findItem(R.id.action_call);
        changeMenu(themeType);
        m10GPopWindow = new PopWindowFor10G(getActivity());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Log.d(TAG, "onNewIntent: "+ bundle.getString("address"));
        int i = item.getItemId();
        if (i == R.id.action_call) {
            UmengUtil.buryPoint(getActivity(), "message_p2pmessage_call","电话",0);

            hideKeyboardAndTryHideGifView();

            String[] itemList = null;
//            //判断是否为本网号码
//            if(AndroidUtil.isCMCCMobileByNumber(LoginUtils.getInstance().getLoginUserName())){
//                String num = LoginDaoImpl.getInstance().queryLoginUser(mContext);
//
//                int leftDuration = (int)SharePreferenceUtils.getParam(mContext,num + CallRecordsUtils.MULTI_CALL_DURATION_LEFT,0);
//                if(leftDuration <= 0){
//                    itemList = getResources().getStringArray(R.array.one_to_one_msg_call_choose_click);// 普通语音视屏
//                }else {
//                    itemList = getResources().getStringArray(R.array.one_to_one_msg_call_choose_click_more);// 和飞信（免费）普通语音视屏
//                }
//
//            }else{
//                itemList = getResources().getStringArray(R.array.one_to_one_msg_call_choose_click); // 普通语音视屏
//            }

            itemList = getResources().getStringArray(R.array.one_to_one_msg_call_hfxandcall);
            MessageOprationDialog messageOprationDialog = new MessageOprationDialog(getContext(), "", itemList, null);
            messageOprationDialog.setOnMessageItemClickListener(new MessageOprationDialog.OnOprationItemClickListener() {
                @Override
                public void onClick(String item, int position, String address) {
                    if (item.equals(getString(R.string.normal_call))|| position == 1) {
                        UmengUtil.buryPoint(getActivity(), "message_p2pmessage_call_cs","普通电话",0);

                        HashMap<String, String> map  = new HashMap<String,String>();
                        map.put("call_type","普通电话");
                        map.put("person","2");
                        MobclickAgent.onEvent(mContext, "Call_event", map);

                        CallRecordsUtils.normalCall(getActivity(), mAddress);
                    } else if (item.equals(getString(R.string.video_call))) {

                        UmengUtil.buryPoint(getActivity(), "message_p2pmessage_call_video","视频通话",0);
                        if(PopWindowFor10GUtil.isNeedPop()) {
                            m10GPopWindow.setType(PopWindowFor10GUtil.TYPE_FOR_VIDEO_CALL);
                            m10GPopWindow.setCallerInfo(mAddress, mPerson);
                            m10GPopWindow.setJustDismiss(true);
                            m10GPopWindow.showAsDropDown(mPop10GDropView, 0,0, Gravity.TOP);
                        }else {
                            CallRecordsUtils.voiceCall(getActivity(),mAddress,true,mPerson);
                        }
                    } else if(item.equals(getString(R.string.smart_voice_call))){
                        UmengUtil.buryPoint(getActivity(), "message_p2pmessage_call_voice","语音电话",0);

                        if(PopWindowFor10GUtil.isNeedPop()) {
                            m10GPopWindow.setType(PopWindowFor10GUtil.TYPE_FOR_CALL);
                            m10GPopWindow.setCallerInfo(mAddress, mPerson);
                            m10GPopWindow.setJustDismiss(true);
                            m10GPopWindow.showAsDropDown(mPop10GDropView, 0, 0, Gravity.TOP);
                        }else {
                            CallRecordsUtils.voiceCall(getActivity(),mAddress,false,mPerson);
                        }
                    }else if(item.equals(getString(R.string.andfetion_call)) || position == 0){
                        diaHeFeiXinCall();
                    }
                }
            });
            messageOprationDialog.show();

        } else if (i == R.id.action_setting) {
            UmengUtil.buryPoint(getActivity(), "message_p2pmessage_setup","聊天设置",0);

            hideKeyboardAndTryHideGifView();
//            OneToOneSettingActivity.start(getActivity(), bundle);
            // startForResult与singletast冲突
            OneToOneSettingActivity.startForResult(getActivity(), LOGIN_USERNAME_REQUEST, bundle);

        }
        return false;
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
        tvTitle= getActivity().findViewById(R.id.title);
        tvTitle.setText(person);
        mPop10GDropView = getActivity().findViewById(R.id.pop_10g_window_drop_view);
    }

    @Override
    public void reSend(Message msg) {
        mPresenter.reSend(msg);
    }

    @Override
    public void sendWithdrawnMessage(Message msg) {
        UmengUtil.buryPoint(mContext,"message_p2pmessage_press_recall","撤回",0);
        mPresenter.sendWithdrawnMessage(msg);
    }

    @Override
    public void deleteMessage(Message msg) {
        UmengUtil.buryPoint(mContext,"message_p2pmessage_press_delete","删除",0);
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

    @Override
    public void sysMessage(int type) {

    }

    @Override
    public void showToast(String toast) {

    }

    @Override
    protected void loadMoreMessages() {
        mPresenter.loadMoreMessages();
    }

    @Override
    public void sendMessage() {
        UmengUtil.buryPoint(MyApplication.getAppContext(), "message_p2pmessage_send", "消息-点对点-发送按钮", 0);
        if(TextUtils.isEmpty(mEtMessage.getText().toString().trim())){
            return;
        }
        mPresenter.sendMessage(mEtMessage.getText().toString(),messageSize);
    }

    @Override
    public void sendVcard(String pcUri, String pcSubject, String pcFileName, long duration){
        mPresenter.sendVcard( pcUri,  pcSubject,  pcFileName,  duration);
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
    protected void sendLocation(double dLatitude, double dLongitude, float fRadius, String pcLabel,String detailAddress) {
        mPresenter.sendLocation(dLatitude,dLongitude,fRadius,pcLabel,detailAddress);
    }

    @Override
    protected void sendFileMsg(Intent data) {
        mPresenter.sendFileMsg(data);
    }

    @Override
    protected void initPresenter(Bundle bundle) {
        mPresenter = new MessageEditorPresenterImpl(this.getActivity(), this, getLoaderManager(), bundle);
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
        return MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT;
    }

    @Override
    protected void clearUnreadCount() {
        mPresenter.updateUnreadCount();
    }

    @Override
    protected void changeMenu(int themeOption) {
        themeType = themeOption;//menu的加载在主题初始化之后，所以用themeType先保存一下。
        if(setItem == null || callItem ==null){
            return;
        }
        int setDrawable=0;
        int callDrawable = 0;
        switch (themeOption){
            case MessageBgSetActivity.THEME_0NE:
                setDrawable = R.drawable.message_setting_selector;
                callDrawable = R.drawable.message_call_selector_theme_two;
                break;
            case MessageBgSetActivity.THEME_TWO:
                setDrawable = R.drawable.selector_message_set_theme_two;
                callDrawable = R.drawable.message_call_selector_theme_two;
                break;
            case MessageBgSetActivity.THEME_THREE:
                setDrawable = R.drawable.selector_message_set_theme_two;
                callDrawable = R.drawable.message_call_selector_theme_two;
                break;
            case MessageBgSetActivity.THEME_FOUR:
                setDrawable = R.drawable.selector_message_set_theme_three;
                callDrawable = R.drawable.selector_message_call_theme_three;
                break;
            case MessageBgSetActivity.THEME_FIVE:
                setDrawable = R.drawable.selector_message_set_theme_four;
                callDrawable = R.drawable.selector_message_call_theme_four;
                break;
            case MessageBgSetActivity.THEME_SIX:
                setDrawable = R.drawable.selector_message_set_theme_five;
                callDrawable = R.drawable.selector_message_call_theme_five;
                break;
            case MessageBgSetActivity.THEME_SEVEN:
                setDrawable = R.drawable.selector_message_set_theme_seven;
                callDrawable = R.drawable.selector_message_call_theme_seven;
                break;
            case MessageBgSetActivity.THEME_EIGHT:
                setDrawable = R.drawable.selector_message_set_theme_eight;
                callDrawable = R.drawable.selector_message_call_theme_eight;
                break;
        }
        setItem.setIcon(setDrawable);
        callItem.setIcon(callDrawable);
    }

    @Override
    protected void sendSmsMessage(String msg) {
        mPresenter.sendSuperMessage(msg);
    }

    @Override
    protected void sendSysSmsMessage(String msg) {
        mPresenter.sendSysMessage(msg);
    }

    @Override
    public void senAudioMessage(String path, long lon) {
        Map<String, String> map = new HashMap();
        map.put("time", String.valueOf(lon / 1000));
        UmengUtil.buryPoint(mContext, "message_p2pmessage_talktime", map);
        mPresenter.sendAudio(path, lon);
    }

    @Override
    public void senAudioMessage(String path, long lon, String detail) {
        Map<String, String> map = new HashMap();
        map.put("time", String.valueOf(lon / 1000));
        UmengUtil.buryPoint(mContext, "message_p2pmessage_talktime", map);
        mPresenter.sendAudio(path, lon, detail);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return super.onTouch(v ,event);
    }

    public String getAddress() {
        return mPresenter.getAddress();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OPEN_GALLERY_REQUEST &&resultCode == ImgEditorProxy.g.getServiceInterface().getFinalRequestEditPictureStatus()) {
            int imgEditorStatus = data.getIntExtra(ImgEditorProxy.g.getServiceInterface().getFinalImageStatus(), -1);
            if (imgEditorStatus != -1) {
                String imageSavePath = data.getStringExtra(ImgEditorProxy.g.getServiceInterface().getFinalExtraImageSavePath());
                mPresenter.sendEditImage(imageSavePath);
            }
        }

        //从MessageDetailActivity -> PreviewImageActivity 路径
        if(requestCode == PREVIEW_IMAGE_REQUEST &&resultCode == ImgEditorProxy.g.getServiceInterface().getFinalRequestEditPictureStatus()){
            int imgEditorStatus = data.getIntExtra(ImgEditorProxy.g.getServiceInterface().getFinalImageStatus(), -1);
            if (imgEditorStatus != -1) {
                String imageSavePath = data.getStringExtra(ImgEditorProxy.g.getServiceInterface().getFinalExtraImageSavePath());
                mPresenter.sendEditImage(imageSavePath);
            }
        }

        if(requestCode == LOGIN_USERNAME_REQUEST && resultCode == Activity.RESULT_OK){
            if(!TextUtils.isEmpty(data.getStringExtra("userName"))){
                tvTitle.setText(data.getStringExtra("userName"));
            }
        }

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
    public void sendSuperMessage(String msg) {
        mPresenter.sendSuperMessage(msg);
    }

    @Override
    public void hideToolBarMenu() {
        setItem.setVisible(false);
        callItem.setVisible(false);
    }

    @Override
    public void showToolBarMenu() {
        setItem.setVisible(true);
        callItem.setVisible(true);
    }

    public static boolean checkCallPhone(final Activity activity, final String dialNum) {
        if (StringUtil.isEmpty(dialNum)) {
            BaseToast.show(R.string.num_empty);
            return false;
        }

        if (!PhoneUtils.isPhoneNumber(dialNum)) {
            BaseToast.show(R.string.num_wrong);
            return false;
        }

        if (!AndroidUtil.isNetworkConnected(activity)) {
            BaseToast.show(R.string.network_disconnect);
            return false;
        }

        String userNum = LoginDaoImpl.getInstance().queryLoginUser(activity);
        if (!TextUtils.isEmpty(userNum) && userNum.equals(dialNum)) {
            BaseToast.show(R.string.call_self);
            return false;
        }
        return true;
    }

    private void saveFirstFetionCall() {
        SharedPreferences misscallinfo = mContext.getSharedPreferences("first_fetion_call_info",MODE_PRIVATE);
        SharedPreferences.Editor editor = misscallinfo.edit();//获取Editor
        editor.putBoolean("is_first_fetion_call", false);
        editor.commit();

    }
    private boolean getFirstFetionCall() {
        SharedPreferences misscallinfo = mContext.getSharedPreferences("first_fetion_call_info", MODE_PRIVATE);
        boolean isFrist = misscallinfo.getBoolean("is_first_fetion_call", true);
        return isFrist;
    }

    /**
     * 和飞信电话
     */
    protected void diaHeFeiXinCall(){
        if(getFirstFetionCall()){
            IKnowDialog sureDialog = new IKnowDialog(mContext, getString(com.cmic.module_base.R.string.calltype_fetioncall_tip), getString(com.cmic.module_base.R.string.i_know));
            sureDialog.mBtnOk.setTextColor(0xFF4184F3);
            sureDialog.setCanceledOnTouchOutside(false);
            sureDialog.show();
            sureDialog.setOnSureClickListener(new IKnowDialog.OnSureClickListener() {
                @Override
                public void onClick() {
                    ArrayList<String> list = new ArrayList<>();
                    list.add(mAddress);
                    if (checkCallPhone(getActivity(), mAddress)) {

                        HashMap<String, String> map  = new HashMap<String,String>();
                        map.put("ref","点对点-加号-和飞信电话");
                        map.put("person","2");
                        map.put("click_name","呼出位置");
                        MobclickAgent.onEvent(mContext, "Multipartyphone_click", map);

                        CallRecordsUtils.multipartyCall(getActivity(), mPerson, list, 0);
                        saveFirstFetionCall();
                    }

                }
            });
        }else {
            HashMap<String, String> map  = new HashMap<String,String>();
            map.put("ref","点对点-加号-和飞信电话");
            map.put("person","2");
            map.put("click_name","呼出位置");
            MobclickAgent.onEvent(mContext, "Multipartyphone_click", map);

            ArrayList<String> list = new ArrayList<>();
            list.add(mAddress);
            CallRecordsUtils.multipartyCall(getActivity(), mPerson, list, 0);
        }
    }

    /**
     * 拨打网络通话
     */
    protected void dialNetworkCall(){
        UmengUtil.buryPoint(getActivity(), "message_p2pmessage_more_ipcall","点对点-加号-音视频电话",0);
        hideKeyboardAndTryHideGifView();
        String[] itemList  = getResources().getStringArray(R.array.network_call_voiceandvideo_array); // 普通语音视屏
        MessageOprationDialog messageOprationDialog = new MessageOprationDialog(getContext(), "", itemList, null);
        messageOprationDialog.setOnMessageItemClickListener(new MessageOprationDialog.OnOprationItemClickListener() {
            @Override
            public void onClick(String item, int which, String address) {
                if (item.equals(getString(R.string.network_call_video))) {
                    UmengUtil.buryPoint(getActivity(), "p2pmessage_more_ipcall_video","点对点-加号-音视频电话-视频电话",0);
                    if(PopWindowFor10GUtil.isNeedPop()) {
                        m10GPopWindow.setType(PopWindowFor10GUtil.TYPE_FOR_VIDEO_CALL);
                        m10GPopWindow.setCallerInfo(mAddress, mPerson);
                        m10GPopWindow.setJustDismiss(true);
                        m10GPopWindow.showAsDropDown(mPop10GDropView, 0,0, Gravity.TOP);
                    }else {
                        CallRecordsUtils.voiceCall(getActivity(),mAddress,true,mPerson);
                    }
                } else if(item.equals(getString(R.string.network_call_voice))){
                    UmengUtil.buryPoint(getActivity(), "p2pmessage_more_ipcall_voice","点对点-加号-音视频电话-语音电话",0);

                    if(PopWindowFor10GUtil.isNeedPop()) {
                        m10GPopWindow.setType(PopWindowFor10GUtil.TYPE_FOR_CALL);
                        m10GPopWindow.setCallerInfo(mAddress, mPerson);
                        m10GPopWindow.setJustDismiss(true);
                        m10GPopWindow.showAsDropDown(mPop10GDropView, 0, 0, Gravity.TOP);
                    }else {
                        CallRecordsUtils.voiceCall(getActivity(),mAddress,false,mPerson);
                    }
                }
            }
        });
        messageOprationDialog.show();

    }

    /**
     * 消息富媒体红点提示
     */
    protected void rmRedTipDisappear(){
        super.rmRedTipDisappear();
        SharePreferenceUtils.setParam(MyApplication.getAppContext(), RM_MORE_NOT_TIP_KEY_SING, true);
    }
}