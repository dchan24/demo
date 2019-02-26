package com.cmicc.module_message.ui.adapter.message;

import android.app.Activity;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_business.util.Status;
import com.cmicc.module_message.ui.adapter.MessageChatListAdapter;
import com.cmicc.module_message.ui.constract.BaseChatContract;
import com.cmcc.cmrcs.android.widget.RecycleViewConstraintLayout;
import com.cmicc.module_message.R;

import static android.support.v7.widget.RecyclerView.NO_POSITION;

public  class AudioMsgRecvHolder extends BaseAudioMsgHolder {

	public ImageView image_audio_unread;
	public CheckBox multiCheckBox;
	public LinearLayout audio_ll ;

	public AudioMsgRecvHolder(View itemView, Activity activity , final MessageChatListAdapter adapter , BaseChatContract.Presenter presenter) {
		super(itemView,activity ,adapter,presenter);
		image_audio_unread = (ImageView) itemView.findViewById(R.id.imageview_msg_unread);
		audio_ll = itemView.findViewById(R.id.audio_ll);
		multiCheckBox = itemView.findViewById(R.id.multi_check);

		//            layout_Audio_content.setOnClickListener(new NoDoubleClickListener());
		sendFailedView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int position = getAdapterPosition();
				if (position == NO_POSITION) {
					return;
				}
				position = adapter.canLoadMore() ? position - 1 : position;
				final Message msg = adapter.getItem(position);
				adapter.audioRecvContentClick(msg, AudioMsgRecvHolder.this, position);
			}
		});

	}

	@Override
	public void bindMultiSelectStatus(boolean isMultiSelectMode , boolean isSelected){
		((RecycleViewConstraintLayout)itemView).setMode(isMultiSelectMode);
		if(isMultiSelectMode){
			//头像不显示，以消息气泡上下居中
			ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)multiCheckBox.getLayoutParams();
			if(sIvHead.getVisibility() == View.INVISIBLE){
				params.topToTop = R.id.ll;
				params.bottomToBottom = R.id.ll;
			}else{
				params.topToTop = R.id.svd_head;
				params.bottomToBottom = R.id.svd_head;
			}
			multiCheckBox.setLayoutParams(params);
			multiCheckBox.setVisibility(View.VISIBLE);
			multiCheckBox.setChecked(isSelected);
			marginView.setVisibility(View.GONE);
		}else{
			multiCheckBox.setVisibility(View.GONE);
			marginView.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * 绑定下载状态
	 */
	public void bindDownloadStatus(){
		int status = mMessage.getStatus();
		switch (status) {
			case Status.STATUS_WAITING:
			case Status.STATUS_LOADING:
				this.layoutLoading.setVisibility(View.VISIBLE);
				this.sendFailedView.setVisibility(View.GONE);
				break;
			case Status.STATUS_OK:
				this.layoutLoading.setVisibility(View.GONE);
				this.sendFailedView.setVisibility(View.GONE);
				break;
			case Status.STATUS_FAIL:
				this.layoutLoading.setVisibility(View.GONE);
				this.sendFailedView.setVisibility(View.VISIBLE);
				break;
			case Status.STATUS_PAUSE:
				this.layoutLoading.setVisibility(View.GONE);
				this.sendFailedView.setVisibility(View.VISIBLE);
				break;
			default:
				this.layoutLoading.setVisibility(View.GONE);
				this.sendFailedView.setVisibility(View.GONE);
				break;

		}
	}
}
