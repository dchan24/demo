package com.cmicc.module_message.ui.adapter.message;

import android.app.Activity;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chinamobile.app.yuliao_business.util.Status;
import com.chinamobile.app.yuliao_business.util.Type;
import com.cmicc.module_message.ui.adapter.MessageChatListAdapter;
import com.cmicc.module_message.ui.constract.BaseChatContract;
import com.cmcc.cmrcs.android.widget.RecycleViewConstraintLayout;
import com.cmcc.cmrcs.android.widget.emoji.EmojiTextView;
import com.cmicc.module_message.R;

public class OAMessageHolder extends BaseViewHolder {

	public EmojiTextView etvTitle;

	public EmojiTextView etvSummary;

	public TextView tvReadAll;

	public LinearLayout lltContent;

	public ImageView sendFailedView;

	public TextView sTvHasRead;

	public CheckBox multiCheckBox;

	public OAMessageHolder(View itemView, Activity activity , MessageChatListAdapter adapter , BaseChatContract.Presenter presenter) {
		super(itemView ,activity ,adapter ,presenter);
		etvTitle = (EmojiTextView) itemView.findViewById(R.id.etvTitle);
		etvSummary = (EmojiTextView) itemView.findViewById(R.id.etvSummary);
		tvReadAll = (TextView) itemView.findViewById(R.id.tvReadAll);
		lltContent = (LinearLayout) itemView.findViewById(R.id.lltContent);
		sendFailedView = (ImageView) itemView.findViewById(R.id.imageview_msg_send_failed);
		sTvHasRead = (TextView) itemView.findViewById(R.id.tv_has_read);
		multiCheckBox = itemView.findViewById(R.id.multi_check);

		sendFailedView.setOnClickListener(new OnMsgFailClickListener());
		sTvHasRead.setOnClickListener(new NoDoubleClickListenerX());
		lltContent.setOnClickListener(new NoDoubleClickListener());
		lltContent.setOnLongClickListener(new OnMsgContentLongClickListener());

	}

	@Override
	public void bindMultiSelectStatus(boolean isMultiSelectMode , boolean isSelected){
		((RecycleViewConstraintLayout)itemView).setMode(isMultiSelectMode);
		if(isMultiSelectMode){
			//头像不显示，以消息气泡上下居中
			ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)multiCheckBox.getLayoutParams();
			int type = mMessage.getType();
			switch (type){
				case Type.TYPE_MSG_OA_ONE_CARD_SEND:
					params.topToTop = R.id.lltContent;
					params.bottomToBottom = R.id.lltContent;

					break;
				case Type.TYPE_MSG_OA_ONE_CARD_RECV:
					if(sIvHead.getVisibility() == View.INVISIBLE){
						params.topToTop = R.id.lltContent;
						params.bottomToBottom = R.id.lltContent;
					}else{
						params.topToTop = R.id.svd_head;
						params.bottomToBottom = R.id.svd_head;
					}
					break;
				default:
					break;

			}

			multiCheckBox.setLayoutParams(params);
			multiCheckBox.setVisibility(View.VISIBLE);
			multiCheckBox.setChecked(isSelected);
		}else{
			multiCheckBox.setVisibility(View.GONE);
		}
	}

	@Override
	public void bindSendStatus() {
		int type = mMessage.getType();
		int status = mMessage.getStatus();
		if (type == Type.TYPE_MSG_OA_ONE_CARD_SEND) {
			switch (status) {
				case Status.STATUS_WAITING:
				case Status.STATUS_LOADING:
					sendFailedView.setVisibility(View.GONE);
					break;
				case Status.STATUS_OK:
					sendFailedView.setVisibility(View.GONE);
					break;
				case Status.STATUS_FAIL:
					sendFailedView.setVisibility(View.VISIBLE);
					break;
				default:
					sendFailedView.setVisibility(View.VISIBLE);
					break;
			}
		} else {
			sendFailedView.setVisibility(View.GONE);
		}
	}

	public void bindText(){
		etvTitle.setText(mMessage.getBody());
		etvSummary.setText(mMessage.getTitle());
	}
}
