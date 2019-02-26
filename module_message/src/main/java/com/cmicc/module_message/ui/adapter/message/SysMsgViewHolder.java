package com.cmicc.module_message.ui.adapter.message;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.utils.TimeUtil;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_common.utils.Log;
import com.cmicc.module_message.ui.adapter.MessageChatListAdapter;
import com.cmicc.module_message.ui.constract.BaseChatContract;
import com.cmicc.module_message.R;

import static com.constvalue.MessageModuleConst.MessageChatListAdapter.VIEW_SHOW_TIME_FLAG;

public class SysMsgViewHolder extends ViewHolder {

	private static final String TAG = SysMsgViewHolder.class.getName();
	public TextView sTvSysMsg;
	public TextView sTvTime;

	public SysMsgViewHolder(View itemView , final Activity activity , MessageChatListAdapter adapter , final BaseChatContract.Presenter presenter) {
		super(itemView, activity, adapter, presenter);
		sTvSysMsg = (TextView) itemView.findViewById(R.id.tv_sys_msg);
		sTvTime = (TextView) itemView.findViewById(R.id.tv_time);
		sTvSysMsg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (v instanceof TextView) {
					if ("还有人未进群,再次邀请".equals(((TextView) v).getText().toString()) ||
							"還有人未進群,再次邀請".equals(((TextView) v).getText().toString())) {
						presenter.sysMessage(1); // 普通群群主
					} else if (((TextView) v).getText().toString().contains("等人未使用和飞信，无法看到群消息，去邀请他们使用吧") ||
							((TextView) v).getText().toString().contains("等人未使用和飛信，無法看到群消息，去邀請他們使用吧")) {
						presenter.sysMessage(2); // 企业群所有群成员
					}
				}
			}
		});
	}

	@Override
	public void bindMultiSelectStatus(boolean isMultiSelectMode , boolean isSelected){
	}

	@Override
	public void bindTime(Message msgBefore, int position) {
		final Message msg = mMessage;
	    // 获取数据库里的时间
		long msgTime = msg.getDate();
		int isShowDateFlag = msg.getFlag();
		String time = "";

        GradientDrawable mGrand = (GradientDrawable) sTvSysMsg.getBackground();
		sTvSysMsg.setTextColor(nameTextColor);
		mGrand.setColor(sysTextBackColor);

		if ((isShowDateFlag & VIEW_SHOW_TIME_FLAG) > 0) {
			time = TimeUtil.timeShow(msgTime);
			sTvTime.setTextColor(Color.parseColor("#2A2A2A"));
			sTvTime.setText(time);
			if (position == 0) {
				ViewGroup.LayoutParams lp = sTvTime.getLayoutParams();
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

		if (position == 0) {
			time = TimeUtil.timeShow(msgTime);
			sTvTime.setTextColor(Color.parseColor("#2A2A2A"));
			sTvTime.setText(time);
			ViewGroup.LayoutParams lp = sTvTime.getLayoutParams();
			if (lp instanceof LinearLayout.LayoutParams) {
				((LinearLayout.LayoutParams) lp).topMargin = (int) AndroidUtil.dip2px(mContext, 16);
				sTvTime.setLayoutParams(lp);
			}
			sTvTime.setVisibility(View.VISIBLE);
	    }

        int visiType = sTvTime.getVisibility();
        if (visiType == View.VISIBLE) {
            if (msgBefore != null && msgBefore.getBigMargin()) {
                ViewGroup.LayoutParams lp = sTvTime.getLayoutParams();
                if (lp instanceof ViewGroup.MarginLayoutParams) {
                    ((ViewGroup.MarginLayoutParams) lp).topMargin = (int) AndroidUtil.dip2px(mContext, 16);
                    sTvTime.setLayoutParams(lp);
                }
            } else if (msgBefore != null && !msgBefore.getBigMargin()) {
                ViewGroup.LayoutParams lp = sTvTime.getLayoutParams();
                if (lp instanceof ViewGroup.MarginLayoutParams) {
                    ((ViewGroup.MarginLayoutParams) lp).topMargin = (int) AndroidUtil.dip2px(mContext, 32);
                    sTvTime.setLayoutParams(lp);
                }

            }

        }
    }

    public void bindText(){
		String body = mMessage.getBody();
		if (!TextUtils.isEmpty(body) && (mMessage.getBody().equals("还有人未进群,再次邀请") ||
				mMessage.getBody().equals("還有人未進群,再次邀請"))) { // 这里是应为查进数据库的可能是繁体简体所以两种都要判断
			SpannableString ss = new SpannableString(body);
			//设置字体前景色
			ss.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.color_4991fb)), body.indexOf(",") + 1, body.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			//设置下划线
			sTvSysMsg.setText(ss);
		} else if (!TextUtils.isEmpty(body) && (mMessage.getBody().contains("等人未使用和飞信，无法看到群消息，去邀请他们使用吧") ||
				mMessage.getBody().contains("等人未使用和飛信，無法看到群消息，去邀請他們使用吧"))) {
			SpannableString ss = new SpannableString(body);
			//设置字体前景色
			ss.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.color_4991fb)), body.length() - 7, body.length() - 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			//设置下划线
			sTvSysMsg.setText(ss);
		} else {
			sTvSysMsg.setText(body);
		}
	}
}
