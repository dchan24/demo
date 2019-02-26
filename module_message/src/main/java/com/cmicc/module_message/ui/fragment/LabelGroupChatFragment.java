package com.cmicc.module_message.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.app.module.proxys.moduleimgeditor.ImgEditorProxy;
import com.chinamobile.app.utils.StringUtil;
import com.chinamobile.app.yuliao_business.model.BaseModel;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_business.util.ConversationUtils;
import com.chinamobile.app.yuliao_common.utils.Log;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_core.util.NumberUtils;
import com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity;
import com.cmicc.module_message.ui.activity.MessageBgSetActivity;
import com.cmicc.module_message.ui.activity.MessageDetailActivity;
import com.cmicc.module_message.ui.activity.LabelGroupMemberListActivity;
import com.cmcc.cmrcs.android.ui.model.MediaItem;
import com.cmicc.module_message.ui.model.impls.MessageEditorModelImpl;
import com.cmcc.cmrcs.android.ui.utils.GroupUtils;
import com.cmcc.cmrcs.android.ui.utils.MessageForwardUtil;
import com.cmcc.cmrcs.android.ui.view.MessageOprationDialog;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.constract.LabelGroupChatContracts;
import com.cmicc.module_message.ui.presenter.LabelGroupChatPresenterImpl;
import com.constvalue.MessageModuleConst;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import static com.cmcc.cmrcs.android.ui.utils.ContactSelectorUtil.SOURCE_LABEL_MULTI_CALL;
import static com.cmcc.cmrcs.android.ui.utils.ContactSelectorUtil.SOURCE_LABEL_MULTI_VIDEO_CALL;


/**
 * Created by cq on 2018/5/10.
 * 分组群发消息页面
 */

public class LabelGroupChatFragment extends BaseChatFragment {
    private static final String TAG = LabelGroupChatFragment.class.getName();

    private LabelGroupChatContracts.Presenter mPresenter;

    private MenuItem setItem;
    private MenuItem multiCallItem;
    private int themeType = 0;
    private TextView tvTitle;

    private static final int REQUEST_CODE = 200;

    private static ArrayList<String> mContactNumbers;
    private MessageForwardUtil messageForwardUtil;
    private int labelGroupId = -1;
    private String labelGroupName;

    public static LabelGroupChatFragment newInstance(Bundle bundle) {
        LabelGroupChatFragment fragment = new LabelGroupChatFragment();
        fragment.setArguments(bundle);
        mContactNumbers = bundle.getStringArrayList("contactNumbers");
        return fragment;
    }

    @Override
    public void initData() {
        LogF.i(TAG, "LabelGroupChatFragment init");
        super.initData();
        mPresenter.start();
        labelGroupId = getArguments().getInt(MessageModuleConst.LABEL_GROUP_ID_KEY, -1);
        labelGroupName = getArguments().getString("person");
        if (labelGroupId == -1 && !TextUtils.isEmpty(labelGroupName)) {
            labelGroupId = GroupUtils.getLabelGroupIdByName(mContext, labelGroupName);
        }
    }

    private void buildAddress() {
        String tempAddress = "";
        if (StringUtil.isEmpty(mAddress) && mContactNumbers != null && mContactNumbers.size() > 0) {
            LinkedList<String> tempList = new LinkedList<String>();
            for (int i = 0; i < mContactNumbers.size(); i++) {
                String number = mContactNumbers.get(i);
                String phone = NumberUtils.getPhone(number);
                if (TextUtils.isEmpty(phone)) {
                    phone = number;
                }
                tempList.add(phone);
            }
            Collections.sort(tempList);
            StringBuilder sb = new StringBuilder();
            for (String s : tempList) {
                sb.append(s).append(';');
            }
            tempAddress = sb.deleteCharAt(sb.length() - 1).toString();
        } else {
            tempAddress = mAddress;
        }
        mAddress = tempAddress;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MessageDetailActivity) getActivity()).getmIvSlient().setVisibility(View.GONE);
    }

    @Override
    protected void onSendClickReport(int type) {
//        HashMap<String, String> map  = new HashMap<String,String>();
//        map.put("message_type","分组群发");
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
    public boolean isSlient() {
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_message_label_group_detail, menu);
        setItem = menu.findItem(R.id.action_setting);
        multiCallItem = menu.findItem(R.id.action_multicall);
        changeMenu(themeType);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i1 = item.getItemId();
        if (i1 == R.id.action_multicall) {
            hideKeyboardAndTryHideGifView();
            String[] itemList = getResources().getStringArray(R.array.group_msg_call_choose_click);
            MessageOprationDialog messageOprationDialog = new MessageOprationDialog(getContext(), "", itemList, null);
            messageOprationDialog.setOnMessageItemClickListener(new MessageOprationDialog.OnOprationItemClickListener() {
                @Override
                public void onClick(String item, int which, String address) {
                    if (item.equals(getString(R.string.multiparty_call))) {
                        //11月29日平台上线boss解耦业务，受限用户能用多方电话
                        Intent intent = ContactSelectorActivity.creatIntent(mContext,SOURCE_LABEL_MULTI_CALL,8);
                        intent.putExtra(ContactSelectorActivity.LABEL_ID, labelGroupId);
                        startActivity(intent);
                    } else if (item.equals(getString(R.string.multi_video_call_toolbar_title))) {
                        Intent intent = ContactSelectorActivity.creatIntent(mContext,SOURCE_LABEL_MULTI_VIDEO_CALL,8);
                        intent.putExtra(ContactSelectorActivity.LABEL_ID, labelGroupId);

                        startActivity(intent);
                    }
                }
            });
            messageOprationDialog.show();
        } else if (i1 == R.id.action_setting) {
            hideKeyboardAndTryHideGifView();
            Intent intent = new Intent(this.getActivity(), LabelGroupMemberListActivity.class);
            intent.putExtra(ContactSelectorActivity.LABEL_ID, labelGroupId);
            startActivity(intent);
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE) {
                if (data != null && data.getBooleanExtra("clear_all_msg", false)) {
                    onNormalLoadDone(null, false);
                }
            }
        }
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
        tvTitle = ((AppCompatActivity) getActivity()).findViewById(R.id.title);
        tvTitle.setText(person);

    }


    @Override
    public void reSend(Message msg) {
        mPresenter.resend(msg);
    }

    @Override
    public void sendWithdrawnMessage(Message msg) {
        mPresenter.sendWithdrawnMessage(msg);
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
    public void showToast(final String toast) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                BaseToast.show(getActivity(), toast);
            }
        });
    }

    @Override
    protected void loadMoreMessages() {
        mPresenter.loadMoreMessages();
    }

    @Override
    public void sendMessage() {
        mPresenter.sendMessage(mEtMessage.getText().toString(), messageSize);
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
        LogF.d(TAG, "Label group address before: " + mAddress);
        buildAddress();
        LogF.d(TAG, "Label group address after: " + mAddress);
        mPresenter = new LabelGroupChatPresenterImpl(this.getActivity(), this, getLoaderManager(), bundle, mAddress);
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
        return MessageModuleConst.MessageChatListAdapter.TYPE_MASS;
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
                setDrawable = R.drawable.tool_bar_add_selector;
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
        mPresenter.sendAudio(path, lon);
    }

    @Override
    public void senAudioMessage(String path, long lon, String detail) {
        mPresenter.sendAudio(path, lon, detail);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
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
    public void sendSuperMessage(String msg) {

    }

    @Override
    public void hideToolBarMenu() {

    }

    @Override
    public void showToolBarMenu() {

    }

}