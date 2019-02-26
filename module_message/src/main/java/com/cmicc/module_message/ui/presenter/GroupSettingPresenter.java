package com.cmicc.module_message.ui.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.widget.Toast;

import com.app.module.proxys.moduleaboutme.AboutMeProxy;
import com.app.module.proxys.modulecontact.ContactProxy;
import com.app.module.proxys.modulemessage.MessageProxy;
import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.utils.RxAsyncHelper;
import com.chinamobile.app.yuliao_business.logic.common.LogicActions;
import com.chinamobile.app.yuliao_business.model.BaseModel;
import com.chinamobile.app.yuliao_business.model.Employee;
import com.chinamobile.app.yuliao_business.model.GroupInfo;
import com.chinamobile.app.yuliao_business.model.GroupMember;
import com.chinamobile.app.yuliao_business.provider.Conversations;
import com.chinamobile.app.yuliao_business.util.ConversationUtils;
import com.chinamobile.app.yuliao_business.util.GroupChatUtils;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.utils.Log;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_common.utils.SharePreferenceUtils;
import com.chinamobile.app.yuliao_contact.model.BaseContact;
import com.chinamobile.app.yuliao_contact.model.SimpleContact;
import com.chinamobile.app.yuliao_core.db.LoginDaoImpl;
import com.chinamobile.app.yuliao_core.util.NumberUtils;
import com.cmcc.cmrcs.android.contact.data.ContactsCache;
import com.cmcc.cmrcs.android.glide.GlidePhotoLoader;
import com.cmcc.cmrcs.android.glide.GlidePhotoLoader.LoadPhotoCallback;
import com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity;

import com.cmicc.module_message.ui.activity.GroupStrangerActivity;


import com.cmcc.cmrcs.android.ui.control.GroupChatControl;
import com.cmcc.cmrcs.android.ui.logic.common.UIObserver;
import com.cmcc.cmrcs.android.ui.logic.common.UIObserverManager;
import com.cmcc.cmrcs.android.ui.utils.ContactSelectorUtil;
import com.cmcc.cmrcs.android.ui.utils.LoginUtils;
import com.cmcc.cmrcs.android.ui.utils.ManagePersonalCfg;
import com.cmcc.cmrcs.android.ui.utils.MessageThemeUtils;
import com.cmcc.cmrcs.android.ui.utils.NickNameUtils;
import com.cmcc.cmrcs.android.ui.utils.PhoneUtils;
import com.cmcc.cmrcs.android.ui.utils.UmengUtil;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.activity.GroupSettingActivity;
import com.cmicc.module_message.ui.constract.GroupSettingContract;
import com.constvalue.MessageModuleConst;
import com.juphoon.cmcc.app.lemon.MtcCliConstants;
import com.juphoon.cmcc.app.lemon.MtcImConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import rx.functions.Func1;

import static com.cmcc.cmrcs.android.ui.utils.ContactSelectorUtil.SOURCE_ADD_CONTACT_TO_GROUP;
import static com.constvalue.MessageModuleConst.GroupSettingActivity.BUNDLE_KEY_ADDRESS;
import static com.constvalue.MessageModuleConst.GroupSettingActivity.BUNDLE_KEY_EXIT_GROUP;
import static com.constvalue.MessageModuleConst.GroupSettingActivity.REQUEST_CODE_ADD_MEMBER;
import static com.constvalue.MessageModuleConst.GroupSettingActivity.REQUEST_CODE_DELETE_MEMBER;

/**
 * @anthor situ
 * @time 2017/5/17 15:29
 * @description 群聊设置
 */
public class GroupSettingPresenter implements GroupSettingContract.IPresenter, LoaderManager.LoaderCallbacks<Cursor> {

    private static final int ID = new Random(System.currentTimeMillis()).nextInt();
    private static final String TAG = "GroupSettingPresenter";

    private Context mContext;
    private GroupSettingContract.IView mView;
    private LoaderManager mLoaderManager;
    private String mAddress; // 会话标识
    private String mGroupName; // 会话名
    private String mGroupCard; //群名片
    private ArrayList<GroupMember> mGroupMembers;
    private ArrayList<String> mListForDelete;
    private boolean mIsChairman; // 自己是否是群主
    private Bundle mBundle;

    private boolean isExitAfterTransfer;

    private String mLoginUserAddress;

    private boolean isEPgroup = false;//是否为企业群
    private String delGroupMemberNameS ;

    private UIObserver mUIObserver = new UIObserver() {
        @Override
        protected void onReceiveAction(int action, Intent intent) {
            final String groupId = intent.getStringExtra(LogicActions.GROUP_CHAT_ID);
            Log.d(TAG, "onReceiveAction action = " + action + ", groupId = " + groupId);
            LogF.d(TAG, "onReceiveAction action = " + action + ", groupId = " + groupId);
            int code = -1;
            String title = "";
            LogF.d(TAG , "onReceiveAction mAddress : "+mAddress + " groupId : "+ groupId);
            if (mAddress.equals(groupId)) {
                switch (action) {
                    case LogicActions.GROUP_CHAT_ADD_PARTICIPANT_FAIL:
                        break;
                    case LogicActions.GROUP_CHAT_ADD_PARTICIPANT_LIST_FAIL:
                        mView.toast(mContext.getString(R.string.invite_defail));
                        break;
                    case LogicActions.GROUP_CHAT_ADD_PARTICIPANT_OK_CB:
                        mView.finish();
                        break;
                    case LogicActions.GROUP_CHAT_ADD_PARTICIPANT_FAIL_CB:
                        title = mContext.getString(R.string.invite_defail);
                        code = intent.getIntExtra(LogicActions.STATE_CODE, -1);
                        if (code == MtcImConstants.MTC_IM_ERR_JOINED_GRP_FULL) {
                            title = mContext.getString(R.string.his_gourp_limit_upper);
                        } else if (code == MtcImConstants.MTC_IM_ERR_EXCEED_MAX_PARTP) {
                            title = mContext.getString(R.string.group_member_limit_upper);
                        }
                        mView.toast(title);
                        break;
                    case LogicActions.GROUP_CHAT_ERROR_GONE:
                        mView.toast(mContext.getString(R.string.group_miss));
                        mView.finish();
                        break;
                    case LogicActions.GROUP_CHAT_ERROR_EXPELLED:
                        mView.finish();
                        setUndisturbSettingLocal(getAddress(), false);//退群后把本地数据更改为非静默状态
                        break;
                    case LogicActions.GROUP_CHAT_REJECTED_CB:
                        int errorCode = intent.getIntExtra(LogicActions.GROUP_CHAT_ERROR_CODE, -1);
                        if (errorCode == MtcImConstants.MTC_IM_ERR_LEAVED) {
                            mView.finish();
                        }
                        setUndisturbSettingLocal(getAddress(), false);//退群后把本地数据更改为非静默状态
                        break;
                    case LogicActions.GROUP_CHAT_MODIFY_CHAIRMAN_SUCCESS_CB:
                        //删除群短信
                        LogF.d(TAG, "isExitAfterTransfer = 147 " + isExitAfterTransfer);
                        GroupChatUtils.delAllGroupMessageConversation(mContext);
                        if (!isExitAfterTransfer) { //群主非退出群时才“已转让”，若是退出群，则提示“已转让群主并退出群聊”
                            mView.toast(mContext.getString(R.string.already_transfer));
                        }
                        mIsChairman = false;
                        if (isExitAfterTransfer) {
                            memberExitGroup();
                        }
                        break;
                    case LogicActions.GROUP_CHAT_BECOME_CHAIRMAN_SUCCESS_CB:
                        mIsChairman = true;
                        break;
                    case LogicActions.GROUP_CHAT_MODIFY_CHAIRMAN_FAIL_CB:
                        title = mContext.getString(R.string.transfer_defail);
                        code = intent.getIntExtra(LogicActions.STATE_CODE, -1);
                        if (code == 59923) {
                            title = mContext.getString(R.string.his_manage_group_num_limit_upper);
                        }
                        mView.toast(title);
                        break;
                    case LogicActions.GROUP_CHAT_LEAVE_FAIL_CB:
                    case LogicActions.GROUP_CHAT_LEAVE_FAIL:
                        mView.toast(mContext.getString(R.string.leave_group_defail));
                        break;
                    case LogicActions.GROUP_CHAT_LEAVE_OK_CB:  // 群成员主动退群 不保留信息
                        LogF.d(TAG, "onReceiveAction action = " + action + ", groupId = " + groupId + " 退出群");
//                        Intent i = new Intent(mContext, MessageDetailActivity.class);
                        Intent i = MessageProxy.g.getServiceInterface().getIntentToActivity(mContext,MessageModuleConst.ActivityType.MESSAGEDETAIL_ACTIVITY);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        i.putExtra("finish", true);
                        mContext.startActivity(i);
                        LogF.d(TAG, "isExitAfterTransfer = 75 " + isExitAfterTransfer);
                        if (!isExitAfterTransfer) {
                            mView.toast(mContext.getString(R.string.leave_group));
                        } else {
                            mView.toast(mContext.getString(R.string.assig_and_bowout));
                        }
                        setUndisturbSettingLocal(getAddress(), false);//退群后把本地数据更改为非静默状态
                        break;
                    case LogicActions.GROUP_CHAT_ERROR_NETWORK:
                        mView.toast(mContext.getString(R.string.public_net_exception));
                        break;
                    case LogicActions.GROUP_CHAT_DISSOLVE_OK_CB: // 群主解散群成功 不保留信息
                        LogF.d(TAG, "onReceiveAction action = " + action + ", groupId = " + groupId + " 群主解散群");
//                        Intent in = new Intent(mContext, MessageDetailActivity.class);
                        Intent in = MessageProxy.g.getServiceInterface().getIntentToActivity(mContext,MessageModuleConst.ActivityType.MESSAGEDETAIL_ACTIVITY);
                        in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        in.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        in.putExtra("finish", true);
                        mContext.startActivity(in);
                        mView.toast(mContext.getString(R.string.group_dismiss));
                        break;
                    case LogicActions.GROUP_CHAT_DISSOLVE_FAIL_CB:
                    case LogicActions.GROUP_CHAT_DISSOLVE_FAIL:
                        mView.toast(mContext.getString(R.string.group_miss_defail));
                        break;
                    case LogicActions.GROUP_CHAT_ERROR: // 群服务异常
                        break;
                    case LogicActions.GROUP_CHAT_SUBJECT_CHANGED_CB: // 群名称修改
                        String groupName = intent.getStringExtra(LogicActions.GROUP_CHAT_SUBJECT);
                        mView.updateGroupName(groupName);
                        break;
                    case LogicActions.GROUP_BE_FOYRCED_DISSOLUTION: // 群被迫解散（群主移除所有群成员）
                        mView.finish();
                        break;
                    case LogicActions.GROUP_KIKED_OUT_GOROUP: // 被踢出群
                        LogF.d(TAG , "onReceiveAction "+ mContext.getString(R.string.you_leave_group));
                        mView.toast(mContext.getString(R.string.you_leave_group));
                        mView.finish();
                        break;
                    case LogicActions.GROUP_CHAT_EXPELLING_PARTICIPANT_OK_CB: // 踢人出群成功
                       if(GroupChatUtils.getGroupInfo(mContext , groupId).getType() == MtcImConstants.EN_MTC_GROUP_TYPE_GENERAL && !TextUtils.isEmpty(delGroupMemberNameS)){ // 放在这里的原因：发现企业群的时候（可以在企业管理后台移除），这个时候群主端收到的状态值也是4。但移除群成员的有可能不是群主。
                           GroupChatUtils.insertSystemMsg(mContext, groupId , GroupChatUtils.SYS_TYPE_DELETED_MEMBER, 0 , delGroupMemberNameS.substring(0, delGroupMemberNameS.length() - 1));
                       }else{
                           LogF.d(TAG, "delGroupMemberNameS is null ");
                       }
                        break;
                    default:

                }

            }
        }
    };

    public GroupSettingPresenter(Context context, GroupSettingContract.IView view, LoaderManager loaderManager, Bundle bundle) {
        mContext = context;
        mView = view;
        mBundle = bundle;
        mLoaderManager = loaderManager;
        mGroupMembers = new ArrayList<GroupMember>();
        mListForDelete = new ArrayList<String>();

        ArrayList<Integer> actions = new ArrayList<Integer>();
        // 退出操作
        actions.add(LogicActions.GROUP_CHAT_LEAVE_OK_CB);
        actions.add(LogicActions.GROUP_CHAT_LEAVE_FAIL_CB);
        actions.add(LogicActions.GROUP_CHAT_LEAVE_FAIL);
        // 添加群成员
        actions.add(LogicActions.GROUP_CHAT_ADD_PARTICIPANT_FAIL);
        actions.add(LogicActions.GROUP_CHAT_ADD_PARTICIPANT_LIST_FAIL);
        actions.add(LogicActions.GROUP_CHAT_ADD_PARTICIPANT_OK_CB);
        actions.add(LogicActions.GROUP_CHAT_ADD_PARTICIPANT_FAIL_CB);
        // 移交群操作
        actions.add(LogicActions.GROUP_CHAT_MODIFY_CHAIRMAN_SUCCESS_CB);
        actions.add(LogicActions.GROUP_CHAT_MODIFY_CHAIRMAN_FAIL_CB);
        actions.add(LogicActions.GROUP_CHAT_ERROR_NO);
        actions.add(LogicActions.GROUP_CHAT_ERROR_NETWORK);
        actions.add(LogicActions.GROUP_CHAT_ERROR_OTHER);
        // 移除群成员
        actions.add(LogicActions.GROUP_CHAT_EXPELLING_PARTICIPANT);
        // 接受群主
        actions.add(LogicActions.GROUP_CHAT_ACCEPT_CHAIRMAN);
        //解散群
        actions.add(LogicActions.GROUP_CHAT_DISSOLVE_OK_CB);
        actions.add(LogicActions.GROUP_CHAT_DISSOLVE_FAIL_CB);
        //群没有
        actions.add(LogicActions.GROUP_CHAT_ERROR_GONE);
        //被踢
        actions.add(LogicActions.GROUP_CHAT_ERROR_EXPELLED);

        actions.add(LogicActions.GROUP_CHAT_REJECTED_CB);
        //成为群主
        actions.add(LogicActions.GROUP_CHAT_BECOME_CHAIRMAN_SUCCESS_CB);
        actions.add(LogicActions.GROUP_CHAT_DISSOLVE_FAIL_CB);
        actions.add(LogicActions.GROUP_CHAT_ERROR); // 群服务异常
        actions.add(LogicActions.GROUP_CHAT_SUBJECT_CHANGED_CB);// 群名称修改
        actions.add(LogicActions.GROUP_BE_FOYRCED_DISSOLUTION);// 群被迫解散
        actions.add(LogicActions.GROUP_KIKED_OUT_GOROUP);// 被踢出群
        actions.add(LogicActions.GROUP_CHAT_EXPELLING_PARTICIPANT_OK_CB);// 踢人出群
        UIObserverManager.getInstance().registerObserver(mUIObserver, actions);
    }

    private void init(Bundle bundle) {
//        mLoginUserAddress = (String) SharePreferenceUtils.getDBParam(mContext, CommonConstant.LOGINED_USER, "");
        mLoginUserAddress = LoginUtils.getInstance().getLoginUserName();
        mAddress = bundle.getString(BUNDLE_KEY_ADDRESS);
        isEPgroup = bundle.getBoolean("isEPgroup", false);
        //群详情订阅
        if (!TextUtils.isEmpty(mAddress)) {
            String identity = GroupChatUtils.getIdentify(mContext, mAddress);
            GroupChatControl.rcsImConfSubsInfo(mAddress, identity);

            GlidePhotoLoader.getInstance(mContext).removeGroupPhotoFromCache(mAddress);
            GlidePhotoLoader.getInstance(mContext).loadGroupPhoto(mContext, new LoadPhotoCallback() {
                @Override
                public void onLoadDone(Bitmap mBitmap) {
                    LogF.d(TAG, "group bitmap reload success ! bitmap:" + mBitmap);
                }
            }, null, mAddress);
        }
//        mLoaderManager.initLoader(ID, null, this);

        GroupInfo groupInfo = GroupChatUtils.getGroupInfo(mContext, mAddress);
        String master = groupInfo.getOwner();
        LogF.d(TAG, "login=" + mLoginUserAddress + ", group master=" + master);
        int type = GroupChatUtils.getMemberType(mContext, mAddress, mLoginUserAddress);
        if (type == Type.TYPE_LEVEL_ORDER) {
            mIsChairman = true;
        } else {
            mIsChairman = false;
        }
        refreshData();

        prepareData();
        mContext.getContentResolver().registerContentObserver(Conversations.GroupInfo.CONTENT_URI, false, mObserver);
    }

    private void refreshData() {
        GroupInfo groupInfo = GroupChatUtils.getGroupInfo(mContext, mAddress);
        mView.chairmanRefresh();
        mGroupName = groupInfo.getPerson();
        mView.updateGroupName(mGroupName);
        //// TODO: 2017/6/15 是否要改成NickNameUtils
        if (mLoginUserAddress.contains("+852")) {
            mGroupCard = GroupChatUtils.getMemberNumber(mContext, mAddress, mLoginUserAddress);
        } else {
            mGroupCard = GroupChatUtils.getMemberNumber(mContext, mAddress, mLoginUserAddress.contains("+86") ? mLoginUserAddress : "+86" + mLoginUserAddress);
        }
        mView.updateGroupCard(mGroupCard);
    }

    @Override
    public void start() {
        init(mBundle);
    }

    @Override
    public String getGroupName() {
        return mGroupName;
    }

    @Override
    public String getGroupCard() {
        return mGroupCard;
    }

    @Override
    public void loadMemberList() {
        mLoaderManager.restartLoader(ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(mContext, Conversations.GroupMember.CONTENT_URI, new String[]{BaseModel.COLUMN_NAME_ID, BaseModel.COLUMN_NAME_ADDRESS, BaseModel.COLUMN_NAME_GROUP_ID,
                BaseModel.COLUMN_NAME_PERSON, BaseModel.COLUMN_NAME_STATUS, // 消息状态
                BaseModel.COLUMN_NAME_TYPE, "level"}, String.format(Conversations.WHERE_COLUMN, BaseModel.COLUMN_NAME_GROUP_ID, mAddress), null, "_id asc");
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
        if (data == null || data.isClosed()) {
            return;
        }
        new RxAsyncHelper("").runInThread(new Func1<Object, Model>() {
            @Override
            public Model call(Object o) {
                Cursor cursor = data;
                Model m = changeCursorToData(cursor);
                return m;
            }
        }).runOnMainThread(new Func1<Model, Object>() {
            @Override
            public Object call(Model model) {
                if (((GroupSettingActivity) mContext).isDestroy) {
                    return null;
                }
                if(model == null){
                    return null;
                }

                mIsChairman = model.isChairman;
                mGroupMembers.clear();
                mGroupMembers.addAll(model.groupMembers);
                mListForDelete.clear();
                mListForDelete.addAll(model.listStr);
                refreshData();
                mView.updateGroupMemberList(mGroupMembers);
                return null;
            }
        }).subscribe();


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    //将cursor转化为使用的数据类型
    private synchronized Model changeCursorToData(Cursor cursor) {
//        mGroupMembers.clear();
//        mListForDelete.clear();
        ArrayList<GroupMember> groupMembers = new ArrayList<>();
        ArrayList<String> list = new ArrayList<>();
        boolean isChairman = false;
//        String LoginUser = (String) SharePreferenceUtils.getDBParam(MyApplication.getApplication(), CommonConstant.LOGINED_USER, "");
        String LoginUser = LoginUtils.getInstance().getLoginUserName();
        if (cursor != null && !cursor.isClosed() && cursor.moveToFirst()) {
            do {
//                GroupMember value = BeanUtils.fillBean(new BeanUtils.ColumnIndex(cursor), cursor, GroupMember.class);
                GroupMember value = new GroupMember();
                value.setId(cursor.getLong(cursor.getColumnIndex(BaseModel.COLUMN_NAME_ID)));
                value.setAddress(cursor.getString(cursor.getColumnIndex(BaseModel.COLUMN_NAME_ADDRESS)));
                value.setGroupId(cursor.getString(cursor.getColumnIndex(BaseModel.COLUMN_NAME_GROUP_ID)));
                value.setPerson(cursor.getString(cursor.getColumnIndex(BaseModel.COLUMN_NAME_PERSON)));
                value.setStatus(cursor.getInt(cursor.getColumnIndex(BaseModel.COLUMN_NAME_STATUS)));
                value.setType(cursor.getInt(cursor.getColumnIndex(BaseModel.COLUMN_NAME_TYPE)));
                value.setMemberLevel(cursor.getInt(cursor.getColumnIndex("level")));
                if (!value.getAddress().contains(mLoginUserAddress)) {
                    StringBuilder sb = new StringBuilder();
                    String name = NickNameUtils.getNickNameWithoutDb(mContext, value.getAddress(), mAddress, LoginUser);
                    if (name != null) {
                        name = name.trim();
                    }
                    if (TextUtils.isEmpty(name)) {
                        name = value.getPerson();
                    }
                    if (name != null) {
                        name = name.trim();
                    }
                    if (TextUtils.isEmpty(name)) {
                        name = NumberUtils.formatPersonStart(value.getAddress());
                        value.setPerson(name);
                    }
                    sb.append(value.getAddress()).append(",").append(name).append(",").append(value.getId());
                    list.add(sb.toString());
                }
                if (value.getType() == Type.TYPE_LEVEL_ORDER) {
                    // 群主放在第一个位置
                    groupMembers.add(0, value);
                    // 标记当前用户是否是群主
                    if (value.getAddress().contains(mLoginUserAddress)) {
                        isChairman = true;
                    } else {
                        isChairman = false;
                    }
                } else {
                    groupMembers.add(value);
                }

            } while (!cursor.isClosed() && cursor.moveToNext());
        } else {
            LogF.e(TAG, "cursor is empty ..");
            // 群成员为空，群不存在，退出群设置页面
            // mView.finish();
        }
        return new Model(groupMembers, list, isChairman);
    }

    @Override
    public ArrayList<GroupMember> getGroupMembers() {
        return mGroupMembers;
    }

    @Override
    public ArrayList<String> getGroupMemberList() {
        return mListForDelete;
    }

    @Override
    public void itemClick(GroupMember groupMember) {
        if (groupMember.getAddress().equals("+")) {
            UmengUtil.buryPoint(mContext, "message_groupmessage_setup_add", "增加联系人", 0);
            openAdd();
        } else if (groupMember.getAddress().equals("-")) {
            UmengUtil.buryPoint(mContext, "groupmessage_setup_reduce", "消息-群聊-群聊设置-删除联系人", 0);
            openDelete();
        } else {
            openItem(groupMember);
        }
    }

    @Override
    public boolean isChairman() {
        return mIsChairman;
    }

    private void openAdd() {
        ArrayList<String> list = new ArrayList<String>();
        for (GroupMember member : mGroupMembers) {
            list.add(PhoneUtils.getMinMatchNumber(member.getAddress()));
        }
        int count = (isEPgroup?2000:500)-(list==null?0:list.size());
        if (count<1) {
            return;
        }
        Intent intent = ContactSelectorActivity.creatIntent(mContext, SOURCE_ADD_CONTACT_TO_GROUP, count);
        if (list != null) {
            intent.putStringArrayListExtra(ContactSelectorUtil.SELECTED_NUMBERS_KEY, list);
        }
        ((Activity) mContext).startActivityForResult(intent,
                REQUEST_CODE_ADD_MEMBER);

    }

    private void openDelete() {
        if (mListForDelete.size() > 0) {
            GroupInfo groupInfo = GroupChatUtils.getGroupInfo(mContext, mAddress);
            Intent intent = ContactProxy.g.getUiInterface().getContactSelectActivityUI()
                    .createIntentForDeleteMemberInGroup(mContext, mAddress, mListForDelete, null, true);
            intent.putExtra("my_num", groupInfo != null ? groupInfo.getOwner() : LoginUtils.getInstance().getLoginUserName());// 删除群成员的时候，列表不显示群主。
            ((Activity) mContext).startActivityForResult(intent, REQUEST_CODE_DELETE_MEMBER);
        }

    }

    private void openItem(GroupMember groupMember) {
        String address = groupMember.getAddress();
        String[] phoneAndCountryCode = NumberUtils.splitPhoneAndCountryCode(address);
        String completeAddress = address;
        address = phoneAndCountryCode[1];
        SimpleContact simpleContact = ContactsCache.getInstance().searchContactByNumber(address);
        String name = NickNameUtils.getNickName(mContext, groupMember.getAddress(), mAddress);
        String loginNum = LoginDaoImpl.getInstance().queryLoginUser(mContext);
        loginNum = PhoneUtils.getMinMatchNumber(loginNum);
        if (TextUtils.equals(address, loginNum)) {
            //跳转到个人名片
//            mContext.startActivity(new Intent(mContext, UserProfileShowActivity.class));
            AboutMeProxy.g.getUiInterface().goToUserProfileActivity(mContext);
            return;
        }
        if (simpleContact == null) {
            if (isEPgroup) {
                Employee employee = new Employee();
                employee.setAreaCode(phoneAndCountryCode[0] == null ? "" : phoneAndCountryCode[0]);
                employee.setName(name);
                employee.setAddress(address);
                employee.setRegMobile(address);
                employee.setMemberLevel(groupMember.getMemberLevel());
                employee.setMemberGroupId(mAddress);
                ContactProxy.g.getUiInterface().getContactDetailActivityUI().showForEmployee(mContext, employee);
            } else {
                GroupStrangerActivity.show(mContext, address, completeAddress, name, mAddress, mGroupName, mGroupCard);
            }

        } else {
            ContactProxy.g.getUiInterface().getContactDetailActivityUI()
                    .showForSimpleContact(mContext, simpleContact, 0);

        }

    }

    public void addMemberToGroup(Intent data, boolean isAdd) {
        if (!AndroidUtil.isNetworkConnected(mContext)) {
            Toast.makeText(mContext, mContext.getString(R.string.network_disconnect), Toast.LENGTH_SHORT).show();
            return;
        }
        if (data != null) {
            final StringBuilder sb = new StringBuilder();
            StringBuffer nameSB = new StringBuffer();
            ArrayList<BaseContact> contactList = (ArrayList<BaseContact>) data.getSerializableExtra(ContactSelectorActivity.KEY_BASECONTACT_LIST);
            for (BaseContact contact : contactList) {
                String number = contact.getNumber();
                if (TextUtils.isEmpty(number)) {
                    number = "";
                }
                sb.append(number).append(";");
                if(!TextUtils.isEmpty(contact.getName())){
                    nameSB.append(contact.getName()).append(";");
                }else {
                    nameSB.append(NumberUtils.toHideAsStar(number)).append(";");
                }
            }
            if (sb.length() >= 1) {
                sb.deleteCharAt(sb.length() - 1);
            }
            String pcUri = sb.toString().trim();
            delGroupMemberNameS = nameSB.toString().trim();
            if (isAdd) {
                GroupChatControl.rcsImSessAddPartp(mAddress, pcUri);
            } else {
                GroupChatControl.rcsImSessEplPartp(mAddress, pcUri);
            }

        }

    }

    @Override
    public String getAddress() {
        return mAddress;
    }

    @Override
    public void updateGroupName(String name) {
        mGroupName = name;
        mView.updateGroupName(mGroupName);
    }

    @Override
    public void updateGroupCard(String name) {
        mGroupCard = name;
        mView.updateGroupCard(mGroupCard);
    }

    @Override
    public boolean getUndisturbSetting(String address) {
        boolean isSlient = ConversationUtils.isSlient(mContext, address);
        mView.setUndisturbSwitch(isSlient);
        return isSlient;
    }

    @Override
    public void setUndisturbSettingLocal(String address, boolean disturb) {
        LogF.i(TAG, "-----setUndisturbSettingLocal-----address : " + address + "---disturb : " + disturb);
        ConversationUtils.setSlient(mContext, address, disturb);
        mContext.getContentResolver().notifyChange(Conversations.Conversation.CONTENT_URI, null);
    }

    @Override
    public void setUndisturbSettingServer(String status) {
        LogF.i(TAG, "-----setUndisturbSettingLocal-----status : " + status);

        //先判断联网状态
        if (!AndroidUtil.isNetworkConnected(mContext)) {
            Toast.makeText(mContext, R.string.contact_list_no_net_hint, Toast.LENGTH_SHORT).show();
            mView.updateUndisturbFinish(false);
            return;
        }
        //再判断是否已经登录
        int loginState = LoginUtils.getInstance().getLoginState();
        if (loginState != MtcCliConstants.MTC_REG_STATE_REGED) {
            Toast.makeText(mContext, R.string.login_no_logins, Toast.LENGTH_SHORT).show();
            mView.updateUndisturbFinish(false);
            return;
        }
        ManagePersonalCfg managePersonalCfg = ManagePersonalCfg.getInstance(mContext);
        //因为我们拦截了checkBox的点击操作，当前状态尚未更改（其实也不会更改了，需要我们收到服务器响应后编码更改）。
        //所以当前状态已打开的时候要发给服务器off，当前状态未打开时要发给服务器on。注意这里是反的！！！
        managePersonalCfg.updateGroupMessageDnd(getAddress(), getGroupName(),
                status, new ManagePersonalCfg.UiCallback() {
                    @Override
                    public void onResult(final boolean isOk, int statusCode) {
                        ((GroupSettingActivity) mView).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mView.updateUndisturbFinish(isOk);
                            }
                        });
                    }
                });
    }


    @Override
    public void memberExitGroup() {
        GroupChatControl.rcsImSessLeave(mAddress);
    }

    @Override
    public void chairmanExitGroup() {
        LogF.d("GroupSettingPresenter", "chairmanExitGroup 设置变量");
        Intent intent = ContactProxy.g.getUiInterface().getContactSelectActivityUI()
                .createIntentForGroupTransfer(mContext, mAddress, mListForDelete);
        intent.putExtra(BUNDLE_KEY_EXIT_GROUP, true);
        mContext.startActivity(intent);
    }

    @Override
    public void rcsImSessDissolve() {
        GroupChatControl.rcsImSessDissolve(mAddress);
    }

    @Override
    public void setExitAfterTransfer(boolean exit) {
        LogF.d(TAG, "setExitAfterTransfer exit = " + exit);
        isExitAfterTransfer = exit;
    }

    @Override
    public void setupThemeThumb() {
        int themeType = 0;
        themeType = (int) SharePreferenceUtils.getParam(mContext, MessageModuleConst.MESSAGE_THEME_TYPE + mLoginUserAddress + mAddress, 0);
        List<Drawable> drawables = MessageThemeUtils.getDrawableFromTheme(mContext, MessageThemeUtils.THEME_DRAWABLE_NUMBER, themeType * MessageThemeUtils.THEME_DRAWABLE_NUMBER);
        mView.updateThemeThumb(drawables.get(6));
    }

    @Override
    public void clearAllMsg() {
        GroupChatUtils.deleteMessageByAddress(mContext, mAddress);
//        MessageUtils.deleteConversationMsg(mContext, mAddress, true);
    }

    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange);
            prepareData();
        }
    };

    private void prepareData(){
        RxAsyncHelper helper = new RxAsyncHelper("");
        helper.runInThread(new Func1<Object, Model>() {
            @Override
            public Model call(Object o) {
                synchronized (this) {
                    String where = String.format(Conversations.WHERE_COLUMN, BaseModel.COLUMN_NAME_GROUP_ID, mAddress);
                    Cursor cursor = mContext.getContentResolver().query(Conversations.GroupMember.CONTENT_URI, new String[]{BaseModel.COLUMN_NAME_ID, BaseModel.COLUMN_NAME_ADDRESS, BaseModel.COLUMN_NAME_GROUP_ID,
                            BaseModel.COLUMN_NAME_PERSON, BaseModel.COLUMN_NAME_STATUS, // 消息状态
                            BaseModel.COLUMN_NAME_TYPE, "level"}, where, null, "_id asc");
                    Model m = changeCursorToData(cursor);
                    return m;
                }
            }
        }).runOnMainThread(new Func1<Model, Object>() {
            @Override
            public Object call(Model model) {
                if (((GroupSettingActivity) mContext).isDestroy) {
                    return null;
                }
                if(model == null){
                    return null;
                }

                mIsChairman = model.isChairman;
                mGroupMembers.clear();
                mGroupMembers.addAll(model.groupMembers);
                mListForDelete.clear();
                mListForDelete.addAll(model.listStr);
                refreshData();
                mView.updateGroupMemberList(mGroupMembers);
                return null;
            }
        }).subscribe();
    }


    private class Model {
        ArrayList<GroupMember> groupMembers;
        ArrayList<String> listStr;
        boolean isChairman;

        public Model(ArrayList<GroupMember> groupMembers, ArrayList<String> listStr, boolean isChairman) {
            this.groupMembers = groupMembers;
            this.listStr = listStr;
            this.isChairman = isChairman;
        }
    }

}
