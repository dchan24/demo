package com.cmicc.module_message.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;

import com.chinamobile.app.yuliao_common.utils.statusbar.StatusBarCompat;
import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmicc.module_message.ui.constract.ExchangeVcardContract;
import com.cmicc.module_message.ui.fragment.ExchangeVcardFragment;
import com.cmicc.module_message.ui.presenter.ExchangeVcardPresenterImpl;
import com.cmcc.cmrcs.android.ui.utils.ActivityUtils;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmicc.module_message.R;

/**
 * Created by tianshuai on 2017/7/20.
 * 交换名片界面
 */

public class ExchangeVcardActivity extends BaseActivity {
    private static final String TAG="ExchangeVcardActivity";
    public static final String VCARDSTRING="vcardString";
    public static final String NUMBER="number";
    public static final String TYPE="type";//3 同意交换
    public static final String ID="id";

    private ExchangeVcardFragment mView;
    private ExchangeVcardContract.Presenter mPresenter;


    private LinearLayout mBackLinView ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange_vcard);
        StatusBarCompat.setStatusBarColor(this, this.getResources().getColor( R.color.color_2c2c2c));

    }
    @Override
    protected void findViews() {
        mBackLinView = (LinearLayout) findViewById(R.id.back_LlView);
        mBackLinView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void init() {
        mView =(ExchangeVcardFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (mView == null) {
            mView = new ExchangeVcardFragment();
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                mView.setArguments(bundle);
            }
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), mView, R.id.contentFrame);
        }
        mPresenter = new ExchangeVcardPresenterImpl(this,mView);
        mView.setPresenter(mPresenter);
    }


    public static void showVcard(Activity context, String strangerNumber, int shareType, Long sysId, String agreeVcardString, boolean  isAgree , int requestCode) {

        Intent intent = new Intent(context, ExchangeVcardActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(VCARDSTRING, agreeVcardString);
        bundle.putString(NUMBER,strangerNumber);
        bundle.putInt(TYPE,shareType);
        bundle.putLong(ID,sysId);
        bundle.putBoolean("isAgree",isAgree);
        intent.putExtras(bundle);
        context.startActivityForResult(intent ,requestCode);

    }

    public void updateHint(){
        BaseToast.show(this, getString(R.string.toast_msg_has_forwarded));
        if(mView!=null){
            mView.updateHint("");
        }
    }

    /**
     * 2018.4.11
     */
    public void showDialog(){
        if(mView!=null){
            mView.showDialog();
        }
    }
}
