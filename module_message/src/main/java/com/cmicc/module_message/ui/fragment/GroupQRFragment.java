package com.cmicc.module_message.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chinamobile.app.yuliao_business.model.GroupQrImage;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_common.utils.Threads.HandlerThreadFactory;
import com.chinamobile.app.yuliao_common.utils.statusbar.StatusBarCompat;
import com.cmcc.cmrcs.android.glide.GlidePhotoLoader;
import com.chinamobile.app.utils.TimeUtil;
import com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity;
import com.cmicc.module_message.ui.constract.GroupQRContract;
import com.cmcc.cmrcs.android.ui.fragments.BaseFragment;
import com.cmcc.cmrcs.android.ui.model.MediaItem;
import com.cmcc.cmrcs.android.ui.utils.UmengUtil;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmicc.module_message.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.MESSAGE_ITEM;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.MESSAGE_ITEM_VALUE;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.MESSAGE_TYPE;
import static com.cmcc.cmrcs.android.ui.utils.ContactSelectorUtil.SOURCE_MESSAGE_FORWARD;


/**
 * Created by hwb on 2017/7/11.
 */

public class GroupQRFragment extends BaseFragment implements GroupQRContract.View,View.OnClickListener {

    RelativeLayout layoutQr;
    ImageView imageQrIcon;
    TextView textGroupName;
    TextView textDate;
    ImageView imageWait;
    LinearLayout imageError;
    ImageView groupPhoto;
//    TextView textError;
    ImageView mShareBtn;
    ImageView mSaveBtn;
    ImageView mLeftUp;
    ImageView mLeftBelow;
    ImageView mRightUp;
    ImageView mRightBelow;
    RelativeLayout mInfo;




    private static final String TAG = GroupQRFragment.class.getSimpleName();

    private GroupQRContract.Presenter mPresenter;
    private Context mContext;
    private Animation animation;// 旋转动画

    private String sGroupId;
    private String sGroupName;
    private static final String QRSendPath =   Environment.getExternalStorageDirectory().getAbsolutePath() + "/meetyou/contacts/logo/qr/" ;
    private static final String QRSavePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/meetyou/contacts/logo/qrsave/" ;

    public static GroupQRFragment newInstance() {
        return new GroupQRFragment();
    }

    public GroupQRFragment() {
    }

    public void setPresenter(@NonNull GroupQRContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void initViews(View rootView){
        super.initViews(rootView);
        layoutQr = (RelativeLayout) rootView.findViewById(R.id.group_qr_layout);
        imageQrIcon = (ImageView) rootView.findViewById(R.id.group_qr_icon);
        textGroupName = (TextView) rootView.findViewById(R.id.group_qr_name);
        textDate = (TextView) rootView.findViewById(R.id.group_qr_date);
        imageWait = (ImageView) rootView.findViewById(R.id.group_qr_wait);
        imageError = (LinearLayout) rootView.findViewById(R.id.group_qr_error);
        groupPhoto = (ImageView) rootView.findViewById(R.id.photo);
//        textError = (TextView)RootView.findViewById(R.id.group_qr_error_tip);
        mShareBtn = (ImageView) rootView.findViewById(R.id.qecode_share_btn);
        mSaveBtn = (ImageView) rootView.findViewById(R.id.qecode_save_btn);

        mLeftBelow = (ImageView) rootView.findViewById(R.id.left_below);
        mLeftUp = (ImageView) rootView.findViewById(R.id.left_up);
        mRightBelow = (ImageView) rootView.findViewById(R.id.right_below);
        mRightUp = (ImageView) rootView.findViewById(R.id.right_up);
        mInfo = (RelativeLayout) rootView.findViewById(R.id.group_info);

        mShareBtn.setOnClickListener(this);
        mSaveBtn.setOnClickListener(this);

        imageQrIcon.setOnClickListener(this);
    }

    @Override
    public void initData() {
        setHasOptionsMenu(true);
        mContext = getContext();
        StatusBarCompat.setStatusBarColor(getActivity(), getResources().getColor(R.color.color_2c2c2c));
        animation = AnimationUtils.loadAnimation(getContext(), R.anim.asp_rotate_left);
        LinearInterpolator lir = new LinearInterpolator();
        animation.setInterpolator(lir);
        Bundle bundle = getActivity().getIntent().getExtras();
        sGroupName = bundle.getString("groupName");
        sGroupId = bundle.getString("address");
        loadView();
        textGroupName.setText(sGroupName);
        GlidePhotoLoader.getInstance(mContext).loadGroupPhoto(mContext, groupPhoto, null, sGroupId);
        mPresenter.start();
    }

    @Override
    public int getLayoutId() {
        return R.layout.group_qr_fragment;
    }

    private void loadView() {
        imageWait.setVisibility(View.VISIBLE);
        imageWait.startAnimation(animation);
    }

    private void successView() {
        if(imageWait != null) {
            imageWait.clearAnimation();
            imageWait.setVisibility(View.GONE);

            mRightBelow.setVisibility(View.VISIBLE);
            mLeftBelow.setVisibility(View.VISIBLE);
            mRightUp.setVisibility(View.VISIBLE);
            mLeftUp.setVisibility(View.VISIBLE);
            mInfo.setVisibility(View.VISIBLE);
            mShareBtn.setVisibility(View.VISIBLE);
            mSaveBtn.setVisibility(View.VISIBLE);
        }
    }

    private void errorView() {
        if(imageWait != null) {
            imageWait.clearAnimation();
            imageWait.setVisibility(View.GONE);
        }
        imageError.setVisibility(View.VISIBLE);

        mRightBelow.setVisibility(View.GONE);
        mLeftBelow.setVisibility(View.GONE);
        mRightUp.setVisibility(View.GONE);
        mLeftUp.setVisibility(View.GONE);
        mInfo.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void setQrImage(GroupQrImage groupImage) {
        byte[] by = groupImage.getQrByte();
        Bitmap bit = BitmapFactory.decodeByteArray(by, 0, by.length);
        imageQrIcon.setImageBitmap(bit);

        String expires = groupImage.getExpires();
        String date = groupImage.getDate();

        long lexpires = TimeUtil.parseTime(expires, TimeUtil.TIME_FORMAT_FOUR);
        long ldate = TimeUtil.getTimeByGMT(date);

        String t1 = "" + TimeUtil.getDaysBetween(ldate, lexpires);
        String t2 = TimeUtil.formatTime(lexpires, "MM月dd日");
        textDate.setText(getResources().getString(R.string.tv_label_qr_code_date, t1, t2));
    }

    // 发广播通知系统更新相册
    private void savePhoto() {
//        File file = new File(QueryQRCodeUtil.get2DCodeCachePath(mContext));
        HandlerThreadFactory.getHandlerThread(HandlerThreadFactory.BackgroundThread).post(new Runnable() {
            @Override
            public void run() {
                String toFile = QRSavePath+sGroupName+"saveQrcode.png";
                saveBitmap(getBitmap(layoutQr),QRSavePath,toFile);
                File file = new File(toFile);
                if (file.exists()) {
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri uri = Uri.fromFile(file);
                    intent.setData(uri);
                    getActivity().sendBroadcast(intent);
                    BaseToast.makeText(mContext, getString(R.string.toast_save_success), Toast.LENGTH_SHORT).show();
                } else {
                    BaseToast.makeText(mContext, getString(R.string.toast_save_failed), 1000).show();
                }
            }
        });

    }

    private void toShare(){
//        Intent i = ContactsSelectActivity.createIntentForMessageForward(mContext);
        Intent i = ContactSelectorActivity.creatIntent(mContext,SOURCE_MESSAGE_FORWARD, 1);
        final String toFile = QRSendPath+System.currentTimeMillis()+"sendQrcode.png";
        HandlerThreadFactory.getHandlerThread(HandlerThreadFactory.BackgroundThread).post(new Runnable() {
            @Override
            public void run() {
//                String fromFile = QueryQRCodeUtil.get2DCodeCachePath(mContext);
                File noMediaFile  =  new File(QRSendPath+".nomedia");
                if(!noMediaFile.exists()){
                    try {
                        noMediaFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                saveBitmap(getBitmap(layoutQr),QRSendPath,toFile);
            }
        });
        MediaItem item = new MediaItem(toFile,MediaItem.MEDIA_TYPE_IMAGE);
        ArrayList<MediaItem> items = new ArrayList<MediaItem>();
        items.add(item);
        Bundle bundle = new Bundle();
        bundle.putInt(MESSAGE_TYPE, Type.TYPE_MSG_IMG_SEND);
        bundle.putBoolean(MESSAGE_ITEM, true);
        bundle.putSerializable(MESSAGE_ITEM_VALUE, items);
        i.putExtras(bundle);
        mContext.startActivity(i);
    }

    public static String saveBitmap(Bitmap bitmap,String pathDir,String savePath) {
        if(bitmap == null || savePath == null)
            return null;
        File fileDir = new File(pathDir);
        if(!fileDir.exists()){
            fileDir.mkdirs();
        }
        File filePic;
        try {
            filePic = new File(savePath);
            if (filePic.exists()) {
                filePic.delete();
            }
            filePic.createNewFile();
            FileOutputStream fos = new FileOutputStream(filePic);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

        return filePic.getAbsolutePath();
    }

    private Bitmap getBitmap(View view){
        try {
            View screenView = getActivity().getWindow().getDecorView();
            screenView.setDrawingCacheEnabled(true);
            screenView.buildDrawingCache();

            //获取屏幕整张图片
            Bitmap bitmap = screenView.getDrawingCache();
            if (bitmap != null) {

                //需要截取的长和宽
                int outWidth = view.getWidth();
                int outHeight = view.getHeight();

                //获取需要截图部分的在屏幕上的坐标(view的左上角坐标）
                int[] viewLocationArray = new int[2];
                view.getLocationOnScreen(viewLocationArray);

                //从屏幕整张图片中截取指定区域
                bitmap = Bitmap.createBitmap(bitmap, viewLocationArray[0], viewLocationArray[1], outWidth, outHeight);
                return bitmap;
            }
        }catch (Exception e){
            LogF.i(TAG,"getBitmap  getScreenShot error");
            return null;
        }
        return null;

    }

    @Override
    public void updateUIAfterQueryQR(final GroupQrImage groupImage) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    if(getActivity() == null  || imageQrIcon == null){
                        return;
                    }
                    if (groupImage != null) {
                        successView();
                        setQrImage(groupImage);
                    } else {
                        errorView();
                    }
                }
            });
        }
    }

    @Override
    public void finishUI(final String toastTest) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    if (!TextUtils.isEmpty(toastTest)) {
                        BaseToast.makeText(getContext(),
                                toastTest, Toast.LENGTH_SHORT).show();
                    }
                    getActivity().finish();
                }
            });
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.qecode_share_btn){
            UmengUtil.buryPoint(mContext,"groupmessage_setup_QR_transmit","消息-群聊-群聊设置-群二维码-转发",0);
            toShare();
        }else if(id == R.id.qecode_save_btn){
            UmengUtil.buryPoint(mContext,"groupmessage_setup_QR_download","消息-群聊-群聊设置-群二维码-下载",0);
            savePhoto();
        }else if (id == R.id.group_qr_icon){
            loadView();
            mPresenter.start();
            imageError.setVisibility(View.GONE);
        }
    }
}
