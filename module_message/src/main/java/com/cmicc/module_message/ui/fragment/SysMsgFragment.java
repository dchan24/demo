package com.cmicc.module_message.ui.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.app.module.proxys.modulemessage.MessageProxy;
import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.utils.RxAsyncHelper;
import com.chinamobile.app.yuliao_business.model.Conversation;
import com.chinamobile.app.yuliao_business.model.GroupInfo;
import com.chinamobile.app.yuliao_business.model.SysMsg;
import com.chinamobile.app.yuliao_business.util.ConversationUtils;
import com.chinamobile.app.yuliao_business.util.GroupChatUtils;
import com.chinamobile.app.yuliao_business.util.Status;
import com.chinamobile.app.yuliao_business.util.SysMsgUtils;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.application.App;
import com.chinamobile.app.yuliao_common.utils.BroadcastActions;
import com.chinamobile.app.yuliao_contact.model.PinYin;
import com.chinamobile.app.yuliao_contact.model.SimpleContact;
import com.chinamobile.icloud.im.sync.model.NoteKind;
import com.chinamobile.icloud.im.sync.model.PhoneKind;
import com.chinamobile.icloud.im.sync.model.RawContact;
import com.chinamobile.icloud.im.sync.platform.BatchOperation;
import com.chinamobile.icloud.im.sync.platform.ContactOperations;
import com.cmcc.cmrcs.android.contact.data.ContactAccessor;
import com.cmcc.cmrcs.android.contact.model.ContactDataBean;
import com.cmcc.cmrcs.android.contact.observer.ContactsObserver;
import com.cmcc.cmrcs.android.contact.util.ContactUtils;
import com.cmicc.module_message.ui.activity.ExchangeVcardActivity;
import com.cmicc.module_message.ui.activity.GroupChatInvitationActivtiy;

import com.cmcc.cmrcs.android.ui.adapter.BaseCustomCursorAdapter.OnRecyclerViewItemClickListener;
import com.cmcc.cmrcs.android.ui.adapter.SysMsgAdapter;
import com.cmicc.module_message.ui.constract.SysMsgContract;
import com.cmicc.module_message.ui.constract.SysMsgContract.Presenter;
import com.cmcc.cmrcs.android.ui.control.GroupChatControl;
import com.cmcc.cmrcs.android.ui.fragments.BaseFragment;
import com.cmicc.module_message.ui.presenter.SysMsgPresenterImpl;
import com.cmcc.cmrcs.android.ui.utils.ConvCache;
import com.cmcc.cmrcs.android.ui.utils.UmengUtil;
import com.cmcc.cmrcs.android.ui.utils.VcardContactUtils;
import com.cmcc.cmrcs.android.ui.utils.WrapContentLinearLayoutManager;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmicc.module_message.R;
import com.constvalue.MessageModuleConst;

import java.util.ArrayList;
import java.util.List;

import rx.functions.Func1;


/**
 * Created by tigger on 2017/5/23.
 */

public class SysMsgFragment extends BaseFragment implements SysMsgContract.View, OnRecyclerViewItemClickListener {

    private static final String TAG = "SysMsgFragment";

    public int GROUP_DELAY_ONE_SECOND_JUMP = 1000 ; // 群延迟一秒跳转，防止特殊操作（群主邀请后马上解散群）
    public int CARD_REQUESTCODE = 1001 ; // 名片个的请求码
    public int GROUP_REQUESTCODE = 1002 ; // 同意进群请求码


    private Activity mActivity ;

    private SysMsgContract.Presenter mPresenter;

    private RecyclerView mRecyclerView;
    private TextView mTvEmpty;

    public SysMsgAdapter mSysMsgAdapter;
    private ProgressDialog mProgressDialog; //加载对话框

    private boolean isUiShow ; // 当前在栈定

    private CallbackBroadcast mCallbackBroadcast ;
    private IntentFilter intentFilter ;


    public static SysMsgFragment newInstantce() {
        return new SysMsgFragment();
    }

    /**
     * 界面使用的布局ID
     * @return
     */
    @Override
    public int getLayoutId() {
        return R.layout.fragment_sys_msg;
    }


    @Override
    public void initViews(View rootView){
        super.initViews(rootView);
        mRecyclerView = rootView.findViewById(R.id.rv_sys_msg_list);
        mTvEmpty = rootView.findViewById(R.id.tv_empty);
    }


    @Override
    public void initData() {
        mActivity = getActivity();
        mPresenter = new SysMsgPresenterImpl(mActivity , this, getLoaderManager());
        initDialog();
        initRecycler();
        initCallbackBroadcast();
        mPresenter.start(); // 开始加载数据库的中的系统消息
    }

    /**
     * 初始化对话框
     */
    public void initDialog() {
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage(getString(R.string.wait_please));
        mProgressDialog.setCancelable(true);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mPresenter.setConsentEntryGroupID("");
            }
        });
    }

    /**
     * 注册广播
     */
    private void initCallbackBroadcast(){
        if(mCallbackBroadcast == null){
            mCallbackBroadcast = new CallbackBroadcast();
            intentFilter = new IntentFilter();
            intentFilter.addAction(BroadcastActions.MESSAGE_GROUP_STATUS_UPDATE_SUCCESS_ACTION);
        }
        if(mActivity!=null){
            mActivity.registerReceiver(mCallbackBroadcast ,intentFilter);
        }else{
            App.getAppContext().registerReceiver(mCallbackBroadcast ,intentFilter);
        }

    }

    /**
     * 初始化RcyclerView
     */
    private void initRecycler(){
        mRecyclerView.setLayoutManager(new WrapContentLinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mSysMsgAdapter = new SysMsgAdapter(mActivity);
        mRecyclerView.setAdapter(mSysMsgAdapter);
        mSysMsgAdapter.setRecyclerViewItemClickListener(this);
    }

    @Override
    public void setPresenter(Presenter p) {
        mPresenter = p;
    }

    @Override
    public void onResume() {
        super.onResume();
        isUiShow = true ;
    }

    /**
     * 系统消息加载完回调这里
     * @param cursor
     */
    @Override
    public void updateListView(Cursor cursor) {
        if(cursor == null ){
            return;
        }
        if (cursor.getCount() <= 0) {
            mTvEmpty.setVisibility(View.VISIBLE);
        } else {
            mTvEmpty.setVisibility(View.GONE);
        }
        mSysMsgAdapter.setCursor(cursor);// 给适配器设置数据
        mSysMsgAdapter.notifyDataSetChanged();
    }


    @Override
    public void dismissProgressDialog() {
        if(mProgressDialog != null && mProgressDialog.isShowing()){
            mProgressDialog.dismiss();
        }
    }


    /**
     * 点击事件
     * @param view
     * @param position
     */
    @Override
    public void onItemClick(View view, int position) {
        SysMsg sysMsg = mSysMsgAdapter.getItem(position);
        if(sysMsg == null ){
            return; // 最好还是判空一下
        }
        address = sysMsg.getAddress(); // 消息发送者地址
        person = sysMsg.getTitle();    // 发送者的名字
        vcardString = sysMsg.getSendAddress(); // 发送过来的内容
        int i = view.getId();

        if (i == R.id.btn_accept) {  // 点击 同意
            if (sysMsg.getType() == Type.TYPE_SYSMSG_VCARD) {  // 交换名片
                UmengUtil.buryPoint(mContext, "message_system_agree","同意交换名片",0);
                if (!AndroidUtil.isNetworkConnected(mActivity)) {
                    BaseToast.show(R.string.network_disconnect);
                    return;
                }
                String applyCardStringInfo = sysMsg.getSendAddress() ;
                if(TextUtils.isEmpty(applyCardStringInfo)){
                    BaseToast.show(R.string.an_invalid_card);
                    return;
                }
//                if(!checkPermission(sysMsg.getSendAddress())){
//                    BaseToast.show(R.string.address_book_permissions);
//                    return;
//                }
                // 同意交换名片 （同意之后进入到 会话界面）
                String strangerNumber = sysMsg.getAddress() ;
                if(TextUtils.isEmpty(strangerNumber)){
                    return;
                }
                // 拼接名片信息
                String myAndApplyCardStringInfo = VcardContactUtils.getInstance().creatMyAndApplyCardStringInfo(mActivity , applyCardStringInfo );
                if(TextUtils.isEmpty(myAndApplyCardStringInfo)){
                    return;
                }
                // 生成vcf文件
                String cvfFliePath = VcardContactUtils.getInstance().creatVcfFile(mActivity , myAndApplyCardStringInfo );
                if(TextUtils.isEmpty(cvfFliePath)){
                    return;
                }
                showDialog();
                VcardContactUtils.getInstance().agreeCardExchange(strangerNumber , myAndApplyCardStringInfo , cvfFliePath , applyCardStringInfo);
            } else if(sysMsg.getType() == Type.TYPE_SYSMSG_INVITE){  // 邀请类信息
                if (!AndroidUtil.isNetworkConnected(mActivity)) {
                    BaseToast.show(R.string.network_disconnect);
                    return;
                }
                showDialog();
                mPresenter.setConsentEntryGroupID(sysMsg.getGroupId()); // 设置当前点击同意入群的ID
                GroupChatControl.acceptInvite(sysMsg.getGroupId()); // 同意群邀请
            }else if( sysMsg.getType() ==  Type.TYPE_SYSMSG_NOTIFY ){ // 通知类不做任何交互操作

            }
        } else if (i == R.id.vcard_info) {  // 点击 查看详情
            if (sysMsg.getType() == Type.TYPE_SYSMSG_VCARD) {
                conversation(); // 已同意 进入会话界面
            }else if(sysMsg.getType() == Type.TYPE_SYSMSG_INVITE ){ // 邀请类  进入会话界面
                mPresenter.gotoChat(sysMsg.getGroupId(), sysMsg.getTitle()); // 跳转到聊天界面
            }else if( sysMsg.getType() ==  Type.TYPE_SYSMSG_NOTIFY ){  // 通知类不做任何交互操作

            }
        } else {  // 点击 整个Item
            if(sysMsg.getType() == Type.TYPE_SYSMSG_VCARD){  // 卡片类型 消息
                if(sysMsg.getStatus() == Status.STATUS_OK){  // 已经同意
                    conversation();  // 进入到消息会话界面
                }else if(sysMsg.getStatus() != Status.STATUS_FAIL && sysMsg.getStatus() != Status.STATUS_DESTROY){  // 还没同意   查看名片详情
                    UmengUtil.buryPoint(mActivity, "message_system_seecard","查看名片",0);
                    ExchangeVcardActivity.showVcard(mActivity, sysMsg.getAddress(), 3, sysMsg.getId(), sysMsg.getSendAddress(), false , CARD_REQUESTCODE);
                }
            }else if( sysMsg.getType() == Type.TYPE_SYSMSG_INVITE ){ // 群邀请
                GroupInfo groupInfo = GroupChatUtils.getGroupInfo(mActivity, sysMsg.getGroupId());
                if (sysMsg.getStatus() == Status.STATUS_OK ) {
                    if (groupInfo != null &&  groupInfo.getStatus() == Status.STATUS_OK ) {
                        mPresenter.gotoChat(sysMsg.getGroupId(), sysMsg.getTitle()); // 跳转到聊天界面
                        if(mActivity != null ){
                            mActivity.finish(); // 关闭本界面
                        }
                    }else if(groupInfo != null && groupInfo.getStatus() == Status.STATUS_LOADING ){
                        GroupChatInvitationActivtiy.goToGroupChatInvitationActivtiy(mActivity , groupInfo.getAddress() , address , sysMsg.getBody() , GROUP_REQUESTCODE) ; // 跳转到群详情页
                    }else{
                        Toast.makeText(mActivity , getString(R.string.you_have_left_the_group) , Toast.LENGTH_SHORT).show();
                    }
                }else if( sysMsg.getStatus()!= Status.STATUS_FAIL && sysMsg.getStatus() != Status.STATUS_DESTROY ){ // 没有同意 没有忽略 没有失效
                    if ( groupInfo != null && groupInfo.getStatus() == Status.STATUS_LOADING  ) { // 还未同意
                        GroupChatInvitationActivtiy.goToGroupChatInvitationActivtiy(mActivity , groupInfo.getAddress() , address , sysMsg.getBody() , GROUP_REQUESTCODE) ; // 跳转到群详情页
                    }else{
                        Toast.makeText(mActivity ,  getString(R.string.you_have_left_the_group) , Toast.LENGTH_SHORT).show();
                    }
                }
            }else if( sysMsg.getType() ==  Type.TYPE_SYSMSG_NOTIFY ){ // 通知类不做任何交互操作。

            }
        }
    }

    /**
     * 长按事件
     * @param v
     * @param position
     * @return
     */
    @Override
    public boolean onItemLongCLickListener(View v, int position) {
        return false;
    }


    @Override
    public void onPause() {
        super.onPause();
        isUiShow = false ;
        ConversationUtils.updateSeen(this.getActivity(), Type.TYPE_BOX_SYSMSG, null); // 跟新一度状态
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mCallbackBroadcast!=null){
            if(mActivity !=null ){
                mActivity.unregisterReceiver(mCallbackBroadcast);
            }else{
                App.getApplication().unregisterReceiver(mCallbackBroadcast);
            }
        }
    }

    public boolean isUiShow() {
        return isUiShow;
    }

    public void setUiShow(boolean uiShow) {
        isUiShow = uiShow;
    }

    /**
     * 2018.4.11 YSF
     */
    private SimpleContact mSimpleContact;
    private String address;
    private String person ;
    private String vcardString ;

    public void updateHint(final String address){
        new RxAsyncHelper("").debound(2000).runInThread(new Func1() {
            @Override
            public SimpleContact call(Object o) {
                Context context = getContext() ;
                if(context != null ){
                    SysMsgUtils.upDateCradState(getContext() ,address); // 更新系统消息的状态
                    SimpleContact contact = ContactUtils.newAndGetContact(getActivity(), vcardString);
                    if(contact!=null) {
                        mSimpleContact = new SimpleContact();
                        mSimpleContact.fill(contact);
                    }
                    return contact;
                }else{
                    return null ;
                }
            }
        }).runOnMainThread(new Func1() {
            @Override
            public Object call(Object o) {
                mSysMsgAdapter.notifyDataSetChanged(); // 状态要更新
                dismissProgressDialog();
                if (o != null) {
                    if (o instanceof SimpleContact) {
                        Activity activity = getActivity();
                        if(activity !=null ){
                            BaseToast.makeText(activity, getString(R.string.toast_save_success), Toast.LENGTH_SHORT).show();
                            conversation();
                        }
                    }
                }else {
                    Activity activity = getActivity();
                    if(activity !=null ){
                        BaseToast.makeText(activity, getString(R.string.save_contact_fail), Toast.LENGTH_SHORT).show();
                    }
                }
                return null;
            }
        }).subscribe();
    }


    /**
     * 隐藏对话框
     */
    public void hindDialog(){
        dismissProgressDialog();
        if (!AndroidUtil.isNetworkConnected(getActivity())) {
            BaseToast.show(R.string.network_disconnect);
        }else{
            BaseToast.show(R.string.operation_failed_toast);
        }
    }

    /**
     * 跳转到会话界面
     */
    private void conversation(){
        Conversation conversation = ConvCache.getInstance().getConversationByAddress(address);
        String clzName = MessageProxy.g.getServiceInterface().getClassName(MessageModuleConst.ClassType.MESSAGE_EDITOR__FRAGMENT_CLASS);
        Bundle bundle = new Bundle();
        Activity context = getActivity() ;
        if (conversation != null) {
            bundle.putString("address", conversation.getAddress());
            if(!TextUtils.isEmpty( conversation.getPerson())){
                bundle.putString("person", conversation.getPerson());
            }else{
                bundle.putString("person", person);
            }
            boolean isSlient = conversation.getSlientDate() > 0;
            bundle.putBoolean("slient", isSlient);
            bundle.putLong("loadtime", 0);
            bundle.putString("clzName", clzName);
            if (conversation.getType() == Type.TYPE_MSG_TEXT_DRAFT) {
                bundle.putString("draft", conversation.getBody());
            }
            bundle.putInt("unread", conversation.getUnReadCount());
            if(context !=null){
                MessageProxy.g.getUiInterface().goMessageDetailActivity(context,bundle);;
            }else{
                android.util.Log.e("GroupStrangerFragemnt" , "context = null ");
            }
        } else {
            //发送消息,第一个号码的联系人
            bundle.putString("address", address);
            bundle.putString("person", person);
            bundle.putString("clzName", clzName);
            if(context !=null){
//                MessageProxy.g.getUiInterface().goMessageDetailActivity(context,bundle);;
                MessageProxy.g.getUiInterface().goMessageDetailActivity(context,bundle);
            }else{
                android.util.Log.e("GroupStrangerFragemnt" , "context = null ");
            }
        }
        if(context!=null){
            context.finish();
        }
    }

    /**
     * 显示加载框
     */
    public void showDialog(){
        if(mProgressDialog != null ){
            mProgressDialog.show();
        }
    }

    /**
     * 跳转到群会话界面
     * @param groupId
     */
    public void goToChat(String groupId ){
        mPresenter.gotoChat( groupId , person ); // 跳转到聊天界面（群聊）
        if(mActivity != null && !TextUtils.isEmpty(mPresenter.getConsentEntryGroupID())){
            mActivity.finish(); // 关闭本界面
        }
    }


    /**
     * 交换名片成功与否从底底进程发送来的广播
     */
    class CallbackBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 群聊邀请
            if(!GroupChatInvitationFragment.getIsOpen()){ //  false 说明界面没有打开 ， 防止处理两次  因为使用广播
                if( intent.getAction() == BroadcastActions.MESSAGE_GROUP_STATUS_UPDATE_SUCCESS_ACTION ){ // 在同意前，群已经解散了
                    String groupID = intent.getStringExtra("GroupID");
                    if (!TextUtils.isEmpty(mPresenter.getConsentEntryGroupID()) && !TextUtils.isEmpty(groupID) && mPresenter.getConsentEntryGroupID().equals(groupID)) {
                        SysMsgUtils.updateGroupInvitation(mContext, groupID, Status.STATUS_DESTROY, 1);
                    }
                    dismissProgressDialog();
                }
            }
        }
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

}
