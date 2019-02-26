package com.cmicc.module_message.ui.presenter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;

import com.chinamobile.app.utils.TimeUtil;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_business.model.YunFile;
import com.chinamobile.app.yuliao_business.provider.Conversations;
import com.chinamobile.app.yuliao_business.util.Status;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_core.util.NumberUtils;
import com.cmcc.cmrcs.android.ui.adapter.headerrecyclerview.BaseHeaderAdapter;
import com.cmcc.cmrcs.android.ui.adapter.headerrecyclerview.PinnedHeaderEntity;
import com.cmcc.cmrcs.android.ui.model.MediaItem;
import com.cmcc.cmrcs.android.ui.model.MediaSet;
import com.cmcc.cmrcs.android.ui.utils.FavoriteUtil;
import com.cmcc.cmrcs.android.ui.utils.YunFileXmlParser;
import com.cmicc.module_message.ui.activity.ChatFileActivity;
import com.constvalue.MessageModuleConst;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @anthor situ
 * @time 2017/6/8 19:02
 * @description 聊天文件
 */

public class ChatFilePresenter implements ChatFileContract.IPresenter, LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ChatFilePresenter";

    public static final int LOADER_ID = new Random(System.currentTimeMillis()).nextInt();

    private Context mContext;
    private ChatFileContract.IView mView;
    private String mAddress;
    private int mBoxType;
    private LoaderManager mLoaderManager;
    private MediaSet mMediaSet;
    private List<PinnedHeaderEntity<MediaItem>> mFileList;
    public int mChatType = -1;
    private boolean mIsImgVideo;

    public ChatFilePresenter(Context context, ChatFileContract.IView view, LoaderManager loaderManager, Bundle bundle) {
        mContext = context;
        mView = view;
        mAddress = bundle.getString(ChatFileActivity.BUNDLE_KEY_ADDRESS);
        mBoxType = bundle.getInt(ChatFileActivity.BUNDLE_KEY_BOXTYPE);
        mIsImgVideo = bundle.getBoolean(ChatFileActivity.BUNDLE_KEY_IS_IMG_VIDEO);
        mLoaderManager = loaderManager;
    }

    @Override
    public void start() {
        mLoaderManager.initLoader(LOADER_ID, null, this);
    }

    @Override
    public void getData() {
        mLoaderManager.restartLoader(LOADER_ID, null, this);
    }

    @Override
    public boolean collect(Message message) {
        message.setBoxType(mBoxType);
        return FavoriteUtil.getInstance().addToFavorite(mContext, message, mChatType, mAddress);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        String ad = mAddress;
        Uri uri = Conversations.Message.CONTENT_URI;
        if (mBoxType == Type.TYPE_BOX_MESSAGE) {
            ad = NumberUtils.getPhone(mAddress);
            uri = Conversations.Message.CONTENT_URI;
            mChatType = MessageModuleConst.MessageChatListAdapter.TYPE_SINGLE_CHAT;
        } else {
            uri = Conversations.Group.CONTENT_URI;
            mChatType = MessageModuleConst.MessageChatListAdapter.TYPE_GROUP_CHAT;
        }
        String whereAddress = Conversations.buildWhereAddress(ad);
        String selection_type = "";
        String[] selectionArgs = null;
        if(mIsImgVideo){
            selection_type = " AND (" + Message.COLUMN_NAME_TYPE + "=? OR " + Message.COLUMN_NAME_TYPE + "=? OR " + Message.COLUMN_NAME_TYPE + "=? OR "
                    + Message.COLUMN_NAME_TYPE + "=? OR " + Message.COLUMN_NAME_TYPE + "=? OR " + Message.COLUMN_NAME_TYPE + "=? ) ";
            selectionArgs = new String[]{Type.TYPE_MSG_IMG_RECV + "", Type.TYPE_MSG_IMG_SEND + "", Type.TYPE_MSG_IMG_SEND_CCIND + "",
                    Type.TYPE_MSG_VIDEO_RECV + "", Type.TYPE_MSG_VIDEO_SEND + "", Type.TYPE_MSG_VIDEO_SEND_CCIND + ""};
        }else{
            selection_type = " AND (" + Message.COLUMN_NAME_TYPE + "=? OR " + Message.COLUMN_NAME_TYPE + "=? OR " + Message.COLUMN_NAME_TYPE + "=? OR "
                    + Message.COLUMN_NAME_TYPE + "=? OR " + Message.COLUMN_NAME_TYPE + "=? ) ";
            selectionArgs = new String[]{
                    Type.TYPE_MSG_FILE_YUN_SEND + "", Type.TYPE_MSG_FILE_SEND + "", Type.TYPE_MSG_FILE_SEND_CCIND + "",
                    Type.TYPE_MSG_FILE_YUN_RECV + "", Type.TYPE_MSG_FILE_RECV + ""};
        }
        CursorLoader loader = new CursorLoader(mContext,
                uri,
                new String[]{Message.COLUMN_NAME_EXT_SIZE_DESCRIPT,Message.COLUMN_NAME_PERSON,Message.COLUMN_NAME_ID, Message.COLUMN_NAME_EXT_FILE_NAME, Message.COLUMN_NAME_EXT_FILE_PATH, Message.COLUMN_NAME_EXT_THUMB_PATH, Message.COLUMN_NAME_THREAD_ID,
                        Message.COLUMN_NAME_DATE, Message.COLUMN_NAME_TYPE, Message.COLUMN_NAME_EXT_DOWN_SIZE, Message.COLUMN_NAME_EXT_FILE_SIZE, Message.COLUMN_NAME_EXT_SHORT_URL, Message.COLUMN_NAME_STATUS,
                        Message.COLUMN_NAME_LOCKED, Type.TYPE_BOX_MESSAGE + " as " + Message.COLUMN_NAME_BOX_TYPE, Message.COLUMN_NAME_SEND_ADDRESS, Message.COLUMN_NAME_MSG_ID,
                        Message.COLUMN_NAME_EXT_SIZE_DESCRIPT, Message.COLUMN_NAME_BODY, Message.COLUMN_NAME_ADDRESS_ID},
                whereAddress + selection_type, selectionArgs,
                Message.COLUMN_NAME_DATE + " DESC");
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mMediaSet = new MediaSet();
        if (mFileList == null) {
            mFileList = new ArrayList<PinnedHeaderEntity<MediaItem>>();
        }
        mFileList.clear();
        String lastItemDate = "";
        String formatDate = "";
        boolean isSuccess = cursor.moveToFirst();
        if (isSuccess) {
            do{
                MediaItem item = new MediaItem(cursor.getString(cursor.getColumnIndex(Message.COLUMN_NAME_EXT_FILE_PATH)), -1);
                item.setThumbPath(cursor.getString(cursor.getColumnIndex(Message.COLUMN_NAME_EXT_THUMB_PATH)));
                item.setTag(cursor.getString(cursor.getColumnIndex(Message.COLUMN_NAME_THREAD_ID)));
                item.setDownSize(cursor.getLong(cursor.getColumnIndex(Message.COLUMN_NAME_EXT_DOWN_SIZE)));
                item.setFileLength(cursor.getLong(cursor.getColumnIndex(Message.COLUMN_NAME_EXT_FILE_SIZE)));
                item.setFileName(cursor.getString(cursor.getColumnIndex(Message.COLUMN_NAME_EXT_FILE_NAME)));
                item.setMessageType(cursor.getInt(cursor.getColumnIndex(Message.COLUMN_NAME_TYPE)));
                item.setmPerson(cursor.getString(cursor.getColumnIndex(Message.COLUMN_NAME_PERSON)));
                item.setSelected(true);
                item.setmExtSizeDescript(cursor.getString(cursor.getColumnIndex(Message.COLUMN_NAME_EXT_SIZE_DESCRIPT)));
                item.setThreadId(cursor.getString(cursor.getColumnIndex(Message.COLUMN_NAME_EXT_SHORT_URL)));
                item.setAddress(mAddress);
                item.setSendAddress(cursor.getString(cursor.getColumnIndex(Message.COLUMN_NAME_SEND_ADDRESS)));
                int id = cursor.getInt(cursor.getColumnIndex(Message.COLUMN_NAME_ID));
                item.setID(id);
                item.setLocalPath(cursor.getString(cursor.getColumnIndex(Message.COLUMN_NAME_EXT_FILE_PATH)));
                item.setStatus(cursor.getInt(cursor.getColumnIndex(Message.COLUMN_NAME_STATUS)));
                item.setLocked(cursor.getInt(cursor.getColumnIndex(Message.COLUMN_NAME_LOCKED)));
                item.setBoxType(cursor.getInt(cursor.getColumnIndex(Message.COLUMN_NAME_BOX_TYPE)));
                item.setMsgId(cursor.getString(cursor.getColumnIndex(Message.COLUMN_NAME_MSG_ID)));
                long date = cursor.getLong(cursor.getColumnIndex(Message.COLUMN_NAME_DATE));
                item.setDate(date);
                if (item.getStatus() == Status.STATUS_DELETE) {
                    continue;
                }

                switch (item.getMessageType()) {
                    case Type.TYPE_MSG_IMG_RECV:
                    case Type.TYPE_MSG_IMG_SEND:
                    case Type.TYPE_MSG_IMG_SEND_CCIND:
                        item.setMediaType(MediaItem.MEDIA_TYPE_IMAGE);
                        break;
                    case Type.TYPE_MSG_VIDEO_RECV:
                    case Type.TYPE_MSG_VIDEO_SEND:
                    case Type.TYPE_MSG_VIDEO_SEND_CCIND:
                        item.setMediaType(MediaItem.MEDIA_TYPE_VIDEO);
                        // 时长
                        int seconds = Integer.parseInt(cursor.getString(cursor.getColumnIndex(Message.COLUMN_NAME_EXT_SIZE_DESCRIPT)));
                        item.setDuration(seconds);
                        break;
                    case Type.TYPE_MSG_FILE_YUN_SEND:
                    case Type.TYPE_MSG_FILE_SEND:
                    case Type.TYPE_MSG_FILE_SEND_CCIND:
                    case Type.TYPE_MSG_FILE_YUN_RECV:
                    case Type.TYPE_MSG_FILE_RECV:
                        item.setMediaType(MediaItem.MEDIA_TYPE_FILE);
                        item.setAddressId(cursor.getInt(cursor.getColumnIndex(Message.COLUMN_NAME_ADDRESS_ID)));
                        int type = item.getMessageType();
                        if (type == Type.TYPE_MSG_FILE_SEND || type == Type.TYPE_MSG_FILE_RECV) {
                            item.setFileLength(item.getFileLength());
                        } else {
                            String body = cursor.getString(cursor.getColumnIndex(Message.COLUMN_NAME_BODY));
                            item.setMessageBody(body);
                            YunFile file = YunFileXmlParser.parserYunFileXml(body);
                            if (file == null) {
                                return;
                            }
                            if(TextUtils.isEmpty(item.getFileName())){
                                item.setFileName(file.getFileName());
                            }
                            item.setFileLength(file.getFileSize());
                        }
                        break;
                    default:
                }

                mMediaSet.addMediaItem(item);

                formatDate = TimeUtil.formatChatFileTime(date);
                if (!lastItemDate.equals(formatDate)) {
                    PinnedHeaderEntity<MediaItem> pinnedHeaderEntity = new PinnedHeaderEntity<MediaItem>(null, BaseHeaderAdapter.TYPE_HEADER, formatDate);
                    mFileList.add(pinnedHeaderEntity);
                    lastItemDate = formatDate;
                }

                PinnedHeaderEntity<MediaItem> pinnedHeaderEntity = new PinnedHeaderEntity<MediaItem>(item, item.getMediaType(), formatDate);
                mFileList.add(pinnedHeaderEntity);
            } while(cursor.moveToNext());
        }else{
            LogF.e(TAG, "moveToFirst failed");
        }
        mView.updateRecyclerView(mFileList);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}
