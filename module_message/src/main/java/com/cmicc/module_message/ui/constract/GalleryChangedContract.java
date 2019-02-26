package com.cmicc.module_message.ui.constract;

import android.os.Bundle;

import com.cmcc.cmrcs.android.ui.contracts.BasePresenter;
import com.cmcc.cmrcs.android.ui.contracts.BaseView;
import com.cmcc.cmrcs.android.ui.model.MediaItem;

import java.util.ArrayList;

/**
 * Created by GuoXietao on 2017/4/10.
 */

public interface GalleryChangedContract {


    interface View extends BaseView<Presenter> {
        void updateSelectNumber(int number);

        void setPresenter(Presenter p);

        void setSelect(MediaItem mediaItem);

        void setSmoothImage(Bundle bundle);

        void onBackPressed();

        void showIvPlay(boolean isShow);
    }

    interface Presenter extends BasePresenter {
        void setAllMediaItems(ArrayList<MediaItem> allMediaItems);

        ArrayList<MediaItem> getMediaItems();

        ArrayList<MediaItem> getSelectItems();

        void handleSelect(boolean shouldAdd);

        void handlePageSelected(int position);

        void handlePhotoClick();

        int getCurrentPosition();

        void setCurrentPosition(int position);

        void setView(GalleryChangedContract.View view);

        void setPreviewSelect(Boolean previewSelect);

        Boolean getPreviewSelect();

        void onDestroy();

        int getMediaType();

        boolean isFilebroken(int position);

        void handleEditImage();

        void startPlay();
    }
}
