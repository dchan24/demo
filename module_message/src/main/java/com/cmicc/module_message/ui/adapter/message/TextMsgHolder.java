package com.cmicc.module_message.ui.adapter.message;

import android.app.Activity;
import android.support.constraint.ConstraintLayout;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.app.module.proxys.modulecontact.ContactProxy;
import com.app.module.proxys.moduleenterprise.EnterPriseProxy;
import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_business.util.MessageUtils;
import com.chinamobile.app.yuliao_business.util.Status;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.utils.FontUtil;
import com.chinamobile.app.yuliao_common.utils.Log;
import com.chinamobile.app.yuliao_common.utils.PopWindowFor10GUtil;
import com.chinamobile.app.yuliao_common.view.PopWindowFor10G;
import com.chinamobile.app.yuliao_core.util.NumberUtils;
import com.cmicc.module_message.ui.adapter.MessageChatListAdapter;
import com.cmicc.module_message.ui.constract.BaseChatContract;
import com.cmcc.cmrcs.android.ui.utils.CallRecordsUtils;
import com.cmcc.cmrcs.android.ui.utils.PhoneUtils;
import com.cmcc.cmrcs.android.ui.view.MessageOprationDialog;
import com.cmcc.cmrcs.android.widget.RecycleViewConstraintLayout;
import com.cmcc.cmrcs.android.widget.emoji.EmojiParser;
import com.cmcc.cmrcs.android.widget.emoji.EmojiTextView;
import com.cmicc.module_message.R;
import com.constvalue.MessageModuleConst;

public class TextMsgHolder extends BaseViewHolder {
	public EmojiTextView sTvMessage;
	public ProgressBar layoutLoading;
	public ImageView sendFailedView;
	public LinearLayout llSmsMark;
	public TextView sTvHasRead;
	public TextView sTvSmsMark;
	public ImageView sendStatus;
	public CheckBox multiCheckBox;
	public View marginView;

	public TextMsgHolder(View itemView, final Activity activity , final MessageChatListAdapter adapter , BaseChatContract.Presenter presenter) {
		super(itemView ,activity ,adapter ,presenter);
		sTvMessage = itemView.findViewById(R.id.tv_message);
		layoutLoading = itemView.findViewById(R.id.progress_send_small);
		sendFailedView = itemView.findViewById(R.id.imageview_msg_send_failed);
		llSmsMark = itemView.findViewById(R.id.ll_sms_mark);
		sTvHasRead = itemView.findViewById(R.id.tv_has_read);
		sTvSmsMark = itemView.findViewById(R.id.tv_sms_mark);
		sendStatus = itemView.findViewById(R.id.iv_send_status);
		multiCheckBox = itemView.findViewById(R.id.multi_check);
		marginView = itemView.findViewById(R.id.margin_view);

		sendFailedView.setOnClickListener(new OnMsgFailClickListener());
		sTvMessage.setOnClickListener(new NoDoubleClickListener());
		sTvHasRead.setOnClickListener(new NoDoubleClickListenerX());
		sTvMessage.setOnLongClickListener(new OnMsgContentLongClickListener());
		sTvMessage.setLinkClickListener(new EmojiTextView.OnLinkClickListener() {
			@Override
			public void onLinkClick(String mUrl, View widget) {
				if (!adapter.isLongClick) {
					if (mUrl.startsWith("http") || mUrl.startsWith("ftp") || mUrl.startsWith("https")) {
						EnterPriseProxy.g.getUiInterface().jumpToCommonBrowserWithShare(activity, mUrl);
					} else if (PhoneUtils.isPhoneNumber(mUrl)) {
						showPhoneNumberDialog(mUrl, true);
					} else if (NumberUtils.isNumeric(mUrl)) {
						showPhoneNumberDialog(mUrl, false);
					} else if (PhoneUtils.isHongKongFixNumber(mUrl)) {
						showPhoneNumberDialog(mUrl, true);
					} else if (PhoneUtils.isHongKongPhone(mUrl)) {
						showPhoneNumberDialog(mUrl, true);
					} else {
						EnterPriseProxy.g.getUiInterface().jumpToCommonBrowserWithShare(activity, "http://" + mUrl);
					}
				}
			}
		});
		sTvMessage.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						adapter.isLongClick = false;
						break;
				}
				return false;
			}
		});

	}

	private void showPhoneNumberDialog(final String number, final boolean isPhoneNumber) {
		String[] itemList = activity.getResources().getStringArray(R.array.msg_number_click);
//		if (App.getApplication() == null) {
//			itemList = activity.getResources().getStringArray(R.array.msg_number_click);
//		} else {
//			itemList = activity.getResources().getStringArray(R.array.msg_number_click);
//		}

		final MessageOprationDialog messageOprationDialog = new MessageOprationDialog(activity, null, itemList, null);
		messageOprationDialog.setOnMessageItemClickListener(new MessageOprationDialog.OnOprationItemClickListener() {
			@Override
			public void onClick(String item, int which, String address) {
				if (which == 1) {
					MessageUtils.copyToClipboard(activity, number);
				} else if (which == 0) {
					messageOprationDialog.dismiss();
					if (isPhoneNumber) {
						showCallDialog(number);
					} else {
						if (PhoneUtils.isHongKongNumber(number)) {
							if (!number.startsWith("+852")) {
								CallRecordsUtils.normalCall(activity, "+852" + number);
								return;
							}
						}
						CallRecordsUtils.normalCall(activity, number);
					}
				} else if (which == 2) {
					ContactProxy.g.getUiInterface().getNewContactActivityUI().showForStrangeNumber(activity, number);
				}
			}
		});
		messageOprationDialog.show();
	}

	@Override
	public void bindMultiSelectStatus(boolean isMultiSelectMode , boolean isSelected){
		((RecycleViewConstraintLayout)itemView).setMode(isMultiSelectMode);
		if(isMultiSelectMode){
			//头像不显示，以消息气泡上下居中
			ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)multiCheckBox.getLayoutParams();
			int type = mMessage.getType();
			switch (type){
				case Type.TYPE_MSG_TEXT_RECV:
				case Type.TYPE_MSG_SMS_RECV:
					if(sIvHead.getVisibility() == View.INVISIBLE){
						params.topToTop = R.id.ll;
						params.bottomToBottom = R.id.ll;
					}else{
						params.topToTop = R.id.svd_head;
						params.bottomToBottom = R.id.svd_head;
					}
					break;
				case Type.TYPE_MSG_TEXT_SEND:
				case Type.TYPE_MSG_TEXT_SEND_CCIND:
				case Type.TYPE_MSG_SMS_SEND:
				case Type.TYPE_MSG_TEXT_QUEUE:
				case Type.TYPE_MSG_TEXT_OUTBOX:
				case Type.TYPE_MSG_TEXT_DRAFT:
				case Type.TYPE_MSG_TEXT_FAIL:
				case Type.TYPE_MSG_TEXT_SUPER_SMS_SEND:
					params.topToTop = R.id.ll;
					params.bottomToBottom = R.id.ll;
					break;
				default:
					if(sIvHead.getVisibility() == View.INVISIBLE){
						params.topToTop = R.id.ll;
						params.bottomToBottom = R.id.ll;
					}else{
						params.topToTop = R.id.svd_head;
						params.bottomToBottom = R.id.svd_head;
					}
					break;
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

	private void showCallDialog(final String phone) {
		String[] itemList = activity.getResources().getStringArray(R.array.msg_call_number_click);
//		if (App.getApplication() == null) {
//			itemList = activity.getResources().getStringArray(R.array.msg_call_number_click);
//		} else {
//			itemList = activity.getResources().getStringArray(R.array.msg_call_number_click);
//		}

		MessageOprationDialog messageOprationDialog = new MessageOprationDialog(activity, null, itemList, null);
		messageOprationDialog.setOnMessageItemClickListener(new MessageOprationDialog.OnOprationItemClickListener() {
			@Override
			public void onClick(String item, int which, String address) {
				if (item.equals(activity.getString(R.string.common_call_dialog_hint))) {
					if (PhoneUtils.isHongKongNumber(phone)) {
						if (!phone.startsWith("+852")) {
							CallRecordsUtils.normalCall(activity, "+852" + phone);
							return;
						}
					}
					CallRecordsUtils.normalCall(activity, phone);
				} else if (item.equals(activity.getString(R.string.voice_call_dialog_hint))) {
					if(PopWindowFor10GUtil.isNeedPop()) {
						PopWindowFor10G m10GPopWindow = new PopWindowFor10G(activity);
						m10GPopWindow.setType(PopWindowFor10GUtil.TYPE_FOR_CALL);
						m10GPopWindow.setCallerInfo(phone, phone);
						m10GPopWindow.showAsDropDown(sTvMessage, 0, 0, Gravity.CENTER);
					}else {
						CallRecordsUtils.voiceCall(activity, phone, false, phone);
					}
				} else if (item.equals(activity.getString(R.string.video_call_dialog_hint))) {
					if(PopWindowFor10GUtil.isNeedPop()) {
						PopWindowFor10G m10GPopWindow = new PopWindowFor10G(activity);
						m10GPopWindow.setType(PopWindowFor10GUtil.TYPE_FOR_VIDEO_CALL);
						m10GPopWindow.setCallerInfo(phone, phone);
						m10GPopWindow.showAsDropDown(sTvMessage, 0, 0, Gravity.CENTER);
					}else {
						CallRecordsUtils.voiceCall(activity, phone, true, phone);
					}
				}
			}
		});
		messageOprationDialog.show();
	}

	@Override
	public void bindSendStatus() {
        int status = mMessage.getStatus();
        int msgType = mMessage.getType();
        int receipt = mMessage.getMessage_receipt();
        if (msgType == Type.TYPE_MSG_TEXT_QUEUE
                || msgType == Type.TYPE_MSG_TEXT_OUTBOX
                || msgType == Type.TYPE_MSG_TEXT_DRAFT
                || msgType == Type.TYPE_MSG_TEXT_FAIL) {
            status = Status.getStatusFromType(msgType);
            msgType = Type.TYPE_MSG_TEXT_SEND;
        }
        if (status == -1 || status == 0) {
            status = Status.STATUS_OK;
        }
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
        } else if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT
                && (msgType == Type.TYPE_MSG_TEXT_SEND || msgType == Type.TYPE_MSG_TEXT_SEND_CCIND)
                && receipt != -1) {
            if (status == Status.STATUS_OK) {
                sTvHasRead.setVisibility(View.VISIBLE);
                sendStatus.setVisibility(View.VISIBLE);
                if (msgType == Type.TYPE_MSG_TEXT_SEND_CCIND) {
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
                layoutLoading.setVisibility(View.VISIBLE);
                sendFailedView.setVisibility(View.GONE);
                break;
            case Status.STATUS_OK:
                layoutLoading.setVisibility(View.GONE);
                sendFailedView.setVisibility(View.GONE);
                break;
            case Status.STATUS_FAIL:
                layoutLoading.setVisibility(View.GONE);
                sendFailedView.setVisibility(View.VISIBLE);
                break;
            default:
                layoutLoading.setVisibility(View.VISIBLE);
                sendFailedView.setVisibility(View.GONE);
                break;
        }
	}

	public void bindTextSend(){
        Message msg = mMessage;
        int type = mMessage.getType();
        float textSize = 16 * FontUtil.getFontScale();
        CharSequence formatted = msg.getCachedFormattedMessage();
        try {
            textSize = Float.parseFloat(msg.getTextSize()) * FontUtil.getFontScale();
            Log.i(TAG, "msg.textSize:" + textSize + "sp  msg.body:" + msg.getBody());
        } catch (Exception e) {
            e.printStackTrace();
        }
        sTvMessage.setTextSize(textSize);
        if (formatted != null) {
            sTvMessage.setLinkText(formatted);
        } else {
            int mEmojiconSize = (int) sTvMessage.getTextSize() + (int) AndroidUtil.dip2px(mContext, 5);
            CharSequence builder = EmojiParser.getInstance(mContext).replaceAllEmojis(mContext, msg.getBody(), mEmojiconSize);
            msg.setCachedFormattedMessage(builder);
            sTvMessage.setLinkText(builder);
        }
        if (rightColorId == mContext.getResources().getColor(R.color.color_4991fb)) {
            sTvMessage.setLinkTextColor(mContext.getResources().getColor(R.color.color_fffa66));
        } else {
            sTvMessage.setLinkTextColor(mContext.getResources().getColor(R.color.white));// color_4991fb
        }

        if (type == Type.TYPE_MSG_TEXT_SUPER_SMS_SEND
                ||type == Type.TYPE_MSG_SMS_SEND) {
            llSmsMark.setVisibility(View.VISIBLE);
            sTvSmsMark.setText(R.string.fragment_share_app_share_sms);
        } else {
            llSmsMark.setVisibility(View.GONE);
        }
	}

	public void bindTextRecv(){

	    Message msg = mMessage;
	    int type = mMessage.getType();
        float textSize = 16 * FontUtil.getFontScale();
        CharSequence formatted = msg.getCachedFormattedMessage();
        if (msg.getTextSize() != null) {
            try {
                textSize = Float.parseFloat(msg.getTextSize()) * FontUtil.getFontScale();
                Log.i(TAG, "msg.textSize:" + textSize + "sp  msg.body:" + msg.getBody());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        sTvMessage.setTextSize(textSize);
        if (formatted != null) {
            sTvMessage.setLinkText(formatted);
        } else {
            int mEmojiconSize = (int) sTvMessage.getTextSize() + (int) AndroidUtil.dip2px(mContext, 5);
            CharSequence builder = EmojiParser.getInstance(mContext).replaceAllEmojis(mContext, msg.getBody(), mEmojiconSize);
            msg.setCachedFormattedMessage(builder);
            sTvMessage.setLinkText(builder);
        }
        sTvMessage.setLinkTextColor(mContext.getResources().getColor(R.color.color_4991fb));



        if (type == Type.TYPE_MSG_SMS_RECV) {
            llSmsMark.setVisibility(View.VISIBLE);
        } else {
            llSmsMark.setVisibility(View.GONE);
        }
    }
}
