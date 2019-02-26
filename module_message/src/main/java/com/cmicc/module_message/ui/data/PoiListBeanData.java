package com.cmicc.module_message.ui.data;

public class PoiListBeanData {
    public static final int TYPE_NORMAL_POI_DATA = 0;
    public static final int TYPE_FOOTER_VIEW_DATA = 1;
    public double mLatitude;
    public double mLongtitude;
    public String mTitleAddress;
    public String mDetailAddress;
    public int type = TYPE_NORMAL_POI_DATA;
}
