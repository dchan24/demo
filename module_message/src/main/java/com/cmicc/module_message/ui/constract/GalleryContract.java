package com.cmicc.module_message.ui.constract;

import com.cmcc.cmrcs.android.ui.contracts.BasePresenter;
import com.cmcc.cmrcs.android.ui.contracts.BaseView;
import com.cmcc.cmrcs.android.ui.model.MediaItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GuoXietao on 2017/3/27.
 */

public interface GalleryContract {

    interface View extends BaseView<Presenter> {
        void updateView();

        void initToolBar();

        void updateButton(int number);
        void showCompressProgressBar();
    }

    interface Presenter extends BasePresenter {
        void getMediaList();

        void setView(View v);

        void handleSend(android.view.View v);

        void handlePreviewClicked(android.view.View v);

        void handleImageClicked(android.view.View v, int pos);

        void handleSelect(int pos);

        ArrayList<MediaItem> getSelectItems();

        void onDestroy();

        void handlePhotoModeChange(boolean isChecked);

        void setSelectMediaList(ArrayList<MediaItem> list); // 设置当前选中的图片文件夹
    }
}
