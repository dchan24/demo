package com.cmicc.module_message.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chinamobile.app.utils.TimeUtil;
import com.chinamobile.app.yuliao_business.model.MailOAConversation;
import com.chinamobile.app.yuliao_business.model.OAList;
import com.chinamobile.app.yuliao_business.util.OAUtils;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_contact.model.SimpleContact;
import com.cmcc.cmrcs.android.contact.data.ContactsCache;
import com.cmcc.cmrcs.android.glide.GlidePhotoLoader;
import com.cmcc.cmrcs.android.ui.utils.MailOAUtils;
import com.cmcc.cmrcs.android.ui.view.dragbubble.RoundNumber;
import com.cmcc.cmrcs.android.widget.emoji.EmojiTextView4Convlist;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.activity.MailOASummaryActivity;

import java.util.List;

/**
 * Created by Dchan on 2018/1/17.
 */

public class MailOAListAdapter extends RecyclerView.Adapter<MailOAListAdapter.MailViewHolder>{
	Context mContext;
	List<MailOAConversation> list;
	public MailOAListAdapter(Context mContext  , List<MailOAConversation> list) {
		this.mContext = mContext;
		this.list = list;
	}

	public List<MailOAConversation> getData() {
		return list;
	}

	public void setData(List<MailOAConversation> list) {
		if(list != null){
			this.list = list;
		}
		notifyDataSetChanged();
	}

	@Override
	public MailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(mContext).inflate(R.layout.item_mail_oa_list, parent, false);
		MailViewHolder mailViewHolder = new MailViewHolder(view);
		return mailViewHolder;
	}

	@Override
	public void onBindViewHolder(MailViewHolder holder, int position) {
		MailOAConversation mailOAConversation = list.get(position);
		int unReadCount = mailOAConversation.getUnReadCount();
		String mailCount = "("+mailOAConversation.getMailCount()+")";
		int attachedCount = mailOAConversation.getAttachedCount();
		if(attachedCount>0){
			holder.mail_attached_imageview.setVisibility(View.VISIBLE);
		}else{
			holder.mail_attached_imageview.setVisibility(View.GONE);
		}
		if (unReadCount <= 0) {
			holder.rnMessageBadge.setVisibility(View.GONE);
		} else {
			holder.rnMessageBadge.setVisibility(View.VISIBLE);
//			holder.rnMessageBadge.setText(dragBubbleView ,unReadCount + "");
//			if (unReadCount > 99){
//				ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) holder.rnMessageBadge.getLayoutParams();
//				layoutParams.height = (int) AndroidUtil.dip2px(mContext,31);
//				layoutParams.width = (int) AndroidUtil.dip2px(mContext,36);
//				holder.rnMessageBadge.setLayoutParams(layoutParams);
//				holder.rnMessageBadge.setImageResource(R.drawable.cc_chat_dot_double_digit);
//				holder.rnMessageBadge.setBackgroundResource(R.drawable.cc_chat_dot_double_digit);
//
//			}else {
//				ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) holder.rnMessageBadge.getLayoutParams();
//				layoutParams.height = (int) AndroidUtil.dip2px(mContext,30);
//				layoutParams.width = (int) AndroidUtil.dip2px(mContext,30);
//				holder.rnMessageBadge.setLayoutParams(layoutParams);
//				if (unReadCount > 9){
//					holder.rnMessageBadge.setImageResource(R.drawable.cc_chat_dot_double_digit);
//				}else {
//					holder.rnMessageBadge.setImageResource(R.drawable.cc_chat_dot_one_digit);
//				}
////				holder.rnMessageBadge.setImageResource(R.drawable.cc_chat_dot_one_digit);
//			}
		}

		int boxType = mailOAConversation.getBoxType();
		String content = mailOAConversation.getContent();
		if((boxType & Type.TYPE_BOX_MAIL_OA) > 0 ){
			// 根据号码查名称和头像
			long rawId = 0;
			SimpleContact contact = ContactsCache.getInstance().searchContactByNumber(mailOAConversation.getAddress());
			if(contact!=null){
				rawId = contact.getRawId();
				holder.sTvConvName.setText(contact.getName()+mailCount);
			}else
				holder.sTvConvName.setText(mailOAConversation.getAddress()+mailCount);

			GlidePhotoLoader.getInstance(mContext).loadPhoto(mContext, holder.sIvHead, mailOAConversation.getAddress());


		}else if((boxType & Type.TYPE_BOX_OA) > 0){
			// 根据号码查名称和头像
			OAList oa = OAUtils.getOA(mContext, mailOAConversation.getAddress()); // 12560040000000202
			if(oa!=null){
				String name = oa.getName();
				if (!TextUtils.isEmpty(name)) {
					holder.sTvConvName.setText(name);
				}else{
					holder.sTvConvName.setText(mailOAConversation.getAddress());
				}
				mailOAConversation.setIconPath(oa.getLogo());
				GlidePhotoLoader.getInstance(mContext).loadOAPhoto(holder.sIvHead,mailOAConversation.getAddress() ,oa.getLogo());
			}else{
				holder.sTvConvName.setText(mailOAConversation.getAddress()+mailCount);
				GlidePhotoLoader.getInstance(mContext).loadOAPhoto(holder.sIvHead,mailOAConversation.getAddress() ,null);
			}

		}

		if (!TextUtils.isEmpty(content)) {
			String replaceAll = content.replaceAll("\n", " ");
			if(unReadCount >0){
				replaceAll = String.format(mContext.getString(R.string.news_unit_), unReadCount > 99 ? "99+" : unReadCount + "") + replaceAll;
			}
			holder.sTvContent.setText(replaceAll);
		} else {
			String replaceAll = mContext.getString(R.string.no_subject);
			if(unReadCount >0){
				replaceAll = String.format(mContext.getString(R.string.news_unit_), unReadCount > 99 ? "99+" : unReadCount + "") + replaceAll;
			}
			holder.sTvContent.setText(replaceAll);
		}

		if (!TextUtils.isEmpty(content) && mailOAConversation.getDate() != 0) {
			holder.sTvDate.setText(TimeUtil.formatTime(mailOAConversation.getDate()));
		} else {
			holder.sTvDate.setText("");
		}
	}

	@Override
	public int getItemCount() {
		return list.size();
	}

	public MailOAConversation getValue(String key) {
		if(list == null){
			return null;
		}
		for(MailOAConversation conversation :list){
			if(conversation.getAddress().equals(key)){
				return conversation;
			}
		}
		return null;
	}

	public class MailViewHolder extends RecyclerView.ViewHolder implements OnClickListener,RoundNumber.DragStateListener{
		View mRootView;
		ImageView sIvHead;

		TextView sTvConvName;

		TextView sTvDate;

		EmojiTextView4Convlist sTvContent;

		ImageView mail_attached_imageview;

		ImageView rnMessageBadge;
		public MailViewHolder(View itemView) {
			super(itemView);
			mRootView =  itemView.findViewById(R.id.rl_conv_list_item);
			sIvHead = (ImageView) itemView.findViewById(R.id.svd_head);
			sTvConvName = (TextView) itemView.findViewById(R.id.tv_conv_name);
			sTvDate = (TextView) itemView.findViewById(R.id.tv_date);
			sTvContent = (EmojiTextView4Convlist) itemView.findViewById(R.id.tv_content);
			mail_attached_imageview = (ImageView) itemView.findViewById(R.id.icon_mail_attached);
			rnMessageBadge = (ImageView) itemView.findViewById(R.id.red_dot_silent);

			mRootView.setOnClickListener(this);
//			rnMessageBadge.setDragListener(this);
		}

		@Override
		public void onClick(View v) {
			int position = getAdapterPosition();
			if(position >= list.size()){
				return;
			}
			MailOAConversation conversation = list.get(position);
			MailOASummaryActivity.startMailOASummaryActivity(mContext ,conversation);
		}

		@Override
		public void onDismiss(RoundNumber roundNumber) {
			int position = getAdapterPosition();
			if(position >= list.size()){
				return;
			}
			MailOAConversation conversation = list.get(position);
			MailOAUtils.updateUnreadCount(mContext, conversation.getAddress()
					, conversation.getSendAddress(), conversation.getBoxType());
		}
	}
}
