package com.cmicc.module_message.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chinamobile.app.yuliao_business.model.MailAssistantConversation;
import com.chinamobile.app.yuliao_common.utils.FontUtil;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_common.utils.SystemUtil;
import com.chinamobile.app.yuliao_contact.model.SimpleContact;
import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.utils.TimeUtil;
import com.cmcc.cmrcs.android.contact.data.ContactsCache;
import com.cmcc.cmrcs.android.glide.GlidePhotoLoader;
import com.cmicc.module_message.ui.activity.MailMsgListActivity;
import com.cmicc.module_message.ui.activity.MailSummaryActivity;
import com.cmcc.cmrcs.android.ui.utils.MailAssistantUtils;
import com.cmcc.cmrcs.android.ui.view.dragbubble.DragBubbleView;
import com.cmcc.cmrcs.android.ui.view.dragbubble.RoundNumber;
import com.cmcc.cmrcs.android.widget.emoji.EmojiTextView4Convlist;
import com.cmicc.module_message.R;

import java.util.List;

import static com.chinamobile.app.yuliao_common.utils.FontUtil.getFontScale;

/**
 * Created by Dchan on 2018/1/17.
 */

public class MailListAdapter extends RecyclerView.Adapter<MailListAdapter.MailViewHolder>{
	private static final String TAG = MailListAdapter.class.getSimpleName();
	private final float SETTING_ITEM_FONT_SIZE_TWELVE = 12f;
	private final float SETTING_ITEM_FONT_SIZE_FOURTEEN = 14f;
	private final float SETTING_ITEM_FONT_SIZE = 18f;
	private float ROOT_VIEW_SIZE = SystemUtil.dip2px(80);
	private float SV_HEAD_VIEW_SIZE =  SystemUtil.dip2px(50);
	private float TV_UNREAD_RADIUS_SIZE =8;
	private float TV_UNREAD_FONT_SIZE =9.0f;

	Context mContext;
	List<MailAssistantConversation> list;
	public MailListAdapter(Context mContext  ,List<MailAssistantConversation> list) {
		this.mContext = mContext;
		this.list = list;
	}

	public List<MailAssistantConversation> getData() {
		return list;
	}

	public void setData(List<MailAssistantConversation> list) {
		if(list != null){
			this.list = list;
		}
		notifyDataSetChanged();
	}

	@Override
	public MailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(mContext).inflate(R.layout.item_mail_list, parent, false);
		MailViewHolder mailViewHolder = new MailViewHolder(view);
		return mailViewHolder;
	}

	@Override
	public void onBindViewHolder(MailViewHolder holder, int position) {
		MailAssistantConversation mailAssistantConversation = list.get(position);
		int unReadCount = mailAssistantConversation.getUnReadCount();
		String mailCount = "("+mailAssistantConversation.getMailCount()+")";
		int attachedCount = 0;
		try {
			attachedCount = Integer.parseInt(mailAssistantConversation.getAttachedCount());
		}catch (Exception e){
			e.printStackTrace();
		}
		if(attachedCount>0){
			holder.mail_attached_imageview.setVisibility(View.VISIBLE);
		}else{
			holder.mail_attached_imageview.setVisibility(View.GONE);
		}

		DragBubbleView dragBubbleView = ((MailMsgListActivity) mContext).getDragBubble();
		if (unReadCount <= 0) {
			holder.rnMessageBadge.setVisibility(View.GONE);
			holder.mRedDot.setVisibility(View.GONE);
		} else {
			holder.rnMessageBadge.setVisibility(View.GONE);
			holder.mRedDot.setVisibility(View.VISIBLE);
			holder.rnMessageBadge.setText(dragBubbleView ,unReadCount + "");
		}

		// 根据号码查名称和头像
		long rawId = 0;
		SimpleContact contact = ContactsCache.getInstance().searchContactByNumber(mailAssistantConversation.getAddress());
		if(contact!=null){
			rawId = contact.getRawId();
			holder.sTvConvName.setText(contact.getName()+mailCount);
		}else{
			holder.sTvConvName.setText(mailAssistantConversation.getAddress()+mailCount);
		}
		GlidePhotoLoader.getInstance(mContext).loadPhoto(mContext, holder.sIvHead, mailAssistantConversation.getAddress());
		String content = mailAssistantConversation.getContent();
		if (!TextUtils.isEmpty(content)) {
			String replaceAll = content.replaceAll("\n", " ");
			//CharSequence builderEmoji = com.chinasofti.widget.emoji.EmojiParser.getInstance().replaceAllEmojis(mContext, replaceAll, mEmojiconSize);
			if(unReadCount >0){
				try {
					replaceAll = String.format(mContext.getString(R.string.news_unit_), unReadCount > 99 ? "99+" : unReadCount + "") + replaceAll;
				}catch (Exception e){
					e.printStackTrace();
					LogF.e(TAG ,"form msg fail:"+replaceAll);
				}
			}
			holder.sTvContent.setText(replaceAll);
		} else {
			String replaceAll = mContext.getString(R.string.no_subject);
			if(unReadCount >0){
				replaceAll = String.format(mContext.getString(R.string.news_unit_), unReadCount > 99 ? "99+" : unReadCount + "") + replaceAll;
			}
			holder.sTvContent.setText(replaceAll);
		}
		if (!TextUtils.isEmpty(content) && mailAssistantConversation.getDate() != 0) {
			holder.sTvDate.setText(TimeUtil.formatTime(mailAssistantConversation.getDate()));
		} else {
			holder.sTvDate.setText("");
		}
		reSetTextSize(holder);
		resizeNameWidth( holder );
	}

	@Override
	public int getItemCount() {
		return list.size();
	}

	public MailAssistantConversation getValue(String key) {
		if(list == null){
			return null;
		}
		for(MailAssistantConversation conversation :list){
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

		RoundNumber rnMessageBadge;
		ImageView mRedDot;
		public MailViewHolder(View itemView) {
			super(itemView);
			mRootView =  itemView.findViewById(R.id.rl_conv_list_item);
			sIvHead = (ImageView) itemView.findViewById(R.id.svd_head);
			sTvConvName = (TextView) itemView.findViewById(R.id.tv_conv_name);
			sTvDate = (TextView) itemView.findViewById(R.id.tv_date);
			sTvContent = (EmojiTextView4Convlist) itemView.findViewById(R.id.tv_content);
			mail_attached_imageview = (ImageView) itemView.findViewById(R.id.icon_mail_attached);
			rnMessageBadge = (RoundNumber) itemView.findViewById(R.id.rnMessageBadge);
			mRedDot = (ImageView) itemView.findViewById(R.id.red_dot_silent);

			mRootView.setOnClickListener(this);
			rnMessageBadge.setDragListener(this);
		}

		@Override
		public void onClick(View v) {
			int position = getAdapterPosition();
			if(position >= list.size() || position<0){
				return;
			}
			MailAssistantConversation conversation = list.get(position);
			MailSummaryActivity.startMailSummaryActivity(mContext ,conversation.getAddress());
		}

		@Override
		public void onDismiss(RoundNumber roundNumber) {
			int position = getAdapterPosition();
			if(position >= list.size()){
				return;
			}
			MailAssistantConversation conversation = list.get(position);
			MailAssistantUtils.updateSeen(mContext,conversation.getAddress());
		}
	}

	private void reSetTextSize(MailListAdapter.MailViewHolder holder){
		// itme 布局
		ViewGroup.LayoutParams rootParams = holder.mRootView.getLayoutParams();
		rootParams.height = (int)(ROOT_VIEW_SIZE* (FontUtil.getFontScale()));
		holder.mRootView.setLayoutParams(rootParams);
		// 头像
		ViewGroup.LayoutParams svHeadParams = holder.sIvHead.getLayoutParams();
		svHeadParams.height = (int)(SV_HEAD_VIEW_SIZE*FontUtil.getFontScale());
		svHeadParams.width = svHeadParams.height;
		holder.sIvHead.setLayoutParams(svHeadParams);
		// 右下角图标
		ViewGroup.LayoutParams epParams = holder.mail_attached_imageview.getLayoutParams();
		epParams.height = (int)(SystemUtil.dip2px(14)*FontUtil.getFontScale());
		epParams.width = epParams.height;
		holder.mail_attached_imageview.setLayoutParams(epParams);

		holder.rnMessageBadge.setTextSize(TV_UNREAD_FONT_SIZE* getFontScale());
		holder.rnMessageBadge.setRoundRadius(SystemUtil.dip2px(TV_UNREAD_RADIUS_SIZE)* getFontScale());

		holder.sTvConvName.setTextSize(SETTING_ITEM_FONT_SIZE* FontUtil.getFontScale());
		holder.sTvContent.setTextSize(SETTING_ITEM_FONT_SIZE_FOURTEEN* FontUtil.getFontScale());
		holder.sTvDate.setTextSize(SETTING_ITEM_FONT_SIZE_TWELVE* FontUtil.getFontScale());

	}

	//调整名字宽度以处理红点遮挡的问题
	private void resizeNameWidth( MailListAdapter.MailViewHolder viewHolder){
		if(viewHolder == null){
			return;
		}
		int srceenWidth = AndroidUtil.getScreenWidth(mContext);
		float headWidth = SV_HEAD_VIEW_SIZE* getFontScale();
		float unReadMsgWidth = SystemUtil.dip2px(TV_UNREAD_RADIUS_SIZE)* getFontScale();
		float padding = AndroidUtil.dip2px(mContext ,71) + AndroidUtil.dip2px(mContext ,8)*getFontScale();
		float dateWidth = viewHolder.sTvDate.getPaint().measureText(viewHolder.sTvDate.getText().toString());

		int nameWidth = (int) (srceenWidth - headWidth
				- dateWidth - unReadMsgWidth - padding);
		ViewGroup.LayoutParams params = viewHolder.sTvConvName.getLayoutParams();
		TextPaint textPaint = viewHolder.sTvConvName.getPaint();
		String name = (String) viewHolder.sTvConvName.getText();
		int textLength = (int) textPaint.measureText(name);
		if(nameWidth > textLength){
			params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
			viewHolder.sTvConvName.setLayoutParams(params);
		}else{
			params.width = nameWidth;
			viewHolder.sTvConvName.setLayoutParams(params);
		}
	}

}
