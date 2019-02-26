package com.cmicc.module_message.ui.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chinamobile.app.yuliao_common.utils.BroadcastActions;
import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmicc.module_message.ui.presenter.GroupCardPresenter;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.constract.GroupCardContract;

/**
 * @anthor situ
 * @time 2017/6/5 11:18
 * @description 修改群名片界面
 */

public class GroupCardActivity extends BaseActivity implements GroupCardContract.IView {

    public static final String BUNDLE_KEY_ADDRESS = "group_address";
    public static final String BUNDLE_KEY_CARD = "group_card";

    Toolbar mToolbar;
    TextView mSave;
    ImageView mDelect;
    EditText mEdit;
    private TextView mTitle;
    private RelativeLayout mBack;

    private TextWatcher mTextWatcher;
    private GroupCardContract.IPresenter mPresenter;

    private ProgressDialog mProgressDialog;

    public static boolean isFocuse = false ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_card);
    }

    @Override
    protected void findViews() {
        mToolbar = (Toolbar)findViewById(R.id.id_toolbar);
        mSave = (TextView)findViewById(R.id.group_card_save);
        mSave.setOnClickListener(this);
        mDelect = (ImageView)findViewById(R.id.iv_delect);
        mDelect.setOnClickListener(this);
        mEdit = (EditText)findViewById(R.id.edit_query);
        mTitle = (TextView) findViewById(R.id.title);
        mTitle.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        mBack = (RelativeLayout) findViewById(R.id.back);
    }

    @Override
    protected void init() {
        initToolBar();

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage(getString(R.string.dialog_title_qr_wait));
        mProgressDialog.setCancelable(true);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setIndeterminate(false);

        Bundle bundle = getIntent().getExtras();
        mPresenter = new GroupCardPresenter(this, this, bundle);
        mTextWatcher = (TextWatcher) mPresenter;
        mEdit.addTextChangedListener(mTextWatcher);
        mEdit.setFilters(new InputFilter[]{new GroupNameActivity.EmojiInputFilter()});
        mPresenter.start();

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        registerGorupStatusReceiver();
    }

    private void initToolBar() {
        mToolbar.setTitleTextColor(getResources().getColor(R.color.tv_title_color));
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void showDelete(boolean show) {
        mDelect.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void setSaveClickable(boolean clickable) {
        mSave.setEnabled(clickable);
    }

    @Override
    public void finish(Intent intent) {
        if (intent != null) {
            setResult(Activity.RESULT_OK, intent);
        }
        finish();
    }

    @Override
    public void setEditText(String text) {
        mEdit.setText(text);
        mEdit.setSelection(mEdit.getText().length());
    }

    @Override
    public void toast(final CharSequence sequence) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                BaseToast.makeText(GroupCardActivity.this, sequence, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void toggleProgressDialog(boolean show) {
        if (show) {
            mProgressDialog.show();
        } else {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void setTextColor(int color) {
        if(mSave != null ){
            mSave.setTextColor(color);
        }
    }

    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.group_card_save) {
            mPresenter.save();

        } else if (i == R.id.iv_delect) {
            clearText();

        }
    }

    private void clearText() {
        mEdit.setText("");
    }

    public static void startForResult(Activity activity, int requestCode, String address, String userCard) {
        Intent intent = new Intent(activity, GroupCardActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY_ADDRESS, address);
        bundle.putString(BUNDLE_KEY_CARD, userCard);
        intent.putExtras(bundle);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterGorupStatusReceiver();
    }

    /**
     * 广播
     */
    class GorupStatusReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(BroadcastActions.MESSAGE_GROUP_STATUS_UPDATE_SUCCESS_ACTION.equals(intent.getAction()) ){
                String groupID = intent.getStringExtra("GroupID");
                if(mProgressDialog !=null && mProgressDialog.isShowing()){
                    mProgressDialog.dismiss();
                }
                Toast.makeText(GroupCardActivity.this , GroupCardActivity.this.getResources().getString(R.string.group_dismiss) , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private GorupStatusReceiver mGorupStatusReceiver ;

    private void registerGorupStatusReceiver(){
        isFocuse = true ;
        if(mGorupStatusReceiver == null ){
            mGorupStatusReceiver = new GorupStatusReceiver();
            IntentFilter intentFilter = new IntentFilter(BroadcastActions.MESSAGE_GROUP_STATUS_UPDATE_SUCCESS_ACTION);
            mContext.registerReceiver(mGorupStatusReceiver ,intentFilter);
        }
    }

    private void unRegisterGorupStatusReceiver(){
        isFocuse = false ;
        if(mGorupStatusReceiver!=null){
            mContext.unregisterReceiver(mGorupStatusReceiver);
            mGorupStatusReceiver = null ;
        }
    }

}
