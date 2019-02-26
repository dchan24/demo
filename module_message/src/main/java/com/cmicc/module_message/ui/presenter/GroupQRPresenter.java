package com.cmicc.module_message.ui.presenter;

import android.content.Context;
import android.os.Bundle;

import com.chinamobile.app.yuliao_business.model.GroupQrImage;
import com.chinamobile.app.yuliao_business.util.MsgContentBuilder;
import com.chinamobile.app.yuliao_common.utils.Log;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_core.cmccauth.AuthWrapper;
import com.chinamobile.app.yuliao_core.db.LoginDaoImpl;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.constract.GroupQRContract;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import cn.com.fetion.zxing.qrcode.activity.GroupQrUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.Response;

/**
 * Created by hwb on 2017/7/10.
 */

public class GroupQRPresenter implements GroupQRContract.Presenter {
    private GroupQRContract.View view;
    private Context mContext;

    private String mToken;

    private String sGroupId;
    private String sAddress;
    private String sGroupName;
    private GroupQrImage groupImage;

    private static final String TAG = "GroupQRPresenter";

    @Override
    public void start() {
        load();
    }

    public GroupQRPresenter(GroupQRContract.View v, Context context, Bundle bundle) {
        view = v;
        mContext = context;
        sGroupName = bundle.getString("groupName");
        sGroupId = bundle.getString("address");
        sAddress = LoginDaoImpl.getInstance().queryLoginUser(mContext);
        Log.d(TAG, "sGroupId " + sGroupId + ", sGroupName " + sGroupName + ", sAddress " + sAddress);
    }

    private void getGroupQrImage() {
        GroupQrUtil.QrImageRequest(mContext, mToken, sGroupId, "", qrImageback);
    }

    private Callback qrImageback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            LogF.d(TAG, "onFailure");
            view.updateUIAfterQueryQR(null);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            Headers requestHeaders = response.request().headers();
            if (requestHeaders != null) {
                Map<String, List<String>> mapRequest = requestHeaders.toMultimap();
                for (String key : mapRequest.keySet()) {
                    Log.d(TAG, "request:" + (key == null ? "" : key + ":") + mapRequest.get(key).get(0));
                }
            }

            int code = response.code();
            Log.d(TAG, "response status:" + code);
            Headers headers = response.headers();
            Map<String, List<String>> mapRespone = headers.toMultimap();
            for (String key : mapRespone.keySet()) {
                Log.d(TAG, "respone:" + (key == null ? "" : key + ":") + mapRespone.get(key).get(0));
            }
            String temp = response.body().string();
            Log.d(TAG, "respone body:" + temp);
            if (code == 200) {
                String date = headers.get("Date");
                groupImage = MsgContentBuilder.getGroupQrImage(temp);
                groupImage.setDate(date);
                Log.d(TAG, "response server date:" + date + " image date:" + groupImage.getExpires());
            }
            if (groupImage != null && groupImage.getQrByte() != null) {
                view.updateUIAfterQueryQR(groupImage);
            } else {
                view.updateUIAfterQueryQR(null);
            }
        }
    };

    private void load() {
        AuthWrapper.getInstance(mContext).getRcsAuth(new AuthWrapper.RequestTokenListener() {
            @Override
            public void onSuccess(String arg0, String arg1) {
            }

            @Override
            public void onSuccess(final String token) {
                if (token != null && token.length() > 0) {
                    mToken = token;
                    getGroupQrImage();
                } else {
                    Log.e(TAG, "token is invalid");
                    view.finishUI(mContext.getString(R.string.toast_group_qr_get_token_failed));
                }
            }

            @Override
            public void onFail(final int arg0) {
                Log.e(TAG, "get token fail " + arg0);
                view.finishUI(mContext.getString(R.string.toast_group_qr_get_token_failed));
            }
        });
    }
}
