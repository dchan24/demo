package com.cmicc.module_message.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.chinamobile.app.yuliao_business.model.BaseModel;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_common.utils.Log;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.cmicc.module_message.ui.activity.MessageBgSetActivity;
import com.cmicc.module_message.ui.activity.OneToOneSettingActivity;
import com.cmicc.module_message.ui.constract.MmsSmsEditorContracts;
import com.cmcc.cmrcs.android.ui.model.MediaItem;
import com.cmicc.module_message.ui.model.impls.MessageEditorModelImpl;
import com.cmcc.cmrcs.android.ui.utils.CallRecordsUtils;
import com.cmcc.cmrcs.android.ui.view.MessageOprationDialog;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.presenter.MmsSmsEditorPresenterImpl;
import com.constvalue.MessageModuleConst;

import java.util.ArrayList;


/**
 * Created by xyz on 2017/7/7.
 */

public class MmsSmsFragment extends BaseChatFragment {

    private static final String TAG = "xyz-MmsSmsFragment";

    private MmsSmsEditorContracts.Presenter mPresenter;
    private Bundle bundle;
    private MenuItem setItem;
    private MenuItem callItem;
    private int themeType = 0;
    private TextView tvTitle;

    public static MmsSmsFragment newInstance(Bundle bundle) {
        MmsSmsFragment fragment = new MmsSmsFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void initData() {
        Log.d(TAG, "MmsSmsFragment init");
        super.initData();
        bundle = getArguments();
//        mAddress = NumberUtils.getPhone(mAddress);
//        mIbMore.setEnabled(false);
//        mIbAudio.setEnabled(false);
        mKeyboardPanel.setBackgroundDrawable(getResources().getDrawable(R.drawable.edit_box_shadow_bkg));
        mRichPanel.setVisibility(View.GONE);
        mIbAudio.setVisibility(View.GONE);
        mIbExpression.setVisibility(View.GONE);
        mIbSend.setVisibility(View.VISIBLE);
        mEtMessage.setHint(getString(R.string.send_msg));
        mPresenter.start();
    }

    @Override
    public void onResume() {
        Log.e("time debug", "time onResume ---" + System.currentTimeMillis());
        super.onResume();

    }

    @Override
    protected void onSendClickReport(int type) {
//        HashMap<String, String> map  = new HashMap<String,String>();
//        map.put("message_type","点对点");
//        map.put("send_mode","短信");
//        map.put("click_name","发送类型");
//
//
//        MobclickAgent.onEvent(mContext, "Message_mode", map);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(TAG, "onNewIntent: "+ menu.size());
        menu.clear();
        inflater.inflate(R.menu.menu_message_detail, menu);
        setItem = menu.findItem(R.id.action_setting);
        callItem = menu.findItem(R.id.action_call);
        changeMenu(themeType);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Log.d(TAG, "onNewIntent: "+ bundle.getString("address"));
        int i = item.getItemId();
        if (i == R.id.action_call) {

            hideKeyboardAndTryHideGifView();

            String[] itemList = getResources().getStringArray(R.array.single_call_choose_click);
            MessageOprationDialog messageOprationDialog = new MessageOprationDialog(getContext(), null, itemList, null);
            messageOprationDialog.setOnMessageItemClickListener(new MessageOprationDialog.OnOprationItemClickListener() {
                @Override
                public void onClick(String item, int which, String address) {
                    if (item.equals(getString(R.string.normal_call))) {

                        CallRecordsUtils.normalCall(getActivity(), mAddress);
                    } else if (item.equals(getString(R.string.video_call))) {

                        CallRecordsUtils.voiceCall(getActivity(), mAddress, true,mPerson);
                    }
                }
            });
            messageOprationDialog.show();

        } else if (i == R.id.action_setting) {

            hideKeyboardAndTryHideGifView();
            bundle.putString("setting_chat_type", "sms_mms");
            OneToOneSettingActivity.start(getActivity(), bundle);
            // startForResult与singletast冲突
//                OneToOneSettingActivity.startForResult(getActivity(), 100, bundle);

        } else {
        }
        return false;
    }

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.menu_message_detail, menu);
//        setItem = menu.findItem(R.id.action_setting);
//        callItem = menu.findItem(R.id.action_call);
//        changeMenu(themeType);
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        Bundle bundle;
//        bundle = getArguments();
//        switch (item.getItemId()) {
//            case R2.id.action_call:
//                hideKeyboard();
//                CallRecordsUtils.voiceCall(getActivity(), mAddress,true);
//                break;
//
//            case R2.id.action_setting:
//                hideKeyboard();
//                OneToOneSettingActivity.start(getActivity(), bundle);
//                // startForResult与singletast冲突
////                OneToOneSettingActivity.startForResult(getActivity(), 100, bundle);
//                break;
//            default:
//        }
//        return false;
//    }


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
        // ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(person);
        tvTitle = (TextView) ((AppCompatActivity) getActivity()).findViewById(R.id.title);
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
    public void deleteMessage(Message msg) {
        mPresenter.deleteMessage(msg);
    }

    @Override
    public void deleteMultiMessage(SparseBooleanArray selectList) {

    }

    @Override
    public void forwardMultiMessage(SparseBooleanArray selectList) {

    }

    @Override
    public void addToFavorite(Message msg, int chatType, String address) {
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
//        ((BaseActivity) getActivity()).requestPermissions(new BaseActivity.OnPermissionResultListener() {
//
//            @Override
//            public void onAllGranted() {
//                super.onAllGranted();
//                LogF.d(TAG, "可以发送短信了");
//                mPresenter.sendMessage(mEtMessage.getText().toString(), messageSize);
//            }
//
//            @Override
//            public void onAnyDenied(String[] permissions) {
//                BaseToast.makeText(getActivity(), "需要发送短信权限", Toast.LENGTH_LONG).show();
//            }
//
//            @Override
//            public void onAlwaysDenied(String[] permissions) {
//                String message = "需要发送短信权限";
//                PermissionDeniedDialog permissionDeniedDialog = new PermissionDeniedDialog(getActivity(), message);
//                permissionDeniedDialog.show();
//            }
//        }, Manifest.permission.SEND_SMS);
//        mPresenter.sendMessage(mEtMessage.getText().toString(),messageSize);
        LogF.d(TAG, "rcsImMsgSend mEtMessage:" + mEtMessage.getText().toString());
        mPresenter.sendSuperMessage(mEtMessage.getText().toString(),messageSize);
    }

    @Override
    public void sendVcard(String pcUri, String pcSubject, String pcFileName, long duration) {
        mPresenter.sendVcard(pcUri, pcSubject, pcFileName, duration);
    }

    @Override
    protected void sendImgAndVideo(ArrayList<MediaItem> items) {
        mPresenter.sendImgAndVideo(items);
    }

    @Override
    protected void sendImgAndVideo(ArrayList<MediaItem> items, boolean isOriginPhoto) {

    }

    @Override
    protected void sendLocation(double dLatitude, double dLongitude, float fRadius, String pcLabel,String detailAddress) {

    }

    @Override
    protected void sendFileMsg(Intent data) {
        mPresenter.sendFileMsg(data);
    }

    @Override
    protected void initPresenter(Bundle bundle) {
        mPresenter = new MmsSmsEditorPresenterImpl(this.getActivity(), this, getLoaderManager(), bundle);
    }


    @Override
    protected Message getDraftMessage() {
//        return mPresenter.getDraftMessage();
        return null;
    }

    @Override
    protected void saveDraftMessage(boolean save, Message msg) {
//        mPresenter.saveDraftMessage(save, msg);
    }

    @Override
    public int getChatType() {
        return MessageModuleConst.MessageChatListAdapter.TYPE_SMSMMS_SINGLE_CHAT;
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
                callDrawable = R.drawable.message_call_selector;
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
    public void senAudioMessage(String path, long lon) {
        mPresenter.sendAudio(path);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
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