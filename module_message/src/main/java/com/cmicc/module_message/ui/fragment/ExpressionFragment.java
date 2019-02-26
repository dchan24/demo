package com.cmicc.module_message.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_common.utils.SharePreferenceUtils;
import com.cmcc.cmrcs.android.ui.adapter.ExpressionGridViewAdapter;
import com.cmcc.cmrcs.android.ui.adapter.MViewPagerAdapter;
import com.cmicc.module_message.ui.constract.ExpressionContract;
import com.cmcc.cmrcs.android.ui.fragments.BaseFragment;
import com.cmcc.cmrcs.android.ui.view.PageControlView;
import com.cmcc.cmrcs.android.widget.emoji.EmojiEntity;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.presenter.ExpressionPresenter;

import java.util.ArrayList;

import static com.cmicc.module_message.ui.presenter.ExpressionPresenter.EM_SIZE;
import static com.cmicc.module_message.ui.presenter.ExpressionPresenter.NUMCOLUMNS;


/**
 * Created by GuoXietao on 2017/5/3.
 */

public class ExpressionFragment extends BaseFragment implements ExpressionContract.IView ,View.OnClickListener{
    public static final int GIF_RESULT_OK = -1;
    public static final int GET_GIF = 0;
    public static final String GIF_URL = "gif_url";
    private final String SP_KEY_FIRST_FUNNY_PICS_TIPS = "sp_key_first_funny_pics_tips";     // 是否显示"趣图流量提示"
    ViewPager mVpExpression;
    LinearLayout lltButton;
    PageControlView mPcvExpression;
    ImageButton btn_gif;
    ImageView btn_emoji;
    ImageView btn_emoji_b;
    ImageView ivBubble;
    RelativeLayout first_emoji;
    RelativeLayout sec_emoji;

    private ExpressionContract.IPresenter mPresenter;
    private ArrayList<View> mArray = new ArrayList<View>();
    private ArrayList<View> mArrayB = new ArrayList<View>();

    private setResultToMessageEditorFragment mListener;
    private startGetGif mGifLister;

    private int mPopupHeight;
    private int mPopupWidth;

    @Override
    public void initViews(View rootView){
        super.initViews(rootView);
        mVpExpression = (ViewPager) rootView.findViewById(R.id.vp_expression);
        lltButton = (LinearLayout) rootView.findViewById(R.id.lltButton);
        mPcvExpression = (PageControlView) rootView.findViewById(R.id.pcv_expression);
        btn_gif = (ImageButton) rootView.findViewById(R.id.btn_gif);
        btn_gif.setOnClickListener(this);
        btn_emoji = (ImageView) rootView.findViewById(R.id.btn_emoji);
        btn_emoji.setOnClickListener(this);
        btn_emoji_b = (ImageView) rootView.findViewById(R.id.btn_emoji_b);
        btn_emoji_b.setOnClickListener(this);
        ivBubble = (ImageView) rootView.findViewById(R.id.ivBubble);
        first_emoji = (RelativeLayout) rootView.findViewById(R.id.first_emoji);
        sec_emoji = (RelativeLayout) rootView.findViewById(R.id.sec_emoji);
        first_emoji.setOnClickListener(this);
        sec_emoji.setOnClickListener(this);
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_expression;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        LogF.e("","setUserVisibleHint");
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        LogF.d("","onHiddenChanged");
//        if (hidden) {
//            //do when hidden
//        } else {
//            //do when show
//        }
    }

    @Override
    public void initData() {
        //适配N9008手机Crash问题
        if(mPresenter == null) {
            ExpressionPresenter presenter = new ExpressionPresenter(getActivity());
            setPresenter(presenter);
            presenter.setView(this);
        }
        mPresenter.initData();
        for (int i = 0; i < mPresenter.getPageCount(); i++) {
            GridView gridView = (GridView) LayoutInflater.from(getActivity()).inflate(R.layout.gridview_expression, null).findViewById(R.id.gv_expression);
            ExpressionGridViewAdapter adapter = new ExpressionGridViewAdapter(getActivity(), mPresenter.getEmojiArray(), i, EM_SIZE);
            gridView.setNumColumns(NUMCOLUMNS);
            gridView.setAdapter(adapter);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    EmojiEntity emojiEntity = (EmojiEntity) view.getTag(R.id.emoji_entity);
                    if (null == emojiEntity) {
                        return;
                    }
                    if (emojiEntity.icon == R.drawable.btn_delete_selector) {
                        if (mListener != null) {
                            mListener.deleteTextButton();
                        }
                        return;
                    }
                    if (emojiEntity.emoji != null) {
                        if (mListener != null) {
                            mListener.getResultFrofragment(emojiEntity.emoji);
                        }
                    }
                }
            });
            mArray.add(gridView);
        }
        MViewPagerAdapter mViewPagerAdapter = new MViewPagerAdapter(mArray);
        mVpExpression.setAdapter(mViewPagerAdapter);
        mVpExpression.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mPcvExpression.snapCurrentIndex(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mPcvExpression.initImageSourceId(R.drawable.message_tool_expression_index_norml, R.drawable.message_tool_expression_index_focus);
        mPcvExpression.setCount(mPresenter.getPageCount());
        mPcvExpression.snapCurrentIndex(0);

        showPopupBubbule(true);
    }

    private void changeToEmojiA(){
        mPresenter.initEmojiBData();
        inflateGridView(mArray);

    }

    private void changeToEmojiB(){
        mPresenter.initEmojiBData();
        inflateGridView(mArrayB);

    }
    private void inflateGridView(ArrayList<View> array){
        if (array.size() == 0){
            for (int i = 0; i < mPresenter.getPageCount(); i++) {
                GridView gridView = (GridView) LayoutInflater.from(getActivity()).inflate(R.layout.gridview_expression, null).findViewById(R.id.gv_expression);
                ExpressionGridViewAdapter adapter = new ExpressionGridViewAdapter(getActivity(), mPresenter.getEmojiArray(), i, EM_SIZE);
                gridView.setNumColumns(NUMCOLUMNS);
                gridView.setAdapter(adapter);
                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        EmojiEntity emojiEntity = (EmojiEntity) view.getTag(R.id.emoji_entity);
                        if (null == emojiEntity) {
                            return;
                        }
                        if (emojiEntity.icon == R.drawable.btn_delete_selector) {
                            if (mListener != null) {
                                mListener.deleteTextButton();
                            }
                            return;
                        }
                        if (emojiEntity.emoji != null) {
                            if (mListener != null) {
                                mListener.getResultFrofragment(emojiEntity.emoji);
                            }
                        }
                    }
                });
                array.add(gridView);
            }
        }

        MViewPagerAdapter mViewPagerAdapter = new MViewPagerAdapter(array);
        mVpExpression.setAdapter(mViewPagerAdapter);
        mVpExpression.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mPcvExpression.snapCurrentIndex(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mPcvExpression.initImageSourceId(R.drawable.message_tool_expression_index_norml, R.drawable.message_tool_expression_index_focus);
        mPcvExpression.setCount(mPresenter.getPageCount());
        mPcvExpression.snapCurrentIndex(0);
    }


    public void setListener(setResultToMessageEditorFragment listener) {
        mListener = listener;
    }

    public void setGifSendListener(startGetGif listener){
        mGifLister = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate a fragment view
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        return rootView;
    }

    @Override
    public void setPresenter(ExpressionContract.IPresenter p) {
        mPresenter = p;
    }

    @Override
    public void updateView() {

    }

    public interface setResultToMessageEditorFragment {
        public void getResultFrofragment(String msg);

        // 默认表情的删除
        public void deleteTextButton();

    }

    public interface startGetGif{
        public void startGetGif();
    }

    private void showPopupBubbule(boolean isShow) {
        if (isShow) {
            if ((Boolean) SharePreferenceUtils.getParam(getContext(), SP_KEY_FIRST_FUNNY_PICS_TIPS, true)) {
                ivBubble.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        disablePopupBubble();
                    }
                });

//                Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.anim_in_from_right);
//                animation.setStartOffset(300);
//                ivBubble.setVisibility(View.VISIBLE);
//                ivBubble.setAnimation(animation);
            }
        } else {
            ivBubble.setVisibility(View.GONE);
            ivBubble.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_out_to_right));
        }
    }

    private void disablePopupBubble() {
        ivBubble.setVisibility(View.GONE);
        ivBubble.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_out_to_right));
        SharePreferenceUtils.setParam(getContext(), SP_KEY_FIRST_FUNNY_PICS_TIPS, false);
    }

    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.btn_gif) {

            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
//                getActivity().startActivityForResult(new Intent(getContext(), GifSendActivity.class), GET_GIF);
            if (mGifLister != null) {
                mGifLister.startGetGif();
            }
            disablePopupBubble();

        } else if (i == R.id.btn_emoji || i == R.id.first_emoji) {
            changeToEmojiA();
            first_emoji.setBackgroundColor(Color.parseColor("#D8D8D8"));
            sec_emoji.setBackgroundColor(Color.parseColor("#00000000"));


        }else if (i == R.id.btn_emoji_b || i == R.id.sec_emoji){
            changeToEmojiB();
            sec_emoji.setBackgroundColor(Color.parseColor("#D8D8D8"));
            first_emoji.setBackgroundColor(Color.parseColor("#00000000"));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
