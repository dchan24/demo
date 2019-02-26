package com.cmicc.module_message.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.module.proxys.modulecontact.ContactProxy;
import com.app.util.StrangerEnterpriseUtil;
import com.chinamobile.app.yuliao_business.util.ConversationUtils;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.utils.SharePreferenceUtils;
import com.chinamobile.app.yuliao_common.utils.statusbar.StatusBarCompat;
import com.chinamobile.app.yuliao_contact.model.SimpleContact;
import com.chinamobile.precall.common.OnInComingCallShowListener;
import com.chinamobile.precall.entity.InComingCallInfoEntity;
import com.cmcc.cmrcs.android.contact.data.ContactsCache;
import com.cmcc.cmrcs.android.glide.GlidePhotoLoader;
import com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity;
import com.cmicc.module_message.ui.activity.MessageBgSetActivity;

import com.cmicc.module_message.ui.activity.MessageSearchActivity;
import com.cmicc.module_message.ui.constract.OneToOneSettingContract;
import com.cmcc.cmrcs.android.ui.fragments.BaseFragment;
import com.cmicc.module_message.ui.presenter.OneToOneSettingPresenter;
import com.cmcc.cmrcs.android.ui.utils.ContactSelectorUtil;
import com.cmcc.cmrcs.android.ui.utils.ConvCache;
import com.cmcc.cmrcs.android.ui.utils.LoginUtils;
import com.cmcc.cmrcs.android.ui.utils.MessageThemeUtils;
import com.cmcc.cmrcs.android.ui.utils.UmengUtil;
import com.cmicc.module_message.R;
import com.constvalue.MessageModuleConst;

import java.util.ArrayList;
import java.util.List;

import static com.cmcc.cmrcs.android.ui.utils.ContactSelectorUtil.SOURCE_ONE_TO_ONE_CREATE_GROUP;
import static com.constvalue.ContactModuleConst.CONTACT_DETAIL;
import static com.constvalue.ContactModuleConst.CONTACT_NAME;
import static com.constvalue.ContactModuleConst.CONTACT_NUMBER;
import static com.constvalue.ContactModuleConst.IS_NEW_NOT_EDIT;
import static com.constvalue.MessageModuleConst.OneToOneSettingFragmentConst.REQUEST_CODE_CREATE_GROUP_FINISH;
import static com.constvalue.MessageModuleConst.OneToOneSettingFragmentConst.REQUEST_CODE_SELECT_CONTACT;


/**
 * Created by GuoXietao on 2017/3/31.
 */

public class OneToOneSettingFragment extends BaseFragment implements OneToOneSettingContract.View ,View.OnClickListener,CompoundButton.OnCheckedChangeListener{


    ImageView mIvSettingAvatar;
    TextView mTvSettingName;
    SwitchCompat mSwitchUndisturb;
    SwitchCompat mChatSet2TopSwitch; //聊天置顶设置。
    private ProgressBar mNoMessageProgress;
    ImageView mThemeThumb;
    ImageView ivCreateGroup;
    RelativeLayout mMessageUndisturb;
    RelativeLayout mSearchChatRecord;
    RelativeLayout mChatFile;
    ViewStub mStrangerSaveContactViewStub;

    private String mName;
    private String mStrangerEnterPriseStr;

    @Override
    public void initViews(View rootView){
        super.initViews(rootView);
        mIvSettingAvatar = (ImageView) rootView.findViewById(R.id.iv_setting_avatar);
        mTvSettingName = (TextView) rootView.findViewById(R.id.tv_setting_name);
        mSwitchUndisturb = (SwitchCompat) rootView.findViewById(R.id.switch_undisturb);
        mNoMessageProgress = (ProgressBar) rootView.findViewById(R.id.no_message_progress);
        mThemeThumb = (ImageView) rootView.findViewById(R.id.theme_thumb);
        ivCreateGroup = (ImageView) rootView.findViewById(R.id.ivCreateGroup);
        mMessageUndisturb = (RelativeLayout) rootView.findViewById(R.id.manage_switch_undisturb);
        mSearchChatRecord = (RelativeLayout) rootView.findViewById(R.id.tv_serarch_chat_record);
        mChatFile = (RelativeLayout) rootView.findViewById(R.id.tv_chat_file);
        mChatSet2TopSwitch = (SwitchCompat) rootView.findViewById(R.id.chat_set_to_top_switch);
        mStrangerSaveContactViewStub = rootView.findViewById(R.id.stranger_save_tip_viewstub) ;

        mIvSettingAvatar.setOnClickListener(this);
        rootView.findViewById(R.id.tv_serarch_chat_record).setOnClickListener(this);
        rootView.findViewById(R.id.rl_message_bg_set).setOnClickListener(this);
        rootView.findViewById(R.id.tv_chat_file).setOnClickListener(this);
        ivCreateGroup.setOnClickListener(this);

        //先设置状态再监听变化
        if (ConversationUtils.isSlient(getActivity(), mAddress)) {
            mSwitchUndisturb.setChecked(true);
        } else {
            mSwitchUndisturb.setChecked(false);
        }
        mSwitchUndisturb.setOnCheckedChangeListener(mCheckListener);
        mSwitchUndisturb.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP){
                    UmengUtil.buryPoint(getActivity(), "message_p2pmessage_setup_notdisturb","消息免打扰",0);
                    //防止重复操作，将按钮设置为不可用，将在服务器返回后将之恢复
                    mSwitchUndisturb.setEnabled(false);
                    mSwitchUndisturb.setVisibility(View.GONE);
                    mNoMessageProgress.setVisibility(View.VISIBLE);

                    //因为我们拦截了checkBox的点击操作，当前状态尚未更改（其实也不会更改了，需要我们收到服务器响应后编码更改）。
                    //所以当前状态已打开的时候要发给服务器off，当前状态未打开时要发给服务器on。注意这里是反的！！！
                    String status = mSwitchUndisturb.isChecked()?"off":"on";
                    mPresenter.setUndisturbSettingServer(mAddress, status);
                }
                return true;
            }
        });

    }

    /**
     * 消息免打扰选项
     */
    private CompoundButton.OnCheckedChangeListener mCheckListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mPresenter.setUndisturbSettingLocal(mAddress, isChecked);
        }
    };

    private String mAddress;
    private String mLoginUserAddress;
    private OneToOneSettingContract.Presenter mPresenter;
    private int mBoxType;

    @Override
    public void initData() {
        StatusBarCompat.setStatusBarColor(getActivity(), ContextCompat.getColor(getActivity(), R.color.color_2c2c2c));
        Bundle bundle;
        bundle = getArguments();
        mAddress = bundle.getString("address");
        String person = bundle.getString("person");
        mLoginUserAddress = LoginUtils.getInstance().getLoginUserName();
        //设置头像
        GlidePhotoLoader.getInstance(getContext()).loadPhoto(getContext(), mIvSettingAvatar, mAddress);
        //设置名称
        SimpleContact contact = ContactsCache.getInstance().searchContactByNumberInHash(mAddress);
        if (contact != null) {
            mName = contact.getName() ;
            mTvSettingName.setText(mName);
        } else {
            mName = person ;
            mTvSettingName.setText(mName);
            String personNum = mName;
            if(TextUtils.isEmpty(mName)){
                personNum = mAddress;
            }
        //    CallShowManager.getInstance(mContext).getPairInfo(personNum,LoginUtils.getInstance().getLoginUserName(),new GetStrangerNameListener());
            StrangerEnterpriseUtil.getStrangerPairInfo(mContext,personNum,LoginUtils.getInstance().getLoginUserName(),new GetStrangerNameListener());
        }

        mBoxType = Type.TYPE_BOX_MESSAGE;

        String setting = bundle.getString("setting_chat_type");
        if (setting != null && setting.startsWith("sms_mms")){
//            ivCreateGroup.setVisibility(View.GONE);
            mMessageUndisturb.setVisibility(View.GONE);
//            mSearchChatRecord.setVisibility(View.GONE);
            mChatFile.setVisibility(View.GONE);
            mBoxType = Type.TYPE_BOX_SMS;
        }

        mPresenter = new OneToOneSettingPresenter(this.getContext(), this);
        mPresenter.getUndisturbSetting(mAddress);
        setupThemeThumb();


        // 置顶状态 设置
        //在此添加 对 是否置顶 的判定。因为mAddress 在此处才被赋值。
        boolean isSet2Top = ConversationUtils.isTop(mContext, mAddress);
        mChatSet2TopSwitch.setChecked(isSet2Top);

        // 如果在onCreateView中设置，mAddress 为null， 会有问题。
        mChatSet2TopSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean success = false;
                boolean isTop = ConversationUtils.isTop(mContext, mAddress);

                if(isTop == isChecked){//状态已经对了， 就不用再次设置了。
                    return;
                }

                if(isChecked){
                    long time = System.currentTimeMillis();
                    success = ConversationUtils.setTop(mContext, mAddress, time);
                    if(success){
                        UmengUtil.buryPoint(mContext,"p2pmessage_setup_top","消息-点对点会话-聊天设置-置顶聊天",0);
                        ConvCache.getInstance().updateToTop(mAddress, ConvCache.CacheType.CT_ALL, time);
                    }else{//设置置顶失败， 关闭开关
                        mChatSet2TopSwitch.setChecked(false);
                    }
                }else{
                    success = ConversationUtils.setTop(mContext, mAddress, -1);
                    if(success){
                        ConvCache.getInstance().updateToTop(mAddress, ConvCache.CacheType.CT_ALL, -1);
                    }else{//关闭置顶失败， 打开开关
                        mChatSet2TopSwitch.setChecked(true);
                    }
                }
            }
        });

    }

    private class GetStrangerNameListener implements OnInComingCallShowListener{

        @Override
        public void onSuccess(InComingCallInfoEntity inComingCallInfoEntity) {
            if(inComingCallInfoEntity!=null){
                if(!TextUtils.isEmpty(inComingCallInfoEntity.getEnterprise())){
                    mTvSettingName.setText(inComingCallInfoEntity.getMark());
                    mName = inComingCallInfoEntity.getMark();
                    mStrangerEnterPriseStr = inComingCallInfoEntity.getEnterprise();
                    View saveContactView = mStrangerSaveContactViewStub.inflate();
                    saveContactView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Bundle bundle = new Bundle();
                            bundle.putString(CONTACT_NUMBER, mAddress);
                            bundle.putString(CONTACT_NAME, mName);
//                            bundle.putString(CONTACT_RAWID,mRawId+"");
                            bundle.putBoolean(IS_NEW_NOT_EDIT, true);
                            String[] info =  new String[]{mStrangerEnterPriseStr,"",""};
                            bundle.putStringArray(CONTACT_DETAIL, info);
                            ContactProxy.g.getUiInterface().startNewContactActivity(mContext,bundle);
                        }
                    });
                }
            }
        }

        @Override
        public void onFail(String s) {

        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_one_to_one_setting;
    }

    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.iv_setting_avatar) {
//            String phone = NumberUtils.getNumForStore(mAddress);
//            final Employee employee = EnterpriseDbUtils.queryEmployeePhone(mContext, phone, "");
//            if(employee == null){
//                SimpleContact simpleContact = ContactsCache.getInstance().searchContactByNumberInContactsHash(mAddress);
//                if (simpleContact == null) {
//                    simpleContact = new SimpleContact();
//                    simpleContact.setNumber(mAddress);
//                    Bundle bundle = getArguments();
//                    if (bundle != null) {
//                        int rawId = bundle.getInt("rawId", -1);
//                        if (rawId != -1) {
//                            simpleContact.setRawId(rawId);
//                        }
//                    }
//                    ContactProxy.g.getUiInterface().getContactDetailActivityUI().showForSimpleContact(getContext(), simpleContact);
//                } else {
//                    ContactProxy.g.getUiInterface().getContactDetailActivityUI().showForSimpleContact(getContext(), simpleContact);
//                }
//            }else {
//                ContactProxy.g.getUiInterface().getContactDetailActivityUI().showForEmployee(getContext(), employee);
//            }

            SimpleContact simpleContact = ContactsCache.getInstance().searchContactByNumberInHash(mAddress);
            if (simpleContact == null) {
                simpleContact = new SimpleContact();
                simpleContact.setNumber(mAddress);
//                if(!TextUtils.isEmpty(NickNameUtils.getSameCompanyName(mAddress))){
//                    String stangerName = NickNameUtils.getStrangerName(mAddress);
//                    simpleContact.setName(stangerName);
//                }
                Bundle bundle = getArguments();
                if (bundle != null) {
                    int rawId = bundle.getInt("rawId", -1);
                    if (rawId != -1) {
                        simpleContact.setRawId(rawId);
                    }
                }
                ContactProxy.g.getUiInterface().getContactDetailActivityUI().showForSimpleContact(getContext(), simpleContact, 5);
//                ContactProxy.g.getUiInterface().showStrangerActivityDetail(getContext() ,mAddress);
            } else {
//                simpleContact.setNumber(mAddress);
                ContactProxy.g.getUiInterface().getContactDetailActivityUI().showForSimpleContact(getContext(), simpleContact, 5);
            }
        } else if (i == R.id.tv_serarch_chat_record) {
            UmengUtil.buryPoint(getActivity(), "message_p2pmessage_setup_search","查找聊天记录",0);
            MessageSearchActivity.start(OneToOneSettingFragment.this.getContext(), mAddress, mBoxType);

        } else if (i == R.id.rl_message_bg_set) {

            Intent intent = new Intent(getContext(), MessageBgSetActivity.class);
            intent.putExtra("address", mAddress);
            intent.putExtra("chatTyoe", 0);
            getContext().startActivity(intent);

        } else if (i == R.id.tv_chat_file) {
            UmengUtil.buryPoint(getActivity(), "message_p2pmessage_setup_file","聊天设置",0);
//            ChatFileActivity.start(OneToOneSettingFragment.this.getContext(), mAddress, Type.TYPE_BOX_MESSAGE);

        } else if(i == R.id.ivCreateGroup){
            UmengUtil.buryPoint(getContext(), "message_p2pmessage_setup_add","增加联系人",0);
            ArrayList<String> contacts = new ArrayList<>();
            contacts.add(mAddress);
//            Intent intent = ContactsSelectActivity.createIntentForOneToOneCreateGroup(getContext(), mAddress, contacts);
            Intent intent = ContactSelectorActivity.creatIntent(getContext(), SOURCE_ONE_TO_ONE_CREATE_GROUP, 499);
            intent.putStringArrayListExtra(ContactSelectorUtil.SELECTED_NUMBERS_KEY, contacts);
            startActivityForResult(intent, REQUEST_CODE_SELECT_CONTACT);
        }else {
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mPresenter.setUndisturbSettingLocal(mAddress, isChecked);
    }

    @Override
    public void setUndisturbSwitch(boolean checked) {
        mSwitchUndisturb.setChecked(checked);
    }

    public void setPresenter(OneToOneSettingContract.Presenter presenter) {
        mPresenter = presenter;
    }

    public boolean getSilent(){
        return mSwitchUndisturb.isChecked();
    }

    public String getUserName(){
        return mName;
    }

    public void updateThemeThumb(Drawable drawable) {
        mThemeThumb.setImageDrawable(drawable);
    }

    @Override
    public void onStart() {
        super.onStart();
        setupThemeThumb();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mTvSettingName != null){
            SimpleContact contact = ContactsCache.getInstance().searchContactByNumberInHash(mAddress);
            if (contact != null) {
                mName = contact.getName() ;
                mTvSettingName.setText(mName);
            } else {
                mTvSettingName.setText(mName);
            }
        }
    }

    @Override
    public void setupThemeThumb() {
        int themeType = 0;
        themeType = (int) SharePreferenceUtils.getParam(this.getContext(), MessageModuleConst.MESSAGE_THEME_TYPE + mLoginUserAddress + mAddress, 0);
        List<Drawable> drawables = MessageThemeUtils.getDrawableFromTheme(this.getContext(), MessageThemeUtils.THEME_DRAWABLE_NUMBER, themeType * MessageThemeUtils.THEME_DRAWABLE_NUMBER);
        updateThemeThumb(drawables.get(6));
    }


    @Override
    public void updateUndisturbFinish(boolean isOk) {
        //将按钮恢复可用
        mSwitchUndisturb.setEnabled(true);
        mSwitchUndisturb.setVisibility(View.VISIBLE);
        mNoMessageProgress.setVisibility(View.GONE);

        if(isOk){
            //设置上传服务器成功，这时再更改checked状态
            //状态置反
            mSwitchUndisturb.setChecked(!mSwitchUndisturb.isChecked());
        } else {
            Toast.makeText(getActivity(), R.string.update_settings_failed, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_SELECT_CONTACT:
                    getActivity().finish();
                    break;
                case REQUEST_CODE_CREATE_GROUP_FINISH:
                    getActivity().finish();
                    break;
            }

        }
    }

}
