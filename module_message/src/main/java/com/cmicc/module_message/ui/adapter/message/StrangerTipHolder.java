package com.cmicc.module_message.ui.adapter.message;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.app.module.proxys.modulecontact.ContactProxy;
import com.chinamobile.app.yuliao_business.model.Message;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.adapter.MessageChatListAdapter;
import com.cmicc.module_message.ui.constract.BaseChatContract;

import static com.constvalue.ContactModuleConst.CONTACT_DETAIL;
import static com.constvalue.ContactModuleConst.CONTACT_NAME;
import static com.constvalue.ContactModuleConst.CONTACT_NUMBER;
import static com.constvalue.ContactModuleConst.CONTACT_RAWID;
import static com.constvalue.ContactModuleConst.IS_NEW_NOT_EDIT;

public class StrangerTipHolder extends ViewHolder{
    private Message mStrangerMsg;
    public StrangerTipHolder(View itemView, Activity activity, MessageChatListAdapter adapter, BaseChatContract.Presenter presenter) {
        super(itemView, activity, adapter, presenter);

        TextView SaveContactTv = (TextView)itemView.findViewById(R.id.save_stranger_contact);
        SaveContactTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mStrangerMsg == null)
                    return;

                Bundle bundle = new Bundle();
                bundle.putString(CONTACT_NUMBER, mStrangerMsg.getAddress());
                bundle.putString(CONTACT_NAME, mStrangerMsg.getPerson());
                bundle.putString(CONTACT_RAWID,mStrangerMsg.getId()+"");
                bundle.putBoolean(IS_NEW_NOT_EDIT, true);
                String[] info =  new String[]{mStrangerMsg.getBody(),"",""};
                bundle.putStringArray(CONTACT_DETAIL, info);
                ContactProxy.g.getUiInterface().startNewContactActivity(mContext,bundle);
            }
        });
    }

    public void bindData(Message msg){
        mStrangerMsg = msg;
    }
}
