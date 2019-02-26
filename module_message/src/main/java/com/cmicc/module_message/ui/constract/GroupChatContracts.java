package com.cmicc.module_message.ui.constract;

import android.content.Intent;
import android.database.Cursor;
import android.util.SparseBooleanArray;

import com.chinamobile.app.yuliao_business.model.Message;
import com.cmcc.cmrcs.android.ui.contracts.BasePresenter;
import com.cmcc.cmrcs.android.ui.contracts.BaseView;
import com.cmcc.cmrcs.android.ui.model.MediaItem;

import java.util.ArrayList;

/**
 * Created by tigger on 2017/5/4.
 */

public interface GroupChatContracts {

    interface View extends BaseView<Presenter> {

        void updateChatListView(Cursor cursor, int loadType, int searchPos);

        void showTitleName(String person);
    }

    interface Presenter extends BasePresenter {

//        void sendMessage(String msg);

        /**
         * 字体大小
         *
         * @param msg
         * @param textSize
         */
        void sendMessage(String msg, String textSize);

        void sendMessageAt(String msg, String pcUri, String textSize);
        void sendMessageAtAll(String msg, String textSize);

        void sendWithdrawnMessage(Message msg);

        void deleteMessage(Message msg);

        /**
         * 多选功能, 删除
         * @param selectList
         */
        void deleteMultiMessage(SparseBooleanArray selectList);//

        /**
         * 多选功能， 转发
         * @param selectList
         */
        void forwardMultiMessage(SparseBooleanArray selectList);//

        void updateUnreadCount();

        void loadMoreMessages();

        void resend(Message msg);

        void addToFavorite(Message msg, int chatType, String address);

//        void sendImgAndVideo( ArrayList<MediaItem> list);

        void sendImgAndVideo(ArrayList<MediaItem> list, boolean isOriginalPhoto);

        //发送地理位置
        void sendLocation(double dLatitude, double dLongitude, float fRadius, String pcLabel, String detailAddress);

        void sendFileMsg(Intent intent);

        void sendVcard(String pcUri, String pcSubject, String pcFileName, long duration);

        // 保存草稿
        void saveDraftMessage(boolean save, Message Msg);
        // 获取草稿
        Message getDraftMessage();

        void sendAudio(String path, long length);
        void sendAudio(String path, long length, String detail);

        String getAddress();

        void clearAllMsg();

        void sendEditImage(String editImagePath);

        void startInitLoader(ArrayList<Message> list, long loadStartTime, long updateTime);
    }
}
