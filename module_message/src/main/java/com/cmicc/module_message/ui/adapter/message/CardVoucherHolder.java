package com.cmicc.module_message.ui.adapter.message;

import android.app.Activity;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.app.module.proxys.moduleaboutme.AboutMeProxy;
import com.app.module.proxys.moduleredpager.RedpagerProxy;
import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_business.util.Status;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_core.util.NumberUtils;
import com.cmcc.cmrcs.android.glide.GlidePhotoLoader;
import com.cmicc.module_message.ui.adapter.MessageChatListAdapter;
import com.cmicc.module_message.ui.constract.BaseChatContract;
import com.cmcc.cmrcs.android.ui.utils.LoginUtils;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmcc.cmrcs.android.widget.RecycleViewConstraintLayout;
import com.cmicc.module_message.R;
import com.constvalue.MessageModuleConst;

import static android.support.v7.widget.RecyclerView.NO_POSITION;

/**
 * 卡券 ViewHolder
 */
public class CardVoucherHolder extends BaseViewHolder {
    private static final String TAG = CardVoucherHolder.class.getSimpleName();
    public ImageView sendFailedView;
    public ProgressBar layoutLoading;
    public LinearLayout cardVoucher;
    public ImageView cardValue;
    public ImageView cardType;
    public ImageView cardIcon;
    public TextView cardVoucherTitle;
    public TextView cardVoucherContent;
    public TextView sTvHasRead;
    public ImageView sendStatus;
    public CheckBox multiCheckBox;

    public CardVoucherHolder(View itemView, final Activity activity, final MessageChatListAdapter adapter, final BaseChatContract.Presenter presenter) {
        super(itemView, activity, adapter, presenter);
        sendFailedView = itemView.findViewById(R.id.imageview_msg_send_failed);
        layoutLoading = itemView.findViewById(R.id.progress_send_small);
        cardVoucher = itemView.findViewById(R.id.card_voucher_bg);
        cardValue = itemView.findViewById(R.id.iv_card_value);
        cardType = itemView.findViewById(R.id.iv_card_type);
        cardVoucherTitle = itemView.findViewById(R.id.tv_title);
        cardIcon = itemView.findViewById(R.id.iv_card_icon);
        cardVoucherContent = itemView.findViewById(R.id.card_voucher_content);
        sTvHasRead = itemView.findViewById(R.id.tv_has_read);
        sendStatus = itemView.findViewById(R.id.iv_send_status);
        multiCheckBox = itemView.findViewById(R.id.multi_check);

        sTvHasRead.setOnClickListener(new NoDoubleClickListenerX());

        sendFailedView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = getAdapterPosition();
                if (position == NO_POSITION) {
                    return;
                }
                position = adapter.canLoadMore() ? position - 1 : position;
                final Message msg = adapter.getItem(position);
                presenter.reSend(msg);
            }
        });

        cardVoucher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                if (position == -1) {
                    return;
                }
                final Message msg = adapter.getItem(adapter.canLoadMore() ? position - 1 : position);
                if (msg.getStatus() != Status.STATUS_OK) {
                    LogF.d(TAG, "onContentClick card voucher " + msg.getStatus());
                    return;
                }
                String xml_content = msg.getXml_content();
                if (TextUtils.isEmpty(xml_content)) {
                    LogF.d(TAG, "onContentClick xml_content is null");
                    return;
                }
                try {
                    if (!LoginUtils.getInstance().isLogined()) {
                        BaseToast.show(activity, activity.getString(R.string.check_your_net));
                        return;
                    }
                    String myName = AboutMeProxy.g.getServiceInterface().getMyProfileFamilyName(activity.getApplicationContext()) + AboutMeProxy.g.getServiceInterface().getMyProfileGiveName(activity.getApplicationContext());
                    if (TextUtils.isEmpty(myName)) {
                        myName = NumberUtils.formatPerson(LoginUtils.getInstance().getLoginUserName());
                    }
                    LogF.d(TAG, "  onContentClick  xml_content = " + xml_content);
                    String service_type = RedpagerProxy.g.getUiInterface().parseRedpager4Bean4XmlFromApp(xml_content, "body");
                    if (adapter.getChatType() == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
                        RedpagerProxy.g.getUiInterface().showCardVoucherDialog(activity, service_type, myName, msg.getAddress(), xml_content, true);
                    } else if (adapter.getChatType() == MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT) {
                        RedpagerProxy.g.getUiInterface().showCardVoucherDialog(activity, service_type, myName, msg.getAddress(), xml_content, false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
            int type = mMessage.getType();
            switch (type){
                case Type.TYPE_MSG_CARD_VOUCHER_SEND:
                    params.topToTop = R.id.lltContent;
                    params.bottomToBottom = R.id.lltContent;

                    break;
                case Type.TYPE_MSG_CARD_VOUCHER_RECV:
                    if(sIvHead.getVisibility() == View.INVISIBLE){
                        params.topToTop = R.id.lltContent;
                        params.bottomToBottom = R.id.lltContent;

                    }else{
                        params.topToTop = R.id.svd_head;
                        params.bottomToBottom = R.id.svd_head;
                    }
                    break;
                default:
                    break;

            }

            multiCheckBox.setLayoutParams(params);
            multiCheckBox.setVisibility(View.VISIBLE);
            multiCheckBox.setChecked(isSelected);
        }else{
            multiCheckBox.setVisibility(View.GONE);
        }
    }

    public void bindContent(){
        String title_bag = mMessage.getTitle();
        if (!TextUtils.isEmpty(title_bag)) {
            cardVoucherTitle.setText(title_bag);
        }
        GlidePhotoLoader.getInstance(mContext).loadOAPhoto(cardIcon, "", mMessage.getExtThumbPath());
    }
    @Override
    public void bindSendStatus() {
        int type = mMessage.getType();
        int receipt = mMessage.getMessage_receipt();
        if (type == Type.TYPE_MSG_CARD_VOUCHER_SEND) {
            int status = mMessage.getStatus();
            if (isEPGroup || isPartyGroup) {
                //企业群，党群，特殊消息另起头像，已读状态等下间距去掉
                if(mMessage.getSmallPadding()){
                    sTvHasRead.setPadding(0 ,(int) AndroidUtil.dip2px(mContext, 7) ,0 ,0 );
                }else{
                    sTvHasRead.setPadding(0 ,(int) AndroidUtil.dip2px(mContext, 7) ,0 ,(int) AndroidUtil.dip2px(mContext, 7) );
                }
                if (status == Status.STATUS_OK) {
                    sTvHasRead.setVisibility(View.VISIBLE);
                } else {
                    sTvHasRead.setVisibility(View.INVISIBLE);
                }
            } else if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT && receipt != -1) {
                if (status == Status.STATUS_OK) {
                    sTvHasRead.setVisibility(View.VISIBLE);
                    sendStatus.setVisibility(View.VISIBLE);
                    if (receipt == 0) {
                        sTvHasRead.setTextColor(mContext.getResources().getColor(R.color.color_757575));
                        sTvHasRead.setText("");
                        sendStatus.setImageResource(R.drawable.my_chat_waiting);
                    } else if (receipt == 1) {
                        sTvHasRead.setTextColor(mContext.getResources().getColor(R.color.color_757575));
                        sTvHasRead.setText(mContext.getString(R.string.already_delivered));
                        sendStatus.setImageResource(R.drawable.my_chat_delivered);
                    } else if (receipt == 2) {
                        sTvHasRead.setTextColor(mContext.getResources().getColor(R.color.color_757575));
                        sTvHasRead.setText(mContext.getString(R.string.already_delivered_by_sms));
                        sendStatus.setImageResource(R.drawable.my_chat_delivered);
                    } else if (receipt == 3) {
                        sTvHasRead.setTextColor(mContext.getResources().getColor(R.color.color_757575));
                        sTvHasRead.setText(mContext.getString(R.string.already_notified_by_sms));
                        sendStatus.setImageResource(R.drawable.my_chat_shortmessage);
                    } else {
                        sTvHasRead.setTextColor(mContext.getResources().getColor(R.color.color_757575));
                        sTvHasRead.setText(mContext.getString(R.string.others_offline_already_notified));
                        sendStatus.setImageResource(R.drawable.my_chat_shortmessage);
                    }
                } else {
                    sTvHasRead.setVisibility(View.INVISIBLE);
                    sendStatus.setVisibility(View.INVISIBLE);
                }
            } else {
                sTvHasRead.setVisibility(View.GONE);
                sendStatus.setVisibility(View.GONE);
            }
            switch (status) {
                case Status.STATUS_WAITING:
                case Status.STATUS_LOADING:
                    layoutLoading.setVisibility(View.VISIBLE);
                    sendFailedView.setVisibility(View.GONE);
                    break;
                case Status.STATUS_OK:
                    layoutLoading.setVisibility(View.GONE);
                    sendFailedView.setVisibility(View.GONE);
                    break;
                case Status.STATUS_FAIL:
                case Status.STATUS_PAUSE:
                    layoutLoading.setVisibility(View.GONE);
                    sendFailedView.setVisibility(View.VISIBLE);
                    break;
                default:
                    layoutLoading.setVisibility(View.VISIBLE);
                    sendFailedView.setVisibility(View.GONE);
                    break;
            }
        }
    }
}
