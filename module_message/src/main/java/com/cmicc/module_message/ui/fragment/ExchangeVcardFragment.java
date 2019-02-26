package com.cmicc.module_message.ui.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.module.proxys.moduleaboutme.AboutMeProxy;
import com.app.module.proxys.modulecontact.ContactProxy;
import com.app.module.proxys.modulemessage.MessageProxy;
import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.utils.RxAsyncHelper;
import com.chinamobile.app.yuliao_business.aidl.SendServiceMsg;
import com.chinamobile.app.yuliao_business.logic.common.LogicActions;
import com.chinamobile.app.yuliao_business.model.Conversation;
import com.chinamobile.app.yuliao_business.util.SysMsgUtils;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_contact.model.PinYin;
import com.chinamobile.app.yuliao_contact.model.SimpleContact;
import com.chinamobile.icloud.im.sync.model.EmailKind;
import com.chinamobile.icloud.im.sync.model.NoteKind;
import com.chinamobile.icloud.im.sync.model.OrganizationKind;
import com.chinamobile.icloud.im.sync.model.PhoneKind;
import com.chinamobile.icloud.im.sync.model.RawContact;
import com.chinamobile.icloud.im.sync.platform.BatchOperation;
import com.chinamobile.icloud.im.sync.platform.ContactOperations;
import com.cmcc.cmrcs.android.contact.data.ContactAccessor;
import com.cmcc.cmrcs.android.contact.data.ContactsCache;
import com.cmcc.cmrcs.android.contact.model.ContactDataBean;
import com.cmcc.cmrcs.android.contact.observer.ContactsObserver;
import com.cmcc.cmrcs.android.contact.util.ContactUtils;
import com.cmcc.cmrcs.android.glide.GlidePhotoLoader;

import com.cmicc.module_message.ui.constract.ExchangeVcardContract;
import com.cmcc.cmrcs.android.ui.fragments.BaseFragment;
import com.cmcc.cmrcs.android.ui.utils.ConvCache;
import com.cmcc.cmrcs.android.ui.utils.IPCUtils;
import com.cmcc.cmrcs.android.ui.utils.VcardContactUtils;
import com.cmcc.cmrcs.android.ui.view.RecycleSafeImageView;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.activity.ExchangeVcardActivity;
import com.constvalue.MessageModuleConst;

import java.util.ArrayList;
import java.util.List;

import rx.functions.Func1;

/**
 * Created by tianshuai on 2017/7/20.
 */

public class ExchangeVcardFragment extends BaseFragment implements ExchangeVcardContract.View ,View.OnClickListener{
    private static final String TAG="ExchangeVcardFragment";
    private ExchangeVcardContract.Presenter mPresenter;
    private Bundle bundle;
    private String vcardString;
    private String strangerNumber;
    private int shareType;
    private Long sysId;
    private SimpleContact mSimpleContact;
    private boolean isAgree; //是否已经同意交换了
    private boolean mAnimationPending = false;
    private ProgressDialog mProgressDialog;

    RecycleSafeImageView ivCardPhoto;
    TextView tvCardName;
    TextView tvCardNum;
    LinearLayout companyLL;
    TextView tvCardCompany;
    LinearLayout positionLL ;
    TextView positionTv ;
    LinearLayout emailLL;
    TextView tvCardEmail;
    Button btAgree;
    Button btCheck;

    private Activity mActivity ;

    private boolean isUiShow ; // 当前在栈定


    @Override
    public void initViews(View rootView){
        ivCardPhoto = rootView.findViewById(R.id.strangerUserPhoto);
        tvCardName = rootView.findViewById(R.id.strangerName);
        tvCardNum = rootView.findViewById(R.id.strangerNum);
        companyLL = rootView.findViewById(R.id.company_ll);
        tvCardCompany = rootView.findViewById(R.id.company_tv);
        positionLL = rootView.findViewById(R.id.position_ll);
        positionTv = rootView.findViewById(R.id.position_tv);
        emailLL = rootView.findViewById(R.id.email_ll);
        tvCardEmail = rootView.findViewById(R.id.email_tv);
        btAgree = rootView.findViewById(R.id.agree);
        btAgree.setOnClickListener(this);
        btCheck = rootView.findViewById(R.id.check_vcard);
        btCheck.setOnClickListener(this);
    }
    @Override
    public int getLayoutId() {
        return R.layout.fragment_exchange_vcard;
    }

    @Override
    public void initData() {
        mActivity = getActivity() ;
        initView();
        setHasOptionsMenu(true);
        bundle=getArguments();
        vcardString=bundle.getString(ExchangeVcardActivity.VCARDSTRING);
        strangerNumber=bundle.getString(ExchangeVcardActivity.NUMBER);
        sysId=bundle.getLong(ExchangeVcardActivity.ID);
        shareType=bundle.getInt(ExchangeVcardActivity.TYPE); // 3
        isAgree=bundle.getBoolean("isAgree");
        if(isAgree){
            btAgree.setVisibility(View.GONE);
            btCheck.setVisibility(View.VISIBLE);
        }
        mPresenter.buildEntries(vcardString);
    }

    public void initView(){
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage(getString(R.string.wait_please));
        mProgressDialog.setCancelable(true);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) { // 对方框消息要取消任务队列
                SendServiceMsg msg = new SendServiceMsg();
                msg.action = LogicActions.FILE_CARS_EXCHANGE_CANCLER_GAREE;
                msg.bundle.putInt(LogicActions.USER_ID, 99999);
                IPCUtils.getInstance().send(msg);
                btAgree.setEnabled(true); // 可以再次点击
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume() ;
        mAnimationPending = false ;
        isUiShow = true ;
    }

    public void setPresenter(ExchangeVcardContract.Presenter presenter) {
        mPresenter = presenter;
    }

    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.agree) {
            if (!AndroidUtil.isNetworkConnected(mActivity)) {
                BaseToast.show(R.string.network_disconnect);
                return;
            }
            if(TextUtils.isEmpty(vcardString)){
                BaseToast.show(R.string.an_invalid_card);
                return;
            }
//            if(!checkPermission(vcardString)){
//                BaseToast.show(R.string.address_book_permissions);
//                return;
//            }
            if(TextUtils.isEmpty(strangerNumber)){
                return;
            }
            // 拼接名片信息
            String myAndApplyCardStringInfo = VcardContactUtils.getInstance().creatMyAndApplyCardStringInfo(mActivity , vcardString );
            if(TextUtils.isEmpty(myAndApplyCardStringInfo)){
                return;
            }
            // 生成vcf文件
            String cvfFliePath = VcardContactUtils.getInstance().creatVcfFile(mActivity , myAndApplyCardStringInfo );
            if(TextUtils.isEmpty(cvfFliePath)){
                return;
            }
            showDialog();
            VcardContactUtils.getInstance().agreeCardExchange(strangerNumber , myAndApplyCardStringInfo , cvfFliePath , vcardString);
        } else if (i == R.id.check_vcard) {//SimpleContact contact = ContactsCache.getInstance().searchContactByNumber(strangerNumber);
            if (mSimpleContact != null) {
                ContactProxy.g.getUiInterface().getContactDetailActivityUI().showForSimpleContact(mActivity ,mSimpleContact, 0);
            } else {
                SimpleContact contact = ContactsCache.getInstance().searchContactByNumber(strangerNumber);
                ContactProxy.g.getUiInterface().getContactDetailActivityUI().showForSimpleContact(mActivity ,contact, 0);
            }
            mActivity.finish();
        } else if (i == R.id.profile_photo_out) {
            if (mAnimationPending) {
                return;
            }
            mAnimationPending = true;
            Intent intent = new Intent();
            int[] location = new int[2];
            ivCardPhoto.getLocationOnScreen(location);
            intent.putExtra("locationX", location[0]);
            intent.putExtra("locationY", location[1]);
            intent.putExtra("width", ivCardPhoto.getWidth());
            intent.putExtra("height", ivCardPhoto.getHeight());
            intent.putExtra("number", tvCardNum.getText().toString());
            intent.putExtra("type", "contact");
            AboutMeProxy.g.getUiInterface().goToHeadPhotoActivity(mActivity,intent);
        }
    }

    @Override
    public void showInfo(RawContact rawContact) {
        String family = ""; // prefix + familyname
        String givenName = ""; // middlename + suffix + givenname
        // 姓
        if (rawContact.getStructuredName().getPrefix() != null) {
            family += rawContact.getStructuredName().getPrefix();
        }
        if (rawContact.getStructuredName().getFamilyName() != null) {
            family += rawContact.getStructuredName().getFamilyName();
        }
        // 名
        if (rawContact.getStructuredName().getMiddleName() != null) {
            givenName += rawContact.getStructuredName().getMiddleName();
        }
        if (rawContact.getStructuredName().getSuffix() != null) {
            givenName += rawContact.getStructuredName().getSuffix();
        }
        if (rawContact.getStructuredName().getGivenName() != null) {
            givenName += rawContact.getStructuredName().getGivenName();
        }
        String name="";
        if (!TextUtils.isEmpty(givenName)&&!TextUtils.isEmpty(family)) {
            if(ContactUtils.isAllCharacter(family)&&ContactUtils.isAllCharacter(givenName)){
                name=givenName + family;
            }else{
                name=family + givenName;
            }
        } else {
            name=family+givenName;
        }
        // 名字
        tvCardName.setText(name);
        // 手机
        for (int i = 0; i < rawContact.getPhones().size(); i++) {
            tvCardNum.setText(rawContact.getPhones().get(i).getNumber()); // 不需要隐藏号码RCSHFX-6312
           GlidePhotoLoader.getInstance(getActivity()).loadPhoto(getActivity(),ivCardPhoto,rawContact.getPhones().get(i).getNumber());//RCSHFX-6314
            break;
        }
        // 公司 $ 职位
        List<OrganizationKind> organizations = rawContact.getOrganizations();
        for (int i = 0; i < organizations.size(); i++) {
            companyLL.setVisibility(View.VISIBLE);
            positionLL.setVisibility(View.VISIBLE);
            OrganizationKind organizationKind = organizations.get(i);
            tvCardCompany.setText(organizationKind.getCompany());
            positionTv.setText(organizationKind.getTitle());
            break;
        }
        // 邮箱
        List<EmailKind> emails = rawContact.getEmails();
        for(int i=0;i<emails.size();i++){
            emailLL.setVisibility(View.VISIBLE);
            EmailKind emailKind = emails.get(i);
            tvCardEmail.setText(emailKind.getValue());
            break;
        }
    }

    /**
     * 名片同意成功，保存联系人
     * @param address
     */
    public void updateHint(final String address){
        new RxAsyncHelper("").debound(2000).runInThread(new Func1() {
            @Override
            public SimpleContact call(Object o) {
                SysMsgUtils.upDateCradState(getContext() ,address);
                SimpleContact contact = ContactUtils.newAndGetContact(mActivity, vcardString);
                if(contact!=null) {
                    mSimpleContact=new SimpleContact();
                    mSimpleContact.fill(contact);
                }
                return contact;
            }
        }).runOnMainThread(new Func1() {
            @Override
            public Object call(Object o) {
                mProgressDialog.dismiss();
                if (o != null) {
                    if (o instanceof SimpleContact) {
                        BaseToast.makeText(mActivity, getString(R.string.toast_save_success), Toast.LENGTH_SHORT).show();
                        Handler handler =  new Handler(){
                            @Override
                            public void handleMessage(Message msg) {
                                super.handleMessage(msg);
                                if(msg.what == 10005){
                                    btAgree.setEnabled(true);
                                    conversation(address) ;// 会话界面
                                }
                            }
                        };
                        handler.sendEmptyMessageDelayed(10005  , 1*1000) ; // 延迟两秒跳转

                    }else{
                        btAgree.setEnabled(true);
                    }
                }else {
                    btAgree.setEnabled(true);
                    BaseToast.makeText(mActivity, getString(R.string.save_contact_fail), Toast.LENGTH_SHORT).show();
                }
                btAgree.setVisibility(View.GONE);
                btCheck.setVisibility(View.VISIBLE);
                return null;
            }
        }).subscribe();
    }

    /**
     *显示进度跳
     */
    public void showDialog(){
        if(mProgressDialog != null ){
            mProgressDialog.show();
            btAgree.setEnabled(false); // 按钮不可用 ，防止多次点击
        }
    }

    /**
     * 名片同意失败
     */
    public void cradAgreeFial(){
        mProgressDialog.dismiss();
        btAgree.setEnabled(true); // 交换失败 按钮重新可用
        if (!AndroidUtil.isNetworkConnected(mActivity)) {
            BaseToast.show(R.string.network_disconnect);
        }else{
            BaseToast.show(R.string.operation_failed_toast);
        }
    }

    /**
     * 跳转到会话界面
     */
    private void conversation(String phone){
        Conversation conversation = ConvCache.getInstance().getConversationByAddress(phone);
        String clzName = MessageProxy.g.getServiceInterface().getClassName(MessageModuleConst.ClassType.MESSAGE_EDITOR__FRAGMENT_CLASS);
        Bundle bundle = new Bundle();
        if (conversation != null) {
            bundle.putString("address", conversation.getAddress());
            if(!TextUtils.isEmpty(conversation.getPerson())){
                bundle.putString("person", conversation.getPerson());
            }else{
                bundle.putString("person", tvCardName.getText().toString());
            }
            boolean isSlient = conversation.getSlientDate() > 0;
            bundle.putBoolean("slient", isSlient);
            bundle.putLong("loadtime", 0);
            bundle.putString("clzName", clzName);
            if (conversation.getType() == Type.TYPE_MSG_TEXT_DRAFT) {
                bundle.putString("draft", conversation.getBody());
            }
            bundle.putInt("unread", conversation.getUnReadCount());
            if(mActivity!=null){
//                MessageDetailActivity.show(mActivity, bundle);
                MessageProxy.g.getUiInterface().goMessageDetailActivity(mActivity,bundle);
            }
        } else {
            //发送消息,第一个号码的联系人
            bundle.putString("address", phone);
            bundle.putString("person", tvCardName.getText().toString());
            bundle.putString("clzName", clzName);
            if(mActivity!=null){
//                MessageDetailActivity.show(mActivity, bundle);
                MessageProxy.g.getUiInterface().goMessageDetailActivity(mActivity,bundle);
            }
        }
        if(mActivity!=null){
            mActivity.setResult(1001 , new Intent() ) ;// 关闭系统消息界面
            mActivity.finish(); // 关闭当前界面
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * 检查权限
     * @return
     */
    private boolean checkPermission(String s){
        boolean isHaverWrite = false ;
        if(mContext != null ){
            SimpleContact contact = ContactUtils.newAndGetContact(mContext , s);
            if(contact == null ){
                return isHaverWrite ;
            }else{
                delectSingleContact((int) contact.getRawId(),contact.getNumber());
            }
        }
        return true ;
    }


    /**
     * 删除联系人
     * @param rawId
     * @param number
     * @return
     */
    public int delectSingleContact(int rawId, String number){
        ContactDataBean contactDataBean=new ContactDataBean();
        RawContact detailContact = ContactAccessor.getInstance().getDetailContact(rawId);
        List<PhoneKind> phoneKinds = detailContact.getPhones();
        if(phoneKinds.size()<=1){
            List<Integer> ids = new ArrayList<Integer>();
            ids.add(rawId);
//            int result = ContactsCache.getInstance().deleteContacts(ids);
            return 1;
        }
        for(int i=0; i< phoneKinds.size(); i++){
            if(phoneKinds.get(i).getValue().equals(number)){
                phoneKinds.remove(i);
                contactDataBean.setPhoneKind(phoneKinds);
                break;
            }
        }
        String family = "";
        String givenName = "";
        // 姓
        if (detailContact.getStructuredName().getPrefix() != null) {
            family += detailContact.getStructuredName().getPrefix();
        }
        if (detailContact.getStructuredName().getFamilyName() != null) {
            family += detailContact.getStructuredName().getFamilyName();
        }
        // 名
        if (detailContact.getStructuredName().getMiddleName() != null) {
            givenName += detailContact.getStructuredName().getMiddleName();
        }
        if (detailContact.getStructuredName().getSuffix() != null) {
            givenName += detailContact.getStructuredName().getSuffix();
        }
        if (detailContact.getStructuredName().getGivenName() != null) {
            givenName += detailContact.getStructuredName().getGivenName();
        }
        if(family != null){
            contactDataBean.setFamilyname(family);
        }
        if(givenName != null){
            contactDataBean.setName(givenName);
        }
        if(detailContact.getEmails().size()>0){
            contactDataBean.setEmailKind(detailContact.getEmails().get(0));
        }
        if(detailContact.getOrganizations().size()>0){
            contactDataBean.setOrganizationKind(detailContact.getOrganizations().get(0));
        }
        updateContact(rawId,contactDataBean);
        return 100;
    }

    private ContentResolver mResolver;

    public void updateContact(long rawContactId, ContactDataBean contactDataBean) {
        mResolver = mContext.getContentResolver();
        long startTime = SystemClock.uptimeMillis();
        RawContact rawContact = BuildRawContact(contactDataBean);
        final BatchOperation batchOperation = new BatchOperation( mContext, mResolver);
        rawContact.setContactId(rawContactId);
        ContactOperations.updateContact(mContext, rawContact, batchOperation);
        List<Uri> uris = batchOperation.execute();
        if (uris.toString().equals("[null]") || uris.toString().equals("[]")) {
            return;
        }
        Long end4 = SystemClock.uptimeMillis();
        android.util.Log.d(TAG, "uris=" + uris.size());
        Long end2 = SystemClock.uptimeMillis();
        SimpleContact contact = null ;
        try {
            ContactsObserver.getObserver().notifyContentChange();
            contact = new SimpleContact();
//            contact.setId(ContactAccessor.getInstance().getContactIdFromRawContactId((int) rawContactId));
            contact.setRawId(rawContactId);
            if (contactDataBean.familyname != null && contactDataBean.name != null) {
                if(ContactUtils.isAllCharacter(contactDataBean.familyname)&&ContactUtils.isAllCharacter(contactDataBean.name)){
                    contact.setName(contactDataBean.name.trim() + contactDataBean.familyname.trim());
                }else{
                    contact.setName(contactDataBean.familyname.trim() + contactDataBean.name.trim());
                }
            } else {
                contact.setName(contactDataBean.familyname + contactDataBean.name);
            }
            contact.setName(contactDataBean.familyname + contactDataBean.name);
            PinYin pinYin = PinYin.buildPinYin(contact.getName());
            contact.setPinyin(pinYin);
            contact.setAddressList(rawContact.getPhones());
            if (TextUtils.isEmpty(contact.getNumber())) {
                contact.setNumber(contactDataBean.getEditedPhone());
            }
            android.util.Log.d(TAG, "contact.getNumber(): " + contact.getNumber() + " rid:" + rawContactId + " " + contact.getRawId());
            /* contact.setStarred(mStarred.isChecked() ? 1 : 0); */
            contact.setAccountType(SimpleContact.ACCOUNT_MOBILE_CONTACT);
            Long end1 = SystemClock.uptimeMillis();
//            ContactsCache.getInstance().editSimpleContact(contact);
            Long end = SystemClock.uptimeMillis();
            android.util.Log.d(TAG, "all time: " + (end - startTime));
            android.util.Log.d(TAG, "end1: " + (end1 - startTime));
            android.util.Log.d(TAG, "end2: " + (end2 - startTime));
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public RawContact BuildRawContact(ContactDataBean contactDataBean) {
        RawContact rawContact = new RawContact();
        rawContact.getStructuredName().setGivenName(contactDataBean.name != null ? contactDataBean.name.trim() : contactDataBean.name);
        rawContact.getStructuredName().setFamilyName(contactDataBean.familyname != null ? contactDataBean.familyname.trim() : contactDataBean.familyname);
        if (!TextUtils.isEmpty(contactDataBean.note)) {
            NoteKind kind = new NoteKind();
            kind.setValue(contactDataBean.note);
            rawContact.getNotes().add(kind);
        }
        if (contactDataBean.phoneKindList != null) {
            List<PhoneKind> phoneKinds = contactDataBean.getPhoneKindList();
            if (phoneKinds != null) {
                rawContact.setPhones(phoneKinds);
            }
        }
        if (contactDataBean.emailKind != null) {
            rawContact.getEmails().add(contactDataBean.emailKind);
        }
        if (contactDataBean.organizationKind != null) {
            rawContact.getOrganizations().add(contactDataBean.organizationKind);
        }
        return rawContact;
    }


    public boolean isUiShow() {
        return isUiShow;
    }

    public void setUiShow(boolean uiShow) {
        isUiShow = uiShow;
    }

    @Override
    public void onPause() {
        super.onPause();
        isUiShow = false ;
    }
}
