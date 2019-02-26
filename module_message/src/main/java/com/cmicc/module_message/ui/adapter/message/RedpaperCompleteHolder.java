package com.cmicc.module_message.ui.adapter.message;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.module.proxys.moduleredpager.RedpagerProxy;
import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.utils.TimeUtil;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_common.utils.Log;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.cmicc.module_message.ui.adapter.MessageChatListAdapter;
import com.cmicc.module_message.ui.constract.BaseChatContract;
import com.cmcc.cmrcs.android.ui.utils.LoginUtils;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmicc.module_message.R;

import static com.constvalue.MessageModuleConst.MessageChatListAdapter.VIEW_SHOW_TIME_FLAG;

public class RedpaperCompleteHolder extends ViewHolder {
	private static final String TAG = RedpaperCompleteHolder.class.getSimpleName();
	public TextView sTvRedCompleteMsg;
	public TextView sTvTime;

	public RedpaperCompleteHolder(View itemView, Activity activity , MessageChatListAdapter adapter , BaseChatContract.Presenter presenter) {
		super(itemView, activity, adapter, presenter);
		sTvRedCompleteMsg = (TextView) itemView.findViewById(R.id.tv_red_complete_msg);
		sTvTime = (TextView) itemView.findViewById(R.id.tv_time);

		sTvRedCompleteMsg.setHighlightColor(activity.getResources().getColor(android.R.color.transparent));
		sTvRedCompleteMsg.setMovementMethod(LinkMovementMethod.getInstance());
	}

	@Override
	public void bindTime(Message msgBefore, int position) {
		final Message msg = mMessage;
	    long msgTime = msg.getDate();
		int isShowDateFlag = msg.getFlag();
		String time = "";
		sTvRedCompleteMsg.setTextColor(nameTextColor);
		GradientDrawable mGrand = (GradientDrawable) sTvRedCompleteMsg.getBackground();
		mGrand.setColor(sysTextBackColor);
		if ((isShowDateFlag & VIEW_SHOW_TIME_FLAG) > 0) {
			time = TimeUtil.timeShow(msgTime);
			sTvTime.setTextColor(Color.parseColor("#2A2A2A"));
			sTvTime.setText(time);
			if (position == 0) {
				ViewGroup.LayoutParams lp = (LinearLayout.LayoutParams) sTvTime.getLayoutParams();
				if (lp instanceof LinearLayout.LayoutParams) {
					((LinearLayout.LayoutParams) lp).topMargin = (int) AndroidUtil.dip2px(mContext, 16);
					sTvTime.setLayoutParams(lp);
				}
			}
			sTvTime.setVisibility(View.VISIBLE);
		} else {
			Log.d(TAG, time);
			sTvTime.setVisibility(View.GONE);
		}
	}

	public void setText(String text) {
		SpannableString spannableString = new SpannableString(text);
		if (!TextUtils.isEmpty(text)) {
			int index = text.lastIndexOf("红包");
			if (index != -1 && index - 2 >= 0) {
				spannableString.setSpan(new RedpaperCompleteHolder.Clickable(), index, index + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
		sTvRedCompleteMsg.setText(spannableString);
	}

	private class Clickable extends ClickableSpan {

		@Override
		public void onClick(View widget) {
			int position = getAdapterPosition();
			if (position == -1) {
				return;
			}
			final Message msg = adapter.getItem(adapter.canLoadMore() ? position - 1 : position);
			String xml_content = msg.getXml_content();
			if (TextUtils.isEmpty(xml_content)) {
				BaseToast.show(activity, activity.getString(R.string.redpacket_not_right));
				return;
			}
			if (!LoginUtils.getInstance().isLogined()) {
				BaseToast.show(activity, activity.getString(R.string.check_your_net));
				return;
			}
			try {
				LogF.d(TAG, "红包通知:" + xml_content);
				String service_type = RedpagerProxy.g.getUiInterface().parseRedpager4Bean4XmlFromApp(xml_content, "body");
				RedpagerProxy.g.getUiInterface().Activity4ReceiveResult(activity, xml_content, service_type);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void updateDrawState(TextPaint ds) {
			ds.setColor(activity.getResources().getColor(R.color.color_f9a92d));
		}
	}
}