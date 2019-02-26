package com.cmicc.module_message;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.app.module.base.Module;
import com.app.module.proxys.modulemessage.IMessageServer;
import com.app.module.proxys.modulemessage.IMessageUI;
import com.chinamobile.app.yuliao_business.model.GroupMember;
import com.chinamobile.app.yuliao_common.application.MyApplication;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.cmicc.module_message.ui.activity.ChooseFileSendActivity;
import com.cmicc.module_message.ui.activity.EditGroupPageActivity;
import com.cmicc.module_message.ui.activity.GlobalSearchMessageActivity;
import com.cmicc.module_message.ui.activity.GroupChatListMergaActivity;
import com.cmicc.module_message.ui.activity.GroupChatSearchActivity;
import com.cmicc.module_message.ui.activity.GroupMemberListActivity;
import com.cmicc.module_message.ui.activity.GroupSettingActivity;
import com.cmicc.module_message.ui.activity.GroupStrangerActivity;
import com.cmicc.module_message.ui.activity.MailMsgListActivity;
import com.cmicc.module_message.ui.activity.MailOAMsgListActivity;
import com.cmicc.module_message.ui.activity.MailOASummaryActivity;
import com.cmicc.module_message.ui.activity.MessageSearchActivity;
import com.cmicc.module_message.ui.activity.NonEntryGroupAtivity;
import com.cmcc.cmrcs.android.ui.callback.SendAudioTextCallBack;
import com.cmcc.cmrcs.android.ui.interfaces.AudioListener;
import com.cmcc.cmrcs.android.ui.fragments.HomeFragment;
import com.cmicc.module_message.ui.activity.GDLocationActvity;
import com.cmicc.module_message.ui.activity.MessageDetailActivity;
import com.cmicc.module_message.ui.activity.NoRcsGroupMemberActivity;
import com.cmicc.module_message.ui.activity.NotifySmsActivity;
import com.cmicc.module_message.ui.activity.search.GlobalSearchFunctionActivity;
import com.cmicc.module_message.ui.activity.search.GlobalSearchGroupActivity;
import com.cmicc.module_message.ui.activity.search.GlobalSearchPlatformActivity;
import com.cmicc.module_message.ui.broadcast.MsgNotificationReceiver;
import com.cmicc.module_message.ui.fragment.ConvListFragment;
import com.cmicc.module_message.ui.fragment.GroupChatFragment;
import com.cmicc.module_message.ui.fragment.LabelGroupChatFragment;
import com.cmicc.module_message.ui.fragment.MessageAudioTextFragment;
import com.cmicc.module_message.ui.fragment.MessageEditorFragment;
import com.cmicc.module_message.ui.fragment.MmsSmsFragment;
import com.cmicc.module_message.ui.fragment.PcMessageFragment;
import com.cmicc.module_message.ui.fragment.PublicAccountChatFrament;
import com.cmicc.module_message.ui.listener.UpdateCallingViewListener;
import com.cmicc.module_message.utils.CallViewListenerUtil;
import com.cmicc.module_message.utils.RcsAudioPlayer;
import com.constvalue.MessageModuleConst;

import java.util.ArrayList;

import static com.cmcc.cmrcs.android.ui.activities.GlobalSearchBaseActivity.KEY_KEYWORD;

/**
 * Created by lgh on 2017/10/12.
 */

public class MessageModule extends Module<IMessageUI,IMessageServer>{
    public static final String TAG = "MessageModule";
    @Override
    public String getName() {
        return "MessageModule";
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public IMessageUI getUiInterface() {
        return mIMessageUI;
    }

    private IMessageUI mIMessageUI = new IMessageUI() {

        @Override
        public void clearClickConvAddress(Fragment MessageListFragment) {
            if(MessageListFragment instanceof  ConvListFragment){
                ((ConvListFragment)MessageListFragment).clearClickConvAddress();
            }
        }

        @Override
        public void startLocationActivityForResult(Activity activity, int requestCode,Bundle Extras) {
            if(activity == null){
                return;
            }
            Intent intent = new Intent(activity, GDLocationActvity.class);
            if(Extras!=null) {
                intent.putExtras(Extras);
            }
            activity.startActivityForResult(intent,requestCode);
        }

        @Override
        public Fragment getAudioTextFragment(SendAudioTextCallBack sendAudioTextCallBack) {
            MessageAudioTextFragment messageAudioTextFragment = new MessageAudioTextFragment();
            messageAudioTextFragment.sendAudioTextCallback(sendAudioTextCallBack);
            return messageAudioTextFragment;
        }

        @Override
        public void setAudioFragmentPageType(Fragment audioFragment,int pageType) {
            if(audioFragment instanceof MessageAudioTextFragment){
                ((MessageAudioTextFragment)audioFragment).setFragmentPageType(pageType);
            }
        }

        @Override
        public void onSendToFragmentPageEvent(Fragment fragment,int eventType) {
            if(fragment instanceof MessageAudioTextFragment){
                ((MessageAudioTextFragment)fragment).onEvent(eventType);
            }else if(fragment instanceof GroupChatFragment){
                ((GroupChatFragment)fragment).onEvent(eventType);
            }
        }

//        @Override
//        public void showNotifySmsActivity(Context context, int source) {
//            NotifySmsActivity.show(context,source);
//        }

        @Override
        public Intent getNotifySmsActivityIntent(Context context,int source) {
            Intent intent = new Intent(context, NotifySmsActivity.class);
            intent.putExtra(NotifySmsActivity.SOURCE,source);
            return intent;
        }

        @Override
        public Fragment getFragment(int fragmentType,Bundle bundle) {
            Fragment fragment = null;
            if(fragmentType == MessageModuleConst.FragmentType.CONVY_LIST_FRAGMENT){
                fragment = new ConvListFragment();
            }else if(fragmentType == MessageModuleConst.FragmentType.GROUP_CHAT_FRAGMENT){
                fragment = GroupChatFragment.newInstance(bundle);
            }else if(fragmentType == MessageModuleConst.FragmentType.PUBLIC_ACCOUNT_FRAGMENT){
                fragment = PublicAccountChatFrament.newInstance(bundle);
            }else if(fragmentType == MessageModuleConst.FragmentType.MSM_SMS_FRAGMENT){
                fragment = MmsSmsFragment.newInstance(bundle);
            }else if(fragmentType == MessageModuleConst.FragmentType.LABEL_GROUP_CHAT_FRAGMENT){
                fragment = LabelGroupChatFragment.newInstance(bundle);
            }else if(fragmentType == MessageModuleConst.FragmentType.PC_MESSAGE_FRAGMENT){
                fragment = PcMessageFragment.newInstance(bundle);
            }else if(fragmentType == MessageModuleConst.FragmentType.MESSAGE_EDITOR_FRAGMENG){
                fragment = MessageEditorFragment.newInstance(bundle);
            }
            return fragment;
        }

        @Override
        public void showCallingView(Fragment convListFragment, boolean isShow) {
            if(convListFragment instanceof  ConvListFragment){
                ((ConvListFragment)convListFragment).showCallingView(isShow);
            }
        }

        @Override
        public void updateCallingViewStatus(int status, long base) {
            UpdateCallingViewListener listener = CallViewListenerUtil.getInstance().getmListener();
            if (listener != null) {
                listener.callStatus(status, base);
            }
        }

        @Override
        public void goMessageDetailActivity(Context context, Bundle bundle, int flags) {
            if(context == null){
                return;
            }
            Intent intent = new Intent(context, MessageDetailActivity.class);
            intent.putExtras(bundle);
            intent.addFlags(flags);
            context.startActivity(intent);
        }

        @Override
        public void goMessageDetailActivity(Context context, Bundle bundle) {
            if(context == null){
                return;
            }
            try {
                Intent intent = new Intent(context, MessageDetailActivity.class);
                intent.putExtras(bundle);
                context.startActivity(intent);
            }catch (Exception e){
                e.printStackTrace();
                LogF.e(TAG ,"jump to MessageDetailActivity fail:"+e.getMessage());
                Intent intent = new Intent(context, MessageDetailActivity.class);
                intent.putExtras(bundle);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }

        @Override
        public boolean isAppointedActivity(Activity activity, int appointedActivity) {
            if(activity==null){
                return false;
            }
            switch (appointedActivity){
                case MessageModuleConst.ActivityType.MESSAGEDETAIL_ACTIVITY:
                    if(activity instanceof MessageDetailActivity){
                        return true;
                    }
                    break;
            }
            return false;
        }

        @Override
        public void goNoRcsGroupMemberActivity(Context context,String groupID,String identify) {
            if(context == null){
                return;
            }
            Intent intent = new Intent(context, NonEntryGroupAtivity.class);
            intent.putExtra(NoRcsGroupMemberActivity.GROUPID, groupID);
            intent.putExtra(NoRcsGroupMemberActivity.GROUPURI, identify);
            context.startActivity(intent);
        }

        @Override
        public void goMessageSearchActivity(Context context, int boxType, String address, String keyword, int count, String title) {
            if(context == null){
                return;
            }
            Intent intent = new Intent(context, MessageSearchActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(MessageSearchActivity.BUNDLE_KEY_ADDRESS, address);
            bundle.putString(MessageSearchActivity.BUNDLE_KEY_KEYWORD, keyword);
            bundle.putInt(MessageSearchActivity.BUNDLE_KEY_COUNT, count);
            bundle.putString(MessageSearchActivity.BUNDLE_KEY_TITLE, title);
            bundle.putInt(MessageSearchActivity.BUNDLE_KEY_BOXTYPE, boxType);
            intent.putExtras(bundle);
            context.startActivity(intent);
        }

        @Override
        public void goGlobalSearchMessageActivity(Context context, String keyword) {
            if(context == null){
                return;
            }
            Intent intent = new Intent(context, GlobalSearchMessageActivity.class);
            intent.putExtra(KEY_KEYWORD, keyword);
            context.startActivity(intent);
        }

        @Override
        public void goGroupStrangerActivity(Context context, String num, String completeAddress, String name, String groupId, String groupName, String groupCard) {
            if(context == null){
                return;
            }
            Intent intent = new Intent(context, GroupStrangerActivity.class);
            intent.putExtra("name",name);
            intent.putExtra("num",num);
            intent.putExtra("completeAddress",completeAddress);   // 包含国家码的地址
            intent.putExtra("groupName",groupName);
            intent.putExtra("groupCard",groupCard);
            intent.putExtra("groupId",groupId);
            context.startActivity(intent);
        }

        @Override
        public void goGlobalSearchFunctionActivity(Context context, String keyword) {
            if(context == null){
                return;
            }
            Intent intent = new Intent(context, GlobalSearchFunctionActivity.class);
            intent.putExtra(KEY_KEYWORD, keyword);
            context.startActivity(intent);
        }

        @Override
        public void goGlobalSearchPlatformActivity(Context context, String keyword) {
            if(context == null){
                return;
            }
            Intent intent = new Intent(context, GlobalSearchPlatformActivity.class);
            intent.putExtra(KEY_KEYWORD, keyword);
            context.startActivity(intent);
        }

        @Override
        public void goGlobalSearchGroupActivity(Context context, String keyword) {
            Intent intent = new Intent(context, GlobalSearchGroupActivity.class);
            intent.putExtra(KEY_KEYWORD, keyword);
            context.startActivity(intent);
        }

        @Override
        public void goMailMsgListActivity(Context context) {
            Intent intent = new Intent(context ,MailMsgListActivity.class);
            context.startActivity(intent);
        }

        @Override
        public void goMailOAMsgListActivity(Context context, String address, int boxType) {
            Intent intent = new Intent(context ,MailOAMsgListActivity.class);
            intent.putExtra("address", address);
            intent.putExtra("boxtype", boxType);
            context.startActivity(intent);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }
    };

    @Override
    public IMessageServer getServiceInterface() {
        return mIMessageServer;
    }

    private IMessageServer mIMessageServer =new IMessageServer(){
        @Override
        public String getClassName(int classType) {
            if(classType == MessageModuleConst.ClassType.GROUP_CHAT_MESSAGE_FRAGMENT_CLASS){
                return GroupChatFragment.class.getName();
            }else if(classType == MessageModuleConst.ClassType.PUBLIC_ACCOUNT_CHAT_FRAGMENT_CLASS){
                return PublicAccountChatFrament.class.getName();
            }else if(classType == MessageModuleConst.ClassType.MSM_SMS_FRAGMENT_CLASS){
                return MmsSmsFragment.class.getName();
            }else if(classType == MessageModuleConst.ClassType.LABEL_GROUP_CHAT_FRAGMENT_CLASS){
                return LabelGroupChatFragment.class.getName();
            }else if(classType == MessageModuleConst.ClassType.PC_MESSAGE_FRAGMENT_CLASS){
                return PcMessageFragment.class.getName();
            }else if(classType == MessageModuleConst.ClassType.MESSAGE_EDITOR__FRAGMENT_CLASS){
                return MessageEditorFragment.class.getName();
            }
            return "";
        }

        @Override
        public boolean isShowCallView(Fragment fragment) {
            if(fragment instanceof  ConvListFragment){
                return ((ConvListFragment)fragment).isShowCallingView();
            }
            return false;
        }

        @Override
        public Intent getIntentToActivity(Context context,int toActivity) {
            Intent intent = null;
            if(context == null){
                return intent;
            }
            switch (toActivity){
                case MessageModuleConst.ActivityType.MESSAGEDETAIL_ACTIVITY:
                    intent = new Intent(context,MessageDetailActivity.class);
                    break;
                case MessageModuleConst.ActivityType.GROUPSETTING_ACTIVITY:
                    intent = new Intent(context,GroupSettingActivity.class);
                    break;
                case MessageModuleConst.ActivityType.EDITGROUPPAGE_ACTIVITY:
                    intent = new Intent(context,EditGroupPageActivity.class);
                    break;
                case MessageModuleConst.ActivityType.GROUPCHATLISTMERGA_ACTIVITY:
                    intent = new Intent(context,GroupChatListMergaActivity.class);
                    break;
                case MessageModuleConst.ActivityType.GROUPCHATSEARCH_ACTIVITY:
                    intent = new Intent(context,GroupChatSearchActivity.class);
                    break;

                case MessageModuleConst.ActivityType.CHOOSEFILESEND_ACTIVITY:
                    intent = new Intent(Intent.ACTION_GET_CONTENT);

                    intent.setType("**/*//*");
                    intent.setClass(context, ChooseFileSendActivity.class);
                    intent.putExtra("ACTIVITY_ANIMATION_TYPE", 0);
                    break;
                case MessageModuleConst.ActivityType.MAILOASUMMARY_ACTIVITY:
                    intent = new Intent(context,MailOASummaryActivity.class);
                    break;

            }

            return intent;
        }

        @Override
        public void RcsAudioPlayerStop(Context context) {
            RcsAudioPlayer.getInstence(context).stop();
        }

        @Override
        public void RcsAudioPlayerPlay(Context context,String path, AudioListener listener) {
            try {
                RcsAudioPlayer.getInstence(context).play(path, listener);
            }catch (Exception e){
                RcsAudioPlayer.getInstence(MyApplication.getApplication()).abandonAudioFocus();// 释放音频焦点
            }
        }

        @Override
        public ArrayList<String> getListGroupMemberStr() {
            return GroupMemberListActivity.mListGroupMemberStr;
        }

        @Override
        public ArrayList<GroupMember> getListGroupMember() {
            return GroupMemberListActivity.mListGroupMember;
        }

        @Override
        public void clearMsgNotification(Context context) {
            MsgNotificationReceiver.clearMsgNotification(context);
        }

        @Override
        public void setMsgMotifIsCurrentConvList(boolean isCurrentConvList) {
            MsgNotificationReceiver.sIsCurrentConvList = true;
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }
    };
}
