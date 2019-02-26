package com.cmicc.module_message.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.module.proxys.moduleaboutme.AboutMeProxy;
import com.app.module.proxys.modulecall.CallProxy;
import com.app.module.proxys.moduleredpager.RedpagerProxy;
import com.chinamobile.app.yuliao_business.model.BaseModel;
import com.chinamobile.app.yuliao_business.model.Employee;
import com.chinamobile.app.yuliao_business.model.GroupInfo;
import com.chinamobile.app.yuliao_business.provider.Conversations;
import com.chinamobile.app.yuliao_business.util.Status;
import com.chinamobile.app.yuliao_common.application.App;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_common.utils.SystemFileShare;
import com.chinamobile.app.yuliao_contact.model.PinYin;
import com.chinamobile.app.yuliao_core.db.LoginDaoImpl;
import com.chinamobile.icloud.im.sync.model.EmailKind;
import com.chinamobile.icloud.im.sync.model.OrganizationKind;
import com.chinamobile.icloud.im.sync.model.PhoneKind;
import com.chinamobile.icloud.im.sync.model.RawContact;
import com.cmcc.cmrcs.android.glide.GlidePhotoLoader;
import com.cmcc.cmrcs.android.ui.LazyRecyclerViewHolder;
import com.cmcc.cmrcs.android.ui.ListRecyclerAdapter;
import com.cmcc.cmrcs.android.ui.OnRecyclerItemClickListener;
import com.cmcc.cmrcs.android.ui.TabBar;
import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity;
import com.cmcc.cmrcs.android.ui.activities.HomeActivity;
import com.cmcc.cmrcs.android.ui.adapter.VcardAdapter;
import com.cmcc.cmrcs.android.ui.dialogs.CommomDialog;
import com.cmcc.cmrcs.android.ui.dialogs.SendCradDialog;
import com.cmcc.cmrcs.android.ui.fragments.BaseFragment;
import com.cmicc.module_message.ui.presenter.GroupChatListPresenter;
import com.cmcc.cmrcs.android.ui.utils.ContactSelectorUtil;
import com.cmcc.cmrcs.android.ui.utils.IPCUtils;
import com.cmcc.cmrcs.android.ui.utils.UmengUtil;
import com.cmcc.cmrcs.android.ui.utils.VcardContactUtils;
import com.cmcc.cmrcs.android.ui.utils.WrapContentLinearLayoutManager;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmicc.module_message.R;
import com.constvalue.CallModuleConst;
import com.constvalue.MessageModuleConst;
import com.juphoon.cmcc.app.lemon.MtcImConstants;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.IS_MULTI_FORWARD;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.VIDEO_DOODLE_IMG;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.KEY_GROUP_MEMBER_NUMBER;
import static com.cmcc.cmrcs.android.ui.utils.ContactSelectorUtil.SOURCE_START_OR_NEW_GROUP;
import static com.constvalue.MessageModuleConst.GroupChatSearchActivityConst.KEY_VALUE;

/**
 * Created by albert on 2018/5/7.
 * 聊天群的搜索
 */

public class GroupChatSearchActivity extends BaseActivity {



    private TabLayout mTab;
    private ViewPager mVp;
    private SearchBar mSearchBar;
    private ArrayList<ISearch> mISearches = new ArrayList<>();
    private ViewGroup searchBar;
    private GroupChatSearchListFragment allGroupFragment;

    private Bundle mBundle ;
    static EditText mEditText;
    OnSearchBarActionListener mListener;
    ImageView mIvClear;

    public static void start(Activity activity){
        activity.startActivity(new Intent(activity, GroupChatSearchActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat_search);
        searchBar = (ViewGroup) findViewById(R.id.search_bar);
        mIvClear = findViewById(R.id.iv_clear);
        mEditText = findViewById(R.id.edit_query);
        mSearchBar = new SearchBar(searchBar);
        allGroupFragment = GroupChatSearchListFragment.getByAll();
        mBundle = getIntent().getExtras() ;
        allGroupFragment.setArguments(mBundle);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fragment_container,allGroupFragment,"allGroupFragment");
        fragmentTransaction.commit();
        mSearchBar.setListener(new OnSearchBarActionListener() {
            @Override
            public void onBackPressed() {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if(imm != null && searchBar != null){
                    imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
                }
                finish();
            }

            @Override
            public void onQuery(CharSequence sequence) {
                allGroupFragment.onQuery(sequence);
            }

            @Override
            public void onClearQuery() {
                allGroupFragment.onClearQuery();
            }
        });
    }

    private List<TabBar.Item> bottomBarItems() {
        List<TabBar.Item> items = new ArrayList<>();
        GroupChatSearchListFragment normalGroupFragment = GroupChatSearchListFragment.getByNormal();
        GroupChatSearchListFragment enterpriseGroupFragment = GroupChatSearchListFragment.getByEnterprise();
        items.add(new TabBar.Item(normalGroupFragment, -1, R.string.s_tab_normal_group));
        items.add(new TabBar.Item(enterpriseGroupFragment, -1, R.string.s_tab_enterprise_group));

        mISearches.add(normalGroupFragment);
        mISearches.add(enterpriseGroupFragment);
        return items;
    }

    @Override
    protected void onDestroy() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null){
            imm.hideSoftInputFromInputMethod(getWindow().getDecorView().getWindowToken(), 0);
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
       if(mEditText!=null && !TextUtils.isEmpty(mEditText.getText().toString())){
           allGroupFragment.onQuery(mEditText.getText().toString());
       }
    }

    public interface ISearch{
        void onQuery(CharSequence sequence);
        void onClearQuery();
    }

    private  class SearchBar{

        SearchBar(ViewGroup parent){
            parent.findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mListener != null){
                        mListener.onBackPressed();
                    }
                }
            });
            mEditText.addTextChangedListener(new TextWatcher() {

                private String mQuery;

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    String query = s.toString();
                    if(!query.equals(mQuery)){
                        if(query.isEmpty()){
                            mIvClear.setVisibility(View.GONE);
                            if(mListener != null){
                                mListener.onClearQuery();
                            }
                        }else {
                            mIvClear.setVisibility(View.VISIBLE);
                            if(mListener != null){
                                mListener.onQuery(query);
                            }
                        }
                    }

                    mQuery = query;
                }
            });
            mIvClear = (ImageView) parent.findViewById(R.id.iv_clear);
            mIvClear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mEditText.setText("");
                }
            });
        }

        /**
         * 填写搜索栏的内容
         * @param
         */
        public void setEditTextVaule(String keyVaule){
            mEditText.setText(keyVaule);
        }

        public void setListener(OnSearchBarActionListener listener) {
            mListener = listener;
        }

    }
    public interface OnSearchBarActionListener extends ISearch{
        void onBackPressed();
    }

    public static class GroupChatSearchListFragment extends BaseFragment implements ISearch{

        private static final String TAG = "GroupChatSearchListFragment";

        private static final String BUNDLE_KEY_TYPE = "GroupChatSearchListFragment.TYPE";
        private static final int TYPE_ALL = 0;
        private static final int TYPE_NORMAL = 1;
        private static final int TYPE_ENTERPRISE = 2;
        private int mType;
        private RecyclerView mRv;
        private InternalAdapter mAdapter;
        private TextView noResultView;
        private LinearLayout title_ll;

        private int cmd; //跳转过来的类型（从什么地方跳转过来的，不同地方有不同跳转
        private String toName;//群名
        private Map<String, Boolean> mapCheck = new HashMap<>();//名片选中状态
        private TextView tv_submit; //名片发送按钮
        private Activity mActivity ;
        private GroupChatListPresenter mPresenter ;
        private Bundle mBundle ;

        public static GroupChatSearchListFragment getByAll(){
            return newInstance(TYPE_ALL);
        }

        public static GroupChatSearchListFragment getByNormal(){
            return newInstance(TYPE_NORMAL);
        }

        public static GroupChatSearchListFragment getByEnterprise(){
            return newInstance(TYPE_ENTERPRISE);
        }

        private static GroupChatSearchListFragment newInstance(int type) {
            Bundle args = new Bundle();
            args.putInt(BUNDLE_KEY_TYPE, type);
            GroupChatSearchListFragment fragment = new GroupChatSearchListFragment();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mType = getArguments().getInt(BUNDLE_KEY_TYPE);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            mRv = (RecyclerView) view.findViewById(R.id.recyclerView);
            mRv.getRecycledViewPool().setMaxRecycledViews(0, 10);
            mRv.setHasFixedSize(true);
            WrapContentLinearLayoutManager layoutManager = new WrapContentLinearLayoutManager(mRv.getContext(),
                    LinearLayoutManager.VERTICAL, false);
            mRv.setLayoutManager(layoutManager);
            noResultView = (TextView) view.findViewById(R.id.no_search_result);
            title_ll = view.findViewById(R.id.title_ll);

            mAdapter = new InternalAdapter();
            mAdapter.setShwoNuber(getArguments().getBoolean(KEY_GROUP_MEMBER_NUMBER));
            mAdapter.setOnRecyclerItemClickListener(new OnRecyclerItemClickListener<GroupInfo>() {
                @Override
                public void onItemClick(View v, GroupInfo groupInfo, int position) {
                    disposeClick(groupInfo);
                }
            });
            mRv.setAdapter(mAdapter);
            mActivity = getActivity() ;
            mPresenter = new GroupChatListPresenter(mActivity , null);
            mBundle = getArguments() ;
            if(mBundle!=null
                    && !TextUtils.isEmpty(mBundle.getString(KEY_VALUE))
                    && mActivity instanceof GroupChatSearchActivity){
                ((GroupChatSearchActivity)mActivity).mSearchBar.setEditTextVaule(mBundle.getString(KEY_VALUE));
            }
        }


        /**
         * 处理Item的点击事件
         * @param groupInfo
         */
        private void disposeClick(final GroupInfo groupInfo) {
            if(!TextUtils.isEmpty(groupInfo.getPerson())) {
                toName = groupInfo.getPerson();
            }else{
                toName = "null".intern();
            }
            LogF.d(TAG, "onContactItemClick: mAdapter getItem"+groupInfo.getPerson());
            // 处理来自转发请求
            final Bundle bundle = getArguments();
            if (bundle != null) {
                cmd = bundle.getInt(ContactSelectorActivity.KEY_GROUP_CHAT);
                if(cmd == MessageModuleConst.GroupChatListMergaActivityConst.GROUP_CHAT_LIST){ // 通讯录 - 群聊
                    UmengUtil.buryPoint(getContext(), "contacts_message_groupmessage", "通讯录-群聊-进入群聊", 0);
                    mPresenter.openItem(mContext , groupInfo);
                    //mActivity.finish();
                    return;
                }else if (cmd == ContactSelectorUtil.SOURCE_MESSAGE_FORWARD) { // 来自消息转发
                    String title = mContext.getResources().getString(R.string.send_to);
                    if(!TextUtils.isEmpty(groupInfo.getPerson())) {
                        title += groupInfo.getPerson();
                    }else{
                        title += "null".intern();
                    }
                    CommomDialog commomDialog=new CommomDialog(getActivity(),null,title);
                    commomDialog.setCanceledOnTouchOutside(true);
                    commomDialog.show();
                    commomDialog.setOnPositiveClickListener(new CommomDialog.OnClickListener() {
                        @Override
                        public void onClick() {
                            //消息多选转发功能，返回群信息
                            if(bundle.getBoolean(IS_MULTI_FORWARD)){
                                Intent intent = new Intent();
                                intent.putExtra("isGroup",true);
                                intent.putExtra("number",groupInfo.getAddress());
                                BaseToast.show(mContext, mContext.getResources().getString(R.string.toast_msg_has_forwarded));
                                if(mActivity != null){
                                    mActivity.setResult(Activity.RESULT_OK,intent);
                                    mActivity.finish();
                                }
                            }

                            mPresenter.handleMessageForward(bundle, groupInfo);
                            //视频涂鸦图片转发完成，回到视频通话界面
                            boolean doodleForward = bundle.getBoolean(VIDEO_DOODLE_IMG);
                            if(doodleForward){
                                if (IPCUtils.getInstance().isMergeCall()) {
                                    if(mContext != null){
                                        if(IPCUtils.getInstance().isVoiceCall()){
                                            //CallProxy.g.getUiInterface().goToVoiceCallActivity(mContext);
                                            CallProxy.g.getUiInterface().goToActivity(mContext,null, CallModuleConst.VOICE_CALL_ACTIVITY_TYPE);
                                        }else {
                                           // CallProxy.g.getUiInterface().goToMergeCallActivity(mContext);
                                            CallProxy.g.getUiInterface().goToActivity(mContext,null, CallModuleConst.MERGE_CALL_ACTIVITY_TYPE);
                                        }
                                    }
                                }
                                if(mActivity != null ){
                                    mActivity.finish();
                                }
                            }else {
                                Intent intent  = new Intent();
                                intent.putExtra(ContactSelectorActivity.ACTIVITY_RESULT_FROM,ContactSelectorActivity.SHARE_QRCODE_TO_A_GROUP_REQUEST_CODE);
                                if(mActivity != null ){
                                    mActivity.setResult(Activity.RESULT_OK, intent);
                                    mActivity.finish();
                                    if (bundle.getString(SystemFileShare.SHARE_TYPE, "").equals(SystemFileShare.SYSTEM_SHARE)) {
                                        //分享后跳转到home
                                        Intent intentHome = new Intent(mContext, HomeActivity.class);
                                        mActivity.startActivity(intentHome);
                                    }
                                }
                            }
                        }
                    });
                    return;
                }else if(cmd == ContactSelectorUtil.SOURCE_SHARE_MY_CARD){// 分享本人名片到聊天
                    handleCardToChat(groupInfo.getAddress(), AboutMeProxy.g.getServiceInterface().getPersonalRawContact(getActivity()));
                    return;
                }else if(cmd == ContactSelectorUtil.SOURCE_SHARE_CARD_CONTACT_DETAIL_PAGE){ // 从联系人资料页面分享名片
                    showVCardDialogByContactDetail(groupInfo.getAddress(), true);
                    return;
                }/* else if (cmd == ContactSelectorUtil.SOURCE_CARD_VOUCHER_SHARE) {// 卡券共享
                    RedpagerProxy.g.getUiInterface().selectGroupCallback(groupInfo.getAddress());
                    Intent intent = new Intent();
                    if(mActivity != null){
                        mActivity.setResult(Activity.RESULT_OK, intent);
                        mActivity.finish();
                    }
                    return;
                }*/else if(cmd == SOURCE_START_OR_NEW_GROUP){
                    mPresenter.openItem(mContext , groupInfo);
                    if(mActivity != null){
                        mActivity.setResult(Activity.RESULT_OK);
                        mActivity.finish();
                    }
                    return;
                }
            }
            mPresenter.openItem(mActivity, groupInfo);
        }

        /**
         * 分享名片
         * @param toNumber
         * @param rawContact
         */
        public void handleCardToChat(String toNumber, RawContact rawContact) {
            if (rawContact == null) {
                return;
            }

            String[] str = new String[4];
            for (int i = 0; i < str.length; i++) {
                str[i] = "";
            }

            if (rawContact.getPhones() != null) {
                for (PhoneKind phone : rawContact.getPhones()) {
                    if (!TextUtils.isEmpty(phone.getNumber())) {
                        //mList.add(phone);
                        str[0] = phone.getNumber();
                        break;
                    }
                }
            }
            if (TextUtils.isEmpty(str[0])) {
                str[0] = LoginDaoImpl.getInstance().queryLoginUser(mContext);
            }
            PhoneKind phoneKind = new PhoneKind();
            phoneKind.setNumber(str[0]);
            List<PhoneKind> phoneKinds = new ArrayList<>();
            phoneKinds.add(phoneKind);

            if (rawContact.getOrganizations() != null) {
                if (rawContact.getOrganizations().size() > 0) {
                    OrganizationKind organizationKind = rawContact.getOrganizations().get(0);
                    if (organizationKind != null && !TextUtils.isEmpty(organizationKind.getCompany()) && !TextUtils.isEmpty(organizationKind.getCompany().trim())) {
                        str[1] = organizationKind.getCompany().trim();
                    }
                    if (organizationKind != null && !TextUtils.isEmpty(organizationKind.getTitle()) && !TextUtils.isEmpty(organizationKind.getTitle().trim())) {
                        str[2] = organizationKind.getTitle().trim();
                    }
                }
            }
            if (rawContact.getEmails() != null && rawContact.getEmails().size() > 0) {
                EmailKind eKind = rawContact.getEmails().get(0);
                if (eKind != null && !TextUtils.isEmpty(eKind.getValue()) && !TextUtils.isEmpty(eKind.getValue().trim())) {
                    str[3] = eKind.getValue().trim();
                }
            }
            showVcardExportDialog(str, rawContact.getStructuredName().getDisplayName());
        }


        public void showVcardExportDialog(String[] strings, String name) {
            final AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.NoBackGroundDialog).create();
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_vcard, null);
            dialog.setContentView(view);

            TextView tv_cancel = view.findViewById(R.id.clase);
            tv_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            });

            tv_submit = view.findViewById(R.id.sure);
            tv_submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPresenter.setChecks(mapCheck);
                    mPresenter.submitVcard();
                    if (dialog != null) {
                        dialog.dismiss();
                    }

                }
            });

            RecyclerView mRecyclerView = view.findViewById(R.id.black_item_list);
            LinearLayoutManager mLayoutManager = new WrapContentLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            mRecyclerView.setLayoutManager(mLayoutManager);
            final VcardAdapter mAdapter = new VcardAdapter(getActivity(), strings);
            if(cmd == ContactSelectorUtil.SOURCE_SHARE_MY_CARD ) {
                mAdapter.setHidePhone(true);
            }
            List<String> titles = new ArrayList<>();
            titles.addAll(mAdapter.getData());
            mapCheck.clear();
            for (String title : titles) {
                mapCheck.put(title, true);
            }
            mAdapter.setIsCheckMap(mapCheck);
            setClick();
            mAdapter.setItemClickListener(new VcardAdapter.MyItemClickListener() {

                @Override
                public void onItemClick(View view, int position) {
                    LogF.d(TAG, "VcardAdapter ItemClick" + position);
                    if (mapCheck.get(mAdapter.getData().get(position))) {
                        mapCheck.put(mAdapter.getData().get(position), false);
                    } else {
                        mapCheck.put(mAdapter.getData().get(position), true);
                    }
                    mAdapter.setIsCheckMap(mapCheck);
                    setClick();
                    mAdapter.notifyDataSetChanged();
                }
            });
            mRecyclerView.setAdapter(mAdapter);

            TextView title = view.findViewById(R.id.vcard_dialog_title);
            title.setText(mContext.getResources().getString(R.string.send_my_card_to)+toName);

            ImageView icon = view.findViewById(R.id.contact_icon);
            GlidePhotoLoader.getInstance(mActivity.getApplication()).loadPhoto(getActivity(), icon, strings[0]);
        }

        public void setClick() {
            boolean isClick;
            boolean flag = false;
            if (mapCheck != null && mapCheck.size() > 0) {
                for (String key : mapCheck.keySet()) {
                    if (mapCheck.get(key)) {
                        flag = true;
                        break;
                    }
                }
                isClick = flag;
            } else {
                isClick = false;
            }
            if (isClick) {
                tv_submit.setEnabled(true);
                tv_submit.setTextColor(mContext.getResources().getColor(R.color.black));
            } else {
                tv_submit.setEnabled(false);
                tv_submit.setTextColor(mContext.getResources().getColor(R.color.color_868686));
            }
        }


        /**
         * 分享名片
         * @param number
         * @param isGroup
         */
        private void showVCardDialogByContactDetail(final String number, final boolean isGroup) {
            final Employee employee = (Employee) getArguments().getSerializable(ContactSelectorActivity.BUNDLE_KEY_SIMPLE_CONTACT);
            final SendCradDialog dialog  = new SendCradDialog(getContext());
            dialog.getWindow().setWindowAnimations(R.style.sendCradSytl);
            dialog.setSendCradInterface(new SendCradDialog.SendCradInterface() {
                @Override
                public void sendCrad() {
                    mPresenter.setChecks(mapCheck);
                    mPresenter.submitVCardFromContactDetail(number, employee, isGroup);
                    dialog.dismiss();
                }
                @Override
                public void cancleSend() {
                    dialog.dismiss();
                }

                @Override
                public void companySelect() {
                    Boolean isChocie = mapCheck.get(VcardContactUtils.card_fields[1]);
                    mapCheck.put(VcardContactUtils.card_fields[1] , isChocie == null?false:!isChocie) ;
                    dialog.setCompanyImage(isChocie == null?false:!isChocie);
                }

                @Override
                public void positionSelect() {
                    Boolean isChocie = mapCheck.get(VcardContactUtils.card_fields[2]);
                    mapCheck.put(VcardContactUtils.card_fields[2] , isChocie == null?false:!isChocie) ;
                    dialog.setPositionImage(isChocie == null?false:!isChocie);
                }

                @Override
                public void emailSelect() {
                    Boolean isChocie = mapCheck.get(VcardContactUtils.card_fields[3]);
                    mapCheck.put(VcardContactUtils.card_fields[3] , isChocie == null?false:!isChocie) ;
                    dialog.setEmailImage(isChocie == null?false:!isChocie);
                }
            });
            dialog.setNameText(employee.getName());
            mapCheck.clear();
            String[] str = new String[4];
            str[0] = employee.regMobile;
            if(!TextUtils.isEmpty(employee.departments)){
                str[1]=employee.departments;
            }else{
                if(!TextUtils.isEmpty(employee.enterpriseName)){
                    str[1]=employee.enterpriseName;
                }else{
                    LogF.d(TAG, " initData() employeeEnterpriseName is null");
                }
            }
            if(!TextUtils.isEmpty(employee.positions)){
                str[2]=employee.positions.replaceAll(",", " ");
            }
            if(employee.email != null && employee.email.size() > 0) {
                String email = employee.email.get(0);
                str[3] = email;
            }
            for(int i = 0 ; i< str.length ; i++ ){
                if(i == 0 ){
                    mapCheck.put(VcardContactUtils.card_fields[0],true); // 手机号码必须选中
                    dialog.setHeadImge(str[i]);     // 头像
                    dialog.setPhoneText(str[i]);    // 手机号码
                }else if(i == 1 && !TextUtils.isEmpty(str[i])){
                    mapCheck.put(VcardContactUtils.card_fields[1],false); // 默认不选中
                    dialog.setCompanyText(str[i]);  // 公司名称
                }else if(i == 2 && !TextUtils.isEmpty(str[i])){
                    mapCheck.put(VcardContactUtils.card_fields[2],false); // 默认不选中
                    dialog.setPositionText(str[i]); // 职位
                }else if(i == 3 && !TextUtils.isEmpty(str[i])){
                    mapCheck.put(VcardContactUtils.card_fields[3],false); // 默认不选中
                    dialog.setemailText(str[i]);    // 邮箱
                }
            }
            dialog.show();
        }


        @Override
        public void initData() {
        }

        @Override
        public int getLayoutId() {
            return R.layout.item_recycler_view;
        }

        @Override
        public void onQuery(CharSequence sequence) {
            LogF.d(TAG, "onQuery: " + sequence);
            mAdapter.setKeyWord(sequence.toString());
            Callback callback = new Callback(getActivity()){

                @Override
                public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                    ArrayList<GroupInfo> groupSearchList = new ArrayList<>();
                    if (cursor != null) {
                        for (int i = 0, t = cursor.getCount(); i < t && cursor.moveToNext(); i++) {
                            GroupInfo groupInfo = new GroupInfo();
                            groupInfo.setIdentify(cursor.getString(cursor.getColumnIndex(BaseModel.COLUMN_NAME_IDENTIFY)));
                            groupInfo.setAddress(cursor.getString(cursor.getColumnIndex(BaseModel.COLUMN_NAME_ADDRESS)));
                            groupInfo.setPerson(cursor.getString(cursor.getColumnIndex(BaseModel.COLUMN_NAME_PERSON)));
                            groupInfo.setMemberCount(cursor.getInt(cursor.getColumnIndex(BaseModel.COLUMN_NAME_MEMBER_COUNT)));
                            groupInfo.setType(cursor.getInt(cursor.getColumnIndex(BaseModel.COLUMN_NAME_TYPE)));
                            groupSearchList.add(groupInfo);
                        }
                    }
                    if (groupSearchList.size()==0){
                        noResultView.setVisibility(View.VISIBLE);
                        title_ll.setVisibility(View.GONE);
                    }else {
                        noResultView.setVisibility(View.GONE);
                        title_ll.setVisibility(View.VISIBLE);
                    }
                    Collections.sort(groupSearchList, new Comparator<GroupInfo>() {
                        @Override
                        public int compare(GroupInfo left, GroupInfo right) {
                            PinYin leftPinYin = PinYin.buildPinYinDuoYinXing(left.getPerson());
                            PinYin rightPinYin = PinYin.buildPinYinDuoYinXing(right.getPerson());
                            String lSortKey = leftPinYin.getSortKey();
                            String rSortKey = rightPinYin.getSortKey();
                            if (TextUtils.isEmpty(lSortKey))
                                return 1;
                            if (TextUtils.isEmpty(rSortKey))
                                return -1;
                            if (lSortKey.startsWith("#") && !rSortKey.startsWith("#"))
                                return 1;
                            if (!lSortKey.startsWith("#") && rSortKey.startsWith("#"))
                                return -1;
                            return Collator.getInstance(Locale.CHINA).compare(lSortKey, rSortKey);
                        }
                    });
                    mAdapter.setData(groupSearchList);
                }
            };
            callback.setType(mType);
            callback.setQuery(sequence);
            getLoaderManager().restartLoader(0, null, callback);
        }

        @Override
        public void onClearQuery() {
            LogF.d(TAG, "onClearQuery: ");
            noResultView.setVisibility(View.GONE);
            title_ll.setVisibility(View.GONE);
            mAdapter.setData(null);
        }

        private static class InternalAdapter extends ListRecyclerAdapter<GroupInfo>{

            private String mKeyWord;
            private boolean isShwoNuber ;

            public void setShwoNuber(boolean shwoNuber) {
                isShwoNuber = shwoNuber;
            }

            public void setKeyWord(String keyWord) {
                mKeyWord = keyWord;
            }

            @Override
            protected int getLayoutRes() {
                return R.layout.item_search_group_chat;
            }

            @Override
            protected void onBindViewHolder(LazyRecyclerViewHolder holder, GroupInfo groupInfo, int position) {
                TextView tvContactName = holder.get(R.id.tv_name);
                ImageView ivContactImage = holder.get(R.id.iv_head);
                ImageView ivGroupEP = holder.get(R.id.group_ep);
                ImageView ivGroupParty = holder.get(R.id.group_party);
                TextView nuberTv = holder.get(R.id.nuber_tv);
                GlidePhotoLoader.getInstance(App.getAppContext())
                        .loadGroupPhoto(App.getAppContext(), ivContactImage, null, groupInfo.getAddress());
                if (groupInfo.getType() == MtcImConstants.EN_MTC_GROUP_TYPE_ENTERPRISE) {
                    ivGroupParty.setVisibility(View.GONE);
                    ivGroupEP.setVisibility(View.VISIBLE);
                } else if (groupInfo.getType() == MtcImConstants.EN_MTC_GROUP_TYPE_PARTY) {
                    ivGroupParty.setVisibility(View.VISIBLE);
                    ivGroupEP.setVisibility(View.GONE);
                } else {
                    ivGroupEP.setVisibility(View.GONE);
                    ivGroupParty.setVisibility(View.GONE);
                }
                String name = groupInfo.getPerson();
                if (!TextUtils.isEmpty(mKeyWord) && !TextUtils.isEmpty(name)) {
                    int index = name.toLowerCase().indexOf(mKeyWord.toLowerCase());
                    SpannableString temp = new SpannableString(name);
                    temp.setSpan(new BackgroundColorSpan(tvContactName.getResources().getColor(R.color.color_fcf5aa)),
                            index, index + mKeyWord.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    tvContactName.setText(temp);
                } else {
                    tvContactName.setText(name);
                }
                if(isShwoNuber){
                    nuberTv.setText("("+groupInfo.getMemberCount()+")");
                }
            }
        }


        private abstract static class Callback implements LoaderManager.LoaderCallbacks<Cursor>{

            private String query;
            private Context context;
            private int type;

            public Callback(Context context) {
                this.context = context;
            }

            public void setQuery(CharSequence query) {
                this.query = query.toString();
            }

            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                String groupType = "";
                String where = "";
                if(type == TYPE_ALL){
                    where = BaseModel.COLUMN_NAME_STATUS + "=?" + " AND " + BaseModel.COLUMN_NAME_PERSON + " like ?";
                } else if(type == TYPE_NORMAL){
                    groupType = BaseModel.COLUMN_NAME_TYPE+" = "+String.valueOf(MtcImConstants.EN_MTC_GROUP_TYPE_GENERAL);
                    where = BaseModel.COLUMN_NAME_STATUS + "=?" + " AND " + groupType + " AND " + BaseModel.COLUMN_NAME_PERSON + " like ?";
                } else if(type == TYPE_ENTERPRISE){
                    groupType = "(type = 2 or type = 3)";
                    where = BaseModel.COLUMN_NAME_STATUS + "=?" + " AND " + groupType + " AND " + BaseModel.COLUMN_NAME_PERSON + " like ?";
                }

                String[] whereArgs = new String[]{Status.STATUS_OK + "","%" + query + "%"};
                String[] projection = new String[] { BaseModel.COLUMN_NAME_IDENTIFY,BaseModel.COLUMN_NAME_ADDRESS,BaseModel.COLUMN_NAME_OWNER,BaseModel.COLUMN_NAME_PERSON,BaseModel.COLUMN_NAME_DATE,BaseModel.COLUMN_NAME_TYPE,BaseModel.COLUMN_NAME_MEMBER_COUNT,
                        "(select date from Conversation where address=GroupInfo.address) as d"};
                return new CursorLoader(context, Conversations.GroupInfo.CONTENT_URI, projection, where, whereArgs, "d desc,person asc");
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
            }

            public void setType(int type) {
                this.type = type;
            }
        }
    }


    @Override
    protected void findViews() {
    }

    @Override
    protected void init() {

    }
}
