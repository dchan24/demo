package com.cmicc.module_message.ui.model.impls;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;

import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_business.model.Platform;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.utils.Log;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.utils.TimeUtil;
import com.cmcc.cmrcs.android.ui.model.impls.PublicAccountChatModel;
import com.rcs.rcspublicaccount.util.PublicAccountUtil;
import com.rcspublicaccount.api.bean.MsgContent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static com.constvalue.MessageModuleConst.MessageChatListAdapter.BUBBLE_NOUP_DOWN;
import static com.constvalue.MessageModuleConst.MessageChatListAdapter.BUBBLE_NOUP_NODOWN;
import static com.constvalue.MessageModuleConst.MessageChatListAdapter.BUBBLE_UP_DOWN;
import static com.constvalue.MessageModuleConst.MessageChatListAdapter.BUBBLE_UP_NODOWN;
import static com.constvalue.MessageModuleConst.MessageChatListAdapter.VIEW_SHOW_DATE_FLAG;
import static com.constvalue.MessageModuleConst.MessageChatListAdapter.VIEW_SHOW_TIME_FLAG;
import static com.rcs.rcspublicaccount.util.PublicAccountUtil.GET_PRE_MESSAGE_FAIL;
import static com.rcs.rcspublicaccount.util.PublicAccountUtil.GET_PRE_MESSAGE_SUCCESS;

/**
 * Created by KSBK on 2018/5/24.
 */

public class PublicAccountPreModelImpl implements PublicAccountChatModel {

    private static final String TAG = "PublicAccountPreModelImpl";

    public static final int NUM_LOAD_ONE_TIME = MessageEditorModelImpl.NUM_LOAD_ONE_TIME;


    private Context mContext;
    private String mAddress;
    private PublicAccountChatLoadFinishCallback mPublicAccountChatLoadFinishCallback;

    public static final int LOAD_TYPE_FIRST = 0;
    public static final int LOAD_TYPE_ADD = 1;
    public static final int LOAD_TYPE_DELETE = 2;
    public static final int LOAD_TYPE_UPDATE = 3;
    public static final int LOAD_TYPE_MORE = 4;

    private ArrayList<Platform> mSourceDataList = new ArrayList<>();

    private Handler  handler;
    private int pageNum = 1;
    private int loadType = LOAD_TYPE_FIRST;
    private int loadStatus = 0;//0未开始,1翻页中,2结束



    public PublicAccountPreModelImpl() {
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(android.os.Message msg) {
                switch (msg.what){
                    case GET_PRE_MESSAGE_SUCCESS:
                        List<MsgContent> rcsArgs = (List<MsgContent>) msg.obj;
                        ArrayList<Platform> platforms = new ArrayList<>();
                        if (rcsArgs!=null){
                            if (rcsArgs.size()<NUM_LOAD_ONE_TIME){
                                loadStatus = 2;
                            }
                            for (MsgContent msgContent :
                                    rcsArgs) {
                                Platform platform = PublicAccountUtil.getInstance().msgToPlatform(msgContent,mAddress);
                                platform.setId(System.currentTimeMillis()<<2+new Random().nextInt(99));
                                platforms.add(platform);
                            }

                            Collections.sort(platforms, new Comparator<Platform>() {
                                @Override
                                public int compare(Platform o1, Platform o2) {
                                    return (int) (o1.getDate()-o2.getDate());
                                }
                            });

                            LogF.d(TAG+"ksbk", "handleMessage: "+pageNum+"  size: "+platforms.size());
                            updateMessagesDataResource(platforms);

//                            MsgContent msgContent = mContentList.get(i);
//                            Platform platform = msgToPlatform(msgContent, address);
                        }else {
                            pageNum--;
                            updateMessagesDataResource(new ArrayList<Platform>());
                        }
                        break;
                    case GET_PRE_MESSAGE_FAIL:
                        pageNum--;
                        updateMessagesDataResource(new ArrayList<Platform>());
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public void loadMessages(Context context, int firstLoadNum, String address, long loadTime, LoaderManager loaderManager, PublicAccountChatLoadFinishCallback loadFinishCallback) {
        if (loadStatus!=0){
            return;
        }
        loadStatus = 1;
        mAddress = address;
        mContext = context;
        mPublicAccountChatLoadFinishCallback = loadFinishCallback;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = new Date();
        String time = sdf.format(date) + "+8:00";
        loadType = LOAD_TYPE_FIRST;
        LogF.d(TAG+"ksbk", "loadMessages: "+pageNum);
        PublicAccountUtil.getInstance().getPreMessage(context, mAddress,time, 1, NUM_LOAD_ONE_TIME, pageNum, handler);
        pageNum++;
    }

    @Override
    public void loadMoreMessages(LoaderManager loaderManager) {
        if (loadStatus>1){
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = new Date();
        String time = sdf.format(date) + "+8:00";
        loadType = LOAD_TYPE_MORE;
        LogF.d(TAG+"ksbk", "loadMoreMessages: "+pageNum);
        PublicAccountUtil.getInstance().getPreMessage(mContext, mAddress,time, 1, NUM_LOAD_ONE_TIME, pageNum, handler);
        pageNum++;
    }

    @Override
    public void clearAllMsg() {
        mSourceDataList.clear();
    }




    private void updateMessagesDataResource(ArrayList<Platform> msgs) {
        if (msgs.size() > 0) {
            mSourceDataList.addAll(0, msgs);
            calculateTime(mSourceDataList, 0, msgs.size() - 1);
            calculateBubble(mSourceDataList, 0, msgs.size() - 1);


            Bundle extra = new Bundle();
            extra.putSerializable("extra_result_data", mSourceDataList);
            extra.putBoolean("extra_has_more", true);
            extra.putInt("extra_add_num", msgs.size());
            mPublicAccountChatLoadFinishCallback.onLoadFinished(loadType, 1, extra);
        } else {
            Bundle extra = new Bundle();
            extra.putSerializable("extra_result_data", mSourceDataList);
            mPublicAccountChatLoadFinishCallback.onLoadFinished(loadType, 1, extra);
        }

    }

    private void calculateTime(List<Platform> messages, int start, int end) {
        int size = messages.size();

        Log.d(TAG, "calculateTime, count=" + size);

        long lastTime = -1;
        long mLastDate = -1;
        if (start > 0) {
            Message msg = (Message) messages.get(start - 1);
            mLastDate = msg.getDate();
            lastTime = msg.getDate();
        }

        int tenMin = 300000;// 10 * 30 * 1000;
        for (int i = start; i <= end; i++) {
            // 获取数据库里的时间
            Message msg = (Message) messages.get(i);
            long msgTime = msg.getDate();
            int type = msg.getType();
            int value = 0;
            if (type != Type.TYPE_MSG_SYSTEM_TEXT) {
                if (!TimeUtil.isOneDay(mLastDate, msgTime)) {
                    // yyyy年 MM年 dd日
                    mLastDate = msgTime;
                    lastTime = msgTime;
                    value |= VIEW_SHOW_DATE_FLAG;
                    value |= VIEW_SHOW_TIME_FLAG;
                } else if (lastTime < 0 || msgTime < lastTime || (msgTime - tenMin > lastTime)) {
                    lastTime = msgTime;
                    value |= VIEW_SHOW_TIME_FLAG;
                }
            }
            msg.setFlag(value);
        }
    }

    private void calculateBubble(List<Platform> messages, int start, int end) {
        int size = messages.size();
        for (int i = start; i <= end; i++) {
            boolean noUp = true;
            boolean noDown = true;
            Message msg = (Message) messages.get(i);
            Message preMsg = null;
            Message nextMsg = null;
            String sendAddress = "";
            String preSendAddress = "";
            String nextSendAddress = "";
            boolean isLeft = false;
            boolean preIsLeft = false;
            boolean nextIsLeft = false;
            boolean preIsRevoke = false;
            boolean nextIsRevoke = false;
            if (msg != null && msg.getSendAddress() != null) {
                sendAddress = msg.getSendAddress();
                isLeft = (msg.getType() & Type.TYPE_RECV) > 0;
            }

            if (i > 0) {
                preMsg = (Message) messages.get(i - 1);
                if (preMsg != null && preMsg.getSendAddress() != null) {
                    preSendAddress = preMsg.getSendAddress();
                    preIsLeft = (preMsg.getType() & Type.TYPE_RECV) > 0;
                    preIsRevoke = preMsg.getType() == Type.TYPE_MSG_WITHDRAW_REVOKE || preMsg.getType() == Type.TYPE_MSG_SYSTEM_TEXT;
                }
            }

            if (i < size - 1) {
                nextMsg = (Message) messages.get(i + 1);
                if (nextMsg != null && nextMsg.getSendAddress() != null) {
                    nextSendAddress = nextMsg.getSendAddress();
                    nextIsLeft = (nextMsg.getType() & Type.TYPE_RECV) > 0;
                    nextIsRevoke = nextMsg.getType() == Type.TYPE_MSG_SYSTEM_TEXT || nextMsg.getType() == Type.TYPE_MSG_WITHDRAW_REVOKE;
                }
            }

            if ((msg.getFlag() & VIEW_SHOW_DATE_FLAG) <= 0 && (msg.getFlag() & VIEW_SHOW_TIME_FLAG) <= 0 && preMsg != null && sendAddress.equals(preSendAddress) && isLeft == preIsLeft && !preIsRevoke && !isIgnore(msg) && !isIgnore(preMsg)) {
                noUp = false;
            }

            if (nextMsg != null && (nextMsg.getFlag() & VIEW_SHOW_DATE_FLAG) <= 0 && (nextMsg.getFlag() & VIEW_SHOW_TIME_FLAG) <= 0 && sendAddress.equals(nextSendAddress) && isLeft == nextIsLeft && !nextIsRevoke && !isIgnore(msg) && !isIgnore(nextMsg)) {
                noDown = false;
            }

//                if ((nextMsg != null && (sendAddress.equals(nextSendAddress) == false || isLeft != nextIsLeft)) || sendAddress.equals("")) {
            if (nextMsg != null && (!sendAddress.equals(nextSendAddress) || sendAddress.equals("")) ) {
                msg.setBigMargin(true);
            } else {
                msg.setBigMargin(false);
            }

            if (msg.getBoxType() == Type.TYPE_BOX_SMS || msg.getBoxType() == Type.TYPE_BOX_MMS) {
                noUp = true;
                noDown = true;
                if (msg != null)
                    isLeft = (msg.getType() & Type.TYPE_RECV) > 0;
                if (nextMsg != null)
                    nextIsLeft = (nextMsg.getType() & Type.TYPE_RECV) > 0;

                if ((msg.getFlag() & VIEW_SHOW_DATE_FLAG) <= 0 && (msg.getFlag() & VIEW_SHOW_TIME_FLAG) <= 0 && preMsg != null && isLeft == preIsLeft && !isIgnore(msg) && !isIgnore(preMsg)) {
                    noUp = false;
                }

                if (nextMsg != null && (nextMsg.getFlag() & VIEW_SHOW_DATE_FLAG) <= 0 && (nextMsg.getFlag() & VIEW_SHOW_TIME_FLAG) <= 0 && isLeft == nextIsLeft && !isIgnore(msg) && !isIgnore(nextMsg)) {
                    noDown = false;
                }

                if (nextMsg != null && (isLeft != nextIsLeft )) {
                    msg.setBigMargin(true);
                } else {
                    msg.setBigMargin(false);
                }
            }

            if (nextMsg == null) {
                msg.setIsLast(true);
            } else {
                msg.setIsLast(false);
            }

            if (noUp) {
                if (noDown) {
                    msg.setBubbleType(BUBBLE_NOUP_NODOWN);
                } else {
                    msg.setBubbleType(BUBBLE_NOUP_DOWN);
                }
            } else {
                if (noDown) {
                    msg.setBubbleType(BUBBLE_UP_NODOWN);
                } else {
                    msg.setBubbleType(BUBBLE_UP_DOWN);
                }
            }
        }
    }

    //视频、图片特殊消息不做聚合
    private boolean isIgnore(Message message){
        switch(message.getType()){
            case Type.TYPE_MSG_IMG_RECV:
            case Type.TYPE_MSG_IMG_SEND:
            case Type.TYPE_MSG_IMG_SEND_CCIND:
            case Type.TYPE_MSG_VIDEO_RECV:
            case Type.TYPE_MSG_VIDEO_SEND:
            case Type.TYPE_MSG_VIDEO_SEND_CCIND:
            case Type.TYPE_MSG_FILE_RECV:
            case Type.TYPE_MSG_FILE_SEND:
            case Type.TYPE_MSG_FILE_SEND_CCIND:
            case Type.TYPE_MSG_FILE_YUN_RECV:
            case Type.TYPE_MSG_FILE_YUN_SEND:
            case Type.TYPE_MSG_BAG_RECV_COMPLETE:
            case Type.TYPE_MSG_BAG_SEND_COMPLETE:

                return true;
            default:
                break;
        }
        return false;
    }
}
