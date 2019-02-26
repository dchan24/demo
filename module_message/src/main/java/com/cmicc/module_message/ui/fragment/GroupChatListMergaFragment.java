package com.cmicc.module_message.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.module.proxys.moduleaboutme.AboutMeProxy;
import com.app.module.proxys.modulecall.CallProxy;
import com.chinamobile.app.utils.StringUtil;
import com.chinamobile.app.yuliao_business.model.BaseModel;
import com.chinamobile.app.yuliao_business.model.Employee;
import com.chinamobile.app.yuliao_business.model.GroupInfo;
import com.chinamobile.app.yuliao_business.util.BeanUtils;
import com.chinamobile.app.yuliao_business.util.BuryingPointUtils;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_common.utils.SystemFileShare;
import com.chinamobile.app.yuliao_contact.model.PinYin;
import com.cmcc.cmrcs.android.glide.GlidePhotoLoader;
import com.cmcc.cmrcs.android.ui.OnRecyclerItemClickListener;
import com.cmcc.cmrcs.android.ui.WrapHeaderFooterRecyclerAdapter;
import com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity;
import com.cmcc.cmrcs.android.ui.activities.HomeActivity;
import com.cmicc.module_message.ui.adapter.GroupChatListMergaAdapter;
import com.cmcc.cmrcs.android.ui.adapter.VcardAdapter;
import com.cmicc.module_message.ui.constract.GroupChatListContract;
import com.cmcc.cmrcs.android.ui.dialogs.CommomDialog;
import com.cmcc.cmrcs.android.ui.dialogs.SendCradDialog;
import com.cmcc.cmrcs.android.ui.fragments.BaseFragment;
import com.cmicc.module_message.ui.presenter.GroupChatListPresenter;
import com.cmcc.cmrcs.android.ui.utils.IPCUtils;
import com.cmcc.cmrcs.android.ui.utils.UmengUtil;
import com.cmcc.cmrcs.android.ui.utils.VcardContactUtils;
import com.cmcc.cmrcs.android.ui.utils.WrapContentLinearLayoutManager;
import com.cmcc.cmrcs.android.ui.view.contactlist.ExpIndexView;
import com.cmcc.cmrcs.android.ui.view.contactlist.IndexBarView;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmicc.module_message.R;
import com.constvalue.CallModuleConst;
import com.constvalue.MessageModuleConst;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.chinamobile.app.yuliao_business.model.BaseModel.COLUMN_NAME_ADDRESS;
import static com.chinamobile.app.yuliao_business.model.BaseModel.COLUMN_NAME_DATE;
import static com.chinamobile.app.yuliao_business.model.BaseModel.COLUMN_NAME_IDENTIFY;
import static com.chinamobile.app.yuliao_business.model.BaseModel.COLUMN_NAME_PERSON;
import static com.chinamobile.app.yuliao_business.model.BaseModel.COLUMN_NAME_TYPE;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.BUNDLE_KEY_SIMPLE_CONTACT;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.IS_MULTI_FORWARD;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.KEY_GROUP_CHAT;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.VIDEO_DOODLE_IMG;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.ACTIVITY_RESULT_FROM;
import static com.cmcc.cmrcs.android.ui.utils.ContactSelectorUtil.SOURCE_MESSAGE_FORWARD;
import static com.cmcc.cmrcs.android.ui.utils.ContactSelectorUtil.SOURCE_NEW_GROUP_NO_GROUP;
import static com.cmcc.cmrcs.android.ui.utils.ContactSelectorUtil.SOURCE_SHARE_CARD_CONTACT_DETAIL_PAGE;
import static com.cmcc.cmrcs.android.ui.utils.ContactSelectorUtil.SOURCE_SHARE_MY_CARD;
import static com.cmcc.cmrcs.android.ui.utils.ContactSelectorUtil.SOURCE_START_OR_NEW_GROUP;

/**
 * Created by yangshengfu on 2018/11/22.
 */

public class GroupChatListMergaFragment extends BaseFragment implements GroupChatListContract.IView , View.OnClickListener{


    private String TAG = "GroupChatListMergaFragment";
    private Activity mActivity ;

    private RecyclerView mRecyclerView; // 列表
    private IndexBarView mIndexBarView; // 索引
    private ExpIndexView mExpIndexView; // 气泡
    private View mEmptyView; // 群列表为空
    private LinearLayout mEmptyContentLl ;

    private GroupChatListPresenter mPresenter;

    private WrapContentLinearLayoutManager mLayoutManager;
    private WrapHeaderFooterRecyclerAdapter mHeaderFooterRecyclerAdapter ;
    private GroupChatListMergaAdapter mAdapter ;

    public String mIndexKey;

    private ArrayList<String> mValidWordCache;
    private HashMap<String, PinYin> pinYinHashMap = new HashMap<>();
    private HashMap<String, Character> characterHashMap = new HashMap<>();


    private int mExpIndexViewHeight = 0 ;// 控件mExpIndexView气泡的距离顶部的偏差高度

    private int cmd; //跳转过来的类型（从什么地方跳转过来的，不同地方有不同跳转
    private String toName;//群名
    private Map<String, Boolean> mapCheck = new HashMap<>();//名片选中状态
    private TextView tv_submit; //名片发送按钮

    @Override
    public int getLayoutId() {
        return R.layout.fragment_group_chat_list_merga;
    }

    @Override
    public void initViews(View rootView){
        mRecyclerView = rootView.findViewById(R.id.recyclerView);
        mIndexBarView = rootView.findViewById(R.id.contact_index_bar_view);
        mExpIndexView = rootView.findViewById(R.id.contact_exp_index_view);

        mEmptyView = LayoutInflater.from(mContext).inflate(R.layout.empty_group_chat_list, null);
        mEmptyContentLl = mEmptyView.findViewById(R.id.ll_content);
        mEmptyView.findViewById(R.id.creategroup).setOnClickListener(this); // 群类表为空时，点击创建群按钮

        mIndexBarView.setIndexWordHeightLight(true);
        mIndexBarView.setStarVisible(false);// 不显示⭐️
        mIndexBarView.setOnIndexTouchListener(new IndexBarView.OnIndexTouchListener() {
            @Override
            public void onIndex(String word) {

            }
            @Override
            public void onIndex(String word, int[] location) {
                mExpIndexView.setTopOffset(location[1]-mExpIndexViewHeight);
                mExpIndexView.show(word);
                if (setSelectionByWord(word)) {
                    mIndexBarView.setIndexWordPosition(word);
                }
            }
        });

        mLayoutManager = new WrapContentLinearLayoutManager(mRecyclerView.getContext(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new GroupChatListMergaAdapter();
        mHeaderFooterRecyclerAdapter = new WrapHeaderFooterRecyclerAdapter(mAdapter);
        mRecyclerView.setAdapter(mHeaderFooterRecyclerAdapter);
        mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                ViewGroup.LayoutParams layoutParams = mEmptyContentLl.getLayoutParams();
                layoutParams.height = mRecyclerView.getHeight();
                layoutParams.width = mRecyclerView.getWidth();
                mEmptyContentLl.setLayoutParams(layoutParams);
                mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        });
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            }
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                List<GroupInfo> data = mAdapter.getData();
                if (firstVisibleItem >= 0 && data != null && firstVisibleItem < data.size()) {
                    GroupInfo groupInfo = data.get(firstVisibleItem);
                    if (groupInfo != null) {
                        PinYin pinYin = PinYin.buildPinYinDuoYinXing(groupInfo.getPerson());
                        String indexKey = pinYin.getIndexKey();
                        if ( indexKey != null && !indexKey.equalsIgnoreCase(mIndexKey)) {
                            mIndexBarView.setIndexWordPosition(indexKey.toUpperCase());
                        }
                        mIndexKey = indexKey;
                    }
                }
            }
        });
        mAdapter.setOnRecyclerItemClickListener(new OnRecyclerItemClickListener<GroupInfo>() {
            @Override
            public void onItemClick(View v, GroupInfo groupInfo, int position) {
                disposeClick(groupInfo);
            }
        });
    }

    @Override
    public void initData() {
        mActivity = getActivity() ;
        Bundle bundle = getArguments();
        if (bundle != null) {
            cmd = bundle.getInt(KEY_GROUP_CHAT);
        }
        if(mPresenter!=null){
            mPresenter.start(); // 开始加载数据
        }
    }


    /**
     * 数据加载完毕刷新列表
     * @param cursor
     */
    @Override
    public void updateListView(Cursor cursor) {
        if(cursor == null){
            return;
        }
        BeanUtils.ColumnIndex columnIndex = new BeanUtils.ColumnIndex(cursor);
        GroupInfo temp;
        List<GroupInfo> groupInfos = new ArrayList<>();
        if(cursor.moveToFirst()){
            do {
                temp = getGroupInfoFromCursor(cursor, columnIndex);
                groupInfos.add(temp);
            }while (cursor.moveToNext());
        }
        if (groupInfos.isEmpty()) { // 群列表为空
            mAdapter.setData(null);
            mHeaderFooterRecyclerAdapter.addHeaderView(mEmptyView);// 添加列表空的提示
        } else {
            mHeaderFooterRecyclerAdapter.removeHeaderView(mEmptyView);
            if (characterHashMap != null) {
                characterHashMap.clear();
            }
            if (pinYinHashMap != null) {
                pinYinHashMap.clear();
            }
            Collections.sort(groupInfos, new Comparator<GroupInfo>() {
                @Override
                public int compare(GroupInfo left, GroupInfo right) {
                    String leftId = left.getAddress();
                    String rightId = right.getAddress();
                    if (!characterHashMap.containsKey(leftId)) {
                        PinYin leftPinYin = PinYin.buildPinYinDuoYinXing(left.getPerson());
                        if (!TextUtils.isEmpty(leftPinYin.getIndexKey())) {
                            characterHashMap.put(leftId, leftPinYin.getIndexKey().charAt(0));
                        }
                        pinYinHashMap.put(leftId, leftPinYin);
                    }
                    if (!characterHashMap.containsKey(rightId)) {
                        PinYin rightPinYin = PinYin.buildPinYinDuoYinXing(right.getPerson());
                        if (!TextUtils.isEmpty(rightPinYin.getIndexKey())) {
                            characterHashMap.put(rightId, rightPinYin.getIndexKey().charAt(0));
                        }
                        pinYinHashMap.put(rightId, rightPinYin);
                    }
                    String lSortKey = pinYinHashMap.get(leftId).getSortKey();
                    String rSortKey = pinYinHashMap.get(rightId).getSortKey();
                    if (TextUtils.isEmpty(lSortKey)) return 1;
                    if (TextUtils.isEmpty(rSortKey)) return -1;
                    if (lSortKey.startsWith("#") && !rSortKey.startsWith("#")) return 1;
                    if (!lSortKey.startsWith("#") && rSortKey.startsWith("#")) return -1;
                    return Collator.getInstance(Locale.CHINA).compare(lSortKey, rSortKey);
                }
            });
            int size = groupInfos.size();
            if (characterHashMap != null) {
                for (int i = 0; i < size - 1; i++) {
                    for (int j = 0; j < size - i - 1; j++) {
                        char indexKey1 = characterHashMap.get(groupInfos.get(j).getAddress());
                        char indexKey2 = characterHashMap.get(groupInfos.get(j + 1).getAddress());
                        if (indexKey1 == '#' || indexKey2 == '#') {
                            continue;
                        }
                        if (indexKey1 > indexKey2) {
                            Collections.swap(groupInfos, j, j + 1);
                        }
                    }
                }
            }
            mAdapter.setData(groupInfos);
            mValidWordCache = createValidWordCache(groupInfos);
            refreshIndexBar();
        }
    }

    /**
     * 解析数据源：Cursor 解析群的数据
     * @param cursor
     * @param columnIndex
     * @return
     */
    private static GroupInfo getGroupInfoFromCursor(Cursor cursor, BeanUtils.ColumnIndex columnIndex) {
        GroupInfo g = new GroupInfo();
        String value = columnIndex.getValue(cursor, BaseModel.COLUMN_NAME_ID);
        if (!StringUtil.isEmpty(value)) {
            g.setId(Long.valueOf(value));
        }
        value = columnIndex.getValue(cursor, COLUMN_NAME_IDENTIFY);
        if (!StringUtil.isEmpty(value)) {
            g.setIdentify(value);
        }
        value = columnIndex.getValue(cursor, COLUMN_NAME_ADDRESS);
        if (!StringUtil.isEmpty(value)) {
            g.setAddress(value);
        }
        value = columnIndex.getValue(cursor, COLUMN_NAME_PERSON);
        if (!StringUtil.isEmpty(value)) {
            g.setPerson(value);
        }
        value = columnIndex.getValue(cursor, COLUMN_NAME_DATE);
        if (!StringUtil.isEmpty(value)) {
            g.setDate(Long.valueOf(value));
        }
        value = columnIndex.getValue(cursor, COLUMN_NAME_TYPE);
        if (!StringUtil.isEmpty(value)) {
            g.setType(Integer.valueOf(value));
        }
        return g;
    }

    /**
     * 获取首字母并转化成大写
     * @param groupInfos
     * @return
     */
    public ArrayList<String> createValidWordCache(List<GroupInfo> groupInfos) {
        int size = groupInfos.size();
        ArrayList<String> validWordCache = new ArrayList<>();
        GroupInfo lastGroup = null;
        try {
            for (int i = 0; i < size; i++) {
                if (needToAddCache(groupInfos.get(i), lastGroup)) {
                    validWordCache.add(PinYin.buildPinYinDuoYinXing(groupInfos.get(i).getPerson()).getIndexKey().toUpperCase());
                }
                lastGroup = groupInfos.get(i);
            }
        } catch (Exception e) {
        }
        return validWordCache;
    }

    /**
     * 判断当前联系人首字母是否需要加入到索引列表
     *
     * @param group
     * @param lastGroup
     * @return
     */
    private boolean needToAddCache(GroupInfo group, GroupInfo lastGroup) {
        if (lastGroup == null) {
            return true;
        }
        if (!PinYin.buildPinYinDuoYinXing(group.getPerson()).getIndexKey().equalsIgnoreCase(PinYin.buildPinYinDuoYinXing(lastGroup.getPerson()).getIndexKey())) {
            return true;
        }
        return false;
    }

    /**
     * 获取LoaderManager
     * @return
     */
    @Override
    public LoaderManager getLoaderManger() {
        return getLoaderManager();
    }

    @Override
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
        if(cmd == SOURCE_SHARE_MY_CARD ) {
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
     * 显示收缩结果
     * @param groupInfos
     * @param key
     */
    @Override
    public void showSearchResult(ArrayList<GroupInfo> groupInfos, CharSequence key) {

    }

    /**
     *
     * @param presenter
     */
    public void setPresenter(GroupChatListPresenter presenter){
        this.mPresenter = presenter ;
    }

    /**
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.creategroup){ // 群列表为空，点击创建按钮
            //            Intent intent = ContactsSelectActivity.createIntentForCreateGroupNoChoose(this);
            Intent intent = ContactSelectorActivity.creatIntent(getActivity(), SOURCE_NEW_GROUP_NO_GROUP, 500);
            startActivity(intent);
        }
    }


    /**
     * 群列表数据加载完毕，显示字母索引
     */
    private void refreshIndexBar() {

        ArrayList<String> validWordCache = getValidWordCache();
        if (validWordCache != null && validWordCache.size() > 0 && mIndexBarView != null) {
            mIndexBarView.setVisibility(View.VISIBLE);
            mIndexBarView.refreshIndexBar(validWordCache);
            mIndexBarView.setStarVisible(false);

            int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
            List<GroupInfo> data = mAdapter.getData();
            if (firstVisibleItem >= 0 && data != null && firstVisibleItem < data.size()) {
                GroupInfo groupInfo = data.get(firstVisibleItem);
                if (groupInfo != null) {
                    PinYin pinYin = PinYin.buildPinYinDuoYinXing(groupInfo.getPerson());
                    String indexKey = pinYin.getIndexKey();
                    if ( indexKey != null) {
                        mIndexBarView.setIndexWordPosition(indexKey.toUpperCase());
                    }
                }
            }
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (pinYinHashMap != null) {
            pinYinHashMap.clear();
            pinYinHashMap = null;
        }
        if (characterHashMap != null) {
            characterHashMap.clear();
            characterHashMap = null;
        }
    }

    /**
     *
     * @return
     */
    public ArrayList<String> getValidWordCache() {
        return mValidWordCache;
    }

    /**
     *
     * @param word
     * @return
     */
    public boolean setSelectionByWord(String word) {
        List<GroupInfo> data = mAdapter.getData();
        if (data != null) {
            GroupInfo groupInfo;
            String indexKey;
            for (int i = 0, len = data.size(); i < len; i++) {
                groupInfo = data.get(i);
                PinYin pinYin = PinYin.buildPinYinDuoYinXing(groupInfo.getPerson());
                indexKey = pinYin.getIndexKey();
                if (indexKey != null && indexKey.equalsIgnoreCase(word)) {
                    mLayoutManager.scrollToPositionWithOffset(i, 0);
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * 控件mExpIndexView气泡的距离顶部的偏差高度
     * @param mExpIndexViewHeight
     */
    public void setmExpIndexViewHeight(int mExpIndexViewHeight) {
        this.mExpIndexViewHeight = mExpIndexViewHeight;
    }


    /**
     * 显示分享名片的提示框（公司，职位，邮箱）
     * @param number
     * @param isGroup
     */
    private void showVCardDialogByContactDetail(final String number, final boolean isGroup) {
        final Employee employee = (Employee) getArguments().getSerializable(BUNDLE_KEY_SIMPLE_CONTACT);
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
            if(cmd == MessageModuleConst.GroupChatListMergaActivityConst.GROUP_CHAT_LIST||cmd == SOURCE_START_OR_NEW_GROUP){ // 1 通讯录 - 群聊
                BuryingPointUtils.messageEntrybPuryingPoint(mContext ,"通讯录-群聊");
                UmengUtil.buryPoint(getContext(), "contacts_message_groupmessage", "通讯录-群聊-进入群聊", 0);
                mPresenter.openItem(mActivity, groupInfo);
                //首页发起群聊，选择一个群，需要关闭前面的页面
                if(cmd == SOURCE_START_OR_NEW_GROUP){
                    if(mActivity != null){
                        mActivity.setResult(Activity.RESULT_OK);
                        mActivity.finish();
                    }
                }
                return;
            }else if (cmd == SOURCE_MESSAGE_FORWARD) { // 2 来自消息转发
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
                                       // CallProxy.g.getUiInterface().goToVoiceCallActivity(mContext);
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
                            intent.putExtra(ACTIVITY_RESULT_FROM, ContactSelectorActivity.SHARE_QRCODE_TO_A_GROUP_REQUEST_CODE);
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
            }else if(cmd == SOURCE_SHARE_MY_CARD){//15 分享本人名片到聊天
                mPresenter.handleCardToChat(groupInfo.getAddress(), AboutMeProxy.g.getServiceInterface().getPersonalRawContact(getActivity()));
                return;
            }else if(cmd == SOURCE_SHARE_CARD_CONTACT_DETAIL_PAGE){ //23 从联系人资料页面分享名片
                showVCardDialogByContactDetail(groupInfo.getAddress(), true);
                return;
            }/* else if (cmd == ContactsSelectActivity.CARD_VOUCHER_SHARE) {//35 卡券共享
                RedpagerProxy.g.getUiInterface().selectGroupCallback(groupInfo.getAddress());
                Intent intent = new Intent();
                if(mActivity != null){
                    mActivity.setResult(Activity.RESULT_OK, intent);
                    mActivity.finish();
                }
                return;
            }*/
        }
        mPresenter.openItem(mActivity, groupInfo);
    }
}
