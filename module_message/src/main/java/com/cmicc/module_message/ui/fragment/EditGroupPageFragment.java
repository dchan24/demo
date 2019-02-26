package com.cmicc.module_message.ui.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.yuliao_business.util.BuryingPointUtils;
import com.cmicc.module_message.ui.activity.GroupNameActivity;
import com.cmicc.module_message.ui.constract.EditGroupPageContract;
import com.cmcc.cmrcs.android.ui.dialogs.ReasonDialog;
import com.cmcc.cmrcs.android.ui.fragments.BaseFragment;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmicc.module_message.R;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tigger on 2017/5/18.
 */

public class EditGroupPageFragment extends BaseFragment implements EditGroupPageContract.View, OnClickListener {
    private static int mMaxLength = 30;

    private EditGroupPageContract.Presenter mPresenter;
    private String mName;
    private String mNums;
    private ArrayList<String> mNumList;
    private TextView mTvSure;
    private ProgressDialog mProgressDialog;
    private ReasonDialog reasonDialog;

    EditText mEtGroupNane;
    ImageButton mIbDel;

    public void initViews(View rootView) {
        super.initViews(rootView);
        mEtGroupNane = (EditText) rootView.findViewById(R.id.et_group_name);
        mIbDel = (ImageButton) rootView.findViewById(R.id.ib_del);
        mIbDel.setOnClickListener(this);
        mEtGroupNane.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                iStart = start;
                iCount = count;
            }

            @Override
            public void afterTextChanged(Editable s) {
                String temp = s.toString().trim();
                int num = temp.getBytes(Charset.forName("utf-8")).length;
                while (num > mMaxLength) {
                    int end = iStart + iCount;
                    s.delete(end - 1, end);
                    temp = s.toString().trim();
                    num = temp.getBytes(Charset.forName("utf-8")).length;
                }

                if (TextUtils.isEmpty(temp)) {
                    mIbDel.setVisibility(View.INVISIBLE);
                    mTvSure.setTextColor(getResources().getColor(R.color.color_a6a6a6));
                    mTvSure.setClickable(false);
                    mTvSure.setEnabled(false);
                } else {
                    mIbDel.setVisibility(View.VISIBLE);
                    mTvSure.setTextColor(getResources().getColor(R.color.color_4991fb));
                    mTvSure.setClickable(true);
                    mTvSure.setEnabled(true);
                }
            }
        });
    }


    public static EditGroupPageFragment newInstantce() {
        return new EditGroupPageFragment();
    }

    @Override
    public void initData() {
        Intent intent = getActivity().getIntent();
        mName = getResources().getString(R.string.recent_selector_group_chat) ; //（建群群名统一改为【群聊】）intent.getStringExtra("names");
        if (TextUtils.isEmpty(mName)) {
            mName = "";
        } else {
            mName = filteName(mName);
            String temp = mName.trim();
            int num = temp.getBytes(Charset.forName("utf-8")).length;
            while (num > mMaxLength) {
                int end = iStart + iCount;
                temp = temp.substring(0, temp.length() - 1);
                num = temp.getBytes(Charset.forName("utf-8")).length;
            }
            mName = temp;
        }

        mNumList = (ArrayList<String>) intent.getSerializableExtra("nums");
        mNums = "";
        for (String name : mNumList) {
            mNums = mNums + name + ";";
        }
        if (!TextUtils.isEmpty(mNums)) mNums = mNums.substring(0, mNums.length() - 1);

        initView();
    }

    public void initView() {
        mEtGroupNane.setText(mName);
        mEtGroupNane.setSelection(mName.length());

        mTvSure.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!AndroidUtil.isNetworkConnected(getActivity())) {
                    BaseToast.show(R.string.network_disconnect);
                    return;
                }
                BuryingPointUtils.messageEntrybPuryingPoint(mContext ,"新建群聊");
                sendGroupInvite();
            }
        });
        mEtGroupNane.setFilters(new InputFilter[]{new GroupNameActivity.EmojiInputFilter()});
        mProgressDialog = new ProgressDialog(this.getActivity());
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage(getString(R.string.wait_please));
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setIndeterminate(false);
        reasonDialog = new ReasonDialog(mContext);
        reasonDialog.setOKButMessahe(getString(R.string.create_group_sure_text));
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_edit_group_page;
    }

    @Override
    public void setPresenter(EditGroupPageContract.Presenter p) {
        mPresenter = p;
    }

    @Override
    public void dismissProgressDialog() {
        mProgressDialog.dismiss();
    }

    // 显示失败的原因
    @Override
    public void showReasonsFailureDialog(String message) {
        if(reasonDialog!=null && !reasonDialog.isShowing()){
            reasonDialog.setTextMessage(message);
            reasonDialog.show();
        }
    }

    public void setTvSure(TextView tv) {
        mTvSure = tv;
    }

    private void sendGroupInvite() {
        String temp = mEtGroupNane.getEditableText().toString();
        if (temp == null || "".equals(temp) || temp.trim().isEmpty()) {
            if (mName == null || "".equals(mName) || mName.trim().isEmpty()) {
                BaseToast.show(this.getActivity(), getString(R.string.group_name_can_not_empty));
                return;
            }
        } else {
            mName = temp.trim();
        }
        mProgressDialog.show();
        mPresenter.sendGroupInvite(1, mName, mNums);
    }

    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.ib_del) {
            mEtGroupNane.setText("");
            mIbDel.setVisibility(View.INVISIBLE);
        }
    }

    private int iStart = 0;
    private int iCount = 0;

    private String filteName(String name) {
        if (getHasEmoji(name)) {
            return "";
        }
        return name;
    }

    public boolean getHasEmoji(String name) {
        Pattern emoji = Pattern.compile("[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]", Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
        Matcher emojiMatcher = emoji.matcher(name);
        if (emojiMatcher.find()) {
            return true;
        }
        return false;
    }


    public void dismiss(){
        if(reasonDialog!= null && reasonDialog.isShowing()){
            reasonDialog.dismiss();
        }
    }
}
