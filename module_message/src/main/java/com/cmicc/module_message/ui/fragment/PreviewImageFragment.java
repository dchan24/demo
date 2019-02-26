package com.cmicc.module_message.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.module.proxys.moduleimgeditor.ImgEditorProxy;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.chinamobile.app.yuliao_common.application.App;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_common.utils.statusbar.StatusBarCompat;
import com.cmcc.cmrcs.android.glide.GlideApp;
import com.cmcc.cmrcs.android.ui.contracts.PreviewImageContract;
import com.cmcc.cmrcs.android.ui.fragments.BaseFragment;
import com.cmcc.cmrcs.android.ui.model.ImageData;
import com.cmcc.cmrcs.android.ui.model.MediaItem;
import com.cmcc.cmrcs.android.ui.view.MessageOprationDialog;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmcc.cmrcs.android.widget.ControlViewPager;
import com.cmcc.cmrcs.android.widget.SmoothImageView;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.adapter.PreviewImageAdapter;
import com.cmicc.module_message.ui.presenter.PreviewImagePresenter;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.OnViewTapListener;

import java.io.File;
import java.util.ArrayList;


/**
 * Created by GuoXietao on 2017/4/27.
 */

public class PreviewImageFragment extends BaseFragment implements PreviewImageContract.IView{
    private static final String TAG = "PreviewImageFragment";

    private PreviewImageContract.IPresenter mPresenter;
    private ControlViewPager mVpPreview;
    private ProgressBar mPbLoading;
    private TextView mTvLoading;
    private SmoothImageView mIvSmooth;

    private PreviewImageAdapter mGalleryAdapter;
    private int mFirstIndex;
    private ArrayList<ImageData> mImageDatas;
    private Display mDisplay;
    private Handler mHandler;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_preview_image;
    }
    @Override
    public void initViews(View rootView){
        super.initViews(rootView);
        mVpPreview = rootView.findViewById(R.id.vp_preview);
        mPbLoading = rootView.findViewById(R.id.pb_loading);
        mTvLoading = rootView.findViewById(R.id.tv_loading);
        mIvSmooth = rootView.findViewById(R.id.iv_smooth);
    }

    @Override
    public void udpateView(boolean scrollable, int progressVisiable, int progress) {
        if(mVpPreview == null){
            return;
        }
        mVpPreview.setScrollable(true);
        mPbLoading.setVisibility(progressVisiable);
        mTvLoading.setVisibility(progressVisiable);
        if(progress < 0){
            setProgress(0);
        }else if (progress != -1) {
            setProgress(progress);
        } else {
            setProgress(0);
        }
    }

    @Override
    public void setPresenter(PreviewImageContract.IPresenter p) {
        mPresenter = p;
    }

    @Override
    public void initData() {
        mDisplay = getActivity().getWindowManager().getDefaultDisplay();
        mHandler = new Handler();
        StatusBarCompat.setStatusBarColor(getActivity(), getActivity().getResources().getColor(R.color.statusbar_black));
        if(mPresenter == null) {
            LogF.d(TAG,"sometimes monkey test happened mPresenter == null");
            return;
        }
        mPresenter.setData(getArguments());
        mGalleryAdapter = new PreviewImageAdapter(getActivity(), mPresenter.getMediaSet(), (PreviewImagePresenter) mPresenter);
        mVpPreview.setAdapter(mGalleryAdapter);
        mGalleryAdapter.setOnPhotoTapListener(new OnPhotoTapListener() {
            @Override
            public void onPhotoTap(ImageView view, float x, float y) {
                //onBackPressed();
            }
        });

        mGalleryAdapter.setOnViewTapListener(new OnViewTapListener() {
                @Override
                public void onViewTap(View view, float x, float y) {
                    onBackPressed();
                }
        });

        mGalleryAdapter.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String[] itemList = null;

                if (mPresenter.isNeedShowTowDimensionHint()) {
                    itemList = new String[3];
                    itemList[0] = getString(R.string.forwarld);
                    itemList[1] = getString(R.string.save_picture);
                    itemList[2] = getString(R.string.discriminate_QRcode);
                }  else if(mPresenter.isEditable()){
                    itemList = new String[3];
                    itemList[0] = getString(R.string.forwarld);
                    itemList[1] = getString(R.string.save_picture);
                    itemList[2] = getString(R.string.image_edit);
                }else{
                    itemList = new String[2];
                    itemList[0] = getString(R.string.forwarld);
                    itemList[1] = getString(R.string.save_picture);
                }

                MessageOprationDialog messageOprationDialog = new MessageOprationDialog(getActivity(), null, itemList, null);
                messageOprationDialog.setOnMessageItemClickListener(new MessageOprationDialog.OnOprationItemClickListener() {
                    @Override
                    public void onClick(String item, int which, String address) {
                        if (item.equals(getString(R.string.forwarld))) {
                            mPresenter.sendImage(which);
                        } else if (item.equals(getString(R.string.save_picture))) {
                            Toast.makeText(getActivity(), getString(R.string.is_saving), Toast.LENGTH_SHORT).show();
                            mPresenter.saveImage(which);
                        } else if (item.equals(getString(R.string.discriminate_QRcode))) {
                            mPresenter.handleTwoDimensionScan();
                        } else if(item.equals(getString(R.string.image_edit))){
                            mPresenter.editImage(which);
                        }
                    }
                });
                messageOprationDialog.show();
                return false;
            }
        });
        mVpPreview.setOffscreenPageLimit(1);
        mVpPreview.setCurrentItem(mPresenter.getFirstPositon());
        mVpPreview.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mPresenter.setCurrentPage(position);
                updateSmoothImage(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
        setSmoothImage();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate a fragment view
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @Override
    public void onBackPressed() {
        mIvSmooth.setVisibility(View.VISIBLE);
        mIvSmooth.transformOut();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }

    @Override
    public void onFileRecvFail(boolean isCurrentPage) {
        if(mHandler == null){
            return;
        }
        if(isCurrentPage){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mGalleryAdapter.notifyDataSetChanged();
                    setLayoutLoadingVisivle(View.INVISIBLE);
                    setProgress(0);
                }
            });
        }else{
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mGalleryAdapter.notifyDataSetChanged();
                }
            });
        }

    }

    @Override
    public void onFileCompressing(boolean isCurrentPage) {
        if(mHandler == null){
            return;
        }
        if(isCurrentPage){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mGalleryAdapter.notifyDataSetChanged();
                    setLayoutLoadingVisivle(View.INVISIBLE);
                    setProgress(0);
                }
            });
        }else{
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mGalleryAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ImgEditorProxy.g.getServiceInterface().getFinalRequestEditPictureStatus()) {
            if (resultCode == ImgEditorProxy.g.getServiceInterface().getFinalRequestEditPictureStatus() || resultCode == getActivity().RESULT_FIRST_USER) {
                int imgEditorStatus = data.getIntExtra(ImgEditorProxy.g.getServiceInterface().getFinalImageStatus(), -1);
                if (imgEditorStatus == ImgEditorProxy.g.getServiceInterface().getFinalSendImageStatus()) {
                    getActivity().setResult(ImgEditorProxy.g.getServiceInterface().getFinalRequestEditPictureStatus(), data);
                    getActivity().finish();
                }
            }
        }
    }

    @Override
    public void onFileRecvDone(boolean isCurrentPage) {
        if(mHandler == null || mPresenter == null){
            return;
        }
        if (isCurrentPage) {
            mPresenter.checkImageForTowDimension();

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setProgress(100);
                }
            }, 250);

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mGalleryAdapter.notifyDataSetChanged();
                    setLayoutLoadingVisivle(View.INVISIBLE);
                    setProgress(0);
                }
            }, 500);
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mGalleryAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public void setProgress(final int progress) {
        if (this.getActivity() != null) {
            this.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String p = String.valueOf(progress) + "%";
                    mTvLoading.setText(p);
                }
            });
        }
    }


    /**
     * 显示压缩过程。 只显示旋转圈， 不显示数字
     */
    public void setCompressLoading(){
        if (this.getActivity() != null) {
            this.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mPbLoading.setVisibility(View.VISIBLE);
                    mTvLoading.setVisibility(View.GONE);
                }
            });
        }
    }

    public void setLayoutLoadingVisivle(final int visibility) {
        if (this.getActivity() != null) {
            this.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mPbLoading.setVisibility(visibility);
                    mTvLoading.setVisibility(visibility);
                }
            });
        }
    }


    private void updateSmoothImage(int position) {
        MediaItem item = mPresenter.getMediaSet().getMediaList().get(position);

        //ToDo:有待优化，原图缩回失败图大小不一
        if (mImageDatas != null) {
            int index = position - mPresenter.getFirstPositon() + mFirstIndex;
            if (index >= 0 && index < mImageDatas.size()) {
                ImageData imageData = mImageDatas.get(index);
                mIvSmooth.setOriginalInfo(imageData.getWidth(), imageData.getHeight(), imageData.getX(), imageData.getY());
            } else if (index < 0) {
                ImageData imageData = mImageDatas.get(0);
                mIvSmooth.setOriginalInfo(imageData.getWidth(), imageData.getHeight(), mDisplay.getWidth() / 3, -imageData.getHeight() * 2);
            } else {
                ImageData imageData = mImageDatas.get(mImageDatas.size() - 1);
                mIvSmooth.setOriginalInfo(imageData.getWidth(), imageData.getHeight(), mDisplay.getWidth() / 3, mDisplay.getHeight() + imageData.getHeight() * 2);
            }
        }
        showSmoothImage(item);
    }

    void setSmoothImage() {
        Bundle b = getArguments();
        mImageDatas = (ArrayList<ImageData>) b.getSerializable("imageDatas");
        mFirstIndex = b.getInt("firstIndex");
        ImageData imageData;
        if (mImageDatas != null) {
            imageData = mImageDatas.get(mFirstIndex);
        } else {
            WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            imageData = new ImageData(wm.getDefaultDisplay().getWidth() / 2 - 50, wm.getDefaultDisplay().getHeight() / 2 - 50, 100, 100);
        }
        mIvSmooth.setOriginalInfo(imageData.getWidth(), imageData.getHeight(), imageData.getX(), imageData.getY());
        mIvSmooth.setResize(false);
        mIvSmooth.setTransformDuration(200);
        mIvSmooth.setBgColor(Color.BLACK);
        mIvSmooth.setDontScale(false);

        MediaItem mediaItem = mPresenter.getMediaSet().getMediaList().get(mPresenter.getFirstPositon());
        showSmoothImage(mediaItem);

        mIvSmooth.setResize(false);
        mIvSmooth.setBgColor(getResources().getColor(R.color.black));

        mIvSmooth.setLayoutParams(new RelativeLayout.LayoutParams(-1, -1));
        mIvSmooth.setOnTransformListener(new SmoothImageView.TransformListener() {
            @Override
            public void onTransformStart(int mode) {
                if(mVpPreview==null){
                    return;
                }
                if (mode == 2) {
                    ((RelativeLayout) mIvSmooth.getParent()).setBackgroundColor(Color.TRANSPARENT);
                    mIvSmooth.setBgColor(Color.TRANSPARENT);
                    mVpPreview.setVisibility(View.INVISIBLE);
                    setLayoutLoadingVisivle(View.INVISIBLE);
                }
            }

            @Override
            public void onTransformComplete(int mode) {
                if(mVpPreview==null){
                    return;
                }
                if (mode == 1) {
                    mPresenter.setCurrentPage(mPresenter.getFirstPositon());
                    mVpPreview.setVisibility(View.VISIBLE);
                    // monkey 测试，执行onBackPressed会出现mHandler为null，
                    if(mHandler != null) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (mIvSmooth != null) {
                                    mIvSmooth.setVisibility(View.GONE);
                                }
                            }
                        }, 400);
                    }
                }

                if (mode == 2 && getActivity() != null) {
                    getActivity().finish();
                    getActivity().overridePendingTransition(0, 0);
                }
            }
        });
        mIvSmooth.transformIn();
    }

    private void showSmoothImage(MediaItem mediaItem) {
        // 公众号图片
        String recvImgthumbPath = mediaItem.getThumbPath();
        if (recvImgthumbPath != null && recvImgthumbPath.contains("http:")) {
            RequestOptions options = new RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.DATA).error(R.drawable.chat_image_loading_fail).dontAnimate().dontTransform();
            Glide.with(App.getAppContext()).asBitmap().load(recvImgthumbPath)
                    .apply(options)
                    .into(mIvSmooth);
            return;
        }

        String url = mediaItem.getLocalPath();
        File file = new File(url);
        float fileSize = mediaItem.getFileLength();
        float downSize = mediaItem.getDownSize();

        boolean isFullFile = file.exists() && fileSize != 0 && fileSize == file.length() && downSize == fileSize;

        //缩略图
        String thumbPath = mediaItem.getThumbPath();
        boolean isThumbExist = false;
        int thumbWidth = 0;
        int thumbHeihgt = 0;
        if (!TextUtils.isEmpty(thumbPath)) {
            File f = new File(thumbPath);
            if (f != null && f.exists()) {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(thumbPath, opts);
                if(opts.outWidth<0 || opts.outHeight<0){
                    isThumbExist = false;
                }else{
                    thumbWidth = opts.outWidth;
                    thumbHeihgt = opts.outHeight;
                    isThumbExist = true;
                }
            }
        }

        //优先显示 原图
        if (isFullFile && fileSize<500*1024) {//图片已经下载完毕, 500k以内的图片才用原图做动画
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(url, opts);
            if ((opts.outWidth <= 0 || opts.outHeight <= 0)) {//
                BaseToast.show(R.string.pic_broken);
                if(isThumbExist){
                    GlideApp.with(App.getAppContext())
                            .asBitmap()
                            .load(thumbPath)
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .error(R.drawable.preview_image_fail)
                            .override(thumbWidth, thumbHeihgt)
                            .dontAnimate().dontTransform().into(mIvSmooth);
                }else{
                    GlideApp.with(App.getAppContext())
                            .asBitmap()
                            .load(R.drawable.preview_image_fail)
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .error(R.drawable.preview_image_fail)
                            .override(opts.outWidth, opts.outHeight)//防止drawable的尺寸在加载的时候，处理错误
                            .dontAnimate().dontTransform().into(mIvSmooth);
                }

            }else if(opts.outWidth> 5000 || opts.outHeight>5000){//图片尺寸过大
                if(isThumbExist){
                    GlideApp.with(App.getAppContext())
                            .asBitmap()
                            .load(thumbPath)
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .error(R.drawable.preview_image_fail)
                            .override(thumbWidth, thumbHeihgt)
                            .dontAnimate().dontTransform().into(mIvSmooth);
                }else{
                    GlideApp.with(App.getAppContext())
                            .asBitmap()
                            .load(R.drawable.preview_image_fail)
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .error(R.drawable.preview_image_fail)
                            .override(opts.outWidth, opts.outHeight)//防止drawable的尺寸在加载的时候，处理错误
                            .dontAnimate().dontTransform().into(mIvSmooth);
                }
            }else{
//                int min = opts.outWidth<opts.outHeight? opts.outWidth:opts.outHeight;
//
//                int targetWidth = opts.outWidth;
//                int targetHeight = opts.outHeight;
//
//                int screenHeight = ((PreviewImageActivity) getActivity()).mScreenHeight;
//                if(min > screenHeight){
//                    if(opts.outWidth> opts.outHeight){
//                        targetWidth = opts.outWidth * screenHeight / opts.outHeight;
//                        targetHeight = screenHeight;
//                    }else{
//                        targetWidth = screenHeight;
//                        targetHeight = opts.outHeight * screenHeight /opts.outWidth;
//                    }
//                }

                RequestOptions options = new RequestOptions().error(R.drawable.preview_image_fail)
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE).dontTransform().dontAnimate()
                        .override(opts.outWidth, opts.outHeight);//防止drawable的尺寸在加载的时候，处理错误
                GlideApp.with(App.getAppContext())
                        .asBitmap()
                        .load(url)
                        .apply(options)
                        .into(mIvSmooth);
                //mIvSmooth.setImageBitmap();
            }
            return;

            //1000*10
            //10000*100;
        }


        if (isThumbExist) {
            GlideApp.with(App.getAppContext())
                    .asBitmap()
                    .load(thumbPath)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .error(R.drawable.preview_image_fail)
                    .override(thumbWidth, thumbHeihgt)
                    .dontAnimate().dontTransform().into(mIvSmooth);
            return;
        }

        GlideApp.with(App.getAppContext())
                .load(R.drawable.preview_image_fail)
                .dontAnimate().dontTransform().into(mIvSmooth);
        LogF.i(TAG, "onError: " + url);
    }
}
