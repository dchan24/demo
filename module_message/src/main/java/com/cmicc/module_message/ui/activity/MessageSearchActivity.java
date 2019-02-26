package com.cmicc.module_message.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_common.utils.statusbar.StatusBarCompat;
import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmcc.cmrcs.android.ui.adapter.BaseCustomCursorAdapter;
import com.cmicc.module_message.ui.adapter.MessageSearchAdapter;
import com.cmicc.module_message.ui.constract.MessageSearchContract;
import com.cmicc.module_message.ui.presenter.MessageSearchPresenter;
import com.cmcc.cmrcs.android.ui.utils.UmengUtil;
import com.cmcc.cmrcs.android.ui.utils.WrapContentLinearLayoutManager;
import com.cmcc.cmrcs.android.widget.SearchToolbar;
import com.cmicc.module_message.R;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by situ on 2017/3/30.
 * 单个对话查找聊天记录
 */

public class MessageSearchActivity extends BaseActivity implements MessageSearchContract.IView, BaseCustomCursorAdapter.OnRecyclerViewItemClickListener,View.OnTouchListener {

    RecyclerView mResultListView;
    View mEmptyView;
    TextView mTextHint;
    SearchToolbar mToolbar;

    public static final String BUNDLE_KEY_ADDRESS = "address";
    public static final String BUNDLE_KEY_KEYWORD = "keyword";
    public static final String BUNDLE_KEY_COUNT = "count";
    public static final String BUNDLE_KEY_TITLE = "title";
    public static final String BUNDLE_KEY_BOXTYPE = "boxtype";

    private MessageSearchAdapter mAdapter;
    private MessageSearchPresenter mPresenter;

    private View mLayoutFile;
    private View mLayoutVideoImg;
    private View mTvContact;
    private int mBoxType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_search);
    }

    @Override
    protected void findViews() {
        mResultListView = (RecyclerView)findViewById(R.id.result_list);
        mEmptyView = findViewById(R.id.empty_view);
        mTextHint = (TextView)findViewById(R.id.text_hint);
        mToolbar = (SearchToolbar)findViewById(R.id.id_toolbar);
        mLayoutFile = findViewById(R.id.layout_file_search);
        mLayoutVideoImg = findViewById(R.id.layout_video_img_search);
        mTvContact = findViewById(R.id.text_hint_2);
        mResultListView.setOnTouchListener(closeKeyboardTouchListener);
    }

    @Override
    protected void init() {
        initToolbar();
        mAdapter = new MessageSearchAdapter(this);
        mAdapter.setRecyclerViewItemClickListener(this);
        WrapContentLinearLayoutManager linearLayoutManager = new WrapContentLinearLayoutManager(this);
        mResultListView.setLayoutManager(linearLayoutManager);
        mResultListView.setAdapter(mAdapter);

        final Bundle bundle = getIntent().getExtras();

        String key = bundle.getString(MessageSearchActivity.BUNDLE_KEY_KEYWORD);
        mBoxType = bundle.getInt(MessageSearchActivity.BUNDLE_KEY_BOXTYPE);
        // 如果是不可编辑的模式，即从全局搜索跳转的，不监听edittext变化，否则一进入就监听到“”
        if (key == null) {
            //过滤300毫秒以内的快速输入
            RxTextView.textChanges(mToolbar.getEditText()).debounce(300, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<CharSequence>() {
                        @Override
                        public void call(CharSequence charSequence) {
                            if ((mBoxType & Type.TYPE_BOX_MESSAGE) > 0) {
                                UmengUtil.buryPoint(getApplicationContext(), "message_p2pmessage_setup_searchbox","输入关键词",0);
                                HashMap<String, String> map  = new HashMap<String,String>();
                                map.put("ref","点对点搜内容");
                                MobclickAgent.onEvent(mContext, "Search", map);
                            } else if ((mBoxType & Type.TYPE_BOX_GROUP) > 0) {
                                HashMap<String, String> map  = new HashMap<String,String>();
                                map.put("ref","群聊搜内容");
                                MobclickAgent.onEvent(mContext, "Search", map);
                            }
//                            UmengUtil.buryPoint(getApplicationContext(), "message_p2pmessage_setup_searchbox","输入关键词",0);
                            mPresenter.searchKeyword(charSequence.toString());
                        }
                    });
        }

        mPresenter = new MessageSearchPresenter(this, this, getSupportLoaderManager(), bundle);

        mLayoutFile.setOnClickListener(this);
        mLayoutVideoImg.setOnClickListener(this);
    }

    private void initToolbar() {
        StatusBarCompat.setStatusBarColor(this, ContextCompat.getColor(this, R.color.color_2c2c2c));
//        mToolbar.setEditHint(getResources().getString(R.string.message_search));
        mToolbar.setEditHint(getResources().getString(R.string.input_key_word));
        mToolbar.setOnBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
//        mToolbar.setTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                mPresenter.searchKeyword(charSequence.toString());
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//
//            }
//        });

    }

    @Override
    public void updateResultListView(Cursor data, String keyword, int boxtype) {
        mResultListView.scrollToPosition(0);
        mAdapter.setKeyWord(keyword);
        mAdapter.setBoxType(boxtype);
        // 是否会引起线程不安全，后面跟踪
        mAdapter.setCursor(data, new BaseCustomCursorAdapter.OnSetCursorDoneListener() {
            @Override
            public void onSetCursorDone() {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    public static void start(Context context, String address, int boxType) {
        Intent intent = new Intent(context, MessageSearchActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY_ADDRESS, address);
        bundle.putInt(BUNDLE_KEY_BOXTYPE, boxType);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public static void start(Context context, int boxType, String address, String keyword, int count, String title) {
        Intent intent = new Intent(context, MessageSearchActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY_ADDRESS, address);
        bundle.putString(BUNDLE_KEY_KEYWORD, keyword);
        bundle.putInt(BUNDLE_KEY_COUNT, count);
        bundle.putString(BUNDLE_KEY_TITLE, title);
        bundle.putInt(BUNDLE_KEY_BOXTYPE, boxType);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    public void showEmptyView(boolean show) {
        if (show) {
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void showTextHint(boolean show) {
        if (show) {
            mTextHint.setVisibility(View.VISIBLE);
        } else {
            mTextHint.setVisibility(View.GONE);
        }
    }

    @Override
    public void switchToStaticMode(int count, String keyword, String title) {
        mToolbar.setTitle(title);
        if (count > 0 && !TextUtils.isEmpty(keyword)) {
            String hint = String.format(getResources().getString(R.string.count_chat_message),count + "", keyword);
            mTextHint.setText(hint);
        }
    }

    @Override
    public void showOhterFileSearchView(boolean show) {
        if(show){
            mTvContact.setVisibility(View.VISIBLE);
            mLayoutFile.setVisibility(View.VISIBLE);
            mLayoutVideoImg.setVisibility(View.VISIBLE);
        }else{
            mTvContact.setVisibility(View.GONE);
            mLayoutFile.setVisibility(View.GONE);
            mLayoutVideoImg.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        hideKeyboard();
        if ((mBoxType & Type.TYPE_BOX_MESSAGE) > 0) {
            HashMap<String, String> map  = new HashMap<String,String>();
            map.put("ref","点对点搜内容");
            MobclickAgent.onEvent(mContext, "Search_click", map);
        } else if ((mBoxType & Type.TYPE_BOX_GROUP) > 0) {
            HashMap<String, String> map  = new HashMap<String,String>();
            map.put("ref","群聊搜内容");
            MobclickAgent.onEvent(mContext, "Search_click", map);
        }
        Message message = mAdapter.getItem(position);
        mPresenter.openItem(message);
    }

    @Override
    public boolean onItemLongCLickListener(View v, int position) {
        return false;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        int i = view.getId();
        if (i == R.id.result_list) {
            hideKeyboard();

        } else {
        }
        return false;
    }

    //强制隐藏键盘
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mToolbar.getWindowToken(), 0);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        String address = getIntent().getExtras().getString(MessageSearchActivity.BUNDLE_KEY_ADDRESS);
        int boxType = getIntent().getExtras().getInt(MessageSearchActivity.BUNDLE_KEY_BOXTYPE);
        if(id == R.id.layout_file_search){
            if(TextUtils.isEmpty(address)){
                return;
            }
            ChatFileActivity.start(this ,address ,boxType ,false);
        }else if(id == R.id.layout_video_img_search){
            if(TextUtils.isEmpty(address)){
                return;
            }
            ChatFileActivity.start(this ,address ,boxType ,true);
        }
    }

    private View.OnTouchListener closeKeyboardTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            LogF.d("TAG", "closeKeyboardTouchListener");
            AndroidUtil.hideSoftInput(MessageSearchActivity.this, null);
            return false;
        }
    };
}
