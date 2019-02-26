package com.cmicc.module_message.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.module.proxys.moduleimgeditor.ImgEditorProxy;
import com.chinamobile.app.utils.ImageUtils;
import com.chinamobile.app.utils.RxAsyncHelper;
import com.chinamobile.app.yuliao_common.utils.FileUtil;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_common.utils.PopWindowFor10GUtil;
import com.chinamobile.app.yuliao_common.view.PopWindowFor10G;
import com.chinamobile.app.yuliao_core.util.TimeUtil;
import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmcc.cmrcs.android.ui.model.MediaItem;
import com.cmicc.module_message.ui.presenter.PreviewImagePresenter;
import com.cmcc.cmrcs.android.ui.utils.media.OrientationManager;
import com.cmcc.cmrcs.android.ui.utils.media.play.MVideoPlayListener;
import com.cmcc.cmrcs.android.ui.utils.media.play.MVideoPlayer;
import com.cmcc.cmrcs.android.ui.utils.media.play.MVideoPlayerSurfaceView;
import com.cmcc.cmrcs.android.ui.utils.media.record.MVideoRecordListener;
import com.cmcc.cmrcs.android.ui.utils.media.record.MVideoRecorder;
import com.cmcc.cmrcs.android.ui.utils.media.record.MVideoRecorderSurfaceView;
import com.cmicc.module_message.R;
import com.constvalue.MessageModuleConst;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import rx.functions.Func1;


/**
 * Created by yangshaowei on 2017/11/20.
 * 录像界面
 */
public class VideoRecordActivity extends BaseActivity implements OnClickListener, MVideoPlayListener, MVideoRecordListener, OnTouchListener {

    private static final String TAG = "VideoRecordActivity";

    /**
     * 录制
     */
    private Button mRecord;
    /**
     * 播放
     */
    private Button mPlay;
    /**
     * 切换摄像头
     */
    private ImageView mChangeCamera;
    /**
     * 重录
     */
    private ImageView mReRecord;
    /**
     * 录制底部控件父布局
     */
    private View mRecordParent;
    /**
     * 播放底部控件父布局
     */
    private View mPlayParent;

    /**
     * 底部编辑布局
     */
    private View mImgEdit;

    private PopWindowFor10G mPopWindowFor10G;
    /**
     * 当前状态
     */
    private int mStatus;
    /**
     * 录制展示图
     */
    private MVideoRecorderSurfaceView mRecordSurfaceView;
    /**
     * 播放
     */
    private MVideoPlayerSurfaceView mPlaySurfaceView;
    private OrientationManager orientationManager;
    /**
     * 图片预览
     **/
    private ImageView mPicPre;

    private ProgressBar mPbTimer;

    private TextView mRecordingTime;

    private RelativeLayout mRlBack;

    private View mRedDotVideo;
    private int MAX_TIME = 60000;

    /**
     * 状态
     */
    private static final int STATUS_RECORD_PRE = 1;// 准备录制
    private static final int STATUS_RECORDING = 2;// 录制中
    private static final int STATUS_PLAY_PRE = 3;// 准备播放
    private static final int STATUS_PLAYING = 4;// 播放中
    private static final int STATUS_PAUSE = 5;// 暂停中
    private static final int STATUS_PHOTO_PRE = 6;// 视频预览
    private static final int STATUS_PIC_PRE = 7;// 图像预览

    private String recordFilePath;
    private int recordTime;
    private View bottomLayout;
    private MVideoPlayer player;
    private MVideoRecorder recorder;

    private CountDownTimer timer;
    private ImageView mRecordBack;
    private Vibrator vibrator;
    OrientationManager.ScreenOrientation mScreenOrientation;

    private LongPressRunnable longPressRunnable;
    Handler handler;
    private boolean isVideo = false;
    private int mSpeed = 0;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseWakeLock();
        if (recorder != null) {
            recorder.stopPreView();
        }
        if (orientationManager != null) {
            orientationManager.disable();
        }
    }

    private int orientation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_activity_video_record);
        longPressRunnable = new LongPressRunnable();
        handler = new Handler();
        orientationManager = new OrientationManager(this, SensorManager.SENSOR_DELAY_NORMAL, new OrientationManager.OrientationListener() {

            @Override
            public void onOrientationChange(OrientationManager.ScreenOrientation screenOrientation) {
                mScreenOrientation = screenOrientation;
                switch (screenOrientation) {
                    case PORTRAIT:
                        orientation = 90;
                        break;
                    case REVERSED_PORTRAIT:
                        orientation = 270;
                        break;
                    case REVERSED_LANDSCAPE:
                        orientation = 180;
                        break;
                    case LANDSCAPE:
                        orientation = 0;
                        break;
                }
            }
        });
        orientationManager.enable();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public void setStateBarColor() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            Window window = this.getWindow();
            View decorView = window.getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }else {
            WindowManager.LayoutParams attrs = getWindow().getAttributes();
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(attrs);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    /**
     * 改变播放录制状态的视图显示
     */
    private void changeStatusLayout(int status) {
        mStatus = status;
        mRecordParent.setVisibility(View.GONE);
        mPlayParent.setVisibility(View.GONE);
        mPicPre.setVisibility(View.GONE);
        if (mStatus == STATUS_RECORD_PRE || mStatus == STATUS_RECORDING) {
            mRecordParent.setVisibility(View.VISIBLE);
            mRecordBack.setVisibility(View.VISIBLE);
            mChangeCamera.setVisibility(View.VISIBLE);
            if (mPlaySurfaceView.getVisibility() != View.GONE) {
                mPlaySurfaceView.setVisibility(View.GONE);
            }
            if (mRecordSurfaceView.getVisibility() != View.VISIBLE) {
                mRecordSurfaceView.setVisibility(View.VISIBLE);
            }
            if (mStatus == STATUS_RECORD_PRE) {
                mChangeCamera.setEnabled(true);
                mRecordBack.setEnabled(true);
                mRecordingTime.setVisibility(View.VISIBLE);
                mRecordingTime.setText(getString(R.string.take_photo_video));
            } else if (mStatus == STATUS_RECORDING) {
                mChangeCamera.setEnabled(false);
                mRecordBack.setEnabled(false);
                mRecordingTime.setVisibility(View.VISIBLE);
                mRecord.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_video_n));
            }
        } else if (mStatus == STATUS_PHOTO_PRE) {
            if (mPlaySurfaceView.getVisibility() != View.VISIBLE) {
                mPlaySurfaceView.setVisibility(View.VISIBLE);
            }
            if (mRecordSurfaceView.getVisibility() != View.GONE) {
                mRecordSurfaceView.setVisibility(View.GONE);
            }
            mChangeCamera.setEnabled(false);
            mRecordBack.setEnabled(true);
            mPlayParent.setVisibility(View.VISIBLE);
            mImgEdit.setVisibility(View.VISIBLE);
            mPbTimer.setVisibility(View.GONE);
            mPlay.setVisibility(View.GONE);
            mRecordingTime.setVisibility(View.INVISIBLE);
            mChangeCamera.setVisibility(View.INVISIBLE);
        } else if (mStatus == STATUS_PIC_PRE) {
            mChangeCamera.setEnabled(false);
            mRecordBack.setEnabled(true);
            mPlayParent.setVisibility(View.VISIBLE);
            mImgEdit.setVisibility(View.VISIBLE);
            mPbTimer.setVisibility(View.GONE);
            mPlay.setVisibility(View.GONE);
            mRecordingTime.setVisibility(View.INVISIBLE);
            mChangeCamera.setVisibility(View.INVISIBLE);
            mPicPre.setVisibility(View.GONE);
        } else {
            //录制结束
            mPlayParent.setVisibility(View.VISIBLE);
            mPbTimer.setVisibility(View.GONE);
            mRecordingTime.setVisibility(View.INVISIBLE);
            mChangeCamera.setVisibility(View.INVISIBLE);
            if (mRecordSurfaceView.getVisibility() != View.GONE) {
                mRecordSurfaceView.setVisibility(View.GONE);
            }
            if (mPlaySurfaceView.getVisibility() != View.VISIBLE) {
                mPlaySurfaceView.setPath(recordFilePath);
                mPlaySurfaceView.setVisibility(View.VISIBLE);
            }
            if (mStatus == STATUS_PLAY_PRE || mStatus == STATUS_PAUSE) {
                mReRecord.setEnabled(true);
                mPlay.setVisibility(View.GONE);
            } else if (mStatus == STATUS_PLAYING) {
                mReRecord.setEnabled(true);
                mPlay.setVisibility(View.GONE);
            }
        }

    }

    /**
     * 初始化监听
     */
    private void initListener() {

    }


    /**
     *  重新录制视频
     *  <br/> 包括ui更新和 录制逻辑
     *  <br/>  主线程函数
     */
    private void reRecordListenerJob(){
        reRecord();
        mRecordBack.setVisibility(View.VISIBLE);
        mChangeCamera.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.send) {
            // 发送
            if (isVideo) {
                sendVideo();
            } else {
                sendPhoto();
            }

        } else if (id == R.id.rerecord) {
            reRecordListenerJob();
        } else if (id == R.id.play) {
            handleAction();
        } else if (v.getId() == R.id.action_bar_change_camera_bt) {
            int cameraCount = 0;
            CameraInfo cameraInfo = new CameraInfo();
            cameraCount = Camera.getNumberOfCameras();// 得到摄像头的个数
            Camera.getCameraInfo(cameraPosition, cameraInfo);// 得到每一个摄像头的信息
            int facing = cameraInfo.facing;
            for (int i = 0; i < cameraCount; i++) {
                Camera.getCameraInfo(i, cameraInfo);// 得到每一个摄像头的信息
                if (facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    // 现在是后置，变更为前置
                    if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {// 代表摄像头的方位，CAMERA_FACING_FRONT前置 CAMERA_FACING_BACK后置
                        cameraPosition = i;
                        break;
                    }
                } else {
                    // 现在是前置， 变更为后置
                    if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {// 代表摄像头的方位，CAMERA_FACING_FRONT前置 CAMERA_FACING_BACK后置
                        cameraPosition = i;
                        break;
                    }
                }
            }
            mRecordSurfaceView.changeCamera(cameraPosition);
            recorder.changeCamera(90, cameraPosition);
        } else if (id == R.id.action_bar_back || id == R.id.rl_back) {
            finish();
        } else if(id == R.id.img_edit){
            File file = new File(recordFilePath);
            Uri uri = Uri.fromFile(file);
            String fileName = String.valueOf(System.currentTimeMillis()) + ImgEditorProxy.g.getServiceInterface().getFinalFileNameExtensionMessageImage();
            File mCameraPicture = new File(FileUtil.getSaveDir(),fileName);
            FileUtil.createParentDir(mCameraPicture);
            String mOutputFilePath = mCameraPicture.getAbsolutePath();
            ImgEditorProxy.g.getUiInterface().goPictureEditActivity(VideoRecordActivity.this, uri, mOutputFilePath, MessageModuleConst.PreviewImagePresenterConst.FROM_VIDEO_RECORD_ACTIVITY);
        }

    }

    @Override
    protected void findViews() {
        View content = findViewById(R.id.fl_content);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && ViewCompat.getFitsSystemWindows(content)) {
            ViewCompat.setOnApplyWindowInsetsListener(content, new OnApplyWindowInsetsListener() {
                @Override
                public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                    return insets;
                }
            });
            content.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        mRecordParent = findViewById(R.id.record_parent);
        mPicPre = findViewById(R.id.picPre);
        mRecord = findViewById(R.id.record);
        mReRecord = findViewById(R.id.rerecord);
        mPlayParent = findViewById(R.id.play_parent);
        mImgEdit = findViewById(R.id.img_edit);
        mPlay = findViewById(R.id.play);
        mChangeCamera = findViewById(R.id.action_bar_change_camera_bt);
        mPbTimer = findViewById(R.id.pb_timer);
        mChangeCamera.setOnClickListener(this);
        mRecordSurfaceView = findViewById(R.id.recordSurfaceView);
        mRecordSurfaceView.init(this, cameraPosition);
        mPlaySurfaceView = findViewById(R.id.playSurfaceView);
        mRecordingTime = findViewById(R.id.action_bar_duration_recording_tv);
        mPlaySurfaceView.setOnTouchListener(this);
        bottomLayout = findViewById(R.id.bottomLayout);
        mRecordBack = findViewById(R.id.action_bar_back);
        mRecordBack.setOnClickListener(this);
        mRedDotVideo = findViewById(R.id.red_dot_video);
        mRlBack = findViewById(R.id.rl_back);
        mRlBack.setOnClickListener(this);

        mPopWindowFor10G = new PopWindowFor10G(VideoRecordActivity.this);

        player = mPlaySurfaceView.getPlayer();
        player.setPlayListener(this);
        recorder = mRecordSurfaceView.getRecorder();
        recorder.setRecordListener(this);
        timer = new CountDownTimer(MAX_TIME + 1000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
            }
        };
        initListener();
        changeStatusLayout(STATUS_RECORD_PRE);

        this.findViewById(R.id.send).setOnClickListener(this);
        mReRecord.setOnClickListener(this);
        mRecord.setOnClickListener(this);
        mRecord.setOnTouchListener(this);
        mPlay.setOnClickListener(this);
        mImgEdit.setOnClickListener(this);
    }

    @Override
    protected void init() {

    }

    /**
     * 重新拍摄视频
     */
    private void reRecord() {
        if (!(recordFilePath == null || recordFilePath.length() == 0)) {
            File file = new File(recordFilePath);
            if (file.exists()) {
                file.delete();
            }
        }
        if (isVideo) {
            changeStatusLayout(STATUS_RECORD_PRE);
        } else {
            mRecordSurfaceView.setVisibility(View.GONE);
            mRecordSurfaceView.setVisibility(View.VISIBLE);
            changeStatusLayout(STATUS_RECORD_PRE);
            recorder.startPreView(90, cameraPosition);
        }
    }

    @Override
    public void onBackPressed() {
        if (mStatus == STATUS_RECORD_PRE) {
            this.finish();
        } else if (mStatus == STATUS_RECORDING) {
            recorder.stopRecord();
            timer.cancel();
            return;
        } else if (mStatus == STATUS_PLAY_PRE) {
            reRecord();
            return;
        } else if (mStatus == STATUS_PLAYING) {//录制的视频正在循环播放的过程中， 按下后退键， 执行重新录制。
            player.stopPlay();  //停止播放
            reRecordListenerJob();//开始 重新录制
            return;
        } else if (mStatus == STATUS_PAUSE) {
            reRecord();
            return;
        } else if (mStatus == STATUS_PIC_PRE){
            reRecord();
            mRecordBack.setVisibility(View.VISIBLE);
            mChangeCamera.setVisibility(View.VISIBLE);
            return;
        }
        super.onBackPressed();
        return;
    }

    /**
     * 处理录制和播放按钮
     */
    private void handleAction() {
        if (mStatus == STATUS_RECORD_PRE) {
            acquireWakeLock();
            timer.start();
            recorder.startRecord(getRecordFilePath(), 90);
        } else if (mStatus == STATUS_RECORDING) {
            releaseWakeLock();
            recorder.stopRecord();
            timer.cancel();
        } else if (mStatus == STATUS_PLAY_PRE) {
            acquireWakeLock();
            player.startPlay();
        } else if (mStatus == STATUS_PLAYING) {
            releaseWakeLock();
            player.pause();
        } else if (mStatus == STATUS_PAUSE) {
            releaseWakeLock();
            player.startPlay();
        }
    }

    WakeLock wakeLock = null;

    // 获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
    private void acquireWakeLock() {
        if (null == wakeLock) {
            PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "PostLocationService");
            if (null != wakeLock) {
                wakeLock.acquire();
            }
        }
    }

    // 释放设备电源锁
    private void releaseWakeLock() {
        if (null != wakeLock) {
            wakeLock.release();
            wakeLock = null;
        }
    }

    private String getRecordFilePath() {
        String path = FileUtil.getPhotoDir();
        new File(path).mkdirs();
        return new File(path + "/photo_" + System.currentTimeMillis() + ".mp4").getAbsolutePath();
    }

    private String getPhotoPath() {
        String path = FileUtil.getPhotoDir();
        new File(path).mkdirs();
        return new File(path + "/IMG_" + System.currentTimeMillis() + ".jpg").getAbsolutePath();
    }

    /**
     * 发送视频
     */
    private void sendVideo() {
        final File file = new File(recordFilePath);
        final long fileLenth = file.length();
        if (fileLenth == 0 && recordTime == 0) {
            this.finish();
            return;
        }

        if(PopWindowFor10GUtil.isNeedPop()&& recordTime>=10){
            mPopWindowFor10G.setType(PopWindowFor10GUtil.TYPE_FOR_SEND_VIDEO);
            mPopWindowFor10G.setVideoRecordInfo(recordFilePath,fileLenth,recordTime);
            mPopWindowFor10G.showAsDropDown(mRecord,0,0);
            return;
        }
        Intent intent = new Intent();

        MediaItem item = new MediaItem(recordFilePath, MediaItem.MEDIA_TYPE_VIDEO);
        item.setDuration(recordTime);
        item.setFileLength(fileLenth);

        intent.putExtra(MessageModuleConst.GalleryActivityConst.KEY_MEDIA_SET, item);
        setResult(RESULT_OK, intent);
        VideoRecordActivity.this.finish();

    }

    /**
     * 发送图片
     */
    private void sendPhoto() {
        if(!isTakePic){
            final File file = new File(recordFilePath);
            final long fileLenth = file.length();
            if (fileLenth == 0 && recordTime == 0) {
                this.finish();
                return;
            }
            Intent intent = new Intent();
            MediaItem item = new MediaItem(recordFilePath, MediaItem.MEDIA_TYPE_IMAGE);
            intent.putExtra(MessageModuleConst.GalleryActivityConst.KEY_MEDIA_SET, item);
            setResult(RESULT_OK, intent);
            VideoRecordActivity.this.finish();
        }
    }

    private void sendPhoto(Intent data) {
        String mOutputFilePath = data.getStringExtra(ImgEditorProxy.g.getServiceInterface().getFinalExtraImageSavePath());
        if(!isTakePic){
            final File file = new File(mOutputFilePath);
            final long fileLenth = file.length();
            if (fileLenth == 0 && recordTime == 0) {
                this.finish();
                return;
            }
            Intent intent = new Intent();
            MediaItem item = new MediaItem(mOutputFilePath, MediaItem.MEDIA_TYPE_IMAGE);
            intent.putExtra(MessageModuleConst.GalleryActivityConst.KEY_MEDIA_SET, item);
            setResult(RESULT_OK, intent);
            VideoRecordActivity.this.finish();
        }
    }

    @Override
    public void onRecordPre() {
        changeStatusLayout(STATUS_RECORD_PRE);
        bottomLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRecordStart() {
        changeStatusLayout(STATUS_RECORDING);
    }

    @Override
    public void onRecordFinish(String filePath, int recordTime) {
        mImgEdit.setVisibility(View.GONE);
        recordFilePath = filePath;
        this.recordTime = recordTime;
        long fileLenth = new File(recordFilePath).length();
        if (fileLenth == 0 && recordTime > 0) {
            Toast.makeText(this, getResources().getString(R.string.take_photo_fail_need_permission), Toast.LENGTH_SHORT).show();
        }

        if (fileLenth > 0) {
            MediaPlayer mMediaPlayer = new MediaPlayer();
            FileInputStream fileInputStream = null;
            try {
                File file = new File(recordFilePath);
                if (file.exists()) {
                    fileInputStream = new FileInputStream(file);
                    FileDescriptor fd = fileInputStream.getFD();
                    mMediaPlayer.setDataSource(fd);
                    mMediaPlayer.prepare();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        LogF.d(TAG ,"fileLenth:"+fileLenth+",recordTime:"+recordTime);
        if (fileLenth > 0 && recordTime == 0) {
            Toast.makeText(this, getResources().getString(R.string.take_photo_fail), Toast.LENGTH_SHORT).show();
            recorder.stopPreView();
            recorder.startPreView(90, cameraPosition);
            return;
        }
        changeStatusLayout(STATUS_PLAY_PRE);
    }

    @Override
    public void onRecordTimeOut() {
        isVideo = true;
        handleAction();
    }

    @Override
    public void onPlayPre(int size) {
        changeStatusLayout(STATUS_PLAY_PRE);
        bottomLayout.setVisibility(View.VISIBLE);
        acquireWakeLock();
        player.startPlay();
    }

    @Override
    public void onPlayPause() {
        changeStatusLayout(STATUS_PAUSE);
    }

    @Override
    public void onPlayStop() {

    }

    @Override
    public void onPlayStart() {
        changeStatusLayout(STATUS_PLAYING);
    }

    private int cameraPosition;

    {
        int cameraCount = 0;
        CameraInfo cameraInfo = new CameraInfo();
        cameraCount = Camera.getNumberOfCameras();// 得到摄像头的个数
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, cameraInfo);// 得到每一个摄像头的信息
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {// 代表摄像头的方位，CAMERA_FACING_FRONT前置 CAMERA_FACING_BACK后置
                cameraPosition = i;
                break;
            }
        }
    }

    @Override
    public void onRecordStopError() {
        Toast.makeText(this, getResources().getString(R.string.take_photo_fail), Toast.LENGTH_SHORT).show();
        recorder.stopPreView();
        recorder.startPreView(90, cameraPosition);
    }

    @Override
    public void onPlayFinish() {
        changeStatusLayout(STATUS_PLAY_PRE);
        player.prePlay(recordFilePath);
    }

    @Override
    public void onRecording(int time) {
        if (STATUS_RECORD_PRE == mStatus) {
            return;
        }
        mRecordingTime.setText(TimeUtil.getHHMMSSTimeString(time));
    }

    public void onPlayError(Exception e) {

    }

    @Override
    protected void onPause() {
        if (mStatus == STATUS_RECORDING) {
            recorder.stopRecord();
        }
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ImgEditorProxy.g.getServiceInterface().getFinalRequestEditPictureStatus()) {
            if(data != null){
                int imgEditorStatus = data.getIntExtra(ImgEditorProxy.g.getServiceInterface().getFinalImageStatus(), -1);
                if (imgEditorStatus == ImgEditorProxy.g.getServiceInterface().getFinalSendImageStatus()) {
                    sendPhoto(data);
                }
            }
        }

    }

    @Override
    public void onPlaying(int position) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.record) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                //松手
                onKeyUp();
                mTimeFlag = false;
                mSpeed = 0 ;
                mPbTimer.setVisibility(View.GONE);
                mRedDotVideo.setVisibility(View.GONE);
                mRecord.setBackgroundDrawable(getResources().getDrawable(R.drawable.selector_take_photo_button));
            }
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                //按下
                mTimeFlag = true;
                handler.postDelayed(longPressRunnable, 500);    //同时延长1000启动长按后处理的逻辑Runnable
            }
        }
        return false;
    }

    //长按线程
    private class LongPressRunnable implements Runnable {
        @Override
        public void run() {
            Log.d("ysw", "LongPressRunnable1 ");
            handleAction();
            mHandler.sendEmptyMessage(2);
        }
    }

    private boolean isTakePic = false;
    private void onKeyUp() {
        Log.d("ysw", "onKeyUp1 ");
        if (mStatus == STATUS_RECORDING) {
            //结束录制
            isVideo = true;
            handleAction();
        } else if (recorder.getRecordTime() < recorder.MAX_RECORD_TIME) {
            //非录制状态,启动拍照
            isVideo = false;
            handler.removeCallbacks(longPressRunnable);  //取消长按录制
            if(!isTakePic){
                isTakePic = true;
                mRecord.setEnabled(false);
                mChangeCamera.setEnabled(false);
                mRecordBack.setEnabled(false);
                final int orientationTmp = orientation;
                final Camera camera = recorder.getCamera();
                if(camera != null){

                    Camera.Parameters parameters = camera.getParameters();
                    final Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                    Camera.getCameraInfo(cameraPosition, cameraInfo);
                    if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                        if(mScreenOrientation!=null) {
                            switch (mScreenOrientation) {
                                case PORTRAIT:
                                    parameters.set("rotation", orientationTmp + 180);
                                    break;
                                case REVERSED_PORTRAIT:
                                    parameters.set("rotation", 90);
                                    break;
                                case REVERSED_LANDSCAPE:
                                    parameters.set("rotation", orientationTmp);
                                    break;
                                case LANDSCAPE:
                                    parameters.set("rotation", orientationTmp);
                                    break;
                            }
                        }
                    }else {
                        parameters.set("rotation", orientationTmp);
                    }

                    WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                    int width = wm.getDefaultDisplay().getWidth();
                    int height = wm.getDefaultDisplay().getHeight();
                    List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
                    Camera.Size size = MVideoRecorder.getCloselyPreSize(width, height, sizes);
                    if (size!=null) {
                        parameters.setPictureSize(size.width, size.height);
                    } else {
                        parameters.setPictureSize(width,height);
                    }
                    camera.setParameters(parameters);

                    Log.d("ysw", "onKeyUp2");
                    //使用上一帧作为拍照图片
                    if(recorder.getPreviewFrameData() != null){
                        camera.stopPreview();
                        byte[] data = recorder.getPreviewFrameData();
                        final Matrix matrix = new Matrix();
                        matrix.postRotate(ImageUtils.getOrientation(data)+90);
                        new RxAsyncHelper<>(data).runInThread(new Func1<byte[], Bitmap>() {
                            @Override
                            public Bitmap call(byte[] data) {
                                data = recorder.getPreviewFrameData();
                                //处理data
                                Size previewSize = camera.getParameters().getPreviewSize();//获取尺寸,格式转换的时候要用到
                                Options newOpts = new Options();
                                newOpts.inJustDecodeBounds = true;
                                YuvImage yuvimage = new YuvImage(
                                        data,
                                        ImageFormat.NV21,
                                        previewSize.width,
                                        previewSize.height,
                                        null);
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                yuvimage.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 100, baos);// 80--JPG图片的质量[0-100],100最高
                                data  = baos.toByteArray();


                                if(cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT){
                                    matrix.postScale(-1, 1);  //镜头 镜像翻转
                                    Matrix saveMatrix = new Matrix(matrix);
                                    new TakePicThread(data, saveMatrix).start();  // 存储图片 和旋转矩阵到文件。

                                }else{
                                    new TakePicThread(data, matrix).start();  //存储图片数据到文件
                                }
                                //将rawImage转换成bitmap
                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inPreferredConfig = Bitmap.Config.RGB_565;
                                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);

//                                final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                                Bitmap toShow = Bitmap.createBitmap(bitmap, 0,0, bitmap.getWidth(), bitmap.getHeight(), matrix,true); //旋转图片为新图片
                                bitmap.recycle();
                                return toShow;
                            }
                        }).runOnMainThread(new Func1<Bitmap, Object>() {
                            @Override
                            public Object call(Bitmap toShow) {

                                mRecordSurfaceView.myDraw(toShow);

                                return null;
                            }
                        }).subscribe();

//                                matrix.postRotate(360+90-orientationTmp); // 旋转图像，使之与拍照时的屏幕相匹配


                        return;
                    }

                    try {
                        camera.takePicture(null, null,
                                new Camera.PictureCallback() {

                                    @Override
                                    public void onPictureTaken(byte[] data, final Camera camera) {
                                        Matrix matrix = new Matrix();
                                        matrix.postRotate(ImageUtils.getOrientation(data));

                                        if(cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
                                            matrix.postScale(-1, 1);  //镜头 镜像翻转
                                            Matrix saveMatrix = new Matrix(matrix);
                                            new TakePicThread(data, saveMatrix).start();  // 存储图片 和旋转矩阵到文件。

                                        }else{
                                            new TakePicThread(data, null).start();  //存储图片数据到文件
                                        }

                                        matrix.postRotate(360+90-orientationTmp); // 旋转图像，使之与拍照时的屏幕相匹配

                                        final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                                        Bitmap toShow = Bitmap.createBitmap(bitmap, 0,0, bitmap.getWidth(), bitmap.getHeight(), matrix,true); //旋转图片为新图片
                                        mRecordSurfaceView.myDraw(toShow);
                                        bitmap.recycle();

                                    }
                                });
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }

            }

        }
    }

    @Override
    public void onRecordPreError(Exception e) {
        LogF.d("ksbk", "onRecordPreError: "+e.toString());
        if (recorder != null) {
            recorder.stopPreView();
        }
        Toast.makeText(this, getString(R.string.get_camera_resource_fail), Toast.LENGTH_SHORT).show();
        this.finish();
    }


    public class TakePicThread extends Thread {

        byte[] data;
        private Matrix mMatrix;
        public TakePicThread(byte[] data, final Matrix m){
            this.data = data;
            mMatrix = m;
        }

        @Override
        public void run(){

            recordFilePath = getPhotoPath();
            File pictureFile = null;
            try {
                pictureFile = new File(recordFilePath);
            }catch (NullPointerException e){
                e.printStackTrace();
            }

            if (pictureFile == null) {
                LogF.e("VR", "Error creating media file, check storage permissions");
                return;
            }

            if(mMatrix != null){
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                Bitmap toSave = Bitmap.createBitmap(bitmap, 0,0, bitmap.getWidth(), bitmap.getHeight(), mMatrix,true);
                toSave.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                data = baos.toByteArray();
            }

            try(FileOutputStream fos = new FileOutputStream(pictureFile)) {
                fos.write(data);
            } catch (FileNotFoundException e) {
                Log.d("VR", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("VR", "Error accessing file: " + e.getMessage());
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    changeStatusLayout(STATUS_PIC_PRE);
                    new Handler().postDelayed(new Runnable(){
                        public void run() {
                            mRecordBack.setEnabled(true);
                            mRecord.setEnabled(true);
                            mChangeCamera.setEnabled(true);
                            isTakePic = false;
                        }
                    }, 550);
                }
            });
        }
    }
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1){
                mPbTimer.setProgress(mSpeed);
                if (mSpeed == 60){
                    mPbTimer.setVisibility(View.GONE);
                    mSpeed = 0;
//                    onKeyUp();
                    mTimeFlag = false;
                }
            }else if (msg.what == 2){
                progressSpeed();
                mPbTimer.setVisibility(View.VISIBLE);
                mRedDotVideo.setVisibility(View.VISIBLE);
                mRecordBack.setVisibility(View.GONE);
                mChangeCamera.setVisibility(View.GONE);
                mThread.start();
            }
        }
    };
    //为进度条模拟一个耗时的操作为
    private Thread mThread;
    private boolean mTimeFlag = true;
    private void progressSpeed(){
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (mSpeed < 60 && mTimeFlag){
                    mSpeed ++;
                    mHandler.sendEmptyMessage(1);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.anim_out_to_bottom);
    }
}
