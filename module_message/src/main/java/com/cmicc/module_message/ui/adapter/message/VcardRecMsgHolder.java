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
import com.cmcc.cmrcs.android.ui.control.ComposeMessageActivityControl;
import com.cmcc.cmrcs.android.ui.utils.PhoneUtils;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmcc.cmrcs.android.widget.RecycleViewConstraintLayout;
import com.cmicc.module_message.R;

import java.util.List;

import static android.support.v7.widget.RecyclerView.NO_POSITION;

public class VcardRecMsgHolder extends BaseViewHolder {

	public RelativeLayout cardContentRl; // 名片内容的整个父View
	public RelativeLayout head_rl;       // 头像父View
	public ImageView headImg;            // 头像
	public LinearLayout nameCommpanyLl;  // 名字&公司父View
	public TextView cardNameTv;          // 名片中的名字
	public TextView cardCompanyTv;       // 名片中的公司
	private View cardDividerLine ;       // 名片布局的横线
	private TextView personalCardStip ;  //  个人名片的提示语
	public ProgressBar recLoadingPb;     // 接受进度
	public ImageView recFailedImg;       // 接受失败
	public TextView sTvHasRead;          // 已读动态
	public CheckBox multiCheckBox;

	public VcardRecMsgHolder(View itemView, Activity activity , final MessageChatListAdapter adapter , BaseChatContract.Presenter presenter) {
		super(itemView ,activity ,adapter ,presenter);
		cardContentRl = itemView.findViewById(R.id.ll);
		head_rl = itemView.findViewById(R.id.head_rl);
		headImg = itemView.findViewById(R.id.contact_icon);
		nameCommpanyLl = itemView.findViewById(R.id.name_commpany_ll);
		cardNameTv = itemView.findViewById(R.id.tv_card_name);
		cardCompanyTv = itemView.findViewById(R.id.tv_company);
		recFailedImg = itemView.findViewById(R.id.imageview_msg_rec_failed);
		recLoadingPb = itemView.findViewById(R.id.progress_rec_small);
		sTvHasRead = itemView.findViewById(R.id.tv_has_read);
		cardDividerLine = itemView.findViewById(R.id.card_divider_line);
		personalCardStip = itemView.findViewById(R.id.personal_card_stip);
		multiCheckBox = itemView.findViewById(R.id.multi_check);

		cardContentRl.setOnClickListener(new OnVcardClickListener());
		cardContentRl.setOnLongClickListener(new OnMsgContentLongClickListener());
		sTvHasRead.setOnClickListener(new NoDoubleClickListenerX());
		recFailedImg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int position = getAdapterPosition();
				if (position == NO_POSITION) {
					return;
				}
				if (adapter.canLoadMore() && position < 1) {
					return;
				}
				final Message msg = adapter.getItem(adapter.canLoadMore() ? position - 1 : position);
				if (msg.getStatus() == Status.STATUS_PAUSE || msg.getStatus() == Status.STATUS_FAIL ) {
					if (!AndroidUtil.isNetworkConnected(mContext)) {
						BaseToast.show(R.string.network_disconnect);
						return;
					}
					ComposeMessageActivityControl.resumeFileTransmission(msg, isGroupChat);
				}
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
				params.topToTop = R.id.lltContent;
				params.bottomToBottom = R.id.lltContent;
			}else{
				params.topToTop = R.id.svd_head;
				params.bottomToBottom = R.id.svd_head;
			}
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
				if (organizationKind != null) {
					String company = organizationKind.getCompany();
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
				}
			} else {

				RelativeLayout.LayoutParams lp1 = (RelativeLayout.LayoutParams) head_rl.getLayoutParams();
				lp1.topMargin = (int) AndroidUtil.dip2px(mContext, 9.0f);
				lp1.bottomMargin = (int) AndroidUtil.dip2px(mContext, 9.0f);
				head_rl.setLayoutParams(lp1);

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


	/**
     * 下载状态
	 */
	public void bindDownloadStatus() {
		int status = mMessage.getStatus();
		LogF.d(TAG , "status = "+ status);
		switch (status) {
			case Status.STATUS_WAITING:
			case Status.STATUS_LOADING:
				recLoadingPb.setVisibility(View.VISIBLE);
				recFailedImg.setVisibility(View.GONE);
				break;
			case Status.STATUS_OK:
				recLoadingPb.setVisibility(View.GONE);
				recFailedImg.setVisibility(View.GONE);
				break;
			case Status.STATUS_FAIL:
				recLoadingPb.setVisibility(View.GONE);
				recFailedImg.setVisibility(View.VISIBLE);
				break;
			case Status.STATUS_PAUSE:
				recLoadingPb.setVisibility(View.GONE);
				recFailedImg.setVisibility(View.VISIBLE);
				break;
			default:
				recLoadingPb.setVisibility(View.GONE);
				recFailedImg.setVisibility(View.VISIBLE);
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

			String num = number;
			if (num.contains("+86")) {
				num = num.replace("+86", "");
			}
			if (TextUtils.equals(num, loginNum)) { // 自己的情况
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