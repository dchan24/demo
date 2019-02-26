package com.cmicc.module_message.ui.presenter;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.text.TextUtils;

import com.app.module.proxys.modulemessage.MessageProxy;
import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.utils.RxAsyncHelper;
import com.chinamobile.app.yuliao_business.aidl.SendServiceMsg;
import com.chinamobile.app.yuliao_business.logic.common.LogicActions;
import com.chinamobile.app.yuliao_business.model.BaseModel;
import com.chinamobile.app.yuliao_business.model.Conversation;
import com.chinamobile.app.yuliao_business.model.Employee;
import com.chinamobile.app.yuliao_business.model.GroupInfo;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_business.util.GroupChatUtils;
import com.chinamobile.app.yuliao_business.util.OAUtils;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.utils.FileUtil;
import com.chinamobile.app.yuliao_common.utils.Log;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_common.utils.PopWindowFor10GUtil;
import com.chinamobile.app.yuliao_common.utils.SystemFileShare;
import com.chinamobile.app.yuliao_core.db.LoginDaoImpl;
import com.chinamobile.icloud.im.sync.model.EmailKind;
import com.chinamobile.icloud.im.sync.model.OrganizationKind;
import com.chinamobile.icloud.im.sync.model.PhoneKind;
import com.chinamobile.icloud.im.sync.model.RawContact;
import com.chinamobile.icloud.im.vcard.VCardComposer;
import com.chinamobile.icloud.im.vcard.VCardConfig;
import com.chinamobile.rcs.share.manager.ShareResultUtils;
import com.chinamobile.rcs.share.parameter.StatusCode;
import com.cmcc.cmrcs.android.data.GlobalSearch;
import com.cmcc.cmrcs.android.ui.control.ComposeMessageActivityControl;
import com.cmcc.cmrcs.android.ui.control.GroupChatControl;
import com.cmcc.cmrcs.android.ui.model.GroupChatListModel;
import com.cmcc.cmrcs.android.ui.model.MediaItem;
import com.cmcc.cmrcs.android.ui.model.impls.GroupChatListModelImpl;
import com.cmcc.cmrcs.android.ui.utils.ConvCache;
import com.cmcc.cmrcs.android.ui.utils.IPCUtils;
import com.cmcc.cmrcs.android.ui.utils.ThumbnailUtils;
import com.cmcc.cmrcs.android.ui.utils.VcardContactUtils;
import com.cmcc.cmrcs.android.ui.utils.YYFileUtils;
import com.cmcc.cmrcs.android.ui.utils.message.LocationUtil;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.constract.GroupChatListContract;
import com.constvalue.MessageModuleConst;
import com.juphoon.cmcc.app.lemon.MtcImConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import rx.Subscription;
import rx.functions.Func1;

import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.MESSAGE_BODY;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.MESSAGE_BODY_SIZE;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.MESSAGE_FILE_PATH;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.MESSAGE_FILE_THUMB_PATH;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.MESSAGE_ITEM;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.MESSAGE_ITEM_VALUE;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.MESSAGE_TYPE;
import static com.cmcc.cmrcs.android.ui.activities.ContactSelectorActivity.MESSAGE_XML_CONTENT;

/**
 * Created by tianshuai on 2017/5/23.
 */

public class GroupChatListPresenter implements GroupChatListContract.IPresenter,GroupChatListModel.GroupChatListLoadFinishCallback {
    private static String TAG="GroupChatListPresenter";
    private GroupChatListContract.IView mView;
    private Context mContext;
    private LoaderManager mLoaderManager;
    private GroupChatListModel mGroupChatListModel;
    //是否同步群列表
    private static boolean bSyncGroupList = true;
    private String mToNumber;
    private RawContact mContact = null;
    String[] str = new String[4];
    private Map<String, Boolean> mapCheck = null;//名片选中状态

    //search
    private Subscription mSubscription;
    private GlobalSearch mGlobalSearch;


    public GroupChatListPresenter(Context context, GroupChatListContract.IView view){
        mContext=context;
        mView=view;
        mGroupChatListModel=new GroupChatListModelImpl();
        mGlobalSearch = new GlobalSearch();
    }

    public GroupChatListPresenter(Context context, GroupChatListContract.IView view, int type){
        mContext=context;
        mView=view;
        mGroupChatListModel=new GroupChatListModelImpl(type);
        mGlobalSearch = new GlobalSearch();
    }

    /**
     * 从数据库加载群数据和从平台拉取数据
     */
    @Override
    public void start() {
        mLoaderManager=mView.getLoaderManger();
        //加载群数据
        loadGroups();
        //GroupChatControl.syncGroupChat();

        //第一次进app拉取群数据
        if (  bSyncGroupList ) {
            bSyncGroupList = false;
            GroupChatControl.syncGroupChat();//拉取群列表
            // return;
        }

    }

    /**
     * 从数据库加载群数据
     */
    public void loadGroups(){
        mGroupChatListModel.loadGroupChatList(mContext,mLoaderManager,this);
    }

    /**
     * 数据加载完的回调
     * @param cursor
     */
    @Override
    public void onLoadFinished(Cursor cursor) {
        mView.updateListView(cursor);
    }

    /**
     *
     * @param v
     */
    @Override
    public void setView(GroupChatListContract.IView v) {
        mView=v;
    }

    /**
    * 处理消息转发
    */
    @Override
    public void handleMessageForward(Bundle bundle, GroupInfo groupInfo) {
        if (bundle == null) {
            return;
        }
        String address = groupInfo.getAddress();
        Message msg = null;
        ArrayList<MediaItem> items = null;
        // 是否发送本地图片
        boolean isMediaItem = bundle.getBoolean(MESSAGE_ITEM);
        if (isMediaItem) {
            // 本地图片发送列表信息
            items = (ArrayList<MediaItem>) bundle.getSerializable(MESSAGE_ITEM_VALUE);
            sendLocalPicture(address, items);
        } else {
            // 转发已有消息
            int type = bundle.getInt(MESSAGE_TYPE);
            msg = new Message();
            msg.setType(type);
            switch (type) {
                case Type.TYPE_MSG_TEXT_SEND:
                    String message = bundle.getString(MESSAGE_BODY);
                    String textSize = bundle.getString(MESSAGE_BODY_SIZE);
                    /*if (TextUtils.isEmpty(textSize)) {
                        textSize = msg.getTextSize();
                    }*/
                    if (!TextUtils.isEmpty(message)) {
                        GroupChatControl.rcsImSessMsgSend(address ,message, textSize);
                    }
                    break;
                case Type.TYPE_MSG_IMG_SEND:
                    // 已存在消息的转发
                    msg.setExtFilePath(bundle.getString(MESSAGE_FILE_PATH));
                    msg.setExtThumbPath(bundle.getString(MESSAGE_FILE_THUMB_PATH));
                    rcsImFileTrsfCThumb(BaseModel.DEFAULT_VALUE_INTEGER, LogicActions.GROUP_CHAT_FILE_TRANSFER_THUMB_BY_PHONTO, address, "", msg.getExtFilePath(), msg.getExtThumbPath(), FileUtil.getDuring(msg.getExtFilePath()));
                    break;
                case Type.TYPE_MSG_FILE_SEND:
                    // 已存在消息的转发
                    msg.setExtFilePath(bundle.getString(MESSAGE_FILE_PATH));
                    msg.setExtThumbPath(bundle.getString(MESSAGE_FILE_THUMB_PATH));
                    ComposeMessageActivityControl.rcsImFileTrsfXByFileSystem(address, msg.getExtFilePath(), FileUtil.getDuring(msg.getExtFilePath()));
                    break;
                case Type.TYPE_MSG_CARD_SEND:
                    //转发名片
                    msg.setExtFilePath(bundle.getString(MESSAGE_FILE_PATH));
                    msg.setBody(bundle.getString(MESSAGE_BODY));
                    if(!TextUtils.isEmpty(msg.getBody())) {
                        if(!TextUtils.isEmpty(msg.getExtFilePath())) {
                            rcsImFileTrsfCThumb(BaseModel.DEFAULT_VALUE_INTEGER, LogicActions.GROUP_CHAT_FILE_TRANSFTER, address, msg.getBody(), msg.getExtFilePath(), "", FileUtil.getDuring(msg.getExtFilePath()));
                        }else{
                            rcsImFileTrsfCThumb(BaseModel.DEFAULT_VALUE_INTEGER, LogicActions.GROUP_CHAT_FILE_TRANSFTER, address, msg.getBody(), VcardContactUtils.getFilePath(mContext,msg.getBody()), "", FileUtil.getDuring(VcardContactUtils.getFilePath(mContext,msg.getBody())));

                        }
                    }
                    break;
                case Type.TYPE_MSG_OA_ONE_CARD_SEND: {
                    String xmlContent = OAUtils.replaceDisplayMode(bundle.getString(MESSAGE_XML_CONTENT));

                    GroupChatControl.rcsImSessMsgSendOA(BaseModel.DEFAULT_VALUE_INTEGER, address, xmlContent);
                    break;
                }
                case Type.TYPE_MSG_DATE_ACTIVITY_SEND: {
                    String xmlContent = OAUtils.replaceDisplayMode(bundle.getString(MESSAGE_XML_CONTENT));

                    GroupChatControl.rcsImSessMsgSendDateActivity(BaseModel.DEFAULT_VALUE_INTEGER, address, xmlContent);
                }
                case Type.TYPE_MSG_T_CARD_SEND: {
                    String xmlContent = OAUtils.replaceDisplayMode(bundle.getString(MESSAGE_XML_CONTENT));
                    GroupChatControl.rcsImSessMsgSendD(address, xmlContent);
                }
                    break;
                case Type.TYPE_MSG_LOC_RECV:
                case Type.TYPE_MSG_LOC_SEND:
                msg.setBody(bundle.getString(MESSAGE_BODY));
                String loc_body = LocationUtil.parseFreeText(msg.getBody());
                String loc_title = LocationUtil.parseTitle(msg.getBody());
                double longitude = Double.valueOf(LocationUtil.parseLongitude(msg.getBody()));
                double latitude = Double.valueOf(LocationUtil.parseLatitude(msg.getBody()));
//                if (!isGroup) {
//                    ComposeMessageActivityControl.sendLocation(msg.getPerson(), BaseModel.DEFAULT_VALUE_INTEGER, msg.getAddress(),latitude,longitude,1000,loc_title,loc_body);
//                } else {
                    GroupChatUtils.sendLocation(mContext,address,BaseModel.DEFAULT_VALUE_INTEGER,latitude,longitude,1000,loc_title,loc_body);
                    break;
                //
                default:
                    break;
            }


            if (bundle.getStringArrayList(SystemFileShare.SYSTEM_FILE_PATHS) != null) {

                ArrayList<String> paths = bundle.getStringArrayList(SystemFileShare.SYSTEM_FILE_PATHS);
                for (String extFilePath : paths) {
                    android.util.Log.d(TAG, "handleMessageForward: extFilePath = " + extFilePath);
                    Message msg2 = new Message();
                    msg2.setType(type);
                    msg2.setAddress(address);

                    File file = new File(extFilePath);
                    if (!file.exists()) {
                        return;
                    }

                    msg2.setExtFilePath(extFilePath);
                    if (YYFileUtils.isPhoto(extFilePath)) {
                        msg2.setExtThumbPath(ThumbnailUtils.createThumb(extFilePath,false));
                        msg2.setType(Type.TYPE_MSG_IMG_SEND);
                        rcsImFileTrsfCThumb(BaseModel.DEFAULT_VALUE_INTEGER, LogicActions.GROUP_CHAT_FILE_TRANSFER_THUMB_BY_PHONTO, address, "", msg2.getExtFilePath(), msg2.getExtThumbPath(), 0);
                    } else if(FileUtil.isVideo(extFilePath)) {
                        msg2.setExtThumbPath(ThumbnailUtils.createThumb(extFilePath,true));
                        rcsImFileTrsfCThumb(BaseModel.DEFAULT_VALUE_INTEGER, LogicActions.GROUP_CHAT_FILE_TRANSFER_THUMB_BY_PHONTO, address, "", extFilePath, msg2.getExtThumbPath(), FileUtil.getDuring(extFilePath));
                    } else {
                        msg2.setExtThumbPath(extFilePath);
                        msg2.setType(Type.TYPE_MSG_FILE_SEND);
                        ComposeMessageActivityControl.rcsImFileTrsfXByFileSystem(address, msg2.getExtFilePath(), FileUtil.getDuring(msg2.getExtFilePath()));
                    }
                }

                if (PopWindowFor10GUtil.isNeedTip()) {
                    PopWindowFor10GUtil.showToast(mContext);
                } else {
                    BaseToast.show(mContext, mContext.getString(R.string.toast_msg_sys_forwarded));
                }
                return;
            }


            if (bundle.getString(SystemFileShare.SHARE_TYPE,"").equals(SystemFileShare.SYSTEM_SHARE)) {
                ShareResultUtils.sendShareResult(mContext, StatusCode.SHARE_SUCCESS);
                BaseToast.show(mContext, mContext.getString(R.string.toast_msg_sys_forwarded));
                return;
            }


        }
        BaseToast.show(mContext, mContext.getResources().getString(R.string.toast_msg_has_forwarded));
    }

    /**
     * 分享此名片到群聊
     * @param toNumber
     * @param rawContact
     */
    @Override
    public void handleCardToChat(String toNumber, RawContact rawContact) {
        mToNumber = toNumber;
        if (rawContact == null) {
            return;
        }

        for (int i = 0; i < str.length; i++) {
            str[i] = "";
        }
        mContact = rawContact;
        if (rawContact.getPhones() != null) {
            for (PhoneKind phone : rawContact.getPhones()) {
                if (!TextUtils.isEmpty(phone.getNumber())) {
                    //mList.add(phone);
                    str[0] = phone.getNumber();
                    break;
                }
            }

        }
        if (TextUtils.isEmpty(str[0])) {
            str[0] = LoginDaoImpl.getInstance().queryLoginUser(mContext);
        }
        PhoneKind phoneKind = new PhoneKind();
        phoneKind.setNumber(str[0]);
        List<PhoneKind> phoneKinds = new ArrayList<>();
        phoneKinds.add(phoneKind);
        mContact.setPhones(phoneKinds);

        if (rawContact.getOrganizations() != null) {
            if (rawContact.getOrganizations().size() > 0) {
                OrganizationKind organizationKind = rawContact.getOrganizations().get(0);
                if (organizationKind != null && !TextUtils.isEmpty(organizationKind.getCompany()) && !TextUtils.isEmpty(organizationKind.getCompany().trim())) {
                    str[1] = organizationKind.getCompany().trim();
                }
                if (organizationKind != null && !TextUtils.isEmpty(organizationKind.getTitle()) && !TextUtils.isEmpty(organizationKind.getTitle().trim())) {
                    str[2] = organizationKind.getTitle().trim();
                }
            }
        }
        if (rawContact.getEmails() != null && rawContact.getEmails().size() > 0) {
            EmailKind eKind = rawContact.getEmails().get(0);
            if (eKind != null && !TextUtils.isEmpty(eKind.getValue()) && !TextUtils.isEmpty(eKind.getValue().trim())) {
                str[3] = eKind.getValue().trim();
            }
        }
        mView.showVcardExportDialog(str, rawContact.getStructuredName().getDisplayName());
    }

    /**
     * 发送名片
     */
    @Override
    public void submitVcard() {
        new RxAsyncHelper("").debound(200).runInThread(new Func1() {
            @Override
            public Object call(Object o) {
                String vcardString = createdVcardString();

                if (TextUtils.isEmpty(vcardString) || TextUtils.isEmpty(mToNumber)) {
                    //接收人号码或者名片内容为空则返回
                    return false;
                }
                Message msg = new Message();
                msg.setType(Type.TYPE_MSG_CARD_SEND);
                msg.setBody(vcardString);
                msg.setAddress(mToNumber);
                String fileName = String.valueOf(System.currentTimeMillis())+"uuid";
                File fvcf = new File(FileUtil.getCardClip(mContext).getPath() + File.separatorChar + fileName + ".vcf");
                try(FileOutputStream fos = new FileOutputStream(fvcf)) {
                    if (!fvcf.exists()) {
                        fvcf.createNewFile();
                    }
                    fos.write(vcardString.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String filepath = fvcf.getAbsolutePath();
                long duration = FileUtil.getDuring(filepath);
                rcsImFileTrsfCThumb(BaseModel.DEFAULT_VALUE_INTEGER, LogicActions.GROUP_CHAT_FILE_TRANSFTER, msg.getAddress(), msg.getBody(), filepath, "", duration);
               return true;
            }
        }).runOnMainThread(new Func1() {
            @Override
            public Object call(Object o) {
                if(o != null && (boolean)o){
                    BaseToast.show(mContext,mContext.getString(R.string.has_been_sent));
                }else{
                    BaseToast.show(mContext,mContext.getString(R.string.send_fail));
                }
                ((Activity) mContext).setResult(-1, null);
                ((Activity) mContext).finish();
                return null;
            }
        }).subscribe();

    }

    /**
     *
     * @param mapCheck
     */
    @Override
    public void setChecks(Map<String, Boolean> mapCheck) {
        this.mapCheck = mapCheck;
    }

    /**
     *
     * @param number
     * @param employee
     * @param isGroup
     */
    @Override
    public void submitVCardFromContactDetail(String number, Employee employee, boolean isGroup) {
        String name = employee.name;
        String[] mInfo = new String[]{"", "",""};
        String phone = employee.regMobile;
        if(!TextUtils.isEmpty(employee.departments)){
            mInfo[0]=employee.departments;
        }else{
            if(!TextUtils.isEmpty(employee.enterpriseName)){
                mInfo[0]=employee.enterpriseName;
            }else{
                LogF.d(TAG, " initData() employeeEnterpriseName is null");
            }
        }
        if(!TextUtils.isEmpty(employee.positions)){
            mInfo[1]=employee.positions.replaceAll(",", " ");
        }
        if(employee.email != null && employee.email.size() > 0) {
            String email = employee.email.get(0);
            mInfo[2] = email;
        }
        RawContact rawContact = new RawContact();
        rawContact.getStructuredName().setGivenName(name);
        rawContact.getStructuredName().setDisplayName(name);
        List<PhoneKind> phoneKinds = new ArrayList<>();
        PhoneKind phoneKind=new PhoneKind();
        phoneKind.setNumber(phone);
        phoneKind.setValue(phone);
        phoneKinds.add(phoneKind);
        rawContact.setPhones(phoneKinds);
        List<EmailKind> emails=new ArrayList<>();
        EmailKind emailKind=new EmailKind();
        emailKind.setValue(mInfo[2]);
        emails.add(emailKind);
        rawContact.setEmails(emails);
        List<OrganizationKind> organizations=new ArrayList<>();
        OrganizationKind organizationKind=new OrganizationKind();
        organizationKind.setCompany(mInfo[0]);
        organizationKind.setTitle(mInfo[1]);
        organizations.add(organizationKind);
        rawContact.setOrganizations(organizations);
        mContact = rawContact;
        mToNumber = number;
        submitVcard();
    }

    @Override
    public void search(final CharSequence key) {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
        mSubscription = new RxAsyncHelper<>("").runInSingleFixThread(new Func1<Object, ArrayList<GroupInfo>>() {
            @Override
            public ArrayList<GroupInfo> call(Object s) {
                return mGlobalSearch.searchGroupChat(mContext, key);
            }
        }).runOnMainThread(new Func1<ArrayList<GroupInfo>, Object>() {
            @Override
            public Object call(ArrayList<GroupInfo> searchResult) {
                mView.showSearchResult(searchResult, key);
                return null;
            }
        }).subscribe();

    }

    private void sendLocalPicture(final String address, List<MediaItem> list) {
        if (AndroidUtil.isSdcardAvailable() && list != null) {
            new RxAsyncHelper<>(list).runInThread(new Func1<MediaItem, MediaItem>() {
                @Override
                public MediaItem call(MediaItem item) {
                    if(item == null || TextUtils.isEmpty(item.getLocalPath())){
                        return null;
                    }
                    item.setMicroThumbPath(ThumbnailUtils.createThumb(item.getLocalPath(),false));
                    return item;
                }
            }).runOnMainThread(new Func1<MediaItem, Object>() {
                @Override
                public Object call(MediaItem item) {
                    if (item == null) {
                        return null;
                    }
                    rcsImFileTrsfCThumb(BaseModel.DEFAULT_VALUE_INTEGER, LogicActions.GROUP_CHAT_FILE_TRANSFER_THUMB_BY_PHONTO, address, "", item.getLocalPath(), item.getMicroThumbPath(), 0);
                    return null;
                }
            }).subscribe();
        }
    }

    // 文件传输带缩略图
    private void rcsImFileTrsfCThumb(int userId, int action, String groupid, String pcSubject, String pcFileName, String thumbPath, long duration) {
        SendServiceMsg msg = new SendServiceMsg();
        msg.action = action;
        msg.bundle.putInt(LogicActions.USER_ID, userId);
        msg.bundle.putString(LogicActions.GROUP_CHAT_ID, groupid);
        msg.bundle.putString(LogicActions.FILE_TRANSFER_SUBJECT, pcSubject);
        msg.bundle.putString(LogicActions.FILE_NAME, pcFileName);
        msg.bundle.putString(LogicActions.FILE_THUMB_PATH, thumbPath);
        msg.bundle.putLong(LogicActions.FILE_RECORD_DURATION, duration);
        IPCUtils.getInstance().send(msg);
    }

    @Override
    public void openItem(Context context, GroupInfo groupInfo) {
        Log.e("time debug", "time open ---" + java.lang.System.currentTimeMillis());
        String clzName = null;
        if(groupInfo == null ){
            return;
        }
        Bundle bundle = new Bundle();

        Conversation conversation = ConvCache.getInstance().getConvByAddress(groupInfo.getAddress());

        if (conversation != null) {
            clzName = MessageProxy.g.getServiceInterface().getClassName(MessageModuleConst.ClassType.GROUP_CHAT_MESSAGE_FRAGMENT_CLASS);
            bundle.putString("address", conversation.getAddress());
            bundle.putString("person", conversation.getPerson());
            boolean isSlient = conversation.getSlientDate() > 0;
            bundle.putBoolean("slient", isSlient);
            bundle.putLong("loadtime", 0);
            bundle.putString("clzName", clzName);
            if (conversation.getType() == Type.TYPE_MSG_TEXT_DRAFT) {
                bundle.putString("draft", conversation.getBody());
            }
            bundle.putInt("unread", conversation.getUnReadCount());
            if (conversation.getNotifyDate() > 0) {
                bundle.putBoolean("has_at_msg", true);
            }
            if (conversation.getGroupType() == MtcImConstants.EN_MTC_GROUP_TYPE_ENTERPRISE) {
                bundle.putBoolean("isEPgroup", true);
            } else if(conversation.getGroupType() == MtcImConstants.EN_MTC_GROUP_TYPE_PARTY){
                bundle.putBoolean("isPartyGroup" , true);
            }
            //群类型,之后都用这种吧,不要传上面那个了
            bundle.putInt("grouptype",conversation.getGroupType());
//            MessageProxy.g.getUiInterface().goMessageDetailActivity(context,bundle);;
            MessageProxy.g.getUiInterface().goMessageDetailActivity(context,bundle);
        } else {
            clzName = "GroupChatFragment";
            bundle.putString("address", groupInfo.getAddress());
            bundle.putString("person", groupInfo.getPerson());
            bundle.putLong("loadtime", 0);
            bundle.putString("clzName", clzName);
            if (groupInfo.getType() == MtcImConstants.EN_MTC_GROUP_TYPE_ENTERPRISE) {//企业群
                bundle.putBoolean("isEPgroup", true);
            }else if(groupInfo.getType() == MtcImConstants.EN_MTC_GROUP_TYPE_PARTY){
                bundle.putBoolean("isPartyGroup" , true);
            }

            //群类型,之后都用这种吧,不要传上面那个了
            bundle.putInt("grouptype",groupInfo.getType());

//            MessageProxy.g.getUiInterface().goMessageDetailActivity(context,bundle);;
            MessageProxy.g.getUiInterface().goMessageDetailActivity(context,bundle);
        }
        //updateUnreadCount(mContext, conversation);
    }

    @Override
    public void setLoaderManager(LoaderManager loaderManager) {
        mLoaderManager=loaderManager;
    }

    private String createdVcardString() {
        VCardComposer mComposer;
        final RawContact rawContact = new RawContact();
        //姓名默认选择直接发送
        rawContact.getStructuredName().setDisplayName(mContact.getStructuredName().getDisplayName());
        rawContact.getStructuredName().setFamilyName(mContact.getStructuredName().getFamilyName());
        rawContact.getStructuredName().setMiddleName(mContact.getStructuredName().getMiddleName());
        rawContact.getStructuredName().setGivenName(mContact.getStructuredName().getGivenName());
        rawContact.getStructuredName().setPrefix(mContact.getStructuredName().getPrefix());
        rawContact.getStructuredName().setSuffix(mContact.getStructuredName().getSuffix());
//        List<PhoneKind> phoneKinds = new ArrayList<PhoneKind>();
//        List<EmailKind> emailKinds = new ArrayList<EmailKind>();
        List<OrganizationKind> organizationKinds = new ArrayList<OrganizationKind>();

        //手机
        if (mapCheck.get(VcardContactUtils.card_fields[0]) != null && mapCheck.get(VcardContactUtils.card_fields[0])) {
            rawContact.setPhones(mContact.getPhones());
        }

        if (mContact.getOrganizations() != null && mContact.getOrganizations().size() > 0) {
            OrganizationKind organizationKind = new OrganizationKind();
            //公司
            if (mapCheck.get(VcardContactUtils.card_fields[1]) != null && mapCheck.get(VcardContactUtils.card_fields[1])) {
                organizationKind.setCompany(mContact.getOrganizations().get(0).getCompany());
            }
            if (mapCheck.get(VcardContactUtils.card_fields[2]) != null && mapCheck.get(VcardContactUtils.card_fields[2])) {
                organizationKind.setTitle(mContact.getOrganizations().get(0).getTitle());
            }
            organizationKinds.add(organizationKind);
        }

        rawContact.setOrganizations(organizationKinds);
        //email
        if (mapCheck.get(VcardContactUtils.card_fields[3]) != null && mapCheck.get(VcardContactUtils.card_fields[3])) {
            rawContact.setEmails(mContact.getEmails());
        }

        mComposer = new VCardComposer(mContext, VCardConfig.getVCardTypeFromString("v30_japanese_utf8"), true);
        String vcardString = mComposer.createOneEntryInternalContact(rawContact, null);
        LogF.d(TAG, "分享的vcard字符串=" + vcardString);

        return vcardString;


    }
}
