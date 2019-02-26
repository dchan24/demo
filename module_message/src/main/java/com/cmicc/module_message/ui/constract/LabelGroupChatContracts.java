package com.cmicc.module_message.ui.constract;

import android.content.Intent;
import android.database.Cursor;
import android.util.SparseBooleanArray;

import com.chinamobile.app.yuliao_business.model.Message;
import com.cmcc.cmrcs.android.ui.contracts.BasePresenter;
import com.cmcc.cmrcs.android.ui.contracts.BaseView;
import com.cmcc.cmrcs.android.ui.model.MediaItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cq on 2018/5/10.
 */

public interface LabelGroupChatContracts {
    interface View extends BaseView<LabelGroupChatContracts.Presenter> {

        void updateChatListView(Cursor cursor, int loadType, int searchPos);

        void showTitleName(String person);
    }

    interface Presenter extends BasePresenter {

        /**
         * 字体大小
         * @param msg
         * @param textSize
         */
        void sendMessage(String msg, String textSize);

        void sendWithdrawnMessage(Message msg);

        void deleteMessage(Message msg);

        void deleteMultiMessage(SparseBooleanArray selectList);

        void forwardMultiMessage(SparseBooleanArray selectList);

        void updateUnreadCount();

        void loadMoreMessages();

        void resend(Message msg);

        void sendImgAndVideo(ArrayList<MediaItem> list, boolean isOriginalPhoto);

        void sendFileMsg(Intent intent);

        void sendVcard(String pcUri, String pcSubject, String pcFileName, long duration);

        // 保存草稿
        void saveDraftMessage(boolean save, Message Msg);
        // 获取草稿
        Message getDraftMessage();

        void sendAudio(String path, long length);
        void sendAudio(String path, long length, String detail);

        String getAddress();

        void addToFavorite(Message msg, int chatType, String address);

        void sendEditImage(String editImagePath);

        void sendLocation(double dLatitude, double dLongitude, float fRadius, String pcLabel, String detailAddress);
    }
}
