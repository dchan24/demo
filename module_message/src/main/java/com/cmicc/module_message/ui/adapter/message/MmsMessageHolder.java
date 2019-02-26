package com.cmicc.module_message.ui.adapter.message;

import android.app.Activity;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chinamobile.app.utils.RxAsyncHelper;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_business.provider.Conversations;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.utils.Log;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.cmicc.module_message.ui.adapter.MessageChatListAdapter;
import com.cmicc.module_message.ui.constract.BaseChatContract;
import com.cmcc.cmrcs.android.ui.utils.ThumbnailUtils;
import com.cmcc.cmrcs.android.widget.RecycleViewConstraintLayout;
import com.cmicc.module_message.R;

import java.util.HashMap;

import cn.com.mms.jar.pdu.EncodedStringValue;
import cn.com.mms.jar.pdu.MultimediaMessagePdu;
import cn.com.mms.jar.pdu.PduBody;
import cn.com.mms.jar.pdu.PduHeaders;
import cn.com.mms.jar.pdu.PduPart;
import cn.com.mms.jar.pdu.PduPersister;
import cn.com.mms.utils.MmsUtils;
import rx.functions.Func1;

public class MmsMessageHolder extends BaseViewHolder {

	private static final String TAG = MmsMessageHolder.class.getName();
	public LinearLayout sllMsg;

	public ImageView sIvFileIcon;

	public TextView sTvMmsTheme;

	public TextView sTvMmsSummary;

	public LinearLayout llSmsMark;

	public CheckBox multiCheckBox;

	public MmsMessageHolder(View itemView, Activity activity , MessageChatListAdapter adapter , BaseChatContract.Presenter presenter) {
		super(itemView ,activity ,adapter ,presenter);
		sllMsg = (LinearLayout) itemView.findViewById(R.id.ll_msg);
		sIvFileIcon = (ImageView) itemView.findViewById(R.id.iv_file_icon);
		sTvMmsTheme = (TextView) itemView.findViewById(R.id.textview_mms_theme);
		sTvMmsSummary = (TextView) itemView.findViewById(R.id.textview_mms_summary);
		llSmsMark = (LinearLayout) itemView.findViewById(R.id.ll_sms_mark);
		multiCheckBox = itemView.findViewById(R.id.multi_check);

		sllMsg.setOnClickListener(new NoDoubleClickListener());
		sllMsg.setOnLongClickListener(new OnMsgContentLongClickListener());

	}

	@Override
	public void bindMultiSelectStatus(boolean isMultiSelectMode , boolean isSelected){
		((RecycleViewConstraintLayout)itemView).setMode(isMultiSelectMode);
		if(isMultiSelectMode){
			//头像不显示，以消息气泡上下居中
			ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)multiCheckBox.getLayoutParams();
			int type = mMessage.getType();
			switch (type){
				case Type.TYPE_MSG_MMS_SEND:
					params.topToTop = R.id.lltContent;
					params.bottomToBottom = R.id.lltContent;

					break;
				case Type.TYPE_MSG_MMS_RECV:
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

	public void parseMmsContent(final int id) {
		sIvFileIcon.setTag(id);
		sTvMmsSummary.setTag(id);

		RxAsyncHelper helper = new RxAsyncHelper("");
		helper.runInThread(new Func1<Object, HashMap<String, Object>>() {
			@Override
			public HashMap<String, Object> call(Object o) {
				long debugTime;
				debugTime = SystemClock.currentThreadTimeMillis();

				HashMap<String, Object> map = new HashMap<String, Object>();
				Bitmap bmp = null;
				String s = "";
				PduPersister persister = PduPersister.getPduPersister(mContext.getApplicationContext());

				Uri mmsUri = ContentUris.withAppendedId(Conversations.SMS.CONTENT_URI_MMS, id);
				MultimediaMessagePdu pdu = null;
				try {
					pdu = (MultimediaMessagePdu) persister.load(mmsUri);
				} catch (Exception e) {
					LogF.e(TAG, "解析彩信出现异常，" + e.getMessage());
				}
				if (pdu != null) {
					PduBody body = pdu.getBody();
					int partsNum = body.getPartsNum();
					String fileNameRecv = null;
					boolean hasSetMediaContent = false;// 是否设置了图片视频音频等的内容，包括任意一项即为true
					boolean hasSetTextContent = false;// 是否设置了文字内容
					for (int i = 0; i < partsNum; i++) {
						if (hasSetMediaContent && hasSetTextContent) {
							break;
						}
						PduPart part = body.getPart(i);
						String contentType = new String(part.getContentType());
						LogF.e(TAG, "contentType=" + contentType);
						if (MmsUtils.isMmsSmilType(contentType)) {
							continue;
						}
//            fileNameRecv = new String(part.getContentLocation());
						if (MmsUtils.isMmsImageType(contentType) || MmsUtils.isMmsVideoType(contentType)) {
							if (hasSetMediaContent) {
								continue;
							}

							Uri resourceUri = part.getDataUri();
							ViewGroup.LayoutParams params = sIvFileIcon.getLayoutParams();
							if (MmsUtils.isMmsImageType(contentType)) {
								bmp = MmsUtils.getMmsImage(resourceUri, mContext, true, params.height, params.width);
								if (bmp == null) {
									// htc m8et 彩信转发后part的datauri变为和转发后的一样 ，转发的彩信删除后 原彩信无法正确展示
									// 重新查询原彩信的datauri 手机model为unkow
									// String model = Build.MODEL.toLowerCase();
									Cursor c = mContext.getContentResolver().query(Uri.parse("content://mms/" + id + "/part"), new String[]{"_id", "ct"}, null, null, "seq desc");
									if (c != null) {
										try {
											while (c.moveToNext()) {
												String ct = c.getString(1);
												if (ct.contains("image")) {
													int partId = c.getInt(0);
													bmp = MmsUtils.getMmsImage(Uri.parse("content://mms/part/" + partId), mContext, true, params.height, params.width);
													break;
												} else {
													continue;
												}
											}
										} finally {
											c.close();
										}
									}
								}
							} else {
								bmp = ThumbnailUtils.createVideoThumbnail(mContext, resourceUri, MediaStore.Video.Thumbnails.MINI_KIND);
							}

							hasSetMediaContent = true;
						} else if (MmsUtils.isMmsAudioType(contentType)) {

							if (hasSetMediaContent) {
								continue;
							}

							bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.message_files_icon_content_music);
							String fileName = new String(part.getContentLocation());
							//                holder.sTvMmsSummary.setText(fileName);
							LogF.e(TAG, "fileName=" + fileName);
							hasSetMediaContent = true;
						} else if (MmsUtils.isMmsTextType(contentType)) {
							if (hasSetTextContent) {
								continue;
							}
							if (part.getData() != null) {
								s = new String(part.getData());
							} else {
								Uri dataUri = part.getDataUri();
								MmsUtils.getMmsText(dataUri, mContext);
							}
							hasSetTextContent = true;
						} else if (MmsUtils.isMmsSysOggType(contentType)) {
							if (hasSetMediaContent) {
								continue;
							}

							bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.message_files_icon_content_music);

							String fileName = new String(part.getContentLocation());
							//                holder.sTvMmsSummary.setText(fileName);
							LogF.e(TAG, "fileName=" + fileName);
							hasSetMediaContent = true;
						}
					}
					if (!hasSetMediaContent) {
						//            holder.ivBanner.setVisibility(View.GONE);
					}
					if (!hasSetTextContent) {
						//            holder.etvSummary.setVisibility(View.GONE);
					}
				}
				long tt = SystemClock.currentThreadTimeMillis() - debugTime;
				if (tt > 100) {
					LogF.e(TAG, "!! parseMmsContent use too much time:\n id=" + id + " useTime=" + tt + " ms");
				}

				if (bmp == null) {
					bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.message_files_icon_content_unknown);
				}
				map.put("bmp", bmp);
				map.put("part", s);
				return map;
			}
		}).runOnMainThread(new Func1<HashMap<String, Object>, Object>() {
			@Override
			public Object call(HashMap<String, Object> map) {
				Bitmap bmp = (Bitmap) map.get("bmp");
				String part = (String) map.get("part");

				if ((int) sTvMmsSummary.getTag() == id && (int) sIvFileIcon.getTag() == id) {
					sTvMmsSummary.setText(part);
					sIvFileIcon.setImageBitmap(bmp);
				}
				return null;
			}
		}).subscribe();
	}

	public void bindText(){
		Message msg = mMessage;
		llSmsMark.setVisibility(View.VISIBLE);
		// 获取彩信的下载状态 大小 到期 日期
		// MMS协议定义的彩信类型，其中send-req为128、notification-ind为130、retrieve-conf为132
		int mType = 0;
		// 该彩信的下载状态，未启动-128，下载中-129，传输失败-130，保存失败-135
		int st = 0;
		try {
			mType = Integer.parseInt(msg.getExtTitle());
			st = Integer.parseInt(msg.getExtFileName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (mType == PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND) {
			if (st == 128) {
				Log.i(TAG, "彩信:" + "点击下载");
			} else if (st == 129) {
				Log.i(TAG, "彩信:" + "下载中");
			}
		} else {
			// 展示彩信消息体
			String sub = msg.getBody();
			// 彩信的body查询的字段为其主题： sub
			if (!TextUtils.isEmpty(sub)) {
				EncodedStringValue v = new EncodedStringValue(106, PduPersister.getBytes(sub));
				if (MmsUtils.isGarbled(v.getString())) {
					sub = MmsUtils.getStringOfGarbled(sub, 6);
				} else {
					sub = v.getString();
				}
				sub = mContext.getString(R.string.title) + sub;
			} else {
				sub = mContext.getString(R.string.no_subject_);
			}
			sTvMmsTheme.setText(sub);

			LogF.i(TAG, "sub:" + sub);
			// 开始解析彩信内容
			int mid;
			if (TextUtils.isEmpty((msg.getXml_content()))) {
				if (!TextUtils.isEmpty(msg.getMsgId())) {
					mid = Integer.parseInt(msg.getMsgId().substring(msg.getMsgId().indexOf("-") + 1));
				} else {
					mid = -1;
				}
			} else {
				String content = msg.getXml_content();
				mid = Integer.parseInt(content.substring(content.indexOf("-") + 1));
			}
			LogF.i(TAG, "mid:" + mid);
			parseMmsContent(mid);
		}
	}
}
