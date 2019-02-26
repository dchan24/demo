package com.cmicc.module_message.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Files.FileColumns;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.ToastUtils;
import com.chinamobile.app.utils.RxAsyncHelper;
import com.chinamobile.app.utils.StringUtil;
import com.chinamobile.app.yuliao_common.utils.FileUtil;
import com.chinamobile.app.yuliao_common.utils.PopWindowFor10GUtil;
import com.chinamobile.app.yuliao_common.view.PopWindowFor10G;
import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmcc.cmrcs.android.ui.adapter.ChooseFileImgVedioMusicAdapter;
import com.cmcc.cmrcs.android.ui.dialogs.PermissionDeniedDialog;
import com.cmcc.cmrcs.android.ui.fragments.BaseFragment;
import com.cmcc.cmrcs.android.ui.model.ImgVedioAudioModel;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.activity.ChooseLocalFileActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Created by tigger on 2017/7/3.
 */

public class ChooseFileImgVedioMusicFragment extends BaseFragment implements OnClickListener, OnItemClickListener {

    private static final String TAG = "ChooseFileImgVedioMusicFragment";
    private final static int SCAN_OK = 1;

    ListView mList;
    TextView mTvEmpty;

    private int mMediaType;
    private View mRootView;
    //private ActionBar mActionBar;
    private TextView mTvSend;
    private TextView mTvSlectFileSize;
    private PopWindowFor10G m10GPopWindow;
    private View m10GDropView;
    private ChooseFileImgVedioMusicAdapter mAdapter;

    private List<ImgVedioAudioModel> mMediaList = new ArrayList<ImgVedioAudioModel>();

    private MyTask mTask;

    @Override
    public void init() {
        mMediaType = getArguments().getInt("type", ImgVedioAudioModel.MEDIA_TYPE_IMAGE);
    }

    public void initViews(View rootView) {
        super.initViews(rootView);
        mList = (ListView) rootView.findViewById(R.id.lv_choose);
        mTvEmpty = (TextView) rootView.findViewById(R.id.tv_empty);
        m10GPopWindow = new PopWindowFor10G(getActivity());
    }

    @Override
    public View getLayoutView(LayoutInflater inflate, ViewGroup container) {
        if (mRootView == null) {
            mRootView = inflate.inflate(R.layout.fragment_choose_sd_layout, container, false);
        }
        ViewGroup parent = (ViewGroup) mRootView.getParent();
        if (parent != null) {
            parent.removeView(mRootView);
        }
        return mRootView;
    }

    @Override
    public void initData() {
        initView();
        mAdapter = new ChooseFileImgVedioMusicAdapter(getActivity(), mMediaList, mMediaType);
        mMediaList.clear();
        mAdapter.notifyDataSetChanged();

        ((BaseActivity) getActivity()).requestPermissions(new BaseActivity.OnPermissionResultListener() {

            @Override
            public void onAllGranted() {
                super.onAllGranted();
                getDataList();
            }

            @Override
            public void onAnyDenied(String[] permissions) {
                BaseToast.makeText(getActivity(), getString(R.string.need_sd_permission), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAlwaysDenied(String[] permissions) {
                String message = getString(R.string.need_sd_permission);
                PermissionDeniedDialog permissionDeniedDialog = new PermissionDeniedDialog(getActivity(), message);
                permissionDeniedDialog.show();
            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public void initView() {
        intActionBar();
        mList.setOnItemClickListener(this);

    }

    private void intActionBar() {
        mTvSend = (TextView) ((AppCompatActivity) getActivity()).findViewById(R.id.button_send);
        mTvSlectFileSize = (TextView) ((AppCompatActivity) getActivity()).findViewById(R.id.textview_select_file_size);
        m10GDropView = ((AppCompatActivity) getActivity()).findViewById(R.id.pop_10g_window_drop_view);
        //    mActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        int titleID = R.string.app_name;
        switch (mMediaType) {
            case ImgVedioAudioModel.MEDIA_TYPE_IMAGE:
                titleID = R.string.conversation_file_title_msg;
                break;
            case ImgVedioAudioModel.MEDIA_TYPE_MUSIC:
                titleID = R.string.conversation_file_title_music;
                break;
            case ImgVedioAudioModel.MEDIA_TYPE_VIDEO:
                titleID = R.string.conversation_file_title_vedio;
                break;
            case ImgVedioAudioModel.MEDIA_TYPE_DOC:
                titleID = R.string.conversation_file_title_doc;
                break;
            default:
                break;
        }
        // mActionBar.setTitle(titleID);
        setToolBarTitle(getActivity().getResources().getString(titleID));
        ((ViewGroup) mTvSend.getParent()).setVisibility(View.VISIBLE);
        mTvSend.setEnabled(false);
        mTvSend.setOnClickListener(this);
        mTvSlectFileSize.setText(mContext.getText(R.string.selected) + ": 0B");
        mTvSlectFileSize.setVisibility(View.INVISIBLE);

    }

    private void setToolBarTitle(String title) {
        if (title == null) return;
        Activity activity = getActivity();
        if (activity instanceof ChooseLocalFileActivity) {
            ((ChooseLocalFileActivity) activity).setToolBarTitle(title);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTask != null) {
            mTask.cancel(true);
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_choose_sd_layout;
    }

    private void getDataList() {
        mTask = new MyTask();
        mTask.executeOnExecutor(RxAsyncHelper.getCacheThreadPool());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.button_send) {
            final ImgVedioAudioModel item = mAdapter.getSelect();
            // =====================start 通话中发送文件处理 added && modified by kgb=============================================//
            final ChooseLocalFileActivity sendActivity = (ChooseLocalFileActivity) getActivity();
            if (sendActivity instanceof ChooseLocalFileActivity) {
//                if (mMediaType == ImgVedioAudioModel.MEDIA_TYPE_IMAGE) {
//                    YoushuUtil.onEventVersionNew(baseActivity,YoushuUtil.BUSSINESS_MSG ,"000170");
//                }else if (mMediaType == ImgVedioAudioModel.MEDIA_TYPE_MUSIC) {
//                    YoushuUtil.onEventVersionNew(baseActivity,YoushuUtil.BUSSINESS_MSG ,"000173");
//                }else if (mMediaType == ImgVedioAudioModel.MEDIA_TYPE_VIDEO) {
//                    YoushuUtil.onEventVersionNew(baseActivity,YoushuUtil.BUSSINESS_MSG ,"000176");
//                }
                if (TextUtils.equals(sendActivity.getSendFileAction(), "callandsendfile")) {
//                    boolean isWifiConnected = AndroidUtil.isNetworkConnectedByWifi(sendActivity);
//                    final UICallLogic uiCallLogic = UICallLogic.getInstence();
//
//                    if(!isWifiConnected){//判断非wifi环境下进行提示
//                        DeleteOprationDialog deleteDialog = new DeleteOprationDialog(sendActivity, "提醒", "确定在非WIFI环境下发送？");
//                        deleteDialog.show();
//                        deleteDialog.setOnDeleteClickListener(new DeleteOprationDialog.OnDeleteClickListener() {
//
//                            @Override
//                            public void onClick() {
//                                uiCallLogic.sendFileAndInsert(getActivity(), Uri.parse("file://" + item.getFilepath()), sendActivity.getAddress());
//                                finishOrReturn(sendActivity);
//                                sendActivity.finish();
//                            }
//                        });
//                        return;
//                    }
//                    uiCallLogic.sendFileAndInsert(getActivity(), Uri.parse("file://" + item.getFilepath()), sendActivity.getAddress());
//                    finishOrReturn(sendActivity);
                } else {
                    long fileLength = new File(item.getFilepath()).length();
                    float fileSize_M = fileLength / 1024 / 1024;
                    if (PopWindowFor10GUtil.isNeedPop() && fileSize_M > 2.0f) {
                        if (mMediaType == ImgVedioAudioModel.MEDIA_TYPE_VIDEO) {
                            m10GPopWindow.setType(PopWindowFor10GUtil.TYPE_FOR_SEND_VIDEO_FILE);
                        } else {
                            m10GPopWindow.setType(PopWindowFor10GUtil.TYPE_FOR_SEND_FILE);
                        }
                        m10GPopWindow.setSendFile(Uri.parse("file://" + item.getFilepath()), fileLength);
                        m10GPopWindow.showAsDropDown(m10GDropView, 0, 0);
                        return;
                    }
                    getActivity().setResult(Activity.RESULT_OK, getActivity().getIntent().setData(Uri.parse("file://" + item.getFilepath())));
                }
            }
            // =====================ended 通话中发送文件处理 added && modified by kgb=============================================//
        }
        getActivity().finish();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final ImgVedioAudioModel data = mAdapter.getItem(position);
        final File f = new File(data.getFilepath());

        boolean isBigImg = mMediaType == ImgVedioAudioModel.MEDIA_TYPE_IMAGE
                            && FileUtil.isBigImageFile(f.getPath());

        if (mAdapter.getTemp() == position || isBigImg) {
            mAdapter.select(-1);
            sureBtnState(false);
            mAdapter.notifyDataSetChanged();
            mTvSlectFileSize.setText(mContext.getText(R.string.selected) + ": 0B");
            mTvSlectFileSize.setVisibility(View.INVISIBLE);
            if(isBigImg) {
                ToastUtils.showShort(R.string.big_img_unsupport);
            }
        } else {
            sureBtnState(true);
            mAdapter.select(position);
            if (f!=null && f.exists()) {
                mTvSlectFileSize.setText(mContext.getText(R.string.selected) + ": " + StringUtil.formetFileSize(f.length()));
                mTvSlectFileSize.setVisibility(View.VISIBLE);
            } else {
                mTvSlectFileSize.setText(mContext.getText(R.string.selected) + ": 0B");
                mTvSlectFileSize.setVisibility(View.INVISIBLE);

            }
        }
    }

    private void sureBtnState(boolean isCheck) {
        mTvSend.setEnabled(isCheck);
    }

    private class MyTask extends AsyncTask<Void, Void, List<ImgVedioAudioModel>> {

        @Override
        protected List<ImgVedioAudioModel> doInBackground(Void... params) {
            List<ImgVedioAudioModel> tempMediaList = new ArrayList<ImgVedioAudioModel>();
            Uri mMediaItemUri = getMediaUri(mMediaType);
            String select = null;
            if (mMediaType == ImgVedioAudioModel.MEDIA_TYPE_DOC)
                select = "(" + FileColumns.DATA + " LIKE '%.doc'" + " or " + FileColumns.DATA + " LIKE '%.docx'" + " or " + FileColumns.DATA + " LIKE '%.xls'" + " or " + FileColumns.DATA + " LIKE '%.xlsx'" + " or " + FileColumns.DATA + " LIKE '%.ppt'" + " or " + FileColumns.DATA + " LIKE '%.txt'" + " or " + FileColumns.DATA + " LIKE '%.pdf'" + ")";
            ContentResolver mContentResolver = getActivity().getContentResolver();
            Cursor mCursor = mContentResolver.query(mMediaItemUri, null, select, null, MediaStore.Images.Media.DISPLAY_NAME);
            if (mCursor != null) {
                while (mCursor.moveToNext()) {
                    ImgVedioAudioModel item = new ImgVedioAudioModel();
                    String path = null;
                    String name = null;
                    if (mMediaType == ImgVedioAudioModel.MEDIA_TYPE_IMAGE) {
                        path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                        name = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
                    } else if (mMediaType == ImgVedioAudioModel.MEDIA_TYPE_MUSIC) {
                        path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                        name = mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                    } else if (mMediaType == ImgVedioAudioModel.MEDIA_TYPE_VIDEO) {
                        path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Video.Media.DATA));
                        name = mCursor.getString(mCursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
                    } else {
                        path = mCursor.getString(mCursor.getColumnIndex(FileColumns.DATA));
                        name = mCursor.getString(mCursor.getColumnIndex(FileColumns.DISPLAY_NAME));
                    }
//					File file = new File(path);
//					if (!file.exists() || file.length() <= 0) {
//						// 过滤不存在 或大小为0的文件
//						continue;
//					}

                    if(TextUtils.isEmpty(path)){//某些机型(Nubia z7)会出现 path为空的问题
                        continue;
                    }
                    if (TextUtils.isEmpty(name)) {
                        name = FileUtil.getFileName(path);
                    }
                    if (mMediaType == ImgVedioAudioModel.MEDIA_TYPE_VIDEO && name.startsWith("thumbnails")) {
                        //视频文件夹中不显示thumbnails开头的文件
                    } else {
                        File f = new File(path);
                        if (!f.exists()) { //如果文件不存在， 则过滤
                            continue;
                        } else if (f.exists() && f.length() <= 0) {//如果文件大小为0， 则过滤
                            continue;
                        } else {
                            item.setFilepath(path);
                            item.setFilename(name);
                            tempMediaList.add(item);
                        }
                    }
                }
            }
            mCursor.close();

            if (mMediaType == ImgVedioAudioModel.MEDIA_TYPE_VIDEO || mMediaType == ImgVedioAudioModel.MEDIA_TYPE_IMAGE) {
                Collections.sort(tempMediaList, new Comparator<ImgVedioAudioModel>() {
                    @Override
                    public int compare(ImgVedioAudioModel o1, ImgVedioAudioModel o2) {
                        String o1Filepath = o1.getFilepath();
                        if (!TextUtils.isEmpty(o1Filepath)) {
                            File file1 = new File(o1Filepath);
                            if (file1 != null && file1.exists()) {
                                long lastModified1 = file1.lastModified();
                                if (lastModified1 != 0L) {
                                    String o2Filepath = o2.getFilepath();
                                    if (!TextUtils.isEmpty(o2Filepath)) {
                                        File file2 = new File(o2Filepath);
                                        if (file2 != null && file2.exists()) {
                                            long lastModified2 = file2.lastModified();
                                            if (lastModified2 != 0L) {
                                                if (lastModified1 == lastModified2) {
                                                    return 0;
                                                }
                                                return lastModified1 < lastModified2 ? 1 : -1;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        return 0;
                    }
                });
            }
            return tempMediaList;
        }

        @Override
        protected void onPostExecute(List<ImgVedioAudioModel> result) {
            super.onPostExecute(result);
            if (getActivity() == null || mList == null) {
                return;
            }
            mMediaList = result;
            mAdapter = new ChooseFileImgVedioMusicAdapter(getActivity(), mMediaList, mMediaType);
            mList.setAdapter(mAdapter);
            sureBtnState(false);

            if (mMediaList.size() <= 0) {
                mTvEmpty.setVisibility(View.VISIBLE);
            }
        }

    }

    private Uri getMediaUri(int mediaType) {
        switch (mediaType) {
            case ImgVedioAudioModel.MEDIA_TYPE_IMAGE:
                // 图片路径URI
                return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            case ImgVedioAudioModel.MEDIA_TYPE_VIDEO:
                // 视频URI
                return MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            case ImgVedioAudioModel.MEDIA_TYPE_MUSIC:
                // 音乐URI
                return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            case ImgVedioAudioModel.MEDIA_TYPE_DOC:
                // 文稿URI
                return MediaStore.Files.getContentUri("external");
            default:
                break;
        }
        return null;
    }

    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == SCAN_OK) {
                if (getActivity() == null) {
                    return;
                }
                mMediaList = (List<ImgVedioAudioModel>) msg.obj;
                mAdapter = new ChooseFileImgVedioMusicAdapter(getActivity(), mMediaList, mMediaType);
                mList.setAdapter(mAdapter);
                sureBtnState(false);
            }
        }

    };
}
