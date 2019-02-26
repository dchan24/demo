package com.cmicc.module_message.ui.adapter.message;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.utils.RxAsyncHelper;
import com.chinamobile.app.utils.TimeUtil;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.application.App;
import com.chinamobile.app.yuliao_common.utils.FontUtil;
import com.chinamobile.app.yuliao_common.utils.Log;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_core.db.LoginDaoImpl;
import com.chinamobile.app.yuliao_core.util.NumberUtils;
import com.cmcc.cmrcs.android.glide.GlidePhotoLoader;
import com.cmicc.module_message.ui.adapter.MessageChatListAdapter;
import com.cmicc.module_message.ui.constract.BaseChatContract;
import com.cmcc.cmrcs.android.ui.utils.NickNameUtils;
import com.cmcc.cmrcs.android.ui.utils.PhoneUtils;
import com.cmicc.module_message.R;

import rx.functions.Func1;

import static android.support.v7.widget.RecyclerView.NO_POSITION;
import static com.constvalue.MessageModuleConst.MessageChatListAdapter.BUBBLE_NOUP_DOWN;

import static com.constvalue.MessageModuleConst.MessageChatListAdapter.TYPE_SMSMMS_SINGLE_CHAT;
import static com.constvalue.MessageModuleConst.MessageChatListAdapter.BUBBLE_NOUP_NODOWN;
import static com.constvalue.MessageModuleConst.MessageChatListAdapter.BUBBLE_UP_DOWN;
import static com.constvalue.MessageModuleConst.MessageChatListAdapter.BUBBLE_UP_NODOWN;
import static com.constvalue.MessageModuleConst.MessageChatListAdapter.VIEW_SHOW_TIME_FLAG;
import static com.constvalue.MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT;
import static com.constvalue.MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT;
import static com.constvalue.MessageModuleConst.MessageChatListAdapter.TYPE_SMSMMS_SINGLE_CHAT;
import static com.constvalue.MessageModuleConst.MessageChatListAdapter.TYPE_PUBLICACCOUNT_CHAT;
import  com.constvalue.MessageModuleConst;
import static com.constvalue.MessageModuleConst.MessageChatListAdapter.TYPE_MASS;


public class BaseViewHolder extends ViewHolder {
    protected  static final String TAG = BaseViewHolder.class.getName();
	private final static int SNAME_DEFAULT_FONT_SIZE = 12;
	private final static int STVTIME_DEFAULT_FONT_SIZE = 11;

	public ImageView sIvHead;

	public TextView sTvTime;

	public TextView sName;

	public BaseViewHolder(View itemView , Activity activity , MessageChatListAdapter adapter , BaseChatContract.Presenter presenter) {
		super(itemView, activity, adapter, presenter);
		sIvHead = itemView.findViewById(R.id.svd_head);
		sTvTime = itemView.findViewById(R.id.tv_time);
		sName = itemView.findViewById(R.id.text_name);
		sTvTime.setTextSize(STVTIME_DEFAULT_FONT_SIZE * FontUtil.getFontScale());
		sIvHead.setOnClickListener(new NoDoubleClickListenerX());
		sIvHead.setOnLongClickListener(new OnMsgContentLongClickListener());

		itemView.setOnClickListener(mOnRvItemClickListener);
		itemView.setOnLongClickListener(mOnRvItemLongClickListener);
	}

    @Override
    public void bindTime(Message msgBefore, int position) {
        // 获取数据库里的时间

        Message msg = mMessage;
        long msgTime = msg.getDate();
        int isShowDateFlag = msg.getFlag();
        String time = "";
        if ((isShowDateFlag & VIEW_SHOW_TIME_FLAG) > 0) {
            time = TimeUtil.timeShow(msgTime);
            sTvTime.setTextColor(Color.parseColor("#2A2A2A"));

            if (msg.getType() == Type.TYPE_MSG_TEXT_SUPER_SMS_SEND || msg.getType() == Type.TYPE_MSG_SMS_RECV) {
                sTvTime.setText(Html.fromHtml(mContext.getString(R.string.fragment_share_app_share_sms) + "<br>" + time));
            } else {
                sTvTime.setText(time);
            }

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

    //绑定气泡
	public void bindBubble() {
	    final Message msg = mMessage;
	    int type = msg.getType();
		boolean isLeft = (type & Type.TYPE_RECV) > 0;
		int drawableRes = R.drawable.msgbg_receive_common;
		int size = 14;
		try {
			size = Integer.valueOf(msg.getTextSize());
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		int length = 0;
		if (!TextUtils.isEmpty(msg.getBody())) length = msg.getBody().length();
		if (size * length > 14336) {
			if (!isLeft) drawableRes = R.drawable.msgbg_send_common;
		} else {
			if ((this instanceof TextMsgHolder) || (this instanceof AudioMsgSendHolder) || (this instanceof AudioMsgRecvHolder)) {
				switch (msg.getBubbleType()) {
					case BUBBLE_NOUP_NODOWN:
						if (isLeft) drawableRes = R.drawable.msgbg_receive_text_noup_nodown;
						else drawableRes = R.drawable.msgbg_send_text_noup_nodown;
						break;
					case BUBBLE_NOUP_DOWN:
						if (isLeft) drawableRes = R.drawable.msgbg_receive_text_noup_down;
						else drawableRes = R.drawable.msgbg_send_text_noup_down;
						break;
					case BUBBLE_UP_DOWN:
						if (isLeft) drawableRes = R.drawable.msgbg_receive_text_up_down;
						else drawableRes = R.drawable.msgbg_send_text_up_down;
						break;
					case BUBBLE_UP_NODOWN:
						if (isLeft) drawableRes = R.drawable.msgbg_receive_text_up_nodown;
						else drawableRes = R.drawable.msgbg_send_text_up_nodown;
						break;
				}
			} else if (this instanceof VcardSendMsgHolder) {
				switch (msg.getBubbleType()) {
					case BUBBLE_NOUP_NODOWN:
						if (isLeft) drawableRes = R.drawable.msgbg_receive_noup_nodown;
						else drawableRes = R.drawable.msgbg_send_noup_nodown;
						break;
					case BUBBLE_NOUP_DOWN:
						if (isLeft) drawableRes = R.drawable.msgbg_receive_noup_down;
						else drawableRes = R.drawable.msgbg_send_noup_down;
						break;
					case BUBBLE_UP_DOWN:
						if (isLeft) drawableRes = R.drawable.msgbg_receive_up_down;
						else drawableRes = R.drawable.msgbg_send_up_down;
						break;
					case BUBBLE_UP_NODOWN:
						if (isLeft) drawableRes = R.drawable.msgbg_receive_up_nodown;
						else drawableRes = R.drawable.msgbg_send_up_nodown;
						break;
				}
			}else if (this instanceof VcardRecMsgHolder) {
				switch (msg.getBubbleType()) {
					case BUBBLE_NOUP_NODOWN:
						if (isLeft) drawableRes = R.drawable.msgbg_receive_noup_nodown;
						else drawableRes = R.drawable.msgbg_send_noup_nodown;
						break;
					case BUBBLE_NOUP_DOWN:
						if (isLeft) drawableRes = R.drawable.msgbg_receive_noup_down;
						else drawableRes = R.drawable.msgbg_send_noup_down;
						break;
					case BUBBLE_UP_DOWN:
						if (isLeft) drawableRes = R.drawable.msgbg_receive_up_down;
						else drawableRes = R.drawable.msgbg_send_up_down;
						break;
					case BUBBLE_UP_NODOWN:
						if (isLeft) drawableRes = R.drawable.msgbg_receive_up_nodown;
						else drawableRes = R.drawable.msgbg_send_up_nodown;
						break;
				}
			} else if (this instanceof FileMsgHolder) {
				switch (msg.getBubbleType()) {
					case BUBBLE_NOUP_NODOWN:
						if (isLeft) drawableRes = R.drawable.msgbg_receive_file_noup_nodown;
						else drawableRes = R.drawable.msgbg_send_file_noup_nodown;
						break;
					case BUBBLE_NOUP_DOWN:
						if (isLeft) drawableRes = R.drawable.msgbg_receive_file_noup_down;
						else drawableRes = R.drawable.msgbg_send_file_noup_down;
						break;
					case BUBBLE_UP_DOWN:
						if (isLeft) drawableRes = R.drawable.msgbg_receive_file_up_down;
						else drawableRes = R.drawable.msgbg_send_file_up_down;
						break;
					case BUBBLE_UP_NODOWN:
						if (isLeft) drawableRes = R.drawable.msgbg_receive_file_up_nodown;
						else drawableRes = R.drawable.msgbg_send_file_up_nodown;
						break;
				}
			} else {

			}
		}

		if (this instanceof TextMsgHolder) {
			((TextMsgHolder) this).sTvMessage.setBackgroundResource(drawableRes);
			GradientDrawable mGrand = (GradientDrawable) ((TextMsgHolder) this).sTvMessage.getBackground();
			if (isLeft) {
				mGrand.setColor(leftColorId);
				((TextMsgHolder) this).sTvMessage.setTextColor(leftTextColor);
			} else {
				mGrand.setColor(rightColorId);
				((TextMsgHolder) this).sTvMessage.setTextColor(rightTextColor);
			}
		} else if (this instanceof AudioMsgSendHolder) {
			((AudioMsgSendHolder) this).layout_Audio_content.setBackgroundResource(drawableRes);
			GradientDrawable mGrand = (GradientDrawable) ((AudioMsgSendHolder) this).layout_Audio_content.getBackground();
			mGrand.setColor(rightColorId);
		} else if (this instanceof AudioMsgRecvHolder) {
			((AudioMsgRecvHolder) this).layout_Audio_content.setBackgroundResource(drawableRes);
			GradientDrawable mGrand = (GradientDrawable) ((AudioMsgRecvHolder) this).layout_Audio_content.getBackground();
			mGrand.setColor(leftColorId);
		} else if (this instanceof FileMsgHolder) {
			((FileMsgHolder) this).sllMsg.setBackgroundResource(drawableRes);
			GradientDrawable mGrand = (GradientDrawable) ((FileMsgHolder) this).sllMsg.getBackground();
			mGrand.setColor(Color.WHITE);
		} else if (this instanceof MmsMessageHolder) {
			((MmsMessageHolder) this).sllMsg.setBackgroundResource(drawableRes);
			GradientDrawable mGrand = (GradientDrawable) ((MmsMessageHolder) this).sllMsg.getBackground();
			mGrand.setColor(Color.WHITE);
		}

		if (this instanceof VcardSendMsgHolder) {
			((VcardSendMsgHolder) this).cardContentRl.setBackgroundResource(drawableRes);
			GradientDrawable mGrand = (GradientDrawable) ((VcardSendMsgHolder) this).cardContentRl.getBackground();
			mGrand.setColor(mContext.getResources().getColor(R.color.chat_receive_bkg));
		}

		if (this instanceof VcardRecMsgHolder) {
			((VcardRecMsgHolder) this).cardContentRl.setBackgroundResource(drawableRes);
			GradientDrawable mGrand = (GradientDrawable) ((VcardRecMsgHolder) this).cardContentRl.getBackground();
			mGrand.setColor(mContext.getResources().getColor(R.color.chat_receive_bkg));
		}

		if (this instanceof OAMessageHolder) {
			drawableRes = R.drawable.msg_oa_back;
			LinearLayout lltContent = ((OAMessageHolder) this).lltContent;
			lltContent.setBackgroundResource(drawableRes);
			GradientDrawable mGrand = (GradientDrawable) lltContent.getBackground();
			mGrand.setColor(mContext.getResources().getColor(R.color.chat_receive_bkg));
		}

		if (this instanceof DateActivityMessageHolder) {
			RelativeLayout rltContent = ((DateActivityMessageHolder) this).rltContent;
			rltContent.setBackgroundResource(drawableRes);
			GradientDrawable mGrand = (GradientDrawable) rltContent.getBackground();
			mGrand.setColor(mContext.getResources().getColor(R.color.chat_receive_bkg));
		}

		if (this instanceof EnterpriseShareMessageHolder) {
			RelativeLayout rltContent = ((EnterpriseShareMessageHolder) this).rltContent;
			rltContent.setBackgroundResource(drawableRes);
			GradientDrawable mGrand = (GradientDrawable) rltContent.getBackground();
			mGrand.setColor(mContext.getResources().getColor(R.color.chat_receive_bkg));
		}

		// 图片
		if (this instanceof ImageMsgRecvHolder) {
			FrameLayout view = ((ImageMsgRecvHolder) this).frameLayout;
			view.setBackgroundResource(R.drawable.msgbg_send_image_common);
			GradientDrawable mGrand = (GradientDrawable) view.getBackground();
			mGrand.setColor(mContext.getResources().getColor(R.color.chat_receive_bkg));
		}
		if (this instanceof ImageMsgSendHolder) {
			FrameLayout view = ((ImageMsgSendHolder) this).frameLayout;
			view.setBackgroundResource(R.drawable.msgbg_send_image_common);
			GradientDrawable mGrand = (GradientDrawable) view.getBackground();
			mGrand.setColor(mContext.getResources().getColor(R.color.chat_receive_bkg));
		}

//		// 视频
//		if (this instanceof VideoMsgRecvHolder) {
//			View view = ((VideoMsgRecvHolder) this).mLl;
//			view.setBackgroundResource(R.drawable.msgbg_send_image_common);
//			GradientDrawable mGrand = (GradientDrawable) view.getBackground();
//			mGrand.setColor(mContext.getResources().getColor(R.color.chat_receive_bkg));
//		}
//		if (this instanceof VideoMsgSendHolder) {
//			View view = ((VideoMsgSendHolder) this).mLl;
//			view.setBackgroundResource(R.drawable.msgbg_send_image_common);
//			GradientDrawable mGrand = (GradientDrawable) view.getBackground();
//			mGrand.setColor(mContext.getResources().getColor(R.color.chat_receive_bkg));
//		}
	}

	//绑定姓名
	public void bindName(final Message msgBefore) {
		final Message msg = mMessage;
	    Log.d(TAG, "auth:" + msg.getAuthor() + ",number:" + msg.getSendAddress() + ",isGroupChat:" + isGroupChat + "  Width = " + sName.getMeasuredWidth() + "  Height = " + sName.getMeasuredHeight());

		if (isGroupChat) {
			String name = NickNameUtils.getNickNameFromCache(mContext, msg.getSendAddress(), msg.getAddress());
			if (msg.getBubbleType() == BUBBLE_NOUP_NODOWN || msg.getBubbleType() == BUBBLE_NOUP_DOWN) {
				if (!TextUtils.isEmpty(name)) {
					sName.setText(name.trim());
					sName.setTextColor(nameTextColor);
					sName.setVisibility(View.VISIBLE);
					sName.setTextSize(SNAME_DEFAULT_FONT_SIZE * FontUtil.getFontScale());
				} else {
//					sName.setText(NumberUtils.getNumForStore(msg.getSendAddress()));
					sName.setTag(R.id.glide_image_id, msg.getSendAddress());
					new RxAsyncHelper<>(msg.getAddress()).runInThread(new Func1<String, String>() {
						@Override
						public String call(String s) {
							String nickName = NickNameUtils.getNickName(mContext, msg.getSendAddress(), msg.getAddress());
							return nickName;
						}
					}).runOnMainThread(new Func1<String, Object>() {
						@Override
						public Object call(String nickName) {
							if (sName.getTag(R.id.glide_image_id) != null && sName.getTag(R.id.glide_image_id).equals(msg.getSendAddress())) {
								sName.setText(nickName.trim());
								sName.setTextColor(nameTextColor);
								sName.setVisibility(View.VISIBLE);
								sName.setTextSize(SNAME_DEFAULT_FONT_SIZE * FontUtil.getFontScale());
							}
							return null;
						}
					}).subscribe();
				}
				return;
			}
			boolean isSystemView = false;
			if (msgBefore != null && (msgBefore.getType() == Type.TYPE_MSG_SYSTEM || msgBefore.getType() == Type.TYPE_MSG_WITHDRAW_REVOKE)) {
				isSystemView = true;
			}
			if (isSystemView) {
				if (!TextUtils.isEmpty(name)) {
					sName.setText(name.trim());
					sName.setTextColor(nameTextColor);
					sName.setVisibility(View.VISIBLE);
					sName.setTextSize(SNAME_DEFAULT_FONT_SIZE * FontUtil.getFontScale());
				} else {
					sName.setText(NumberUtils.getNumForStore(msg.getSendAddress()));
					sName.setTag(R.id.glide_image_id, msg.getSendAddress());
					new RxAsyncHelper<>(msg.getAddress()).runInThread(new Func1<String, String>() {
						@Override
						public String call(String s) {
							String nickName = NickNameUtils.getNickName(mContext, msg.getSendAddress(), msg.getAddress());
							return nickName;
						}
					}).runOnMainThread(new Func1<String, Object>() {
						@Override
						public Object call(String nickName) {
							if (sName.getTag(R.id.glide_image_id) != null && sName.getTag(R.id.glide_image_id).equals(msg.getSendAddress())) {
								sName.setText(nickName.trim());
								sName.setTextColor(nameTextColor);
								sName.setVisibility(View.VISIBLE);
								sName.setTextSize(SNAME_DEFAULT_FONT_SIZE * FontUtil.getFontScale());
							}
							return null;
						}
					}).subscribe();
				}
				return;
			}
		}
		sName.setVisibility(View.GONE);
	}

    public void bindHead(final Message msgBefore) {
        final Message msg = mMessage;
        if (msg.getBubbleType() != BUBBLE_NOUP_NODOWN && msg.getBubbleType() != BUBBLE_NOUP_DOWN) {
            boolean isSystemView = false;
            if (msgBefore != null && (msgBefore.getType() == Type.TYPE_MSG_SYSTEM || msgBefore.getType() == Type.TYPE_MSG_WITHDRAW_REVOKE)) {
                isSystemView = true;
            }

            if (isSystemView) {
                sIvHead.setVisibility(View.VISIBLE);
            }else{
                sIvHead.setVisibility(View.INVISIBLE);
            }
        } else {
            sIvHead.setVisibility(View.VISIBLE);
            String address = "";
            boolean isLeft = (msg.getType() & Type.TYPE_RECV) > 0;
            if (isLeft) {
                if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
                    address = msg.getSendAddress();
                } else if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_PUBLICACCOUNT_CHAT) {
                    GlidePhotoLoader.getInstance(App.getAppContext()).loadPlatformPhoto(App.getAppContext(), sIvHead, msg.getAddress());
                    return;
                } else if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_PC_CHAT) {
                    GlidePhotoLoader.getInstance(mContext).loadPcMsgPhoto( sIvHead, msg.getAddress(), true);
                    return;
                } else {
                    address = msg.getAddress();
                }
            } else {
                address = msg.getSendAddress();
                if (mChatType == TYPE_SMSMMS_SINGLE_CHAT) {
                    String loginNum = LoginDaoImpl.getInstance().queryLoginUser(mContext);
                    address = PhoneUtils.getMinMatchNumber(loginNum);
                } else if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_PC_CHAT) {
                    GlidePhotoLoader.getInstance(mContext).loadPcMsgPhoto( sIvHead, msg.getAddress(), false);
                    return;
                }
            }
            GlidePhotoLoader.getInstance(mContext).loadPhoto(mContext, sIvHead, address);
        }
    }

	/**
	 *  消息底部的发送状态
	 */
	public void bindSendStatus(){ }



	private View.OnClickListener mOnRvItemClickListener = new  View.OnClickListener(){

		@Override
		public void onClick(View v) {
			LogF.d("onTouch","onClick");
			if(adapter.getIsMultiSelectMode()){

				int position = getAdapterPosition();
				if (position == NO_POSITION) {
					return ;
				}
				//已经选中的选项，再次点击，进行移除,否则添加
				if(adapter.getSelectedList().get(position)){
					adapter.removeSelection(position ,true);
				}else{
					//限制多选
					if (adapter.getSelectedList().size() >= 100){
						Toast.makeText(mContext,mContext.getString(R.string.multi_select_tip),Toast.LENGTH_SHORT).show();
						return ;
					}
					adapter.addSelection(position);
				}
			}

		}
	};

	private View.OnLongClickListener mOnRvItemLongClickListener = new View.OnLongClickListener(){

		@Override
		public boolean onLongClick(View v) {
			LogF.d("onTouch","onLongClick");
			if(adapter.getIsMultiSelectMode()){

				int position = getAdapterPosition();
				if (position == NO_POSITION) {
					return true;
				}
				//已经选中的选项，再次点击，进行移除,否则添加
				if(adapter.getSelectedList().get(position)){
					adapter.removeSelection(position ,true);
				}else{
					//限制多选
					if (adapter.getSelectedList().size() >= 100){
						Toast.makeText(mContext,mContext.getString(R.string.multi_select_tip),Toast.LENGTH_SHORT).show();
						return true;
					}
					adapter.addSelection(position);
				}
			}

			return adapter.getIsMultiSelectMode();
		}
	};
}
