package com.cmicc.module_message.ui.activity;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.utils.AndroidUtil;
import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmcc.cmrcs.android.ui.utils.ThumbnailUtils;
import com.cmic.module_base.R;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import cn.com.mms.jar.MmsException;
import cn.com.mms.jar.pdu.EncodedStringValue;
import cn.com.mms.jar.pdu.NotificationInd;
import cn.com.mms.jar.pdu.PduHeaders;
import cn.com.mms.jar.pdu.PduPersister;
import cn.com.mms.utils.MmsUtils;

/**
 * Created by tigger on 2017/7/6.
 * MMS详情界面
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class MmsDetailActivity extends BaseActivity {

    private static final String TAG = "MmsDetailActivity";
    Toolbar mToolbar;
    LinearLayout mMmsLayout;
    TextView subject;
    private PduPersister pduPersister;
    private int mType;
    private Uri mmsUri;
    private String mTitle;
    private static final String[] PART_PROJECTION = new String[]{"_id", "ct",
            "_data", "text", "cl", "name"};
    private Context mContext;
    private TextView mToolBarTitle;
    private RelativeLayout mBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_mms_detail);
    }

    private void initData() {
        LogF.d(TAG, "彩信收件箱uri = " + mmsUri);
        long id = -1;
        if(mmsUri != null){
            id = ContentUris.parseId(mmsUri);
        }
        LogF.d(TAG, "彩信id = " + id);
        Cursor cursor = getContentResolver().query(Telephony.Mms.CONTENT_URI,
                new String[]{Telephony.Mms.SUBJECT, Telephony.Mms.SUBJECT_CHARSET}, "_id=" + id,
                null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String subject = cursor.getString(cursor
                    .getColumnIndex(Telephony.Mms.SUBJECT));
            LogF.d(TAG, "彩信主题subject = " + subject);
            if (!TextUtils.isEmpty(subject)) {
                int charset = cursor.getInt(cursor
                        .getColumnIndex(Telephony.Mms.SUBJECT_CHARSET));
                EncodedStringValue v = null;
                v = new EncodedStringValue(charset,
                        PduPersister.getBytes(subject));
                if (MmsUtils.isGarbled(v.getString())) {
                    mTitle = MmsUtils.getStringOfGarbled(subject, 6);
                } else {
                    mTitle = v.getString();
                }
            }
            cursor.close();
        }
        subject.setTextIsSelectable(true);
        if (TextUtils.isEmpty(mTitle) || mTitle.equals("null")) {
            subject.setVisibility(View.GONE);
        } else if (MmsUtils.isGarbled(mTitle)) {
            mTitle = MmsUtils.getStringOfGarbled(mTitle, 6);
        }
        subject.setText(getString(R.string.title) + mTitle);
        try {
            loadPdu(mmsUri);
        } catch (MmsException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void findViews() {
        mToolbar = (Toolbar) findViewById(R.id.id_toolbar);
        mMmsLayout = (LinearLayout) findViewById(R.id.pdu_layout);
        subject = (TextView) findViewById(R.id.subject);
        mToolBarTitle = (TextView) findViewById(R.id.title);
        mToolBarTitle.setText(getString(R.string.super_msg_detail));
        mToolBarTitle.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        mBack = (RelativeLayout) findViewById(R.id.back);
    }

    @Override
    protected void init() {
        initToolBar();
        mmsUri = getIntent().getData();
        mContext = this;
        pduPersister = PduPersister.getPduPersister(getApplicationContext());
        initData();
        mBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initToolBar() {
        mToolbar.setTitle("");
        mToolbar.setSubtitle(null);
        mToolbar.setLogo(null);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
//        mToolbar.setTitle(getString(R.string.super_msg_detail));
        mToolBarTitle.setText(getString(R.string.super_msg_detail));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onClick(View v) {

    }


    /**
     * @param uri
     * @throws MmsException 加载彩信
     */
    private void loadPdu(Uri uri) throws MmsException {
        long id = ContentUris.parseId(mmsUri);
        Cursor cPart = null;
        HashMap<Integer, String[]> smilMap = new HashMap<Integer, String[]>();
        HashMap<Integer, String[]> partMap = new HashMap<Integer, String[]>();
        try {
            String selectionPart = new String("mid=" + id);
            cPart = mContext.getContentResolver().query(
                    Uri.parse("content://mms/part"), PART_PROJECTION,
                    selectionPart, null, null);
            String strSmilText = null;
            String[] values = new String[cPart.getColumnCount()];
            while (cPart.moveToNext()) {
                for (int i = 0; i < cPart.getColumnCount(); i++) {
                    values[i] = cPart.getString(i);
                }
                if (MmsUtils.isMmsSmilType(values[1])) {// 判断附件类型 "ct",
                    strSmilText = values[3];// "text",
                    break;
                }

            }
            cPart.moveToFirst();
            int j = -1;
            boolean bError = TextUtils.isEmpty(strSmilText) ? true : false;
            do {
                values = new String[cPart.getColumnCount()];
                for (int i = 0; i < cPart.getColumnCount(); i++) {
                    values[i] = cPart.getString(i);
                }
                j += 1;
                if (MmsUtils.isMmsImageType(values[1])
                        || values[1].equals("text/plain")
                        || MmsUtils.isMmsVideoType(values[1])
                        || MmsUtils.isMmsAudioType(values[1])) {
                    if ((!TextUtils.isEmpty(strSmilText)) && (!bError)) {
                        int index = -1;
                        if (values[4] != null)
                            index = strSmilText.indexOf(values[4]);
                        else if (values[5] != null)
                            index = strSmilText.indexOf(values[5]);
                        if (index >= 0)
                            smilMap.put(index, values);
                        else
                            bError = true;
                    }

                    partMap.put(j, values);
                }

            } while (cPart.moveToNext());
            List<Integer> list = new ArrayList<Integer>();
            if (bError)
                list.addAll(partMap.keySet());
            else
                list.addAll(smilMap.keySet());
            Collections.sort(list);
            for (Integer key : list) {
                if (bError)
                    values = (String[]) partMap.get(key);
                else
                    values = (String[]) smilMap.get(key);
                if (MmsUtils.isMmsImageType(values[1])) {
                    Uri uriPart = Uri.parse("content://mms/part/" + values[0]);
                    createImageInfo(uriPart, values[1]);
                } else if (values[1].equals("text/plain")) {
                    if (values[2] != null) {//_data
                        createTextInfo(MmsUtils.getMmsText(values[0], mContext));
                    } else {//text
                        createTextInfo(values[3]);
                    }
                } else if (MmsUtils.isMmsVideoType(values[1])) {
                    Uri uriPart = Uri.parse("content://mms/part/" + values[0]);
                    createVideoInfo(uriPart, values[1]);
                } else if (MmsUtils.isMmsAudioType(values[1])) {
                    Uri uriPart = Uri.parse("content://mms/part/" + values[0]);
                    createAudioInfo(uriPart, values[1]);
                }
            }
        } catch (Exception e) {
            LogF.i(TAG, "加载彩信异常 " + e.getMessage());
            e.printStackTrace();
        } finally {

            if (cPart != null) {
                cPart.close();
            }
            smilMap.clear();
            partMap.clear();
        }

        if (mType == PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND) {
            NotificationInd notificationInd = (NotificationInd) pduPersister
                    .load(uri);
            String contentLocation = new String(
                    notificationInd.getContentLocation());
            String from = notificationInd.getFrom().getString();
        }
    }

    private void createVideoInfo(final Uri uri, final String contentType) {
        String filename = "";
        ImageView imageView = new ImageView(mContext);
        TextView filenameView = new TextView(mContext);
        Cursor cursor = mContext.getContentResolver().query(uri, null, null,
                null, null);
        if (cursor.moveToFirst()) {
            filename = cursor.getString(cursor.getColumnIndex("cl"));
        }
        try {
            filename = new String(filename.getBytes("iso8859-1"), Charset.defaultCharset());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (cursor != null) {
            cursor.close();
        }
        filenameView.setTextIsSelectable(true);
        filenameView.setText(filename);
        filenameView.setGravity(Gravity.CENTER);
        // Uri uri = MmsFileUtil.createFileInSdcard(videoModel, this);
        // imageView.setTag(uri);
        imageView.setPadding(5, 0, 5, 10);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        imageView.setLayoutParams(layoutParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        mMmsLayout.addView(imageView);
        mMmsLayout.addView(filenameView);
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(mContext, uri, MediaStore.Video.Thumbnails.MINI_KIND);
        if (bitmap != null) {
            imageView.setBackground(new BitmapDrawable(mContext.getResources(), bitmap));
            imageView.setImageResource(R.drawable.icon_play_middle);
        } else {
            imageView.setImageResource(R.drawable.playbar_icon_play_pressed);
        }
        bitmap = null;
        imageView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(uri, contentType);
                MmsDetailActivity.this.startActivity(intent);
            }
        });

    }

    private void createAudioInfo(final Uri uri, final String contentType) {
        LogF.d(TAG, "uri = " + uri);
        LogF.d(TAG, "contentType = " + contentType);
        String filename = "";
        ImageView imageView = new ImageView(mContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        imageView.setLayoutParams(layoutParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        TextView filenameView = new TextView(mContext);
        Cursor cursor = mContext.getContentResolver().query(uri, null, null,
                null, null);
        if (cursor.moveToFirst()) {
            filename = cursor.getString(cursor.getColumnIndex("cl"));
        }
        if (cursor != null) {
            cursor.close();
        }
        try {
            filename = new String(filename.getBytes("iso8859-1"), Charset.defaultCharset());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        filenameView.setTextIsSelectable(true);
        filenameView.setText(filename);
        filenameView.setGravity(Gravity.CENTER);
        imageView.setBackgroundResource(R.drawable.filelist_icon_music);
        imageView.setImageResource(R.drawable.playbar_icon_play_pressed);
        // imageView.setTag(MmsFileUtil.createFileInSdcard(audioModel, this));
        imageView.setPadding(5, 0, 5, 10);
        mMmsLayout.addView(imageView);
        mMmsLayout.addView(filenameView);
        imageView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(uri, contentType);
                MmsDetailActivity.this.startActivity(intent);
            }
        });
    }

    private void createTextInfo(final String text) {
        if (!TextUtils.isEmpty(text)) {
            TextView info = new TextView(this);
            info.setTextIsSelectable(true);
            info.setAutoLinkMask(Linkify.ALL);
            info.setTextColor(Color.DKGRAY);
            info.setTextSize(18.5f);
            info.setPadding(5, 10, 5, 10);
            info.setText(text);
            mMmsLayout.addView(info);
        }
    }

    private void createImageInfo(final Uri imageUri, final String contentType) {
        ImageView imageView = new ImageView(mContext);
        imageView.setMinimumHeight((int) AndroidUtil.dip2px(mContext, 70));
        imageView.setMinimumWidth((int) AndroidUtil.dip2px(mContext, 70));
        imageView.setImageBitmap(MmsUtils.getMmsImage(imageUri, mContext,
                false, 100, 100));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        imageView.setLayoutParams(layoutParams);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setTag(imageUri);
        mMmsLayout.addView(imageView);
        imageView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(imageUri, contentType);
                MmsDetailActivity.this.startActivity(intent);
            }
        });
    }
}
