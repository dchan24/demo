package com.cmicc.module_message.ui.activity.search;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.cmcc.cmrcs.android.ui.activities.GlobalSearchBaseActivity;
import com.cmcc.cmrcs.android.ui.adapter.PlatformSearchAdapter;
import com.cmcc.cmrcs.android.ui.contracts.GlobalSearchBaseContract;
import com.cmcc.cmrcs.android.ui.presenters.GlobalSearchPlatformPresenter;
import com.cmicc.module_message.R;

import java.util.ArrayList;

/**
 * @anthor situ
 * @time 2017/10/17 15:59
 * @description 全局搜索-查找群聊
 */

public class GlobalSearchPlatformActivity extends GlobalSearchBaseActivity {

    @Override
    public GlobalSearchBaseContract.IPresenter initPresenter() {
        return new GlobalSearchPlatformPresenter(this, this);
    }

    @Override
    public RecyclerView.Adapter initAdapter() {
        final PlatformSearchAdapter platformSearchAdapter = new PlatformSearchAdapter(this);
        platformSearchAdapter.setOnItemClickListener(new PlatformSearchAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mPresenter.itemClick(platformSearchAdapter.getItem(position), platformSearchAdapter.getKeyWord());
            }
        });
        return platformSearchAdapter;
    }

    @Override
    public String initEditHint() {
        return getResources().getString(R.string.chat_platform_search);
    }

    @Override
    public String initResultHint() {
        return getResources().getString(R.string.chat_platform);
    }

    @Override
    public void refresh(ArrayList list, String keyword) {
        PlatformSearchAdapter platformSearchAdapter = (PlatformSearchAdapter) mAdapter;
        platformSearchAdapter.setKeyWord(keyword);
        platformSearchAdapter.notifyDataSetChanged(list);

    }

    public static void start(Context context) {
        Intent intent = new Intent(context, GlobalSearchPlatformActivity.class);
        context.startActivity(intent);
    }

    public static void start(Context context, String keyword) {
        Intent intent = new Intent(context, GlobalSearchPlatformActivity.class);
        intent.putExtra(KEY_KEYWORD, keyword);
        context.startActivity(intent);
    }
}
