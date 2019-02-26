package com.cmicc.module_message.ui.adapter.message;

import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.utils.StringUtil;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.utils.FileUtil;
import com.chinamobile.app.yuliao_common.utils.Log;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.cmicc.module_message.ui.adapter.MessageChatListAdapter;
import com.cmicc.module_message.ui.constract.BaseChatContract;
import com.cmcc.cmrcs.android.widget.emoji.EmojiParser;
import com.cmcc.cmrcs.android.widget.emoji.EmojiTextView;
import com.cmicc.module_message.R;

import static android.support.v7.widget.RecyclerView.NO_POSITION;
import static com.constvalue.MessageModuleConst.MessageChatListAdapter.BUBBLE_NOUP_DOWN;
import static com.constvalue.MessageModuleConst.MessageChatListAdapter.BUBBLE_NOUP_NODOWN;
import static com.constvalue.MessageModuleConst.MessageChatListAdapter.BUBBLE_UP_DOWN;
import static com.constvalue.MessageModuleConst.MessageChatListAdapter.BUBBLE_UP_NODOWN;

public class BaseAudioMsgHolder extends BaseViewHolder {
    private static final String TAG = BaseAudioMsgHolder.class.getName();

	public TextView audioTime;

	public ImageView sendFailedView;
	public ProgressBar layoutLoading;

	public RelativeLayout layout_Audio_content;
	public ImageView image_audio;
	public ImageView img_play_icon ;

	public RelativeLayout audoi_and_text_messag ;
//	private RelativeLayout msg_audio_text_content ;
	public EmojiTextView audioAndTv_message ;
	public ImageView img_audio_play_small_icon ;
	public ProgressBar audioPlayProgressBar ;
	public RelativeLayout audio_progressbar_rl ;
	public View marginView;
	MessageChatListAdapter adapter ;
	Activity activity ;

	public BaseAudioMsgHolder(View itemView, Activity activity , final MessageChatListAdapter adapter , BaseChatContract.Presenter presenter) {
		super(itemView ,activity ,adapter ,presenter);
		this.adapter = adapter ;
		this.activity =activity ;
		audioTime = (TextView) itemView.findViewById(R.id.textview_audio_time);
		sendFailedView = (ImageView) itemView.findViewById(R.id.imageview_msg_send_failed);
		layoutLoading = (ProgressBar) itemView.findViewById(R.id.progress_send_small);
		layout_Audio_content = (RelativeLayout) itemView.findViewById(R.id.linearlayout_msg_content);
		image_audio = (ImageView) itemView.findViewById(R.id.imageview_msg_audio);
		img_play_icon = (ImageView) itemView.findViewById(R.id.img_audio_play_icon);

		audoi_and_text_messag = (RelativeLayout) itemView.findViewById(R.id.audoi_and_text_messag);
//		msg_audio_text_content = (RelativeLayout) itemView.findViewById(R.id.linearlayout_msg_audio_text_content);
		audioAndTv_message = (EmojiTextView) itemView.findViewById(R.id.tv_audioTv_message);
		img_audio_play_small_icon = (ImageView) itemView.findViewById(R.id.img_audio_play_small_icon);
		audioPlayProgressBar = (ProgressBar) itemView.findViewById(R.id.audioplay_progress_bar);// 播放进度

		audio_progressbar_rl = (RelativeLayout) itemView.findViewById(R.id.audio_progressbar);// 播放进度
		marginView = (View)itemView.findViewById(R.id.margin_view);


		layout_Audio_content.setOnClickListener(new OnClickListener() { // 点击事件，播放语音
			@Override
			public void onClick(View v) {
				int position = getAdapterPosition();
				if (position == NO_POSITION) {
					return;
				}
				position = adapter.canLoadMore() ? position - 1 : position;
				final Message msg = adapter.getItem(position);
				LogF.d(TAG , "position = "+ position + "  msgID = "+ msg.getMsgId() + "  boy = "+ msg.getBody()+"  filePath = "+ msg.getExtThumbPath());
				int type = msg.getType();
				switch (type) {
					case Type.TYPE_MSG_AUDIO_SEND:
						adapter.audioSendContentClick(msg, (AudioMsgSendHolder) BaseAudioMsgHolder.this); // 自己发送的语音
						break;
					case Type.TYPE_MSG_AUDIO_RECV:
						adapter.audioRecvContentClick(msg, (AudioMsgRecvHolder) BaseAudioMsgHolder.this, position); // 接受到的语音
						break;
				}
			}
		});
		layout_Audio_content.setOnLongClickListener(new OnMsgContentLongClickListener()); // 长按事件

		if(audoi_and_text_messag != null ){
			audoi_and_text_messag.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					int position = getAdapterPosition();
					if (position == NO_POSITION) {
						return;
					}
					position = adapter.canLoadMore() ? position - 1 : position;
					final Message msg = adapter.getItem(position);
					LogF.d(TAG , "position = "+ position + "  msgID = "+ msg.getMsgId() + "  boy = "+ msg.getBody()+"  filePath = "+ msg.getExtThumbPath());
					int type = msg.getType();
					switch (type) {
						case Type.TYPE_MSG_AUDIO_SEND:
							adapter.audioSendContentClick(msg, (AudioMsgSendHolder) BaseAudioMsgHolder.this); // 自己发送的语音
							break;
						case Type.TYPE_MSG_AUDIO_RECV:
							adapter.audioRecvContentClick(msg, (AudioMsgRecvHolder) BaseAudioMsgHolder.this, position); // 接受到的语音
							break;
					}
				}
			});

			audoi_and_text_messag.setOnLongClickListener(new OnMsgContentLongClickListener()); // 长按事件
		}


//            sendFailedView.setOnClickListener(new OnMsgFailClickListener());
	}


    /**
     *  设置音频消息的时间 和 发送状态
     */
	public void bindAudioTime() {
		ViewGroup.LayoutParams paramSend = (ViewGroup.LayoutParams) this.layout_Audio_content.getLayoutParams();// 取控件当前的布局参数
		String duration = mMessage.getExtSizeDescript();
		LogF.d(TAG , "duration : "+ duration );
		int tm = 0;// duration
		if (StringUtil.isEmpty(duration)) {
			tm = (int) FileUtil.getDuring(mMessage.getExtFilePath()) / 1000;
			if (tm == 61) {
				tm = 60;
			}
			duration = tm + "";
		}
		if (tm <= 0) try {
			tm = Integer.parseInt(duration);
			if (tm == 61) {
				tm = 60;
			}
		} catch (Exception e) {
			LogF.e(TAG, Log.getStackTraceString(e));
		}
		LogF.d(TAG , " tm = "+ tm) ;
		int dimens = 0;
		if (tm <= 5) {
			dimens = (int) mContext.getResources().getDimension(R.dimen.content_audio_width_5);
		} else if (tm > 5 && tm <= 10) {
			dimens = (int) mContext.getResources().getDimension(R.dimen.content_audio_width_10);
		} else if (tm > 10 && tm <= 15) {
			dimens = (int) mContext.getResources().getDimension(R.dimen.content_audio_width_15);
		} else if (tm > 15 && tm <= 20) {
			dimens = (int) mContext.getResources().getDimension(R.dimen.content_audio_width_20);
		} else if (tm > 20 && tm <= 25) {
			dimens = (int) mContext.getResources().getDimension(R.dimen.content_audio_width_25);
		} else {
			dimens = (int) mContext.getResources().getDimension(R.dimen.content_audio_width_max);
		}
		this.audioTime.setText(tm + "”");
		paramSend.width = dimens;
	}



	public void bindAudioAndTextBubble(int status , int type , Message msg) {
		boolean isLeft = (type & Type.TYPE_RECV) > 0;
		int drawableRes = R.drawable.msgbg_receive_common;
		int size = 14;
		try {
			size = Integer.valueOf(msg.getTextSize());
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		int length = 0;
		if (!TextUtils.isEmpty(msg.getBody())) length = msg.getBody().length();
		if (size * length > 14336) {
			if (!isLeft) drawableRes = R.drawable.msgbg_send_common;
		} else {
			if ((this instanceof AudioMsgSendHolder) || (this instanceof AudioMsgRecvHolder)) {
				switch (msg.getBubbleType()) {
					case BUBBLE_NOUP_NODOWN:
						if (isLeft) drawableRes = R.drawable.msgbg_receive_text_noup_nodown;
						else drawableRes = R.drawable.msgbg_send_text_noup_nodown;
						break;
					case BUBBLE_NOUP_DOWN:
						if (isLeft) drawableRes = R.drawable.msgbg_receive_text_noup_down;
						else drawableRes = R.drawable.msgbg_send_text_noup_down;
						break;
					case BUBBLE_UP_DOWN:
						if (isLeft) drawableRes = R.drawable.msgbg_receive_text_up_down;
						else drawableRes = R.drawable.msgbg_send_text_up_down;
						break;
					case BUBBLE_UP_NODOWN:
						if (isLeft) drawableRes = R.drawable.msgbg_receive_text_up_nodown;
						else drawableRes = R.drawable.msgbg_send_text_up_nodown;
						break;
				}
			}
		}
		if(isLeft){
			audioAndTv_message.setTextColor(leftTextColor);
		}else{
			audioAndTv_message.setTextColor(rightTextColor);
		}
		int mEmojiconSize = (int) audioAndTv_message.getTextSize() + (int) AndroidUtil.dip2px(mContext, 5);
		CharSequence builder = EmojiParser.getInstance(mContext).replaceAllEmojis(mContext, msg.getBody(), mEmojiconSize);
		audioAndTv_message.setText(builder);

		if (this instanceof AudioMsgSendHolder) {
			audoi_and_text_messag.setBackgroundResource(drawableRes);
			GradientDrawable mGrand = (GradientDrawable) audoi_and_text_messag.getBackground();
			mGrand.setColor(rightColorId);
		} else if (this instanceof AudioMsgRecvHolder) {
			audoi_and_text_messag.setBackgroundResource(drawableRes);
			GradientDrawable mGrand = (GradientDrawable) audoi_and_text_messag.getBackground();
			mGrand.setColor(leftColorId);
		}
	}
}
