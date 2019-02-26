package com.cmicc.module_message.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.utils.StringUtil;
import com.chinamobile.app.yuliao_business.aidl.SendServiceMsg;
import com.chinamobile.app.yuliao_business.logic.common.LogicActions;
import com.chinamobile.app.yuliao_business.model.BaseModel;
import com.chinamobile.app.yuliao_business.model.GroupInfo;
import com.chinamobile.app.yuliao_business.util.GroupChatUtils;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_common.utils.ObjectUtils;
import com.chinamobile.app.yuliao_contact.model.BaseContact;
import com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity;
import com.cmicc.module_message.ui.activity.GroupSMSActivity;
import com.cmicc.module_message.ui.activity.GroupSMSEditActivity;
import com.cmicc.module_message.ui.activity.GroupSMSSendeeActivity;
import com.cmcc.cmrcs.android.ui.dialogs.CommomDialog;
import com.cmcc.cmrcs.android.ui.dialogs.IconContentDialog;
import com.cmcc.cmrcs.android.ui.fragments.BaseFragment;
import com.cmcc.cmrcs.android.ui.logic.common.UIObserver;
import com.cmcc.cmrcs.android.ui.logic.common.UIObserverManager;
import com.cmicc.module_message.ui.presenter.GroupSMSEditPresenterImpl;
import com.cmcc.cmrcs.android.ui.utils.ContactSelectorUtil;
import com.cmcc.cmrcs.android.ui.utils.IPCUtils;
import com.cmcc.cmrcs.android.ui.utils.LoginUtils;
import com.cmcc.cmrcs.android.ui.utils.UmengUtil;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmicc.module_message.R;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

/**
 * Created by LY on 2018/5/11.
 */

public class GroupSmsEditFragment extends BaseFragment implements TextWatcher , View.OnClickListener {

    private  String TAG = "GroupSmsEditFragment";
    public static final int FROM_TYPE_GROUP_MASS = 1;

    public EditText et_edit;
    ImageView tv_send;
    private TextView sms_sendee ;
    private ImageView select_sendee ;
    private ImageView smsDirection;
    IconContentDialog mProgressDialog;
    private String address ;
    private int fromType;
    private String groupName ;
    private GroupSMSEditPresenterImpl groupSMSEditPresenter;
    private boolean isOwner ;
    private TextView tvIsFree;

    private GroupInfo groupInfo = null ;
    private ArrayList<String> hpones = new ArrayList<>();
    private String recipient = "" ;
    private String addresseNames = "" ; // 收件人的名字
    private ArrayList<Integer> labelIdList = new ArrayList<>();
    private ArrayList<BaseContact> selectContactList = new ArrayList<>();
    private static final int REQ_SELECT_CONTACT = 1;

    private Toast toast ;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            dismisProgressDialog();
            switch (msg.what){
                case LogicActions.MESSAGE_SEND_G_OK_CB:{
                    BaseToast.show(R.string.errcode_success);
                    et_edit.setText("");
                    Activity activity = getActivity() ;
                    if(activity != null  && !TextUtils.isEmpty(groupName) && !TextUtils.isEmpty(address)){
                        GroupSMSActivity.start(activity , address , groupName);
                    }
                    if(activity != null && !activity.isFinishing()){
                        activity.finish();
                    }
                    break;
                }
                case LogicActions.MESSAGE_SEND_G_FAIL_CB:{
                    BaseToast.show(R.string.send_fail);
                    break;
                }
                case LogicActions.MESSAGE_SEND_G_FAIL_OUT_QUOTA_CB:{
                    BaseToast.show(R.string.today_can_not_send);
                    break;
                }
                default:
                    break;
            }
        }
    };

    @Override
    public void initViews(View rootView){
        super.initViews(rootView);
        et_edit = (EditText) rootView.findViewById(R.id.et_edit);
        tv_send = (ImageView) rootView.findViewById(R.id.tv_send);
        sms_sendee = (TextView) rootView.findViewById(R.id.sms_sendee);
        tvIsFree = rootView.findViewById(R.id.tv_isFree);
        sms_sendee.setMovementMethod(ScrollingMovementMethod.getInstance()); // 内容过多是可以滚动
        if (fromType == FROM_TYPE_GROUP_MASS) {
            sms_sendee.setMaxHeight(dp2px(96));
            tvIsFree.setText(getString(R.string.you_are_use_group_mass));
        }
        select_sendee = (ImageView) rootView.findViewById(R.id.select_sendee);
        select_sendee.setOnClickListener(this);
        rootView.findViewById(R.id.sms_sendee).setOnClickListener(this);
        smsDirection = (ImageView) rootView.findViewById(R.id.sms_direction);
        smsDirection.setOnClickListener(this);
    }

    private int dp2px(int dp){
        return (int) (dp * this.getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public void initData() {
        toast = new Toast(getActivity());
        recipient = getResources().getString(R.string.accept_person_all) ;
        groupSMSEditPresenter = new GroupSMSEditPresenterImpl();
        et_edit.addTextChangedListener(this);
        tv_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UmengUtil.buryPoint(mContext,"groupmessage_sms_send","消息-群聊-加号-群短信-发送按钮",0);

                if(!AndroidUtil.isFastDoubleClick()){
                    if(AndroidUtil.isNetworkAvailable(mContext)){
                        if(hpones.size() == 0){
                            toast.cancel();
                            toast.makeText( getActivity() , R.string.select_the_recipient ,Toast.LENGTH_SHORT ).show();
                            //BaseToast.show(R.string.select_the_recipient);
                            return;
                        }
                        StringBuilder sb = new StringBuilder();
                        for( int i = 0 ;  i < hpones.size() ; i++ ){
                            sb.append(hpones.get(i));
                            if(i != hpones.size() -1){
                                sb.append(";");
                            }
                        }
                        mProgressDialog.show();
                        String gorupURI = GroupChatUtils.getGroupIdByIdentify(mContext ,address );
                        if (fromType == FROM_TYPE_GROUP_MASS) {
                            rcsImMsgSendSuperSms(et_edit.getText().toString(), et_edit.getText().toString(), sb.toString(), addresseNames);
                        } else {
                            rcsImMsgSendSuperSms(address, gorupURI, et_edit.getText().toString(), et_edit.getText().toString(), sb.toString(), addresseNames);
                        }
//                        int resultId = groupSMSEditPresenter.sendGroupSMS(mContext , address , et_edit.getText().toString());
//                        if (resultId == -1){// 执行成功
//                            showProgressDialog();
//                        } else {
//                            BaseToast.show(resultId);
//                        }
                    }else {
                        BaseToast.show(R.string.login_no_network_tip);
                    }
                }
            }
        });
        tv_send.setClickable(false);
        ArrayList<Integer> actions = new ArrayList<Integer>();
        actions.add(LogicActions.MESSAGE_SEND_G_OK_CB);
        actions.add(LogicActions.MESSAGE_SEND_G_FAIL_CB);
        actions.add(LogicActions.MESSAGE_SEND_G_FAIL_OUT_QUOTA_CB);
        UIObserverManager.getInstance().registerObserver(mUIObserver, actions);
        showProgressDialog();
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_groupsmsedit_layout;
    }

    private UIObserver mUIObserver = new UIObserver() {
        @Override
        protected void onReceiveAction(int action, Intent intent) {
            if (action == LogicActions.MESSAGE_SEND_G_OK_CB) {
                if (mHandler != null){
                    mHandler.sendEmptyMessageDelayed(LogicActions.MESSAGE_SEND_G_OK_CB,  1 * 1000);
                }
            } else if (action == LogicActions.MESSAGE_SEND_G_FAIL_CB) {
                if (mHandler != null){
                    mHandler.sendEmptyMessageDelayed(LogicActions.MESSAGE_SEND_G_FAIL_CB,  1 * 1000);
                }
            } else if (action == LogicActions.MESSAGE_SEND_G_FAIL_OUT_QUOTA_CB) {
                if (mHandler != null){
                    mHandler.sendEmptyMessageDelayed(LogicActions.MESSAGE_SEND_G_FAIL_OUT_QUOTA_CB,  1 * 1000);
                }
            }
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (ObjectUtils.isNotNull(mHandler)){
            mHandler.removeCallbacksAndMessages(null);
        }
        mHandler = null;
        dismisProgressDialog();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if(StringUtil.trim(s.toString()).isEmpty()){
            tv_send.setClickable(false);
            tv_send.setBackgroundResource(R.drawable.chat_send_grey);
        }else {
            tv_send.setClickable(true);
            tv_send.setBackgroundResource(R.drawable.chat_send_normal);
        }
        if(!TextUtils.isEmpty(s.toString()) && s.toString().length() >225 ){
            if (StringUtil.isEmoji(s.toString().substring(224 , 226))){//emoji占两个char，避免emoji被截断出现乱码
                et_edit.setText(s.toString().substring(0 , 224));
            }else {
                et_edit.setText(s.toString().substring(0 , 225));
            }

            et_edit.setSelection(et_edit.getText().toString().length());
            toast.cancel();
            toast.makeText( getActivity() , R.string.limit_upper_225 ,Toast.LENGTH_SHORT ).show();
            //BaseToast.show(R.string.limit_upper_225);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    /**
     * 显示进度条
     */
    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new IconContentDialog(mContext , getString(R.string.sending_wait));
        }
        mProgressDialog.setCancelable(true);
        mProgressDialog.setCanceledOnTouchOutside(false);
    }

    /**
     * 关闭进度条
     */
    public void dismisProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    /**
     * 设置群地址
     * @param context
     * @param address
     */
    public void setAddress(final Context context , final String address, int fromType) {
        this.address = address;
        this.fromType = fromType;
        new Thread(new Runnable() {
            @Override
            public void run() {
                groupInfo =  GroupChatUtils.getGroupInfo(context , address);
                String mLoginUserAddress = LoginUtils.getInstance().getLoginUserName(); // 当前登录的用户手机号 自己是群主才显示 并且是非异网卡
               if(groupInfo!=null){
                   if(groupInfo.getOwner() != null){
                       isOwner = groupInfo.getOwner().contains(mLoginUserAddress);
                   }
                   groupName = groupInfo.getPerson() ;
               }
            }
        }).start();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId() ;
        if(id == R.id.select_sendee || id == R.id.sms_sendee  ){ // 选择联系人
            if (fromType == 1) {
                Intent intent = ContactSelectorActivity.creatIntent(mContext, ContactSelectorUtil.SOURCE_GROUP_MASS, 200);
                intent.putParcelableArrayListExtra(ContactSelectorActivity.KEY_INTENT_LABEL_GROUP_PHONE_LIST, selectContactList); // 一开始选中的人的手机号码
                intent.putIntegerArrayListExtra(ContactSelectorActivity.KEY_INTENT_LABEL_GROUP_ID_LIST, labelIdList);
                startActivityForResult(intent, REQ_SELECT_CONTACT);
                return;
            }
            UmengUtil.buryPoint(mContext,"groupmessage_sms_add","消息-群聊-加号-群短信-选择成员",0);
            if(groupInfo != null ){
               Intent intent = new Intent(getActivity() , GroupSMSSendeeActivity.class);
               intent.putExtra(GroupSMSSendeeActivity.GROUPID , address);
               intent.putStringArrayListExtra(GroupSMSEditActivity.PHONES , hpones) ; // 一开始选中的人的手机号码
                if(isOwner){ // 普通群群主 企业群群主 都是500
                    intent.putExtra(GroupSMSEditActivity.MAXNUMBEROFPEOPLE , 500 ) ;
                }else{ // 普通群成员进不来这个界面，所以剩下的就是企业群普通群成员
                    intent.putExtra(GroupSMSEditActivity.MAXNUMBEROFPEOPLE , 200 ) ;
                }
               startActivityForResult(intent , GroupSMSEditActivity.requestCode);
            }else{
                Toast.makeText(getActivity() , "群异常，请退出重试" , Toast.LENGTH_SHORT).show();
            }
        }else if(id == R.id.sms_direction){

            final CommomDialog mDialog = new CommomDialog(mContext,mContext.getString(R.string.price_remind),
                    getString(R.string.group_fee_reminding_CCMC_stip));
            mDialog.setContentUseHtml(getString(R.string.group_fee_reminding_CCMC_stip));
            mDialog.setPositiveBtnOnly();
            mDialog.setPositiveTextSize(16);
            mDialog.setmBtnOkCopywriting(getString(R.string.i_know));
            mDialog.show();
            mDialog.setOnPositiveClickListener(new CommomDialog.OnClickListener() {
                @Override
                public void onClick() {
                    mDialog.dismiss();
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GroupSMSEditActivity.requestCode && resultCode == GroupSMSEditActivity.requestCode){
            addresseNames= data.getStringExtra(GroupSMSEditActivity.NAMES);
            String names = recipient + addresseNames ;
            SpannableString ss = new SpannableString(names);
            ss.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.color_4991fb)), names.indexOf("：") + 1, names.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sms_sendee.setText(ss);
            hpones = data.getStringArrayListExtra(GroupSMSEditActivity.PHONES);
        } else if (requestCode == REQ_SELECT_CONTACT && resultCode == RESULT_OK) {
            labelIdList = data.getIntegerArrayListExtra(ContactSelectorActivity.KEY_INTENT_LABEL_GROUP_ID_LIST);
            selectContactList = data.getParcelableArrayListExtra(ContactSelectorActivity.KEY_INTENT_RESULT_CODE);
            StringBuilder nameList = new StringBuilder();
            if (selectContactList != null && selectContactList.size() != 0) {
                for (int i = 0; i < selectContactList.size(); i++) {
                    hpones.add(selectContactList.get(i).getNumber());
                    nameList.append(selectContactList.get(i).getName());
                    if(i != selectContactList.size() - 1) {
                        nameList.append("、");
                    }
                }
            }
            addresseNames = nameList.toString();
            String names = recipient + addresseNames;
            SpannableString ss = new SpannableString(names);
            ss.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.color_4991fb)), names.indexOf("：") + 1, names.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sms_sendee.setText(ss);
        }
    }

    /**
     * 发送短信
     * @param message
     * @param size
     * @param mAddress
     */
    private void rcsImMsgSendSuperSms(String groupID ,String gorupURI , String message, String size , String mAddress , String addresseNames) {
        LogF.d(TAG, "rcsImMsgSendSuperSms mMessage:" + message + " mAddress = "+ mAddress );
        SendServiceMsg msg = new SendServiceMsg();
        msg.action = LogicActions.MESSAGE_GROUP_SMS;       // action
        msg.bundle.putInt(LogicActions.USER_ID, BaseModel.DEFAULT_VALUE_INTEGER );        // 消息ID
        msg.bundle.putString(LogicActions.GROUP_ID , groupID );   // 群ID
        msg.bundle.putString(LogicActions.PARTICIPANT_URI, gorupURI ); // 群URI
        msg.bundle.putString(LogicActions.PARTICIPANT_LIST, mAddress ); // 收件人地址
        msg.bundle.putString(LogicActions.MESSAGE_ADDRESSEE , addresseNames);   // 收件人的名字
        msg.bundle.putString(LogicActions.MESSAGE_CONTENT, message );  // 消息的内容
        msg.bundle.putInt(LogicActions.MESSAGE_TYPE, Type.TYPE_MSG_TEXT_SUPER_SMS_SEND ); // 消息类型
        msg.bundle.putString(LogicActions.MESSAGE_SIZE, size);   // 内容大小
        IPCUtils.getInstance().send(msg);
    }

    private void rcsImMsgSendSuperSms(String message, String size , String mAddress , String addresseNames) {
        LogF.d(TAG, "rcsImMsgSendSuperSms mMessage:" + message + " mAddress = "+ mAddress);
        SendServiceMsg msg = new SendServiceMsg();
        msg.action = LogicActions.MESSAGE_GROUP_MASS;       // action
        msg.bundle.putInt(LogicActions.USER_ID, BaseModel.DEFAULT_VALUE_INTEGER);        // 消息ID
//        msg.bundle.putString(LogicActions.GROUP_ID , groupID );   // 群ID
//        msg.bundle.putString(LogicActions.PARTICIPANT_URI, gorupURI ); // 群URI
        msg.bundle.putString(LogicActions.PARTICIPANT_LIST, mAddress); // 收件人地址
        msg.bundle.putString(LogicActions.MESSAGE_ADDRESSEE , addresseNames);   // 收件人的名字
        msg.bundle.putString(LogicActions.MESSAGE_CONTENT, message);  // 消息的内容
        msg.bundle.putInt(LogicActions.MESSAGE_TYPE, Type.TYPE_MSG_GROUP_MASS_SEND); // 消息类型
        msg.bundle.putString(LogicActions.MESSAGE_SIZE, size);   // 内容大小
        IPCUtils.getInstance().send(msg);
    }

    @Override
    public void onStop() {
        super.onStop();
        toast.cancel();
    }
}
