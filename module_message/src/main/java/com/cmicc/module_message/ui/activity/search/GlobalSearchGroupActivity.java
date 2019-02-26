package com.cmicc.module_message.ui.activity.search;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.cmcc.cmrcs.android.ui.activities.GlobalSearchBaseActivity;
import com.cmcc.cmrcs.android.ui.adapter.GroupSearchAdapter;
import com.cmcc.cmrcs.android.ui.contracts.GlobalSearchBaseContract;
import com.cmcc.cmrcs.android.ui.presenters.GlobalSearchGroupPresenter;
import com.cmicc.module_message.R;

import java.util.ArrayList;

/**
 * @anthor situ
 * @time 2017/10/17 15:59
 * @description 全局搜索-查找群聊
 */

public class GlobalSearchGroupActivity extends GlobalSearchBaseActivity {

    @Override
    public GlobalSearchBaseContract.IPresenter initPresenter() {
        return new GlobalSearchGroupPresenter(this, this);
    }

    @Override
    public RecyclerView.Adapter initAdapter() {
        final GroupSearchAdapter groupSearchAdapter = new GroupSearchAdapter(this);
        groupSearchAdapter.setOnItemClickListener(new GroupSearchAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mPresenter.itemClick(groupSearchAdapter.getItem(position), groupSearchAdapter.getKeyWord());
            }
        });
        return groupSearchAdapter;
    }

    @Override
    public String initEditHint() {
        return getResources().getString(R.string.group_chat_search);
    }

    @Override
    public String initResultHint() {
        return getResources().getString(R.string.group_chat);
    }

    @Override
    public void refresh(ArrayList list, String keyword) {
        GroupSearchAdapter groupSearchAdapter = (GroupSearchAdapter) mAdapter;
        groupSearchAdapter.setKeyWord(keyword);
        groupSearchAdapter.notifyDataSetChanged(list);

    }

    public static void start(Context context) {
        Intent intent = new Intent(context, GlobalSearchGroupActivity.class);
        context.startActivity(intent);
    }

    public static void start(Context context, String keyword) {
        Intent intent = new Intent(context, GlobalSearchGroupActivity.class);
        intent.putExtra(KEY_KEYWORD, keyword);
        context.startActivity(intent);
    }
}
