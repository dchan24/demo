package com.cmicc.module_message.ui.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chinamobile.app.yuliao_common.utils.SharePreferenceUtils;
import com.chinamobile.app.yuliao_core.db.LoginDaoImpl;
import com.chinamobile.app.utils.AndroidUtil;
import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmicc.module_message.ui.adapter.MessageBgListAdapter;
import com.cmicc.module_message.ui.data.MessageBgModel;
import com.cmcc.cmrcs.android.ui.utils.BitmapUtils;
import com.cmcc.cmrcs.android.ui.utils.MessageThemeUtils;
import com.cmcc.cmrcs.android.ui.utils.WrapContentLinearLayoutManager;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmcc.cmrcs.android.widget.GridSpacingItemDecoration;
import com.cmicc.module_message.R;
import com.constvalue.MessageModuleConst;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhufang_lu on 2017/4/11.
 * 消息背景主题设置界面
 */

public class MessageBgSetActivity extends BaseActivity implements View.OnTouchListener {
    public static final String MESSAGE_STYLE_CHANGE = "message_style_change";
    public static final int THEME_0NE = 0;
    public static final int THEME_TWO = 1;
    public static final int THEME_THREE = 2;
    public static final int THEME_FOUR = 3;
    public static final int THEME_FIVE = 4;
    public static final int THEME_SIX = 5;
    public static final int THEME_SEVEN = 6;
    public static final int THEME_EIGHT = 7;
    private MessageBgListAdapter appListCenterAdapter;

    public RecyclerView centerSnapRecyclerView;

    Toolbar mToolbar;

    TextView mColorViewLeft;

    TextView mColorViewRight;

    ImageView mColorImageView;

    private TextView mTitle;
    private RelativeLayout mBack;

    private int mRecyclerViewState = RecyclerView.SCROLL_STATE_IDLE;

    private List<MessageBgModel> list;
    private int mPosition = 0;
    private String mAddress;
    private String userNum;
    private int mType; //记录主题类型
    private int mChatType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_message_theme);

    }

    @Override
    protected void findViews() {
        centerSnapRecyclerView = (RecyclerView)findViewById(R.id.centerSnapRecyclerView);
        mToolbar = (Toolbar)findViewById(R.id.message_bg_set_bar);
        mColorViewLeft = (TextView)findViewById(R.id.cvOne);
        mColorViewRight = (TextView)findViewById(R.id.cvTwo);
        mTitle = (TextView) findViewById(R.id.title);
        mTitle.setText(getString(R.string.message_bg_set_title));
        mTitle.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        mBack = (RelativeLayout) findViewById(R.id.back);
        mBack.setOnClickListener(this);
        mColorImageView = (ImageView)findViewById(R.id.ivBackground);
        findViewById(R.id.set_theme_confirm).setOnClickListener(this);
        findViewById(R.id.set_theme_cancel).setOnClickListener(this);
        initToolbar();
//        ButterKnife.bind(this);
        initThemeAndList();
        setUpRecyclerView();
    }

    @Override
    protected void init() {
        scrollValue = GridSpacingItemDecoration.dp2px((32 + 4 + 11) * 2);
    }

    private void setUpRecyclerView() {
        final WrapContentLinearLayoutManager layoutManagerCenter
                = new WrapContentLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        centerSnapRecyclerView.setLayoutManager(layoutManagerCenter);
        appListCenterAdapter = new MessageBgListAdapter(this);
        centerSnapRecyclerView.setAdapter(appListCenterAdapter);
        centerSnapRecyclerView.addItemDecoration(new GridSpacingItemDecoration(3, 20, true));
        final SnapHelper snapHelperCenter = new LinearSnapHelper();
        snapHelperCenter.attachToRecyclerView(centerSnapRecyclerView);

        appListCenterAdapter.updateList(list);

        centerSnapRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (mRecyclerViewState == RecyclerView.SCROLL_STATE_SETTLING
                        && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    View snapView = snapHelperCenter.findSnapView(layoutManagerCenter);
                    mPosition = recyclerView.getChildAdapterPosition(snapView);
                    mCenterPoint = mPosition;
                    setBg();
                }
                mRecyclerViewState = newState;
            }
        });
        Intent intent = getIntent();
        if (intent != null) {
            mAddress = intent.getStringExtra("address");
            mChatType = intent.getIntExtra("chatType", 0);
        }
        userNum = LoginDaoImpl.getInstance().queryLoginUser(this);
        mType = (int) SharePreferenceUtils.getParam(this, MessageModuleConst.MESSAGE_THEME_TYPE + userNum + mAddress, THEME_0NE);
        int offsetValue = GridSpacingItemDecoration.screenWidth() / 2 - GridSpacingItemDecoration.dp2px(43 + 4);
        //GridSpacingItemDecoration中设置了，第一个item和最后一个item是（43dp=图半径+margin）的偏移 。中间的其他item是4dp的间隔。
        layoutManagerCenter.scrollToPositionWithOffset(mType, offsetValue);
        mPosition = mType;
        mCenterPoint = mPosition;
        setBg();
    }

    private void initThemeAndList() {
        list = new ArrayList<>();
        list.add(new MessageBgModel("defaultTheme", R.drawable.chat_theme_default));
        list.add(new MessageBgModel("angleTheme", R.drawable.chat_theme_default));
        list.add(new MessageBgModel("doodleTheme", R.drawable.chat_second_theme));
        list.add(new MessageBgModel("forestTheme", R.drawable.theme_thumbnail_forest));
        list.add(new MessageBgModel("leafTheme", R.drawable.theme_thumbnail_leaf));
        list.add(new MessageBgModel("veinsTheme", R.drawable.theme_thumbnail_veins));
        list.add(new MessageBgModel("veinsTheme", R.drawable.chat_theme_dazzle));
        list.add(new MessageBgModel("veinsTheme", R.drawable.chat_theme_pink));
    }

    private void setBg() {
        int colorType = 0;
        int drawableId = R.drawable.theme_background_angles;
        switch (mPosition) {
            case 0:
                drawableId = R.drawable.theme_background_angles;
                colorType = 0;
                break;
            case 1:
                drawableId = R.drawable.theme_background_angles;
                colorType = 1;
                break;
            case 2:
                drawableId = R.drawable.theme_background_doodles;
                colorType = 2;
                break;
            case 3:
                drawableId = R.drawable.theme_background_forest;
                colorType = 3;
                break;
            case 4:
                drawableId = R.drawable.theme_background_leaf;
                colorType = 4;
                break;
            case 5:
                drawableId = R.drawable.theme_background_veins;
                colorType = 5;
                break;
            case 6:
                drawableId = R.drawable.chat_theme_dazzle_bg;
                colorType = 6;
                break;
            case 7:
                drawableId = R.drawable.chat_theme_pink_bg;
                colorType = 7;
                break;
        }
        int color[] = MessageThemeUtils.getValueFromTheme(MessageBgSetActivity.this, 1, colorType * MessageThemeUtils.THEME_COLOR_NUMBER);
        if(mColorViewLeft != null) {
            GradientDrawable leftDrawable = (GradientDrawable) mColorViewLeft.getBackground();
            leftDrawable.setColor(color[0]);
        }
        if(mColorViewRight != null) {
            GradientDrawable rightDrawable = (GradientDrawable) mColorViewRight.getBackground();
            rightDrawable.setColor(color[1]);
        }
        ViewGroup.LayoutParams lp = mColorImageView.getLayoutParams();
        float bkgHeight = AndroidUtil.getScreenHeight(this) - getResources().getDimension(R.dimen.toolbar_height);
        lp.width = (int)bkgHeight;
        mColorImageView.setLayoutParams(lp);
        mColorImageView.setImageBitmap(BitmapUtils.readBitMap(this,drawableId));
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setTitle("");//getString(R.string.message_bg_set_title)
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.set_theme_confirm) {
            if (mChatType == 0) {
            } else if (mChatType == 1) {
            }

            if (TextUtils.isEmpty(userNum)) {//检查登录用户的手机号
                BaseToast.show(R.string.user_name_is_null);
                return;
            }
            if (TextUtils.isEmpty(mAddress)) {//检查对话用户的手机号
                BaseToast.show(R.string.message_user_name_is_null);
                return;
            }


            //用userNum和mAddress来区分不同的用户设置
            SharePreferenceUtils.setParam(this, MessageModuleConst.MESSAGE_THEME_TYPE + userNum + mAddress, mPosition);
            SharePreferenceUtils.setParam(this, MESSAGE_STYLE_CHANGE + userNum + mAddress, 0);
            finish();

        } else if (i == R.id.set_theme_cancel) {
            finish();
        }else if (i == R.id.back){
            finish();
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                px = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                curPx = event.getX();
                if (Math.abs(curPx - px) > 20) {
                    if (curPx < px && 0 <= mCenterPoint && mCenterPoint < 7) { //左滑
                        centerSnapRecyclerView.smoothScrollBy(scrollValue, 0);
                        mCenterPoint++;
                    } else if (curPx > px && 1 <= mCenterPoint && mCenterPoint < 8) {//右滑
                        centerSnapRecyclerView.smoothScrollBy(-scrollValue, 0);
                        mCenterPoint--;
                    }
                    mPosition = mCenterPoint;
                    setBg();
                }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        centerSnapRecyclerView.clearOnScrollListeners();
        super.onDestroy();
    }

    private int scrollValue;
    private float px, py, curPx, curPy;
    private int mCenterPoint;

}
