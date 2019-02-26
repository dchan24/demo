package com.cmicc.module_message.ui.adapter.message;

import android.app.Activity;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.module.proxys.moduleaboutme.AboutMeProxy;
import com.app.module.proxys.modulecontact.ContactProxy;
import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_business.util.Status;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_contact.model.SimpleContact;
import com.chinamobile.app.yuliao_core.db.LoginDaoImpl;
import com.chinamobile.icloud.im.sync.model.OrganizationKind;
import com.chinamobile.icloud.im.sync.model.PhoneKind;
import com.chinamobile.icloud.im.sync.model.RawContact;
import com.chinamobile.icloud.im.sync.model.StructuredNameKind;
import com.chinamobile.icloud.im.vcard.ReadVCardAndAddContacts;
import com.cmcc.cmrcs.android.contact.data.ContactsCache;
import com.cmcc.cmrcs.android.glide.GlidePhotoLoader;
import com.cmicc.module_message.ui.adapter.MessageChatListAdapter;
import com.cmicc.module_message.ui.constract.BaseChatContract;
import com.cmcc.cmrcs.android.ui.utils.PhoneUtils;
import com.cmcc.cmrcs.android.widget.RecycleViewConstraintLayout;
import com.cmicc.module_message.R;
import com.constvalue.MessageModuleConst;

import java.util.List;

import static android.support.v7.widget.RecyclerView.NO_POSITION;

public class VcardSendMsgHolder extends BaseViewHolder {

	public RelativeLayout cardContentRl; // 名片内容的整个父View
	public RelativeLayout head_rl;       // 头像父View
	public ImageView headImg;            // 头像
	public LinearLayout nameCommpanyLl;  // 名字&公司父View
	public TextView cardNameTv;          // 名片中的名字
	public TextView cardCompanyTv;       // 名片中的公司
	private View cardDividerLine ;       // 名片布局的横线
	private TextView personalCardStip ;  //  个人名片的提示语
    public ImageView sendFailedView;
    public ProgressBar layoutLoading;
    public ImageView sendStatus;
    public TextView sTvHasRead;
	public CheckBox multiCheckBox;

	public VcardSendMsgHolder(View itemView, Activity activity , MessageChatListAdapter adapter , BaseChatContract.Presenter presenter) {
		super(itemView ,activity ,adapter ,presenter);
		cardContentRl = itemView.findViewById(R.id.ll);
		head_rl = itemView.findViewById(R.id.head_rl);
		headImg = itemView.findViewById(R.id.contact_icon);
		nameCommpanyLl = itemView.findViewById(R.id.name_commpany_ll);
		cardNameTv = itemView.findViewById(R.id.tv_card_name);
		cardCompanyTv = itemView.findViewById(R.id.tv_company);
		sendFailedView = itemView.findViewById(R.id.imageview_msg_send_failed);
		layoutLoading = itemView.findViewById(R.id.progress_send_small);
		sTvHasRead = itemView.findViewById(R.id.tv_has_read);
		sendStatus = itemView.findViewById(R.id.iv_send_status);
		cardDividerLine = itemView.findViewById(R.id.card_divider_line);
		personalCardStip = itemView.findViewById(R.id.personal_card_stip);
		multiCheckBox = itemView.findViewById(R.id.multi_check);

		sendFailedView.setOnClickListener(new OnMsgFailClickListener());
		cardContentRl.setOnClickListener(new OnVcardClickListener());
		cardContentRl.setOnLongClickListener(new OnMsgContentLongClickListener());
		sTvHasRead.setOnClickListener(new NoDoubleClickListenerX());

	}

	@Override
	public void bindMultiSelectStatus(boolean isMultiSelectMode , boolean isSelected){
		((RecycleViewConstraintLayout)itemView).setMode(isMultiSelectMode);
		if(isMultiSelectMode){
			//消息气泡上下居中
			ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)multiCheckBox.getLayoutParams();
			params.topToTop = R.id.lltContent;
			params.bottomToBottom = R.id.lltContent;
			multiCheckBox.setLayoutParams(params);
			multiCheckBox.setVisibility(View.VISIBLE);
			multiCheckBox.setChecked(isSelected);
		}else{
			multiCheckBox.setVisibility(View.GONE);
		}
	}

	public void bindCard(String rCard) {
		if(!TextUtils.isEmpty(rCard)) {
			cardNameTv.setTextColor(Color.parseColor("#333333"));
			headImg.setVisibility(View.VISIBLE);
			cardCompanyTv.setVisibility(View.VISIBLE);
			cardDividerLine.setVisibility(View.VISIBLE);
			personalCardStip.setVisibility(View.VISIBLE);
			RawContact rawContact = null;
			try {
				rawContact = ReadVCardAndAddContacts.createdVcardStringToContact(mContext.getApplicationContext(), rCard);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (rawContact == null || TextUtils.isEmpty(rCard)) {
				cardNameTv.setText("");
				headImg.setImageResource(R.drawable.contacts_headportrait_default_in);
				cardCompanyTv.setVisibility(View.GONE);
				return;
			}
			//名字
			cardNameTv.setText(rawContact.getStructuredName().getDisplayName());
			if (rawContact.getPhones() != null && rawContact.getPhones().size() >= 1 && rawContact.getPhones().get(0) != null && !TextUtils.isEmpty(rawContact.getPhones().get(0).getNumber())) {
				GlidePhotoLoader.getInstance(mContext.getApplicationContext()).loadPhoto(mContext.getApplicationContext(), headImg, rawContact.getPhones().get(0).getNumber().trim());
			} else {
				headImg.setImageResource(R.drawable.cc_chat_personal_default); //  contacts_headportrait_default_in
			}
			if (rawContact.getOrganizations() != null && rawContact.getOrganizations().size() > 0) {
				OrganizationKind organizationKind = rawContact.getOrganizations().get(0);
				final String company = organizationKind.getCompany();
				if (!TextUtils.isEmpty(company)) {// 公司
					cardCompanyTv.setText(company);
					cardCompanyTv.setVisibility(View.VISIBLE);
					RelativeLayout.LayoutParams lp1 = (RelativeLayout.LayoutParams) head_rl.getLayoutParams();
					lp1.topMargin = (int) AndroidUtil.dip2px(mContext, 17.0f);
					lp1.bottomMargin = (int) AndroidUtil.dip2px(mContext, 17.0f);
					head_rl.setLayoutParams(lp1);

                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) nameCommpanyLl.getLayoutParams();
                    lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    lp.topMargin = (int) AndroidUtil.dip2px(mContext, 18.0f);
                    nameCommpanyLl.setLayoutParams(lp);
                } else {
                    cardCompanyTv.setVisibility(View.GONE);
                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) nameCommpanyLl.getLayoutParams();
                    lp.height = (int) AndroidUtil.dip2px(mContext, 36.0f);
                    lp.topMargin = (int) AndroidUtil.dip2px(mContext, 9.0f);
                    lp.bottomMargin = (int) AndroidUtil.dip2px(mContext, 9.0f);
                    nameCommpanyLl.setLayoutParams(lp);
                }
			} else {
				RelativeLayout.LayoutParams lp1 = (RelativeLayout.LayoutParams) head_rl.getLayoutParams();
				lp1.topMargin = (int) AndroidUtil.dip2px(mContext, 9.0f);
				lp1.bottomMargin = (int) AndroidUtil.dip2px(mContext, 9.0f);
				head_rl.setLayoutParams(lp1);
				cardCompanyTv.setVisibility(View.GONE);
				cardCompanyTv.setVisibility(View.GONE);
				RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) nameCommpanyLl.getLayoutParams();
				lp.height = (int) AndroidUtil.dip2px(mContext, 36.0f);
				lp.topMargin = (int) AndroidUtil.dip2px(mContext, 9.0f);
				lp.bottomMargin = (int) AndroidUtil.dip2px(mContext, 9.0f);
				nameCommpanyLl.setLayoutParams(lp);
			}
		}else{
			cardNameTv.setText(R.string.unknown_business_card);
			cardNameTv.setTextColor(Color.parseColor("#6f6f6f"));
			headImg.setVisibility(View.GONE);
			cardCompanyTv.setVisibility(View.GONE);
			cardDividerLine.setVisibility(View.GONE);
			personalCardStip.setVisibility(View.GONE);
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
                if (msgType == Type.TYPE_MSG_CARD_SEND_CCIND) {
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
                layoutLoading.setVisibility(View.GONE);
                sendFailedView.setVisibility(View.VISIBLE);
                break;
        }
    }

	public class OnVcardClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			// 2018.4.18 YSF
			int position = getAdapterPosition();
			if (position == NO_POSITION) {
				return;
			}
			int p;
			if (adapter.canLoadMore()) {
				p = position - 1;
			} else {
				p = position;
			}
			if (p < 0) {
				return;
			}
			final Message msg = adapter.getItem(p);
			if (msg == null) {
				LogF.e(TAG, "-------- msg is null ------");
				return;
			}
			final String vcardString = msg.getBody();
			RawContact rawContact = ReadVCardAndAddContacts.createdVcardStringToContact(activity.getApplicationContext(), vcardString);
			if (rawContact == null || rawContact.getPhones() == null) {
				return; // 根据名片信息创建名片对象  如果为null 结束一切操作
			}
			List<PhoneKind> phonesList = rawContact.getPhones();
			StructuredNameKind structuredNameKind = rawContact.getStructuredName();
			if (phonesList.size() == 0 || phonesList.get(0) == null || structuredNameKind == null) {
				return;
			}
			String name = structuredNameKind.getDisplayName(); // 名字
			String number = phonesList.get(0).getNumber(); // 手机号码
			SimpleContact simpleContact = ContactsCache.getInstance().searchContactByNumber(number);
			String loginNum = LoginDaoImpl.getInstance().queryLoginUser(activity);
			loginNum = PhoneUtils.getMinMatchNumber(loginNum);
			if (!TextUtils.isEmpty(number) && number.endsWith(loginNum)) { // 自己的情况,loginNum这个没有区号
				AboutMeProxy.g.getUiInterface().goToUserProfileActivity(activity);
			} else if (simpleContact == null) { // 还没有此联系人
				simpleContact = new SimpleContact();
				simpleContact.setName(name);
				simpleContact.setNumber(number); //名片信息 ，公司，职位，邮箱
				ContactProxy.g.getUiInterface().getContactDetailActivityUI().showForSimpleContactWithVCardString(activity, simpleContact, vcardString);
			} else { // 此联系已经存在   发消息 电话 语音 视屏界面
				ContactProxy.g.getUiInterface().getContactDetailActivityUI().showForSimpleContact(activity, simpleContact, 0);
			}
		}
	}
}