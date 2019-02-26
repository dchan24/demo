package com.cmicc.module_message.ui.adapter.message;

import android.app.Activity;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.app.module.proxys.modulemessage.MessageProxy;
import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.yuliao_business.util.Status;
import com.chinamobile.app.yuliao_business.util.Type;
import com.cmicc.module_message.ui.adapter.MessageChatListAdapter;
import com.cmicc.module_message.ui.constract.BaseChatContract;
import com.cmcc.cmrcs.android.widget.RecycleViewConstraintLayout;
import com.cmicc.module_message.R;
import com.constvalue.MessageModuleConst;

public class LocationMsgSendHolder extends BaseViewHolder {
	private View mLocationInfoView;
	public TextView mTvTime;
	public TextView mTextName;
	public TextView sTvHasRead;
	public TextView mFamousAddress;
	public ImageView sendFailedView;
	public ImageView mSvHead;
	public ImageView mMapImage;
	public ImageView sendStatus;
	public ProgressBar layoutLoading;
	private double mLongitude;
	private double mLatitude;
	private String mSpecialAddress;
	private String mAddress;
    public CheckBox multiCheckBox;

	public LocationMsgSendHolder(View itemView, final Activity activity , final MessageChatListAdapter adapter , BaseChatContract.Presenter presenter) {
		super(itemView ,activity ,adapter ,presenter);
		mLocationInfoView = itemView.findViewById(R.id.location_info_view);
		sendFailedView = itemView.findViewById(R.id.loc_msg_send_failed);
		layoutLoading = itemView.findViewById(R.id.progress_send_small);
		mTvTime = itemView.findViewById(R.id.tv_time);
		mTextName = itemView.findViewById(R.id.text_name);
		mMapImage = itemView.findViewById(R.id.image_view_lloc_icon);
		mSvHead = itemView.findViewById(R.id.svd_head);
		sTvHasRead = itemView.findViewById(R.id.tv_has_read);
		sTvHasRead.setOnClickListener(new NoDoubleClickListenerX());
		mFamousAddress = itemView.findViewById(R.id.lloc_famous_address_text);
		sendStatus = itemView.findViewById(R.id.iv_send_status);
        multiCheckBox = itemView.findViewById(R.id.multi_check);

        mLocationInfoView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle extras = new Bundle();
				extras.putDouble(MessageModuleConst.INTENT_KEY_FOR_LATITUDE, mLatitude);
				extras.putDouble(MessageModuleConst.INTENT_KEY_FOR_LONGITUDE, mLongitude);
				extras.putString(MessageModuleConst.INTENT_LEY_FOR_LOCATION_ADDRESS, mAddress);
				extras.putString(MessageModuleConst.INTENT_LEY_FOR_LOCATION_SPECIAL_ADDRESS, mSpecialAddress);
				extras.putBoolean(MessageModuleConst.INTENT_KEY_FOR_DISPLAY_MAP, true);
				MessageProxy.g.getUiInterface().startLocationActivityForResult(activity, MessageModuleConst.START_LOCATION_ACTIVITY_REQUEST_MAP_CODE, extras);
			}
		});
		mLocationInfoView.setOnLongClickListener(new OnMsgContentLongClickListener());
		if (sendFailedView != null) {
			sendFailedView.setOnClickListener(new OnMsgFailClickListener());
		}

	}

    @Override
    public void bindMultiSelectStatus(boolean isMultiSelectMode , boolean isSelected){
        ((RecycleViewConstraintLayout)itemView).setMode(isMultiSelectMode);
        if(isMultiSelectMode){
            //头像不显示，以消息气泡上下居中
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)multiCheckBox.getLayoutParams();
            int type = mMessage.getType();
            switch (type){
                case Type.TYPE_MSG_LOC_SEND:
                case Type.TYPE_MSG_LOC_SEND_CCIND:
                    params.topToTop = R.id.lltContent;
                    params.bottomToBottom = R.id.lltContent;

                    break;
                case Type.TYPE_MSG_LOC_RECV:
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

	public void setSafeVisibility(View v, int visibility) {
		if (v == null)
			return;
		v.setVisibility(visibility);
	}

	public void setLongitude(double longitude) {
		mLongitude = longitude;
	}

	public void setLatitude(double latitude) {
		mLatitude = latitude;
	}

	public void setSpecialAddress(String specialAddress) {
		mSpecialAddress = specialAddress;
	}

	public void setAddress(String address) {
		mAddress = address;
	}

	@Override
	public void bindSendStatus() {
		int status = mMessage.getStatus();
		int msgType = mMessage.getType();
		int receipt = mMessage.getMessage_receipt();
        if (isEPGroup || isPartyGroup) {
            if (msgType == Type.TYPE_MSG_LOC_SEND || msgType == Type.TYPE_MSG_LOC_SEND_CCIND) {
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
        } else if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT && receipt != -1) {
            if (status == Status.STATUS_OK) {
                sTvHasRead.setVisibility(View.VISIBLE);
                sendStatus.setVisibility(View.VISIBLE);
                if (msgType == Type.TYPE_MSG_LOC_SEND_CCIND) {
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
                setSafeVisibility(layoutLoading, View.VISIBLE);
                setSafeVisibility(sendFailedView, View.GONE);
                break;
            case Status.STATUS_OK:
                setSafeVisibility(layoutLoading, View.GONE);
                setSafeVisibility(sendFailedView, View.GONE);
                break;
            case Status.STATUS_FAIL:
            case Status.STATUS_PAUSE:
                setSafeVisibility(layoutLoading, View.GONE);
                setSafeVisibility(sendFailedView, View.VISIBLE);
                break;
            default:
                setSafeVisibility(layoutLoading, View.GONE);
                setSafeVisibility(sendFailedView, View.GONE);
                break;
        }
	}
}
