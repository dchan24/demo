package com.cmicc.module_message.ui.constract;

import android.widget.ImageView;

import com.cmcc.cmrcs.android.ui.contracts.BasePresenter;
import com.cmcc.cmrcs.android.ui.contracts.BaseView;

/**
 * Created by zhufang_lu on 2017/6/19.
 */

public interface GroupStrangerContract {
    interface View extends BaseView<Presenter> {
        void showNum(String name,String num);
        void setCompleteAddress(String completeAddress);
    }

    interface Presenter extends BasePresenter {
        void loadPhoto(ImageView imageView);
    }
}
