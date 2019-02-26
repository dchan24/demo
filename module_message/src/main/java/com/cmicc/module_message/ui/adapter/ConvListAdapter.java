package com.cmicc.module_message.ui.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.ArrayMap;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.module.proxys.modulecall.CallProxy;
import com.app.module.proxys.modulecontact.ContactProxy;
import com.app.util.StrangerEnterpriseUtil;
import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.utils.CommonConstant;
import com.chinamobile.app.utils.RxAsyncHelper;
import com.chinamobile.app.utils.StringUtil;
import com.chinamobile.app.utils.TimeUtilsForMultiLan;
import com.chinamobile.app.yuliao_business.model.BaseModel;
import com.chinamobile.app.yuliao_business.model.Conversation;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_business.model.OAList;
import com.chinamobile.app.yuliao_business.util.ConversationUtils;
import com.chinamobile.app.yuliao_business.util.GroupChatUtils;
import com.chinamobile.app.yuliao_business.util.OAUtils;
import com.chinamobile.app.yuliao_business.util.Status;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.application.MyApplication;
import com.chinamobile.app.yuliao_common.utils.FontUtil;
import com.chinamobile.app.yuliao_common.utils.Log;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_common.utils.SystemUtil;
import com.chinamobile.precall.common.OnInComingCallShowListener;
import com.chinamobile.precall.entity.InComingCallInfoEntity;
import com.cmcc.cmrcs.android.contact.data.ContactsCache;
import com.cmcc.cmrcs.android.glide.GlidePhotoLoader;
import com.cmcc.cmrcs.android.ui.activities.HomeActivity;
import com.cmcc.cmrcs.android.ui.control.ComposeMessageActivityControl;
import com.cmcc.cmrcs.android.ui.utils.ConvCache;
import com.cmcc.cmrcs.android.ui.utils.ConvCache.CacheType;
import com.cmcc.cmrcs.android.ui.utils.IPCUtils;
import com.cmcc.cmrcs.android.ui.utils.LoginUtils;
import com.cmcc.cmrcs.android.ui.utils.MailOAUtils;
import com.cmcc.cmrcs.android.ui.utils.NickNameUtils;
import com.cmcc.cmrcs.android.ui.utils.UmengUtil;
import com.cmcc.cmrcs.android.ui.view.dragbubble.DragBubbleView;
import com.cmcc.cmrcs.android.ui.view.dragbubble.RoundNumber;
import com.cmcc.cmrcs.android.widget.emoji.EmojiParser;
import com.cmcc.cmrcs.android.widget.emoji.EmojiTextView4Convlist;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.activity.NotifySmsActivity;
import com.juphoon.cmcc.app.lemon.MtcImConstants;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.com.mms.jar.pdu.EncodedStringValue;
import cn.com.mms.jar.pdu.PduPersister;
import cn.com.mms.utils.MmsUtils;
import rx.functions.Func1;

import static com.chinamobile.app.yuliao_common.utils.FontUtil.getFontScale;


/**
 * Created by tigger on 2017/3/15.
 */

public class ConvListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "ConvListAdapter";
    public interface OnRecyclerViewItemClickListener {

        void onItemClick(View view, int position);

        boolean onItemLongCLickListener(View v, int position);

    }

    public interface OnPcOnlieItemClickListener {

        void onPcOnlineClick();

    }

    public interface OnCheckChangeListener{

        void onCheckChange(int selectedCount);

    }

    private static final int TYPE_VIEW_SEARCH = 0;
    private static final int TYPE_VIEW_TIP = 1;
    private static final int TYPE_VIEW_PC_ONLINE = 2;
    private static final int TYPE_VIEW_CONV = 3;
    private static final int TYPE_VIEW_CALLING = 4;

    private Context mContext;

    private int mContentTextMaxWidth;
    private float CON_NAME_FONT_SIZE = 18f;
    private float TV_CONTENT_FONT_SIZE = 14f;
    private float TV_DATE_FONT_SIZE = 12f;
    private float TV_UNREAD_FONT_SIZE = 9.0f;
    private float TV_UNREAD_RADIUS_SIZE = 8;
    private float TV_CONTENT_NAME_MAX_EMS = 11;
    private float SV_HEAD_VIEW_SIZE = SystemUtil.dip2px(50);
    private float ROOT_VIEW_SIZE = SystemUtil.dip2px(80);
    private DisplayImageOptions options;

    /*通话入口数据*/
    private boolean isVoice;
    private String callingViewText;
    private long callTime=0;
    private boolean updateChronometer = true;

    private String mClickConvAddress = "";//优化未读数更新显示：记录点击进入的会话，在返回到会话列表时，将未读数致为0

    public static final ConvCache mCache = ConvCache.getInstance();
    protected OnRecyclerViewItemClickListener mOnRecyclerViewItemClickListener;
    protected OnPcOnlieItemClickListener mOnPcOnlieItemClickListener;
    protected OnCheckChangeListener mOnCheckChangeListener;
    private CacheType mCacheType;

    private boolean hasSearchView = false;//是否有搜索框
//    private boolean hasTipView = false;//是否有提示框（网络错误提示框或者通知消息提示框）
    private boolean hasPCOnLineView = false;//是否有PC在线框
    private boolean hasCallingView = false;//是否有通话入口
    private boolean netError = false;//网络错误提示框
    private boolean showNotificationNoticeView = false;//通知消息提示框
    private TipViewCreator mTipViewCreator = TipViewCreator.DEFAULT;

    private boolean isMultiDelMode ;//是否批量删除模式
    private SparseBooleanArray selectedList = new SparseBooleanArray();

    public void setHasSearchView(boolean hasSearchView) {
        this.hasSearchView = hasSearchView;
    }

//    public void setHasTipView(boolean hasTipView) {
//        setHasTipView(hasTipView, TipViewCreator.DEFAULT);
//    }

    public void showCallingView(boolean isVoice, String text, long base) {
        hasCallingView = true;
        this.isVoice = isVoice;
        callingViewText = text;
        callTime = base;
        notifyDataSetChanged();
    }

    public void removeCallingView() {
        updateChronometer = true;
        hasCallingView = false;
        notifyDataSetChanged();
    }

    public void updateCallingView(boolean isVoice, String text, long base) {
        this.isVoice = isVoice;
        callingViewText = text;
        callTime = base;

//        notifyItemChanged(getCallViewPosition());
//        notifyDataSetChanged();
    }

    public boolean isShowCallingView() {
        return hasCallingView;
    }

    public void setNetErrorView(boolean isShow) {
        netError = isShow;
        setHasTipView(TipViewCreator.DEFAULT);
    }

    public void setNotificationNoticeView(boolean isShow, TipViewCreator tipViewCreator) {
        showNotificationNoticeView = isShow;
        if (isShow) {
            setHasTipView(tipViewCreator);
        }else {
            notifyDataSetChanged();
        }
    }
    private void setHasTipView(TipViewCreator tipViewCreator){
        this.mTipViewCreator = tipViewCreator;
        notifyDataSetChanged();
    }

    public interface TipViewCreator{
        @LayoutRes int layoutRes();
        void onCreateView(View view);

        TipViewCreator DEFAULT = new Default();

        public class Default implements TipViewCreator{

            @Override
            public int layoutRes() {
                return R.layout.item_net_error;
            }

            @Override
            public void onCreateView(View view) {

            }
        }
    }

    public boolean hasTipView() {
        return netError||showNotificationNoticeView;
    }

    public void setHasPCOnLineView(boolean hasPCOnLineView) {
        this.hasPCOnLineView = hasPCOnLineView;
    }

    public ConvListAdapter(Context context, CacheType type) {
        mContext = context;
        mCacheType = type;
        // 默认 holder.sTvContent组件最大宽度为240dp
        mContentTextMaxWidth = (int) AndroidUtil.dip2px(context, 240);

        options = new DisplayImageOptions.Builder().showImageOnFail(R.drawable.message_msglist_head_public)
                // 加载出错时显示错误的默认图片
                .cacheInMemory(true)
                // 开启内存缓存
                .cacheOnDisc(true)
                // 开启硬盘缓存
                .bitmapConfig(Bitmap.Config.RGB_565)
                // 图片色值配置
                .imageScaleType(ImageScaleType.EXACTLY).displayer(new RoundedBitmapDisplayer((int) AndroidUtil.dip2px(mContext, 25))).build();

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_VIEW_SEARCH) {//搜索框
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_conv_search, parent, false);
            SearchViewHolder searchViewHolder = new SearchViewHolder(view);
            return searchViewHolder;
        } else if (viewType == TYPE_VIEW_TIP) {//网络错误提示框或者通知消息提示框
            FrameLayout layout = new FrameLayout(mContext);
            layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            NetErrorViewHolder netErrorViewHolder = new NetErrorViewHolder(layout);
            return netErrorViewHolder;
        } else if (viewType == TYPE_VIEW_PC_ONLINE) {//PC在线框
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_pc_online, parent, false);
            PCOnlineViewHolder pcOnlineViewHolder = new PCOnlineViewHolder(view);
            return pcOnlineViewHolder;
        } else if (viewType == TYPE_VIEW_CALLING) {//通话入口
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_calling, parent, false);
            CallingViewHolder callingViewHolder = new CallingViewHolder(view);
            return callingViewHolder;
        } else {//会话
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_conv_list, parent, false);
            ViewHolder v = new ViewHolder(view);
            return v;
        }

    }

    public int getExtraViewNum() {
        int extraViewNum = 0;
        if (hasSearchView) {
            extraViewNum++;
        }
        if (netError){
            extraViewNum++;
            return extraViewNum;
        }
        if (hasCallingView) {
            extraViewNum++;
        }else if (showNotificationNoticeView){
            extraViewNum++;
        }

        if (hasPCOnLineView) {
            extraViewNum++;
        }
        return extraViewNum;
    }

    @Override
    public int getItemViewType(int position) {
        int [] extra = new int[3];//第一位为搜索，第二位为TipView，第三位为PCOnLineView
        extra[0] = hasSearchView? 1 : 0;
        extra[1] = hasTipView()||hasCallingView? 1 : 0;
        extra[2] = (!netError)&&hasPCOnLineView? 1 : 0;

        if (hasSearchView && position == 0) {
            return TYPE_VIEW_SEARCH;
        }

        if ((netError||((!hasCallingView)&&showNotificationNoticeView))&& position == extra[0]) {
            return TYPE_VIEW_TIP;
        }

        if ((!netError)&&hasCallingView&& position == extra[0]) {
            return TYPE_VIEW_CALLING;
        }

        if ((!netError)&&hasPCOnLineView && position == extra[0] + extra[1]) {
            return TYPE_VIEW_PC_ONLINE;
        }

        return TYPE_VIEW_CONV;
    }

    @Override
    public long getItemId(int position) {
        int extraViewNum = getExtraViewNum();

        if (hasSearchView && position == 0) {
            return 0 << 3;
        }

        if (hasTipView()) {
            if (extraViewNum == 1 && position == 0) {
                return mTipViewCreator.hashCode();
            }
            if (extraViewNum > 1 && position == 1) {
                return mTipViewCreator.hashCode();
            }
        }

        if (hasPCOnLineView) {
            if (extraViewNum == 1 && position == 0) {
                return 0 << 3 + 5;
            }
            if (extraViewNum >= 2 && position == 1) {
                return 0 << 3 + 5;
            }
            if (extraViewNum > 2 && position == 2) {
                return 0 << 3 + 5;
            }
        }

        if (hasCallingView) {
            if (extraViewNum == 1 && position == 0) {
                return 0;
            }
            if (extraViewNum >= 2 && position == 1) {
                return 0;
            }
            if (extraViewNum >= 3 && position == 2) {
                return 0;
            }
            if (extraViewNum > 3 && position == 3) {
                return 0;
            }
        }

        position = position - extraViewNum;
        Conversation item = getDataList().get(position);
        if (item.getBoxType() == Type.TYPE_BOX_PLATFORM || item.getBoxType() == Type.TYPE_BOX_PLATFORM_DEFAULT) {
            return item.getId() << 3 + 2;
        } else if (item.getBoxType() == Type.TYPE_BOX_NOTIFY) {
            return item.getId() << 3 + 3;
        }
        return item.getId() << 3 + 1;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == TYPE_VIEW_CONV) {
            ViewHolder holder = (ViewHolder) viewHolder;
            Conversation conv = getItem(position);
//            LogF.d(TAG, "Conversation: " + conv != null ?conv.toString():"");
            if (conv == null) {
                Log.e(TAG, "conv is null : position=" + position + ", dataSize=" + getItemCount());
                return;
            }

            if (holder.mCurrentFontScale != mFontScale) {
                reSetTextSize(holder);
                holder.mCurrentFontScale = mFontScale;
            }
            holder.mRlConvListItem.setBackgroundResource(conv.getTopDate() > 0 ? R.drawable.contact_item_selector_top : R.drawable.contact_item_selector);
            bindName(holder, conv);
            bindHead(holder, conv);
            bindContent(holder, conv);
            holder.sTvDate.setText(TimeUtilsForMultiLan.formatTime(mContext,conv.getDate()));
            bindUnreadCount(holder, conv, position);

            if(isMultiDelMode){
                holder.checkBox.setVisibility(View.VISIBLE);
//                int size = selectedList.size();
//                LogF.d(TAG,"selectedList = " + size);
                if(selectedList.get(position)){
                    holder.checkBox.setChecked(true);
                }else{
                    holder.checkBox.setChecked(false);
                }

            }else{
                holder.checkBox.setVisibility(View.GONE);
            }
        } else if(viewType == TYPE_VIEW_TIP){
            ViewGroup group = (ViewGroup) viewHolder.itemView;
            View view = LayoutInflater.from(mContext).inflate(mTipViewCreator.layoutRes(), group, false);
            group.removeAllViews();
            group.addView(view);
            mTipViewCreator.onCreateView(view);
        } else if (viewType == TYPE_VIEW_CALLING) {
            CallingViewHolder callingViewHolder = (CallingViewHolder) viewHolder;
            if (isVoice) {
                callingViewHolder.callIconIv.setImageResource(R.drawable.cc_chat_calling);
            } else {
                callingViewHolder.callIconIv.setImageResource(R.drawable.cc_chat_video);
            }
            callingViewHolder.statusTv.setText(callingViewText);
            if (callTime == 0) {
                callingViewHolder.timeCh.stop();
                callingViewHolder.timeCh.setVisibility(View.GONE);
            } else {
                if (updateChronometer) {
                    updateChronometer = false;
                    callingViewHolder.timeCh.setVisibility(View.VISIBLE);
                    callingViewHolder.timeCh.setBase(callTime);
                    callingViewHolder.timeCh.start();
                }
            }
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List<Object> payloads) {

        super.onBindViewHolder(holder, position, payloads);
    }

    public Conversation getItem(int position) {
        position = position - getExtraViewNum();
        if (position >= getDataList().size() || position<0) {
            return null;
        }
        return getDataList().get(position);
    }

    @Override
    public int getItemCount() {
        return getDataList().size() + getExtraViewNum();
    }

    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    private void bindUnreadCount(final ViewHolder holder, final Conversation conv, final int position) {
        int unreadCount = conv.getUnReadCount();
        int type = conv.getType();
        int status = conv.getStatus();
        LogF.d(TAG + "bindUnreadCount"," unreadCount = " + unreadCount + " content = "+conv.getBody()+" type = "+type+" status = "+status);
        if (type == Type.TYPE_MSG_TEXT_QUEUE || type == Type.TYPE_MSG_TEXT_OUTBOX || type == Type.TYPE_MSG_TEXT_DRAFT || type == Type.TYPE_MSG_TEXT_FAIL) {
            status = Status.getStatusFromType(type);
            type = Type.TYPE_MSG_TEXT_SEND;
        }
        boolean isLeft = (type & Type.TYPE_RECV) > 0;
        boolean isSlient = conv.getSlientDate() > 0;
        if (unreadCount <= 0) {
            //优化未读数消失的时机
            if ((!TextUtils.isEmpty(mClickConvAddress) && conv.getAddress().equals(mClickConvAddress))) {
                mClickConvAddress = "";
            }
            holder.rnMessageBadge.setVisibility(View.GONE);
            holder.mRedDotSilent.setVisibility(View.GONE);
            if (isSlient && conv.getBoxType() != Type.TYPE_BOX_MAILASSISTANT ) {
                holder.sIvConvSlient.setVisibility(View.VISIBLE);
            } else {
                holder.sIvConvSlient.setVisibility(View.GONE);
            }
        } else {
            holder.sIvConvSlient.setVisibility(View.GONE);
            if (isSlient || conv.getBoxType() == Type.TYPE_BOX_MAIL_OA
                    || conv.getBoxType() == Type.TYPE_BOX_MAILASSISTANT // 139邮箱
                    || conv.getAddress().equals(ConversationUtils.addressPlatform) //公众号二级页面
                    || mCacheType == CacheType.CT_PLATFORM //订阅号入口
                    || conv.getBoxType() == Type.TYPE_BOX_OA //三级页面的OA
                    || conv.getAddress().equals(ConversationUtils.addressNotify) //通知类短信入口
                    || mCacheType == CacheType.CT_NOTIFY //通知类短信
                    || conv.getBoxType() == Type.TYPE_BOX_SYSMSG //系统消息会话
                    ) {
                holder.rnMessageBadge.setVisibility(View.GONE);
                holder.mRedDotSilent.setVisibility(View.VISIBLE);
            } else {
                holder.rnMessageBadge.setVisibility(View.VISIBLE);
                holder.mRedDotSilent.setVisibility(View.GONE);
                String tmpCount = String.valueOf(unreadCount);
                if (unreadCount > 99) {
                    tmpCount = "99+";
                }
                DragBubbleView dragBubbleView = null;
                if (mContext instanceof HomeActivity) {
                    dragBubbleView = ((HomeActivity) mContext).getDragBubble();
                } else if (mContext instanceof NotifySmsActivity) {
                    dragBubbleView = ((NotifySmsActivity) mContext).getDragBubble();
                }
                holder.rnMessageBadge.setText(dragBubbleView, tmpCount);
                if (unreadCount > 99) {
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.rnMessageBadge.getLayoutParams();
                    layoutParams.width = (int) AndroidUtil.dip2px(mContext, 41);
                    holder.rnMessageBadge.setLayoutParams(layoutParams);
                    holder.rnMessageBadge.setImageResource(R.drawable.ic_massage_threedigit);
//                    holder.rnMessageBadge.setBackgroundResource(R.drawable.cc_chat_dot_double_digit);
                } else {
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.rnMessageBadge.getLayoutParams();

                    if (unreadCount > 9) {
                        layoutParams.width = (int) AndroidUtil.dip2px(mContext, 37);
                        holder.rnMessageBadge.setImageResource(R.drawable.ic_massage_twodigit);
                    } else {
                        layoutParams.width = (int) AndroidUtil.dip2px(mContext, 29);
                        holder.rnMessageBadge.setImageResource(R.drawable.ic_massage_onedigit);
                    }
                    holder.rnMessageBadge.setLayoutParams(layoutParams);
                }
            }

        }

        if (!isLeft && (status == Status.STATUS_FAIL || status == Status.STATUS_PAUSE)) {
            holder.sIvFailStatus.setVisibility(View.VISIBLE);
            holder.rnMessageBadge.setVisibility(View.GONE);
        } else {
            holder.sIvFailStatus.setVisibility(View.GONE);
        }

        holder.rnMessageBadge.setDragListener(new RoundNumber.DragStateListener() {
            @Override
            public void onDismiss(RoundNumber roundNumber) {
                if (conv.getUnReadCount() > 0) {
                    UmengUtil.buryPoint(mContext, "message_clear","滑动消除",0);
                    final String addr = conv.getAddress();
                    final int boxType = conv.getBoxType();
                    ConvCache.getInstance().clearUnreadNumFake(addr);
                    if (boxType == Type.TYPE_BOX_NOTIFY) {
                        final ArrayList<String> unReadAddress = ConvCache.getInstance().getUnreadNotifyConvs();
                        new RxAsyncHelper("").runInThread(new Func1() {
                            @Override
                            public Object call(Object o) {
                                ConversationUtils.updateSeenSpecify(mContext, unReadAddress);
                                for (String s : unReadAddress) {
                                    ComposeMessageActivityControl.rcsImSendUnreadSys(ConversationUtils.addressPc, conv.getDate(), s, CommonConstant.SINGLECHATTYPE);
                                }
                                return null;
                            }
                        }).subscribe();
                    } else {
                        new RxAsyncHelper("").runInThread(new Func1() {
                            @Override
                            public Object call(Object o) {
                                if ((boxType & Type.TYPE_BOX_GROUP) > 0) {
                                    Message msg = GroupChatUtils.getLastMessage(mContext, addr);
                                    if (msg != null) {
                                        String identify = msg.getIdentify();
                                        if (TextUtils.isEmpty(msg.getIdentify())) {
                                            identify = GroupChatUtils.getIdentify(mContext, msg.getAddress());
                                        }
                                        ComposeMessageActivityControl.rcsImSendUnreadSys(ConversationUtils.addressPc, msg.getDate(), identify, CommonConstant.GROUPCHATTYPE);
                                    }
                                } else if ((boxType & Type.TYPE_BOX_MESSAGE) > 0) {
                                    ComposeMessageActivityControl.rcsImSendUnreadSys(ConversationUtils.addressPc, conv.getDate(), addr, CommonConstant.SINGLECHATTYPE);
                                } else if ((boxType & Type.TYPE_BOX_PLATFORM) > 0) {
                                    ComposeMessageActivityControl.rcsImSendUnreadSys(ConversationUtils.addressPc, conv.getDate(), addr, CommonConstant.PLATFORMCHATTYPE);
                                } else if ((boxType & Type.TYPE_BOX_MAILASSISTANT) > 0) {
                                    ComposeMessageActivityControl.rcsImSendUnreadSys(ConversationUtils.addressPc, conv.getDate(), addr, CommonConstant.MAILCHATTYPE);
                                }else if ((boxType & Type.TYPE_BOX_ONLY_ONE_LEVEL_OA) > 0){
                                    MailOAUtils.updateSeenRead(mContext,addr,addr,boxType);
                                }else if((boxType & Type.TYPE_BOX_SYSMSG) > 0){
                                    ConversationUtils.updateSeen(mContext, Type.TYPE_BOX_SYSMSG, null);
                                }
                                ConversationUtils.updateSeen(mContext, boxType, addr, "");
                                return null;
                            }
                        }).subscribe();
                    }
                }
            }
        });
        final Handler h = new Handler();
        holder.rnMessageBadge.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
//                        holder.mBubbleBg.setVisibility(View.GONE);
                        break;
                    case MotionEvent.ACTION_UP:

                        h.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                final int visibility = holder.rnMessageBadge.getVisibility();
//                                holder.mBubbleBg.setVisibility(visibility == View.GONE ? View.GONE : View.VISIBLE);
                            }
                        }, 500);
                }
                return false;
            }
        });
    }

    private final Map<String, String> mCacheOAName = new ArrayMap<>();

    private void bindName(final ViewHolder holder, final Conversation conv) {
        int boxType = conv.getBoxType();
        final String address = conv.getAddress();
        String person = conv.getPerson();
        holder.sTvConvName.setTag(R.id.tv_conv_name, address);
        if (boxType == Type.TYPE_BOX_PLATFORM || boxType == Type.TYPE_BOX_PLATFORM_DEFAULT) {
            if (!TextUtils.isEmpty(person)) {
                holder.sTvConvName.setText(person);
                return;
            }
            if (!StringUtil.isEmpty(address)) {
                holder.sTvConvName.setText("");
                loadPersonAsync(conv, holder.sTvConvName);
                return;
            }
        } else if (boxType == Type.TYPE_BOX_MAILASSISTANT) {
            holder.sTvConvName.setText(mContext.getString(R.string.email_control_helper));
            if (TextUtils.isEmpty(conv.getPerson())) {
                conv.setPerson(mContext.getString(R.string.email_control_helper));
            }
            return;
        } else if (boxType == Type.TYPE_BOX_ONLY_ONE_LEVEL_OA) {
            String oaPerson = conv.getPerson();
            if (!TextUtils.isEmpty(oaPerson)) {
                holder.sTvConvName.setText(oaPerson);
            } else {
                if (TextUtils.isEmpty(mCacheOAName.get(address))) {
                    new RxAsyncHelper<>(address).runInThread(new Func1<String, OAList>() {
                        @Override
                        public OAList call(String s) {
                            OAList oa = OAUtils.getOA(mContext, address);
                            ContentValues values = new ContentValues();
                            values.put(BaseModel.COLUMN_NAME_PERSON, oa.getName());
                            values.put(BaseModel.COLUMN_NAME_ICON_PATH, oa.getLogo());
                            ConversationUtils.update(mContext, address, values);
                            return oa;
                        }
                    }).runOnMainThread(new Func1<OAList, Object>() {
                        @Override
                        public Object call(OAList oaList) {
                            String name = oaList.getName();
                            mCacheOAName.put(address, name);
                            holder.sTvConvName.setText(name);
                            return null;
                        }
                    }).subscribe();
                } else {
                    holder.sTvConvName.setText(mCacheOAName.get(address));
                }
            }
            return;
        } else if (boxType == Type.TYPE_BOX_MAIL_OA || boxType == Type.TYPE_BOX_OA) {
            String oaPerson = conv.getPerson();
            if (!TextUtils.isEmpty(oaPerson)) {
                holder.sTvConvName.setText(oaPerson);
                return;
            }
            new RxAsyncHelper(address).runInThread(new Func1<String, OAList>() {

                @Override
                public OAList call(String address) {
                    if (!TextUtils.isEmpty(address) && address.contains("@")) {
                        address = address.substring(0, address.indexOf("@"));
                    }
                    OAList oa = OAUtils.getOA(mContext, address);
                    ContentValues values = new ContentValues();
                    values.put(BaseModel.COLUMN_NAME_PERSON, oa.getName());
                    values.put(BaseModel.COLUMN_NAME_ICON_PATH, oa.getLogo());
                    ConversationUtils.update(mContext, address, values);
                    return oa;
                }
            }).runOnMainThread(new Func1<OAList, Object>() {
                @Override
                public Object call(OAList oa) {
                    if (oa != null) {
                        String name = oa.getName();
                        String tag = (String) holder.sTvConvName.getTag(R.id.tv_conv_name);
                        conv.setPerson(name);
                        String logo = oa.getLogo();
                        if (conv.getIconPath() == null) {
                            conv.setIconPath(logo);
                            LogF.d(TAG, "get OA logo:" + logo);
                        }
                        if (holder.sTvConvName != null && TextUtils.equals(address, tag)) {
                            if (!TextUtils.isEmpty(name)) {
                                holder.sTvConvName.setText(name);
                            } else {
                                holder.sTvConvName.setText(address);
                            }
                            GlidePhotoLoader.getInstance(mContext).loadOAPhoto(holder.sIvHead, address, logo);
                        }
                    }
                    return null;
                }
            }).subscribe();
        } else if (boxType == Type.TYPE_BOX_GROUP_MASS) {
            holder.sTvConvName.setText(mContext.getString(R.string.group_mass_assistant));
//            if (TextUtils.isEmpty(conv.getPerson())) {
            conv.setPerson(mContext.getString(R.string.group_mass_assistant));
//            }
            return;
        }
        String name = getPerson(conv);
        conv.setPerson(name);
        holder.sTvConvName.setText(name);
        if(name!=null && conv.getAddress()!=null && name.equals(conv.getAddress())){
//            CallShowManager.getInstance(mContext).getPairInfo(conv.getAddress(), LoginUtils.getInstance().getLoginUserName(),new GetStrangerNameListener(holder.sTvConvName,conv));
            StrangerEnterpriseUtil.getStrangerPairInfo(mContext,conv.getAddress(), LoginUtils.getInstance().getLoginUserName(),new GetStrangerNameListener(holder.sTvConvName,conv));
        }else{
            conv.setStrangerEnterprise("");
        }

    }

    private class GetStrangerNameListener implements OnInComingCallShowListener{
        public TextView mNameTv;
        public Conversation mConv;

        public GetStrangerNameListener(TextView nameText,Conversation conv){
            mNameTv = nameText;
            mConv = conv;
        }

        @Override
        public void onSuccess(InComingCallInfoEntity inComingCallInfoEntity) {
            if(inComingCallInfoEntity!=null){
                if(!TextUtils.isEmpty(inComingCallInfoEntity.getEnterprise())){
                    mNameTv.setText(inComingCallInfoEntity.getMark());
                    mConv.setPerson(inComingCallInfoEntity.getMark());
                    mConv.setStrangerEnterprise(inComingCallInfoEntity.getEnterprise());
                }
            }
        }

        @Override
        public void onFail(String s) {

        }
    }

    private void reSetTextSize(ViewHolder holder) {
        LayoutParams rootParams = holder.mRootView.getLayoutParams();
        rootParams.height = (int) (ROOT_VIEW_SIZE * (getFontScale()));
        holder.mRootView.setLayoutParams(rootParams);
//
        LayoutParams svHeadParams = holder.sIvHead.getLayoutParams();
        svHeadParams.height = (int) (SV_HEAD_VIEW_SIZE * getFontScale());
        svHeadParams.width = svHeadParams.height;
        holder.sIvHead.setLayoutParams(svHeadParams);

        LayoutParams epParams = holder.mEp_flag.getLayoutParams();
        epParams.height = (int) (SystemUtil.dip2px(18) * FontUtil.getFontScale());
        epParams.width = epParams.height;
        holder.mEp_flag.setLayoutParams(epParams);

        holder.sTvConvName.setTextSize(CON_NAME_FONT_SIZE * getFontScale());
        holder.sTvConvName.setMaxEms((int) (TV_CONTENT_NAME_MAX_EMS / getFontScale()));
        holder.rnMessageBadge.setTextSize(TV_UNREAD_FONT_SIZE * getFontScale());
        holder.rnMessageBadge.setRoundRadius(SystemUtil.dip2px(TV_UNREAD_RADIUS_SIZE) * getFontScale());
        holder.sTvDate.setTextSize(TV_DATE_FONT_SIZE * getFontScale());
        holder.sTvContent.setTextSize(TV_CONTENT_FONT_SIZE * getFontScale());
        holder.sTvDraftHint.setTextSize(TV_CONTENT_FONT_SIZE * getFontScale());
    }

    private float mFontScale = 1.0f;

    public void onAppFontSizeChanged(float scale) {
        mFontScale = scale;
        notifyDataSetChanged();
    }

    private void bindHead(ViewHolder holder, Conversation conv) {
        int boxType = conv.getBoxType();
//        Log.d(TAG, "bindHead boxType:" + boxType);
        String address = conv.getAddress();
        if ((boxType & Type.TYPE_BOX_SYSMSG) > 0) {
            GlidePhotoLoader.getInstance(mContext).loadSysMsgPhoto( holder.sIvHead, address);
        } else if ((boxType & Type.TYPE_BOX_NOTIFY) > 0) {
            GlidePhotoLoader.getInstance(mContext).loadNotifyMsgPhoto( holder.sIvHead, address);
        } else if ((boxType & Type.TYPE_BOX_PC) > 0) {
            GlidePhotoLoader.getInstance(mContext).loadPcMsgPhoto( holder.sIvHead, address, true);
        } else if ((boxType & Type.TYPE_BOX_GROUP) > 0) {
            GlidePhotoLoader.getInstance(mContext).loadGroupPhoto(mContext, holder.sIvHead, null, address);
        } else if ((boxType & Type.TYPE_BOX_PLATFORM) > 0 || (boxType & Type.TYPE_BOX_PLATFORM_DEFAULT) > 0) {
            GlidePhotoLoader.getInstance(mContext).loadPlatformPhoto(mContext, holder.sIvHead, address);
        } else if ((boxType & Type.TYPE_BOX_SMS) > 0
                || (boxType & Type.TYPE_BOX_BLACK_MESSAGE) > 0
                || (boxType & Type.TYPE_BOX_MMS) > 0) {
            GlidePhotoLoader.getInstance(mContext).loadPhoto(mContext, holder.sIvHead, address);
        } else if (boxType == Type.TYPE_BOX_MAIL_OA || boxType == Type.TYPE_BOX_OA /*|| boxType == Type.TYPE_BOX_ONLY_ONE_LEVEL_OA*/) {
            GlidePhotoLoader.getInstance(mContext).loadOAPhoto(holder.sIvHead, address, conv.getIconPath());
        } else if (boxType == Type.TYPE_BOX_ONLY_ONE_LEVEL_OA) {
            GlidePhotoLoader.getInstance(mContext).loadOneLevelOAPhoto(holder.sIvHead ,address, conv.getIconPath());
        } else if (boxType == Type.TYPE_BOX_MAILASSISTANT) {
            GlidePhotoLoader.getInstance(mContext).loadMailAssistant(holder.sIvHead, address);
        } else {
            GlidePhotoLoader.getInstance(mContext).loadPhoto(mContext, holder.sIvHead, address);
        }

        if (conv.getGroupType() == MtcImConstants.EN_MTC_GROUP_TYPE_ENTERPRISE) {// 企业群
            holder.mEp_flag.setImageResource(R.drawable.cc_chat_company);
            holder.mEp_flag.setVisibility(View.VISIBLE);
            holder.partyGroupCoinImg.setVisibility(View.GONE);
        } else if (conv.getGroupType() == MtcImConstants.EN_MTC_GROUP_TYPE_PARTY) {// 党建群
            holder.partyGroupCoinImg.setImageResource(R.drawable.cc_chat_ic_party_group);
            holder.mEp_flag.setVisibility(View.GONE);
            holder.partyGroupCoinImg.setVisibility(View.VISIBLE);
        } else if ((boxType & Type.TYPE_BOX_NOTIFY) > 0 || (boxType & Type.TYPE_BOX_SMS) > 0
                || (boxType & Type.TYPE_BOX_MMS) > 0) {                                      // 通知类短信
            holder.mEp_flag.setImageResource(R.drawable.cc_chat_message);
            holder.mEp_flag.setVisibility(View.VISIBLE);
            holder.partyGroupCoinImg.setVisibility(View.GONE);
        } else {
            holder.mEp_flag.setVisibility(View.GONE);
            holder.partyGroupCoinImg.setVisibility(View.GONE);
        }
    }

    private void bindContent(final ViewHolder holder, final Conversation conv) {
        if (TextUtils.isEmpty(conv.getBody()) && conv.getType() == Type.TYPE_MSG_TEXT_RECV) { //清空消息后，会话内容置空
            holder.sTvDraftHint.setVisibility(View.GONE);
            holder.sTvContent.setText("");
            return;
        }
        long time = System.currentTimeMillis();
        int type = conv.getType();
//        Log.d(TAG, "bindContent type:" + type);
        final int status = conv.getStatus();
//        Log.d(TAG, "bindContent status:" + status);
        holder.sTvContent.setTag(R.id.tv_content, conv.getAddress());
        final int boxType = conv.getBoxType();
        String content = conv.getBody();
        switch (type) {
            case Type.TYPE_MSG_SYSTEM_TEXT:
            case Type.TYPE_MSG_TEXT_QUEUE:
            case Type.TYPE_MSG_TEXT_OUTBOX:
            case Type.TYPE_MSG_TEXT_FAIL:
            case Type.TYPE_MSG_TEXT_RECV:
            case Type.TYPE_MSG_TEXT_SEND:
            case Type.TYPE_MSG_TEXT_SEND_CCIND:
            case Type.TYPE_MSG_SMS_SEND:
            case Type.TYPE_MSG_TEXT_DRAFT:
            case Type.TYPE_MSG_SINGLE_PIC_TEXT_RECV:
            case Type.TYPE_MSG_MULIT_PIC_TEXT_RECV:
            case Type.TYPE_SMS_NOTICE:
            case Type.TYPE_SYSMSG_VCARD:
                break;
            case Type.TYPE_MSG_SMS_RECV:
            case Type.TYPE_MSG_TEXT_SUPER_SMS_SEND:
                content = mContext.getString(R.string.news) + conv.getBody();
                break;
            case Type.TYPE_MSG_MMS_SEND:
            case Type.TYPE_MSG_MMS_RECV:
                // 彩信的body查询的字段为其主题： sub
                try {
                    if (!TextUtils.isEmpty(content)) {
                        byte[] bt = content.getBytes("iso-8859-1");
                        content = new String(bt, "UTF-8");
                        EncodedStringValue v = null;
                        v = new EncodedStringValue(106, PduPersister.getBytes(content));
                        if (MmsUtils.isGarbled(v.getString())) {
                            content = MmsUtils.getStringOfGarbled(content, 6);
                        } else {
                            content = v.getString();
                        }
                        content = mContext.getString(R.string.super_news) + mContext.getString(R.string.title) + content;
                    } else {
                        content = mContext.getString(R.string.super_news) + mContext.getString(R.string.no_subject);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case Type.TYPE_MSG_IMG_RECV:
            case Type.TYPE_MSG_IMG_SEND_CCIND:
            case Type.TYPE_MSG_IMG_SEND:
                content = mContext.getString(R.string.picture_);
                break;
            case Type.TYPE_MSG_AUDIO_RECV:
            case Type.TYPE_MSG_AUDIO_SEND:
            case Type.TYPE_MSG_AUDIO_SEND_CCIND:
                if(!TextUtils.isEmpty(content)){
                    content = mContext.getString(R.string.voice_)+ content ;
                }else{
                    content = mContext.getString(R.string.voice_);
                }
                break;
            case Type.TYPE_MSG_FILE_YUN_SEND:
            case Type.TYPE_MSG_FILE_SEND:
            case Type.TYPE_MSG_FILE_SEND_CCIND:
            case Type.TYPE_MSG_FILE_YUN_RECV:
            case Type.TYPE_MSG_FILE_RECV:
                content = mContext.getString(R.string.file_);
                break;
            case Type.TYPE_MSG_CARD_SEND:
            case Type.TYPE_MSG_CARD_SEND_CCIND:
            case Type.TYPE_MSG_CARD_RECV:
                content = mContext.getString(R.string.business_card_);
                break;
            case Type.TYPE_MSG_VIDEO_SEND:
            case Type.TYPE_MSG_VIDEO_RECV:
            case Type.TYPE_MSG_VIDEO_SEND_CCIND:
                content = mContext.getString(R.string.video_);
                break;
            case Type.TYPE_MSG_OA_ONE_CARD_SEND:
            case Type.TYPE_MSG_OA_ONE_CARD_RECV:
                String body = conv.getBody();
                if(TextUtils.isEmpty(body)){
                    content = mContext.getString(R.string.oa_message);
                }else {
                    content = body;
                    conv.setLatestContent(null);
                }
                break;
            case Type.TYPE_MSG_CARD_VOUCHER_RECV:
            case Type.TYPE_MSG_CARD_VOUCHER_SEND:
                content = mContext.getString(R.string.card_voucher) + conv.getBody();
                break;
            case Type.TYPE_MSG_DATE_ACTIVITY_SEND:
            case Type.TYPE_MSG_DATE_ACTIVITY_RECV:
                content = mContext.getString(R.string.date_activity_message);
                break;
            case Type.TYPE_MSG_T_CARD_SEND:
            case Type.TYPE_MSG_T_CARD_RECV:
            case Type.TYPE_MSG_T_CARD_SEND_CCIND:
            case Type.TYPE_MSG_ENTERPRISE_SHARE_SEND:
            case Type.TYPE_MSG_ENTERPRISE_SHARE_RECV:
                content = mContext.getString(R.string.enterprise_share_message) + conv.getBody();
                break;
            case Type.TYPE_MSG_WITHDRAW_REVOKE:
                content = conv.getBody();
                break;
            case Type.TYPE_MSG_BAG_SEND:
            case Type.TYPE_MSG_CASH_BAG_SEND:
            case Type.TYPE_MSG_BAG_RECV:
            case Type.TYPE_MSG_CASH_BAG_RECV:
                content = mContext.getString(R.string.meetyou_red_message) + conv.getBody();
                break;
            case Type.TYPE_MSG_CASH_BAG_RETURN:
                content = mContext.getString(R.string.meetyou_red_message) + conv.getBody();
                break;
            case Type.TYPE_MSG_BAG_RECV_COMPLETE:
            case Type.TYPE_MSG_BAG_SEND_COMPLETE:
                content = conv.getBody();
                break;
            case Type.TYPE_MSG_OA_CARD_SEND:
            case Type.TYPE_MSG_OA_CARD_RECV:
                content = conv.getBody();
                break;

            case Type.TYPE_MSG_LOC_RECV:
            case Type.TYPE_MSG_LOC_SEND:
            case Type.TYPE_MSG_LOC_SEND_CCIND:
                content = mContext.getString(R.string.location_type);
                break;
            case Type.TYPE_MSG_GROUP_MASS_SEND:
                content = conv.getBody();
                break;
            default:
                content = mContext.getString(R.string.unkwown_type_);
        }
        final int mEmojiconSize = 47;
        // 设置草稿内容
        if (conv.getType() == Type.TYPE_MSG_TEXT_DRAFT || conv.getHasDraft()) {
            holder.sTvDraftHint.setVisibility(View.VISIBLE);
            int draftWidth = holder.sTvDraftHint.getWidth();
            holder.sTvContent.setMaxWidth(mContentTextMaxWidth - draftWidth + 1);
            String body = conv.getBody();
            if(conv.getHasDraft()){
                body = conv.getDraftMsg();
            }
            CharSequence draftBuilder = EmojiParser.getInstance().replaceAllEmojis(mContext,body , mEmojiconSize);
            //            holder.sTvContent.setLinkText(draftBuilder);
            holder.sTvContent.setText(draftBuilder);
            return;
        } else {
            holder.sTvDraftHint.setVisibility(View.GONE);
            holder.sTvContent.setMaxWidth(mContentTextMaxWidth);
        }
        CharSequence builderEmoji = null;
        SpannableStringBuilder builder = conv.getLatestContent();
        content = StringUtil.replaceNtoBlank(content);
        if (builder != null) {

        } else if ((boxType & Type.TYPE_BOX_GROUP) > 0) {
            if ((type & Type.TYPE_RECV) > 0) {
                long extNotify = conv.getNotifyDate();
                long time1 = System.currentTimeMillis();
                String nickName = NickNameUtils.getNickName(mContext, conv.getSendAddress(), conv.getAddress());
                LogF.d("abc", "nick name time : " + (System.currentTimeMillis() - time1));
                if (extNotify > 0) {
                    boolean isSlient = conv.getSlientDate() > 0;
                    int unreadCount = conv.getUnReadCount();
                    String slientString = "";
                    if (isSlient && unreadCount > 0) {
                        slientString = "[" + unreadCount + mContext.getString(R.string.news_unit) + "] ";
                        if (unreadCount > 99) {
                            slientString = mContext.getString(R.string.lots_of_news);
                        }
                    }
                    String atString = mContext.getString(R.string.notify_me).intern();
                    String extString = slientString + atString;
                    builder = new SpannableStringBuilder(extString);
                    ForegroundColorSpan redSpan = new ForegroundColorSpan(mContext.getResources().getColor(R.color.message_draft));
                    builder.setSpan(redSpan, slientString.length(), slientString.length() + atString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    builderEmoji = EmojiParser.getInstance().replaceAllEmojis(mContext, nickName + ": " + content, mEmojiconSize);
                    builder.append(builderEmoji);
                    conv.setLatestContent(builder);
                } else {
                    builder = EmojiParser.getInstance().replaceAllEmojis(mContext, nickName + ": " + content, mEmojiconSize);
                    conv.setLatestContent(builder);
                }
            } else if (boxType == Type.TYPE_BOX_MAIL_OA || boxType == Type.TYPE_BOX_OA || boxType == Type.TYPE_BOX_ONLY_ONE_LEVEL_OA) {
                final String finalContent = content;
                new RxAsyncHelper<>(conv.getSendAddress()).runInThread(new Func1<String, String>() {
                    @Override
                    public String call(String nickName) {
                        if ((boxType & Type.TYPE_BOX_OA) > 0) {
                            OAList oa = OAUtils.getOA(mContext, nickName);
                            if (oa != null) {
                                String name = oa.getName();
                                if (!TextUtils.isEmpty(name)) {
                                    nickName = name;
                                }
                            }
                        } else {
                            nickName = getPerson(conv);
                        }
                        return nickName;
                    }
                }).runOnMainThread(new Func1<String, Object>() {
                    @Override
                    public Object call(String nickName) {
                        String tag = (String) holder.sTvContent.getTag(R.id.tv_content);
                        if (TextUtils.equals(conv.getAddress(), tag)) {
                            if ((boxType & Type.TYPE_BOX_MAIL_OA) > 0 || (boxType & Type.TYPE_BOX_OA) > 0) {
                                if (!TextUtils.isEmpty(finalContent)) {
                                    String replaceAll = contentTrim(finalContent.replaceAll("\n".intern(), " ".intern()));
                                    SpannableStringBuilder builder = new SpannableStringBuilder(nickName + "：".intern());
                                    CharSequence builderEmoji = EmojiParser.getInstance().replaceAllEmojis(mContext, replaceAll, mEmojiconSize);
                                    builder.append(builderEmoji);
                                    holder.sTvContent.setText(builder);
                                    conv.setLatestContent(builder);
                                } else {
                                    if (TextUtils.isEmpty(nickName)) {
                                        holder.sTvContent.setText("");
                                    } else {
                                        holder.sTvContent.setText(nickName + "：" + mContext.getString(R.string.no_subject));
                                        conv.setLatestContent(new SpannableStringBuilder(nickName + "：" + mContext.getString(R.string.no_subject)));
                                    }
                                }
                            }
                        }
                        return null;
                    }
                }).subscribe();
            } else {
                builder = EmojiParser.getInstance().replaceAllEmojis(mContext, content, mEmojiconSize);
                conv.setLatestContent(builder);
            }
        } else if ((boxType & Type.TYPE_BOX_PLATFORM) > 0 && mCacheType == CacheType.CT_PLATFORM) {
            //
            if (conv.getUnReadCount() > 0) {
                content = String.format(mContext.getString(R.string.news_unit_), conv.getUnReadCount() > 99 ? "99+" : conv.getUnReadCount() + "") + content;

            }
            builder = EmojiParser.getInstance().replaceAllEmojis(mContext, content, mEmojiconSize);
            conv.setLatestContent(builder);
        } else {
            builder = EmojiParser.getInstance().replaceAllEmojis(mContext, content, mEmojiconSize);
            conv.setLatestContent(builder);
        }
        holder.sTvContent.setText(builder);
    }

    // 139邮箱content太长，setText 会很耗时，先截断一下
    private String contentTrim(String content) {
        if (content == null || content.length() <= 30) {
            return content;
        } else {
            return content.substring(0, 30) + "...";
        }
    }

    private String getPerson(Conversation conv) {
        String address = conv.getAddress();
        if (StringUtil.isEmpty(address))
            return "null".intern();

        int boxType = conv.getBoxType();
//        Log.d(TAG, "boxType:" + boxType);
        if (boxType == Type.TYPE_BOX_GROUP) {
            String person = conv.getPerson();
            if (TextUtils.isEmpty(person)) {
                person = NickNameUtils.getPerson(mContext, boxType, address);
            }
            return person;
        } else if (boxType == Type.TYPE_BOX_MASS) {
            return conv.getPerson();
        }
        return NickNameUtils.getPerson(mContext, boxType, address);
    }

    private void loadPersonAsync(final Conversation conv, final TextView tv) {
        final String address = conv.getAddress();
        final int boxType = conv.getBoxType();
        RxAsyncHelper helper = new RxAsyncHelper("");
        helper.runInThread(new Func1<Object, String>() {
            @Override
            public String call(Object o) {
                return NickNameUtils.getPerson(mContext, boxType, address);
            }
        }).runOnMainThread(new Func1<String, Object>() {
            @Override
            public Object call(String s) {
                conv.setPerson(s);
                if (tv != null && address != null) {
                    if (address.equals(tv.getTag())) {
                        tv.setText(s);
                    }
                }
                return null;
            }
        }).subscribe();
    }

    class SearchViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

        public SearchViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.findViewById(R.id.search_edit).setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            final int size = ContactsCache.getInstance().getContactList().size();
            if (size == 0) ContactsCache.getInstance().refresh();  //如果联系人列表缓存的结合为空时刷新
            UmengUtil.buryPoint(mContext, "message_search","搜索",0);
            ContactProxy.g.getUiInterface().startSearchActivity(mContext, "hint");
        }
    }

    class NetErrorViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

        public NetErrorViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void onClick(View v) {
        }
    }

    class PCOnlineViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

        public PCOnlineViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mOnPcOnlieItemClickListener.onPcOnlineClick();
        }
    }

    public class CallingViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
        TextView statusTv;
        ImageView callIconIv;
        public Chronometer timeCh;
        public CallingViewHolder(View itemView) {
            super(itemView);

            statusTv = itemView.findViewById(R.id.status_tv);
            callIconIv = itemView.findViewById(R.id.call_icon_iv);
            timeCh = itemView.findViewById(R.id.call_time_ch);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (!IPCUtils.getInstance().isCalling()) {
                return;
            }
            int type = IPCUtils.getInstance().getCallType();
            String className = IPCUtils.getInstance().getClassName();
            CallProxy.g.getUiInterface().backToCallActivityFromMsg(MyApplication.getAppContext(),type,className);
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder implements OnClickListener, View.OnLongClickListener{

        View mRootView;
        ImageView sIvHead;

        TextView sTvConvName;

        TextView sTvDate;

        TextView sTvUnreadText;

//        TextView sTvUnread;

        RoundNumber rnMessageBadge;

//        ImageView mBubbleBg;

        TextView sTvUnreadSilence;

        ImageView sIvFailStatus;

        ImageView sIvConvSlient;

        EmojiTextView4Convlist sTvContent;

        TextView sTvDraftHint;

        //        ConstraintLayout mRlConvListItem;
        ConstraintLayout mRlConvListItem;

        ImageView mRedDotSilent;

        ImageView mEp_flag;//企业群标识
        ImageView partyGroupCoinImg ;

        CheckBox checkBox;
        public float mCurrentFontScale = 1.0f;

        public ViewHolder(View itemView) {
            super(itemView);
            mRootView = itemView.findViewById(R.id.rl_conv_list_item);
            checkBox = (CheckBox)itemView.findViewById(R.id.check_box);
            sIvHead = (ImageView) itemView.findViewById(R.id.svd_head);
            sTvConvName = (TextView) itemView.findViewById(R.id.tv_conv_name);
            sTvDate = (TextView) itemView.findViewById(R.id.tv_date);
            sTvUnreadText = (TextView) itemView.findViewById(R.id.tv_unread_text);
//            sTvUnread = (TextView) itemView.findViewById(R.id.tv_unread);
//            sTvUnreadSilence = (TextView) itemView.findViewById(R.id.tv_unread_silence);
            sTvDraftHint = (TextView) itemView.findViewById(R.id.tv_msg_draft_hint);
            rnMessageBadge = (RoundNumber) itemView.findViewById(R.id.rnMessageBadge);
//            mBubbleBg = (ImageView) itemView.findViewById(R.id.bubble_bg);
            sIvFailStatus = (ImageView) itemView.findViewById(R.id.iv_fail_status);
            sIvConvSlient = (ImageView) itemView.findViewById(R.id.iv_conv_slient);
            sTvContent = (EmojiTextView4Convlist) itemView.findViewById(R.id.tv_content);
            mRlConvListItem = (ConstraintLayout) itemView.findViewById(R.id.rl_conv_list_item);
            mEp_flag = (ImageView) itemView.findViewById(R.id.svd_head_EP_type);
            mRedDotSilent = (ImageView) itemView.findViewById(R.id.red_dot_silent);

            partyGroupCoinImg = (ImageView) itemView.findViewById(R.id.party_group_coin_img);
            sTvContent.setCanMove(false);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
//            checkBox.setOnCheckedChangeListener(this);
        }

        @Override
        public void onClick(View v) {
            //会话数量改变的时候点击可能会崩溃？？？
            if (getAdapterPosition() == -1) {
                return;
            }
            int position = getAdapterPosition();
//            if (hasSearchView) {
//                position = position - 1;
//            }
            Conversation conversation = getItem(position);
            if(conversation!=null) {
                mClickConvAddress = conversation.getAddress();
            }
            mOnRecyclerViewItemClickListener.onItemClick(v, position);
//            notifyItemChanged(getAdapterPosition());
//            onBindViewHolder(ViewHolder.this ,getAdapterPosition());
//            notifyItemChanged(getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            int position = getAdapterPosition();
//            if (hasSearchView) {
//                position = position - 1;
//            }
            return mOnRecyclerViewItemClickListener.onItemLongCLickListener(v, position);
        }

    }

    public void selectAll(){

        int totalSize = getDataList().size();
        LogF.i(TAG,"selectAll totalSize = " + totalSize);
        for(int i = 0; i < totalSize ; i ++){
            selectedList.put(i,true);
        }
        notifyDataChanged();
    }

    public void cancelSelectAll(){
        LogF.i(TAG,"cancelSelectAll");
        selectedList.clear();
        notifyDataChanged();
    }

    public void addSelection(int position){
        LogF.i(TAG,"addSelection position = " + position);
        selectedList.put(position,true);
        notifyItemDataChanged(position);
    }

    public void removeSelection(int position){
        LogF.i(TAG,"removeSelection position = " + position);
        selectedList.delete(position);
        notifyItemDataChanged(position);
    }

    private void notifyDataChanged(){
        notifyDataSetChanged();
        if(mOnCheckChangeListener != null){
            mOnCheckChangeListener.onCheckChange(selectedList.size());
        }
    }

    private void notifyItemDataChanged(int position){
        notifyItemChanged(position);
        if(mOnCheckChangeListener != null){
            mOnCheckChangeListener.onCheckChange(selectedList.size());
        }
    }

    public void clearSelection(){
        LogF.i(TAG,"clearSelection");
        selectedList.clear();
    }

    public void setMultiDelMode(boolean isMultiDelMode){
        this.isMultiDelMode = isMultiDelMode;
        notifyDataSetChanged();
    }

    public void setRecyclerViewItemClickListener(OnRecyclerViewItemClickListener listener) {
        mOnRecyclerViewItemClickListener = listener;
    }

    public void setOnPcOnlieItemClickListener(OnPcOnlieItemClickListener listener) {
        mOnPcOnlieItemClickListener = listener;
    }

    public void setOnCheckChangeListener(OnCheckChangeListener listener){
        mOnCheckChangeListener = listener;
    }

    public SparseBooleanArray getSelectedList(){
        return selectedList;
    }

    public ArrayList<Conversation> getDataList() {
        return (ArrayList<Conversation>) mCache.getCache(mCacheType);
    }

    public void clearClickConvAddress() {
        mClickConvAddress = "";
    }


    public int getCallViewPosition() {
        return (hasSearchView? 1 : 0) + (hasTipView()? 1 : 0) + (hasPCOnLineView? 1 : 0);
    }
}
