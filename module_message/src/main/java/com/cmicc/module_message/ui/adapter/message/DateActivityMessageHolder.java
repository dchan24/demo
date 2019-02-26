package com.cmicc.module_message.ui.adapter.message;

import android.app.Activity;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.yuliao_business.util.Status;
import com.chinamobile.app.yuliao_business.util.Type;
import com.cmicc.module_message.ui.adapter.MessageChatListAdapter;
import com.cmicc.module_message.ui.constract.BaseChatContract;
import com.cmcc.cmrcs.android.widget.RecycleViewConstraintLayout;
import com.cmicc.module_message.R;

public class DateActivityMessageHolder extends BaseViewHolder {

	public TextView etvTitle;

	public TextView etvSummary;

	public RelativeLayout rltContent;

	public ImageView sendFailedView;

	public TextView sTvHasRead;

	public CheckBox multiCheckBox;

	public DateActivityMessageHolder(View itemView, Activity activity , final MessageChatListAdapter adapter , BaseChatContract.Presenter presenter) {
		super(itemView ,activity ,adapter ,presenter);
		etvTitle = (TextView) itemView.findViewById(R.id.etvTitle);
		etvSummary = (TextView) itemView.findViewById(R.id.etvSummary);
		sendFailedView = (ImageView) itemView.findViewById(R.id.imageview_msg_send_failed);
		rltContent = (RelativeLayout) itemView.findViewById(R.id.rltContent);
		sTvHasRead = (TextView) itemView.findViewById(R.id.tv_has_read);
		multiCheckBox = itemView.findViewById(R.id.multi_check);

		sendFailedView.setOnClickListener(new OnMsgFailClickListener());
		sTvHasRead.setOnClickListener(new NoDoubleClickListenerX());
		rltContent.setOnClickListener(new NoDoubleClickListener());
		rltContent.setOnLongClickListener(new OnMsgContentLongClickListener());

	}
	@Override
	public void bindMultiSelectStatus(boolean isMultiSelectMode , boolean isSelected){
		((RecycleViewConstraintLayout)itemView).setMode(isMultiSelectMode);
		if(isMultiSelectMode){
			//头像不显示，以消息气泡上下居中
			ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)multiCheckBox.getLayoutParams();
			int type = mMessage.getType();
			switch (type){
				case Type.TYPE_MSG_DATE_ACTIVITY_SEND:
					params.topToTop = R.id.rltContent;
					params.bottomToBottom = R.id.rltContent;

					break;
				case Type.TYPE_MSG_DATE_ACTIVITY_RECV:
					if(sIvHead.getVisibility() == View.INVISIBLE){
						params.topToTop = R.id.rltContent;
						params.bottomToBottom = R.id.rltContent;
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
		int status = mMessage.getStatus();
		if (mMessage.getType() == Type.TYPE_MSG_DATE_ACTIVITY_SEND) {
			if (isEPGroup || isPartyGroup) {
				//企业群，党群，特殊消息另起头像，已读状态等下间距去掉
				if(mMessage.getSmallPadding()){
					sTvHasRead.setPadding(0 ,(int) AndroidUtil.dip2px(mContext, 7) ,0 ,0 );
				}else{
					sTvHasRead.setPadding(0 ,(int) AndroidUtil.dip2px(mContext, 7) ,0 ,(int) AndroidUtil.dip2px(mContext, 7) );
				}
				if ( status == Status.STATUS_OK) {
					sTvHasRead.setVisibility(View.VISIBLE);
				} else {
					sTvHasRead.setVisibility(View.INVISIBLE);
				}
			} else {
				sTvHasRead.setVisibility(View.GONE);
			}
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
