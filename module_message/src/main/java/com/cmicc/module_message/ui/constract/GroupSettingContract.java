package com.cmicc.module_message.ui.constract;

import android.content.Intent;
import android.graphics.drawable.Drawable;

import com.chinamobile.app.yuliao_business.model.GroupMember;
import com.cmcc.cmrcs.android.ui.contracts.BasePresenter;

import java.util.ArrayList;
import java.util.List;

/**
 * @anthor situ
 * @time 2017/5/17 15:25
 * @description 群聊设置
 */

public interface GroupSettingContract {
    interface IView {
        /**
         * 刷新群聊名称
         */
        void updateGroupName(String name);

        /**
         * 刷新群成员
         */
        void updateGroupMemberList(List<GroupMember> groupMembers);

        /**
         * 刷新我的群名片
         */
        void updateGroupCard(String name);

        void finish();

        void toast(CharSequence sequence);

        void setUndisturbSwitch(boolean checked);

        void chairmanRefresh();

        void updateThemeThumb(Drawable drawable);

        void updateUndisturbFinish(boolean isOk);
    }

    interface IPresenter extends BasePresenter {
        /**
         * 获取群名
         */
        String getGroupName();

        /**
         * 获取群名片
         */
        String getGroupCard();

        /**
         * 加载群成员列表
         */
        void loadMemberList();

        ArrayList<GroupMember> getGroupMembers();

        ArrayList<String> getGroupMemberList();

        void itemClick(GroupMember groupMember);

        boolean isChairman();

        void addMemberToGroup(Intent data, boolean isAdd);

        String getAddress();

        void updateGroupName(String name);

        void updateGroupCard(String name);

        boolean getUndisturbSetting(String address);

        void setUndisturbSettingLocal(String address, boolean disturb);

        void setUndisturbSettingServer(String status);

        void memberExitGroup();

        void chairmanExitGroup();

        void rcsImSessDissolve();

        /**
         * 设置标记位，接收到转让群成功回调是否退出群
         * @param exit
         */
        void setExitAfterTransfer(boolean exit);

        void setupThemeThumb();

        void clearAllMsg();

    }
}
