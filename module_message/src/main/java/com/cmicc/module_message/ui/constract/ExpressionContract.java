package com.cmicc.module_message.ui.constract;

import com.cmcc.cmrcs.android.widget.emoji.EmojiEntity;

import java.util.List;

/**
 * Created by GuoXietao on 2017/5/3.
 */

public interface ExpressionContract {

    interface IView {
        void setPresenter(IPresenter p);

        void updateView();
    }

    interface IPresenter {
        void setView(IView v);

        void initData();

        List<EmojiEntity> getEmojiArray();

        int getPageCount();

        void initEmojiBData();

    }

}
