package com.cmicc.module_message.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.app.module.WebConfig;
import com.app.module.proxys.moduleenterprise.EnterPriseProxy;
import com.chinamobile.app.yuliao_business.model.MailOA;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.utils.TimeUtil;
import com.cmcc.cmrcs.android.glide.GlidePhotoLoader;
import com.cmcc.cmrcs.android.ui.activities.HomeActivity;
import com.cmicc.module_message.R;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dchan on 2018/1/17.
 */

public class MailOASummaryAdapter extends RecyclerView.Adapter <ViewHolder>{
	private static final String TAG = MailOASummaryAdapter.class.getSimpleName();
	Context mContext;
	List<MailOA> mDataList;

	public MailOASummaryAdapter(Context mContext) {
		this.mContext = mContext;
		mDataList = new ArrayList<>();
	}
	public void setData(List<MailOA> mailList) {
		if (mailList != null) {
			mDataList = mailList;
		}
	}

	public List<MailOA> getDataList(){
		return mDataList;
	}


	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		LogF.d(TAG ,"onCreateViewHolder ,viewType"+viewType);
//		if (viewType == BaseCustomCursorAdapter.TYPE_HEAD) {
//			View headView = getHeadView(parent);
//			if (headView != null) {
//				return new HeadViewHolder(headView);
//			}
//		}
		View view = LayoutInflater.from(mContext).inflate(R.layout.item_mail_oa_summary, parent, false);
		MailOASummaryViewHolder viewHolder = new MailOASummaryViewHolder(view);
		return viewHolder;
	}

	@Override
	public void onBindViewHolder(ViewHolder viewHolder, int position) {
		if(!(viewHolder instanceof MailOASummaryViewHolder)){
			return;
		}
		MailOASummaryViewHolder holder = (MailOASummaryViewHolder) viewHolder;
		if(position >= mDataList.size()){
			return;
		}
		MailOA mailOA = mDataList.get(position);
		int boxType = mailOA.getBoxType();
		long mailTime = mailOA.getSendTime();

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

		String title = mailOA.getMailTitle();
		if (!TextUtils.isEmpty(title)) {
			title = title.replaceAll("\n", " ");
			holder.mail_title.setText(title);
		}else{
			holder.mail_title.setText(mContext.getString(R.string.no_subject));
		}

		String attached = mailOA.getAttachedNameString();
		if (!TextUtils.isEmpty(attached)) {
			attached = attached.replaceAll("\n", " ");
			holder.mail_attached.setVisibility(View.VISIBLE);
			holder.mail_attached.setText(mContext.getString(R.string.enclosure)+"（"+mailOA.getAttachedCount()+"）："+attached);
		}else{
			holder.mail_attached.setVisibility(View.GONE);
		}

		String url = "";
		if(!TextUtils.isEmpty(mailOA.getSubOriginUrl())){
			url = mailOA.getSubOriginUrl();
		}else if(!TextUtils.isEmpty(mailOA.getMailUrl())){
			url = mailOA.getMailUrl();
		}

		ViewCompat.setBackground(holder.mViewLook, getViewLookDrawable(holder.mViewLook.getContext(), mailOA.getAddress()));
		if(TextUtils.isEmpty(url)){
			holder.mViewLook.setVisibility(View.GONE);
		}else {
			holder.mViewLook.setVisibility(View.VISIBLE);
			//有链接，有子页面，内容详情限制显示两行，多出的省略
			holder.mail_summary.setMaxLines(2);
			holder.mail_summary.setEllipsize(TextUtils.TruncateAt.END);
		}

		String content = mailOA.getMailSummary();
		if (!TextUtils.isEmpty(content)) {
//			content = content.replaceAll("\n", " ");
			holder.mail_summary.setText(content);
		} else {
			holder.mail_summary.setText(mContext.getString(R.string.empty_mail));
		}

	}

	private Drawable getViewLookDrawable(Context context, String address){
		int res = R.drawable.mail_oa_item_load_more_bg;
		Integer integer = GlidePhotoLoader.sOAICONS.get(address);
		if(integer != null){
			if(integer == R.drawable.my_messages_journal ||
					integer == R.drawable.my_messages_news){
				res = R.drawable.mail_oa_item_load_more_bg;
			}else if(integer == R.drawable.my_messages_clock ||
					integer == R.drawable.my_messages_information ||
					integer == R.drawable.cc_chat_approval){
				res = R.drawable.mail_oa_item_load_more_yellow_bg;
			}
		}
		return ContextCompat.getDrawable(context, res);
	}

	@Override
	public int getItemCount() {
		return mDataList.size();
	}



	public class MailOASummaryViewHolder extends ViewHolder implements OnClickListener{
		public TextView mail_title;
		public TextView mail_summary;
		public TextView mail_attached;
		public TextView send_time;
		public View mViewLook;
		public MailOASummaryViewHolder(View itemView) {
			super(itemView);
			mail_title = (TextView)itemView.findViewById(R.id.mail_title);
			mail_summary = (TextView)itemView.findViewById(R.id.mail_summary);
			mail_attached = (TextView)itemView.findViewById(R.id.mail_attach);
			send_time = (TextView)itemView.findViewById(R.id.send_time);
			mViewLook = itemView.findViewById(R.id.ll_mail_oa);
			itemView.setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			if(getAdapterPosition() >= mDataList.size()){
				return;
			}
			MailOA mailOA = mDataList.get(getAdapterPosition());
			String mailId = mailOA.getMailId();
			LogF.i("xxx", "点击的MailID = " + mailId);
			String url = "";
			if(!TextUtils.isEmpty(mailOA.getSubOriginUrl())){
				url = mailOA.getSubOriginUrl();
			}else if(!TextUtils.isEmpty(mailOA.getMailUrl())){
				url = mailOA.getMailUrl();
			}
			if(TextUtils.isEmpty(url)){
				return;
			}
			try {
				url = URLDecoder.decode(url, "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			if(url.contains("http://120.197.235.114/atdc/login/ssoAttendance?")){//处理考勤打卡
				if(url.contains("comeFrom=1")){
					url = url.substring(0 ,url.indexOf("&comeFrom=1"));
				}
			}
			WebConfig.ShareItem shareItem = new WebConfig.ShareItem();
			shareItem.shareType = mailOA.getType();
			WebConfig.Builder builder = new WebConfig
					.Builder();
			WebConfig webConfig = null;
			if(mailOA.getBoxType() == Type.TYPE_BOX_MAIL_OA){
				webConfig = builder.enableRequestToken(false)
						.clearCookie()
						.build(url);
			}else if(url.contains("token={token}")){
				webConfig = builder.enableRequestToken(true)
						.placeholderToken()
						.build(url);
			}else if(url.contains("mail.chinamobile.com")){ // smap认证, 统一邮箱
				WebConfig.SmapItem item = new WebConfig.SmapItem();
				item.appId = "cmmail@mss.cmcc";
				webConfig = builder.enableRequestToken(true)
						.enableSmapApp(item)
						.build(url);
			}else if(checkUrlIfNeedGoToWorkPlatform(url)){
				final Uri uri = Uri.parse(url);
				String enterpriseId = uri.getQueryParameter("enterpriseId");
				HomeActivity.goToWorkPlatform(mContext, enterpriseId);
				return;
			} else {
				webConfig = builder.enableRequestToken(true).build(url);
			}
			EnterPriseProxy.g
					.getUiInterface()
					.gotoEnterpriseH5Activity(((Activity) mContext), webConfig);
		}
	}

	private boolean checkUrlIfNeedGoToWorkPlatform(String url) {
		// "andfetion://com.chinasofti.rcs/goWorkPlatform?enterpriseId=37612" 跳工作台
		final Uri uri = Uri.parse(url);
		boolean checkScheme = "andfetion".equalsIgnoreCase(uri.getScheme());
		boolean checkHost = "com.chinasofti.rcs".equalsIgnoreCase(uri.getHost());
		boolean goWorkPlatform = uri.getPath().contains("goWorkPlatform");
		return checkScheme && checkHost && goWorkPlatform;
	}

}
