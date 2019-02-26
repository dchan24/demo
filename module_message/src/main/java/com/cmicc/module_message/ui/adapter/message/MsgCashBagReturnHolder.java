package com.cmicc.module_message.ui.adapter.message;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_business.util.DateUtils;
import com.cmicc.module_message.ui.adapter.MessageChatListAdapter;
import com.cmicc.module_message.ui.constract.BaseChatContract;
import com.cmicc.module_message.R;

public class MsgCashBagReturnHolder extends BaseViewHolder {

	public TextView[] textViews = new TextView[13];

	public MsgCashBagReturnHolder(View itemView, final Activity activity , final MessageChatListAdapter adapter , BaseChatContract.Presenter presenter) {
		super(itemView ,activity ,adapter ,presenter);
		textViews[0] = itemView.findViewById(R.id.title);
		textViews[1] = itemView.findViewById(R.id.date);
		textViews[2] = itemView.findViewById(R.id.return_money_declare);
		textViews[3] = itemView.findViewById(R.id.return_money);
		textViews[4] = itemView.findViewById(R.id.return_type_declare);
		textViews[5] = itemView.findViewById(R.id.return_type);
		textViews[6] = itemView.findViewById(R.id.return_way_declare);
		textViews[7] = itemView.findViewById(R.id.return_way);
		textViews[8] = itemView.findViewById(R.id.return_reason_declare);
		textViews[9] = itemView.findViewById(R.id.return_reason);
		textViews[10] = itemView.findViewById(R.id.return_time_declare);
		textViews[11] = itemView.findViewById(R.id.return_time);
		textViews[12] = itemView.findViewById(R.id.return_remark);
	}

	public void bindTextRecv(){
	    Message msg = mMessage;
		String templateName = msg.getTemplate_name();
		String templateValueText = msg.getTemplate_value_text();
		if (!TextUtils.isEmpty(templateName) && !TextUtils.isEmpty(templateValueText)){
			String[] templateNames = templateName.split(",");
			String[] templateValueTexts = templateValueText.split(",");
			for (int i = 0; i < templateNames.length; i++) {
				textViews[i * 2 + 2].setText(templateNames[i]);
				textViews[i * 2 + 3].setText(templateValueTexts[i]);
			}
			textViews[1].setText(DateUtils.formatDate("MM月dd日", msg.getDate()));
		}
		textViews[0].setText(msg.getTemplate_title());
    }
}
