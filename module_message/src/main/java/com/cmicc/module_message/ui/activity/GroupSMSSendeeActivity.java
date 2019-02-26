package com.cmicc.module_message.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.chinamobile.app.yuliao_business.model.BaseModel;
import com.chinamobile.app.yuliao_business.model.GroupMember;
import com.chinamobile.app.yuliao_business.provider.Conversations;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_common.utils.PinyinUtils;
import com.chinamobile.app.yuliao_contact.model.AdvancedSearchContact;
import com.chinamobile.app.yuliao_contact.model.PinYin;
import com.chinamobile.app.yuliao_contact.model.SimpleContact;
import com.chinamobile.app.yuliao_core.util.NumberUtils;
import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmicc.module_message.ui.adapter.GroupSMSSendeeAdapter;
import com.cmicc.module_message.ui.dialogs.GroupSmsMaxDialog;
import com.cmcc.cmrcs.android.ui.utils.UmengUtil;
import com.cmcc.cmrcs.android.ui.utils.WrapContentLinearLayoutManager;
import com.cmcc.cmrcs.android.ui.view.contactlist.ContactCustomSearchBar;
import com.cmcc.cmrcs.android.ui.view.contactlist.ExpIndexView;
import com.cmcc.cmrcs.android.ui.view.contactlist.IndexBarView;
import com.cmicc.module_message.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * Created by LY on 2018/6/22.
 * 群短信发送界面
 */

public class GroupSMSSendeeActivity extends BaseActivity implements GroupSMSSendeeAdapter.OnItemClick ,
        View.OnClickListener , LoaderManager.LoaderCallbacks<Cursor> ,IndexBarView.OnIndexTouchListener {

    private String TAG = "GroupSMSSendeeActivity";
    public static String GROUPID = "GROUPID";

    private RelativeLayout back_rl ;
    private TextView title_text ;
    private TextView sure_text ;
    private ContactCustomSearchBar search ;
    private GroupSMSSendeeAdapter mGroupSMSSendeeAdapter ;
    private RecyclerView mRecyclerView ;
    private CheckBox checkBox ;
    private View allSelectView ;
    private IndexBarView mIndexBarView ;
    private ExpIndexView mExpIndexView ;
    private  TextView mNoResultsStipTv ;

    private String groupID ;
    private ArrayList<GroupSmsSendee> datas = new ArrayList<>();
    private ArrayList<String> hpones ;
    private ArrayList<GroupSmsSendee> initSeleDatas = new ArrayList<>();
    private HashMap<String  , Integer> letterIndex = new HashMap<>();
    private boolean isSearchStatus ;  // 是否为搜索状态

    private String tvSureText = "";
    private int MAXNUMBEROFPEOPLE = 200 ; // 可以选择的最大人数
    private boolean isHideStar = false;
    private ArrayList<GroupSmsSendee> selectData = new ArrayList<>() ;

    private GroupSmsMaxDialog maxDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activtiy_group_sms_sendee_layout);
    }

    @Override
    protected void findViews() {
        back_rl = findViewById(R.id.left_back); // 返回
        title_text = findViewById(R.id.title_text);  // title
        sure_text =  findViewById(R.id.sure_text);   // 确认按钮
        search =  findViewById(R.id.layout_search); // 搜索栏
        mRecyclerView = findViewById(R.id.recyclerView_recently_person);
        checkBox = findViewById(R.id.contact_check_all);
        allSelectView = findViewById(R.id.layout_allcheck_contactlist);
        mIndexBarView = findViewById(R.id.contact_index_bar_view);
        mExpIndexView = findViewById(R.id.contact_exp_index_view);
        mNoResultsStipTv = findViewById(R.id.no_results_stip_tv);
        mIndexBarView.setExpIndexView(mExpIndexView);
        mIndexBarView.setIndexWordHeightLight(true);
        mRecyclerView.setLayoutManager(new WrapContentLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        back_rl.setOnClickListener(this); // 返回
        sure_text.setOnClickListener(this); // 确定
        allSelectView.setOnClickListener(this); // 全选
        mIndexBarView.setOnIndexTouchListener(this); // 字母索引
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                if (layoutManager instanceof LinearLayoutManager) {
                    LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;
                    //获取第一个可见view的位置
                    int firstItemPosition = linearManager.findFirstVisibleItemPosition();
                    LogF.d( TAG , "firstItemPosition = "+ firstItemPosition);
                    if(mGroupSMSSendeeAdapter.getItem(firstItemPosition)!= null &&
                            !TextUtils.isEmpty(mGroupSMSSendeeAdapter.getItem(firstItemPosition).getLetter())){
                        mIndexBarView.setIndexWordPosition(mGroupSMSSendeeAdapter.getItem(firstItemPosition).getLetter().toUpperCase());
                    }
                }
            }
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        search.setHint(getResources().getString(R.string.search_member));
        search.setDataSet( new ArrayList<ContactCustomSearchBar.CustomContact>());
        search.setOnAvatarItemClickListener(new ContactCustomSearchBar.OnAvatarItemClickListener() {
            @Override
            public void onAvatarItemClick(final ContactCustomSearchBar.CustomContact toRemoveEmployee) {
                mGroupSMSSendeeAdapter.moverSelectSendee(toRemoveEmployee.getCustomContactPhone()); // 原来选中的对象移除
                new Thread(new Runnable() { // 原来选中的对象选中状态改变
                    @Override
                    public void run() {
                        Iterator<GroupSmsSendee>  iterator = datas.iterator();
                        while (iterator.hasNext()){
                            GroupSmsSendee groupSmsSendee = iterator.next() ;
                            if(groupSmsSendee.getCustomContactPhone().contains(toRemoveEmployee.getCustomContactPhone()) && groupSmsSendee.isChoice()){
                                groupSmsSendee.setChoice(false);
                                GroupSMSSendeeActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mGroupSMSSendeeAdapter.setDatas(datas);
                                    }
                                });
                                break;
                            }
                        }
                    }
                }).start();
            }
        });
    }

    @Override
    protected void init() {
        tvSureText = getResources().getString(R.string.sure);
        if(getIntent()!=null){
            groupID = getIntent().getStringExtra(GROUPID); // 群ID
            hpones = getIntent().getStringArrayListExtra(GroupSMSEditActivity.PHONES); // 处于选择中的人
            MAXNUMBEROFPEOPLE = getIntent().getIntExtra(GroupSMSEditActivity.MAXNUMBEROFPEOPLE , 200); // 默认两百
        }
        getSupportLoaderManager().initLoader(new Random(System.currentTimeMillis()).nextInt(),null,this); // 加载数据
        mGroupSMSSendeeAdapter = new GroupSMSSendeeAdapter(this);
        mGroupSMSSendeeAdapter.setOnItemClick( this );
        mRecyclerView.setAdapter(mGroupSMSSendeeAdapter);
        mGroupSMSSendeeAdapter.setMAXNUMBEROFPEOPLE(MAXNUMBEROFPEOPLE); // 设置最大的可选人数
        initDialog() ;
    }

    /**
     *
     */
    private void showIndexBarView(ArrayList<String> as){
        mIndexBarView.refreshIndexBar(as);
        hideStar(); //字母索引不显示星号
        mIndexBarView.setVisibility(View.VISIBLE);
        search.addTextChangedListener(new TextWatcher() { // 搜索
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if(TextUtils.isEmpty(s.toString())){ // 非搜索状态
                    isSearchStatus = false ;
                    allSelectView.setVisibility(View.VISIBLE);
                    mIndexBarView.setVisibility(View.VISIBLE);
                    mGroupSMSSendeeAdapter.setSearchStats(false , "");
                }else{                               // 搜索状态
                    isSearchStatus = true ;
                    allSelectView.setVisibility(View.GONE);
                    mIndexBarView.setVisibility(View.GONE);
                    mGroupSMSSendeeAdapter.setSearchStats(true ,s.toString());
                }
                ArrayList<GroupSmsSendee> sears = searchContactAdvancedAsync(s.toString(), datas , 0 , datas.size() ); // 在总数据源中，根据找出符合key的搜索的结果集
                mGroupSMSSendeeAdapter.setDatas(sears);
                if( isSearchStatus && ( sears == null || sears.size() == 0 ) ){ // 搜索状态
                    mNoResultsStipTv.setVisibility(View.VISIBLE);
                }else{
                    mNoResultsStipTv.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * 初始话对话开框
     */
    private void initDialog(){
        maxDialog = new GroupSmsMaxDialog(this);
        maxDialog.setStipText(String.format(getResources().getString(R.string.single_maximum_number) , MAXNUMBEROFPEOPLE));
        maxDialog.getiKoneText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                maxDialog.dismiss();
            }
        });
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        LogF.d(TAG,"mGroupChatId--:"+groupID);
        String where = "group_id = ? ";
        CursorLoader loader = new CursorLoader(mContext, Conversations.GroupMember.CONTENT_URI,
                new String[] { BaseModel.COLUMN_NAME_GROUP_ID,BaseModel.COLUMN_NAME_ADDRESS
                        ,BaseModel.COLUMN_NAME_PERSON,BaseModel.COLUMN_NAME_TYPE,BaseModel.COLUMN_NAME_STATUS},
                where, new String[]{groupID}, null );
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        LogF.d(TAG,"onLoadFinished--:"+data);
        if(data == null ){
            return;
        }
        LogF.d(TAG,"onLoadFinished-- count :"+data.getCount());
        analysisCursor(data); // 解析数据
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        LogF.d(TAG,"onLoaderReset--:"+loader);
    }

    /**
     * 解析Curosr数据
     * @param data
     */
    private void analysisCursor(final Cursor data){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (data.moveToNext()) {
                    GroupSmsSendee groupSmsSendee= new GroupSmsSendee();
                    GroupMember groupMember = new GroupMember();
                    String address = data.getString(data.getColumnIndex(BaseModel.COLUMN_NAME_ADDRESS));
                    groupMember.setAddress(address);
                    groupSmsSendee.setNumber(address);
                    String person = data.getString(data.getColumnIndex(BaseModel.COLUMN_NAME_PERSON)) ;
                    int type = data.getInt(data.getColumnIndex(BaseModel.COLUMN_NAME_TYPE)) ;
                    groupMember.setType(type);
                    int status = data.getInt(data.getColumnIndex(BaseModel.COLUMN_NAME_STATUS)) ;
                    groupMember.setStatus(status);
                    groupSmsSendee.setRawId(1); // 显示头像和名字 为0显示手机号码（搜索栏）
                    groupSmsSendee.setGroupMember(groupMember);
                    if(!TextUtils.isEmpty(person)){
                        groupMember.setPerson(person);
                        groupSmsSendee.setName(person);
                        groupSmsSendee.setPinyin(PinYin.buildPinYin(person));
                        String pinyin = PinyinUtils.getInstance(mContext).getPinyin(person);
                        if ( !"".equals(pinyin) ) {
                            String sortString = pinyin.substring(0, 1).toUpperCase();
                            if (sortString.matches("[A-Z]")) {
                                groupSmsSendee.setLetter(sortString);
                            } else {
                                groupSmsSendee.setLetter("#");
                            }
                        }
                    }else{
                        groupMember.setPerson(NumberUtils.toHideAsStar(NumberUtils.getNumForStore(address)));
                        groupSmsSendee.setName(NumberUtils.toHideAsStar(NumberUtils.getNumForStore(address)));
                        groupSmsSendee.setPinyin(PinYin.buildPinYin(NumberUtils.getNumForStore(address)));
                        groupSmsSendee.setLetter("#");
                    }
                    if(hpones != null &&  hpones.contains(address)){ // 一开始就处于选中的状态
                        groupSmsSendee.setChoice(true);
                        initSeleDatas.add(groupSmsSendee);
                    }
                    LogF.d(TAG,"onLoadFinished--: address = "+address + "  person = "+ person + "  type = "+ type + "  status = "+ status );
                    datas.add(groupSmsSendee);
                }
                data.close();
                if(mGroupSMSSendeeAdapter!=null){
                    Collections.sort(datas , pinyinComparator);
                    GroupSMSSendeeActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mGroupSMSSendeeAdapter.setDatas(datas); // 适配的数据源
                            mGroupSMSSendeeAdapter.setSelectionDatas(initSeleDatas); // 一开始就选择状态
                            if(datas.size()>0) {
                                mIndexBarView.setIndexWordPosition(datas.get(0).getLetter().toUpperCase()); // 字母索引显示在第一个Item
                            }
                            findIndex();   // 字母索引与 Item  index 对应
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * 字母索引不显示星号
     */
    private void hideStar() {
        mIndexBarView.setStarVisible(false);
        isHideStar = true;
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int id = v.getId() ;
        if(id == R.id.left_back){
            finish();
        }else if( id == R.id.layout_allcheck_contactlist ){ //  全选按钮
            allSelectView.setClickable(false);
            if(checkBox.isChecked()){
                allSelect(false ); // 取消全选
            }else{
                allSelect(true );  // 全选
            }
        }else if(id == R.id.sure_text){  // 确定按钮
            UmengUtil.buryPoint(mContext,"groupmessage_sms_add_done","消息-群聊-加号-群短信-选择成员-确定",0);
            if(mGroupSMSSendeeAdapter.getSelectionDatas().size() <= 0  ){
                return;
            }
            processingData(mGroupSMSSendeeAdapter.getSelectionDatas());
        }
    }

    /**
     * 全选和取消全选
     * @param isAll
     */
    private void allSelect(final boolean isAll ){
        new Thread(new Runnable() {
            @Override
            public void run() {
                selectData.clear();
                if(isAll){  // 全选
                    selectData.addAll(mGroupSMSSendeeAdapter.getSelectionDatas()); // 保存原来选中的
                    for(int i = 0 ; i < datas.size() ; i++ ){
                        if( selectData.size() < MAXNUMBEROFPEOPLE ){ // 加这个，效率高一些
                            if(!datas.get(i).isChoice()){
                                datas.get(i).setChoice(isAll);
                                selectData.add(datas.get(i));  // 添加后面可以选中的
                            }
                        }else{
                            break;
                        }
                    }
                }else{ // 取消全选
                    for(int i = 0 ; i < datas.size() ; i++ ){
                        datas.get(i).setChoice(isAll);
                    }
                }
                GroupSMSSendeeActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mGroupSMSSendeeAdapter.setDatas(datas);
                        mGroupSMSSendeeAdapter.setSelectionDatas(selectData);
                        if(isAll){ // 全选
                            if(selectData.size()<datas.size()){  // 表示不能全选 数据源数据总数大于最大可选人数
                                if(!maxDialog.isShowing()){// 弹框显示
                                    maxDialog.show();
                                }
                                checkBox.setChecked(!isAll);
                            }else{
                                checkBox.setChecked(isAll);
                            }
                        }else{
                            checkBox.setChecked(isAll); // 全选按钮
                        }
                        allSelectView.setClickable(true);
                    }
                });
            }
        }).start();
    }


    /**
     * 处理数据（返回数据）
     */
    private void processingData(ArrayList<GroupSmsSendee> smsSendees){
        StringBuilder  nameSB = new StringBuilder();
        ArrayList<String> phones = new ArrayList<>();
        for(int i = 0 ; i< smsSendees.size() ; i++ ){
            String name = smsSendees.get(i).getGroupMember().getPerson();
            String phone = smsSendees.get(i).getGroupMember().getAddress();
            if(!TextUtils.isEmpty(phone) && !TextUtils.isEmpty(name)){
                nameSB.append(name);
                if(i!=smsSendees.size()-1){
                    nameSB.append("、");
                }
                phones.add(phone);
            }
        }
        Intent intent = new Intent();
        intent.putExtra( GroupSMSEditActivity.NAMES, nameSB.toString());
        intent.putStringArrayListExtra( GroupSMSEditActivity.PHONES, phones);
        setResult(GroupSMSEditActivity.requestCode , intent);
        finish();
    }

    /**
     * Item 点击
     * @param count
     */
    @Override
    public void onItemClick(int count) {
        if(count == 0 ){ // 没有选择
            sure_text.setClickable(false);
            sure_text.setText(getResources().getString(R.string.sure));
            sure_text.setTextColor(getResources().getColor(R.color.color_d5d5d5));
            checkBox.setChecked(false);
        }else{
            sure_text.setClickable(true);
            sure_text.setTextColor(getResources().getColor(R.color.color_0D6CF9));
            String text = tvSureText + String.format("(%d/%d)",count,MAXNUMBEROFPEOPLE);
            sure_text.setText(text);
            if(count == datas.size()){ // 全选
                checkBox.setChecked(true);
            }else{
                checkBox.setChecked(false);
            }
        }
        search.setDataSet(mGroupSMSSendeeAdapter.getSelectionDatas()); // 设置搜索栏的数据
        search.getListView().setSelection(mGroupSMSSendeeAdapter.getSelectionDatas().size()-1); //滑动到最后
    }

    /**
     * 达到最大选择人数
     * @param maximumNumber
     */
    @Override
    public void maximumNumber(int maximumNumber) {
        if(!maxDialog.isShowing()){
            maxDialog.show();
        }
        //Toast.makeText(this , getResources().getString(R.string.sendee_maximum_number) , Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onIndex(String word) {

    }

    @Override
    public void onIndex(String word, int[] location) {
        setSelection(word);
        int[] parentLocation = new int[2];
        //getLocationOnScreen(parentLocation);
        mExpIndexView.setTopOffset(location[1] - parentLocation[1] );
        mExpIndexView.show(word);
    }

    private static int getStatusBarHeight(Context context) {
        int result = 0;
        int resId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) {
            result = context.getResources().getDimensionPixelOffset(resId);
        }
        return result;
    }


    /**
     * 滚动到指定位置
     * @param word 字母
     */
    private void setSelection(String word) {
        Integer position = null;
        position = letterIndex.get(word);
        if(position != null ){
            moveToPosition((LinearLayoutManager) mRecyclerView.getLayoutManager(),  mRecyclerView, position );
            mIndexBarView.setIndexWordPosition(word);
        }
    }

    public void moveToPosition(LinearLayoutManager manager, RecyclerView mRecyclerView, int n) {
        int firstItem = manager.findFirstVisibleItemPosition();
        int lastItem = manager.findLastVisibleItemPosition();
        if (n <= firstItem) {
            mRecyclerView.scrollToPosition(n);
        } else if (n <= lastItem) {
            int top = mRecyclerView.getChildAt(n - firstItem).getTop();
            mRecyclerView.scrollBy(0, top);
        } else {
            mRecyclerView.scrollToPosition(n);
        }

    }


    /**
     * 字母索引 与 item 的 下标 对应
     */
    private void findIndex(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ArrayList<String> as = new ArrayList<>();
                for(int i = 0 ; i <datas.size() ; i++ ){
                    if(i == 0){
                        letterIndex.put(datas.get(i).getLetter().toUpperCase(), 0);
                        as.add(datas.get(i).getLetter().toUpperCase());
                    }else{
                        if(!datas.get(i-1).getLetter().equals(datas.get(i).getLetter())){
                            letterIndex.put(datas.get(i).getLetter().toUpperCase(), i);
                            as.add(datas.get(i).getLetter().toUpperCase());
                        }
                    }
                }
                GroupSMSSendeeActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showIndexBarView(as);
                    }
                });
            }
        }).start();
    }


    /**
     * 处理搜索时的数据
     * @param keyWord
     * @param sourceList
     * @param pos
     * @param size
     * @return
     */
    private  ArrayList<GroupSmsSendee> searchContactAdvancedAsync(String keyWord, ArrayList<GroupSmsSendee> sourceList, int pos, int size) {
        long time = System.currentTimeMillis();
        ArrayList<GroupSmsSendee> allResults = new ArrayList<>();
        if (sourceList == null){
            return allResults;
        }
        if(!TextUtils.isEmpty(keyWord)){
            for(int i=pos; i < pos + size; i++){
                GroupSmsSendee contact = sourceList.get(i);
                if (contact != null) {
                    String snipToShow = "";
                    //snip高级搜索，如:号码
                    if(contact.getGroupMember().getAddress().indexOf(keyWord) != -1){
                        snipToShow = contact.getGroupMember().getAddress();
                    }
                    contact.resetMatchedIndex();
                    int nameMatchIndex = contact.getGroupMember().getPerson().indexOf(keyWord);
                    String pattern = keyWord.toLowerCase();
                    //获取名字拼音首字母拼接字符串的列表，因为可能存在多音字，所以normalPinyinFirstList的大小可能大于1.
                    List<String> normalPinyinFirstList = PinYin.buildPinYin(contact.getGroupMember().getPerson()).getNormalPinyinFirstList();
                    String normalPinyin = null;
                    int indexSP;
                    if (normalPinyinFirstList != null && normalPinyinFirstList.size() > 0 ) {
                        // search by first pinyin
                        indexSP = 0;
                        for (String strTmp : normalPinyinFirstList) {
                            if (strTmp.contains(pattern)) {//当首字母拼接字符串含有搜索关键字
                                normalPinyin = strTmp;
                                contact.setSimplePinyinMatchIndex(indexSP);//记录用的是哪个首字母拼接字符串
                                break;
                            }
                            indexSP += 1;
                        }
                    }
                    if (normalPinyin != null) {//keyword为首字母
                        List nameMatchId = new ArrayList();//记录keyword每个字母的对应起始位置
                        List first = PinYin.buildPinYin(contact.getGroupMember().getPerson()).getFirstMatchId();//记录contact每个汉字拼音首字母的起始位置
                        if (first != null) {
                            int vIndex = normalPinyin.indexOf(pattern);//keyword在contact首字母拼接字符串的起始位置
                            contact.setStartIndex(vIndex);
                            for (int j = 0; j < pattern.length(); j++) {
                                nameMatchId.add(first.get(vIndex));
                                contact.firstCharactorCounts += 1;
                                vIndex++;
                            }
                        }
                        contact.setPinyinMatchId(nameMatchId);
                        contact.setFirstMatch(true);
                        calWightLight(contact);
                        contact.setSearchType(AdvancedSearchContact.SEARCH_TYPE_PINYIN);
                        allResults.add(contact);
                    } else if (match(contact, pattern, false)) { //全拼音匹配
                        // search by pinyin
                        calWightLight(contact);
                        contact.setSearchType(AdvancedSearchContact.SEARCH_TYPE_PINYIN);
                        allResults.add(contact);
                    } else if (nameMatchIndex != -1) { //名字匹配
                        // search by original name
                        contact.setNameMatchId(nameMatchIndex);
                        contact.setSearchType(AdvancedSearchContact.SEARCH_TYPE_NAME);
                        allResults.add(contact);
                    } else if (!TextUtils.isEmpty(snipToShow)) {//其他资料匹配
                        // 屏蔽掉号码搜索
                        //search by nickname or job or company or email or note
//                        AdvancedSearchContact contactSearch = new AdvancedSearchContact();
//                        contactSearch.fill(contact);
//                        contactSearch.setSnip(snipToShow);
//                        contactSearch.setNumber(snipToShow);
//                        contact.setSearchType(AdvancedSearchContact.SEARCH_TYPE_OTHER);
//                        ArrayList<PhoneKind> phones = new ArrayList<>();
//                        phones.add( new PhoneKind());
//                        contact.setAddressList(phones); // 目的是使得手机号码搜索有效 adpter 中用扫搜索显示
//                        allResults.add(contact);
                    }
                }
            }
        }else{
            allResults.addAll(datas);
        }
        return allResults;
    }

    private void calWightLight(GroupSmsSendee item) {
        List nameMatchId = item.getPinyinMatchId();
        StringTokenizer reorder_str = new StringTokenizer(PinYin.buildPinYin(item.getGroupMember().getPerson()).getNormalPinyin());
        int live_len = 0;
        int loc = 0;
        item.cleanWeightLight();
        while (reorder_str.hasMoreTokens()) {
            int str_len = reorder_str.nextToken().length();
            live_len += str_len;
            for (Iterator i$ = nameMatchId.iterator(); i$.hasNext();) {
                int matchid = ((Integer) i$.next()).intValue();
                if ((matchid >= live_len - str_len) && (matchid < live_len)) {
                    item.appendWeightLight(loc);
                    break;
                }
            }
            live_len++;
            loc++;
        }
    }

    private boolean match(GroupSmsSendee item, String pattern, boolean useT9) {
        StringTokenizer st = null;
        if (useT9)
            st = new StringTokenizer(PinYin.buildPinYin(item.getGroupMember().getPerson()).getT9Pinyin());
        else
            st = new StringTokenizer(PinYin.buildPinYin(item.getGroupMember().getPerson()).getNormalPinyin());
        int count = 1;
        if (useT9)
            count = st.countTokens();
        int i = 0;
        boolean match = false;
        while ((i < count) && (!match)) {
            match = matchsub(item, pattern, useT9, i);
            i++;
        }
        return match;
    }

    private boolean matchsub(GroupSmsSendee item, String pattern, boolean useT9, int tokenbegin) {
        item.firstCharactorCounts = 0;
        StringTokenizer st = null;
        if (useT9){
            st = new StringTokenizer(PinYin.buildPinYin(item.getGroupMember().getPerson()).getT9Pinyin());
        } else{
            st = new StringTokenizer(PinYin.buildPinYin(item.getGroupMember().getPerson()).getNormalPinyin());
        }
        StringBuffer sbuf = new StringBuffer();
        int m = pattern.length();
        int i = 0;
        int l = 0;
        int j = 0;
        int tmpIndex = 0;
        List nameMatchId = new ArrayList();
        int tokenindex = 0;
        int tokenLen = 0;
        while (st.hasMoreTokens()) {
            if (tokenindex < tokenbegin) {
                tokenindex++;
                String next = st.nextToken();
                tokenLen += next.length() + 1;
                continue;
            }
            tokenindex++;
            i = 0;
            String next = st.nextToken();
            if (j >= m) {
                break;
            }
            if ((next.contains("_")) && (nameMatchId.size() < m)) {
                String[] aa = Pattern.compile("_").split(next);
                String[] rTmp = new String[aa.length];
                ArrayList eq = new ArrayList(aa.length);
                int[] mFirst = new int[aa.length];
                int[] removeCount = new int[aa.length];
                int[] lenCount = new int[aa.length];
                int tmplen = l;
                int index = tmpIndex;
                for (int k = 0; k < aa.length; k++) {
                    boolean isFirstMatch = false;
                    String tmps = aa[k];
                    int wanttoremoveid = 0;
                    index = tmpIndex;
                    i = 0;
                    int n = tmps.length();
                    int cpare = 0;
                    sbuf.setLength(0);
                    boolean match = true;
                    while ((i < n) && (index < m)) {
                        if ((tmps.charAt(i) == pattern.charAt(index)) && (match)) {
                            if (i == 0)
                                isFirstMatch = true;
                            if (!isFirstMatch) {
                                break;
                            }
                            sbuf.append(tmps.charAt(i));
                            index++;
                            cpare++;
                        } else {
                            match = false;
                            if (i != 0) {
                                break;
                            }
                            int t = tmps.indexOf(pattern.charAt(index));
                            if ((t == -1) || (t == 0))
                                break;
                            String next_tmp = tmps.substring(0, t + 1);
                            if ((t > index) || (!pattern.substring(index - t, index + 1).equals(next_tmp))) {
                                break;
                            }
                            wanttoremoveid = t;

                            match = true;
                            i--;
                            index -= t;
                        }
                        if (!match)
                            break;
                        i++;
                    }
                    if (isFirstMatch) {
                        mFirst[k] = 1;
                        lenCount[k] = tmplen;
                        removeCount[k] = wanttoremoveid;
                        eq.add(Integer.valueOf(cpare));
                    } else {
                        eq.add(Integer.valueOf(0));
                        mFirst[k] = 0;
                    }
                    tmplen += aa[k].length() + 1;
                    rTmp[k] = sbuf.toString();
                }
                CompareMax value = getMaxIndex(eq, mFirst);
                if (value.maxValue > 0) {
                    j = tmpIndex + value.maxValue - removeCount[value.index];
                    tmpIndex = j;
                    String v = rTmp[value.index];
                    int idx = -1;
                    int len = lenCount[value.index];
                    if (len >= 0) {
                        for (int removeid = 0; removeid < removeCount[value.index]; removeid++) {
                            nameMatchId.remove(nameMatchId.size() - 1);
                        }
                        for (int s = 0; s < value.maxValue; s++) {
                            nameMatchId.add(Integer.valueOf(len++));
                        }
                        tmpIndex = j;
                    }
                    if (value.isFirstCharactor) {
                        item.firstCharactorCounts += 1;
                    }
                }
                l += next.length() + 1;
                value = null;
            } else {
                int n = next.length();
                boolean match = true;
                boolean isFirstMatch = false;
                while ((i < n) && (j < m)) {
                    if ((next.charAt(i) == pattern.charAt(j)) && (match)) {
                        if (i == 0) {
                            item.firstCharactorCounts += 1;
                            isFirstMatch = true;
                        }
                        if (!isFirstMatch)
                            break;
                        nameMatchId.add(Integer.valueOf(l));
                        j++;
                        tmpIndex = j;
                    } else {
                        match = false;
                        if (i == 0) {
                            int t = next.indexOf(pattern.charAt(j));
                            if ((t != -1) && (t != 0)) {
                                String next_tmp = next.substring(0, t + 1);
                                if ((t <= j) && (pattern.substring(j - t, j + 1).equals(next_tmp))) {
                                    match = true;

                                    for (int removeid = j - 1; removeid >= j - t;) {
                                        nameMatchId.remove(nameMatchId.size() - 1);
                                        removeid--;
                                    }
                                    i--;
                                    l--;
                                    j -= t;
                                }
                            }
                        }
                    }
                    if (!match) {
                        l += next.length() - i;
                        break;
                    }
                    i++;
                    l++;
                }
                l++;
            }
        }
        boolean match = nameMatchId.size() == m;
        if (match) {
            if (tokenLen > 0) {
                for (i = 0; i < nameMatchId.size(); i++) {
                    int value = ((Integer) nameMatchId.get(i)).intValue() + tokenLen;
                    nameMatchId.set(i, Integer.valueOf(value));
                }
            }
            item.setPinyinMatchId(nameMatchId);
        } else {
            nameMatchId.clear();
        }
        return match;
    }

    public  CompareMax getMaxIndex(ArrayList<Integer> list, int[] mFirst) {
        int nRet = 0;
        int index = 0;
        boolean r = false;
        for (int i = 0; i < list.size(); i++) {
            if (((Integer) list.get(i)).intValue() > nRet) {
                nRet = ((Integer) list.get(i)).intValue();
                index = i;
                r = mFirst[i] != 0;
            }
        }
        return new CompareMax(index, nRet, r);
    }

    public class CompareMax {
        public int index = 0;
        public int maxValue = 0;
        public boolean isFirstCharactor = false;

        public CompareMax(int index, int maxValue, boolean isFirstCharactor) {
            this.index = index;
            this.maxValue = maxValue;
            this.isFirstCharactor = isFirstCharactor;
        }
    }


    /**
     * 比较排序
     */
    GroupInfoPinyinComparator pinyinComparator = new GroupInfoPinyinComparator();
    class GroupInfoPinyinComparator implements Comparator<GroupSmsSendee> {
        public int compare(GroupSmsSendee o1, GroupSmsSendee o2) {
            if (o1.getLetter().equals("@") || o2.getLetter().equals("#")) {
                return -1;
            } else if (o1.getLetter().equals("#")
                    || o2.getLetter().equals("@")) {
                return 1;
            } else {
                return o1.getLetter().compareTo(o2.getLetter());
            }
        }
    }

    /**
     * 暂时用内部类在做
     */
    public class GroupSmsSendee extends SimpleContact{

        private GroupMember groupMember;
        private boolean isChoice;
        private String letter ;

        public GroupMember getGroupMember() {
            return groupMember;
        }

        public void setGroupMember(GroupMember groupMember) {
            this.groupMember = groupMember;
        }

        public boolean isChoice() {
            return isChoice;
        }

        public void setChoice(boolean choice) {
            isChoice = choice;
        }

        public String getLetter() {
            return letter;
        }

        public void setLetter(String letter) {
            this.letter = letter;
        }
    }
}
