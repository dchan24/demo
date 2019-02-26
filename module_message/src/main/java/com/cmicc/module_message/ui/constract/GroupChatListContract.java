package com.cmicc.module_message.ui.constract;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;

import com.chinamobile.app.yuliao_business.model.Employee;
import com.chinamobile.app.yuliao_business.model.GroupInfo;
import com.chinamobile.icloud.im.sync.model.RawContact;
import com.cmcc.cmrcs.android.ui.contracts.BasePresenter;
import com.cmcc.cmrcs.android.ui.contracts.BaseView;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by tianshuai on 2017/5/23.
 */

public interface GroupChatListContract {
    //GroupChatFragemnt 实现
    interface IView extends BaseView<IPresenter> {
        // 数据加载完成，更新列表数据
        void updateListView(Cursor cursor);
        //获取LoaderManager
        LoaderManager getLoaderManger();
        //显示分享名片的提示框
        void showVcardExportDialog(String[] strings,String name);
        //显示群聊的搜索结果
        void showSearchResult(ArrayList<GroupInfo> groupInfos,CharSequence key);
    }

    interface IPresenter extends BasePresenter {
        // 给IPresenter设置IView，调用IView类的方法
        void setView(IView v);
        void setLoaderManager(LoaderManager loaderManager);
        void openItem(Context context, GroupInfo groupInfo);

        // 用于转发
        void handleMessageForward(Bundle bundle, GroupInfo groupInfo);

        //分享此名片到群聊
        void handleCardToChat(String toNumber, RawContact rawContact);
        // 发送名片
        void submitVcard();
        // 设置分享名片 （公司，职位，邮箱）选中的字段
        void setChecks(Map<String, Boolean> mapCheck);
        // 生成要发送的名片信息
        void submitVCardFromContactDetail(String number, Employee employee, boolean isGroup);

        void search(CharSequence key);

    }
}
