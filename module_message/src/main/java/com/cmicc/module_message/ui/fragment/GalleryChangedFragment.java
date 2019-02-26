package com.cmicc.module_message.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.module.proxys.moduleimgeditor.ImgEditorProxy;
import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.yuliao_common.application.App;
import com.chinamobile.app.yuliao_common.utils.statusbar.StatusBarCompat;
import com.cmicc.module_message.ui.adapter.GalleryChangedAdapter;
import com.cmicc.module_message.ui.constract.GalleryChangedContract;
import com.cmcc.cmrcs.android.ui.fragments.BaseFragment;
import com.cmcc.cmrcs.android.ui.model.MediaItem;
import com.cmcc.cmrcs.android.ui.view.HackyViewPager;
import com.cmcc.cmrcs.android.widget.SmoothImageView;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.presenter.GalleryPresenter;
import com.github.chrisbanes.photoview.OnViewTapListener;

import java.util.ArrayList;


/**
 * Created by GuoXietao on 2017/4/10.
 */

public class GalleryChangedFragment extends BaseFragment implements GalleryChangedContract.View {

    HackyViewPager mVpPreview;
    SmoothImageView mIvSmooth; // 图片缩放动画的view
    private View mVideoLayer; // 视频item的 播放按钮的容器， 因为photoview在zoomable为false时， 无法监听touch事件，所以构造此层来处理touch。
    private ImageView mIvPlay;        //视频播放按钮
    ImageView mIvSelect;
    Button mBtnSend;
    private RelativeLayout mTopPanel; //顶部操作栏
    private RelativeLayout mBottomPanel; //底部操作栏
    LinearLayout mLlSelect;

    private GalleryChangedContract.Presenter mPresenter;
    private Activity activity;
    private int mLocationX;
    private int mLocationY;
    private int mWidth;
    private int mHeight;
    private int mFirstPosition;
    private int mFirstRow;
    private int mFirstCollumn;
    private Handler mHandler;
    private Bitmap mBitmap;
    private GalleryChangedAdapter mGalleryChangedAdapter;
    private CheckBox mCbOriginalPhoto;
    private boolean mIsOriginPhoto;
    private TextView  toolbarTitle;
    private TextView mEditImageTv;
    private int tootie ;

    private boolean isPanelVisiable = true;

    @Override
    public void initViews(View rootView){
        super.initViews(rootView);
        mVpPreview = rootView.findViewById(R.id.vp_preview);
        mIvSmooth = rootView.findViewById(R.id.iv_smooth);
        mIvPlay = rootView.findViewById(R.id.iv_play);
        mIvSelect = rootView.findViewById(R.id.iv_select);
        mBtnSend = rootView.findViewById(R.id.btn_send);

        mTopPanel = rootView.findViewById(R.id.top_panel);
        mBottomPanel = rootView.findViewById(R.id.bottom_panel);

        mLlSelect = rootView.findViewById(R.id.ll_select);
        mCbOriginalPhoto = rootView.findViewById(R.id.cb_original_photo);

        // 标题栏
        toolbarTitle = rootView.findViewById(R.id.title);
        //编辑按钮
        mEditImageTv = rootView.findViewById(R.id.edit);
        mEditImageTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.handleEditImage();
            }
        });

        activity = getActivity();

        //返回按钮
        rootView.findViewById(R.id.quit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.setResult(1 ,getActivity().getIntent());
                activity.finish();
            }
        });
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_gallery_changed;
    }


    @Override
    public void initData() {
        Bundle bundle = getArguments();
        mIsOriginPhoto = bundle.getBoolean(GalleryPresenter.ORIGIN_PHOTO);
        mCbOriginalPhoto.setChecked(mIsOriginPhoto);
        //设置放大动画
        mIvPlay.setVisibility(View.GONE);
        setSmoothImage(bundle);
        StatusBarCompat.setStatusBarColor(getActivity(), 0xd91f1f1f);
        ArrayList<MediaItem> mediaItems = mPresenter.getMediaItems();
        if(mediaItems!=null){
            tootie = mediaItems.size();
            mGalleryChangedAdapter = new GalleryChangedAdapter(getActivity(), mediaItems);
            mGalleryChangedAdapter.setPhotoClickListener(new OnViewTapListener() {
                @Override
                public void onViewTap(View view, float x, float y) {
                    mPresenter.handlePhotoClick();
                    if(isPanelVisiable){
                        mTopPanel.setVisibility(View.GONE);
                        mBottomPanel.setVisibility(View.GONE);
                        isPanelVisiable = false;
                    }else{
                        mTopPanel.setVisibility(View.VISIBLE);
                        mBottomPanel.setVisibility(View.VISIBLE);
                        isPanelVisiable = true;
                    }
                }
            });
            mVpPreview.setAdapter(mGalleryChangedAdapter);
            if(tootie!=0){
                toolbarTitle.setText(getString(R.string.pre_view)+"("+ (mPresenter.getCurrentPosition()+1)+"/"+ tootie+")");
            }
        }
        mVpPreview.setOffscreenPageLimit(1);
        mVpPreview.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (positionOffset > 0) {
                    mIvPlay.setVisibility(View.GONE);
                } else if (mPresenter.getMediaType() == MediaItem.MEDIA_TYPE_VIDEO) {
                    mPresenter.handlePageSelected(mPresenter.getCurrentPosition());// ViewPager滑动，当前向是否选中的逻辑
                }
            }

            @Override
            public void onPageSelected(int position) {
                if(tootie!=0){
                    toolbarTitle.setText(getString(R.string.pre_view)+"("+ (position+1)+"/"+ tootie+")");
                }
                mPresenter.handlePageSelected(position); // ViewPager滑动，当前向是否选中的逻辑
                if (mPresenter.isFilebroken(position)) {
                    mLlSelect.setClickable(false);
                    mIvPlay.setEnabled(false);
                } else {
                    mLlSelect.setClickable(true);
                    mIvPlay.setEnabled(true);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        mVpPreview.setCurrentItem(mPresenter.getCurrentPosition());
        mLlSelect.setOnClickListener(new View.OnClickListener() { // 选择按钮点击逻辑
            @Override
            public void onClick(View v) {
                mPresenter.handleSelect(false);
            }

        });
        mBtnSend.setOnClickListener(new View.OnClickListener() { // 发送
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(GalleryPresenter.ORIGIN_PHOTO, mIsOriginPhoto);
                activity.setResult(-2, intent);
                activity.finish();
            }
        });
        int i = mPresenter.getCurrentPosition();
        if(mediaItems !=null && i >=0 && i<mediaItems.size() ){ // 报过 下标越界异常
            setSelect(mediaItems.get(mPresenter.getCurrentPosition())); // 进入界面选中开始预览的第一项
        }
        updateSelectNumber(mPresenter.getSelectItems().size());
        mIvPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.startPlay();
            }
        });

        mCbOriginalPhoto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {//是否原图的选项
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setOriginalPhotoState(isChecked);
                if(isChecked){
                    mPresenter.handleSelect(true);
                }
            }
        });
    }

    private void setOriginalPhotoState(boolean isOriginPhoto){
        mIsOriginPhoto = isOriginPhoto;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate a fragment view
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        return rootView;
    }

    public void updateSelectNumber(int number) {
        if (number == 0) {
            mBtnSend.setText(getString(R.string.send_));
            mBtnSend.setEnabled(false);
            mBtnSend.setTextColor(getActivity().getResources().getColor(R.color.color_ffffff));
        } else {
            mBtnSend.setText(getString(R.string.send_) +"("+ number + ")");
            mBtnSend.setEnabled(true);
            mBtnSend.setTextColor(getActivity().getResources().getColor(R.color.color_ffffff));
        }
    }

    @Override
    public void setPresenter(GalleryChangedContract.Presenter p) {
        mPresenter = p;
    }

    @Override
    public void setSelect(MediaItem mediaItem) {
        if(GalleryPresenter.mOrderSelectedItems !=null && mediaItem!=null){
            if(GalleryPresenter.mOrderSelectedItems.contains(mediaItem)){ // 当前向已经选中
                mIvSelect.setImageResource(R.drawable.chat_image_box_selected);
            }else{
                mIvSelect.setImageResource(R.drawable.chat_image_box_unselected);
            }
        }
        if (mediaItem != null && mediaItem.getMediaType() == MediaItem.MEDIA_TYPE_VIDEO) {
            mEditImageTv.setVisibility(View.GONE);
        } else {
            mEditImageTv.setVisibility(View.VISIBLE);
        }
    }

    private void updateSmoothImage(int position) {
        if (mPresenter.getPreviewSelect()) {
            return;
        }
        Bitmap bitmap = GalleryPresenter.getSelectListBitmap(position); // getBitmap
        if (bitmap == null) {
            mIvSmooth.setImageDrawable(App.getAppContext().getResources().getDrawable(R.drawable.chat_image_loading_fail));
        } else {
            mIvSmooth.setImageBitmap(bitmap);
        }

        //计算新的缩略图位置
        position += 1;
        int newRow = (position) / 3;
        int newColumn = (position) % 3;
        int deltaRow = newRow - mFirstRow;
        int deltaColumn = newColumn - mFirstCollumn;
        float fourDip2Px = AndroidUtil.dip2px(App.getAppContext(), 4);
        int newLocationX = mLocationX + deltaColumn * ((int) fourDip2Px + mWidth);
        int newLocationY = mLocationY + deltaRow * ((int) fourDip2Px + mHeight);
        mIvSmooth.setOriginalInfo(mWidth, mHeight, newLocationX, newLocationY);

    }


    @Override
    public void setSmoothImage(Bundle bundle) {
        if (mPresenter.getPreviewSelect()) {
            return;
        }
        mVpPreview.setVisibility(View.INVISIBLE);
        mLocationX = bundle.getInt("locationX", 0);
        mLocationY = bundle.getInt("locationY", 0);
        mWidth = bundle.getInt("width", 0);
        mHeight = bundle.getInt("height", 0);
        mFirstPosition = bundle.getInt(GalleryPresenter.CURRENT_POSITION, 0);
        mFirstRow = (mFirstPosition + 1) / 3;
        mFirstCollumn = (mFirstPosition + 1) % 3;
        float fiftySixDip2Px = AndroidUtil.dip2px(App.getAppContext(), 56);
        mLocationY = mLocationY - (int) fiftySixDip2Px;
        mIvSmooth.setOriginalInfo(mWidth, mHeight, mLocationX, mLocationY);
        mIvSmooth.setResize(false);
        mIvSmooth.setTransformDuration(200);
        mIvSmooth.setBgColor(getResources().getColor(R.color.black));
        mBitmap = null;
        Bitmap bitmap = GalleryPresenter.getSelectListBitmap(mFirstPosition); // getBitmap(mFirstPosition)
        if (bitmap == null) {
            mIvSmooth.setImageDrawable(App.getAppContext().getResources().getDrawable(R.drawable.chat_image_loading_fail));
        } else {
            mIvSmooth.setImageBitmap(bitmap);
        }

        mIvSmooth.setLayoutParams(new RelativeLayout.LayoutParams(-1, -1));

        mIvSmooth.setOnTransformListener(new SmoothImageView.TransformListener() {
            @Override
            public void onTransformStart(int mode) {
                if (mode == 1) {
                    mIvPlay.setVisibility(View.GONE);
                }
                if (mode == 2) {
                    ((RelativeLayout) mIvSmooth.getParent()).setBackgroundColor(Color.TRANSPARENT);
                    mVpPreview.setVisibility(View.INVISIBLE);
                    mBottomPanel.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onTransformComplete(int mode) {
                if (mode == 1) {
                    //放大动画执行结束
                    if (mPresenter.getMediaType() == MediaItem.MEDIA_TYPE_VIDEO) {
                        mIvPlay.setVisibility(View.VISIBLE);
                    } else {
                        mIvPlay.setVisibility(View.GONE);
                    }
                    mVpPreview.setVisibility(View.VISIBLE);
                    mHandler = new Handler();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mIvSmooth != null) {
                                mIvSmooth.setVisibility(View.GONE);
                            }
                        }
                    }, 600);
                }
                if (mode == 2) {
                    //缩小动画执行结束
                    mIvSmooth.setImageDrawable(null);
                    if (mBitmap != null && !mBitmap.isRecycled()) {
                        mBitmap.recycle();
                        mBitmap = null;
                    }
                    Intent intent = new Intent();
                    intent.putExtra(GalleryPresenter.ORIGIN_PHOTO, mIsOriginPhoto);
                    getActivity().setResult(Activity.RESULT_OK, intent);
                    getActivity().finish();
                    getActivity().overridePendingTransition(0, 0);
                }
            }
        });

        //开始执行放大动画
        mIvSmooth.transformIn();
    }

    @Override
    public void onBackPressed() {
        mIvPlay.setVisibility(View.GONE);
        if (mPresenter.getPreviewSelect()) {
            Intent intent = new Intent();
            intent.putExtra(GalleryPresenter.ORIGIN_PHOTO, mIsOriginPhoto);
            getActivity().setResult(Activity.RESULT_OK, intent);
            getActivity().finish();
        } else {
            updateSmoothImage(mPresenter.getCurrentPosition());
            if (mHandler != null) {
                mHandler.removeCallbacksAndMessages(null);
                mHandler = null;
            }
            mIvSmooth.setVisibility(View.VISIBLE);
            mIvSmooth.transformOut();
        }
    }

    @Override
    public void showIvPlay(boolean isShow) {
        if (isShow) {
            mIvPlay.setVisibility(View.VISIBLE);
        } else {
            mIvPlay.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode	==	ImgEditorProxy.g.getServiceInterface().getFinalRequestEditPictureStatus()){
            if(resultCode	==	ImgEditorProxy.g.getServiceInterface().getFinalRequestEditPictureStatus()	||	resultCode	==	getActivity().RESULT_FIRST_USER){
                int imgEditorStatus = data.getIntExtra(ImgEditorProxy.g.getServiceInterface().getFinalImageStatus(),-1);
                if(imgEditorStatus == ImgEditorProxy.g.getServiceInterface().getFinalSendImageStatus()){
                    activity.setResult(ImgEditorProxy.g.getServiceInterface().getFinalRequestEditPictureStatus(), data);
                    activity.finish();
                }
            }
        }
    }

}
