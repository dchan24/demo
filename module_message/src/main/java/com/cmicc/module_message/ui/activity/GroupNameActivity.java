package com.cmicc.module_message.ui.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chinamobile.app.yuliao_common.application.MyApplication;
import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmicc.module_message.ui.constract.GroupNameContract;
import com.cmicc.module_message.ui.presenter.GroupNamePresenter;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmicc.module_message.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @anthor situ
 * @time 2017/5/23 9:28
 * @description 修改群聊名称
 */

public class GroupNameActivity extends BaseActivity implements GroupNameContract.IView {

    public static final String BUNDLE_KEY_ADDRESS = "group_address";
    public static final String BUNDLE_KEY_NAME = "group_name";

    Toolbar mToolbar;
    TextView mSave;
    TextView mChangeName;
    ImageView mDelect;
    EditText mEdit;
    RelativeLayout mBack;

    private TextWatcher mTextWatcher;
    private GroupNameContract.IPresenter mPresenter;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_name);
    }

    @Override
    protected void findViews() {
        mToolbar = (Toolbar)findViewById(R.id.id_toolbar);
        mToolbar.setTitle("");
        mSave = (TextView)findViewById(R.id.group_name_save);
        mChangeName = (TextView)findViewById(R.id.change_name);
        mSave.setOnClickListener(this);
        mDelect = (ImageView)findViewById(R.id.iv_delect);
        mDelect.setOnClickListener(this);
        mEdit = (EditText)findViewById(R.id.edit_query);
        mBack = (RelativeLayout) findViewById(R.id.back);
        mBack.setOnClickListener(this);

    }

    @Override
    protected void init() {
        initToolBar();
        mChangeName.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage(getString(R.string.dialog_title_qr_wait));
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setIndeterminate(false);

        Bundle bundle = getIntent().getExtras();
        mPresenter = new GroupNamePresenter(this, this, bundle);
        mTextWatcher = (TextWatcher) mPresenter;
        mEdit.addTextChangedListener(mTextWatcher);
        mEdit.setFilters(new InputFilter[]{new EmojiInputFilter()});
        mPresenter.start();
    }

    private void initToolBar() {
        mToolbar.setTitleTextColor(getResources().getColor(R.color.tv_title_color));
        setSupportActionBar(mToolbar);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public static void startForResult(Activity activity, int requestCode, String address, String groupName) {
        Intent intent = new Intent(activity, GroupNameActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY_ADDRESS, address);
        bundle.putString(BUNDLE_KEY_NAME, groupName);
        intent.putExtras(bundle);
        activity.startActivityForResult(intent, requestCode);
    }

    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.group_name_save) {
            mPresenter.save();

        } else if (i == R.id.iv_delect) {
            clearText();

        }else if (i == R.id.back){
            finish();
        }
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
        if(TextUtils.isEmpty(text)){
            return;
        }
        mEdit.setText(text);
        mEdit.setSelection(mEdit.getText().length());
    }

    @Override
    public void toast(final CharSequence sequence) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                BaseToast.makeText(GroupNameActivity.this, sequence, Toast.LENGTH_SHORT).show();
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
        mSave.setTextColor(color);
    }

    private void clearText() {
        mEdit.setText("");
    }

    public static class EmojiInputFilter implements InputFilter {

        private Pattern emoji = Pattern.compile("[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
                Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            Matcher emojiMatcher = emoji.matcher(source);
            if (emojiMatcher.find()) {
                if (MyApplication.getAppContext() != null) {
                    BaseToast.makeText(MyApplication.getAppContext(), MyApplication.getAppContext().getString(R.string.can_not_suppport_it), Toast.LENGTH_SHORT).show();
                }
                return "";
            }
            return source;
        }
    }



}
