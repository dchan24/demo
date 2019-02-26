package com.cmicc.module_message.ui.adapter.message;

import android.app.Activity;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.module.proxys.moduleaboutme.AboutMeProxy;
import com.app.module.proxys.moduleredpager.RedpagerProxy;
import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_business.util.Status;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.application.MyApplication;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_core.util.NumberUtils;
import com.cmicc.module_message.ui.adapter.MessageChatListAdapter;
import com.cmcc.cmrcs.android.ui.callback.HbAuthCallback;
import com.cmicc.module_message.ui.constract.BaseChatContract;
import com.cmcc.cmrcs.android.ui.dialogs.HbAuthDialog;
import com.cmcc.cmrcs.android.ui.dialogs.RedPaperProgressDialog;
import com.cmcc.cmrcs.android.ui.utils.LoginUtils;
import com.cmcc.cmrcs.android.ui.utils.NickNameUtils;
import com.cmcc.cmrcs.android.ui.utils.PhoneUtils;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmcc.cmrcs.android.widget.RecycleViewConstraintLayout;
import com.cmicc.module_message.R;
import com.constvalue.MessageModuleConst;
import com.feinno.redpaper.sdk.IQueryHbAuthCallback;
import com.feinno.redpaper.utils.SdkInitManager4Red;

import static android.support.v7.widget.RecyclerView.NO_POSITION;

public class RedpaperMsgHolder extends BaseViewHolder {
	private static final String TAG = RedpaperMsgHolder.class.getSimpleName();
	public ImageView sendFailedView;
	public ProgressBar layoutLoading;
	public RelativeLayout redpaper;
	public View redButton;
	public TextView redpaperTitle;
	public TextView redpaperType;
	public TextView sTvHasRead;
	public ImageView sendStatus;
	HbAuthDialog mHbAuthDialog;
	RedPaperProgressDialog mRedPaperProgressDialog;
	public CheckBox multiCheckBox;

	public RedpaperMsgHolder(View itemView , final Activity activity , final MessageChatListAdapter adapter , final BaseChatContract.Presenter presenter) {
		super(itemView ,activity ,adapter ,presenter);
		sendFailedView = (ImageView) itemView.findViewById(R.id.imageview_msg_send_failed);
		layoutLoading = (ProgressBar) itemView.findViewById(R.id.progress_send_small);
		redpaper = (RelativeLayout) itemView.findViewById(R.id.redpaper_type_background);
		redButton = itemView.findViewById(R.id.red_button);
		redpaperTitle = (TextView) itemView.findViewById(R.id.red_title);
		redpaperType = (TextView) itemView.findViewById(R.id.red_type);
		sTvHasRead = (TextView) itemView.findViewById(R.id.tv_has_read);
		sendStatus = (ImageView) itemView.findViewById(R.id.iv_send_status);
		multiCheckBox = itemView.findViewById(R.id.multi_check);

		sTvHasRead.setOnClickListener(new NoDoubleClickListenerX());

		sendFailedView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				int position = getAdapterPosition();
				if (position == NO_POSITION) {
					return;
				}
				position = adapter.canLoadMore() ? position - 1 : position;
				final Message msg = adapter.getItem(position);
				presenter.reSend(msg);
			}
		});

		redpaper.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int position = getAdapterPosition();
				if (position == -1) {
					return;
				}
				String mLoginUserAddress = LoginUtils.getInstance().getLoginUserName();
				if (PhoneUtils.isNotChinaNum(mLoginUserAddress)) {
					BaseToast.show(activity, activity.getString(R.string.not_support_redpager));
					return;
				}
				if (!LoginUtils.getInstance().isLogined()) {
					BaseToast.show(activity, activity.getString(R.string.check_your_net));
					return;
				}
				mRedPaperProgressDialog = RedPaperProgressDialog.getInstance(mContext);
				mRedPaperProgressDialog.setRedPaperProgressListener(new RedPaperProgressDialog.RedPaperProgressListener() {
					@Override
					public void onSuccess() {
						goToRedpager();
					}

					@Override
					public void onFail() {
					}

					@Override
					public void onDisappear() {
					}
				});
				mRedPaperProgressDialog.show();
				mRedPaperProgressDialog.loading();
			}
		});
		redpaper.setOnLongClickListener(new OnMsgContentLongClickListener());
	}

	@Override
	public void bindMultiSelectStatus(boolean isMultiSelectMode , boolean isSelected){
		((RecycleViewConstraintLayout)itemView).setMode(isMultiSelectMode);
		if(isMultiSelectMode){
			//头像不显示，以消息气泡上下居中
			ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)multiCheckBox.getLayoutParams();
			int type = mMessage.getType();
			switch (type){
				case Type.TYPE_MSG_BAG_SEND:
				case Type.TYPE_MSG_CASH_BAG_SEND:
					params.topToTop = R.id.lltContent;
					params.bottomToBottom = R.id.lltContent;
					break;
				case Type.TYPE_MSG_BAG_RECV:
				case Type.TYPE_MSG_CASH_BAG_RECV:
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

	private void goToRedpager() {
		final HbAuthCallback hbAuthCallback = new HbAuthCallback() {
			@Override
			public void onGrantedAuthSuccess() {
				if (null != mHbAuthDialog && mHbAuthDialog.isShowing()) {
					mHbAuthDialog.dismiss();
					mHbAuthDialog = null;
					BaseToast.show(R.string.addHbAuth_onSuccess);
				}
				final Message msg = mMessage;
				if(msg == null){
					return;
				}

				if (msg.getStatus() != Status.STATUS_OK) {
					LogF.i(TAG, "  onContentClick  red bag " + msg.getStatus());
					return;
				}
				String xml_content = msg.getXml_content();
				if (TextUtils.isEmpty(xml_content)) {
					LogF.i(TAG, "  onContentClick  xml_content is null ");
					BaseToast.show(activity, activity.getString(R.string.redpacket_not_right));
					return;
				}
				try {
					LogF.i(TAG, "  onContentClick  xml_content = " + xml_content);
					String service_type = RedpagerProxy.g.getUiInterface().parseRedpager4Bean4XmlFromApp(xml_content, "body");
					if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
						String nickName = NickNameUtils.getNickName(activity, LoginUtils.getInstance().getLoginUserName(), msg.getAddress());
						nickName = nickName.trim();
						RedpagerProxy.g.getUiInterface().showRedpagerDialog(activity, service_type, nickName, msg.getAddress(), xml_content, true);
					} else if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT) {
						String sender = "";
						String familyName = AboutMeProxy.g.getServiceInterface().getMyProfileFamilyName(mContext.getApplicationContext());
						String givenName = AboutMeProxy.g.getServiceInterface().getMyProfileGiveName(mContext.getApplicationContext());
						sender = familyName + givenName;
						if (TextUtils.isEmpty(sender)) {
							sender = NumberUtils.formatPerson(LoginUtils.getInstance().getLoginUserName());
						}
						RedpagerProxy.g.getUiInterface().showRedpagerDialog(activity, service_type, sender, msg.getAddress(), xml_content, false);
					}
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
					LogF.e(TAG, "onGrantedAuthSuccess ,Error!");
				}
			}

			@Override
			public void onGrantedAuthFailed() {
				if (null != mHbAuthDialog && mHbAuthDialog.isShowing()) {
					mHbAuthDialog.dismiss();
					mHbAuthDialog = null;
					BaseToast.show(com.cmic.module_base.R.string.addHbAuth_onFailed);
				}
			}

			@Override
			public void onDeniedAuth() {
				if (null != mHbAuthDialog && mHbAuthDialog.isShowing()) {
					mHbAuthDialog.dismiss();
					mHbAuthDialog = null;
				}
			}
		};
		SdkInitManager4Red.getInstance().queryHbAuth(mContext, new IQueryHbAuthCallback() {

			@Override
			public void onGranted() {
				LogF.i(TAG, "SdkInitManager4Red.queryHbAuth--->onGranted--->和包已授权");
				if (null != mRedPaperProgressDialog && mRedPaperProgressDialog.isShowing()) {
					mRedPaperProgressDialog.dismiss();
				}
				// 和包已授权直接进入钱包
				hbAuthCallback.onGrantedAuthSuccess();
			}

			@Override
			public void onDenied() {
				LogF.i(TAG, "SdkInitManager4Red.queryHbAuth--->onDenied--->和包未授权");
				if (null != mRedPaperProgressDialog && mRedPaperProgressDialog.isShowing()) {
					mRedPaperProgressDialog.dismiss();
				}
				mHbAuthDialog = HbAuthDialog.getInstance(mContext);
				mHbAuthDialog.setHbAuthCallback(hbAuthCallback);
				mHbAuthDialog.show();
			}

			@Override
			public void onFailed(String msg) {
				LogF.i(TAG, "SdkInitManager4Red.queryHbAuth--->onFailed--->msg:" + msg);
				if (null != mRedPaperProgressDialog && mRedPaperProgressDialog.isShowing()) {
					mRedPaperProgressDialog.dismiss();
				}
				if (null != msg && (msg.toLowerCase().contains("unknownhostexception") || msg.toLowerCase().contains("connectexception"))) {
					BaseToast.show(R.string.public_net_exception);
				} else {
					BaseToast.show(R.string.onFailed_err);
				}
			}

			@Override
			public void onTokenError() {
				LogF.i(TAG, "SdkInitManager4Red.queryHbAuth--->onTokenError--->和包授权查询失败，token失效");
				RedpagerProxy.g.getUiInterface().initRedPaper(MyApplication.getAppContext());
				if (null != mRedPaperProgressDialog) {
					mRedPaperProgressDialog.loading();
				}
			}
		});
	}

	public void bindContent(){
		int type = mMessage.getType();
		String title_bag = mMessage.getTitle();
		if (!TextUtils.isEmpty(title_bag)) {
			redpaperTitle.setText(title_bag);
		}
		//是否为打开状态
		boolean isOpen = mMessage.getExtStatus() == 1;
		if (type == Type.TYPE_MSG_BAG_SEND || type == Type.TYPE_MSG_BAG_RECV) {
			redpaperType.setText(mContext.getString(R.string.fection_flow));
			if (isOpen) {
				redpaper.setBackgroundResource(R.drawable.cc_chat_redbag_4g_alreadyclicked);
				redButton.setVisibility(View.INVISIBLE);
				redpaperTitle.setTextColor(mContext.getResources().getColor(R.color.color_fff4e4));
				redpaperType.setTextColor(mContext.getResources().getColor(R.color.color_f6c9c6));
			} else {
				redpaper.setBackgroundResource(R.drawable.cc_chat_redbag_4g_normal);
				redButton.setVisibility(View.VISIBLE);
				redpaperTitle.setTextColor(mContext.getResources().getColor(R.color.color_fadbac));
				redpaperType.setTextColor(mContext.getResources().getColor(R.color.color_e37f76));
			}
		} else if (type == Type.TYPE_MSG_CASH_BAG_SEND || type == Type.TYPE_MSG_CASH_BAG_RECV) {
			redpaperType.setText(mContext.getString(R.string.fection_cash));
			if (isOpen) {
				redpaper.setBackgroundResource(R.drawable.cc_chat_redbag_cash_alreadyclicked);
				redButton.setVisibility(View.INVISIBLE);
				redpaperTitle.setTextColor(mContext.getResources().getColor(R.color.color_fff4e4));
				redpaperType.setTextColor(mContext.getResources().getColor(R.color.color_f6c9c6));
			} else {
				redpaper.setBackgroundResource(R.drawable.cc_chat_redbag_cash_normal);
				redButton.setVisibility(View.VISIBLE);
				redpaperTitle.setTextColor(mContext.getResources().getColor(R.color.color_fadbac));
				redpaperType.setTextColor(mContext.getResources().getColor(R.color.color_e37f76));
			}
		}
	}

	@Override
	public void bindSendStatus() {
		int type = mMessage.getType();
		int receipt = mMessage.getMessage_receipt();

		if (type == Type.TYPE_MSG_BAG_SEND || type == Type.TYPE_MSG_CASH_BAG_SEND) {
			int status = mMessage.getStatus();
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
				case Status.STATUS_PAUSE:
					layoutLoading.setVisibility(View.GONE);
					sendFailedView.setVisibility(View.VISIBLE);
					break;
				default:
					layoutLoading.setVisibility(View.VISIBLE);
					sendFailedView.setVisibility(View.GONE);
					break;

			}
		}
	}
}
