package com.cmicc.module_message.ui.constract;

import android.content.Intent;
import android.util.SparseBooleanArray;

import com.chinamobile.app.yuliao_business.model.Message;
import com.cmcc.cmrcs.android.ui.contracts.BasePresenter;
import com.cmcc.cmrcs.android.ui.contracts.BaseView;
import com.cmcc.cmrcs.android.ui.model.MediaItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tigger on 2018/5/7.
 */

public interface PcMessageContracts {
    interface View extends BaseView<Presenter> {

    }

    interface Presenter extends BasePresenter {
        void sendMessage(String msg);

        /**
         * 带消息字体
         * @param msg
         * @param size
         */
        void sendMessage(String msg, String size);
        void sendLocation(double dLatitude, double dLongitude, float fRadius, String pcLabel, String detailAddress);
        void sendWithdrawnMessage(Message msg);

        void deleteMessage(Message msg);

        void deleteMultiMessage(SparseBooleanArray selectList);

        void forwardMultiMessage(SparseBooleanArray selectList);

        void updateUnreadCount();

        void loadMoreMessages();

        void sendImgAndVideo(ArrayList<MediaItem> list, boolean isOriginPhoto);

        void sendFileMsg(Intent intent);

        void sendVcard(String pcUri, String pcSubject, String pcFileName, long duration) ;
        void reSend(Message msg);

        // 保存草稿
        void saveDraftMessage(boolean save, Message Msg);
        // 获取草稿
        Message getDraftMessage();

        void sendAudio(String path, long length);
        void sendAudio(String path, long length, String detail);

        String getAddress();

        void sendEditImage(String editImagePath);

         void addToFavorite(Message msg, int chatType, String address);
    }
}
