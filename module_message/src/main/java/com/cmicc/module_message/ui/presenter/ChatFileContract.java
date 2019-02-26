package com.cmicc.module_message.ui.presenter;

import com.chinamobile.app.yuliao_business.model.Message;
import com.cmcc.cmrcs.android.ui.adapter.headerrecyclerview.PinnedHeaderEntity;
import com.cmcc.cmrcs.android.ui.contracts.BasePresenter;
import com.cmcc.cmrcs.android.ui.model.MediaItem;

import java.util.List;

/**
 * @anthor situ
 * @time 2017/6/8 18:51
 * @description 聊天文件
 */

public interface ChatFileContract {

    interface IView {

        void updateRecyclerView(List<PinnedHeaderEntity<MediaItem>> list);

    }

    interface IPresenter extends BasePresenter {
        void getData();
        boolean collect(Message message);
    }
}
