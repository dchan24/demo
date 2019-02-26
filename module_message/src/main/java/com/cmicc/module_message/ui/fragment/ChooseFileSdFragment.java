package com.cmicc.module_message.ui.fragment;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.chinamobile.app.utils.StringUtil;
import com.chinamobile.app.yuliao_common.utils.FileUtil;
import com.chinamobile.app.yuliao_common.utils.PopWindowFor10GUtil;
import com.chinamobile.app.yuliao_common.view.PopWindowFor10G;
import com.cmicc.module_message.ui.activity.ChooseLocalFileActivity;
import com.cmicc.module_message.ui.adapter.ChooseFileSdAdapter;
import com.cmcc.cmrcs.android.ui.fragments.BaseFragment;
import com.cmcc.cmrcs.android.ui.interfaces.IFragmentBack;
import com.cmicc.module_message.R;

import java.io.File;


/**
 * Created by tigger on 2017/7/3.
 */

public class ChooseFileSdFragment extends BaseFragment implements OnClickListener, OnItemClickListener, IFragmentBack {

    private View mRootView;
    //private ActionBar mActionBar;
    private TextView mTvSend;
    private TextView mTvSlectFileSize;
    ListView mList;
    TextView mTvEmpty;
    private  ChooseFileSdAdapter mAdapter;
    public static File mCurrentFile;
    /**
     * SD卡路径
     */
    private static String EXTERNAL_DEFAULT_TOP_DIR = Environment.getExternalStorageDirectory().getAbsolutePath();
    /**
     * 手机内存卡路径
     */
    private static final String MOBILE_PATH_NEW = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static final String MOBILE_PATH = "/storage/sdcard0";
    private String mPath;
    private File sdcard;
    private PopWindowFor10G m10GPopWindow;
    private View m10GDropView;
    public void initViews(View rootView){
        super.initViews(rootView);
        mList = (ListView) rootView.findViewById(R.id.lv_choose);
        mTvEmpty = (TextView) rootView.findViewById(R.id.tv_empty);
        m10GPopWindow = new PopWindowFor10G(getActivity());

    }
    @Override
    public void init() {
        Bundle bundle = getArguments();
        mPath = bundle.getString("save");
//        EXTERNAL_DEFAULT_TOP_DIR = FileUtil.getSDPath(baseActivity);
        if (mPath.equals("sd")) {
            sdcard = new File(EXTERNAL_DEFAULT_TOP_DIR);
        } else if (mPath.equals("mobile")) {
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                sdcard = new File(MOBILE_PATH_NEW);
            } else {
                sdcard = new File(MOBILE_PATH);
            }
        }
    }

    @Override
    public View getLayoutView(LayoutInflater inflater, ViewGroup container) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_choose_sd_layout, container, false);
        }
        ViewGroup parent = (ViewGroup) mRootView.getParent();
        if (parent != null) {
            parent.removeView(mRootView);
        }
        return mRootView;
    }

    @Override
    public void initData() {
        File localFile = null;
        if (mPath.equals("sd")) {
            localFile = new File(EXTERNAL_DEFAULT_TOP_DIR);
        } else if (mPath.equals("mobile")) {
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                localFile = new File(MOBILE_PATH_NEW);
            } else {
                localFile = new File(MOBILE_PATH);
            }
        }

        mCurrentFile = localFile;
        initView();
    }

    public void initView(){
        intActionBar();

        mList.setOnItemClickListener(this);

        mAdapter = new ChooseFileSdAdapter(getActivity(), mCurrentFile);
        mList.setAdapter(mAdapter);
        if (mAdapter.getSelect() == null) {
            mTvSend.setEnabled(false);
        } else {
            mTvSend.setEnabled(true);
        }

        updateEmptyView();
    }

    private void intActionBar() {
        mTvSend = (TextView)((AppCompatActivity) getActivity()).findViewById(R.id.button_send);
        mTvSlectFileSize = (TextView) ((AppCompatActivity) getActivity()).findViewById(R.id.textview_select_file_size);
        m10GDropView = ((AppCompatActivity) getActivity()).findViewById(R.id.pop_10g_window_drop_view);
//        mActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (mPath.equals("sd")) {
//            mActionBar.setTitle(getResources().getString(R.string.conversation_file_sd));
            setToolBarTitle(getActivity().getResources().getString(R.string.conversation_file_sd));
        } else if (mPath.equals("mobile")) {
//            mActionBar.setTitle(getResources().getString(R.string.conversation_file_mobile_memory));
//            mActionBar.setTitle("本地文件");
            setToolBarTitle(getActivity().getResources().getString(R.string.conversation_file_sd));
        }

        ((ViewGroup)mTvSend.getParent()).setVisibility(View.VISIBLE);
        mTvSend.setEnabled(false);
        mTvSend.setOnClickListener(this);
        mTvSlectFileSize.setText(mContext.getText(R.string.selected) + ": 0B");
        mTvSlectFileSize.setVisibility(View.INVISIBLE);
    }

    private void setToolBarTitle(String title){
        if(title == null)
            return;
        Activity activity = getActivity();
        if(activity instanceof ChooseLocalFileActivity){
            ((ChooseLocalFileActivity) activity).setToolBarTitle(title);
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_choose_sd_layout;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.button_send){
            final File file = mAdapter.getSelect();
            if(file == null){
                return;
            }
            //=====================file 通话中发送文件处理 added && modified by kgb=============================================//
            final ChooseLocalFileActivity sendActivity = (ChooseLocalFileActivity) getActivity();
            if (sendActivity instanceof ChooseLocalFileActivity) {
                if (TextUtils.equals(sendActivity.getSendFileAction(), "callandsendfile")) {
//                    boolean isWifiConnected = AndroidUtil.isNetworkConnectedByWifi(sendActivity);
//                    if(!isWifiConnected){//判断非wifi环境下进行提示
//                        DeleteOprationDialog deleteDialog = new DeleteOprationDialog(sendActivity, "提醒", "确定在非WIFI环境下发送？");
//                        deleteDialog.show();
//                        deleteDialog.setOnDeleteClickListener(new DeleteOprationDialog.OnDeleteClickListener() {
//
//                            @Override
//                            public void onClick() {
//                                UICallLogic.getInstence().sendFileAndInsert(getActivity(), Uri.fromFile(file), sendActivity.getAddress());
//                                finishaOrReturn(sendActivity);
//                                sendActivity.finish();
//                            }
//                        });
//                        return;
//                    }
//                    UICallLogic.getInstence().sendFileAndInsert(getActivity(), Uri.fromFile(file), sendActivity.getAddress());
//                    finishaOrReturn(sendActivity);

                } else {
                    float fileSize_M = file.length()/1024/1024;
                    if(PopWindowFor10GUtil.isNeedPop()&& fileSize_M >2.0f){
                        m10GPopWindow.setType(PopWindowFor10GUtil.TYPE_FOR_SEND_FILE);
                        m10GPopWindow.setSendFile(Uri.fromFile(file),file.length());
                        m10GPopWindow.showAsDropDown(m10GDropView,0,0);
                        return;
                    }
                    getActivity().setResult(Activity.RESULT_OK,
                            getActivity().getIntent().setData(Uri.fromFile(file)));
                }
            }
            //=====================ended 通话中发送文件处理 added && modified by kgb=============================================//
        }
        getActivity().finish();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //点击进入下一层
        File node = (File) (parent.getAdapter().getItem(position));
        final File data = mAdapter.getItem(position);
        if(node.isDirectory()) {
            mAdapter.setFile(node);
        } else {
            String name = data.getName().toLowerCase();
            boolean isBigImg = FileUtil.isBigImageFile(data.getPath());
            if(mAdapter.getTemp() == position || isBigImg) {
                mAdapter.select(-1);
                mAdapter.notifyDataSetChanged();
                if(isBigImg) {
                    ToastUtils.showShort(R.string.big_img_unsupport);
                }
            } else {
                mAdapter.select(position);
            }
        }
        mCurrentFile = node;
        if(mAdapter.getSelect()==null){
            mTvSend.setEnabled(false);
            mTvSlectFileSize.setText(mContext.getText(R.string.selected) + ": 0B");
            mTvSlectFileSize.setVisibility(View.INVISIBLE);
        }else{
            mTvSend.setEnabled(true);
            if(data!= null ){
                if(data.exists()){
                    mTvSlectFileSize.setText(mContext.getText(R.string.selected) +": " + StringUtil.formetFileSize(data.length()));
                    mTvSlectFileSize.setVisibility(View.VISIBLE);

                }else {
                    mTvSlectFileSize.setText(mContext.getText(R.string.selected) + ": 0B");
                    mTvSlectFileSize.setVisibility(View.INVISIBLE);

                }
            }
        }

        updateEmptyView();
    }

    private void updateEmptyView(){
        if (mAdapter.getCount() <= 0){
            mTvEmpty.setVisibility(View.VISIBLE);
        }else {
            mTvEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((ChooseLocalFileActivity)getActivity()).setFragmentBack(this);
    }

    @Override
    public boolean onFragmentBack() {
        if(mCurrentFile.isFile()){
            mCurrentFile = mCurrentFile.getParentFile();
        }

        mTvSend.setEnabled(false);
        mTvSlectFileSize.setText(mContext.getText(R.string.selected) + ": 0B");
        mTvSlectFileSize.setVisibility(View.INVISIBLE);
        if(!mCurrentFile.equals(sdcard)){
            File localFile = mCurrentFile.getParentFile();
            mCurrentFile = new File(mCurrentFile.getAbsolutePath().substring(
                    0, mCurrentFile.getAbsolutePath().lastIndexOf(File.separator)));
            mAdapter.setFile(localFile);
            updateEmptyView();
        }else{
            return false;
        }
        return true;
    }
}
