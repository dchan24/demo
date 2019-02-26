package com.cmicc.module_message.ui.constract;

import com.chinamobile.app.yuliao_business.model.Message;
import com.cmcc.cmrcs.android.ui.contracts.BasePresenter;
import com.cmcc.cmrcs.android.ui.contracts.BaseView;
import com.cmcc.cmrcs.android.ui.model.MediaItem;

import java.util.ArrayList;

/**
 * @anthor situ
 * @time 2017/7/7 14:52
 * @description 公众号会话界面s
 */

public interface PublicAccountChatContract {
    interface View extends BaseView<GroupChatContracts.Presenter> {

    }

    interface Presenter extends BasePresenter {

        void init();

        void updateUnreadCount();

        void loadMoreMessages();

        void reSend(Message msg);

        void deleteMessage(Message msg);

        void addToFavorite(Message msg, int chatType, String address);

        void sendMessage(String message);

        void sendAudioMessage(String path, long length);
        void sendAudioMessage(String path, long length, String detail);

        void sendImgAndVideo(ArrayList<MediaItem> list, boolean isOriginPhoto);

        void sendVcard(String pcUri, String pcSubject, String pcFileName, long duration) ;

        void clearAllMsg();

        public void sendEditImage(String imagePath);
    }

}
