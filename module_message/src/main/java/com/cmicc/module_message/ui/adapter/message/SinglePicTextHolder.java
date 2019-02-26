package com.cmicc.module_message.ui.adapter.message;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.module.WebConfig;
import com.app.module.proxys.moduleenterprise.EnterPriseProxy;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.chinamobile.app.utils.TimeUtil;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_common.application.App;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.cmcc.cmrcs.android.glide.GlideApp;
import com.cmicc.module_message.ui.adapter.MessageChatListAdapter;
import com.cmicc.module_message.ui.constract.BaseChatContract;
import com.cmicc.module_message.R;

import static android.support.v7.widget.RecyclerView.NO_POSITION;

public class SinglePicTextHolder extends BaseViewHolder {
	private View clickableContent;
	private TextView mSeTitle;
	private TextView mSeTime;
	private ImageView mSeImage;
	private TextView mContent;

	public SinglePicTextHolder(View itemView, Activity activity , MessageChatListAdapter adapter , BaseChatContract.Presenter presenter) {
		super(itemView ,activity ,adapter ,presenter);
		clickableContent = itemView.findViewById(R.id.ll_simple_edition_text_image);
		mSeTitle = itemView.findViewById(R.id.tv_se_title);
		mSeTime = itemView.findViewById(R.id.tv_se_time);
		mSeImage = itemView.findViewById(R.id.iv_se_image);
		mContent = itemView.findViewById(R.id.tv_se_content);
		clickableContent.setOnClickListener(new OnPicTextClickListener());
		clickableContent.setOnLongClickListener(new OnMsgContentLongClickListener());

		itemView.setOnClickListener(null);
		itemView.setOnLongClickListener(null);
	}

	public void bindSinglePicText(Message msg) {
		long msgTime = msg.getDate();
		mSeTime.setText(TimeUtil.formatTime(msgTime, TimeUtil.TIME_FORMAT_10));
		mSeTitle.setText(msg.getSubTitle());
		mContent.setText(msg.getSubBody());
		String subImgPath = msg.getSubImgPath();
		RequestOptions options = new RequestOptions().error(R.drawable.message_picture_default_picture_big)
				.diskCacheStrategy(DiskCacheStrategy.DATA).dontAnimate().dontTransform();
		GlideApp.with(App.getAppContext())
				.load(subImgPath)
				.apply(options)
				.into(mSeImage);
	}

	public class OnPicTextClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			int position = getAdapterPosition();
			if (position == NO_POSITION) {
				return;
			}
			Message msg = adapter.getItem(adapter.canLoadMore() ? position - 1 : position);
			int i = v.getId();
			if (i == R.id.ll_simple_edition_text_image) {
				if (msg != null) {
					startWebByUrl(msg);
				} else {
					LogF.e(TAG, "msg is null");
				}
			}
		}
	}

	// 跳转url
	private void startWebByUrl(Message message) {
		if (TextUtils.isEmpty(message.getSubUrl()) || "null".equals(message.getSubUrl())) {
			LogF.e(TAG, "url is null");
			return;
		}
		WebConfig webConfig = new WebConfig.Builder()
				.enableRequestToken(false)
				.enableShare()
				.title(message.getAuthor())
				.build(message.getSubUrl());
		EnterPriseProxy.g.getUiInterface().jumpToBrowser(mContext, webConfig);
	}
}
