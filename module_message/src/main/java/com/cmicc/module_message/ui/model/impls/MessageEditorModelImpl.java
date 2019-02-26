package com.cmicc.module_message.ui.model.impls;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.chinamobile.app.yuliao_business.model.BaseModel;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_business.provider.Conversations;
import com.chinamobile.app.yuliao_business.util.MessageUtils;
import com.chinamobile.app.yuliao_business.util.Status;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.utils.TimeUtil;
import com.cmicc.module_message.ui.model.MessageEditorModel;
import com.cmcc.cmrcs.android.ui.utils.MessageCursorLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.constvalue.MessageModuleConst.MessageChatListAdapter.BUBBLE_NOUP_DOWN;
import static com.constvalue.MessageModuleConst.MessageChatListAdapter.BUBBLE_NOUP_NODOWN;
import static com.constvalue.MessageModuleConst.MessageChatListAdapter.BUBBLE_UP_DOWN;
import static com.constvalue.MessageModuleConst.MessageChatListAdapter.BUBBLE_UP_NODOWN;
import static com.constvalue.MessageModuleConst.MessageChatListAdapter.VIEW_SHOW_DATE_FLAG;
import static com.constvalue.MessageModuleConst.MessageChatListAdapter.VIEW_SHOW_TIME_FLAG;

/**
 * Created by ksk on 2017/3/22.
 */

public class MessageEditorModelImpl implements MessageEditorModel, LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "MessageEditorModelImpl";
    private static final int MORE_MSG_ID = new Random(System.currentTimeMillis()).nextInt();
    private static final int ID = MORE_MSG_ID + 1;
    public static final int NUM_LOAD_ONE_TIME = 20;

    private int mMessageLoadCount = NUM_LOAD_ONE_TIME;

    private Context mContext;
    private LoaderManager mLoaderManager;
    private String mAddress;
    private MessageEditorLoadFinishCallback mMessageEditorLoadFinishCallback;

    public static final int LOAD_TYPE_FIRST = 0;
    public static final int LOAD_TYPE_ADD = 1;
    public static final int LOAD_TYPE_DELETE = 2;
    public static final int LOAD_TYPE_UPDATE = 3;
    public static final int LOAD_TYPE_MORE = 4;

    protected long mLoadFirstTime = 0; //首次进入传入的时间，作为判断是否来自搜索的依据
    protected int mSearchPos;//如果来自搜索，记录页面停留的位置
    protected long mCurrentUpdateTime = -100; //最近更新时间
    protected long mGlobalLoadStartTime = 0; //数据源第一条数据的时间
    private MoreMsgLoaderCallback mMoreMsgLoaderCallback;
    private ArrayList<Message> mSourceDataList = new ArrayList<>();

    private long beginTime;

    @Override
    public void loadMoreMessages(LoaderManager loaderManager) {
        if (mGlobalLoadStartTime <= 0) {
            LogF.d(TAG, "not init mGlobalLoadStartTime, return");
            return;
        }
        if (mMoreMsgLoaderCallback == null) {
            mMoreMsgLoaderCallback = new MoreMsgLoaderCallback();
        }
        loaderManager.initLoader(MORE_MSG_ID, null, mMoreMsgLoaderCallback);
    }

    private class MoreMsgLoaderCallback implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader onCreateLoader(int id, Bundle args) {
            String where = Conversations.buildWhereAddress(mAddress);
            // 过滤掉草稿 & 记住上次加载的消息中的最早时间
            where = where + " AND type<>3 AND date<" + mGlobalLoadStartTime;
            String order = String.format(Conversations.DATE_DESC_LIMIT, NUM_LOAD_ONE_TIME);
            MessageCursorLoader loader = new MessageCursorLoader(mContext);
            MessageCursorLoader.CursorToDataHelper<Message> dataHelper = new MessageCursorLoader.CursorToDataHelper<Message>(Message.class, false);
            dataHelper.setIsFromLoadMore(true);
            loader.setDataHelper(dataHelper);
            MessageCursorLoader.MessageQueryData data = new MessageCursorLoader.MessageQueryData(Conversations.Message.CONTENT_URI,
                    new String[]{
                            "_id", Type.TYPE_BOX_MESSAGE + " AS box_type", "msg_id", "thread_id", "address", "person",
                            "body", "date", "update_time", "type", "status", "read", "locked", "error_code", "ext_url", "ext_short_url",
                            "ext_title", "ext_file_name", "ext_file_path", "ext_thumb_path", "ext_size_descript",
                            "ext_file_size", "ext_down_size", "seen", "send_address", "text_size", "sub_original_link","sub_source_link",
                            "title", "xml_content", "ext_status",BaseModel.COLUMN_NAME_SUB_TITLE,BaseModel.COLUMN_NAME_SUB_BODY
                            ,BaseModel.COLUMN_NAME_AUTHOR,BaseModel.COLUMN_NAME_SUB_IMG_PATH, "show_send", "message_receipt"
                    }, where, null, order);
            loader.addMessageData(data);
            return loader;
        }

        @Override
        public void onLoadFinished(Loader loader, Cursor cursor) {
            LogF.d(TAG, "MoreMsgLoaderCallback onLoadFinished");

            MessageCursorLoader msgLoader = null;
            if (loader instanceof MessageCursorLoader) {
                msgLoader = ((MessageCursorLoader) loader);
            } else {
                LogF.e(TAG, "loader class invaild, return " + loader.getClass());
                return;
            }

            ArrayList<Message> dataList = null;
            MessageCursorLoader.CursorToDataHelper helper = msgLoader.getDataHelper();
            if (helper != null) {
                dataList = helper.getLoadDataArrayList();
                updateMessagesDataResource(dataList, true);

                Loader normalLoader = mLoaderManager.getLoader(ID);
                if (normalLoader != null && normalLoader instanceof MessageCursorLoader) {
                    // 修改加载时间
                    ArrayList<MessageCursorLoader.MessageQueryData> queryDatas = ((MessageCursorLoader)normalLoader).getMessageData();
                    for (MessageCursorLoader.MessageQueryData queryData : queryDatas) {
                        MessageCursorLoader.MessageQueryCondition condition = new MessageCursorLoader.MessageQueryCondition();
                        condition.setPrefix(Conversations.buildWhereAddress(mAddress) + " AND type<>3 AND date>=%d" + " AND update_time>%d");
                        condition.setOrder(Conversations.DATE_DESC);
                        condition.setTime(mGlobalLoadStartTime, mCurrentUpdateTime);
                        queryData.setCondition(condition);
                    }
                }
            } else {
                LogF.e(TAG, "---------helper is null----------");
            }

            mLoaderManager.destroyLoader(MORE_MSG_ID);
        }

        @Override
        public void onLoaderReset(Loader loader) {
            LogF.d(TAG, "MoreMsgLoaderCallback onLoaderReset");
        }
    }

    @Override
    public void loadMessages(Context context, String address, long loadStartTime, long updateTime, LoaderManager loaderManager, ArrayList<Message> list, MessageEditorLoadFinishCallback messageChatListLoadFinishCallback) {
        mContext = context;
        mSourceDataList = list;
        mLoaderManager = loaderManager;
        mAddress = address;
        mCurrentUpdateTime = updateTime;
        mGlobalLoadStartTime = loadStartTime;
        mMessageEditorLoadFinishCallback = messageChatListLoadFinishCallback;

        loaderManager.initLoader(ID, null, this);
    }

    @Override
    public void loadMessages(Context context, int firstLoadNum, String address, long loadTime, LoaderManager loaderManager, MessageEditorLoadFinishCallback messageChatListLoadFinishCallback) {
        mContext = context;
        mLoaderManager = loaderManager;
        mAddress = address;
        mLoadFirstTime = loadTime;
        mMessageEditorLoadFinishCallback = messageChatListLoadFinishCallback;

        if (firstLoadNum > 0) {
            mMessageLoadCount = Math.round(firstLoadNum / NUM_LOAD_ONE_TIME + 0.5f) * NUM_LOAD_ONE_TIME;
        }

        if (mLoadFirstTime > 0) {
            String where = Conversations.buildWhereAddress(mAddress);
            // 过滤掉草稿 & 记住上次加载的消息中的最早时间
            where = where + " AND type<>3 AND date>=" + mLoadFirstTime;
            Cursor cursor = context.getContentResolver().query(Conversations.Message.CONTENT_URI, new String[]{"_id"}, where, null, null);
            if (cursor != null) {
                mSearchPos = cursor.getCount();
                mMessageLoadCount = mMessageLoadCount + cursor.getCount();
                cursor.close();
            }
        }

        loaderManager.initLoader(ID, null, this);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        beginTime = System.currentTimeMillis();
        String where = Conversations.buildWhereAddress(mAddress);
        // 过滤掉草稿 & 记住上次加载的消息中的最早时间
        where = where + " AND type<>3 AND date>=" + mGlobalLoadStartTime + " AND update_time>" + mCurrentUpdateTime;
        String order = String.format(Conversations.DATE_DESC_LIMIT, mMessageLoadCount);
        MessageCursorLoader loader = new MessageCursorLoader(mContext);
        // todo 当前消息类型为Message.class，BaseCustomCursorAdapter中的mDataClass更新类型时需要同步
        MessageCursorLoader.CursorToDataHelper<Message> dataHelper = new MessageCursorLoader.CursorToDataHelper<>(Message.class, false);
        loader.setDataHelper(dataHelper);
        MessageCursorLoader.MessageQueryData data = new MessageCursorLoader.MessageQueryData(Conversations.Message.CONTENT_URI,
                new String[]{
                        "_id", Type.TYPE_BOX_MESSAGE + " AS box_type", "msg_id", "thread_id", "address", "person",
                        "body", "date", "update_time", "type", "status", "read", "locked", "error_code", "ext_url", "ext_short_url",
                        "ext_title", "ext_file_name", "ext_file_path", "ext_thumb_path", "ext_size_descript",
                        "ext_file_size", "ext_down_size", "seen", "send_address", "text_size", "sub_original_link","sub_source_link",
                        "title", "xml_content", "ext_status", BaseModel.COLUMN_NAME_SUB_TITLE,BaseModel.COLUMN_NAME_SUB_BODY
                        ,BaseModel.COLUMN_NAME_AUTHOR,BaseModel.COLUMN_NAME_SUB_IMG_PATH, "show_send", "message_receipt"
                }, where, null, order);
        loader.addMessageData(data);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        LogF.d(TAG, "onLoadFinished time : " + (System.currentTimeMillis() - beginTime));

        MessageCursorLoader msgLoader = null;
        if (loader instanceof MessageCursorLoader) {
            msgLoader = ((MessageCursorLoader) loader);
        } else {
            LogF.e(TAG, "loader class invaild, return " + loader.getClass());
            return;
        }

        ArrayList<Message> dataList = null;
        MessageCursorLoader.CursorToDataHelper helper = msgLoader.getDataHelper();
        if (helper != null) {
            dataList = helper.getLoadDataArrayList();
            if (dataList.size() > 0) {
                updateMessagesDataResource(dataList, false);

                // 修改加载时间
                ArrayList<MessageCursorLoader.MessageQueryData> queryDatas = msgLoader.getMessageData();
                for (MessageCursorLoader.MessageQueryData queryData : queryDatas) {
                    MessageCursorLoader.MessageQueryCondition condition = new MessageCursorLoader.MessageQueryCondition();
                    condition.setPrefix(Conversations.buildWhereAddress(mAddress) + " AND type<>3 AND date>=%d" + " AND update_time>%d");
                    condition.setOrder(Conversations.DATE_DESC);
                    condition.setTime(mGlobalLoadStartTime, mCurrentUpdateTime);
                    queryData.setCondition(condition);
                }
            } else {
                LogF.d(TAG, "---------update list is empty----------");
            }
        } else {
            LogF.e(TAG, "---------helper is null----------");
        }
        LogF.d(TAG, "onLoadFinished over time : " + (System.currentTimeMillis() - beginTime));

    }

    @Override
    public void onLoaderReset(Loader loader) {
        LogF.d(TAG, "onLoaderReset");
    }

    private void updateMessagesDataResource(ArrayList<Message> msgs, boolean isLoadMore) {
        int state = LOAD_TYPE_UPDATE;

        if (isLoadMore) {
            state = LOAD_TYPE_MORE;
            if (msgs.size() > 0) {
                ArrayList<Message> tmpMsgs = new ArrayList<>();
                tmpMsgs.addAll(msgs);
                for (Message msg : tmpMsgs) {
                    if (msg.getStatus() == Status.STATUS_DELETE) {
                        msgs.remove(msg);
                        MessageUtils.delete(mContext, msg.getId());
                    }
                }
                mSourceDataList.addAll(0, msgs);
                calculateTime(mSourceDataList, 0, msgs.size() - 1);
                calculateBubble(mSourceDataList, 0, msgs.size() - 1);

                //更新最近更新时间
                for (Message msg : msgs) {
                    if (msg.getUpdateTime() > mCurrentUpdateTime) {
                        mCurrentUpdateTime = msg.getUpdateTime();
                    }
                }

                // 更新第一条消息时间
                mGlobalLoadStartTime = mSourceDataList.get(0).getDate();

                Bundle extra = new Bundle();
                extra.putSerializable("extra_result_data", mSourceDataList);
                extra.putInt("extra_add_num", msgs.size());
                mMessageEditorLoadFinishCallback.onLoadFinished(state, 1, extra);
            } else {
                Bundle extra = new Bundle();
                mMessageEditorLoadFinishCallback.onLoadFinished(state, 1, extra);
            }
        } else {
            //完成model数据增量更新
            if (mSourceDataList.size() <= 0) {
                state = LOAD_TYPE_FIRST;
                ArrayList<Message> tmpMsgs = new ArrayList<>();
                tmpMsgs.addAll(msgs);
                for (Message msg : tmpMsgs) {
                    if (msg.getStatus() == Status.STATUS_DELETE) {
                        msgs.remove(msg);
                        MessageUtils.delete(mContext, msg.getId());
                    }
                }
                mSourceDataList = msgs;
                calculateTime(mSourceDataList, 0, mSourceDataList.size() - 1);
                calculateBubble(mSourceDataList, 0, mSourceDataList.size() - 1);
            } else {
                ArrayList<Message> tmpMsgs = new ArrayList<>();
                tmpMsgs.addAll(mSourceDataList);
                for (Message msg : msgs) {
                    boolean isContain = false;
                    for (int i = tmpMsgs.size() - 1; i >= 0; i--) {
                        Message tmpMsg = tmpMsgs.get(i);
                        if (tmpMsg.getId() == msg.getId()) {
                            isContain = true;
                            if (msg.getStatus() == Status.STATUS_DELETE) {
                                tmpMsgs.remove(i);
                                MessageUtils.delete(mContext, msg.getId());
                                if (state != LOAD_TYPE_ADD) {
                                    state = LOAD_TYPE_DELETE;
                                }
                                if (tmpMsgs.size() > 0) {
                                    if (i > 0 && i < tmpMsgs.size()) {
                                        calculateTime(tmpMsgs, i - 1, i);
                                        calculateBubble(tmpMsgs, i - 1, i);
                                    } else if (i <= 0) {
                                        calculateTime(tmpMsgs, 0, 0);
                                        calculateBubble(tmpMsgs, 0, 0);
                                    } else if (i >= tmpMsgs.size()) {
                                        calculateTime(tmpMsgs, tmpMsgs.size() - 1, tmpMsgs.size() - 1);
                                        calculateBubble(tmpMsgs, tmpMsgs.size() - 1, tmpMsgs.size() - 1);
                                    }
                                }
                                break;
                            } else {
                                if(msg.getDate() > tmpMsg.getDate()){
                                    isContain = false;
                                    tmpMsgs.remove(i);
                                    if (tmpMsgs.size() > 0) {
                                        if (i > 0 && i < tmpMsgs.size()) {
                                            calculateTime(tmpMsgs, i - 1, i);
                                            calculateBubble(tmpMsgs, i - 1, i);
                                        } else if (i <= 0) {
                                            calculateTime(tmpMsgs, 0, 0);
                                            calculateBubble(tmpMsgs, 0, 0);
                                        } else if (i >= tmpMsgs.size()) {
                                            calculateTime(tmpMsgs, tmpMsgs.size() - 1, tmpMsgs.size() - 1);
                                            calculateBubble(tmpMsgs, tmpMsgs.size() - 1, tmpMsgs.size() - 1);
                                        }
                                    }
                                    break;
                                }

                                if (msg.getType() != tmpMsg.getType()) {
                                    tmpMsg.setType(msg.getType());
                                    if (i > 0 && i < tmpMsgs.size() - 1) {
                                        calculateTime(tmpMsgs, i - 1, i + 1);
                                        calculateBubble(tmpMsgs, i - 1, i + 1);
                                    } else if (i <= 0) {
                                        calculateTime(tmpMsgs, 0, tmpMsgs.size() - 1 > 0 ? 1 : 0);
                                        calculateBubble(tmpMsgs, 0, tmpMsgs.size() - 1 > 0 ? 1 : 0);
                                    } else if (i >= tmpMsgs.size() - 1) {
                                        calculateTime(tmpMsgs, tmpMsgs.size() > 1 ? tmpMsgs.size() - 2 : tmpMsgs.size() - 1, tmpMsgs.size() - 1);
                                        calculateBubble(tmpMsgs, tmpMsgs.size() > 1 ? tmpMsgs.size() - 2 : tmpMsgs.size() - 1, tmpMsgs.size() - 1);
                                    }
                                }
                                msg.setFlag(tmpMsg.getFlag());
                                msg.setBigMargin(tmpMsg.getBigMargin());
                                msg.setIsLast(tmpMsg.getIsLast());
                                msg.setBubbleType(tmpMsg.getBubbleType());
                                tmpMsgs.set(i, msg);
                                break;
                            }
                        }
                    }

                    if (!isContain) {
                        state = LOAD_TYPE_ADD;
                        tmpMsgs.add(msg);
                        if(tmpMsgs.size() > 1){
                            calculateTime(tmpMsgs, tmpMsgs.size() - 2, tmpMsgs.size() - 1);
                            calculateBubble(tmpMsgs, tmpMsgs.size() - 2, tmpMsgs.size() - 1);
                        }else if(tmpMsgs.size() == 1){
							calculateTime(tmpMsgs, tmpMsgs.size() - 1, tmpMsgs.size() - 1);
							calculateBubble(tmpMsgs, tmpMsgs.size() - 1, tmpMsgs.size() - 1);
						}

                    }
                }
                mSourceDataList = tmpMsgs;
            }

            //更新最近更新时间
            for (Message msg : msgs) {
                if (msg.getUpdateTime() > mCurrentUpdateTime) {
                    mCurrentUpdateTime = msg.getUpdateTime();
                }
            }

            if (mSourceDataList.size() > 0) {
                mGlobalLoadStartTime = mSourceDataList.get(0).getDate();
            } else {
                mGlobalLoadStartTime = 0;
            }

            Bundle extra = new Bundle();
            boolean hasMore = true;
            if (mMessageLoadCount > mSourceDataList.size()) {
                hasMore = false;
                mMessageLoadCount = 0;
            }

            if (mSourceDataList != null) {
                extra.putSerializable("extra_result_data", mSourceDataList);
                LogF.d(TAG, "set result " + mSourceDataList.size());
            }
            extra.putBoolean("extra_has_more", hasMore);
            if (mLoadFirstTime > 0) {
                mSearchPos = mSourceDataList.size() - mSearchPos + 1;
                mMessageEditorLoadFinishCallback.onLoadFinished(state, mSearchPos, extra);
                mLoadFirstTime = 0;
                mSearchPos = 0;
            } else {
                mMessageEditorLoadFinishCallback.onLoadFinished(state, mSourceDataList.size(), extra);
            }
        }
    }

    private void calculateTime(List<Message> messages, int start, int end) {
        int size = messages.size();

        LogF.d(TAG, "calculateTime, count=" + size);

        long lastTime = -1;
        long mLastDate = -1;
        if (start > 0) {
            Message msg = (Message) messages.get(start - 1);
            mLastDate = msg.getDate();
            lastTime = msg.getDate();
        }

        int tenMin = 300000;// 10 * 30 * 1000;
        if(start < 0){
            start = 0;
        }
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

    private void calculateBubble(List<Message> messages, int start, int end) {
        int size = messages.size();
        if(start < 0){
            start = 0;
        }
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
            if ( nextMsg != null && (!sendAddress.equals(nextSendAddress)|| sendAddress.equals("")) ) {
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

                if (nextMsg != null && (isLeft != nextIsLeft) ) {
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
            LogF.d("calculateBubble","noup = " + noUp + " nodown = " + noDown);
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
