package com.cmicc.module_message.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.cmcc.cmrcs.android.ui.activities.GlobalSearchBaseActivity;
import com.cmcc.cmrcs.android.ui.adapter.GlobalSearchAdapter;
import com.cmcc.cmrcs.android.ui.contracts.GlobalSearchBaseContract;
import com.cmicc.module_message.ui.presenter.GlobalSearchMessagePresenter;
import com.cmicc.module_message.R;

import java.util.ArrayList;

/**
 * @anthor situ
 * @time 2017/10/16 14:33
 * @description 全局搜索聊天记录
 */

public class GlobalSearchMessageActivity extends GlobalSearchBaseActivity {

    @Override
    public GlobalSearchBaseContract.IPresenter initPresenter() {
        return new GlobalSearchMessagePresenter(this, this);
    }

    @Override
    public RecyclerView.Adapter initAdapter() {
        final GlobalSearchAdapter adapter =  new GlobalSearchAdapter(this);
        adapter.setOnItemClickListener(new GlobalSearchAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mPresenter.itemClick(adapter.getItem(position), adapter.getKeyWord());
            }
        });
        return adapter;
    }

    @Override
    public String initEditHint() {
        return getResources().getString(R.string.message_search);
    }

    @Override
    public String initResultHint() {
        return getResources().getString(R.string.chat_message);
    }

    @Override
    public void refresh(ArrayList list, String keyword) {
        GlobalSearchAdapter globalSearchAdapter = (GlobalSearchAdapter) mAdapter;
        globalSearchAdapter.setKeyWord(keyword);
        globalSearchAdapter.notifyDataSetChanged(list);
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, GlobalSearchMessageActivity.class);
        context.startActivity(intent);
    }

    public static void start(Context context, String keyword) {
        Intent intent = new Intent(context, GlobalSearchMessageActivity.class);
        intent.putExtra(KEY_KEYWORD, keyword);
        context.startActivity(intent);
    }
}
