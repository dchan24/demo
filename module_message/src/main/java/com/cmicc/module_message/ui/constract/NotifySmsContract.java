package com.cmicc.module_message.ui.constract;

import android.content.Context;

import com.chinamobile.app.yuliao_business.model.Conversation;
import com.cmcc.cmrcs.android.ui.contracts.BasePresenter;
import com.cmcc.cmrcs.android.ui.contracts.BaseView;
import com.cmcc.cmrcs.android.ui.utils.ConvCache;

/**
 * Created by tigger on 2017/7/27.
 */

public interface NotifySmsContract {
    interface View extends BaseView<NotifySmsContract.Presenter>{
        void notifyDataSetChanged();
    }

    interface Presenter extends BasePresenter {
        void openItem(Context context, Conversation conversation);
        ConvCache.CacheType getCacheType();
    }
}
