package com.cmicc.module_message.ui.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.app.module.proxys.moduleaboutme.AboutMeProxy;
import com.chinamobile.app.yuliao_business.aidl.SendServiceMsg;
import com.chinamobile.app.yuliao_business.logic.common.LogicActions;
import com.chinamobile.app.yuliao_common.utils.BroadcastActions;
import com.chinamobile.app.utils.RxAsyncHelper;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_core.db.LoginDaoImpl;
import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.yuliao_core.util.NumberUtils;
import com.chinamobile.icloud.im.sync.model.PhoneKind;
import com.chinamobile.icloud.im.sync.model.RawContact;
import com.cmcc.cmrcs.android.ui.activities.HomeActivity;

import com.cmicc.module_message.ui.activity.GroupStrangerActivity;
import com.cmicc.module_message.ui.constract.GroupStrangerContract;
import com.cmcc.cmrcs.android.ui.dialogs.SendCradDialog;
import com.cmcc.cmrcs.android.ui.fragments.BaseFragment;
import com.cmcc.cmrcs.android.ui.utils.IPCUtils;
import com.cmcc.cmrcs.android.ui.utils.VcardContactUtils;
import com.cmcc.cmrcs.android.ui.view.RecycleSafeImageView;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmicc.module_message.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.functions.Func1;

/**
 * Created by zhufang_lu on 2017/6/19.
 */

public class GroupStrangerFragment extends BaseFragment implements GroupStrangerContract.View,View.OnClickListener{

    private static final String TAG="GroupStrangerFragment";
    public static final int SHARETYPE_EXVCARD = 2; //交互名片分享
    private GroupStrangerContract.Presenter mPresenter;
    private String mGroup_name = "";
    private String mGroupCard = "";
    private boolean mAnimationPending = false;
    TextView tvName;
    TextView tvNum;
    RecycleSafeImageView ivPhoto;
    Button btExchangeCard;
    TextView tvTextHint;
    Map<String, Boolean> mapCheck = new HashMap<>();
    String completeAddress ; // 有国家码的手机号码
    private String mStrNoteKind ;
    private Context mContext;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_group_stranger;
    }

    @Override
    public void initViews(View rootView){
        super.initViews(rootView);
        tvName = rootView.findViewById(R.id.strangerName);
        tvNum = rootView.findViewById(R.id.strangerNum);
        ivPhoto = rootView.findViewById(R.id.strangerUserPhoto);
        ivPhoto.setOnClickListener(this);
        btExchangeCard = rootView.findViewById(R.id.btn_exchange_card);
        btExchangeCard.setOnClickListener(this);
        tvTextHint = rootView.findViewById(R.id.text_hint);
    }
    @Override
    public void initData() {
        mContext = getContext() ;
        mPresenter.start(); // 接收传过来的数据，显示名字和手机号码
        mPresenter.loadPhoto(ivPhoto); // 加载头像
        mapCheck.clear();
        for(int i = 0 ; i< 4 ; i++ ){
            if (i == 0) {
                mapCheck.put(VcardContactUtils.card_fields[i], true); // 手机号码必须选中
            } else {
                mapCheck.put(VcardContactUtils.card_fields[i], false); // 默认选中
            }
        }
        initCallbackBroadcast();
    }

    @Override
    public void onResume() {
        super.onResume();
        mAnimationPending = false;
    }

    public void updateHint(boolean isExchangeCard){
        if(isExchangeCard){
            btExchangeCard.setText(getActivity().getResources().getString(R.string.exchange_card_again));
            tvTextHint.setText(getActivity().getResources().getString(R.string.have_send_card));
        }else{
            btExchangeCard.setText(getActivity().getResources().getString(R.string.exchange_card));
            tvTextHint.setText(getActivity().getResources().getString(R.string.stranger_hint));
        }
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.btn_exchange_card) {
            if(TextUtils.isEmpty(tvNum.getText().toString()) || tvNum.getText().toString().length()<8 ){ // 手机号码不合法
                Toast.makeText(mContext , getString(R.string.can_not_exchange) , Toast.LENGTH_SHORT).show();
                return;
            }
            mGroupCard = AboutMeProxy.g.getServiceInterface().getMyProfileFamilyName(mContext) + AboutMeProxy.g.getServiceInterface().getMyProfileGiveName(mContext);
            if (TextUtils.isEmpty(mGroupCard)) {
                mGroupCard = LoginDaoImpl.getInstance().queryLoginUser(mContext);
            }
            if (TextUtils.isEmpty(mGroup_name)) {
                mGroup_name = getArguments().getString("groupName");
            }
            if(TextUtils.isEmpty(mGroup_name)){
                mStrNoteKind = mGroupCard + getString(R.string.request_exchange);
            }else{
                mStrNoteKind = mGroupCard + getString(R.string.from_group_chat)+mGroup_name+getString(R.string.request_exchange);
            }
            LogF.d(TAG, "mStrNoteKind: " + mStrNoteKind);
            String phnoe  = LoginDaoImpl.getInstance().queryLoginUser(mContext);
            mDialog.setNameText(mGroupCard); // 名字
            mDialog.setPhoneText(phnoe);     // 手机
            mDialog.setHeadImge(phnoe); // 头像
            String company = AboutMeProxy.g.getServiceInterface().getMeCompany(mContext);
            String[] ss ;
            if(!TextUtils.isEmpty(company) && (ss=company.split(" "))!=null){
                for(int j = 0 ; j < ss.length ; j++ ){
                    if(j == 0 && !TextUtils.isEmpty(ss[j])){
                        mDialog.setCompanyText(ss[j]); // 公司
                    }else if(j == 1 && !TextUtils.isEmpty(ss[j])){
                        mDialog.setPositionText(ss[j]); // 职位
                    }else if(j==2&& !TextUtils.isEmpty(ss[j])){
                        mDialog.setemailText(ss[j]); // 邮箱
                    }
                }
            }
            mDialog.show();
        } else if (i == R.id.strangerUserPhoto) {
            if (mAnimationPending) {
                return;
            }
            mAnimationPending = true;
            Intent intent = new Intent();
            int[] location = new int[2];
            ivPhoto.getLocationOnScreen(location);
            intent.putExtra("locationX", location[0]);
            intent.putExtra("locationY", location[1]);
            intent.putExtra("width", ivPhoto.getWidth());
            intent.putExtra("height", ivPhoto.getHeight());
            intent.putExtra("number", getArguments().getString("num"));
            intent.putExtra("type", "contact");
            AboutMeProxy.g.getUiInterface().goToHeadPhotoActivity(mContext,intent);

        }
    }

    public static GroupStrangerFragment newInstance() {
        GroupStrangerFragment fragment = new GroupStrangerFragment();
        return fragment;
    }

    public void setPresenter(@NonNull GroupStrangerContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showNum(String name,String num) {
        tvName.setText(name);
        tvNum.setText(num);
    }

    @Override
    public void setCompleteAddress(String comAddress) {
        completeAddress = comAddress ;
    }


    /**
     * 2018.4.10 YSF
     */
    private CallbackBroadcast mCallbackBroadcast = new CallbackBroadcast();
    private SendCradDialog mDialog ;

    /**
     * 注册广播
     */
    private void initCallbackBroadcast(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastActions.MESSAGE_SEND_ACTION_EXVCARD_OK_CB);
        intentFilter.addAction(BroadcastActions.MESSAGE_SEND_ACTION_EXVCARD_FAIL_CB);
        intentFilter.addAction(BroadcastActions.MESSAGE_CRAD_EXCHANGE_AGREE_SAVE_CONTACT);
        mDialog  = new SendCradDialog(mContext);
        mContext.registerReceiver(mCallbackBroadcast,intentFilter);
        mDialog.getWindow().setWindowAnimations(R.style.sendCradSytl);
        mDialog.setSendCradInterface(new SendCradDialog.SendCradInterface() {
            @Override
            public void sendCrad() {
                if(mDialog.getProgressBarHind()==View.GONE){
                    exchangeCrad();
                }else{
                   Toast.makeText(getContext(),getString(R.string.handle_wait), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void cancleSend() {
                mDialog.dismiss();
            }

            @Override
            public void companySelect() {
                Boolean isChocie = mapCheck.get(VcardContactUtils.card_fields[1]);
                mapCheck.put(VcardContactUtils.card_fields[1] , isChocie==null?false:!isChocie) ;
                mDialog.setCompanyImage(isChocie==null?false:!isChocie);
            }

            @Override
            public void positionSelect() {
                Boolean isChocie = mapCheck.get(VcardContactUtils.card_fields[2]);
                mapCheck.put(VcardContactUtils.card_fields[2] , isChocie==null?false:!isChocie) ;
                mDialog.setPositionImage(isChocie==null?false:!isChocie);
            }

            @Override
            public void emailSelect() {
                Boolean isChocie = mapCheck.get(VcardContactUtils.card_fields[3]);
                mapCheck.put(VcardContactUtils.card_fields[3] , isChocie==null?false:!isChocie) ;
                mDialog.setEmailImage(isChocie==null?false:!isChocie);
            }
        });
        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                SendServiceMsg msg = new SendServiceMsg();
                msg.action = LogicActions.APPLY_CARS_EXCHANGE_CANCLER_GAREE;
                msg.bundle.putInt(LogicActions.USER_ID, 99998);
                IPCUtils.getInstance().send(msg);
                mDialog.setProgressBarGone();
            }
        });
    }

    /**
     * 交换卡片的方法
     */
    private void exchangeCrad(){
        mDialog.setProgressBar(); // 显示发送进度条

        new RxAsyncHelper("").runInThread(new Func1() {
            @Override
            public Object call(Object o) {
                String loginNum = LoginDaoImpl.getInstance().queryLoginUser(mContext);
                loginNum = NumberUtils.getDialablePhoneWithCountryCode(loginNum);// 必须要加上国家码，否则后面会出现发消息收不到情况。
                RawContact vcardContact = AboutMeProxy.g.getServiceInterface().getPersonalRawContact(mContext);
                List<PhoneKind> phoneKinds=new ArrayList<>();
                PhoneKind phoneKind=new PhoneKind();
                phoneKind.setNumber(loginNum);
                phoneKinds.add(phoneKind);
                vcardContact.setPhones(phoneKinds);
                VcardContactUtils.getInstance().applyBusinessCardExchange(mContext , mapCheck , vcardContact ,completeAddress , mStrNoteKind);
                return null;
            }
        }).runOnMainThread(new Func1() {
            @Override
            public Object call(Object o) {
                return null;
            }
        }).subscribe();
    }


    /**
     * 交换名片成功与否从底底进程发送来的广播
     */
    class CallbackBroadcast extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction() == BroadcastActions.MESSAGE_SEND_ACTION_EXVCARD_OK_CB){ // 名片发送成功
                mDialog.dismiss();
                final Activity activity = getActivity() ;
                if(activity != null && activity instanceof GroupStrangerActivity ){
                    ((GroupStrangerActivity) activity).setIsExchangeCard(true);
                    ((GroupStrangerActivity) activity).updateHint();
                     Handler handler = new Handler(){
                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);
                            if(msg.what == 10006 ){
                                activity.finish();
                            }
                        }
                    };
                    handler.sendEmptyMessageDelayed(10006 , (long) (0.5*1000)); // 延迟一秒跳转
                }
            }else if(intent.getAction() == BroadcastActions.MESSAGE_SEND_ACTION_EXVCARD_FAIL_CB){ // 名片发送失败
                mDialog.dismiss();
                if (!AndroidUtil.isNetworkConnected(mContext)) {
                    BaseToast.show(R.string.network_disconnect);
                }else{
                    BaseToast.show(R.string.operation_failed_toast);
                }
            }else if(intent.getAction() == BroadcastActions.MESSAGE_CRAD_EXCHANGE_AGREE_SAVE_CONTACT){
                final String phone = intent.getStringExtra(HomeActivity.save_contact_hpone);
                if(!TextUtils.isEmpty(phone)){
                    BaseToast.show(R.string.the_other_party_has_agreed);
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mCallbackBroadcast != null && getContext() != null ){
            mContext.unregisterReceiver(mCallbackBroadcast);
        }
    }
}
