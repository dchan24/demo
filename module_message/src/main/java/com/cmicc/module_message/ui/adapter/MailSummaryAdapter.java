package com.cmicc.module_message.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.module.WebConfig;
import com.app.module.proxys.moduleenterprise.EnterPriseProxy;
import com.chinamobile.app.yuliao_business.model.MailAssistant;
import com.chinamobile.app.yuliao_common.utils.FontUtil;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_common.utils.SystemUtil;
import com.chinamobile.app.utils.TimeUtil;
import com.cmcc.cmrcs.android.ui.view.AlignTextView;
import com.cmicc.module_message.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dchan on 2018/1/17.
 */

public class MailSummaryAdapter extends RecyclerView.Adapter <ViewHolder>{

	private final float SETTING_ITEM_FONT_SIZE_SIXTEEN = 16f;
	private final float SETTING_ITEM_FONT_SIZE_FOURTEEN = 14f;

	private static final String TAG = MailSummaryAdapter.class.getSimpleName();
	Context mContext;
	List<MailAssistant> mDataList;

	public MailSummaryAdapter(Context mContext) {
		this.mContext = mContext;
		mDataList = new ArrayList<>();
	}
	public void setData(List<MailAssistant> mailList) {
		if (mailList != null) {
			mDataList = mailList;
		}
	}

	public List<MailAssistant> getDataList(){
		return mDataList;
	}


	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		LogF.d(TAG ,"onCreateViewHolder ,viewType"+viewType);
//		if (viewType == BaseCustomCursorAdapter.TYPE_HEAD) {
//			View headView = getHeadView(parent);
//			if (headView != null) {
//				return new HeadViewHolder(headView);
//			}
//		}
		View view = LayoutInflater.from(mContext).inflate(R.layout.item_mail_summary, parent, false);
		MailSummaryViewHolder viewHolder = new MailSummaryViewHolder(view);
		return viewHolder;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
		if(!(viewHolder instanceof MailSummaryViewHolder)){
			return;
		}
		MailSummaryViewHolder holder = (MailSummaryViewHolder) viewHolder;
		if(position >= mDataList.size()){
			return;
		}
		MailAssistant mailAssistant = mDataList.get(position);
		long mailTime = mailAssistant.getSendTime();

		String time = "";
		if (TimeUtil.isToday(mailTime)) {
			time = TimeUtil.formatTime(mailTime, TimeUtil.TIME_FORMAT_14);
		} else {
			time = TimeUtil.formatTime(mailTime, TimeUtil.TIME_FORMAT_10);
			if (TextUtils.isEmpty(time)) {
				time = TimeUtil.formatTime(mailTime, TimeUtil.TIME_FORMAT_14);
			} else {
				time += " " + TimeUtil.formatTime(mailTime, TimeUtil.TIME_FORMAT_14);
			}
		}
		holder.send_time.setText(time);

		String title = mailAssistant.getMailTitle();
		if (!TextUtils.isEmpty(title)) {
			title = title.replaceAll("\n", " ");
			holder.mail_title.setText(title);
		}else{
			holder.mail_title.setText(mContext.getString(R.string.no_subject));
		}

		String content = mailAssistant.getMailSummary();
		if (!TextUtils.isEmpty(content)) {
//			content = content.replaceAll("\n", " ");
			holder.mail_summary.setText(content);
		} else {
			holder.mail_summary.setText(mContext.getString(R.string.empty_mail));
		}

		String attached = mailAssistant.getAttachedNameString();
		if (!TextUtils.isEmpty(attached)) {
			attached = attached.replaceAll("\n", " ");
			holder.mail_attached.setVisibility(View.VISIBLE);
			holder.mail_attached.setText(mContext.getString(R.string.enclosure)+"（"+mailAssistant.getAttachedCount()+"）："+attached);
		}else{
			holder.mail_attached.setVisibility(View.GONE);
		}
		reSetTextSize( holder);
	}

	@Override
	public int getItemCount() {
		return mDataList.size();
	}


	public class HeadViewHolder extends RecyclerView.ViewHolder{

		public HeadViewHolder(View itemView) {
			super(itemView);
		}
	}

	public class MailSummaryViewHolder extends RecyclerView.ViewHolder implements OnClickListener{
		public TextView mail_title;
		public TextView mail_summary;
		public AlignTextView mail_attached;
		public TextView send_time;
		private TextView readAllTv ;
		private ImageView rightArrowImage ;
		public MailSummaryViewHolder(View itemView) {
			super(itemView);
			mail_title = (TextView)itemView.findViewById(R.id.mail_title);
			mail_summary = (TextView) itemView.findViewById(R.id.mail_summary);
			mail_attached = (AlignTextView)itemView.findViewById(R.id.mail_attach);
			send_time = (TextView)itemView.findViewById(R.id.send_time);
			readAllTv = (TextView)itemView.findViewById(R.id.read_all);
			rightArrowImage = (ImageView)itemView.findViewById(R.id.right_arrow_img);
			itemView.setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			if(getAdapterPosition() >= mDataList.size()){
				return;
			}
			String mailId = mDataList.get(getAdapterPosition()).getMailId();
			LogF.i("xxx", "点击的MailID = " + mailId);
			WebConfig webConfig = new WebConfig.Builder()
					.enableRequestToken(false)
					.title(mContext.getString(R.string.detail_mail))
					.enableFromEmail(true)
					.setEmailId(stringToHexString(mailId))
					.build(mContext.getString(R.string.url_mail139));
			EnterPriseProxy.g.getUiInterface().jumpToBrowser(mContext, webConfig);
		}
	}

	private static String stringToHexString(String s) {
		String str = "";
		for (int i = 0; i < s.length(); i++) {
			int ch = (int) s.charAt(i);
			String s4 = Integer.toHexString(ch);
			str = str + s4;
		}
		return str;
	}

	private void reSetTextSize(MailSummaryAdapter.MailSummaryViewHolder holder){

		ViewGroup.LayoutParams epParams = holder.rightArrowImage.getLayoutParams();
		epParams.height = (int)(SystemUtil.dip2px(14)*FontUtil.getFontScale());
		epParams.width = epParams.height;
		holder.rightArrowImage.setLayoutParams(epParams);

		holder.send_time.setTextSize(SETTING_ITEM_FONT_SIZE_FOURTEEN* FontUtil.getFontScale());
		holder.mail_title.setTextSize(SETTING_ITEM_FONT_SIZE_SIXTEEN* FontUtil.getFontScale());
		holder.mail_summary.setTextSize(SETTING_ITEM_FONT_SIZE_FOURTEEN* FontUtil.getFontScale());
		holder.mail_attached.setTextSize(SETTING_ITEM_FONT_SIZE_FOURTEEN* FontUtil.getFontScale());
		holder.readAllTv.setTextSize(SETTING_ITEM_FONT_SIZE_FOURTEEN* FontUtil.getFontScale());

	}
}
