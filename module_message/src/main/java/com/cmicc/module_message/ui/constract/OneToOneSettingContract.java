package com.cmicc.module_message.ui.constract;

import com.cmcc.cmrcs.android.ui.contracts.BasePresenter;
import com.cmcc.cmrcs.android.ui.contracts.BaseView;

/**
 * Created by GuoXietao on 2017/3/31.
 */

public interface OneToOneSettingContract {

    interface View extends BaseView<Presenter> {
        void setUndisturbSwitch(boolean checked);
        void setupThemeThumb();
        void updateUndisturbFinish(boolean isOk);
    }

    interface Presenter extends BasePresenter {
        boolean getUndisturbSetting(String address);

        void setUndisturbSettingLocal(String address, boolean disturb);

        void setUndisturbSettingServer(String address, String status);

    }
}
