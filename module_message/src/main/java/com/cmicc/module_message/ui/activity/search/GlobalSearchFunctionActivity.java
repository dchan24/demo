package com.cmicc.module_message.ui.activity.search;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.cmcc.cmrcs.android.ui.activities.GlobalSearchBaseActivity;
import com.cmcc.cmrcs.android.ui.adapter.FunctionSearchAdapter;
import com.cmcc.cmrcs.android.ui.contracts.GlobalSearchBaseContract;
import com.cmicc.module_message.ui.presenter.GlobalSearchFunctionPresenter;
import com.cmicc.module_message.R;

import java.util.ArrayList;

/**
 * @anthor situ
 * @time 2017/10/17 15:59
 * @description 全局搜索-查找群聊
 */

public class GlobalSearchFunctionActivity extends GlobalSearchBaseActivity {

    @Override
    public GlobalSearchBaseContract.IPresenter initPresenter() {
        return new GlobalSearchFunctionPresenter(this, this);
    }

    @Override
    public RecyclerView.Adapter initAdapter() {
        final FunctionSearchAdapter functionSearchAdapter = new FunctionSearchAdapter(this);
        functionSearchAdapter.setOnItemClickListener(new FunctionSearchAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mPresenter.itemClick(functionSearchAdapter.getItem(position), functionSearchAdapter.getKeyWord());
            }
        });
        return functionSearchAdapter;
    }

    @Override
    public String initEditHint() {
        return getResources().getString(R.string.function_search_tip);
    }

    @Override
    public String initResultHint() {
        return getResources().getString(R.string.function_search);
    }

    @Override
    public void refresh(ArrayList list, String keyword) {
        FunctionSearchAdapter functionSearchAdapter = (FunctionSearchAdapter) mAdapter;
        functionSearchAdapter.setKeyWord(keyword);
        functionSearchAdapter.notifyDataSetChanged(list);

    }

    public static void start(Context context) {
        Intent intent = new Intent(context, GlobalSearchFunctionActivity.class);
        context.startActivity(intent);
    }

    public static void start(Context context, String keyword) {
        Intent intent = new Intent(context, GlobalSearchFunctionActivity.class);
        intent.putExtra(KEY_KEYWORD, keyword);
        context.startActivity(intent);
    }
}
