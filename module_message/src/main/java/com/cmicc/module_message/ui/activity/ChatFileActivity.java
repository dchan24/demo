package com.cmicc.module_message.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.module.proxys.moduleaboutme.AboutMeProxy;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_contact.model.SimpleContact;
import com.cmcc.cmrcs.android.contact.data.ContactsCache;
import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmicc.module_message.ui.adapter.ChatFileAdapter;
import com.cmcc.cmrcs.android.ui.adapter.headerrecyclerview.PinnedHeaderEntity;
import com.cmicc.module_message.ui.presenter.ChatFileContract;
import com.cmcc.cmrcs.android.ui.model.MediaItem;
import com.cmicc.module_message.ui.presenter.ChatFilePresenter;
import com.cmcc.cmrcs.android.ui.recyclerview.itemDecoration.ChoosePictureItemDecoration;
import com.cmicc.module_message.R;

import java.util.List;

/**
 * @anthor situ
 * @time 2017/6/8 14:09
 * @description 聊天文件界面
 */

public class ChatFileActivity extends BaseActivity implements ChatFileContract.IView {

    public static final String BUNDLE_KEY_ADDRESS = "address";
    public static final String BUNDLE_KEY_BOXTYPE = "boxtype";
    public static final String BUNDLE_KEY_IS_IMG_VIDEO = "is_img_video";
    public static final String BUNDLE_KEY_NICKNAME_OWNER = "nick_name_owner";//自己的昵称，用于单聊的聊天文件
    public static final String BUNDLE_KEY_NICKNAME_OTHER = "nick_name_other";//对方的昵称，用于单聊的聊天文件

    Toolbar mToolbar;
    RecyclerView mRecyclerView;
    LinearLayout mLlNoFile;
    ActionBar ab;
    private TextView mTitle;
    private RelativeLayout mBack;
    TextView mTvNoContent;

    private ChatFileAdapter mAdapter;
    public ChatFileContract.IPresenter mPresenter;

    private boolean mIsFirstLoad = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_file);
    }

    @Override
    protected void findViews() {
        mToolbar = (Toolbar)findViewById(R.id.id_toolbar);
        mRecyclerView = (RecyclerView)findViewById(R.id.chat_file_list);
        mLlNoFile = (LinearLayout) findViewById(R.id.no_file_now);
        mTitle = (TextView) findViewById(R.id.title);
        mBack = (RelativeLayout) findViewById(R.id.back);
        mTvNoContent = (TextView) findViewById(R.id.text_no_file_hint);
    }

    @Override
    protected void init() {
        initToolBar();
        Bundle bundle = getIntent().getExtras();
        int spanCount = 4;
        if(!bundle.getBoolean(BUNDLE_KEY_IS_IMG_VIDEO)){
            spanCount = 1;
            mRecyclerView.setBackgroundResource(R.color.color_f5f5f5);
            mTitle.setText(getResources().getString(R.string.title_chat_file));
            mTvNoContent.setText(getResources().getString(R.string.no_files_now));
        }else{
            mTitle.setText(getResources().getString(R.string.video_img));
            mTvNoContent.setText(getResources().getString(R.string.no_content_show));
        }
        mTitle.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
        mAdapter = new ChatFileAdapter(this, bundle.getInt(BUNDLE_KEY_BOXTYPE)
                ,bundle.getString(BUNDLE_KEY_ADDRESS),bundle.getBoolean(BUNDLE_KEY_IS_IMG_VIDEO) ,bundle.getString(BUNDLE_KEY_NICKNAME_OTHER ,""));
        mRecyclerView.setAdapter(mAdapter);
//        mRecyclerView.addItemDecoration(new PinnedHeaderItemDecoration.Builder(BaseHeaderAdapter.TYPE_HEADER).enableDivider(true)
//                .setDividerId(R.drawable.chat_file_divider).create());
        ChoosePictureItemDecoration decoration = new ChoosePictureItemDecoration(this);
        int padding = decoration.getDectorationSpace();
        mRecyclerView.addItemDecoration(new ChoosePictureItemDecoration(this));
        mRecyclerView.setPadding(0,0,0,0);
        mPresenter = new ChatFilePresenter(this, this, getSupportLoaderManager(), bundle);
        mPresenter.start();

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initToolBar() {
        mToolbar.setTitleTextColor(getResources().getColor(R.color.tv_title_color));
        setSupportActionBar(mToolbar);

        ab = getSupportActionBar();
        ab.setTitle("");//getResources().getString(R.string.chat_file)
//        ab.setTitle(getResources().getString(R.string.no_files_now));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public static void start(Context context, String address, int boxType ,boolean isImgVideo) {
        Intent intent = new Intent(context, ChatFileActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY_ADDRESS, address);
        bundle.putInt(BUNDLE_KEY_BOXTYPE, boxType);
        bundle.putBoolean(BUNDLE_KEY_IS_IMG_VIDEO,isImgVideo);
        if(boxType != Type.TYPE_BOX_GROUP){
            SimpleContact contact = ContactsCache.getInstance().searchContactByNumberInHash(address);
            if(contact != null && !TextUtils.isEmpty(contact.getName())){
                bundle.putString(BUNDLE_KEY_NICKNAME_OTHER ,contact.getName());
            }
            String owner = AboutMeProxy.g.getServiceInterface().getMyProfileGiveName(context);
            if(!TextUtils.isEmpty(owner)){
                bundle.putString(BUNDLE_KEY_NICKNAME_OWNER ,owner);
            }
        }
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    public void updateRecyclerView(List<PinnedHeaderEntity<MediaItem>> list) {
        mAdapter.setData(list);
        // 进入界面首次load到数据才滚到底端
        if (mIsFirstLoad) {
            mRecyclerView.scrollToPosition(0);
            mIsFirstLoad = false;
        }
        if (list.size() > 0){
            mLlNoFile.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }else {
            mLlNoFile.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }
    }
}
