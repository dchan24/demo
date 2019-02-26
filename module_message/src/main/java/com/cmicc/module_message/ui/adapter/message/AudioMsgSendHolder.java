package com.cmicc.module_message.ui.adapter.message;

import android.app.Activity;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.yuliao_business.util.Status;
import com.chinamobile.app.yuliao_business.util.Type;
import com.cmicc.module_message.ui.adapter.MessageChatListAdapter;
import com.cmicc.module_message.ui.constract.BaseChatContract;
import com.cmcc.cmrcs.android.widget.RecycleViewConstraintLayout;
import com.cmicc.module_message.R;
import com.constvalue.MessageModuleConst;

public class AudioMsgSendHolder extends BaseAudioMsgHolder {
	public TextView sTvHasRead;
	public ImageView sendStatus;
    public CheckBox multiCheckBox;

	public AudioMsgSendHolder(View itemView, Activity activity , MessageChatListAdapter adapter , BaseChatContract.Presenter presenter) {
		super(itemView,activity,adapter,presenter);
		sTvHasRead = itemView.findViewById(R.id.tv_has_read);
		sendStatus = itemView.findViewById(R.id.iv_send_status);
        multiCheckBox = itemView.findViewById(R.id.multi_check);

		sTvHasRead.setOnClickListener(new NoDoubleClickListenerX());
		sendFailedView.setOnClickListener(new OnMsgFailClickListener());

	}

	@Override
    public void bindMultiSelectStatus(boolean isMultiSelectMode , boolean isSelected){
        ((RecycleViewConstraintLayout)itemView).setMode(isMultiSelectMode);
        if(isMultiSelectMode){
            //头像不显示，以消息气泡上下居中
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)multiCheckBox.getLayoutParams();
            params.topToTop = R.id.ll;
            params.bottomToBottom = R.id.ll;
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
		int msgType = mMessage.getType();
		int receipt = mMessage.getMessage_receipt();
        if (isEPGroup || isPartyGroup) {
            //企业群，党群，特殊消息另起头像，已读状态等下间距去掉
            if(mMessage.getSmallPadding()){
                sTvHasRead.setPadding(0 ,(int) AndroidUtil.dip2px(mContext, 7) ,0 ,0 );
            }else{
                sTvHasRead.setPadding(0 ,(int) AndroidUtil.dip2px(mContext, 7) ,0 ,(int) AndroidUtil.dip2px(mContext, 7) );
            }
            if (status == Status.STATUS_OK) {
                sTvHasRead.setVisibility(View.VISIBLE);
            } else {
                sTvHasRead.setVisibility(View.INVISIBLE);
            }
        } else if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT && receipt != -1) {
            if (status == Status.STATUS_OK) {
                sTvHasRead.setVisibility(View.VISIBLE);
                sendStatus.setVisibility(View.VISIBLE);
                if (msgType == Type.TYPE_MSG_AUDIO_SEND_CCIND) {
                    sTvHasRead.setTextColor(mContext.getResources().getColor(R.color.color_757575));
                    sTvHasRead.setText(mContext.getString(R.string.from_pc));
                    sendStatus.setImageResource(R.drawable.my_chat_pc);
                    ViewGroup.LayoutParams params = sendStatus.getLayoutParams();
                    params.width = (int) AndroidUtil.dip2px(mContext, 10f);
                    params.height = (int) AndroidUtil.dip2px(mContext, 8.7f);
                    sendStatus.setLayoutParams(params);
                } else {
                    if (receipt == 0) {
                        sTvHasRead.setTextColor(mContext.getResources().getColor(R.color.color_757575));
                        sTvHasRead.setText("");
                        sendStatus.setImageResource(R.drawable.my_chat_waiting);
                    } else if (receipt == 1) {
                        sTvHasRead.setTextColor(mContext.getResources().getColor(R.color.color_757575));
                        sTvHasRead.setText(mContext.getString(R.string.already_delivered));
                        sendStatus.setImageResource(R.drawable.my_chat_delivered);
                    } else if (receipt == 2) {
                        sTvHasRead.setTextColor(mContext.getResources().getColor(R.color.color_757575));
                        sTvHasRead.setText(mContext.getString(R.string.already_delivered_by_sms));
                        sendStatus.setImageResource(R.drawable.my_chat_delivered);
                    } else if (receipt == 3) {
                        sTvHasRead.setTextColor(mContext.getResources().getColor(R.color.color_757575));
                        sTvHasRead.setText(mContext.getString(R.string.already_notified_by_sms));
                        sendStatus.setImageResource(R.drawable.my_chat_shortmessage);
                    } else {
                        sTvHasRead.setTextColor(mContext.getResources().getColor(R.color.color_757575));
                        sTvHasRead.setText(mContext.getString(R.string.others_offline_already_notified));
                        sendStatus.setImageResource(R.drawable.my_chat_shortmessage);
                    }
                }
            } else {
                sTvHasRead.setVisibility(View.INVISIBLE);
                sendStatus.setVisibility(View.INVISIBLE);
            }
        } else {
            sTvHasRead.setVisibility(View.GONE);
            sendStatus.setVisibility(View.GONE);
        }
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
