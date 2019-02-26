package com.cmicc.module_message.ui.constract;

import android.content.Intent;
import android.database.Cursor;

import com.chinamobile.app.yuliao_business.model.Message;
import com.cmcc.cmrcs.android.ui.contracts.BasePresenter;
import com.cmcc.cmrcs.android.ui.contracts.BaseView;
import com.cmcc.cmrcs.android.ui.model.MediaItem;

import java.util.ArrayList;

/**
 * Created by xyz on 2017/7/7.
 */

public interface MmsSmsEditorContracts {

    interface View extends BaseView<Presenter> {
        void updateChatListView(Cursor cursor, int loadType, int searchPos);

        void showTitleName(String person);
    }

    interface Presenter extends BasePresenter {

        void sendSuperMessage(String message, String size);

        void sendMessage(String msg);

        /**
         * 带消息字体
         * @param msg
         * @param size
         */
        void sendMessage(String msg, String size);

        void sendWithdrawnMessage(Message msg);

        void deleteMessage(Message msg);

        void updateUnreadCount();

        void loadMoreMessages();

        void sendImgAndVideo(ArrayList<MediaItem> list);

        void sendFileMsg(Intent intent);

        void sendVcard(String pcUri, String pcSubject, String pcFileName, long duration) ;
        void reSend(Message msg);

        // 保存草稿
        void saveDraftMessage(boolean save, Message Msg);
        // 获取草稿
        Message getDraftMessage();

        void sendAudio(String path);

    }

}
