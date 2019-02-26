package com.cmicc.module_message.ui.constract;

import android.os.Bundle;
import android.view.SurfaceHolder;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.cmcc.cmrcs.android.ui.contracts.BasePresenter;
import com.cmcc.cmrcs.android.ui.contracts.BaseView;

/**
 * Created by Tiu on 2017/7/7.
 */

public interface MessageVideoContract {
    interface View extends BaseView<Presenter> {
        SurfaceHolder getSurfaceHolder();

        Bundle getDataSource();

        void setControlViewPlay(boolean isplay);

        SeekBar getSeekBar();

        void updateCurrentTextView(String format);

        void updateEndPositionTextView(String format);

        void showFrame(boolean b);

        void updateSurfaceView(FrameLayout.LayoutParams layoutParams);

    }

    interface Presenter extends BasePresenter {
        void handleControl();

        void onDestroy();

        void saveVideo(Bundle bundle);

        void showDialog(Bundle bundle);
    }
}
