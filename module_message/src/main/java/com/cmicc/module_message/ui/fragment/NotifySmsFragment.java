package com.cmicc.module_message.ui.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chinamobile.app.utils.RxAsyncHelper;
import com.chinamobile.app.yuliao_business.model.BaseModel;
import com.chinamobile.app.yuliao_business.model.Conversation;
import com.chinamobile.app.yuliao_business.provider.Conversations;
import com.chinamobile.app.yuliao_business.util.ConversationUtils;
import com.chinamobile.app.yuliao_business.util.Status;
import com.chinamobile.app.yuliao_common.utils.Log;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_common.utils.statusbar.StatusBarCompat;
import com.cmcc.cmrcs.android.ui.dialogs.CommomDialog;
import com.cmcc.cmrcs.android.ui.fragments.BaseFragment;
import com.cmcc.cmrcs.android.ui.utils.ConvCache;
import com.cmcc.cmrcs.android.ui.utils.WrapContentLinearLayoutManager;
import com.cmcc.cmrcs.android.ui.view.MessageOprationDialog;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.activity.NotifySmsActivity;
import com.cmicc.module_message.ui.adapter.ConvListAdapter;
import com.cmicc.module_message.ui.constract.NotifySmsContract;
import com.constvalue.MessageModuleConst;
import com.rcs.rcspublicaccount.util.PublicAccountUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import rx.functions.Func1;

/**
 * Created by tigger on 2017/7/27.
 */

public class NotifySmsFragment extends BaseFragment implements NotifySmsContract.View, ConvListAdapter.OnRecyclerViewItemClickListener,View.OnClickListener,ConvListAdapter.OnCheckChangeListener {

    private static final String TAG = "NotifySmsFragment";

    private NotifySmsContract.Presenter mPresenter;

    RecyclerView mRecyclerView;
    ImageView mIvEmpty;
    TextView multiDelBtn;
    private int source;
    private int mode = MessageModuleConst.NotifySmsActivityConst.OUT_MULTIDELETE_MODE;

    @Override
    public void initViews(View rootView) {
        super.initViews(rootView);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_notify_sms_list);
        mIvEmpty = (ImageView) rootView.findViewById(R.id.iv_empty);
        multiDelBtn = (TextView)rootView.findViewById(R.id.multidel_btn);
    }

    private ConvListAdapter mConvListAdapter;

    public static NotifySmsFragment newInstantce() {
        return new NotifySmsFragment();
    }

    @Override
    public void initData() {
        Activity activity = getActivity();
        if(activity == null || mPresenter == null)
            return;
        //设置状态栏
        StatusBarCompat.setStatusBarColor(getActivity(), getResources().getColor(R.color.color_2c2c2c));

        mRecyclerView.setLayoutManager(new WrapContentLinearLayoutManager(this.getActivity(), LinearLayoutManager.VERTICAL, false));
        mConvListAdapter = new ConvListAdapter(activity, mPresenter.getCacheType());

        checkAdapterItemCount();
        mRecyclerView.setAdapter(mConvListAdapter);
        mConvListAdapter.setRecyclerViewItemClickListener(this);
        mConvListAdapter.setOnCheckChangeListener(this);
        multiDelBtn.setOnClickListener(this);

    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_notify_sms;
    }

    @Override
    public void notifyDataSetChanged() {
        if(mConvListAdapter == null){
            return;
        }
        checkAdapterItemCount();
        mConvListAdapter.notifyDataSetChanged();
    }

    private void checkAdapterItemCount() {
        if (mConvListAdapter.getItemCount() <= 0) {
            mIvEmpty.setVisibility(View.VISIBLE);
        } else {
            mIvEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        //有时候位置为-1，应用会崩溃，后续需要优化，暂时直接解决崩溃问题
        if (position == -1) {
            return;
        }
        //批量删除模式
        if(mode == MessageModuleConst.NotifySmsActivityConst.INTO_MULTIDELETE_MODE){
            //已经选择的选项，再次选择，进行移除,否则添加
            if(mConvListAdapter.getSelectedList().get(position)){
                mConvListAdapter.removeSelection(position);
            }else{
                mConvListAdapter.addSelection(position);
            }

        }else{
            final Conversation conversation = mConvListAdapter.getItem(position);
            mPresenter.openItem(this.getActivity(), conversation);
        }
    }

    @Override
    public void onCheckChange(int selectedCount) {
        LogF.d(TAG,"onCheckChange selectedCount = " + selectedCount);
        if(selectedCount == 0){
            multiDelBtn.setEnabled(false);
            multiDelBtn.setTextColor(Color.parseColor("#FFBFBFBF"));
        }else{
            multiDelBtn.setEnabled(true);
            multiDelBtn.setTextColor(Color.parseColor("#FF2A2A2A"));
        }

        NotifySmsActivity activity = (NotifySmsActivity)getActivity();
        if(activity != null){
            activity.onCheckChange(selectedCount,mConvListAdapter.getDataList().size());
        }
    }

    @Override
    public boolean onItemLongCLickListener(View v, final int position) {
        final Conversation conversation = mConvListAdapter.getItem(position);
        if(conversation == null){
            return false;
        }
        String address = conversation.getAddress();
        final String title = conversation.getPerson();
        final int boxType = conversation.getBoxType();

        MessageOprationDialog messageOprationDialog;
        ArrayList<String> list = new ArrayList<>();
        if (conversation.getUnReadCount() > 0) {
//            list.add("标为已读");
        }
        if (conversation.getTopDate() > 0) {
            list.add(getString(R.string.cancal_top));
        } else {
            list.add(getString(R.string.set_chat_top));
        }
        list.add(getString(R.string.delete_chat));
        if(source == MessageModuleConst.NotifySmsActivityConst.SOURCE_NOTIFYSMS){
            list.add(getString(R.string.mutli_delete));
        }
        String[] itemList = list.toArray(new String[list.size()]);
        messageOprationDialog = new MessageOprationDialog(this.getActivity(), null, itemList, address);
//        messageOprationDialog.setShowTitle(true);
        messageOprationDialog.setOnMessageItemClickListener(new MessageOprationDialog.OnOprationItemClickListener() {
            @Override
            public void onClick(String item, int which, String address) {
                if (item.equals(getString(R.string.set_read_label))) {
                    LogF.e(TAG, "通知类短信标记为已读尚未处理");
                } else if (item.equals(getString(R.string.set_chat_top))) {
                    long time = System.currentTimeMillis();
                    if (ConversationUtils.setTop(getActivity(), address, time))
                        ConvCache.getInstance().updateToTop(address,mPresenter.getCacheType(), time);
//                    ContentValues contentValues = new ContentValues();
//                    ConversationUtils.update(getActivity(), address, contentValues);
//                    ConvCache.getInstance().setTop(CacheType.CT_NOTIFY, address, true);
//                    mConvListAdapter.notifyDataSetChanged();
//                    mConvListAdapter.updateTop();
                } else if (item.equals(getString(R.string.delete_chat))) {
//                    ConversationUtils.delete(getActivity(), boxType, address, null, false);
                    if (conversation.getTopDate() <= 0) {
                        //非置顶
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(BaseModel.COLUMN_NAME_STATUS, Status.STATUS_DELETE);
                        ConversationUtils.update(getActivity(), address, contentValues, boxType);
                    }else {
                        PublicAccountUtil.getInstance().clearPlatformMsg(getActivity(),address);
                    }
                } else if (item.equals(getString(R.string.cancal_top))) {
                    if (ConversationUtils.setTop(getActivity(), address, -1))
                        ConvCache.getInstance().updateToTop(address,mPresenter.getCacheType(), -1);
//                    ContentValues contentValues = new ContentValues();
//                    ConversationUtils.update(getActivity(), address, contentValues);
//                    ConvCache.getInstance().setTop(CacheType.CT_NOTIFY, address, false);
//                    mConvListAdapter.notifyDataSetChanged();
//                    mConvListAdapter.updateTop();
                }else if (item.equals(getString(R.string.mutli_delete))){
                    NotifySmsActivity activity = (NotifySmsActivity)getActivity();
                    if(activity != null){
                        activity.changeMode(MessageModuleConst.NotifySmsActivityConst.INTO_MULTIDELETE_MODE);
                        mConvListAdapter.addSelection(position);
                        changeMode(MessageModuleConst.NotifySmsActivityConst.INTO_MULTIDELETE_MODE);
                    }
                }
            }
        });
        messageOprationDialog.show();
        return true;
    }

    public void selectAll(){
        mConvListAdapter.selectAll();
    }

    public void cancelSelectAll(){
        mConvListAdapter.cancelSelectAll();
    }

    public void changeMode(int mode){
        this.mode = mode;
        LogF.d(TAG,"changeMode mode = " + mode);
        switch (mode){
            case MessageModuleConst.NotifySmsActivityConst.INTO_MULTIDELETE_MODE:
                LogF.d(TAG,"changeMode 进入批量删除模式");
                multiDelBtn.setVisibility(View.VISIBLE);
                mConvListAdapter.setMultiDelMode(true);
                break;

            case MessageModuleConst.NotifySmsActivityConst.OUT_MULTIDELETE_MODE:
                LogF.d(TAG,"changeMode 退出批量删除模式");
                multiDelBtn.setVisibility(View.GONE);
                mConvListAdapter.clearSelection();
                mConvListAdapter.setMultiDelMode(false);
                break;

            default:
                break;
        }
    }

    @Override
    public void onDestroyView() {
        ConvCache.getInstance().setConvCacheFinishCallback2(null);
        super.onDestroyView();
    }

    public void setPresenter(NotifySmsContract.Presenter mPresenter) {
        this.mPresenter = mPresenter;
    }

    public void setSource(int source){
        this.source = source;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.multidel_btn){

            final CommomDialog dialog = new CommomDialog(getActivity(),"",getString(R.string.mutli_delete_message));
            dialog.setPositiveName(getString(R.string.delete));
            dialog.setNegativeName(getString(R.string.cancel));
            dialog.setOnNegativeClickListener(new CommomDialog.OnClickListener() {
                @Override
                public void onClick() {
                    dialog.dismiss();
                }
            });
            dialog.setOnPositiveClickListener(new CommomDialog.OnClickListener() {
                @Override
                public void onClick() {
                    //进度条
                    final ProgressDialog mProgressDialog = new ProgressDialog(getActivity());
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    mProgressDialog.setMessage(getString(R.string.dialog_multi_delete));
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.setIndeterminate(false);
                    mProgressDialog.show();

                    RxAsyncHelper rxAsyncHelper = new RxAsyncHelper("");
                    rxAsyncHelper.runInThread(new Func1() {
                        @Override
                        public Object call(Object o) {
                            SparseBooleanArray selectedList = mConvListAdapter.getSelectedList();
                            ArrayList<Map> list = new ArrayList();

                            for(int i = 0; i < selectedList.size() ; i ++){
                                int position = selectedList.keyAt(i);
                                Conversation conversation = mConvListAdapter.getItem(position);
                                if (conversation.getTopDate() <= 0) {
                                    //非置顶
                                    String address = conversation.getAddress();
                                    ContentValues contentValues = new ContentValues();
                                    contentValues.put(BaseModel.COLUMN_NAME_STATUS, Status.STATUS_DELETE);

                                    Map dataMap = new HashMap();
                                    dataMap.put(ConversationUtils.ADDRESS_KEY , address);
                                    dataMap.put(ConversationUtils.CONTENTVALUES_KEY ,contentValues);

                                    list.add(dataMap);
//                                ConversationUtils.update(getActivity(), address, contentValues, boxType);
                                }
                            }
                            ConversationUtils.updateBatch(getActivity() ,Conversations.Conversation.CONTENT_URI ,list);
                            return null;
                        }
                    }).runOnMainThread(new Func1() {
                        @Override
                        public Object call(Object o) {

                            mProgressDialog.dismiss();
                            Toast.makeText(getActivity(),getString(R.string.mutli_delete_success),Toast.LENGTH_SHORT).show();
                            NotifySmsActivity activity = (NotifySmsActivity)getActivity();
                            if(activity != null){
                                activity.changeMode(MessageModuleConst.NotifySmsActivityConst.OUT_MULTIDELETE_MODE);
                                changeMode(MessageModuleConst.NotifySmsActivityConst.OUT_MULTIDELETE_MODE);
                            }

                            return null;
                        }
                    }).subscribe();

                }
            });
            dialog.show();
        }
    }

}
