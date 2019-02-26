package com.cmicc.module_message.ui.constract;


import com.amap.api.maps2d.model.LatLng;
import com.cmicc.module_message.ui.data.PoiListBeanData;

import java.util.ArrayList;
import java.util.Map;

public class LocationConstract {
    public interface IView{
        public void showChooseAddress(String title,String detail);
        public void setDataList(ArrayList<PoiListBeanData> list);
        public void addDataList(ArrayList<PoiListBeanData> list);
        public void showProgress(boolean show);
        public void onAllPoiLoaded();
        public void selectedLocationPos(boolean selected);
    }

    public interface IPresenter{
        public void location();
        public void onDrestroy();
        public void backToLocationPosition();
        public LatLng getLatLng();
        public void setOnlyShowMap(boolean onlyShowMap);
        public void moveToLatlng(LatLng latlng,boolean justMove);
        public void loadMorePoiData();
        public void searchMap(String keyWord);
        public String getLocationPosCityName();
    }
}
