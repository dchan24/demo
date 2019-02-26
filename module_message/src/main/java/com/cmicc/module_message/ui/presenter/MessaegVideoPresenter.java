package com.cmicc.module_message.ui.presenter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.application.App;
import com.chinamobile.app.yuliao_common.utils.FileUtil;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.utils.RxAsyncHelper;
import com.chinamobile.app.utils.AndroidUtil;
import com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity;
import com.cmicc.module_message.ui.activity.MessagevideoActivity;
import com.cmicc.module_message.ui.constract.MessageVideoContract;
import com.cmcc.cmrcs.android.ui.view.MessageOprationDialog;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.fragment.MessageVideoFragment;

import java.io.File;

import rx.functions.Func1;

import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.MESSAGE_FILE_PATH;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.MESSAGE_FILE_THUMB_PATH;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.MESSAGE_TYPE;
import static com.cmcc.cmrcs.android.ui.utils.ContactSelectorUtil.SOURCE_MESSAGE_FORWARD;

/**
 * Created by Tiu on 2017/7/7.
 */

public class MessaegVideoPresenter implements MessageVideoContract.Presenter, SurfaceHolder.Callback, SeekBar.OnSeekBarChangeListener {
    private static final String TAG = MessaegVideoPresenter.class.getSimpleName();
    private final MessagevideoActivity mActivity;
    MessageVideoContract.View mView;

    private MediaPlayer mPlayer;
    private SurfaceHolder mSurfaceHolder;

    private STATE state = STATE.STATE_IDLE;
    private PowerManager.WakeLock wakeLock = null;
    private int currentPosition;
    private SeekBar mSeekbar;

    private Runnable setCurrentTextView = new Runnable() {

        @Override
        public void run() {
            int current = 0;
            if (mPlayer != null) {
                try {
                    current = mPlayer.getCurrentPosition();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            current /= 1000;
            mView.updateCurrentTextView(String.format("%02d:%02d",  (current % 3600) / 60, (current % 60)));
        }
    };

    private Runnable refreshSeekBar = new Runnable() {

        @Override
        public void run() {
            while (mPlayer != null && state == STATE.STATE_PLAYING) {
                try {
                    Thread.sleep(500);
                    if (mPlayer != null && mPlayer.isPlaying()) {
                        int current = mPlayer.getCurrentPosition();
                        mSeekbar.setProgress(current);
                        // final int currentSe = current/1000;
                        mSeekbar.post(setCurrentTextView);
                    }
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    };

    private Handler mHandler;

    @Override
    public void handleControl() {
        if (mPlayer == null) {
            return;
        }

        if (mPlayer.isPlaying()) {
            releaseWakeLock();
            mPlayer.pause();
            state = STATE.STATE_PAUSE;
            mView.setControlViewPlay(false);
        } else {
            if (state == STATE.STATE_IDLE || state == STATE.STATE_STOP) {
                int duration = mPlayer.getDuration();
                if(duration < 1000){
                    duration = 1000;
                }
                mSeekbar.setMax(duration);
                duration /= 1000;
                mView.updateEndPositionTextView(String.format("%02d:%02d",  (duration % 3600) / 60, (duration % 60)));
                duration = 0;
                mView.updateCurrentTextView(String.format("%02d:%02d",  (duration % 3600) / 60, (duration % 60)));
                mSeekbar.setProgress(0);


            }
            acquireWakeLock();
            mPlayer.start();
            mHandler = new Handler();
            if (state == STATE.STATE_IDLE) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mView.showFrame(false);
                    }
                }, 300) ;
            }
            state = STATE.STATE_PLAYING;
            mView.setControlViewPlay(false);
            new RxAsyncHelper<>("").runInThread(new Func1<String, Object>() {
                @Override
                public Object call(String s) {
                    refreshSeekBar.run();
                    return null;
                }
            }).subscribe();
            mView.setControlViewPlay(true);
        }
    }

    @Override
    public void onDestroy() {
        if (mPlayer != null) {
            state = STATE.STATE_STOP;
            mPlayer.release();
            mPlayer = null;
            releaseWakeLock();
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }

    }

    @Override
    public void saveVideo(Bundle bundle) {
        final String videoPath = bundle.getString("path");
        final String imagePath = bundle.getString("image");
        if (AndroidUtil.isSdcardAvailable()) {
            if (AndroidUtil.isSdcardReady()) {
                AsyncTask mAsyncTask = new AsyncTask<Void, Void, Boolean>() {

                    @Override
                    protected Boolean doInBackground(Void... params) {
                        if (!TextUtils.isEmpty(videoPath)) {
                            File file = new File(videoPath);
                            if (file.exists()) {
                                File savefile = new File(FileUtil.getSaveDir(), file.getName());
                                if (savefile.exists()) {
                                    return true;
                                } else {
                                    // 插入系统图库，并更新媒体库
                                    byte[] bytes = FileUtil.readFileToBytes(file);
                                    if(bytes == null){
                                        LogF.i(TAG ,"readFileToBytes fail ,bytes is null");
                                        return false;
                                    }
                                    boolean b = FileUtil.writeBytesToFile(savefile.getAbsolutePath(), bytes);
                                    Intent intent_scan = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                    Uri uri = Uri.fromFile(savefile);
                                    intent_scan.setData(uri);
                                    mActivity.sendBroadcast(intent_scan);
                                    return b;
                                }
                            }
                        }
                        return false;
                    }

                    @Override
                    protected void onPostExecute(Boolean result) {
                        if (result) {
//                            BaseToast.show(mActivity, mActivity.getString(R.string.picture_save_success));
                            BaseToast.show(mActivity, "视频已保存");
                        } else {
//                            BaseToast.show(mContext, getString(R.string.save_fail));
                            BaseToast.show(mActivity, "视频保存失败");
                        }
                    };

                }.execute();
            } else {
//                BaseToast.show(mContext, getString(R.string.space_no_enough));
                BaseToast.show(mActivity, "内存不足");
            }
        }



    }

    @Override
    public void showDialog(final Bundle bundle) {
        final String videoPath = bundle.getString("path");
        final String imagePath = bundle.getString("image");
        String[] itemList = mActivity.getResources().getStringArray(R.array.msg_video_save_long_click);
        MessageOprationDialog messageOprationDialog;
        messageOprationDialog = new MessageOprationDialog(mActivity, null, itemList, null);
        messageOprationDialog.setOnMessageItemClickListener(new MessageOprationDialog.OnOprationItemClickListener() {
            @Override
            public void onClick(String item, int which, String address) {
                if (item.equals(mActivity.getString(R.string.forwarld))) {
//                    String extFilePath = videoPath;
                    File file = new File(videoPath);
                    if (!file.exists()) {
//                        BaseToast.show(mContext, mContext.getString(R.string.toast_download_video));
                        return;
                    }
//                    Intent i = ContactsSelectActivity.createIntentForMessageForward(mActivity.getApplicationContext());
                    Intent i = ContactSelectorActivity.creatIntent(mActivity,SOURCE_MESSAGE_FORWARD, 1);
                    Bundle bundle = new Bundle();
                    bundle.putInt(MESSAGE_TYPE, Type.TYPE_MSG_IMG_SEND);
                    bundle.putString(MESSAGE_FILE_PATH, videoPath);
                    bundle.putString(MESSAGE_FILE_THUMB_PATH, imagePath);
                    i.putExtras(bundle);
                    mActivity.startActivity(i);
                } else if (item.equals(mActivity.getString(R.string.save_video))) {
                    saveVideo(bundle);
                }
            }
        });
        messageOprationDialog.show();
    }

    public enum STATE {
        STATE_IDLE(0), STATE_PLAYING(1), STATE_PAUSE(2), STATE_STOP(3);

        private int state;

        private STATE(int state) {
            this.state = state;
        }

        public int getState() {
            return state;
        }

    }

    public MessaegVideoPresenter(MessagevideoActivity messagevideoActivity) {
        mActivity = messagevideoActivity;
    }

    @Override
    public void start() {
        mSeekbar = mView.getSeekBar();
        mSeekbar.setOnSeekBarChangeListener(this);
        mSurfaceHolder = mView.getSurfaceHolder();
        mSurfaceHolder.addCallback(this);

    }

    public void setView(MessageVideoFragment view) {
        mView = view;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        if (mPlayer != null) {
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;
        }
        try {
            mPlayer = new MediaPlayer();
            mPlayer.setDisplay(mSurfaceHolder);
//            mPlayer.setDataSource(String.valueOf(Uri.parse(mView.getDataSource().getString("path"))));
            mPlayer.setDataSource(mActivity, Uri.parse(mView.getDataSource().getString("path")));
            mPlayer.prepare();
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    state = STATE.STATE_STOP;
                    mView.setControlViewPlay(false);
                    mSeekbar.setProgress(mSeekbar.getMax());
                    int duration = mSeekbar.getMax() / 1000;
                    mView.updateCurrentTextView(String.format("%02d:%02d",  (duration % 3600) / 60, (duration % 60)));
                    releaseWakeLock();

                }
            });
            mPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                @Override
                public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                    changeVideoSize();
                }
            });
            handleControl();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(mActivity, mActivity.getText(R.string.video_load_fail), Toast.LENGTH_SHORT).show();
            String path = mView.getDataSource().getString("path");
            if(!TextUtils.isEmpty(path)){
                File file = new File(path);
                if(file.exists()){
                    LogF.d("MessaegVideoPresenter","delete video");
                    FileUtil.deleteFile(file);
                }
            }
            mActivity.finish();

        }
    }

    public void changeVideoSize() {
        int videoWidth = mPlayer.getVideoWidth();
        int videoHeight = mPlayer.getVideoHeight();

        DisplayMetrics dm = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int mSurfaceViewWidth = dm.widthPixels;
        int mSurfaceViewHeight = dm.heightPixels;

        //根据视频尺寸去计算->视频可以在sufaceView中放大的最大倍数。
        float max;
        if (mActivity.getResources().getConfiguration().orientation== ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            //竖屏模式下按视频宽度计算放大倍数值
            max = Math.max((float) videoWidth / (float) mSurfaceViewWidth,(float) videoHeight / (float) mSurfaceViewHeight);
        } else{
            //横屏模式下按视频高度计算放大倍数值
            max = Math.max(((float) videoWidth/(float) mSurfaceViewHeight),(float) videoHeight/(float) mSurfaceViewWidth);
        }

        //视频宽高分别/最大倍数值 计算出放大后的视频尺寸
        videoWidth = (int) Math.ceil((float) videoWidth / max);
        videoHeight = (int) Math.ceil((float) videoHeight / max);

        //无法直接设置视频尺寸，将计算出的视频尺寸设置到surfaceView 让视频自动填充。
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(videoWidth, videoHeight);
        layoutParams.gravity = Gravity.CENTER;
        mView.updateSurfaceView(layoutParams);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                state = STATE.STATE_PLAYING;
                releaseWakeLock();
                mPlayer.stop();
            }
            mPlayer.release();
            mPlayer = null;
        }
    }

    // 获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
    private void acquireWakeLock() {
        if (null == wakeLock) {
            PowerManager pm = (PowerManager) App.getAppContext().getSystemService(Context.POWER_SERVICE);
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

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            if (mPlayer != null) {
                int currentProgress = mPlayer.getCurrentPosition();
                if (Math.abs(progress - currentProgress) > seekBar.getMax() / 10 || progress >= seekBar.getMax()) {
                    mPlayer.seekTo(progress);
                    currentPosition = progress;
                    int duration = currentPosition / 1000;
                    mView.updateCurrentTextView(String.format("%02d:%02d",  (duration % 3600) / 60, (duration % 60)));
                }
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (state == STATE.STATE_PLAYING) {
            mPlayer.seekTo(currentPosition);
            mPlayer.start();
        }
    }

}
