package com.cmicc.module_message.ui.adapter.message;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Telephony;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;

import com.app.module.WebConfig;
import com.app.module.proxys.moduleaboutme.AboutMeProxy;
import com.app.module.proxys.modulecontact.ContactProxy;
import com.app.module.proxys.moduleenterprise.EnterPriseProxy;
import com.app.module.proxys.moduleimgeditor.ImgEditorProxy;
import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.yuliao_business.model.BaseModel;
import com.chinamobile.app.yuliao_business.model.Employee;
import com.chinamobile.app.yuliao_business.model.GroupInfo;
import com.chinamobile.app.yuliao_business.model.GroupMember;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_business.model.YunFile;
import com.chinamobile.app.yuliao_business.util.BuryingPointUtils;
import com.chinamobile.app.yuliao_business.util.GroupChatUtils;
import com.chinamobile.app.yuliao_business.util.MessageUtils;
import com.chinamobile.app.yuliao_business.util.Status;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.application.App;
import com.chinamobile.app.yuliao_common.application.MyApplication;
import com.chinamobile.app.yuliao_common.utils.ApplicationUtils;
import com.chinamobile.app.yuliao_common.utils.CommonConstant;
import com.chinamobile.app.yuliao_common.utils.FileUtil;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_common.utils.ObjectUtils;
import com.chinamobile.app.yuliao_common.utils.SharePreferenceUtils;
import com.chinamobile.app.yuliao_contact.model.SimpleContact;
import com.chinamobile.app.yuliao_core.db.LoginDaoImpl;
import com.chinamobile.app.yuliao_core.util.NumberUtils;
import com.chinamobile.app.yuliao_core.util.TimeUtil;
import com.cmcc.cmrcs.android.contact.data.ContactsCache;
import com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity;
import com.cmicc.module_message.ui.activity.GroupStrangerActivity;
import com.cmicc.module_message.ui.activity.MmsDetailActivity;
import com.cmcc.cmrcs.android.ui.activities.date.DateActivity;
import com.cmcc.cmrcs.android.ui.dialogs.IKnowDialog;
import com.cmcc.cmrcs.android.ui.utils.EnterpriseShareUtil;
import com.cmcc.cmrcs.android.ui.utils.GroupUtils;
import com.cmcc.cmrcs.android.ui.utils.NickNameUtils;
import com.cmcc.cmrcs.android.ui.utils.PhoneUtils;
import com.cmcc.cmrcs.android.ui.utils.UmengUtil;
import com.cmcc.cmrcs.android.ui.utils.YYUtils;
import com.cmcc.cmrcs.android.ui.utils.YunFileXmlParser;
import com.cmcc.cmrcs.android.ui.view.MessageOprationDialog;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.activity.MessageDetailActivity;
import com.cmicc.module_message.ui.adapter.MessageChatListAdapter;
import com.cmicc.module_message.ui.constract.BaseChatContract;
import com.cmicc.module_message.ui.presenter.PreviewImagePresenter;
import com.cmicc.module_message.utils.RcsAudioPlayer;
import com.constvalue.MessageModuleConst;

import java.io.File;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.support.v7.widget.RecyclerView.NO_POSITION;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.MESSAGE_BODY;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.MESSAGE_BODY_SIZE;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.MESSAGE_FILE_PATH;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.MESSAGE_FILE_THUMB_PATH;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.MESSAGE_TYPE;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.MESSAGE_XML_CONTENT;
import static com.cmcc.cmrcs.android.ui.utils.ContactSelectorUtil.SOURCE_MESSAGE_FORWARD;


/**
 *
 *
 * ViewHolder 有三个子类， 分别是BaseViewHolder, RedpaperCompleteHolder, SysMsgViewHolder
 */
public class ViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG =  ViewHolder.class.getName();
    private static final long TEN_MINUTES = 60 * 10 * 1000;
	public CountDownTimer sCDT;
	protected Activity activity;
    protected Message mMessage;//基本所有的ViewHolder都是和一条Message相对应的 。
	protected Context mContext;
	protected MessageChatListAdapter adapter;
	protected BaseChatContract.Presenter presenter;

	//ViewHolder用到的颜色。
	protected int leftColorId;//左边， 背景色
    protected int rightColorId;//右边， 背景色
	protected int leftTextColor;//左边文字颜色
	protected int rightTextColor;//右边文字颜色

	protected int nameTextColor;//消息发送人 名称颜色
	protected int sysTextBackColor;//群系统提示消息背景色


    //当前聊天界面的 逻辑参数
    protected boolean isEPGroup;  //是否是企业群
    protected boolean isPartyGroup; //是否是党群
    protected boolean isGroupChat; //是否是群聊
    protected int mChatType;   //聊天类型， (单聊，群聊，公众号， 我的电脑 等)

	public ViewHolder(View itemView ,Activity activity ,MessageChatListAdapter adapter ,BaseChatContract.Presenter presenter) {
		super(itemView);
		this.activity = activity;
		this.adapter = adapter;
		this.presenter = presenter;
		this.mContext = activity;
	}


    /**
     * 公用 点击事件<br/>
     * 负责 头像，已读动态的点击
     */
	public class NoDoubleClickListenerX implements View.OnClickListener{
		public static final int MIN_CLICK_DELAY_TIME = 1000;
		private long lastClickTime = 0;

		@Override
		public void onClick(View v) {
			long currentTime = Calendar.getInstance().getTimeInMillis();
			if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME ) {
				lastClickTime = currentTime;
				onNoDoubleClick(v);
			}
		}

		public void onNoDoubleClick(View v) {
            int position = getAdapterPosition();
            if (position == NO_POSITION) {
                return;
            }
            final Message msg = mMessage;
            int type = msg.getType();
            int mRawId = adapter.getRawId();
            if (v.getId() == R.id.svd_head) {
                if (App.getApplication() == null) {
                    return;
                }
                if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
                    UmengUtil.buryPoint(activity, "message_groupmessage_profilephoto", "联系人头像", 0);
                } else if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT) {
                    UmengUtil.buryPoint(activity, "message_p2pmessage_profilephoto", "联系人头像", 0);
                }

                if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_PC_CHAT) { //我的电脑页面头像不可点击
                    return;
                }
                String address = msg.getSendAddress();
                String completeAddress = address;
                address = PhoneUtils.getMinMatchNumber(address);
                String loginNum = LoginDaoImpl.getInstance().queryLoginUser(activity);
                loginNum = PhoneUtils.getMinMatchNumber(loginNum);
                if (TextUtils.equals(address, loginNum)) {
                    //跳转到个人名片
                    AboutMeProxy.g.getUiInterface().goToUserProfileActivity(activity);
                    return;
                }
                SimpleContact simpleContact = ContactsCache.getInstance().searchContactByNumber(address);
                if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
                    if (simpleContact == null) {
                        String name = NickNameUtils.getNickName(activity, msg.getSendAddress(), msg.getAddress());
                        if (adapter.getIsEPGroup() || adapter.getIsPartyGroup()) {
                            Employee employee = new Employee();
                            employee.setRegMobile(address);
                            employee.setName(name);
                            employee.setAddress(address);
                            employee.setMemberLevel(GroupUtils.getGroupLevel(mContext,msg.getAddress(),address));
                            employee.setMemberGroupId(msg.getAddress());
                            ContactProxy.g.getUiInterface().getContactDetailActivityUI().showForEmployee(mContext, employee);
                        } else {
                            GroupInfo groupInfo = GroupChatUtils.getGroupInfo(mContext, msg.getAddress());
                            String groupName = groupInfo.getPerson();
                            String groupCard = GroupChatUtils.getMemberNumber(mContext, msg.getAddress(), loginNum.contains("+86") ? loginNum : "+86" + loginNum);
                            GroupStrangerActivity.show(activity, address, completeAddress, name, msg.getAddress(), groupName, groupCard);
                        }
                    } else {
                        ContactProxy.g.getUiInterface().getContactDetailActivityUI().showForSimpleContact(activity, simpleContact, 0);
                    }
                } else if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT) {
                    if (simpleContact == null) {
                        if (TextUtils.isEmpty(address) && (type & Type.TYPE_RECV) > 0) {
                            address = msg.getAddress();
                        }
                        simpleContact = ContactsCache.getInstance().searchContactByNumber(address);
                        if (simpleContact == null) {
                            simpleContact = new SimpleContact();
                            simpleContact.setNumber(msg.getAddress());
                            simpleContact.setRawId(mRawId);
                            ContactProxy.g.getUiInterface().getContactDetailActivityUI().showForSimpleContact(mContext, simpleContact, 0);
                        } else {
                            ContactProxy.g.getUiInterface().getContactDetailActivityUI().showForSimpleContact(mContext, simpleContact, 0);
                        }

                    } else {
                        ContactProxy.g.getUiInterface().getContactDetailActivityUI().showForSimpleContact(mContext, simpleContact, 0);
                    }
                } else if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_SMSMMS_SINGLE_CHAT) {
                    if (type == Type.TYPE_MSG_TEXT_QUEUE || type == Type.TYPE_MSG_TEXT_OUTBOX || type == Type.TYPE_MSG_TEXT_DRAFT || type == Type.TYPE_MSG_TEXT_FAIL) {
                        type = Type.TYPE_MSG_TEXT_SEND;
                    }
                    boolean isLeft = (type & Type.TYPE_RECV) > 0;
                    if (!isLeft)
                        AboutMeProxy.g.getUiInterface().goToUserProfileActivity(mContext);
                    else {
                        address = msg.getAddress();
                        address = PhoneUtils.getMinMatchNumber(address);
                        simpleContact = ContactsCache.getInstance().searchContactByNumber(address);
                        if (simpleContact == null) {
                            simpleContact = new SimpleContact();
                            simpleContact.setNumber(msg.getAddress());
                            ContactProxy.g.getUiInterface().getContactDetailActivityUI().showForSimpleContact(mContext, simpleContact, 0);
                        } else {
                            ContactProxy.g.getUiInterface().getContactDetailActivityUI().showForSimpleContact(mContext, simpleContact, 0);
                        }
                    }
                }
                return;
            }

            if (v.getId() == R.id.tv_has_read) {
                if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT && (adapter.getIsEPGroup() || adapter.getIsPartyGroup())) {
                    String apAddress = (String) SharePreferenceUtils.getDBParam(SharePreferenceUtils.NAVIGATION_FILE_NAME, MyApplication.getApplication(), CommonConstant.NAVIGATION_AP_ADDRESS + msg.getSendAddress(), "");
                    String h5Address = (String) SharePreferenceUtils.getDBParam(SharePreferenceUtils.NAVIGATION_FILE_NAME, MyApplication.getApplication(), CommonConstant.NAVIGATION_READ_DETAIL_ADDRESS + msg.getSendAddress(), "");
                    if (!TextUtils.isEmpty(apAddress) && !TextUtils.isEmpty(h5Address)) {
                        String url = h5Address + "/message/#/readlist?";
                        String identify = GroupChatUtils.getIdentify(mContext, msg.getAddress());
                        if (TextUtils.isEmpty(identify)) {
                            LogF.e(TAG, "----error empty identify----");
                            return;
                        }

                        String groupNum = identify.substring(14, identify.indexOf("@"));
                        url = url + "to-uri=group:" + AndroidUtil.toURLEncoded(groupNum) + "&";
                        url = url + "from-uri=tel:" + AndroidUtil.toURLEncoded(msg.getSendAddress()) + "&";
                        url = url + "message-id=" + AndroidUtil.toURLEncoded(msg.getMsgId()) + "&";
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                        String format = sdf.format(msg.getDate());
                        url = url + "message-time=" + AndroidUtil.toURLEncoded(format) + "&";
                        url = url + "ap-address=" + AndroidUtil.toURLEncoded(apAddress);

                        WebConfig webConfig3 = new WebConfig.Builder().enableRequestToken(true).appId("63").build(url);
                        EnterPriseProxy.g.getUiInterface().gotoEnterpriseH5Activity(mContext, webConfig3);
                    } else {
                        LogF.e(TAG, "----error---- 导航地址获取失败");
                    }
                }
                return;
            }
		}
	}


	public class NoDoubleClickListener implements View.OnClickListener {
        public static final int MIN_CLICK_DELAY_TIME = 1000;
        private long lastClickTime = 0;

        @Override
        public void onClick(View v) {
            long currentTime = Calendar.getInstance().getTimeInMillis();
            if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME ) {
                lastClickTime = currentTime;
                onNoDoubleClick(v);
            }
        }

        public void onNoDoubleClick(View v) {
            Message msg = mMessage;//Message 不再从adapter取， 避免数组越界
            int type = msg.getType();
            int position = getAdapterPosition();
            if (position == NO_POSITION) {
                return;
            }
            switch (type) {
                case Type.TYPE_MSG_IMG_RECV:
                case Type.TYPE_MSG_IMG_SEND:
                case Type.TYPE_MSG_IMG_SEND_CCIND:
                    adapter.imageContentShow(position, ViewHolder.this, msg);
                    break;
                case Type.TYPE_MSG_OA_ONE_CARD_SEND:
                case Type.TYPE_MSG_OA_ONE_CARD_RECV:
                    if (activity != null) {
                        String subOriginUrl = null;
                        try {
                            subOriginUrl = URLDecoder.decode(msg.getSubOriginUrl(), "utf-8");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (subOriginUrl != null) {
                            Uri uri = Uri.parse(subOriginUrl);
                            uri.buildUpon().appendQueryParameter("_gc", "1");
                            String newUrl = WebConfig.generateStringUrl(uri);
                            WebConfig webConfig = new WebConfig.Builder().enableRequestToken(true).build(newUrl);
                            EnterPriseProxy.g.getUiInterface().gotoEnterpriseH5Activity(mContext, webConfig);
                        }
                    }
                    break;
                case Type.TYPE_MSG_DATE_ACTIVITY_RECV:
                case Type.TYPE_MSG_DATE_ACTIVITY_SEND:
                    DateActivity.launchActivity(activity, msg.getSubOriginUrl());
                    break;
                case Type.TYPE_MSG_ENTERPRISE_SHARE_RECV:
                case Type.TYPE_MSG_ENTERPRISE_SHARE_SEND:
                    LogF.d(TAG, "YAYIJI TYPE_MSG_ENTERPRISE_SHARE_SEND");
                    WebConfig.ShareItem shareItem = new WebConfig.ShareItem();
                    shareItem.shareType = Type.TYPE_MSG_T_CARD_SEND;
                    WebConfig webConfig = new WebConfig.Builder().shareItem(shareItem).build(msg.getSubOriginUrl());
                    EnterPriseProxy.g.getUiInterface().gotoEnterpriseH5Activity(mContext, webConfig);
                    break;
                case Type.TYPE_MSG_T_CARD_RECV:
                case Type.TYPE_MSG_T_CARD_SEND:
                case Type.TYPE_MSG_T_CARD_SEND_CCIND:
                    if (TextUtils.isEmpty(msg.getSubOriginUrl())) {
                        return;
                    }
                    LogF.d(TAG, "YAYIJI TYPE_MSG_T_CARD_SEND");

                    String bodyUrl = msg.getSubSourceUrl();
                    if (TextUtils.isEmpty(bodyUrl)) {
                        bodyUrl = YYUtils.getMatchString(msg.getXml_content(), "<body_link>(.*?)</body_link>");
                    }
                    EnterPriseProxy.g.getUiInterface().jumpToCommonBrowserWithShare(mContext, bodyUrl);
                    break;
                case Type.TYPE_MSG_MMS_RECV:
                case Type.TYPE_MSG_MMS_SEND:
                    //进入彩信详情
                    long mid;
                    if (TextUtils.isEmpty((msg.getXml_content()))) {
                        if (ObjectUtils.isNotNull(msg.getMsgId())) {
                            mid = Integer.parseInt(msg.getMsgId().substring(msg.getMsgId().indexOf("-") + 1));
                        } else {
                            BaseToast.show(R.string.read_mms_error);
                            return;
                        }
                    } else {
                        String content = msg.getXml_content();
                        mid = Integer.parseInt(content.substring(content.indexOf("-") + 1));
                    }
                    LogF.d(TAG, "mid:" + mid);
                    Intent mmsIntent = new Intent(activity, MmsDetailActivity.class);
                    Uri msgUri = Telephony.Mms.CONTENT_URI.buildUpon().
                            appendPath(Long.toString(mid)).build();
                    mmsIntent.setData(msgUri);
                    Bundle bundle = new Bundle();
                    bundle.putLong("pduid", mid);
                    mmsIntent.putExtras(bundle);
                    activity.startActivity(mmsIntent);
                    break;
            }
        }
    }

	public class OnMsgFailClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			adapter.showDialog(activity.getString(R.string.resent_message_hint), activity.getString(R.string.btn_cancel), activity.getString(R.string.btn_sure), new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					adapter.getDialog().dismiss();
				}
			}, new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					adapter.getDialog().dismiss();
					final Message msg = mMessage;
					if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
						UmengUtil.buryPoint(activity, "message_groupmessage_resend", "消息-群聊-重发", 0);
					} else if (mChatType== MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT) {
						UmengUtil.buryPoint(activity, "message_p2pmessage_resend", "消息-点对点-重发", 0);
					}
                    BuryingPointUtils.messageManipulationPuryingPoint(mContext , "重发");
                    presenter.reSend(msg);
				}
			});
		}
	}

	public class OnMsgContentLongClickListener implements View.OnLongClickListener {

		@Override
		public boolean onLongClick(View v) {
			if (adapter != null && adapter.getChatType() == MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT) {
				UmengUtil.buryPoint(MyApplication.getAppContext(), "message_p2pmessage_press", "消息-点对点会话-长按消息内容", 0);
			}
			adapter.isLongClick = true;
			MessageOprationDialog messageOprationDialog;
			String[] itemList = null;
			final int position = getAdapterPosition();
			if (position == NO_POSITION) {
				return true;
			}
			final int mChatType = adapter.getChatType();
			final Message msg = adapter.getItem(adapter.canLoadMore() ? position - 1 : position);
			LogF.d(TAG, "msg = " + msg);
			if (v.getId() == R.id.svd_head) {

				if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
					String address = msg.getSendAddress();
                    address = NumberUtils.getDialablePhoneWithCountryCode(address);
					String loginNum = NumberUtils.getDialablePhoneWithCountryCode(LoginDaoImpl.getInstance().queryLoginUser(activity));
					if (!TextUtils.equals(address, loginNum)) {
						String name = NickNameUtils.getNickName(activity, msg.getSendAddress(), msg.getAddress());
						GroupMember gm = new GroupMember();
						gm.setPerson(name);
						gm.setAddress(address);
						adapter.mSelectAtCallback.selectAtMember(gm);
					}
				}
				return true;
			}

			if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
				UmengUtil.buryPoint(activity, "message_groupmessage_press", "长按消息", 0);
			}


			int type = msg.getType();
			int boxType = msg.getBoxType();
			switch (type) {
				case Type.TYPE_MSG_TEXT_SUPER_SMS_SEND:
				case Type.TYPE_MSG_SMS_SEND:
				case Type.TYPE_MSG_SMS_RECV:
				case Type.TYPE_MSG_TEXT_RECV:
					if (App.getApplication() == null) {
						itemList = activity.getResources().getStringArray(R.array.msg_text_long_click_no_app);
					} else {
						if(mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT || mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT ||
								mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_PC_CHAT){
							itemList = activity.getResources().getStringArray(R.array.msg_text_long_click_more);
						}else{
							itemList = activity.getResources().getStringArray(R.array.msg_text_long_click);
						}
					}

					messageOprationDialog = new MessageOprationDialog(activity, null, itemList, null);
					messageOprationDialog.setOnMessageItemClickListener(new MessageOprationDialog.OnOprationItemClickListener() {
						@Override
						public void onClick(String item, int which, String address) {
							if (item.equals(activity.getString(R.string.copy))) {
								if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
									UmengUtil.buryPoint(activity, "message_groupmessage_press_copy", "复制", 0);
								} else {
									UmengUtil.buryPoint(activity, "message_p2pmessage_press_copy", "复制", 0);
								}
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"复制");
								MessageUtils.copyToClipboard(activity, msg.getBody());
							} else if (item.equals(activity.getString(R.string.forwarld))) {
								if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
									UmengUtil.buryPoint(activity, "message_groupmessage_press_send", "转发", 0);
								} else {
									UmengUtil.buryPoint(activity, "message_p2pmessage_press_send", "转发", 0);
								}
                                Intent i = ContactSelectorActivity.creatIntent(mContext,SOURCE_MESSAGE_FORWARD, 1);
								Bundle bundle = new Bundle();
								bundle.putInt(MESSAGE_TYPE, Type.TYPE_MSG_TEXT_SEND);
								bundle.putString(MESSAGE_BODY, msg.getBody());
								if (!BaseModel.DEFAULT_TEXT_SIZE.equals(msg.getTextSize())) {
									bundle.putString(MESSAGE_BODY_SIZE, msg.getTextSize());
								}
								i.putExtras(bundle);
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"转发");
								activity.startActivity(i);
							} else if (item.equals(activity.getString(R.string.delete))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"删除");
								presenter.deleteMessage(msg);
							} else if (item.equals(activity.getString(R.string.collect))) {
								if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
									UmengUtil.buryPoint(activity, "message_groupmessage_press_favorites", "收藏", 0);
								} else {
									UmengUtil.buryPoint(activity, "message_p2pmessage_press_favorites", "收藏", 0);
								}
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"收藏");
								presenter.addToFavorite(msg, mChatType, msg.getAddress());
							}else if(item.equals(activity.getString(R.string.multi_select))){
								//	进入多选模式
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"多选");
								((MessageDetailActivity) activity).changeMode(MessageDetailActivity.INTO_MULTI_SELECT_MODE);
								adapter.addSelection(position);
							}
						}
					});
					messageOprationDialog.show();
					break;
				case Type.TYPE_MSG_TEXT_SEND:
				case Type.TYPE_MSG_TEXT_SEND_CCIND:
					boolean isCmcc = false;
					String loginNum = LoginDaoImpl.getInstance().queryLoginUser(activity);
					if (!TextUtils.isEmpty(loginNum)) {
						isCmcc = AndroidUtil.isCMCCMobileByNumber(loginNum);
					}
					if (boxType == Type.TYPE_BOX_PLATFORM || boxType == Type.TYPE_BOX_PLATFORM_DEFAULT ) {
						itemList = activity.getResources().getStringArray(R.array.msg_text_long_click);
					}else if(boxType == Type.TYPE_BOX_PC){
						itemList = activity.getResources().getStringArray(R.array.msg_text_long_click_more);
					} else if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT && boxType == Type.TYPE_BOX_MESSAGE && isCmcc) {
						if (CanWithdraw(msg)) {
							// 如果当前用户是移动号，都要有转短功能
							itemList = activity.getResources().getStringArray(R.array.msg_text_to_sms_sender_long_click);
						} else {
							itemList = activity.getResources().getStringArray(R.array.msg_text_to_sms_sender_no_withdraw_long_click);
						}
					} else {
						if (CanWithdraw(msg)) {
							// 如果当前用户是非移动号，都不要有转短功能
							itemList = activity.getResources().getStringArray(R.array.msg_text_sender_long_click);
						} else {
							itemList = activity.getResources().getStringArray(R.array.msg_text_long_click_more);
						}
					}
					messageOprationDialog = new MessageOprationDialog(activity, null, itemList, null);
					messageOprationDialog.setOnMessageItemClickListener(new MessageOprationDialog.OnOprationItemClickListener() {
						@Override
						public void onClick(String item, int which, String address) {
							if (item.equals(activity.getString(R.string.copy))) {
								if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
									UmengUtil.buryPoint(activity, "message_groupmessage_press_copy", "复制", 0);
								} else {
									UmengUtil.buryPoint(activity, "message_p2pmessage_press_copy", "复制", 0);
								}
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"复制");
								MessageUtils.copyToClipboard(activity, msg.getBody());
							} else if (item.equals(activity.getString(R.string.forwarld))) {
								if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
									UmengUtil.buryPoint(activity, "message_groupmessage_press_send", "转发", 0);
								} else {
									UmengUtil.buryPoint(activity, "message_p2pmessage_press_send", "转发", 0);
								}

                                Intent i = ContactSelectorActivity.creatIntent(activity, SOURCE_MESSAGE_FORWARD,1);

								Bundle bundle = new Bundle();
								bundle.putInt(MESSAGE_TYPE, Type.TYPE_MSG_TEXT_SEND);
								bundle.putString(MESSAGE_BODY, msg.getBody());
								if (!BaseModel.DEFAULT_TEXT_SIZE.equals(msg.getTextSize())) {
									bundle.putString(MESSAGE_BODY_SIZE, msg.getTextSize());
								}
								i.putExtras(bundle);
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"转发");
								activity.startActivity(i);
							} else if (item.equals(activity.getString(R.string.back))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"撤回");
								if (ApplicationUtils.isNetworkAvailable(activity)) {
									//bingle
									if (CanWithdraw(msg)) {
										if (msg.getStatus() == Status.STATUS_WAITING || msg.getStatus() == Status.STATUS_LOADING) {
											BaseToast.show(R.string.back_londing);
										} else if (msg.getStatus() == Status.STATUS_FAIL || msg.getStatus() == Status.STATUS_PAUSE) {
											BaseToast.show(R.string.back_defail);
										} else if (MessageUtils.checkWithdrawnCap()) {
											LogF.i(TAG, "bingle--msgID：" + msg.getId());
											presenter.sendWithdrawnMessage(msg);
											firstShowWithdrawDialog();
										}
									} else {
										showWithdrawDialog();
									}
								}else{
									BaseToast.show(R.string.charge_network_error);
								}
							} else if (item.equals(activity.getString(R.string.delete))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"删除");
								presenter.deleteMessage(msg);
							} else if (item.equals(activity.getString(R.string.collect))) {
								if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
									UmengUtil.buryPoint(activity, "message_groupmessage_press_favorites", "收藏", 0);
								} else {
									UmengUtil.buryPoint(activity, "message_p2pmessage_press_favorites", "收藏", 0);
								}
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"收藏");
								presenter.addToFavorite(msg, mChatType, msg.getAddress());
							} else if (item.equals(activity.getString(R.string.change_to_sms_to_send))) {
								//转为短信发送
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"转短信");
								UmengUtil.buryPoint(activity, "message_p2pmessage_press_sms", "消息-点对点会话-长按消息-转为短信发送（移动用户）", 0);
								presenter.sendSuperMessage(msg.getBody());
							}else if(item.equals(activity.getString(R.string.multi_select))){
								//多选模式
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"多选");
								((MessageDetailActivity) activity).changeMode(MessageDetailActivity.INTO_MULTI_SELECT_MODE);
								adapter.addSelection(position);
							}
						}
					});
					messageOprationDialog.show();
					break;

				case Type.TYPE_MSG_VIDEO_RECV:

                    if(BitmapFactory.decodeFile(msg.getExtThumbPath()) == null){// 缩略图加载失败
                        BaseToast.show(R.string.load_failed);
                        break;
                    }

					itemList = activity.getResources().getStringArray(R.array.msg_img_long_click);
					messageOprationDialog = new MessageOprationDialog(activity, null, itemList, null);
					messageOprationDialog.setOnMessageItemClickListener(new MessageOprationDialog.OnOprationItemClickListener() {
						@Override
						public void onClick(String item, int which, String address) {
							if (item.equals(activity.getString(R.string.forwarld))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"转发");
								if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
									UmengUtil.buryPoint(activity, "message_groupmessage_press_send", "转发", 0);
								} else {
									UmengUtil.buryPoint(activity, "message_p2pmessage_press_send", "转发", 0);
								}

								String extFilePath = msg.getExtFilePath();
								File file = new File(extFilePath);
								if (!file.exists() || msg.getExtFileSize()>msg.getExtDownSize()) {
									BaseToast.show(R.string.forward_video_before_download);
									return;
								}
                                Intent i = ContactSelectorActivity.creatIntent(mContext,SOURCE_MESSAGE_FORWARD, 1);
								Bundle bundle = new Bundle();
								bundle.putInt(MESSAGE_TYPE, Type.TYPE_MSG_IMG_SEND);
								bundle.putString(MESSAGE_FILE_PATH, extFilePath);
								bundle.putString(MESSAGE_FILE_THUMB_PATH, msg.getExtThumbPath());
								i.putExtras(bundle);
								activity.startActivity(i);
							} else if (item.equals(activity.getString(R.string.delete))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"删除");
								if (msg.getStatus() == Status.STATUS_WAITING || msg.getStatus() == Status.STATUS_LOADING) {
									BaseToast.show(R.string.delete_londing);
								} else {
									presenter.deleteMessage(msg);
								}
							} else if (item.equals(activity.getString(R.string.collect))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"收藏");
								if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
									UmengUtil.buryPoint(activity, "message_groupmessage_press_favorites", "收藏", 0);
								} else {
									UmengUtil.buryPoint(activity, "message_p2pmessage_press_favorites", "收藏", 0);
								}
								presenter.addToFavorite(msg, mChatType, msg.getAddress());
							}else if (item.equals(activity.getString(R.string.multi_select))){
								//多选模式
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"多选");
								((MessageDetailActivity) activity).changeMode(MessageDetailActivity.INTO_MULTI_SELECT_MODE);
								adapter.addSelection(position);
							}
						}
					});
					messageOprationDialog.show();
					break;
				case Type.TYPE_MSG_VIDEO_SEND:
				case Type.TYPE_MSG_VIDEO_SEND_CCIND:

					if(BitmapFactory.decodeFile(msg.getExtThumbPath()) == null){// 缩略图加载失败
						BaseToast.show(R.string.load_failed);
						break;
					}

					if (boxType == Type.TYPE_BOX_PLATFORM || boxType == Type.TYPE_BOX_PLATFORM_DEFAULT || boxType == Type.TYPE_BOX_PC) {
						itemList = activity.getResources().getStringArray(R.array.msg_img_long_click);
					} else {
						if (CanWithdraw(msg)) {
							itemList = activity.getResources().getStringArray(R.array.msg_img_sender_long_click);
						} else {
							itemList = activity.getResources().getStringArray(R.array.msg_img_long_click);
						}
					}
					messageOprationDialog = new MessageOprationDialog(activity, null, itemList, null);
					messageOprationDialog.setOnMessageItemClickListener(new MessageOprationDialog.OnOprationItemClickListener() {
						@Override
						public void onClick(String item, int which, String address) {
							if (item.equals(activity.getString(R.string.forwarld))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"转发");
								if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
									UmengUtil.buryPoint(activity, "message_groupmessage_press_send", "转发", 0);
								} else {
									UmengUtil.buryPoint(activity, "message_p2pmessage_press_send", "转发", 0);
								}
								String extFilePath = msg.getExtFilePath();
								File file = new File(extFilePath);
								if(Type.TYPE_MSG_VIDEO_SEND_CCIND == msg.getType()
                                        &&(!file.exists() || msg.getExtDownSize()<msg.getExtFileSize())){
                                    BaseToast.show(R.string.forward_video_before_download);
                                    return;
                                }
                                Intent i = ContactSelectorActivity.creatIntent(mContext,SOURCE_MESSAGE_FORWARD, 1);
								Bundle bundle = new Bundle();
								bundle.putInt(MESSAGE_TYPE, Type.TYPE_MSG_IMG_SEND);
								bundle.putString(MESSAGE_FILE_PATH, extFilePath);
								bundle.putString(MESSAGE_FILE_THUMB_PATH, msg.getExtThumbPath());
								i.putExtras(bundle);
								activity.startActivity(i);
							} else if (item.equals(activity.getString(R.string.back))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"撤回");
								if (ApplicationUtils.isNetworkAvailable(activity)) {
									//bingle
									if (CanWithdraw(msg)) {
										if (msg.getStatus() == Status.STATUS_WAITING || msg.getStatus() == Status.STATUS_LOADING) {
											BaseToast.show(R.string.back_londing);
										} else if (msg.getStatus() == Status.STATUS_FAIL || msg.getStatus() == Status.STATUS_PAUSE) {
											BaseToast.show(R.string.back_defail);
										} else if (MessageUtils.checkWithdrawnCap()) {
											LogF.i(TAG, "bingle--msgID：" + msg.getId());
											presenter.sendWithdrawnMessage(msg);
											firstShowWithdrawDialog();
										}
									} else {
										showWithdrawDialog();
									}
								}else{
									BaseToast.show(R.string.charge_network_error);
								}
							} else if (item.equals(activity.getString(R.string.delete))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"删除");
								presenter.deleteMessage(msg);
							} else if (item.equals(activity.getString(R.string.collect))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"收藏");
								if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
									UmengUtil.buryPoint(activity, "message_groupmessage_press_favorites", "收藏", 0);
								} else {
									UmengUtil.buryPoint(activity, "message_p2pmessage_press_favorites", "收藏", 0);
								}
								presenter.addToFavorite(msg, mChatType, msg.getAddress());
							}else if(item.equals(activity.getString(R.string.multi_select))){
								//多选模式
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"多选");
								((MessageDetailActivity) activity).changeMode(MessageDetailActivity.INTO_MULTI_SELECT_MODE);
								adapter.addSelection(position);
							}
						}
					});
					messageOprationDialog.show();
					break;

				case Type.TYPE_MSG_IMG_RECV:

					if(BitmapFactory.decodeFile(msg.getExtThumbPath()) == null){// 缩略图加载失败
						BaseToast.show(R.string.load_failed);
						break;
					}
                    itemList = mContext.getResources().getStringArray(R.array.msg_img_edit_long_click);
					final boolean isRecvImageDownloading = (msg.getExtDownSize() < msg.getExtFileSize()) || (!new File(msg.getExtFilePath()).exists());
					messageOprationDialog = new MessageOprationDialog(activity, null, itemList, null);
					messageOprationDialog.setOnMessageItemClickListener(new MessageOprationDialog.OnOprationItemClickListener() {
						@Override
						public void onClick(String item, int which, String address) {
							if (item.equals(activity.getString(R.string.forwarld))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"转发");
								if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
									UmengUtil.buryPoint(activity, "message_groupmessage_press_send", "转发", 0);
								} else {
									UmengUtil.buryPoint(activity, "message_p2pmessage_press_send", "转发", 0);
								}
								String extFilePath = msg.getExtFilePath();
								if (isRecvImageDownloading) {
									BaseToast.show(R.string.toast_download_img);
									return;
								} else if (msg.getStatus() == Status.STATUS_LOADING) {
									BaseToast.show(R.string.downloading_img);
									return;
								}
                                Intent i = ContactSelectorActivity.creatIntent(mContext,SOURCE_MESSAGE_FORWARD, 1);
								Bundle bundle = new Bundle();
								bundle.putInt(MESSAGE_TYPE, Type.TYPE_MSG_IMG_SEND);
								bundle.putString(MESSAGE_FILE_PATH, extFilePath);
								bundle.putString(MESSAGE_FILE_THUMB_PATH, msg.getExtThumbPath());

                                boolean isBigImage = msg.getExtFileSize()> FileUtil.MAX_IMG_SIZE;//目前，只有接收的情况，会出现超大图.
                                if(isBigImage){
                                    File midFile = new File(PreviewImagePresenter.getPreviewImagePath(extFilePath));
                                    if(midFile == null || !midFile.exists()){//中间图不存在
                                        BaseToast.show(R.string.downloading_img);
                                        PreviewImagePresenter.compressImage(extFilePath,null,null);
                                    }else{
                                        bundle.putString(MESSAGE_FILE_PATH, midFile.getPath());
                                    }
                                }
                                i.putExtras(bundle);
								activity.startActivity(i);
							} else if (item.equals(activity.getString(R.string.delete))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"删除");
								presenter.deleteMessage(msg);
							} else if (item.equals(activity.getString(R.string.image_edit))) {
								String extFilePath = msg.getExtFilePath();
								File file = new File(extFilePath);
								Uri uri = Uri.fromFile(file);
								if (isRecvImageDownloading) {
                                    if (!file.exists()) {
                                        BaseToast.show(R.string.toast_download_img_edit);
                                        return;
                                    } else if (msg.getStatus() == Status.STATUS_LOADING) {
                                        BaseToast.show(R.string.downloading_img);
                                        return;
                                    }
								} else {
                                    boolean isBigImage = msg.getExtFileSize()> FileUtil.MAX_IMG_SIZE;//目前，只有接收的情况，会出现超大图.
								    if(isBigImage){
                                        File midFile = new File(PreviewImagePresenter.getPreviewImagePath(extFilePath));
                                        if(midFile == null || !midFile.exists()){//中间图不存在
                                            BaseToast.show(R.string.downloading_img);
                                            PreviewImagePresenter.compressImage(extFilePath,null,null);
                                        }else{
                                            uri = Uri.fromFile(midFile);
                                            ImgEditorProxy.g.getUiInterface().copyAndEditImageActivity(activity, uri, midFile.getPath());
                                        }
                                    }else{
                                        ImgEditorProxy.g.getUiInterface().copyAndEditImageActivity(activity, uri, extFilePath);
                                    }
								}
							} else if (item.equals(activity.getString(R.string.collect))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"收藏");
								if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
									UmengUtil.buryPoint(activity, "message_groupmessage_press_favorites", "收藏", 0);
								} else {
									UmengUtil.buryPoint(activity, "message_p2pmessage_press_favorites", "收藏", 0);
								}
								if (TextUtils.isEmpty(msg.getPerson()) && !TextUtils.isEmpty(adapter.getPublicAccountTitle()) && mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_PUBLICACCOUNT_CHAT) {
									msg.setPerson(adapter.getPublicAccountTitle());
								}
								presenter.addToFavorite(msg, mChatType, msg.getAddress());
							}else if(item.equals(activity.getString(R.string.multi_select))){
								//多选模式
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"多选");
								((MessageDetailActivity) activity).changeMode(MessageDetailActivity.INTO_MULTI_SELECT_MODE);
								adapter.addSelection(position);
							}
						}
					});
					messageOprationDialog.show();
					break;
				case Type.TYPE_MSG_IMG_SEND:
				case Type.TYPE_MSG_IMG_SEND_CCIND:

					if(BitmapFactory.decodeFile(msg.getExtThumbPath()) == null){// 缩略图加载失败
						BaseToast.show(R.string.load_failed);
						break;
					}

					final boolean isCCINDDownloading = (type == Type.TYPE_MSG_IMG_SEND_CCIND && msg.getExtDownSize() < msg.getExtFileSize()) || (!new File(msg.getExtFilePath()).exists());//抄送的文件。

					if (boxType == Type.TYPE_BOX_PLATFORM || boxType == Type.TYPE_BOX_PLATFORM_DEFAULT || boxType == Type.TYPE_BOX_PC) {
						itemList = activity.getResources().getStringArray(R.array.msg_img_edit_long_click);
					} else {
						if (CanWithdraw(msg)) {
							itemList = activity.getResources().getStringArray(R.array.msg_img_edit_sender_long_click);
						} else {
							itemList = activity.getResources().getStringArray(R.array.msg_img_edit_long_click);
						}
					}
					messageOprationDialog = new MessageOprationDialog(activity, "", itemList, null);
					messageOprationDialog.setOnMessageItemClickListener(new MessageOprationDialog.OnOprationItemClickListener() {
						@Override
						public void onClick(String item, int which, String address) {
							if (item.equals(activity.getString(R.string.forwarld))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"转发");
								if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
									UmengUtil.buryPoint(activity, "message_groupmessage_press_send", "转发", 0);
								} else {
									UmengUtil.buryPoint(activity, "message_p2pmessage_press_send", "转发", 0);
								}
								String extFilePath = msg.getExtFilePath();
								if (isCCINDDownloading) {
									BaseToast.show(R.string.toast_download_img);
									return;
								}
//								Intent i = ContactsSelectActivity.createIntentForMessageForward(activity);
                                Intent i = ContactSelectorActivity.creatIntent(mContext,SOURCE_MESSAGE_FORWARD, 1);
								Bundle bundle = new Bundle();
								bundle.putInt(MESSAGE_TYPE, Type.TYPE_MSG_IMG_SEND);
								bundle.putString(MESSAGE_FILE_PATH, extFilePath);
								bundle.putString(MESSAGE_FILE_THUMB_PATH, msg.getExtThumbPath());
								i.putExtras(bundle);
								activity.startActivity(i);
							} else if (item.equals(activity.getString(R.string.back))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"撤回");
								if (ApplicationUtils.isNetworkAvailable(activity)) {
									if (CanWithdraw(msg)) {
										if (msg.getStatus() == Status.STATUS_WAITING || msg.getStatus() == Status.STATUS_LOADING) {
											BaseToast.show(R.string.back_londing);
										} else if (msg.getStatus() == Status.STATUS_FAIL || msg.getStatus() == Status.STATUS_PAUSE) {
											BaseToast.show(R.string.back_defail);
										} else if (MessageUtils.checkWithdrawnCap()) {
											LogF.i(TAG, "bingle--msgID：" + msg.getId());
											presenter.sendWithdrawnMessage(msg);
											firstShowWithdrawDialog();
										}
									} else {
										showWithdrawDialog();
									}
								}else{
									BaseToast.show(R.string.charge_network_error);
								}
							} else if (item.equals(activity.getString(R.string.delete))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"删除");
								presenter.deleteMessage(msg);
							} else if (item.equals(activity.getString(R.string.image_edit))) {
								String extFilePath = msg.getExtFilePath();
								File file = new File(extFilePath);
								Uri uri = Uri.fromFile(file);
								if (isCCINDDownloading) {
                                    if (!file.exists()) {
                                        BaseToast.show(R.string.toast_download_img_edit);
                                        return;
                                    } else if (msg.getStatus() == Status.STATUS_LOADING) {
                                        BaseToast.show(R.string.downloading_img);
                                        return;
                                    }
								} else {
                                    ImgEditorProxy.g.getUiInterface().copyAndEditImageActivity(activity, uri, extFilePath);
								}
							} else if (item.equals(activity.getString(R.string.collect))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"转发");
								if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
									UmengUtil.buryPoint(activity, "message_groupmessage_press_favorites", "收藏", 0);
								} else {
									UmengUtil.buryPoint(activity, "message_p2pmessage_press_favorites", "收藏", 0);
								}
								String extFilePath = msg.getExtFilePath();
								File file = new File(extFilePath);
								if (!file.exists()) {
									BaseToast.show(R.string.download_first);
									return;
								} else if (msg.getStatus() == Status.STATUS_LOADING) {
									BaseToast.show(R.string.downloading_img);
									return;
								}
								presenter.addToFavorite(msg, mChatType, msg.getAddress());
							}else if (item.equals(activity.getString(R.string.multi_select))){
								//多选模式
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"多选");
								((MessageDetailActivity) activity).changeMode(MessageDetailActivity.INTO_MULTI_SELECT_MODE);
								adapter.addSelection(position);
							}
						}
					});
					messageOprationDialog.show();
					break;
				case Type.TYPE_MSG_AUDIO_SEND:
				case Type.TYPE_MSG_AUDIO_SEND_CCIND:
					if (boxType == Type.TYPE_BOX_PLATFORM || boxType == Type.TYPE_BOX_PLATFORM_DEFAULT ||boxType == Type.TYPE_BOX_PC
							|| !CanWithdraw(msg)) {
						if(!TextUtils.isEmpty(msg.getBody())){
							itemList = activity.getResources().getStringArray(R.array.msg_audio_receiver_long_click_no_collect);
						}else{
							itemList = activity.getResources().getStringArray(R.array.msg_audio_receiver_long_click);
						}
					} else {
						if(!TextUtils.isEmpty(msg.getBody())){
							itemList = activity.getResources().getStringArray(R.array.msg_text_long_click_de_and_rest);
						}else{
							itemList = activity.getResources().getStringArray(R.array.msg_audio_sender_long_click);
						}
					}
					messageOprationDialog = new MessageOprationDialog(activity, null, itemList, null);
					messageOprationDialog.setOnMessageItemClickListener(new MessageOprationDialog.OnOprationItemClickListener() {
						@Override
						public void onClick(String item, int which, String address) {
							if (item.equals(activity.getString(R.string.back))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"撤回");
								if (ApplicationUtils.isNetworkAvailable(activity)) {
									if (CanWithdraw(msg)) {
										if (msg.getStatus() == Status.STATUS_WAITING || msg.getStatus() == Status.STATUS_LOADING) {
											BaseToast.show(R.string.back_londing);
										} else if (msg.getStatus() == Status.STATUS_FAIL || msg.getStatus() == Status.STATUS_PAUSE) {
											BaseToast.show(R.string.back_defail);
										} else if (MessageUtils.checkWithdrawnCap()) {
											LogF.i(TAG, "bingle--msgID：" + msg.getId());
											if (RcsAudioPlayer.getInstence(mContext).isPlaying() && adapter != null && !TextUtils.isEmpty(adapter.audioMessageID) && adapter.audioMessageID.equals(msg.getMsgId())) {
												RcsAudioPlayer.getInstence(mContext).stop();
												if (adapter.mPlayingSendAudio != null) { // 发送图片复位
													adapter.mPlayingSendAudio.setImageResource(R.drawable.message_voice_playing_send_f6);
												}
												if (adapter.mPlayBgSend != null) { // 发送图片复位
													adapter.mPlayBgSend.setImageResource(R.drawable.chat_voiceinformation_stop_big2);
												}
												if (adapter.mPlaySmallSend != null) { // 发送图片复位
													adapter.mPlaySmallSend.setImageResource(R.drawable.chat_voiceinformation_stop_small2);
												}
												if (adapter.mAudioPlayProgressBar != null) { // 发送图片复位
													adapter.mAudioPlayProgressBar.setProgress(0);
													adapter.mAudioPlayProgressBar.setVisibility(View.GONE);
												}
											}
											presenter.sendWithdrawnMessage(msg);
											firstShowWithdrawDialog();
										}
									} else {
										showWithdrawDialog();
									}
								}else{
									BaseToast.show(R.string.charge_network_error);
								}
							} else if (item.equals(activity.getString(R.string.delete))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"删除");
								if(RcsAudioPlayer.getInstence(mContext).isPlaying() && adapter != null && !TextUtils.isEmpty(adapter.audioMessageID) && adapter.audioMessageID.equals(msg.getMsgId())){
									RcsAudioPlayer.getInstence(mContext).stop();
									if (adapter.mPlayingSendAudio != null) { // 发送图片复位
										adapter.mPlayingSendAudio.setImageResource(R.drawable.message_voice_playing_send_f6);
									}
									if (adapter.mPlayBgSend != null) { // 发送图片复位
										adapter.mPlayBgSend.setImageResource(R.drawable.chat_voiceinformation_stop_big2);
									}
									if (adapter.mPlaySmallSend != null) { // 发送图片复位
										adapter.mPlaySmallSend.setImageResource(R.drawable.chat_voiceinformation_stop_small2);
									}
									if (adapter.mAudioPlayProgressBar != null) { // 发送图片复位
										adapter.mAudioPlayProgressBar.setProgress(0);
										adapter.mAudioPlayProgressBar.setVisibility(View.GONE);
									}
								}
								presenter.deleteMessage(msg);
							} else if (item.equals(activity.getString(R.string.collect))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"收到");
								if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
									UmengUtil.buryPoint(activity, "message_groupmessage_press_favorites", "收藏", 0);
								} else {
									UmengUtil.buryPoint(activity, "message_p2pmessage_press_favorites", "收藏", 0);
								}
								if(RcsAudioPlayer.getInstence(mContext).isPlaying() && adapter != null && !TextUtils.isEmpty(adapter.audioMessageID) && adapter.audioMessageID.equals(msg.getMsgId())){
									RcsAudioPlayer.getInstence(mContext).stop();
									if (adapter.mPlayingSendAudio != null) { // 发送图片复位
										adapter.mPlayingSendAudio.setImageResource(R.drawable.message_voice_playing_send_f6);
									}
									if (adapter.mPlayBgSend != null) { // 发送图片复位
										adapter.mPlayBgSend.setImageResource(R.drawable.chat_voiceinformation_stop_big2);
									}
									if (adapter.mPlaySmallSend != null) { // 发送图片复位
										adapter.mPlaySmallSend.setImageResource(R.drawable.chat_voiceinformation_stop_small2);
									}
									if (adapter.mAudioPlayProgressBar != null) { // 发送图片复位
										adapter.mAudioPlayProgressBar.setProgress(0);
										adapter.mAudioPlayProgressBar.setVisibility(View.GONE);
									}
								}
								presenter.addToFavorite(msg, mChatType, msg.getAddress());
							}else if(item.equals(activity.getString(R.string.copy))){
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"复制");
								if(RcsAudioPlayer.getInstence(mContext).isPlaying() && adapter != null && !TextUtils.isEmpty(adapter.audioMessageID) && adapter.audioMessageID.equals(msg.getMsgId())){
									RcsAudioPlayer.getInstence(mContext).stop();
									if (adapter.mPlayingSendAudio != null) { // 发送图片复位
										adapter.mPlayingSendAudio.setImageResource(R.drawable.message_voice_playing_send_f6);
									}
									if (adapter.mPlayBgSend != null) { // 发送图片复位
										adapter.mPlayBgSend.setImageResource(R.drawable.chat_voiceinformation_stop_big2);
									}
									if (adapter.mPlaySmallSend != null) { // 发送图片复位
										adapter.mPlaySmallSend.setImageResource(R.drawable.chat_voiceinformation_stop_small2);
									}
									if (adapter.mAudioPlayProgressBar != null) { // 发送图片复位
										adapter.mAudioPlayProgressBar.setProgress(0);
										adapter.mAudioPlayProgressBar.setVisibility(View.GONE);
									}
								}
								MessageUtils.copyToClipboard(activity, msg.getBody());
							}else if(item.equals(activity.getString(R.string.multi_select))){
								//多选模式
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"多选");
								((MessageDetailActivity) activity).changeMode(MessageDetailActivity.INTO_MULTI_SELECT_MODE);
								adapter.addSelection(position);
							}
						}
					});
					messageOprationDialog.show();
					break;
				case Type.TYPE_MSG_AUDIO_RECV:
					if(!TextUtils.isEmpty(msg.getBody())){
						itemList = activity.getResources().getStringArray(R.array.msg_audio_receiver_long_click_no_collect); // 没有收藏
					}else{
						itemList = activity.getResources().getStringArray(R.array.msg_audio_receiver_long_click);
					}
					messageOprationDialog = new MessageOprationDialog(activity, null, itemList, null);
					messageOprationDialog.setOnMessageItemClickListener(new MessageOprationDialog.OnOprationItemClickListener() {
						@Override
						public void onClick(String item, int which, String address) {
							if (item.equals(activity.getString(R.string.delete))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"删除");
								if(RcsAudioPlayer.getInstence(mContext).isPlaying() && adapter != null && !TextUtils.isEmpty(adapter.audioMessageID) && adapter.audioMessageID.equals(msg.getMsgId())){
									RcsAudioPlayer.getInstence(mContext).stop();
									if (adapter.mPlayingRecAudio != null) { // 发送图片复位
										adapter.mPlayingRecAudio.setImageResource(R.drawable.message_voice_playing_f6);
									}
									if (adapter.mPlayBgRec != null) { // 发送图片复位
										adapter.mPlayBgRec.setImageResource(R.drawable.chat_voiceinformation_stop_big);
									}
									if (adapter.mPlaySmallRec != null) { // 发送图片复位
										adapter.mPlaySmallRec.setImageResource(R.drawable.chat_voiceinformation_stop_small);
									}
									if (adapter.mAudioPlayProgressBar != null) { // 发送图片复位
										adapter.mAudioPlayProgressBar.setProgress(0);
										adapter.mAudioPlayProgressBar.setVisibility(View.GONE);
									}
								}
								presenter.deleteMessage(msg);
							} else if (item.equals(activity.getString(R.string.collect))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"收藏");
								if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
									UmengUtil.buryPoint(activity, "message_groupmessage_press_favorites", "收藏", 0);
								} else {
									UmengUtil.buryPoint(activity, "message_p2pmessage_press_favorites", "收藏", 0);
								}
								if(RcsAudioPlayer.getInstence(mContext).isPlaying() && adapter != null && !TextUtils.isEmpty(adapter.audioMessageID) && adapter.audioMessageID.equals(msg.getMsgId())){
									RcsAudioPlayer.getInstence(mContext).stop();
									if (adapter.mPlayingRecAudio != null) { // 发送图片复位
										adapter.mPlayingRecAudio.setImageResource(R.drawable.message_voice_playing_f6);
									}
									if (adapter.mPlayBgRec != null) { // 发送图片复位
										adapter.mPlayBgRec.setImageResource(R.drawable.chat_voiceinformation_stop_big);
									}
									if (adapter.mPlaySmallRec != null) { // 发送图片复位
										adapter.mPlaySmallRec.setImageResource(R.drawable.chat_voiceinformation_stop_small);
									}
									if (adapter.mAudioPlayProgressBar != null) { // 发送图片复位
										adapter.mAudioPlayProgressBar.setProgress(0);
										adapter.mAudioPlayProgressBar.setVisibility(View.GONE);
									}
								}
								presenter.addToFavorite(msg, mChatType, msg.getAddress());
							}else if(item.equals(activity.getString(R.string.copy))){
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"复制");
								if(RcsAudioPlayer.getInstence(mContext).isPlaying() && adapter != null && !TextUtils.isEmpty(adapter.audioMessageID) && adapter.audioMessageID.equals(msg.getMsgId())){
									RcsAudioPlayer.getInstence(mContext).stop();
									if (adapter.mPlayingRecAudio != null) { // 发送图片复位
										adapter.mPlayingRecAudio.setImageResource(R.drawable.message_voice_playing_f6);
									}
									if (adapter.mPlayBgRec != null) { // 发送图片复位
										adapter.mPlayBgRec.setImageResource(R.drawable.chat_voiceinformation_stop_big);
									}
									if (adapter.mPlaySmallRec != null) { // 发送图片复位
										adapter.mPlaySmallRec.setImageResource(R.drawable.chat_voiceinformation_stop_small);
									}
									if (adapter.mAudioPlayProgressBar != null) { // 发送图片复位
										adapter.mAudioPlayProgressBar.setProgress(0);
										adapter.mAudioPlayProgressBar.setVisibility(View.GONE);
									}
								}
								MessageUtils.copyToClipboard(activity, msg.getBody());
							}else if(item.equals(activity.getString(R.string.multi_select))){
								//多选模式
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"多选");
								((MessageDetailActivity) activity).changeMode(MessageDetailActivity.INTO_MULTI_SELECT_MODE);
								adapter.addSelection(position);
							}
						}
					});
					messageOprationDialog.show();
					break;
				case Type.TYPE_MSG_FILE_SEND:
				case Type.TYPE_MSG_FILE_SEND_CCIND:
					if (boxType == Type.TYPE_BOX_PLATFORM || boxType == Type.TYPE_BOX_PLATFORM_DEFAULT ||boxType == Type.TYPE_BOX_PC
							|| !CanWithdraw(msg)) {
						itemList = activity.getResources().getStringArray(R.array.msg_img_long_click);
					} else {
						itemList = activity.getResources().getStringArray(R.array.msg_img_sender_long_click);
					}
					messageOprationDialog = new MessageOprationDialog(activity, null, itemList, null);
					messageOprationDialog.setOnMessageItemClickListener(new MessageOprationDialog.OnOprationItemClickListener() {
						@Override
						public void onClick(String item, int which, String address) {
							if (item.equals(activity.getString(R.string.forwarld))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"转发");
								if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
									UmengUtil.buryPoint(activity, "message_groupmessage_press_send", "转发", 0);
								} else {
									UmengUtil.buryPoint(activity, "message_p2pmessage_press_send", "转发", 0);
								}
								String extFilePath = msg.getExtFilePath();
								File file = new File(extFilePath);
								if (!file.exists()) {
									BaseToast.show(R.string.forward_file_before_download);
									return;
								}
//								Intent i = ContactsSelectActivity.createIntentForMessageForward(activity);
                                Intent i = ContactSelectorActivity.creatIntent(mContext,SOURCE_MESSAGE_FORWARD, 1);
								Bundle bundle = new Bundle();
								bundle.putInt(MESSAGE_TYPE, Type.TYPE_MSG_FILE_SEND);
								bundle.putString(MESSAGE_FILE_PATH, extFilePath);
								bundle.putString(MESSAGE_FILE_THUMB_PATH, msg.getExtThumbPath());
								i.putExtras(bundle);
								activity.startActivity(i);
							} else if (item.equals(activity.getString(R.string.back))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"撤回");
								if (ApplicationUtils.isNetworkAvailable(activity)) {
									if (CanWithdraw(msg)) {
										if (msg.getStatus() == Status.STATUS_WAITING || msg.getStatus() == Status.STATUS_LOADING) {
											BaseToast.show(R.string.back_londing);
										} else if (msg.getStatus() == Status.STATUS_FAIL || msg.getStatus() == Status.STATUS_PAUSE) {
											BaseToast.show(R.string.back_defail);
										} else if (MessageUtils.checkWithdrawnCap()) {
											LogF.i(TAG, "bingle--msgID：" + msg.getId());
											presenter.sendWithdrawnMessage(msg);
											firstShowWithdrawDialog();
										}
									} else {
										showWithdrawDialog();
									}
								}else{
									BaseToast.show(R.string.charge_network_error);
								}
							} else if (item.equals(activity.getString(R.string.delete))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"删除");
								presenter.deleteMessage(msg);
							} else if (item.equals(activity.getString(R.string.collect))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"收藏");
								if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
									UmengUtil.buryPoint(activity, "message_groupmessage_press_favorites", "收藏", 0);
								} else {
									UmengUtil.buryPoint(activity, "message_p2pmessage_press_favorites", "收藏", 0);
								}
								presenter.addToFavorite(msg, mChatType, msg.getAddress());
							} else if (item.equals(activity.getString(R.string.multi_select))) {
								//多选模式
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"多选");
								((MessageDetailActivity) activity).changeMode(MessageDetailActivity.INTO_MULTI_SELECT_MODE);
								adapter.addSelection(position);
							}
						}
					});
					messageOprationDialog.show();
					break;
				case Type.TYPE_MSG_FILE_RECV:
					itemList = activity.getResources().getStringArray(R.array.msg_img_long_click);
					messageOprationDialog = new MessageOprationDialog(activity, null, itemList, null);
					messageOprationDialog.setOnMessageItemClickListener(new MessageOprationDialog.OnOprationItemClickListener() {
						@Override
						public void onClick(String item, int which, String address) {
							if (item.equals(activity.getString(R.string.forwarld))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"转发");
								if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
									UmengUtil.buryPoint(activity, "message_groupmessage_press_send", "转发", 0);
								} else {
									UmengUtil.buryPoint(activity, "message_p2pmessage_press_send", "转发", 0);
								}
								String extFilePath = msg.getExtFilePath();
								File file = new File(extFilePath);
								if (!file.exists() || file.length()<msg.getExtFileSize()) {// 文件不存在， 或者文件未下载结束。
									BaseToast.show(R.string.forward_file_before_download);
									return;
								}
//								Intent i = ContactsSelectActivity.createIntentForMessageForward(activity);
                                Intent i = ContactSelectorActivity.creatIntent(mContext,SOURCE_MESSAGE_FORWARD, 1);
								Bundle bundle = new Bundle();
								bundle.putInt(MESSAGE_TYPE, Type.TYPE_MSG_FILE_SEND);
								bundle.putString(MESSAGE_FILE_PATH, extFilePath);
								bundle.putString(MESSAGE_FILE_THUMB_PATH, msg.getExtThumbPath());
								i.putExtras(bundle);
								activity.startActivity(i);
							} else if (item.equals(activity.getString(R.string.delete))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"删除");
								presenter.deleteMessage(msg);
							} else if (item.equals(activity.getString(R.string.collect))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"收藏");
								if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
									UmengUtil.buryPoint(activity, "message_groupmessage_press_favorites", "收藏", 0);
								} else {
									UmengUtil.buryPoint(activity, "message_p2pmessage_press_favorites", "收藏", 0);
								}
								presenter.addToFavorite(msg, mChatType, msg.getAddress());
							}else if (item.equals(activity.getString(R.string.multi_select))){
								//多选模式
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"多选");
								((MessageDetailActivity) activity).changeMode(MessageDetailActivity.INTO_MULTI_SELECT_MODE);
								adapter.addSelection(position);
							}
						}
					});
					messageOprationDialog.show();
					break;
				case Type.TYPE_MSG_FILE_YUN_SEND:
                    if (CanWithdraw(msg)) {
                        itemList = activity.getResources().getStringArray(R.array.msg_img_sender_long_click);
                    } else {
                        itemList = activity.getResources().getStringArray(R.array.msg_img_long_click);
                    }
					messageOprationDialog = new MessageOprationDialog(activity, activity.getString(R.string.file), itemList, null);
					messageOprationDialog.setOnMessageItemClickListener(new MessageOprationDialog.OnOprationItemClickListener() {
						@Override
						public void onClick(String item, int which, String address) {
							if (item.equals(activity.getString(R.string.forwarld))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"转发");
								if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
									UmengUtil.buryPoint(activity, "message_groupmessage_press_send", "转发", 0);
								} else {
									UmengUtil.buryPoint(activity, "message_p2pmessage_press_send", "转发", 0);
								}
								YunFile yunfile = YunFileXmlParser.parserYunFileXml(msg.getBody());
								if (yunfile == null) {
									return;
								}
								File file = new File(yunfile.getLocalPath());
								if (!file.exists() || file.length()< yunfile.getFileSize()) {
									BaseToast.show(R.string.forward_file_before_download);
									return;
								}
//								Intent i = ContactsSelectActivity.createIntentForMessageForward(activity);
                                Intent i = ContactSelectorActivity.creatIntent(mContext,SOURCE_MESSAGE_FORWARD, 1);
								Bundle bundle = new Bundle();
								bundle.putInt(MESSAGE_TYPE, Type.TYPE_MSG_FILE_SEND);
								bundle.putString(MESSAGE_FILE_PATH, file.getAbsolutePath());
								bundle.putString(MESSAGE_FILE_THUMB_PATH, msg.getExtThumbPath());
								i.putExtras(bundle);
								activity.startActivity(i);
							} else if (item.equals(activity.getString(R.string.back))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"撤回");
								if (ApplicationUtils.isNetworkAvailable(activity)) {
									if (CanWithdraw(msg)) {
										if (msg.getStatus() == Status.STATUS_WAITING || msg.getStatus() == Status.STATUS_LOADING) {
											BaseToast.show(R.string.back_londing);
										} else if (msg.getStatus() == Status.STATUS_FAIL || msg.getStatus() == Status.STATUS_PAUSE) {
											BaseToast.show(R.string.back_defail);
										} else if (MessageUtils.checkWithdrawnCap()) {
											LogF.i(TAG, "bingle--msgID：" + msg.getId());
											presenter.sendWithdrawnMessage(msg);
											firstShowWithdrawDialog();
										}
									} else {
										showWithdrawDialog();
									}
								}else{
									BaseToast.show(R.string.charge_network_error);
								}
							} else if (item.equals(activity.getString(R.string.delete))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"删除");
								presenter.deleteMessage(msg);
							} else if (item.equals(activity.getString(R.string.collect))) {
								presenter.addToFavorite(msg, mChatType, address);
							}else if(item.equals(activity.getString(R.string.multi_select))){
								//多选模式
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"多选");
								((MessageDetailActivity) activity).changeMode(MessageDetailActivity.INTO_MULTI_SELECT_MODE);
								adapter.addSelection(position);
							}
						}
					});
					messageOprationDialog.show();
					break;
				case Type.TYPE_MSG_FILE_YUN_RECV:
					itemList = activity.getResources().getStringArray(R.array.msg_img_long_click);
					messageOprationDialog = new MessageOprationDialog(activity, null, itemList, null);
					messageOprationDialog.setOnMessageItemClickListener(new MessageOprationDialog.OnOprationItemClickListener() {
						@Override
						public void onClick(String item, int which, String address) {
							if (item.equals(activity.getString(R.string.forwarld))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"转发");
								if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
									UmengUtil.buryPoint(activity, "message_groupmessage_press_send", "转发", 0);
								} else {
									UmengUtil.buryPoint(activity, "message_p2pmessage_press_send", "转发", 0);
								}
								YunFile yunfile = YunFileXmlParser.parserYunFileXml(msg.getBody());
								if (yunfile == null) {
									return;
								}
								File file = new File(yunfile.getLocalPath());
								if (!file.exists()|| file.length()< yunfile.getFileSize()) {
									BaseToast.show(R.string.forward_file_before_download);
									return;
								}
//								Intent i = ContactsSelectActivity.createIntentForMessageForward(activity);
                                Intent i = ContactSelectorActivity.creatIntent(mContext,SOURCE_MESSAGE_FORWARD, 1);
								Bundle bundle = new Bundle();
								bundle.putInt(MESSAGE_TYPE, Type.TYPE_MSG_FILE_SEND);
								bundle.putString(MESSAGE_FILE_PATH, file.getAbsolutePath());
								bundle.putString(MESSAGE_FILE_THUMB_PATH, msg.getExtThumbPath());
								i.putExtras(bundle);
								activity.startActivity(i);
							} else if (item.equals(activity.getString(R.string.delete))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"删除");
								presenter.deleteMessage(msg);
							} else if (item.equals(activity.getString(R.string.collect))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"收藏");
								presenter.addToFavorite(msg, mChatType, address);
							} else if(item.equals(activity.getString(R.string.multi_select))){
								//多选模式
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"多选");
								((MessageDetailActivity) activity).changeMode(MessageDetailActivity.INTO_MULTI_SELECT_MODE);
								adapter.addSelection(position);
							}
						}
					});
					messageOprationDialog.show();
					break;
				//名片
				case Type.TYPE_MSG_CARD_RECV:
					if(msg != null && !TextUtils.isEmpty(msg.getBody())){
						itemList = activity.getResources().getStringArray(R.array.msg_cardrecv_long_click);// 操作选项
					}else{// 名片文件为null 时只有删除
						itemList = activity.getResources().getStringArray(R.array.msg_text_long_click_mms);// 操作选项
					}
					messageOprationDialog = new MessageOprationDialog(activity, null, itemList, null);
					messageOprationDialog.setOnMessageItemClickListener(new MessageOprationDialog.OnOprationItemClickListener() {
						@Override
						public void onClick(String item, int which, String address) {
							if (item.equals(activity.getString(R.string.forwarld))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"转发");
								if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
									UmengUtil.buryPoint(activity, "message_groupmessage_press_send", "转发", 0);
								} else {
									UmengUtil.buryPoint(activity, "message_p2pmessage_press_send", "转发", 0);
								}
//								Intent i = ContactsSelectActivity.createIntentForMessageForward(activity);
                                Intent i = ContactSelectorActivity.creatIntent(mContext,SOURCE_MESSAGE_FORWARD, 1);
								Bundle bundle = new Bundle();
								String extFilePath = msg.getExtFilePath();
								File file = null;
								try {
									file = new File(extFilePath);
								} catch (NullPointerException e) {
									e.printStackTrace();
								}
								if (file != null && file.exists()) {
									bundle.putString(MESSAGE_FILE_PATH, extFilePath);
								}
								bundle.putInt(MESSAGE_TYPE, Type.TYPE_MSG_CARD_SEND);
								bundle.putString(MESSAGE_BODY, msg.getBody());
								i.putExtras(bundle);
								activity.startActivity(i);
							} else if (item.equals(activity.getString(R.string.delete))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"删除");
								presenter.deleteMessage(msg);
							} else if (item.equals(activity.getString(R.string.collect))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"收藏");
								if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
									UmengUtil.buryPoint(activity, "message_groupmessage_press_favorites", "收藏", 0);
								} else {
									UmengUtil.buryPoint(activity, "message_p2pmessage_press_favorites", "收藏", 0);
								}
								presenter.addToFavorite(msg, mChatType, msg.getAddress());
							} else if(item.equals(activity.getString(R.string.multi_select))){
								//多选模式
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"多选");
								((MessageDetailActivity) activity).changeMode(MessageDetailActivity.INTO_MULTI_SELECT_MODE);
								adapter.addSelection(position);
							}
						}
					});
					messageOprationDialog.show();
					break;
				case Type.TYPE_MSG_CARD_SEND:
				case Type.TYPE_MSG_CARD_SEND_CCIND:
					if (boxType == Type.TYPE_BOX_PLATFORM || boxType == Type.TYPE_BOX_PLATFORM_DEFAULT ||TextUtils.isEmpty(msg.getExtFilePath()) || boxType == Type.TYPE_BOX_PC
							|| !CanWithdraw(msg)) {
						itemList = activity.getResources().getStringArray(R.array.msg_cardrecv_long_click);//不通过菊风sdk直接插入的没有撤回
					} else {
						itemList = activity.getResources().getStringArray(R.array.msg_cardsend_long_click);// 操作选项
					}

					messageOprationDialog = new MessageOprationDialog(activity, null, itemList, null);
					messageOprationDialog.setOnMessageItemClickListener(new MessageOprationDialog.OnOprationItemClickListener() {
						@Override
						public void onClick(String item, int which, String address) {
							if (item.equals(activity.getString(R.string.forwarld))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"转发");
								if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
									UmengUtil.buryPoint(activity, "message_groupmessage_press_send", "转发", 0);
								} else {
									UmengUtil.buryPoint(activity, "message_p2pmessage_press_send", "转发", 0);
								}
//								Intent i = ContactsSelectActivity.createIntentForMessageForward(activity);
                                Intent i = ContactSelectorActivity.creatIntent(mContext,SOURCE_MESSAGE_FORWARD, 1);
								Bundle bundle = new Bundle();
								String extFilePath = msg.getExtFilePath();

								if (extFilePath != null) {
									File file = new File(extFilePath);
									if (file.exists()) {
										bundle.putString(MESSAGE_FILE_PATH, extFilePath);
									}
								}

								bundle.putInt(MESSAGE_TYPE, Type.TYPE_MSG_CARD_SEND);
								bundle.putString(MESSAGE_BODY, msg.getBody());
								i.putExtras(bundle);
								activity.startActivity(i);
							} else if (item.equals(activity.getString(R.string.back))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"撤回");
								if (ApplicationUtils.isNetworkAvailable(activity)) {
									if (CanWithdraw(msg)) {
										if (msg.getStatus() == Status.STATUS_WAITING || msg.getStatus() == Status.STATUS_LOADING) {
											BaseToast.show(R.string.back_londing);
										} else if (msg.getStatus() == Status.STATUS_FAIL || msg.getStatus() == Status.STATUS_PAUSE) {
											BaseToast.show(R.string.back_defail);
										} else if (MessageUtils.checkWithdrawnCap()) {
											LogF.i(TAG, "bingle--msgID：" + msg.getId());
											presenter.sendWithdrawnMessage(msg);
											firstShowWithdrawDialog();
										}
									} else {
										showWithdrawDialog();
									}
								}else{
									BaseToast.show(R.string.charge_network_error);
								}
							} else if (item.equals(activity.getString(R.string.delete))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"删除");
								presenter.deleteMessage(msg);
							} else if (item.equals(activity.getString(R.string.collect))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"收藏");
								if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
									UmengUtil.buryPoint(activity, "message_groupmessage_press_favorites", "收藏", 0);
								} else {
									UmengUtil.buryPoint(activity, "message_p2pmessage_press_favorites", "收藏", 0);
								}
								presenter.addToFavorite(msg, mChatType, msg.getAddress());
							} else if (item.equals(activity.getString(R.string.multi_select))){
								//多选模式
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"多选");
								((MessageDetailActivity) activity).changeMode(MessageDetailActivity.INTO_MULTI_SELECT_MODE);
								adapter.addSelection(position);
							}
						}
					});
					messageOprationDialog.show();
					break;

				case Type.TYPE_MSG_OA_ONE_CARD_SEND:
				case Type.TYPE_MSG_OA_ONE_CARD_RECV:
					if (adapter.getIsPreMsg()) {
						itemList = activity.getResources().getStringArray(R.array.array_msg_public_oa_one_card_long_click);
					} else {
						itemList = activity.getResources().getStringArray(R.array.array_msg_oa__one_card_long_click);
					}
					messageOprationDialog = new MessageOprationDialog(activity, null, itemList, null);
					messageOprationDialog.setOnMessageItemClickListener(new MessageOprationDialog.OnOprationItemClickListener() {
						@Override
						public void onClick(String item, int which, String address) {
							switch (which) {
								//转发
								case 0:
                                    BuryingPointUtils.messageManipulationPuryingPoint(mContext,"转发");
//									Intent i = ContactsSelectActivity.createIntentForMessageForward(activity);
                                    Intent i = ContactSelectorActivity.creatIntent(mContext,SOURCE_MESSAGE_FORWARD, 1);
									Bundle bundle = new Bundle();
									bundle.putInt(MESSAGE_TYPE, Type.TYPE_MSG_OA_ONE_CARD_SEND);
									bundle.putString(MESSAGE_XML_CONTENT, msg.getXml_content());
									i.putExtras(bundle);
									activity.startActivity(i);
									break;
								//删除
								case 1:
                                    BuryingPointUtils.messageManipulationPuryingPoint(mContext,"删除");
									presenter.deleteMessage(msg);
									break;
								//多选
								case 2:
									//多选模式
                                    BuryingPointUtils.messageManipulationPuryingPoint(mContext,"多选");
									((MessageDetailActivity) activity).changeMode(MessageDetailActivity.INTO_MULTI_SELECT_MODE);
									adapter.addSelection(position);
									break;
								default:
									break;
							}
						}
					});
					messageOprationDialog.show();
					break;
				case Type.TYPE_MSG_DATE_ACTIVITY_SEND:
				case Type.TYPE_MSG_DATE_ACTIVITY_RECV:
					itemList = activity.getResources().getStringArray(R.array.array_msg_date_activity_long_click);
					messageOprationDialog = new MessageOprationDialog(activity, null, itemList, null);
					messageOprationDialog.setOnMessageItemClickListener(new MessageOprationDialog.OnOprationItemClickListener() {
						@Override
						public void onClick(String item, int which, String address) {
							switch (which) {
								case 0:
                                    BuryingPointUtils.messageManipulationPuryingPoint(mContext,"转发");
//									Intent i = ContactsSelectActivity.createIntentForMessageForward(activity);
                                    Intent i = ContactSelectorActivity.creatIntent(mContext,SOURCE_MESSAGE_FORWARD, 1);
									Bundle bundle = new Bundle();
									bundle.putInt(MESSAGE_TYPE, Type.TYPE_MSG_DATE_ACTIVITY_SEND);
									bundle.putString(MESSAGE_XML_CONTENT, msg.getXml_content());
									i.putExtras(bundle);
									activity.startActivity(i);
									break;
								case 1:
                                    BuryingPointUtils.messageManipulationPuryingPoint(mContext,"删除");
									presenter.deleteMessage(msg);
									break;
								//多选
								case 2:
									//多选模式
                                    BuryingPointUtils.messageManipulationPuryingPoint(mContext,"多选");
									((MessageDetailActivity) activity).changeMode(MessageDetailActivity.INTO_MULTI_SELECT_MODE);
									adapter.addSelection(position);
									break;
								default:
									break;
							}
						}
					});
					messageOprationDialog.show();
					break;
				case Type.TYPE_MSG_ENTERPRISE_SHARE_SEND:
				case Type.TYPE_MSG_ENTERPRISE_SHARE_RECV:
					itemList = activity.getResources().getStringArray(R.array.array_msg_ente_share_long_click);
					messageOprationDialog = new MessageOprationDialog(activity, null, itemList, null);
					messageOprationDialog.setOnMessageItemClickListener(new MessageOprationDialog.OnOprationItemClickListener() {
						@Override
						public void onClick(String item, int which, String address) {
							switch (which) {
								case 0:
                                    BuryingPointUtils.messageManipulationPuryingPoint(mContext,"转发");
//									Intent i = ContactsSelectActivity.createIntentForMessageForward(activity);
                                    Intent i = ContactSelectorActivity.creatIntent(mContext,SOURCE_MESSAGE_FORWARD, 1);
									Bundle bundle = new Bundle();
									bundle.putInt(MESSAGE_TYPE, Type.TYPE_MSG_ENTERPRISE_SHARE_SEND);
									bundle.putString(MESSAGE_XML_CONTENT, msg.getXml_content());
									i.putExtras(bundle);
									activity.startActivity(i);
									break;
								case 1:
                                    BuryingPointUtils.messageManipulationPuryingPoint(mContext,"删除");
									presenter.deleteMessage(msg);
									break;
								//多选
								case 2:
									//多选模式
                                    BuryingPointUtils.messageManipulationPuryingPoint(mContext,"多选");
									((MessageDetailActivity) activity).changeMode(MessageDetailActivity.INTO_MULTI_SELECT_MODE);
									adapter.addSelection(position);
									break;
								default:
									break;
							}
						}
					});
					messageOprationDialog.show();
					break;
				case Type.TYPE_MSG_T_CARD_RECV:
				case Type.TYPE_MSG_T_CARD_SEND:
				case Type.TYPE_MSG_T_CARD_SEND_CCIND:
					if (type == Type.TYPE_MSG_T_CARD_SEND || type == Type.TYPE_MSG_T_CARD_SEND_CCIND) {
						if (CanWithdraw(msg)) {
							itemList = activity.getResources().getStringArray(R.array.array_msg_oasend_long_click);
						} else {
							itemList = activity.getResources().getStringArray(R.array.array_msg_oa__one_card_long_click);
						}
					} else {
						itemList = activity.getResources().getStringArray(R.array.array_msg_t_card_long_click);
					}
					messageOprationDialog = new MessageOprationDialog(activity, null, itemList, null);
					messageOprationDialog.setOnMessageItemClickListener(new MessageOprationDialog.OnOprationItemClickListener() {
						@Override
						public void onClick(String item, int which, String address) {
							if (item.equals(activity.getString(R.string.forwarld))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"转发");
								if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
									UmengUtil.buryPoint(activity, "message_groupmessage_press_send", "转发", 0);
								} else {
									UmengUtil.buryPoint(activity, "message_p2pmessage_press_send", "转发", 0);
								}
//								Intent i = ContactsSelectActivity.createIntentForMessageForward(activity);
                                Intent i = ContactSelectorActivity.creatIntent(mContext,SOURCE_MESSAGE_FORWARD, 1);
								Bundle bundle = new Bundle();
								bundle.putInt(MESSAGE_TYPE, Type.TYPE_MSG_T_CARD_SEND);
								bundle.putString(MESSAGE_XML_CONTENT, msg.getXml_content());
								i.putExtras(bundle);
								activity.startActivity(i);
							} else if (item.equals(activity.getString(R.string.back))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"撤回");
								if (ApplicationUtils.isNetworkAvailable(activity)) {
									if (CanWithdraw(msg)) {
										if (msg.getStatus() == Status.STATUS_WAITING || msg.getStatus() == Status.STATUS_LOADING) {
											BaseToast.show(R.string.back_londing);
										} else if (msg.getStatus() == Status.STATUS_FAIL || msg.getStatus() == Status.STATUS_PAUSE) {
											BaseToast.show(R.string.back_defail);
										} else if (MessageUtils.checkWithdrawnCap()) {
											LogF.i(TAG, "bingle--msgID：" + msg.getId());
											presenter.sendWithdrawnMessage(msg);
											firstShowWithdrawDialog();
										}
									} else {
										showWithdrawDialog();
									}
								}else{
									BaseToast.show(R.string.charge_network_error);
								}
							} else if (item.equals(activity.getString(R.string.delete))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"删除");
								presenter.deleteMessage(msg);
							} else if (item.equals(activity.getString(R.string.collect))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"收藏");
								if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
									UmengUtil.buryPoint(activity, "message_groupmessage_press_favorites", "收藏", 0);
								} else {
									UmengUtil.buryPoint(activity, "message_p2pmessage_press_favorites", "收藏", 0);
								}
								presenter.addToFavorite(msg, mChatType, msg.getAddress());
							} else if(item.equals(activity.getString(R.string.multi_select))){
								//多选模式
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"多选");
								((MessageDetailActivity) activity).changeMode(MessageDetailActivity.INTO_MULTI_SELECT_MODE);
								adapter.addSelection(position);
							}
						}
					});
					messageOprationDialog.show();
					break;
				// 彩信
				case Type.TYPE_MSG_MMS_RECV:
					itemList = activity.getResources().getStringArray(R.array.msg_text_long_click_mms);
					messageOprationDialog = new MessageOprationDialog(activity, null, itemList, null);
					messageOprationDialog.setOnMessageItemClickListener(new MessageOprationDialog.OnOprationItemClickListener() {
						@Override
						public void onClick(String item, int which, String address) {
							if (item.equals(activity.getString(R.string.delete))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"删除");
								presenter.deleteMessage(msg);
							}else if(item.equals(activity.getString(R.string.multi_select))){
								//多选模式
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"多选");
								((MessageDetailActivity) activity).changeMode(MessageDetailActivity.INTO_MULTI_SELECT_MODE);
								adapter.addSelection(position);
							}
						}
					});
					messageOprationDialog.show();
					break;
				case Type.TYPE_MSG_MMS_SEND:
					itemList = activity.getResources().getStringArray(R.array.msg_text_long_click_mms);
					messageOprationDialog = new MessageOprationDialog(activity, null, itemList, null);
					messageOprationDialog.setOnMessageItemClickListener(new MessageOprationDialog.OnOprationItemClickListener() {
						@Override
						public void onClick(String item, int which, String address) {
							if (item.equals(activity.getString(R.string.delete))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"删除");
								presenter.deleteMessage(msg);
							}else if(item.equals(activity.getString(R.string.multi_select))){
								//多选模式
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"多选");
								((MessageDetailActivity) activity).changeMode(MessageDetailActivity.INTO_MULTI_SELECT_MODE);
								adapter.addSelection(position);
							}
						}
					});
					messageOprationDialog.show();
					break;
				case Type.TYPE_MSG_SINGLE_PIC_TEXT_RECV:
					if (adapter.getIsPreMsg()) {
						itemList = activity.getResources().getStringArray(R.array.array_msg_public_single_pic_text_long_click);
					} else {
						itemList = activity.getResources().getStringArray(R.array.array_msg_single_pic_text_long_click);
					}
					messageOprationDialog = new MessageOprationDialog(activity, null, itemList, null);
					messageOprationDialog.setOnMessageItemClickListener(new MessageOprationDialog.OnOprationItemClickListener() {
						@Override
						public void onClick(String item, int which, String address) {
							if (item.equals(activity.getString(R.string.forwarld))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"转发");
								if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
									UmengUtil.buryPoint(activity, "message_groupmessage_press_send", "转发", 0);
								} else {
									UmengUtil.buryPoint(activity, "message_p2pmessage_press_send", "转发", 0);
								}
//								Intent i = ContactsSelectActivity.createIntentForMessageForward(activity);
                                Intent i = ContactSelectorActivity.creatIntent(mContext,SOURCE_MESSAGE_FORWARD, 1);
								Bundle bundle = new Bundle();
								bundle.putInt(MESSAGE_TYPE, msg.getType());
								String xmlContent = EnterpriseShareUtil.createEnterpriseShareXML(activity, msg.getSubTitle(), msg.getSubTitle(), msg.getSubUrl(), msg.getSubImgPath());
								bundle.putString(MESSAGE_XML_CONTENT, xmlContent);
								i.putExtras(bundle);
								activity.startActivity(i);
							} else if (item.equals(activity.getString(R.string.delete))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"删除");
								presenter.deleteMessage(msg);
							} else if (item.equals(activity.getString(R.string.collect))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"收藏");
								if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
									UmengUtil.buryPoint(activity, "message_groupmessage_press_favorites", "收藏", 0);
								} else {
									UmengUtil.buryPoint(activity, "message_p2pmessage_press_favorites", "收藏", 0);
								}
								if (TextUtils.isEmpty(msg.getPerson()) && !TextUtils.isEmpty(adapter.getPublicAccountTitle()) && mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_PUBLICACCOUNT_CHAT) {
									msg.setPerson(adapter.getPublicAccountTitle());
								}
								presenter.addToFavorite(msg, mChatType, msg.getAddress());
							}
						}
					});
					messageOprationDialog.show();
					break;
				case Type.TYPE_MSG_MULIT_PIC_TEXT_RECV:
					int i = v.getId();
					int pos = 0;
					if (i == R.id.fl_complex_main) {
						pos = 0;
					} else if (i == R.id.pp_complex_1) {
						pos = 1;
					} else if (i == R.id.pp_complex_2) {
						pos = 2;
					} else if (i == R.id.pp_complex_3) {
						pos = 3;
					} else if (i == R.id.pp_complex_4) {
						pos = 4;
					} else if (i == R.id.pp_complex_5) {
						pos = 5;
					} else if (i == R.id.pp_complex_6) {
						pos = 6;
					} else if (i == R.id.pp_complex_7) {
						pos = 7;
					} else if (i == R.id.pp_complex_8) {
						pos = 8;
					}

					final int finalpos = pos;

					final String[] title = TextUtils.split(msg.getSubTitle(), ",");
					final String[] url = TextUtils.split(msg.getSubUrl(), ",");
					final String[] imgpath = TextUtils.split(msg.getSubImgPath(), ",");

					final String xmlContent = EnterpriseShareUtil.createEnterpriseShareXML(activity, title[finalpos], url[finalpos], url[finalpos], imgpath[finalpos]);

					itemList = activity.getResources().getStringArray(R.array.array_msg_public_long_click);
					messageOprationDialog = new MessageOprationDialog(activity, null, itemList, null);
					messageOprationDialog.setOnMessageItemClickListener(new MessageOprationDialog.OnOprationItemClickListener() {
						@Override
						public void onClick(String item, int which, String address) {
							if (item.equals(activity.getString(R.string.forwarld))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"转发");
								if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
									UmengUtil.buryPoint(activity, "message_groupmessage_press_send", "转发", 0);
								} else {
									UmengUtil.buryPoint(activity, "message_p2pmessage_press_send", "转发", 0);
								}
//								Intent i = ContactsSelectActivity.createIntentForMessageForward(activity);
                                Intent i = ContactSelectorActivity.creatIntent(mContext,SOURCE_MESSAGE_FORWARD, 1);
								Bundle bundle = new Bundle();
								bundle.putInt(MESSAGE_TYPE, msg.getType());
								bundle.putString(MESSAGE_XML_CONTENT, xmlContent);
								i.putExtras(bundle);
								activity.startActivity(i);
							} else if (item.equals(activity.getString(R.string.delete))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"删除");
								presenter.deleteMessage(msg);
							} else if (item.equals(activity.getString(R.string.collect))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"收藏");
								if (mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT) {
									UmengUtil.buryPoint(activity, "message_groupmessage_press_favorites", "收藏", 0);
								} else {
									UmengUtil.buryPoint(activity, "message_p2pmessage_press_favorites", "收藏", 0);
								}
								Message m = new Message();
								msg.copyTo(m);
								m.setSubTitle(title[finalpos]);
								m.setSubUrl(url[finalpos]);
								m.setSubImgPath(imgpath[finalpos]);
								m.setXml_content(xmlContent);
								m.setSubOriginUrl(url[finalpos]);
								m.setBody(title[finalpos]);
								m.setSubBody(title[finalpos]);
								m.setType(Type.TYPE_MSG_T_CARD_SEND);
								if (TextUtils.isEmpty(m.getPerson()) && !TextUtils.isEmpty(adapter.getPublicAccountTitle()) && mChatType == MessageModuleConst.MessageChatListAdapter.TYPE_PUBLICACCOUNT_CHAT) {
									m.setPerson(adapter.getPublicAccountTitle());
								}
								presenter.addToFavorite(m, mChatType, msg.getAddress());
							}
						}
					});
					messageOprationDialog.show();
					break;

				case Type.TYPE_MSG_LOC_RECV:
					itemList = activity.getResources().getStringArray(R.array.msg_loc_rcv_long_click);// 操作选项
					messageOprationDialog = new MessageOprationDialog(activity, null, itemList, null);
					messageOprationDialog.setOnMessageItemClickListener(new MessageOprationDialog.OnOprationItemClickListener() {
						@Override
						public void onClick(String item, int which, String address) {
							if (item.equals(activity.getString(R.string.delete))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"删除");
								presenter.deleteMessage(msg);
							} else if (item.equals(activity.getString(R.string.collect))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"收藏");
								presenter.addToFavorite(msg, mChatType, msg.getAddress());
							} else if (item.equals(activity.getString(R.string.forwarld))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"转发");
//								Intent i = ContactsSelectActivity.createIntentForMessageForward(activity);
                                Intent i = ContactSelectorActivity.creatIntent(mContext,SOURCE_MESSAGE_FORWARD, 1);
								Bundle bundle = new Bundle();
								bundle.putInt(MESSAGE_TYPE, Type.TYPE_MSG_LOC_RECV);
								bundle.putString(MESSAGE_BODY, msg.getBody());
								bundle.putString(MESSAGE_XML_CONTENT, msg.getXml_content());
								i.putExtras(bundle);
								activity.startActivity(i);
							} else if(item.equals(activity.getString(R.string.multi_select))){
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"多选");
								//多选模式
								((MessageDetailActivity) activity).changeMode(MessageDetailActivity.INTO_MULTI_SELECT_MODE);
								adapter.addSelection(position);
							}
						}
					});
					messageOprationDialog.show();
					break;
				case Type.TYPE_MSG_LOC_SEND:
				case Type.TYPE_MSG_LOC_SEND_CCIND:
					if (CanWithdraw(msg) && boxType != Type.TYPE_BOX_PC) {
						itemList = activity.getResources().getStringArray(R.array.msg_loc_send_long_click);// 操作选项
					} else {
						itemList = activity.getResources().getStringArray(R.array.msg_loc_rcv_long_click);
					}
					messageOprationDialog = new MessageOprationDialog(activity, null, itemList, null);
					messageOprationDialog.setOnMessageItemClickListener(new MessageOprationDialog.OnOprationItemClickListener() {
						@Override
						public void onClick(String item, int which, String address) {
							if (item.equals(activity.getString(R.string.delete))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"删除");
								presenter.deleteMessage(msg);
							} else if (item.equals(activity.getString(R.string.back))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"撤回");
								if (ApplicationUtils.isNetworkAvailable(activity)) {
									if (CanWithdraw(msg)) {
										if (msg.getStatus() == Status.STATUS_WAITING || msg.getStatus() == Status.STATUS_LOADING) {
											BaseToast.show(R.string.back_londing);
										} else if (msg.getStatus() == Status.STATUS_FAIL || msg.getStatus() == Status.STATUS_PAUSE) {
											BaseToast.show(R.string.back_defail);
										} else if (MessageUtils.checkWithdrawnCap()) {
											LogF.i(TAG, "bingle--msgID：" + msg.getId());
											presenter.sendWithdrawnMessage(msg);
											firstShowWithdrawDialog();
										} else {
										}
									} else {
										showWithdrawDialog();
									}
								}else{
									BaseToast.show(R.string.charge_network_error);
								}
							} else if (item.equals(activity.getString(R.string.collect))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"收藏");
								presenter.addToFavorite(msg, mChatType, msg.getAddress());
							} else if (item.equals(activity.getString(R.string.forwarld))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"转发");
//								Intent i = ContactsSelectActivity.createIntentForMessageForward(activity);
                                Intent i = ContactSelectorActivity.creatIntent(mContext,SOURCE_MESSAGE_FORWARD, 1);
								Bundle bundle = new Bundle();
								bundle.putString(MESSAGE_BODY, msg.getBody());
								bundle.putInt(MESSAGE_TYPE, Type.TYPE_MSG_LOC_SEND);
								bundle.putString(MESSAGE_XML_CONTENT, msg.getXml_content());
								i.putExtras(bundle);
								activity.startActivity(i);
							} else if (item.equals(activity.getString(R.string.multi_select))){
								//多选模式
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"多选");
								((MessageDetailActivity) activity).changeMode(MessageDetailActivity.INTO_MULTI_SELECT_MODE);
								adapter.addSelection(position);
							}
						}
					});
					messageOprationDialog.show();
					break;

				case Type.TYPE_MSG_BAG_SEND:
				case Type.TYPE_MSG_CASH_BAG_SEND:
				case Type.TYPE_MSG_BAG_RECV:
				case Type.TYPE_MSG_CASH_BAG_RECV:
					itemList = new String[] {activity.getString(R.string.delete)};
					messageOprationDialog = new MessageOprationDialog(activity, null, itemList, null);
					messageOprationDialog.setOnMessageItemClickListener(new MessageOprationDialog.OnOprationItemClickListener() {
						@Override
						public void onClick(String item, int which, String address) {
							if (item.equals(activity.getString(R.string.delete))) {
                                BuryingPointUtils.messageManipulationPuryingPoint(mContext,"删除");
								presenter.deleteMessage(msg);
							}
						}
					});
					messageOprationDialog.show();
					break;

				default:
					break;
			}
			return true;
		}
	}

    /**
     * 是否可以撤回
     * @param msg
     * @return
     */
	private boolean CanWithdraw(Message msg) {
		return msg != null && msg.getStatus() == Status.STATUS_OK && TimeUtil.getCurrentTime() - msg.getDate() <= TEN_MINUTES;
	}

	private void firstShowWithdrawDialog() {
		boolean isFirstShow = (boolean) SharePreferenceUtils.getDBParam(mContext, "first_show_withdraw_dialog", true);
		if (isFirstShow) {
			final IKnowDialog dialog = new IKnowDialog(mContext, activity.getString(R.string.withdraw_out_of_time),
					activity.getString(R.string.i_know));
			dialog.setCanceledOnTouchOutside(false);
			dialog.mBtnOk.setTextColor(0xFF4184F3);
			dialog.show();
			dialog.setOnSureClickListener(new IKnowDialog.OnSureClickListener() {
				@Override
				public void onClick() {
					SharePreferenceUtils.setDBParam(mContext, "first_show_withdraw_dialog", false);
					dialog.dismiss();
				}
			});
		}
	}

	private void showWithdrawDialog() {
		final IKnowDialog dialog = new IKnowDialog(mContext, activity.getString(R.string.withdraw_out_of_time),
				activity.getString(R.string.i_know));
		dialog.mBtnOk.setTextColor(0xFF4184F3);
		dialog.show();
		dialog.setOnSureClickListener(new IKnowDialog.OnSureClickListener() {
			@Override
			public void onClick() {
				dialog.dismiss();
			}
		});
	}

    /**
     * ViewHolder与message 一一对应。
     * @param m
     */
	public void setMessage(Message m){
	    this.mMessage = m;
    }


    /**
     * 当前主题的一些颜色
     * @param colors
     */
    public void setColors(int [] colors){
        leftColorId      = colors[0];
        rightColorId     = colors[1];
        leftTextColor    = colors[2];
        rightTextColor   = colors[3];
        nameTextColor    = colors[4];
        sysTextBackColor = colors[5];
    }

    /**
     *
     * @param isGroupChat
     * @param isEPGroup
     * @param isPartyGroup
     * @param chatType
     */
    public void setChatArgs(boolean isGroupChat, boolean isEPGroup, boolean isPartyGroup, int chatType){
        this.isEPGroup = isEPGroup;
        this.isGroupChat = isGroupChat;
        this.isPartyGroup = isPartyGroup;
        this.mChatType = chatType;
    }

	//绑定消息时间
	public void bindTime(Message msgBefore, int position) { }

	public void bindMultiSelectStatus(boolean isMultiSelectMode, boolean isSelected){}



}
