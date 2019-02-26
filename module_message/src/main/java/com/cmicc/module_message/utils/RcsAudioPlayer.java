package com.cmicc.module_message.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.cmcc.cmrcs.android.ui.interfaces.AudioListener;
import com.cmicc.module_message.ui.adapter.MessageChatListAdapter;
import com.cmicc.module_message.R;
import java.io.IOException;
import java.lang.ref.WeakReference;


/**
 *
 * Created by DChan on 2017/7/4.
 *音频播放
 *
 */
public class RcsAudioPlayer {

	private String TAG = "RcsAudioPlayer";

	private WeakReference<MessageChatListAdapter> mAdapter ;

	private static RcsAudioPlayer mRcsMediaPlayer;

	private MediaPlayer mMediaPlayer;
	private AudioManager mAudioManager ; // 播放模式的切换

	private String filePath ;
	private AudioListener audioListener ;
	private Handler handler = new Handler();
	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			float position = mMediaPlayer.getCurrentPosition();
			int duation = mMediaPlayer.getDuration();
			LogF.d(TAG , " position = "+ position + "  duation = "+ duation);
			if(mAdapter != null) {
				MessageChatListAdapter adapter = mAdapter.get();
				if (adapter != null) {
					if (adapter instanceof MessageChatListAdapter && adapter.mAudioPlayProgressBar != null) {
						if (position < duation) {
							handler.postDelayed(runnable, 100);
							adapter.mAudioPlayProgressBar.setProgress((int) (position + 20)); // 更新进度
						} else {
							adapter.mAudioPlayProgressBar.setProgress(adapter.mAudioPlayProgressBar.getMax()); // 更新进度
							handler.removeCallbacks(runnable);
						}
					}
				}
			}
		}
	};
	private RcsAudioPlayer(Context context) {
		if(context != null) {
			final Context c = context.getApplicationContext();
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setWakeMode(c, PowerManager.PARTIAL_WAKE_LOCK);
			mAudioManager = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
//				mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION );
//			} else {
//				mAudioManager.setMode(AudioManager.MODE_IN_CALL );
//			}
//			mAudioManager.setSpeakerphoneOn(true);//默认为扬声器播放
		}
	}

	public static RcsAudioPlayer getInstence(Context context) {
		if (mRcsMediaPlayer == null) {
			mRcsMediaPlayer = new RcsAudioPlayer(context);
		}
		return mRcsMediaPlayer;
	}



	public void play(String filePath, final AudioListener listener) throws Exception {
		this.filePath = filePath ;
		this.audioListener = listener;
		handler.removeCallbacks(runnable);
		mMediaPlayer.reset();
		mMediaPlayer.setDataSource(filePath);
		mMediaPlayer.prepare();
		mMediaPlayer.seekTo(0);
		mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				//mAudioManager.abandonAudioFocus(afChangeListener);
				handler.removeCallbacks(runnable);
				listener.AudioComplete();
			}
		});
		//mMediaPlayer.
		int result = mAudioManager.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT  );//AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK   AUDIOFOCUS_GAIN
		if(result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
			if(mAdapter != null) {
				MessageChatListAdapter adapter = mAdapter.get();
				if (adapter != null && adapter instanceof MessageChatListAdapter && adapter.mAudioPlayProgressBar != null) {
					adapter.mAudioPlayProgressBar.setProgress(0);
					adapter.mAudioPlayProgressBar.setMax(mMediaPlayer.getDuration());
				}
			}
			mMediaPlayer.start();// 开始播放
			handler.postDelayed(runnable , 100);
		}
	}

	public void pause() {
		if (isPlaying()) {
			handler.removeCallbacks(runnable );
			mMediaPlayer.pause();
		}
	}
	public int getDuration() {
			return mMediaPlayer.getDuration();
	}
	public int getCurrentDuration() {
		return mMediaPlayer.getCurrentPosition();
	}

	public void stop() {
		if (isPlaying()) {
			handler.removeCallbacks(runnable );
			mMediaPlayer.stop();
			mMediaPlayer.reset();
		}
		abandonAudioFocus();
		if(mAdapter != null) {
			MessageChatListAdapter adapter = mAdapter.get();
			if (adapter != null) {
				if (adapter.mAudioPlayProgressBar != null) {
					adapter.mAudioPlayProgressBar.setProgress(0);// 从头开始
					adapter.mAudioPlayProgressBar.setVisibility(View.GONE);
				}
				if (adapter.animationDrawableRecv != null && adapter.animationDrawableRecv.isRunning()) {
					adapter.animationDrawableRecv.stop();
				} else if (adapter.animationDrawable != null && adapter.animationDrawable.isRunning()) {
					adapter.animationDrawable.stop();
				}
				if (adapter.mPlayingRecAudio != null) { // 接收方图片复位
					adapter.mPlayingRecAudio.setImageResource(R.drawable.message_voice_playing_f6);
				}
				if (adapter.mPlayBgRec != null) { // 接收方图片复位
					adapter.mPlayBgRec.setImageResource(R.drawable.chat_voiceinformation_stop_big);
				}
				if (adapter.mPlaySmallRec != null) {
					adapter.mPlaySmallRec.setImageResource(R.drawable.chat_voiceinformation_stop_small);
				}
				if (adapter.mPlayingSendAudio != null) { // 发送图片复位
					adapter.mPlayingSendAudio.setImageResource(R.drawable.message_voice_playing_send_f6);
				}
				if (adapter.mPlayBgSend != null) { // 接收方图片复位
					adapter.mPlayBgSend.setImageResource(R.drawable.chat_voiceinformation_stop_big2);
				}
				if (adapter.mPlaySmallSend != null) {
					adapter.mPlaySmallSend.setImageResource(R.drawable.chat_voiceinformation_stop_small2);
				}
				adapter.isPlayingAudio = -1;
				adapter.audioMessageID = "";
				adapter.isPlayingAudioMessage = false; // 没有播放语音
				adapter.mPlayingRecAudio = null;
				adapter.mPlayingSendAudio = null;
				adapter.mPlayBgRec = null;
				adapter.mPlayBgSend = null;
				adapter.mPlaySmallRec = null;
				adapter.mPlaySmallSend = null;
				adapter.mAudioPlayProgressBar = null;
			}
		}
	}
	public void start(){
		if(mMediaPlayer!=null){
			mMediaPlayer.start();
		}
	}

	public boolean isPlaying() {
		return mMediaPlayer != null && mMediaPlayer.isPlaying();
	}

	public void release() {
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}
	
	public String getAudioDuring(String path) {
		return getAudioDuring(path,180);
	}
	
	/**
	 * 描述		：获取Audio的长度（秒）
	 * @param path 路径
	 * @param maxDuration 最大秒数（默认为180秒，不大于0为无上限）
	 * @return
	 */
	public String getAudioDuring(String path, int maxDuration) {
		MediaPlayer mMediaPlayer = new MediaPlayer();
		int mMaxDuration = 180;
		if(maxDuration > 0){
			mMaxDuration = maxDuration;
		}
		try {
			mMediaPlayer.setDataSource(path);
			mMediaPlayer.prepare();
		} catch (Exception e) {
			e.printStackTrace();
		}
		int seconds = mMediaPlayer.getDuration() / 1000;
		if(maxDuration > 0 && seconds >= mMaxDuration-1){
			seconds = mMaxDuration;
		}
		Log.v("RcsAudioPlayer", "path: " + path);
		Log.v("RcsAudioPlayer", "seconds: " + seconds);
		if (seconds <= 0)
			return "1\"";
		if (seconds > 60 * 60) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append(seconds).append("\"");
		return sb.toString();
	}



	/**
	 * 2018.5.5 YSF
	 * 切换到外放
	 */
	public void changeToSpeaker(){
		LogF.d(TAG , "changeToSpeaker 外放");
		mAudioManager.setSpeakerphoneOn(true);
		mAudioManager.setMode(AudioManager.MODE_NORMAL);

	}

	/**
	 * 2018.5.5 YSF
	 * 切换到听筒
	 */
	public void changeToReceiver(){
		LogF.d(TAG , "changeToReceiver 听筒");
		mAudioManager.setSpeakerphoneOn(false);
		mMediaPlayer.pause();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			//mAudioManager.setStreamVolume(AudioManager.MODE_IN_COMMUNICATION ,mAudioManager.getStreamMaxVolume(AudioManager.MODE_IN_COMMUNICATION)  ,  AudioManager.FLAG_SHOW_UI );
			mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
		} else {
			//mAudioManager.setStreamVolume(AudioManager.MODE_IN_CALL ,mAudioManager.getStreamMaxVolume(AudioManager.MODE_IN_CALL ) , AudioManager.FLAG_SHOW_UI );
			mAudioManager.setMode(AudioManager.MODE_IN_CALL);
		}
//		int position = mMediaPlayer.getCurrentPosition(); // 获取当前的播放进度
//		mMediaPlayer.seekTo(0);
//		if(position >=3* 1000 ){
//			mMediaPlayer.seekTo(position - (3 * 1000 ));
//		}else{
//			mMediaPlayer.seekTo(0);
//		}
//		mMediaPlayer.start();

//		Log.d("MessageChatListAdapter" , "听筒" + " position = "+ position);
		try {
			handler.removeCallbacks(runnable );
			if(mAdapter != null){
				MessageChatListAdapter adapter = mAdapter.get();
				if (adapter != null && adapter instanceof MessageChatListAdapter) {
					if(adapter.mAudioPlayProgressBar != null ){
						adapter.mAudioPlayProgressBar.setProgress(0); // 从头播放
					}
				}
				mMediaPlayer.stop();
				mMediaPlayer.reset();
				mMediaPlayer.setDataSource(filePath);
				mMediaPlayer.prepare();
				mMediaPlayer.seekTo(0);
				if(adapter != null && adapter instanceof MessageChatListAdapter && adapter.mAudioPlayProgressBar != null){
					adapter.mAudioPlayProgressBar.setProgress(0);
					adapter.mAudioPlayProgressBar.setMax(mMediaPlayer.getDuration());
				}
			}
			mMediaPlayer.start();
			handler.postDelayed(runnable,100 );
		} catch (IOException e) {
			e.printStackTrace();
		}
		mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				if(audioListener != null ){
					//mAudioManager.abandonAudioFocus(afChangeListener);
					handler.removeCallbacks(runnable );
					audioListener.AudioComplete();
				}
			}
		});
	}

	public void setUp( Context context ){
		if(mMediaPlayer != null && mAudioManager != null ){
			mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC , AudioManager.ADJUST_RAISE ,  AudioManager.FLAG_SHOW_UI);
		}
	}

	public void setDoew(  Context context  ){
		if(mMediaPlayer != null && mAudioManager != null ){
			mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC , AudioManager.ADJUST_LOWER ,  AudioManager.FLAG_SHOW_UI);
		}
	}
	/**
	 * 2018.5.5 YSF
	 * 切换到耳机模式
	 */
	public void changeToHeadset(){
		mAudioManager.setSpeakerphoneOn(false);
	}

	AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {

		@Override
		public void onAudioFocusChange(int focusChange) {
			Log.d(TAG , "focusChange = "+ focusChange );
			if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {   // 暂时失去焦点，必须暂停，但很快就可能获得
				Log.d(TAG , "AUDIOFOCUS_LOSS_TRANSIENT" );
				if ( mMediaPlayer.isPlaying() ) {
					 mMediaPlayer.pause();
				}
			} else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {      // 获取到焦点了
				Log.d(TAG , "AUDIOFOCUS_GAIN" );
				if ( mMediaPlayer == null ) {
					//initBeepSound();
				} else if (!mMediaPlayer.isPlaying()) {
					mMediaPlayer.start();
				}
			} else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {      // 失去焦点，并且常时间不会获取到。(场景：我们在播放语音消息，然后去播放音乐，就走这里)
				Log.d(TAG , "AUDIOFOCUS_LOSS" );
				if (mMediaPlayer.isPlaying()) {
					mMediaPlayer.pause();
					mMediaPlayer.stop();
					if(mAdapter != null) {
						MessageChatListAdapter adapter = mAdapter.get();
						if (adapter != null && adapter instanceof MessageChatListAdapter) { // 停止动画
							handler.removeCallbacks(runnable);
							if (adapter.mAudioPlayProgressBar != null) {
								adapter.mAudioPlayProgressBar.setProgress(0);// 从头开始
								adapter.mAudioPlayProgressBar.setVisibility(View.GONE);
							}
							if (adapter.animationDrawableRecv != null && adapter.animationDrawableRecv.isRunning()) {
								adapter.animationDrawableRecv.stop();
							} else if (adapter.animationDrawable != null && adapter.animationDrawable.isRunning()) {
								adapter.animationDrawable.stop();
							}
							if (adapter.mPlayingRecAudio != null) { // 接收方图片复位
								adapter.mPlayingRecAudio.setImageResource(R.drawable.message_voice_playing_f6);
							}
							if (adapter.mPlayBgRec != null) { // 接收方图片复位
								adapter.mPlayBgRec.setImageResource(R.drawable.chat_voiceinformation_stop_big);
							}
							if (adapter.mPlaySmallRec != null) {
								adapter.mPlaySmallRec.setImageResource(R.drawable.chat_voiceinformation_stop_small);
							}
							if (adapter.mPlayingSendAudio != null) { // 发送图片复位
								adapter.mPlayingSendAudio.setImageResource(R.drawable.message_voice_playing_send_f6);
							}
							if (adapter.mPlayBgSend != null) { // 接收方图片复位
								adapter.mPlayBgSend.setImageResource(R.drawable.chat_voiceinformation_stop_big2);
							}
							if (adapter.mPlaySmallSend != null) {
								adapter.mPlaySmallSend.setImageResource(R.drawable.chat_voiceinformation_stop_small2);
							}
							adapter.isPlayingAudio = -1;
							adapter.audioMessageID = "";
							adapter.isPlayingAudioMessage = false; // 没有播放语音
							adapter.mPlayingRecAudio = null;
							adapter.mPlayingSendAudio = null;
							adapter.mPlayBgRec = null;
							adapter.mPlayBgSend = null;
							adapter.mPlaySmallRec = null;
							adapter.mPlaySmallSend = null;
							adapter.mAudioPlayProgressBar = null;
						}
					}
				}
				mAudioManager.abandonAudioFocus(afChangeListener);
			} else if (focusChange == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
				Log.d(TAG , "AUDIOFOCUS_REQUEST_GRANTED" );
				if (mMediaPlayer.isPlaying()) {
					mMediaPlayer.stop();
				}
			} else if (focusChange == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
				Log.d(TAG , "AUDIOFOCUS_REQUEST_FAILED" );
				if (mMediaPlayer.isPlaying()) {
					mMediaPlayer.stop();
				}
			}else if(focusChange == AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK){
				Log.d(TAG , "AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK" );

			}
		}
	};

	public void requestAudioFocus(){
		if( mAudioManager != null && afChangeListener != null ){
			int result = mAudioManager.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN );//AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
		}
	}

	public void abandonAudioFocus(){
		if( mAudioManager != null && afChangeListener != null ){
			mAudioManager.abandonAudioFocus(afChangeListener);
		}
	}

	public void setmAdapter(MessageChatListAdapter mAdapter) {
		this.mAdapter = new WeakReference<>(mAdapter);
	}
}
