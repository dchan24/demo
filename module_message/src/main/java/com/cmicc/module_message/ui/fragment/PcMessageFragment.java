package com.cmicc.module_message.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.app.module.proxys.moduleimgeditor.ImgEditorProxy;
import com.chinamobile.app.yuliao_business.model.BaseModel;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_common.utils.Log;
import com.cmicc.module_message.ui.activity.MessageBgSetActivity;
import com.cmicc.module_message.ui.activity.MessageDetailActivity;
import com.cmcc.cmrcs.android.ui.model.MediaItem;
import com.cmcc.cmrcs.android.ui.model.impls.PcMessageModelImpl;
import com.cmcc.cmrcs.android.ui.utils.MessageForwardUtil;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.constract.PcMessageContracts;
import com.cmicc.module_message.ui.presenter.PcMessagePresenterImpl;
import com.constvalue.MessageModuleConst;

import java.util.ArrayList;

/**
 * Created by tigger on 2018/5/7.
 */

public class PcMessageFragment extends BaseChatFragment {
    private static final String TAG = "PcMessageFragment";

    private PcMessageContracts.Presenter mPresenter;
    private MenuItem setItem;
    private int themeType = 0;

    private TextView tvTitle;
    private MessageForwardUtil messageForwardUtil;

    public static PcMessageFragment newInstance(Bundle bundle) {
        PcMessageFragment fragment = new PcMessageFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void initData() {
        Log.d(TAG, "GroupChatFragment init");
        super.initData();
        mPresenter.start();
    }

    @Override
    protected void onSendClickReport(int type) {
//        HashMap<String, String> map  = new HashMap<String,String>();
//        map.put("message_type","我的电脑");
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
    public void updateChatListView(int loadType, int searchPos, Bundle bundle) {
        Log.e("time debug", "time update ---" + System.currentTimeMillis());

        boolean fromMoreMsg = loadType == PcMessageModelImpl.LOAD_TYPE_MORE;
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
            if (loadType == PcMessageModelImpl.LOAD_TYPE_FIRST) {
                onFirstLoadDone(searchPos, list, hasMore);
            } else {
                onNormalLoadDone(list, loadType == PcMessageModelImpl.LOAD_TYPE_ADD);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.menu_pc_message, menu);
//        setItem = menu.findItem(R.id.action_setting);
//        changeMenu(themeType);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_setting) {
            hideKeyboardAndTryHideGifView();
        }
        return false;
    }

    @Override
    public void showTitleName(CharSequence person) {
        tvTitle= getActivity().findViewById(R.id.title);
        tvTitle.setText(person);
    }

    @Override
    public void reSend(Message msg) {
        mPresenter.reSend(msg);
    }

    @Override
    public void sendWithdrawnMessage(Message msg) {
        mPresenter.sendWithdrawnMessage(msg);
    }

    @Override
    public void showToast(String toast) {

    }

    @Override
    public void deleteMessage(Message msg) {
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
    protected void loadMoreMessages() {
        mPresenter.loadMoreMessages();
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
    protected void sendVcard(String pcUri, String pcSubject, String pcFileName, long duration) {
        mPresenter.sendVcard( pcUri,  pcSubject,  pcFileName,  duration);
    }

    @Override
    protected void sendMessage() {
        mPresenter.sendMessage(mEtMessage.getText().toString(),messageSize);
    }

    @Override
    protected void initPresenter(Bundle bundle) {
        mPresenter = new PcMessagePresenterImpl(this.getActivity(), this, getLoaderManager(), bundle);
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
        return MessageModuleConst.MessageChatListAdapter.TYPE_PC_CHAT;
    }

    @Override
    protected void clearUnreadCount() {
        mPresenter.updateUnreadCount();
    }

    @Override
    protected void changeMenu(int themeOption) {
        themeType = themeOption;//menu的加载在主题初始化之后，所以用themeType先保存一下。
        if(setItem == null) {
            return;
        }
        int setDrawable=0;
        switch (themeOption) {
            case MessageBgSetActivity.THEME_0NE:
                setDrawable = R.drawable.message_setting_selector;
                break;
            case MessageBgSetActivity.THEME_TWO:
                setDrawable = R.drawable.selector_message_set_theme_two;
                break;
            case MessageBgSetActivity.THEME_THREE:
                setDrawable = R.drawable.selector_message_set_theme_two;
                break;
            case MessageBgSetActivity.THEME_FOUR:
                setDrawable = R.drawable.selector_message_set_theme_three;
                break;
            case MessageBgSetActivity.THEME_FIVE:
                setDrawable = R.drawable.selector_message_set_theme_four;
                break;
            case MessageBgSetActivity.THEME_SIX:
                setDrawable = R.drawable.selector_message_set_theme_five;
                break;
            case MessageBgSetActivity.THEME_SEVEN:
                setDrawable = R.drawable.selector_message_set_theme_seven;
                break;
            case MessageBgSetActivity.THEME_EIGHT:
                setDrawable = R.drawable.selector_message_set_theme_eight;
                break;
        }
        setItem.setIcon(setDrawable);
    }

    @Override
    public void senAudioMessage(String path, long lon) {
        mPresenter.sendAudio(path, lon);
    }

    @Override
    public void senAudioMessage(String path, long lon, String detail) {
        mPresenter.sendAudio(path, lon, detail);
    }

    @Override
    public void sendSuperMessage(String msg) {

    }

    @Override
    public void hideToolBarMenu() {

    }

    @Override
    public void showToolBarMenu() {

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //从MessageDetailActivity-->GalleryActivity 路径
        if (requestCode == OPEN_GALLERY_REQUEST && resultCode == ImgEditorProxy.g.getServiceInterface().getFinalRequestEditPictureStatus()) {
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

        if(requestCode == FORWARD_REQUEST_CODE && resultCode == RESULT_OK){
            if(data != null){
                ((MessageDetailActivity)getActivity()).changeMode(MessageDetailActivity.OUT_MULTI_SELECT_MODE);
                boolean isGroup = data.getBooleanExtra("isGroup",false);
                String number = data.getStringExtra("number");
                messageForwardUtil.handleMessageForward(number,isGroup);
            }
        }

    }
}