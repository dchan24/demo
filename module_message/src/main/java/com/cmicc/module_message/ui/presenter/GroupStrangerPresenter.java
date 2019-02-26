package com.cmicc.module_message.ui.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.ImageView;

import com.chinamobile.app.yuliao_core.util.NumberUtils;
import com.cmcc.cmrcs.android.glide.GlidePhotoLoader;
import com.cmicc.module_message.ui.constract.GroupStrangerContract;

/**
 * Created by zhufang_lu on 2017/6/19.
 */

public class GroupStrangerPresenter implements GroupStrangerContract.Presenter {
    private String TAG = "GroupStrangerPresenter";
    private Context mContext;
    private GroupStrangerContract.View mView ;
    private String mNum;
    private String mName;
    private String completeAddress ;
    public GroupStrangerPresenter(GroupStrangerContract.View view,Context context){
        this.mContext = context;
        this.mView = view;
    }

    /**
     * 显示名字
     * 显示手机号码
     */
    @Override
    public void start() {
        Intent intent = ((Activity)mContext).getIntent();
        if(intent==null){
            return;
        }
        mNum = intent.getStringExtra("num");
        mName = intent.getStringExtra("name");
        completeAddress = intent.getStringExtra("completeAddress"); // 有国家码的地址
        if(!TextUtils.isEmpty(mNum)){
            String number = NumberUtils.toHideAsStar(mNum);
            if(TextUtils.isEmpty(mName)){
                mName = number;
            }
            mView.showNum(mName,number);
        }
        mView.setCompleteAddress(completeAddress);
    }

    /**
     * 加载头像
     * @param imageView
     */
    @Override
    public void loadPhoto(ImageView imageView) {
        GlidePhotoLoader.getInstance(mContext).loadPhoto(mContext,imageView,mNum);
    }
}
