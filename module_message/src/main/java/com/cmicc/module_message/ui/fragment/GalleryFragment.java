package com.cmicc.module_message.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.app.module.proxys.moduleimgeditor.ImgEditorProxy;
import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.utils.RxAsyncHelper;
import com.chinamobile.app.yuliao_common.sysetem.MetYouActivityManager;
import com.cmicc.module_message.ui.adapter.AlbumAdapter;
import com.cmicc.module_message.ui.adapter.GalleryAdapter;
import com.cmicc.module_message.ui.activity.GalleryActivity;
import com.cmicc.module_message.ui.constract.GalleryContract;
import com.cmcc.cmrcs.android.ui.dialogs.HandlingProgressDialog;
import com.cmcc.cmrcs.android.ui.fragments.BaseFragment;
import com.cmcc.cmrcs.android.ui.model.MediaItem;
import com.cmcc.cmrcs.android.ui.model.MediaSet;
import com.cmicc.module_message.ui.presenter.GalleryPresenter;
import com.cmcc.cmrcs.android.ui.recyclerview.itemDecoration.ChoosePictureItemDecoration;
import com.cmicc.module_message.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rx.functions.Func1;

/**
 * Created by GuoXietao on 2017/3/27.
 */

public class GalleryFragment extends BaseFragment implements GalleryContract.View , AdapterView.OnItemClickListener ,View.OnClickListener{

    public RecyclerView mRecyclerViewGallery;
    TextView mTvPreview;
    Button mButtonSend;
    CheckBox mCbOriginalPhoto;

    GalleryContract.Presenter mPresenter;
    GalleryActivity activity;
    private GalleryAdapter mGalleryAdapter;

    private TextView selectName ;
    private ImageView dropDownImage ;
    private ListView selectList ;
    private HashMap<String, MediaSet> mGroupMap = new HashMap<String, MediaSet>();
    private List<MediaSet> mMediaSetList = new ArrayList<MediaSet>();
    private int selectAlbumPosition;
    private AlbumAdapter mAlbumAdapter;
    private static final int STR_MAX_CHAR_COUNT = 8;
    private AnimationSet animationSetIn , animationSetOut ;
    private HandlingProgressDialog mProgressDialog;
    private ImageView waittingImg;
    private Animation animation;// 旋转动画

    @Override
    public void initViews(View rootView){
        super.initViews(rootView);
        mRecyclerViewGallery = (RecyclerView) rootView.findViewById(R.id.recyclerView_gallery);
        mTvPreview = (TextView) rootView.findViewById(R.id.tv_preview);
        mButtonSend = (Button) rootView.findViewById(R.id.button_send);
        mCbOriginalPhoto = (CheckBox) rootView.findViewById(R.id.cb_original_photo);
        selectList = (ListView) rootView.findViewById(R.id.list_select);
        selectList.setOnItemClickListener( this ); // 设置Item监听器
        waittingImg = (ImageView) rootView.findViewById(R.id.img_wait);

        animation = AnimationUtils.loadAnimation(mContext, com.cmic.module_base.R.anim.asp_rotate_left);
        LinearInterpolator lir = new LinearInterpolator();
        animation.setInterpolator(lir);

    }
    public void setPresenter(GalleryContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void initData() {
        initToolBar();
        updateButton(0);
        final GridLayoutManager layoutManager = new GridLayoutManager(activity, 4);
        mRecyclerViewGallery.setLayoutManager(layoutManager);
        ChoosePictureItemDecoration decoration = new ChoosePictureItemDecoration(activity);
        int decorationSpace = decoration.getDectorationSpace();
        mRecyclerViewGallery.addItemDecoration(decoration);
        mRecyclerViewGallery.setPadding(decorationSpace,mRecyclerViewGallery.getPaddingTop(),decorationSpace,mRecyclerViewGallery.getPaddingBottom());
        ((SimpleItemAnimator) mRecyclerViewGallery.getItemAnimator()).setSupportsChangeAnimations(false);

        mRecyclerViewGallery.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if(mGalleryAdapter == null){
                    return;
                }
                if(newState == RecyclerView.SCROLL_STATE_IDLE){
                    int first = layoutManager.findFirstVisibleItemPosition();
                    int end = layoutManager.findLastVisibleItemPosition();
                    mGalleryAdapter.enableRefresh(true);
                    mGalleryAdapter.notifyItemRangeChanged(first, end - first+1);
                }/*else if(newState == RecyclerView.SCROLL_STATE_DRAGGING){
                    mGalleryAdapter.enableRefresh(true);
                }else if(newState == RecyclerView.SCROLL_STATE_SETTLING){
                    mGalleryAdapter.enableRefresh(true);
                }*/
            }




            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                if(mGalleryAdapter == null){//
                    return;
                }
                int ydistance = 0;
                if(dy>0){
                    ydistance = dy;
                }else{
                    ydistance = 0-dy;
                }
                int dpDistance = AndroidUtil.px2dip(mContext,ydistance);
                if(dpDistance>60){
                    mGalleryAdapter.enableRefresh(false);
                }else{
                    mGalleryAdapter.enableRefresh(true);
                }

            }
        });
        showProcess();
        new RxAsyncHelper<>("").runInThread(new Func1<String, Object>() {
            @Override
            public Object call(String s) {
                if(mPresenter == null){
                    return null;
                }
                mPresenter.getMediaList();
                arrangementList();
                return null;
            }
        }).runOnMainThread(new Func1<Object, Object>() {

            @Override
            public Object call(Object o) {
                if(getActivity() == null){
                    return null;
                }
                hideProcess();
                setAlbum( 0 ) ;
                mButtonSend.setOnClickListener(new View.OnClickListener() {  // 发送逻辑
                    @Override
                    public void onClick(View v) {
                        mPresenter.handleSend(v);
                    }

                });

                mTvPreview.setOnClickListener(new View.OnClickListener() {  // 图片预览
                    @Override
                    public void onClick(View v) {
                        mPresenter.handlePreviewClicked(v);
                    }
                });

                mCbOriginalPhoto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { //原图
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mPresenter.handlePhotoModeChange(isChecked);
                    }
                });
                mAlbumAdapter = new AlbumAdapter(getActivity() , mMediaSetList, selectList, 1);
                selectList.setAdapter(mAlbumAdapter);

                animationSetIn = (AnimationSet) AnimationUtils.loadAnimation(getActivity(), R.anim.anim_send_crad_in);
                animationSetOut = (AnimationSet) AnimationUtils.loadAnimation(getActivity(), R.anim.anim_send_crad_out);
                return null;
            }
        }).subscribe();


    }

    private void arrangementList(){
        if(GalleryPresenter.mAllMediaItems == null  || GalleryPresenter.mAllMediaItems.size() <=0 ){
            return;
        }

        MediaSet videoMediaSet = new MediaSet();
        videoMediaSet.setMediaSetName(getString(R.string.video));
        for (MediaItem item : GalleryPresenter.mAllMediaItems ) {
            if (item.getMediaType() == MediaItem.MEDIA_TYPE_VIDEO || item.getMediaType() == MediaItem.MEDIA_TYPE_IMAGE) {
                if (item.getMediaType() == MediaItem.MEDIA_TYPE_VIDEO) {
                    videoMediaSet.addMediaItem(item);
                } else if (!mGroupMap.containsKey(item.getBucketDisplayName())) {
                    MediaSet mediaSet = new MediaSet();
                    mediaSet.setMediaSetName(item.getBucketDisplayName());
                    mediaSet.addMediaItem(item);
                    mMediaSetList.add(mediaSet);
                    mGroupMap.put(item.getBucketDisplayName(), mediaSet);
                } else {
                    mGroupMap.get(item.getBucketDisplayName()).addMediaItem(item);
                }
            }
        }

        MediaSet allSet = new MediaSet(GalleryPresenter.mAllMediaItems);
        allSet.setMediaSetName(getString(R.string.all_photos));
        if (videoMediaSet.getCount() > 0) {
            mMediaSetList.add(0, videoMediaSet);
        }
        mMediaSetList.add(0, allSet); // 所有照片放在第一个

        for (MediaSet set : mMediaSetList) {
            if (set.getMediaList().size() > 0) {
                set.setMediaSetPath(set.getMediaList().get(0).getLocalPath());
            }
            set.setNum(set.getMediaList().size());
        }
    }

    protected void setAlbum(int i) {
        selectAlbumPosition = i;
        if(selectAlbumPosition>=0 && selectAlbumPosition<mMediaSetList.size()){
            ArrayList<MediaItem> arrayList = mMediaSetList.get(selectAlbumPosition).getMediaList() ;
            mPresenter.setSelectMediaList(arrayList); // 设置当前选中的图片文件夹
            if(mGalleryAdapter == null){
                mGalleryAdapter = new GalleryAdapter(getActivity());
                mGalleryAdapter.setListener(new GalleryAdapter.Listener() {
                    @Override
                    public void onClick(View v, int position) {
                        int i = v.getId();
                        if (i == R.id.iv_gallery) {  // 预览
                            mPresenter.handleImageClicked(v, position);
                        } else if (i == R.id.rliv_select || i == R.id.iv_select ) { // 选择
                            mPresenter.handleSelect(position);
                        }
                    }
                });
                mRecyclerViewGallery.setAdapter(mGalleryAdapter);
            }
            mGalleryAdapter.setmAllMediaItemsClear(arrayList);
            selectName.setText(formatString(mMediaSetList.get(selectAlbumPosition).getMediaSetName())); // 选中的标题
        }
    }

    private String formatString(String stringToFormat) {
        if (stringToFormat.length() > STR_MAX_CHAR_COUNT) {
            stringToFormat = stringToFormat.substring(0, STR_MAX_CHAR_COUNT) + "...";
        }
        return stringToFormat;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_gallery;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        return rootView;
    }

    @Override
    public void updateButton(int number) {
        if (number == 0) {
            mButtonSend.setText(getString(R.string.send_));
            mButtonSend.setEnabled(false);
            mTvPreview.setEnabled(false);
            int colorWhite = getActivity().getResources().getColor(R.color.color_ffffff);
            int colorPreview = getActivity().getResources().getColor(R.color.color_ffd5d5d5);
            mTvPreview.setTextColor(colorPreview);
            mButtonSend.setTextColor(colorWhite);
        } else {
            mButtonSend.setText(getString(R.string.send_)+"(" + number + ")");
            mButtonSend.setEnabled(true);
            mTvPreview.setEnabled(true);
            mTvPreview.setTextColor(0xFF2a2a2a);
            mButtonSend.setTextColor(getActivity().getResources().getColor(R.color.color_ffffff));
        }
    }

    /**
     * 显示处理怎在压缩图片提示框
     */
    @Override
    public void showCompressProgressBar() {
        if (mProgressDialog == null){
            mProgressDialog = new HandlingProgressDialog(getActivity());
        }
        if (!mProgressDialog.isShowing()){
            mProgressDialog.show();
        }

    }

    @Override
    public void updateView() {
        //局部刷新可视items
        GridLayoutManager gl = (GridLayoutManager) mRecyclerViewGallery.getLayoutManager();
        int first = gl.findFirstVisibleItemPosition();
        int length = gl.findLastVisibleItemPosition() - first;
        mGalleryAdapter.notifyItemRangeChanged(first, length + 1);
        updateButton(mPresenter.getSelectItems().size());
    }

    @Override
    public void initToolBar() {
        activity = (GalleryActivity) getActivity();
        activity.findViewById(R.id.select_picture_custom_toolbar_back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.finish();
            }
        });
        activity.findViewById(R.id.left_back).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });

        selectName = (TextView) activity.findViewById(R.id.select_picture_custom_toolbar_title_text);
        dropDownImage = (ImageView) activity.findViewById(R.id.drop_down_image);
        selectName.setOnClickListener(this);
        activity.findViewById(R.id.select_rl).setOnClickListener(this);

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        MetYouActivityManager.getInstance().setForceZomProcess(false);
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -2 && data !=null) {
            if(mPresenter != null){
                mPresenter.handlePhotoModeChange(data.getBooleanExtra(GalleryPresenter.ORIGIN_PHOTO, false));
                mPresenter.handleSend(null);
            }
        } else if (resultCode == ImgEditorProxy.g.getServiceInterface().getFinalRequestEditPictureStatus()) {
            int imgEditorStatus = data.getIntExtra(ImgEditorProxy.g.getServiceInterface().getFinalImageStatus(), -1);
            if (imgEditorStatus == ImgEditorProxy.g.getServiceInterface().getFinalSendImageStatus()) {
                saveOrSendImg(data);
            }
        } else if (data != null) {
            boolean state = data.getBooleanExtra(GalleryPresenter.ORIGIN_PHOTO, false);
            mCbOriginalPhoto.setChecked(state);
            updateView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //小心并发修改
        //        updateView();
        //        mGalleryAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        if(mPresenter != null){
            mPresenter.onDestroy();
        }
        super.onDestroy();
    }

    /**
     * listView 的itme点击监听
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        setAlbum(position);
        if(selectList!=null && selectList.getVisibility() == View.GONE){
            dropDownImage.setBackgroundResource(R.drawable.cc_work_titlebar_up);
            selectList.startAnimation(animationSetIn);
            selectList.setVisibility(View.VISIBLE);
        }else if(selectList!=null && selectList.getVisibility() == View.VISIBLE){
            dropDownImage.setBackgroundResource(R.drawable.cc_work_titlebar_down);
            selectList.startAnimation(animationSetOut);
            selectList.setVisibility(View.GONE);

        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId() ;
        if(id == R.id.select_rl || id == R.id.select_picture_custom_toolbar_title_text ){
            if(selectList!=null && selectList.getVisibility() == View.GONE && animationSetIn!=null){
                dropDownImage.setBackgroundResource(R.drawable.cc_work_titlebar_up);
                selectList.startAnimation(animationSetIn);
                selectList.setVisibility(View.VISIBLE);
            }else if(selectList!=null && selectList.getVisibility() == View.VISIBLE){
                dropDownImage.setBackgroundResource(R.drawable.cc_work_titlebar_down);
                selectList.startAnimation(animationSetOut);
                selectList.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 保存或者发送编辑图片
     * @param data
     * @param
     */
    private void saveOrSendImg(Intent data) {
        int imgEditorStatus = data.getIntExtra(ImgEditorProxy.g.getServiceInterface().getFinalImageStatus(), -1);
        if (imgEditorStatus == ImgEditorProxy.g.getServiceInterface().getFinalSendImageStatus()) {
            activity.setResult(ImgEditorProxy.g.getServiceInterface().getFinalRequestEditPictureStatus(), data);
            getActivity().finish();
        }
    }

    private void showProcess() {
        if (waittingImg != null) {
            waittingImg.setVisibility(View.VISIBLE);
            waittingImg.startAnimation(animation);
        }
    }

    private void hideProcess() {
        if (waittingImg != null) {
            waittingImg.clearAnimation();
            waittingImg.setVisibility(View.GONE);
        }
    }
}

