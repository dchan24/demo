package com.cmicc.module_message.ui.adapter.message;

import android.app.Activity;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.yuliao_business.util.Status;
import com.chinamobile.app.yuliao_business.util.Type;
import com.cmicc.module_message.ui.adapter.MessageChatListAdapter;
import com.cmicc.module_message.ui.constract.BaseChatContract;
import com.cmcc.cmrcs.android.widget.RecycleViewConstraintLayout;
import com.cmicc.module_message.R;
import com.constvalue.MessageModuleConst;

public class EnterpriseCardMessageHolder extends BaseViewHolder {
	public TextView etvTitle;
	public TextView etvSummary;
	public RelativeLayout rltContent;
	public ImageView sendFailedView;
	public ImageView mImageShow;
	public TextView sTvHasRead;
	public ImageView sendStatus;
    public CheckBox multiCheckBox;

	public EnterpriseCardMessageHolder(View itemView, Activity activity , MessageChatListAdapter adapter , BaseChatContract.Presenter presenter) {
		super(itemView ,activity ,adapter ,presenter);
		etvTitle = itemView.findViewById(R.id.etvTitle);
		etvSummary = itemView.findViewById(R.id.etvSummary);
		rltContent = itemView.findViewById(R.id.rltContent);
		sendFailedView = itemView.findViewById(R.id.imageview_msg_send_failed);
		mImageShow = itemView.findViewById(R.id.ivMeetYou);
		sTvHasRead = itemView.findViewById(R.id.tv_has_read);
		sendStatus = itemView.findViewById(R.id.iv_send_status);
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
                case Type.TYPE_MSG_T_CARD_SEND:
                case Type.TYPE_MSG_T_CARD_SEND_CCIND:
                    params.topToTop = R.id.rltContent;
                    params.bottomToBottom = R.id.rltContent;

                    break;
                case Type.TYPE_MSG_T_CARD_RECV:
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
		int msgType = mMessage.getType();
		int receipt = mMessage.getMessage_receipt();
        if (isEPGroup || isPartyGroup) {
            if (msgType == Type.TYPE_MSG_T_CARD_SEND || msgType == Type.TYPE_MSG_T_CARD_SEND_CCIND) {
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
            } else {
                sTvHasRead.setVisibility(View.GONE);
            }
        } else if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT
                && receipt != -1
                && (msgType == Type.TYPE_MSG_T_CARD_SEND || msgType == Type.TYPE_MSG_T_CARD_SEND_CCIND)) {
            if (status == Status.STATUS_OK) {
                sTvHasRead.setVisibility(View.VISIBLE);
                sendStatus.setVisibility(View.VISIBLE);
                if (msgType == Type.TYPE_MSG_T_CARD_SEND_CCIND) {
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
	}

	public void bindText(){
        String subTitile = mMessage.getSubTitle();
        String subBody = mMessage.getSubBody();

        etvTitle.setText(subTitile);
        if (TextUtils.isEmpty(subBody)) {
            etvSummary.setText(subTitile);
        } else {
            etvSummary.setText(subBody);
        }
        String subImgPath = mMessage.getSubImgPath();
        if (TextUtils.isEmpty(subImgPath)) {
            mImageShow.setImageResource(R.drawable.cc_chat_shareimage_default);//cc_chat_link_pccupancychart
        } else {
            RequestOptions options = new RequestOptions().placeholder(R.drawable.cc_chat_shareimage_default).error(R.drawable.cc_chat_shareimage_default);
            Glide.with(mContext).asBitmap().load(subImgPath).apply(options).into(mImageShow);
        }
    }
}
