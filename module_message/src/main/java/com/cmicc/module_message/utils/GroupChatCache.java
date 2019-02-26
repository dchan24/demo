package com.cmicc.module_message.utils;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import com.chinamobile.app.utils.RxAsyncHelper;
import com.chinamobile.app.utils.TimeUtil;
import com.chinamobile.app.yuliao_business.model.BaseModel;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_business.provider.Conversations;
import com.chinamobile.app.yuliao_business.provider.Conversations.Group;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.utils.Log;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.cmicc.module_message.ui.model.impls.MessageEditorModelImpl;
import com.cmcc.cmrcs.android.ui.utils.MessageCursorLoader;
import com.cmcc.cmrcs.android.ui.utils.MulitCursorLoader.SortCursor;

import java.util.ArrayList;
import java.util.List;

import rx.functions.Func1;

import static com.constvalue.MessageModuleConst.MessageChatListAdapter.BUBBLE_NOUP_DOWN;
import static com.constvalue.MessageModuleConst.MessageChatListAdapter.BUBBLE_NOUP_NODOWN;
import static com.constvalue.MessageModuleConst.MessageChatListAdapter.BUBBLE_UP_DOWN;
import static com.constvalue.MessageModuleConst.MessageChatListAdapter.BUBBLE_UP_NODOWN;
import static com.constvalue.MessageModuleConst.MessageChatListAdapter.VIEW_SHOW_DATE_FLAG;
import static com.constvalue.MessageModuleConst.MessageChatListAdapter.VIEW_SHOW_TIME_FLAG;
import static com.cmicc.module_message.ui.model.impls.MessageEditorModelImpl.NUM_LOAD_ONE_TIME;

public class GroupChatCache {
    public static final String TAG = "GroupChatCache";

    public interface OnLoadFinishListener {
        public void onLoadFinished(int loadType, int searchPos, long updateTime, long loadStartTime, Bundle bundle);
    }

    private static GroupChatCache mGroupChatCache = null;

    private int prepardId = 0;
    private GroupChatCache.Status status = GroupChatCache.Status.PENDING;
    private GroupChatCache.OnLoadFinishListener mListener;

    private Bundle mExtra;
    private String mAddress;

    private enum Status {
        /**
         * Indicates that the task has not been executed yet.
         */
        PENDING,
        /**
         * Indicates that the task is running.
         */
        RUNNING,
        /**
         * Indicates that loadTask has finished.
         */
        FINISHED,
    }

    private GroupChatCache() {
    }

    public static GroupChatCache getInstance() {
        synchronized (GroupChatCache.class) {
            if (mGroupChatCache == null) {
                mGroupChatCache = new GroupChatCache();
            }
            return mGroupChatCache;
        }
    }

    public int prepareGroupChatCache(final Context context, final String address, final Bundle bundle) {
        final int pId = ++prepardId;
        Log.d(TAG, "----prepareGroupChatCache------" + address + " pId = " + pId);

        status = GroupChatCache.Status.RUNNING;
        mListener = null;
        RxAsyncHelper helper = new RxAsyncHelper("");
        helper.runInThread(new Func1<Object, Bundle>() {
            @Override
            public Bundle call(Object o) {
                long loadFirstTime = bundle.getLong("loadtime", 0);
                int firstLoadNum = bundle.getInt("unread", 0);
                int messageLoadCount = NUM_LOAD_ONE_TIME;
                int searchPos = 0;
                if (firstLoadNum > 0) {
                    messageLoadCount = Math.round(firstLoadNum / NUM_LOAD_ONE_TIME + 0.5f) * NUM_LOAD_ONE_TIME;
                }
                if (loadFirstTime > 0) {
                    String where = Conversations.buildWhereAddress(address);
                    // 过滤掉草稿 & 记住上次加载的消息中的最早时间
                    where = where + " AND type<>3 AND date>=" + loadFirstTime;
                    Cursor cursor = context.getContentResolver().query(Conversations.Group.CONTENT_URI, new String[]{"_id"}, where, null, null);
                    if (cursor != null) {
                        searchPos = cursor.getCount();
                        messageLoadCount = messageLoadCount + cursor.getCount();
                        cursor.close();
                    }
                }

                String where = Conversations.buildWhereAddress(address);
                where = where + " AND type<>3 AND date>=0 AND update_time>-100";
                String order = String.format(Conversations.DATE_DESC_LIMIT, messageLoadCount);
                String[] projection = new String[]{
                        "_id", Type.TYPE_BOX_GROUP + " AS box_type", "msg_id", "address", "send_address", "person", "body",
                        "date", "update_time", "type", "status", "locked", "error_code", "ext_url", "ext_title", "ext_file_name", "ext_file_path", "ext_size_descript", "ext_file_size", "ext_down_size", "ext_thumb_path",
                        "thread_id", "read", "ext_file_size", "ext_down_size", "ext_short_url", "seen", "notify_date", "title", "identify", BaseModel.COLUMN_NAME_PA_UUID, BaseModel.COLUMN_NAME_XML_CONTENT,
                        BaseModel.COLUMN_NAME_ANIM_ID, BaseModel.COLUMN_NAME_AUTHOR, BaseModel.COLUMN_NAME_SUB_IMG_PATH, BaseModel.COLUMN_NAME_SUB_ORIGIN_LINK, BaseModel.COLUMN_NAME_SUB_SOURCE_LINK,
                        BaseModel.COLUMN_NAME_SUB_TITLE, BaseModel.COLUMN_NAME_SUB_URL, BaseModel.COLUMN_NAME_SUB_BODY, BaseModel.COLUMN_NAME_ADDRESS_ID, BaseModel.COLUMN_TEXT_SIZE, BaseModel.COLUMN_NAME_AT_LIST, "ext_status",
                        BaseModel.COLUMN_EXACT_READ, BaseModel.COLUMN_EXD_SEND_STATUS};
                Cursor cursor = context.getContentResolver().query(Group.CONTENT_URI, projection, where, null, order);
                cursor = new SortCursor(cursor, false);

                Bundle extra = new Bundle();
                extra.putInt("searchPos", searchPos);
                extra.putInt("messageLoadCount", messageLoadCount);
                extra.putLong("loadFirstTime", loadFirstTime);

                changeCursorToData(cursor, extra);
                return extra;
            }
        }).runOnMainThread(new Func1<Bundle, Object>() {
            @Override
            public Object call(Bundle extra) {
                if (pId == prepardId) {
                    LogF.i(TAG, "----prepareGroupChatCache over------" + address);

                    mExtra = extra;
                    status = Status.FINISHED;
                    mAddress = address;
                    if (mListener != null) {
                        getGroupChatCache(mListener, pId);
                        mListener = null;
                        mExtra = null;
                        status = Status.PENDING;
                    }
                }
                return null;
            }
        }).subscribe();

        return pId;
    }

    //将cursor转化为使用的数据类型
    private void changeCursorToData(Cursor cursor, Bundle extra) {
        long currentUpdateTime = -100; //最近更新时间
        long globalLoadStartTime = 0; //数据源第一条数据的
        List<Message> cacheList = new ArrayList();

        if (cursor == null) {
            return;
        }

        if (cursor.moveToFirst()) {
            do {
                Message value = (Message) MessageCursorLoader.getValueFromCursor(cursor, Message.class, true);
                cacheList.add(value);
            } while (cursor.moveToNext());
            cursor.moveToFirst();
        } else {
            LogF.e(TAG, "changeCursorToData mCursor is empty ..");
        }

        calculateTime(cacheList, 0, cacheList.size() - 1);
        calculateBubble(cacheList, 0, cacheList.size() - 1);

        //更新最近更新时间
        for (Message msg : cacheList) {
            if (msg.getUpdateTime() > currentUpdateTime) {
                currentUpdateTime = msg.getUpdateTime();
            }
        }

        if (cacheList.size() > 0) {
            globalLoadStartTime = cacheList.get(0).getDate();
        } else {
            globalLoadStartTime = 0;
        }
        extra.putSerializable("extra_result_data", (ArrayList<Message>) cacheList);
        extra.putLong("globalLoadStartTime", globalLoadStartTime);
        extra.putLong("currentUpdateTime", currentUpdateTime);
    }

    public void getGroupChatCache(GroupChatCache.OnLoadFinishListener listener, int pId) {
        Log.d(TAG, "----getGroupChatCache------" + mAddress + " pId = " + pId);

        if (pId == prepardId) {
            if (status == GroupChatCache.Status.FINISHED) {
                Bundle extra = new Bundle();
                boolean hasMore = true;
                ArrayList<Message> cacheList = (ArrayList<Message>) mExtra.getSerializable("extra_result_data");
                int messageLoadCount = mExtra.getInt("messageLoadCount", 0);
                long loadFirstTime = mExtra.getLong("loadFirstTime", 0L);
                int searchPos = mExtra.getInt("searchPos", 0);
                long globalLoadStartTime = mExtra.getLong("globalLoadStartTime", 0l);
                long currentUpdateTime = mExtra.getLong("currentUpdateTime", 0l);
                if (messageLoadCount > cacheList.size()) {
                    hasMore = false;
                }

                if (cacheList != null) {
                    extra.putSerializable("extra_result_data", cacheList);
                    Log.d(TAG, "set result " + cacheList.size());
                }
                extra.putBoolean("extra_has_more", hasMore);
                if (loadFirstTime > 0) {
                    searchPos = cacheList.size() - searchPos + 1;
                    listener.onLoadFinished(MessageEditorModelImpl.LOAD_TYPE_FIRST, searchPos, currentUpdateTime, globalLoadStartTime, extra);
                } else {
                    listener.onLoadFinished(MessageEditorModelImpl.LOAD_TYPE_FIRST, cacheList.size(), currentUpdateTime, globalLoadStartTime, extra);
                }

                mListener = null;
                mExtra = null;
                status = Status.PENDING;
            } else {
                mListener = listener;
            }
        }
    }

    private void calculateTime(List<Message> messages, int start, int end) {
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

    private void calculateBubble(List<Message> messages, int start, int end) {
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
            if ((nextMsg != null && (!sendAddress.equals(nextSendAddress) || sendAddress.equals("") || isIgnore(nextMsg))) || isIgnore(msg)) {
                msg.setBigMargin(true);
            } else {
                msg.setBigMargin(false);
            }

            if(isIgnore(msg) || (nextMsg != null && isIgnore(nextMsg)) ){
                msg.setSmallPadding(true);
            } else {
                msg.setSmallPadding(false);
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

                if (nextMsg != null && (isLeft != nextIsLeft || isIgnore(nextMsg)) || isIgnore(msg)) {
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

            if (preMsg == null) {
                msg.setIsFirst(true);
            } else {
                msg.setIsFirst(false);
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
