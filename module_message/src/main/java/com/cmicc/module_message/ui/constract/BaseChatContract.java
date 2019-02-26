package com.cmicc.module_message.ui.constract;

import android.os.Bundle;
import android.util.SparseBooleanArray;

import com.chinamobile.app.yuliao_business.model.Message;
import com.cmcc.cmrcs.android.ui.contracts.BasePresenter;
import com.cmcc.cmrcs.android.ui.contracts.BaseView;
import com.cmcc.cmrcs.android.ui.model.MediaItem;

import java.util.ArrayList;

/**
 * Created by tigger on 2017/5/5.
 */

public interface BaseChatContract {
    interface View extends BaseView<Presenter> {

        void updateChatListView(int loadType, int searchPos, Bundle bundle);

        void showTitleName(CharSequence person);

        void reSend(Message msg);

        void sendWithdrawnMessage(Message msg);

        void moveToOffsetEnd();

        void showToast(String toast);

        void finish();

        void deleteMessage(Message msg);

        void deleteMultiMessage(SparseBooleanArray selectList);

        void forwardMultiMessage(SparseBooleanArray selectList);

        void addToFavorite(Message msg, int chatType, String address);

        void sysMessage( int type );

        void hideSuperMsg();

        void sendSuperMessage(String msg);

        void cleanSMSContent();

        void hideToolBarMenu();

        void showToolBarMenu();
    }

    interface Presenter extends BasePresenter {
        void reSend(Message msg);

        void sendWithdrawnMessage(Message msg);

        void reSendImgAndVideo( ArrayList<MediaItem> list);

        void moveToOffsetEnd();

        void deleteMessage(Message msg);

        void deleteMultiMessage(SparseBooleanArray selectList);

        void forwardMultiMessage(SparseBooleanArray selectList);

        void addToFavorite(Message msg, int chatType, String address);

        void sysMessage( int type );

        void sendSuperMessage(String msg);
    }
}