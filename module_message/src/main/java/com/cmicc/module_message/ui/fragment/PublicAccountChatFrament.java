package com.cmicc.module_message.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.module.WebConfig;
import com.app.module.proxys.moduleenterprise.EnterPriseProxy;
import com.app.module.proxys.moduleimgeditor.ImgEditorProxy;
import com.chinamobile.app.yuliao_business.model.BaseModel;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_business.model.PlatformMenu;
import com.chinamobile.app.yuliao_business.util.MessageUtils;
import com.chinamobile.app.yuliao_common.utils.Log;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.cmcc.cmrcs.android.ui.control.ComposeMessageActivityControl;
import com.cmcc.cmrcs.android.ui.interfaces.SendAudioMessageCallBack;
import com.cmcc.cmrcs.android.ui.model.MediaItem;
import com.cmicc.module_message.ui.model.impls.MessageEditorModelImpl;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.constract.PublicAccountChatContract;
import com.cmicc.module_message.ui.presenter.PublicAccountChatPresenter;
import com.constvalue.MessageModuleConst;
import com.rcs.rcspublicaccount.PublicAccountDetailActivity;
import com.rcs.rcspublicaccount.util.PublicAccountUtil;
import com.rcspublicaccount.api.bean.Menu;
import com.rcspublicaccount.api.bean.MenuList;

import java.util.ArrayList;
import java.util.List;

/**
 * @anthor situ
 * @time 2017/7/7 9:55
 * @description 公众号会话界面
 */

public class PublicAccountChatFrament extends BaseChatFragment implements View.OnClickListener ,SendAudioMessageCallBack {

    private static final String TAG = "PublicAccountChatFrament";

    private PublicAccountChatContract.Presenter mPresenter;

    protected View mRootView;
    protected ImageView mPublicPlatMenuBtn;
    private View mPlatMenus;
    protected ViewStub mPublicPlatToolsView;
    private TextView public_menu_name1;
    private TextView public_menu_name2;
    private TextView public_menu_name3;
    private RelativeLayout public_menu1;
    private RelativeLayout public_menu2;
    private RelativeLayout public_menu3;
    private View public_menu1_line2;
    private View public_menu1_line3;
    private boolean mPlatformToolsShown;
    private ArrayList<PlatformMenu> platformMenu;
    private ArrayList<PlatformMenu> mLocalItemMenus;
    private MenuList mMenuList;
    private List<Menu> mMenuLists;
    private List<Menu> mItemMenus;
    private PopupWindow window;
    int[] xy = new int[2];
    private boolean isPreConversation;


    public static PublicAccountChatFrament newInstance(Bundle bundle) {
        PublicAccountChatFrament fragment = new PublicAccountChatFrament();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        this.mRecorderRedDot.setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler = null;
    }

    private static final int REQUEST_CODE = 200;


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE) {
                if (data != null && data.getBooleanExtra("clear_all_msg", false)) {
//                    mRLMsgCountTip_up.setVisibility(View.GONE);
//                    mBtMsgATTip.setVisibility(View.GONE);
                    mPresenter.clearAllMsg();
                    if(mMessageChatListAdapter != null) {
                        if (mMessageChatListAdapter.getDataList() != null){
                            mMessageChatListAdapter.getDataList().clear();
                        }
                    }
                    onNormalLoadDone(null, false);
                } else if (data != null && !data.getBooleanExtra("isAttention", true)) {
                    //如果取消关注公众号，把结果继续传到上一级activity，用于刷新列表，并关闭当前activity
                    Intent intent = new Intent();
                    intent.putExtra("isAttention", false);
                    if (getActivity() != null) {
                        getActivity().setResult(Activity.RESULT_OK, intent);
                        getActivity().finish();
                    }
                }
            }
        }

        //从MessageDetailActivity -> PreviewImageActivity 路径
        if(requestCode == PREVIEW_IMAGE_REQUEST &&resultCode == ImgEditorProxy.g.getServiceInterface().getFinalRequestEditPictureStatus()){
            int imgEditorStatus = data.getIntExtra(ImgEditorProxy.g.getServiceInterface().getFinalImageStatus(), -1);
            if (imgEditorStatus != -1) {
                String imageSavePath = data.getStringExtra(ImgEditorProxy.g.getServiceInterface().getFinalExtraImageSavePath());
                mPresenter.sendEditImage(imageSavePath);
            }
        }

    }

    @Override
    public void updateChatListView(int loadType, int searchPos, Bundle bundle) {
        Log.e("time debug", "time update ---" + System.currentTimeMillis());

        boolean fromMoreMsg = loadType == MessageEditorModelImpl.LOAD_TYPE_MORE;
        boolean hasMore = false;
        int addNum = 0;
        ArrayList<? extends BaseModel> list = null;
        if (bundle != null) {
            hasMore = bundle.getBoolean("extra_has_more", false);
            addNum = bundle.getInt("extra_add_num", 0);
            list = (ArrayList<? extends BaseModel>) bundle.getSerializable("extra_result_data");
        }

        if (fromMoreMsg) {
            onLoadMoreDone(list, addNum);
        } else {
            if (loadType == MessageEditorModelImpl.LOAD_TYPE_FIRST) {
                onFirstLoadDone(searchPos, list, hasMore);
            } else {
                onNormalLoadDone(list, loadType == MessageEditorModelImpl.LOAD_TYPE_ADD);
            }
        }
    }

    @Override
    public void onFirstLoadDone(int searchPos, ArrayList<? extends BaseModel> list, boolean hasMore) {
        super.onFirstLoadDone(searchPos, list, hasMore);
        if (getView() != null) {
            if (list.size() == 0) {
                getView().findViewById(R.id.no_message_hint).setVisibility(View.VISIBLE);
            } else {
                getView().findViewById(R.id.no_message_hint).setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void showTitleName(CharSequence person) {
        TextView tvTitle= (TextView)((AppCompatActivity) getActivity()).findViewById(R.id.title);
        tvTitle.setText(person);
    }

    @Override
    public void reSend(Message msg) {
        mPresenter.reSend(msg);
    }

    @Override
    public void sendWithdrawnMessage(Message msg) {

    }

    @Override
    public void deleteMessage(Message msg) {
        mPresenter.deleteMessage(msg);
    }

    @Override
    public void deleteMultiMessage(SparseBooleanArray selectList) {

    }

    @Override
    public void forwardMultiMessage(SparseBooleanArray selectList) {

    }

    @Override
    public void addToFavorite(Message msg, int chatType, String address) {
        mPresenter.addToFavorite(msg, chatType, address);
    }

    @Override
    public void sysMessage(int type) {

    }

    @Override
    public void showToast(String toast) {

    }


    @Override
    protected void loadMoreMessages() {
        mPresenter.loadMoreMessages();
    }

    @Override
    public void sendVcard(String pcUri, String pcSubject, String pcFileName, long duration){
        mPresenter.sendVcard( pcUri,  pcSubject,  pcFileName,  duration);
    }
    @Override
    protected void sendImgAndVideo(ArrayList<MediaItem> items) {
        mPresenter.sendImgAndVideo(items, false);
    }

    @Override
    protected void sendImgAndVideo(ArrayList<MediaItem> items, boolean isOriginPhoto) {
        mPresenter.sendImgAndVideo(items, isOriginPhoto);
    }

    @Override
    protected void sendLocation(double dLatitude, double dLongitude, float fRadius, String pcLabel,String detailAddress) {

    }

    @Override
    protected void sendFileMsg(Intent data) {

    }

    @Override
    protected void sendMessage() {
        if(TextUtils.isEmpty(mEtMessage.getText().toString().trim())){
            return;
        }
        mPresenter.sendMessage(mEtMessage.getText().toString());
    }

    @Override
    public void senAudioMessage(String path, long lon) {
        mPresenter.sendAudioMessage(path, lon);
    }

    @Override
    public void senAudioMessage(String path, long lon, String detail) {
        mPresenter.sendAudioMessage(path, lon, detail);
    }

    @Override
    protected void initPresenter(Bundle bundle) {
        mPresenter = new PublicAccountChatPresenter(this.getActivity(), this, getLoaderManager(), bundle, handler);
    }

    @Override
    protected Message getDraftMessage() {
        return null;
    }

    @Override
    protected void saveDraftMessage(boolean save, Message Msg) {

    }

    @Override
    public int getChatType() {
        return MessageModuleConst.MessageChatListAdapter.TYPE_PUBLICACCOUNT_CHAT;
    }

    @Override
    protected void clearUnreadCount() {
        mPresenter.updateUnreadCount();
    }

    @Override
    protected void changeMenu(int themeOption) {

    }

    @Override
    public void moveToEnd() {
        mLinearLayoutManager.scrollToPositionWithOffset(mLinearLayoutManager.getItemCount() - 1, -40000);
        forceStopRecyclerViewScroll();
    }

    @Override
    public void initData() {
        super.initData();
        mPresenter.init();
        isPreConversation = getArguments().getBoolean("preConversation", false);
        setupViews();

        // 编辑框屏蔽加号与麦克风
//        mIbMore.setEnabled(false);
        mIbAudio.setVisibility(View.GONE);
        mIbSend.setVisibility(View.VISIBLE);

        //屏蔽多功能面板
        mRichPanel.setVisibility(View.GONE);

        // 设置标题名字
        String publicName = getArguments().getString("name");
        showTitleName(publicName);

        mMessageChatListAdapter.setPublicAccountTitle(publicName);
        mPresenter.start();
    }

    private void setupViews() {
        mRootView = getView();
        mPublicPlatMenuBtn = (ImageView) mRootView.findViewById(R.id.conversation_bottom_showCustomMenuView);
        mPublicPlatMenuBtn.setVisibility(View.GONE);
        mPublicPlatToolsView = (ViewStub) mRootView.findViewById(R.id.publicpaltform_tools_view);
        mRecyclerView.setBackgroundColor(getResources().getColor(R.color.color_ebebeb));
        initViewMenu();
        mPublicPlatMenuBtn.setOnClickListener(this);
        showPlatformMenu();
        mRootView.findViewById(R.id.ib_more).setVisibility(View.GONE);
        mRootView.findViewById(R.id.ib_file).setVisibility(View.VISIBLE);

        if (isPreConversation) {
            View inputAndMenu = mRootView.findViewById(R.id.input_and_menu);
            inputAndMenu.setVisibility(View.GONE);
        } else {
            View inputAndMenu = mRootView.findViewById(R.id.input_and_menu);
            inputAndMenu.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 高级菜单 init
     */
    private void initViewMenu() {
        mPlatMenus = mPublicPlatToolsView.inflate();
        mPublicPlatToolsView.setVisibility(View.GONE);
        public_menu_name1 = (TextView) mRootView.findViewById(R.id.public_menu_name1);
        public_menu_name2 = (TextView) mRootView.findViewById(R.id.public_menu_name2);
        public_menu_name3 = (TextView) mRootView.findViewById(R.id.public_menu_name3);

        public_menu1_line2 = (View) mRootView.findViewById(R.id.public_menu1_line2);
        public_menu1_line3 = (View) mRootView.findViewById(R.id.public_menu1_line3);

        public_menu1 = (RelativeLayout) mRootView.findViewById(R.id.public_menu1);
        public_menu2 = (RelativeLayout) mRootView.findViewById(R.id.public_menu2);
        public_menu3 = (RelativeLayout) mRootView.findViewById(R.id.public_menu3);
        public_menu1_line2.setVisibility(View.GONE);
        public_menu1_line3.setVisibility(View.GONE);
        public_menu1.setVisibility(View.GONE);
        public_menu2.setVisibility(View.GONE);
        public_menu3.setVisibility(View.GONE);
        public_menu1.setTag(R.id.public_menu1, false);
        public_menu2.setTag(R.id.public_menu1, false);
        public_menu3.setTag(R.id.public_menu1, false);
    }

    /**
     * 显示高级菜单数据
     */
    protected void showPlatformMenu() {
        String address = getArguments().getString("address");
        platformMenu = MessageUtils.getPlatformMenuById(getContext(), address);
        if (platformMenu != null && platformMenu.size() != 0) {
            if (platformMenu.size() > 0 && platformMenu.get(0) != null) {
                public_menu_name1.setText(platformMenu.get(0).getMenu_title());
                public_menu1.setVisibility(View.VISIBLE);
                public_menu1.setOnClickListener(this);
                if (platformMenu.get(0).getMenu_type() != 0 && platformMenu.get(0).getMenu_type() != 1 && platformMenu.get(0).getMenu_type() != 2 && platformMenu.get(0).getMenu_type() != 3) {
                    Drawable drawable= getResources().getDrawable(R.drawable.tabber_icon_more);
                    /// 这一步必须要做,否则不会显示.
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                    public_menu_name1.setCompoundDrawables(drawable, null, null, null);
                }
                public_menu1_line2.setVisibility(View.VISIBLE);

            }
            if (platformMenu.size() > 1 && platformMenu.get(1) != null) {
                public_menu_name2.setText(platformMenu.get(1).getMenu_title());
                public_menu2.setVisibility(View.VISIBLE);
                public_menu2.setOnClickListener(this);
                public_menu1_line3.setVisibility(View.VISIBLE);
                if (platformMenu.get(1).getMenu_type() != 0 && platformMenu.get(1).getMenu_type() != 1 && platformMenu.get(1).getMenu_type() != 2 && platformMenu.get(1).getMenu_type() != 3) {
                    Drawable drawable= getResources().getDrawable(R.drawable.tabber_icon_more);
                    /// 这一步必须要做,否则不会显示.
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                    public_menu_name2.setCompoundDrawables(drawable, null, null, null);
                }
            }
            if (platformMenu.size() > 2 && platformMenu.get(2) != null) {
                public_menu3.setVisibility(View.VISIBLE);
                public_menu3.setOnClickListener(this);
                public_menu_name3.setText(platformMenu.get(2).getMenu_title());
                public_menu1_line3.setVisibility(View.VISIBLE);
                if (platformMenu.get(2).getMenu_type() != 0 && platformMenu.get(2).getMenu_type() != 1 && platformMenu.get(2).getMenu_type() != 2 && platformMenu.get(2).getMenu_type() != 3) {
                    Drawable drawable= getResources().getDrawable(R.drawable.tabber_icon_more);
                    /// 这一步必须要做,否则不会显示.
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                    public_menu_name3.setCompoundDrawables(drawable, null, null, null);
                }
            }
            showPublicPlatMenuBtn(true);
            handler.post(new Runnable() {

                @Override
                public void run() {
                    showPlatToolsView();
                }
            });
        }
    }

    /**
     * 是否显示高级菜单
     */
    private void showPlatToolsView() {
        hideKeyboardAndTryHideGifView();
        mFlMore.setVisibility(View.GONE);
        if (mPlatformToolsShown) {
            if (mPlatMenus != null) {
                mPlatMenus.setVisibility(View.GONE);
            }
            mInputLayout.setVisibility(View.VISIBLE);
//            mRichPanel.setVisibility(View.VISIBLE);
            mPublicPlatMenuBtn.setImageResource(R.drawable.icon_keyboard_down);
        } else {
            mInputLayout.setVisibility(View.GONE);
//            mRichPanel.setVisibility(View.GONE);
            if (mPlatMenus != null) {
                mPlatMenus.setVisibility(View.VISIBLE);
            }
            mPublicPlatMenuBtn.setImageResource(R.drawable.icon_keyboard_up);
        }
        mPlatformToolsShown = !mPlatformToolsShown;
    }


    @Override
    public void onClick(View v) {
        //截取点击
        if (v.getId()==R.id.ib_file
                ||v.getId()==R.id.ib_more){// ||v.getId()==R.id.iv_item_redpager
            BaseToast.show(R.string.feature_not_support);
            return;
        }


        super.onClick(v);
        int i1 = v.getId();
        if (i1 == R.id.conversation_bottom_showCustomMenuView) {
            if (mMenuLists == null && platformMenu == null) {
                return;
            }
            showPlatToolsView();
        } else if (i1 == R.id.public_menu1) {
            if (mMenuLists != null && mMenuLists.size() != 0) {
                Menu parentMenu = mMenuLists.get(0);
                if (parentMenu.getType() == 1) {
                    openBrowser(parentMenu.getCommandid(), parentMenu.getTitle());
                } else if (parentMenu.getType() == 2) {
                } else if (parentMenu.getType() == 3) {
                } else if (parentMenu.getType() == 0) {
                    BaseToast.show(getContext(), "收取中,请稍候...");
                    ComposeMessageActivityControl.rcsImMenuMsgSendP(((PublicAccountChatPresenter) mPresenter).mAddress, mMenuLists.get(0).getCommandid());
                } else {
                    mItemMenus = parentMenu.getSubmenu();
                    for (int i = 0; i < mItemMenus.size(); i++) {
                        Menu menu = mItemMenus.get(i);
                        if (menu == null || menu.getTitle() == null || "".equals(menu.getTitle())) {
                            mItemMenus.remove(i);
                            i--;
                        }
                    }
                    for (int j = 0; j < mItemMenus.size(); j++) {
                        if (mItemMenus.size() == j + 1) {
                            break;
                        }
                        int curPriority = mItemMenus.get(j).getPriority();
                        int nextPriority = mItemMenus.get(j + 1).getPriority();
                        if (curPriority > nextPriority) {
                            mItemMenus.add(mItemMenus.remove(j + 1));
                        }
                    }
                    if (mItemMenus == null || mItemMenus.isEmpty())
                        return;
                    if (window != null && window.isShowing()) {
                        window.dismiss();
                        boolean isNeedShow = (Boolean) v.getTag(R.id.public_menu1);
                        if (!isNeedShow) {
                            makePopupWindow(v);
                        }
                    } else {
                        makePopupWindow(v);
                    }

                }
            } else if (platformMenu != null && platformMenu.size() != 0) {
                isShowSubMenu(platformMenu, 0, v);
            }
            public_menu1.setTag(R.id.public_menu1, true);
            public_menu2.setTag(R.id.public_menu1, false);
            public_menu3.setTag(R.id.public_menu1, false);
        } else if (i1 == R.id.public_menu2) {
            if (mMenuLists != null && mMenuLists.size() >= 2) {
                Menu parentMenu = mMenuLists.get(1);
                if (parentMenu.getType() == 1) {
                    openBrowser(parentMenu.getCommandid(), parentMenu.getTitle());
                    return;
                } else if (parentMenu.getType() == 2) {
                } else if (parentMenu.getType() == 3) {
                } else if (parentMenu.getType() == 0) {
                    BaseToast.show(getContext(), getString(R.string.gettign_wait));
                    ComposeMessageActivityControl.rcsImMenuMsgSendP(((PublicAccountChatPresenter) mPresenter).mAddress, mMenuLists.get(1).getCommandid());
                } else {
                    mItemMenus = parentMenu.getSubmenu();
                    for (int i = 0; i < mItemMenus.size(); i++) {
                        Menu menu = mItemMenus.get(i);
                        if (menu == null || menu.getTitle() == null || "".equals(menu.getTitle())) {
                            mItemMenus.remove(i);
                            i--;
                        }
                    }
                    for (int j = 0; j < mItemMenus.size(); j++) {
                        if (mItemMenus.size() == j + 1) {
                            break;
                        }
                        int curPriority = mItemMenus.get(j).getPriority();
                        int nextPriority = mItemMenus.get(j + 1).getPriority();
                        if (curPriority > nextPriority) {
                            mItemMenus.add(mItemMenus.remove(j + 1));
                        }
                    }
                    if (mItemMenus == null || mItemMenus.isEmpty())
                        return;
                    if (window != null && window.isShowing()) {
                        window.dismiss();
                        boolean isNeedShow = (Boolean) v.getTag(R.id.public_menu1);
                        if (!isNeedShow) {
                            makePopupWindow(v);
                        }
                    } else {
                        makePopupWindow(v);
                    }
                }
            } else if (platformMenu != null && platformMenu.size() >= 2) {
                isShowSubMenu(platformMenu, 1, v);

            }
            public_menu1.setTag(R.id.public_menu1, false);
            public_menu2.setTag(R.id.public_menu1, true);
            public_menu3.setTag(R.id.public_menu1, false);
        } else if (i1 == R.id.public_menu3) {
            if (mMenuLists != null && mMenuLists.size() >= 3) {
                Menu parentMenu = mMenuLists.get(2);
                if (parentMenu.getType() == 1) {
                    openBrowser(parentMenu.getCommandid(), parentMenu.getTitle());
                    return;
                } else if (parentMenu.getType() == 2) {
                } else if (parentMenu.getType() == 3) {
                } else if (parentMenu.getType() == 0) {
                    BaseToast.show(getContext(), getString(R.string.gettign_wait));
                    ComposeMessageActivityControl.rcsImMenuMsgSendP(((PublicAccountChatPresenter) mPresenter).mAddress, mMenuLists.get(2).getCommandid());
                } else {
                    mItemMenus = parentMenu.getSubmenu();
                    for (int i = 0; i < mItemMenus.size(); i++) {
                        Menu menu = mItemMenus.get(i);
                        if (menu == null || menu.getTitle() == null || "".equals(menu.getTitle())) {
                            mItemMenus.remove(i);
                            i--;
                        }
                    }
                    for (int j = 0; j < mItemMenus.size(); j++) {
                        if (mItemMenus.size() == j + 1) {
                            break;
                        }
                        int curPriority = mItemMenus.get(j).getPriority();
                        int nextPriority = mItemMenus.get(j + 1).getPriority();
                        if (curPriority > nextPriority) {
                            mItemMenus.add(mItemMenus.remove(j + 1));
                        }
                    }
                    if (mItemMenus == null || mItemMenus.isEmpty()) {
                        return;
                    }
                    if (window != null && window.isShowing()) {
                        window.dismiss();
                        boolean isNeedShow = (Boolean) v.getTag(R.id.public_menu1);
                        if (!isNeedShow) {
                            makePopupWindow(v);
                        }
                    } else {
                        makePopupWindow(v);
                    }
                }
            } else if (platformMenu != null && platformMenu.size() >= 3) {
                isShowSubMenu(platformMenu, 2, v);
            }
            public_menu1.setTag(R.id.public_menu1, false);
            public_menu2.setTag(R.id.public_menu1, false);
            public_menu3.setTag(R.id.public_menu1, true);
        } else {
        }

    }

    @Override
    protected void onSendClickReport(int type) {

    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(android.os.Message msg) {
            if(getActivity() == null || handler == null){
                return;
            }
            int what = msg.what;
            switch (what) {
                case PublicAccountUtil.GET_MENU_SUCCESS:
                    mMenuList = (MenuList) msg.obj;
                    String menuTimestamp = mMenuList.getMenuTimestamp();
                    LogF.d("platformEditro", "platformMenu-----menuTimestamp" + menuTimestamp);
                    mMenuLists = mMenuList.getMenuList();
                    // 去除空的menu
                    for (int i = 0; i < mMenuLists.size(); i++) {
                        if (mMenuLists.get(i) == null || mMenuLists.get(i).getTitle() == null || "".equals(mMenuLists.get(i).getTitle())) {
                            mMenuLists.remove(i);
                            i--;
                        }
                    }
                    // 排序
                    for (int j = 0; j < mMenuLists.size(); j++) {
                        if (mMenuLists.size() == j + 1) {
                            break;
                        }
                        int curPriority = mMenuLists.get(j).getPriority();
                        int nextPriority = mMenuLists.get(j + 1).getPriority();
                        if (curPriority > nextPriority) {
                            mMenuLists.add(mMenuLists.remove(j + 1));
                        }
                    }
                    public_menu1_line2.setVisibility(View.GONE);
                    public_menu1_line3.setVisibility(View.GONE);
                    public_menu1.setVisibility(View.GONE);
                    public_menu2.setVisibility(View.GONE);
                    public_menu3.setVisibility(View.GONE);
                    if (mMenuLists.size() > 0 && mMenuLists.get(0) != null) {
                        public_menu_name1.setText(mMenuLists.get(0).getTitle());
                        public_menu1.setVisibility(View.VISIBLE);
                        public_menu1.setOnClickListener(PublicAccountChatFrament.this);
                        if (mMenuLists.get(0).getType() != 0 && mMenuLists.get(0).getType() != 1 && mMenuLists.get(0).getType() != 2 && mMenuLists.get(0).getType() != 3) {
                            Drawable drawable= getResources().getDrawable(R.drawable.tabber_icon_more);
                            /// 这一步必须要做,否则不会显示.
                            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                            public_menu_name1.setCompoundDrawables(drawable, null, null, null);
                        }
                        public_menu1_line2.setVisibility(View.VISIBLE);

                    }
                    if (mMenuLists.size() > 1 && mMenuLists.get(1) != null) {
                        public_menu_name2.setText(mMenuLists.get(1).getTitle());
                        public_menu2.setVisibility(View.VISIBLE);
                        public_menu2.setOnClickListener(PublicAccountChatFrament.this);
                        public_menu1_line3.setVisibility(View.VISIBLE);
                        if (mMenuLists.get(1).getType() != 0 && mMenuLists.get(1).getType() != 1 && mMenuLists.get(1).getType() != 2 && mMenuLists.get(1).getType() != 3) {
                            Drawable drawable= getResources().getDrawable(R.drawable.tabber_icon_more);
                            /// 这一步必须要做,否则不会显示.
                            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                            public_menu_name2.setCompoundDrawables(drawable, null, null, null);
                        }
                    }
                    if (mMenuLists.size() > 2 && mMenuLists.get(2) != null) {
                        public_menu3.setVisibility(View.VISIBLE);
                        public_menu3.setOnClickListener(PublicAccountChatFrament.this);
                        public_menu_name3.setText(mMenuLists.get(2).getTitle());
                        public_menu1_line3.setVisibility(View.VISIBLE);
                        if (mMenuLists.get(2).getType() != 0 && mMenuLists.get(2).getType() != 1 && mMenuLists.get(2).getType() != 2 && mMenuLists.get(2).getType() != 3) {
                            Drawable drawable= getResources().getDrawable(R.drawable.tabber_icon_more);
                            /// 这一步必须要做,否则不会显示.
                            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                            public_menu_name3.setCompoundDrawables(drawable, null, null, null);
                        }
                    }
                    if (mMenuLists != null && mMenuLists.size() > 0) {
                        showPublicPlatMenuBtn(true);
                        if (platformMenu == null || platformMenu.size() == 0) {
                            showPlatToolsView();
                        }
                    }else{
                        showPublicPlatMenuBtn(false);
                        if (mPlatMenus != null) {
                            mPlatMenus.setVisibility(View.GONE);
                        }
                        mInputLayout.setVisibility(View.VISIBLE);
                    }
                    break;
                case PublicAccountUtil.GET_MENU_FAIL:
                    showPublicPlatMenuBtn(false);
                    if (mPlatMenus != null) {
                        mPlatMenus.setVisibility(View.GONE);
                    }
                    mInputLayout.setVisibility(View.VISIBLE);
                    break;
                case 0:
                    if (platformMenu != null && platformMenu.size() != 0) {
                        PublicAccountUtil.getInstance().getPublicMenu(((PublicAccountChatPresenter)mPresenter).mAddress, platformMenu.get(0).getMenuTimestamp(), handler);
                    } else {
                        PublicAccountUtil.getInstance().getPublicMenu(((PublicAccountChatPresenter)mPresenter).mAddress, "", handler);
                    }
                    break;
                default:
                    break;
            }
        }

    };

    /**
     * @param v 高级菜单弹框
     */
    private void makePopupWindow(final View v) {
        if (window == null)
            window = new PopupWindow(getActivity());
        if (window.isShowing()) {
            window.dismiss();
            return;
        }
        View.OnClickListener mMenuClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int pos = (Integer) v.getTag();
                if (mItemMenus != null) {
                    if (mItemMenus.get(pos).getType() == 1) {
                        openBrowser(mItemMenus.get(pos).getCommandid(), mItemMenus.get(pos).getTitle());
                    } else if (mItemMenus.get(pos).getType() == 2) {
                        // Device API调用
                    } else if (mItemMenus.get(pos).getType() == 3) {
                        // 应用调用
                    } else if (mItemMenus.get(pos).getType() == 0) {
                        // 模拟消息发送
                        BaseToast.show(getContext(), getString(R.string.gettign_wait));
                        ComposeMessageActivityControl.rcsImMenuMsgSendP(((PublicAccountChatPresenter)mPresenter).mAddress, mItemMenus.get(pos).getCommandid());
                    }
                } else {
                    if (mLocalItemMenus.get(pos).getMenu_type() == 1) {
                        openBrowser(mLocalItemMenus.get(pos).getMenu_commandid(), mLocalItemMenus.get(pos).getMenu_title());
                    } else if (mLocalItemMenus.get(pos).getMenu_type() == 2) {
                        // Device API调用
                    } else if (mLocalItemMenus.get(pos).getMenu_type() == 3) {
                        // 应用调用
                    } else if (mLocalItemMenus.get(pos).getMenu_type() == 0) {
                        // 模拟消息发送
                        BaseToast.show(getContext(), getString(R.string.gettign_wait));
                        ComposeMessageActivityControl.rcsImMenuMsgSendP(((PublicAccountChatPresenter)mPresenter).mAddress, mLocalItemMenus.get(pos).getMenu_commandid());
                    }
                }
                if (window.isShowing()) {
                    window.dismiss();
                    return;
                }
            }
        };

        LinearLayout contentView = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.public_menu_listview, null);
        TextView public_item_menu_name1 = (TextView) contentView.findViewById(R.id.public_item_menu_name1);
        TextView public_item_menu_name2 = (TextView) contentView.findViewById(R.id.public_item_menu_name2);
        TextView public_item_menu_name3 = (TextView) contentView.findViewById(R.id.public_item_menu_name3);
        TextView public_item_menu_name4 = (TextView) contentView.findViewById(R.id.public_item_menu_name4);
        TextView public_item_menu_name5 = (TextView) contentView.findViewById(R.id.public_item_menu_name5);

        View public_menu_line1 = (View) contentView.findViewById(R.id.public_menu_line1);
        View public_menu_line2 = (View) contentView.findViewById(R.id.public_menu_line2);
        View public_menu_line3 = (View) contentView.findViewById(R.id.public_menu_line3);
        View public_menu_line4 = (View) contentView.findViewById(R.id.public_menu_line4);

        LinearLayout public_item_menu_name1_ll = (LinearLayout) contentView.findViewById(R.id.public_item_menu_name1_ll);
        LinearLayout public_item_menu_name2_ll = (LinearLayout) contentView.findViewById(R.id.public_item_menu_name2_ll);
        LinearLayout public_item_menu_name3_ll = (LinearLayout) contentView.findViewById(R.id.public_item_menu_name3_ll);
        LinearLayout public_item_menu_name4_ll = (LinearLayout) contentView.findViewById(R.id.public_item_menu_name4_ll);
        LinearLayout public_item_menu_name5_ll = (LinearLayout) contentView.findViewById(R.id.public_item_menu_name5_ll);
        public_item_menu_name1_ll.setVisibility(View.GONE);
        public_item_menu_name2_ll.setVisibility(View.GONE);
        public_item_menu_name3_ll.setVisibility(View.GONE);
        public_item_menu_name4_ll.setVisibility(View.GONE);
        public_item_menu_name5_ll.setVisibility(View.GONE);
        if (mItemMenus != null) {
            if (mItemMenus.size() > 0 && mItemMenus.get(0) != null) {
                public_item_menu_name1.setText(mItemMenus.get(0).getTitle());
                public_item_menu_name1_ll.setVisibility(View.VISIBLE);
                public_item_menu_name1_ll.setTag(0);
                public_item_menu_name1_ll.setOnClickListener(mMenuClickListener);
            }
            if (mItemMenus.size() > 1 && mItemMenus.get(1) != null) {
                public_item_menu_name2.setText(mItemMenus.get(1).getTitle());
                public_item_menu_name2_ll.setVisibility(View.VISIBLE);
                public_item_menu_name2_ll.setTag(1);
                public_item_menu_name2_ll.setOnClickListener(mMenuClickListener);
            }

            if (mItemMenus.size() > 2 && mItemMenus.get(2) != null) {
                public_item_menu_name3.setText(mItemMenus.get(2).getTitle());
                public_item_menu_name3_ll.setVisibility(View.VISIBLE);
                public_item_menu_name3_ll.setTag(2);
                public_item_menu_name3_ll.setOnClickListener(mMenuClickListener);
            }
            if (mItemMenus.size() > 3 && mItemMenus.get(3) != null) {
                public_item_menu_name4.setText(mItemMenus.get(3).getTitle());
                public_item_menu_name4_ll.setVisibility(View.VISIBLE);
                public_item_menu_name4_ll.setTag(3);
                public_item_menu_name4_ll.setOnClickListener(mMenuClickListener);
            }
            if (mItemMenus.size() > 4 && mItemMenus.get(4) != null) {
                public_item_menu_name5.setText(mItemMenus.get(4).getTitle());
                public_item_menu_name5_ll.setVisibility(View.VISIBLE);
                public_item_menu_name5_ll.setTag(4);
                public_item_menu_name5_ll.setOnClickListener(mMenuClickListener);
            }
            switch (mItemMenus.size()) {
                case 5:
                    public_menu_line4.setVisibility(View.VISIBLE);
                case 4:
                    public_menu_line3.setVisibility(View.VISIBLE);
                case 3:
                    public_menu_line2.setVisibility(View.VISIBLE);
                case 2:
                    public_menu_line1.setVisibility(View.VISIBLE);
                    break;
            }

            if (mItemMenus.size() == 1) {
                public_menu_line1.setVisibility(View.GONE);
                public_menu_line2.setVisibility(View.GONE);
                public_menu_line3.setVisibility(View.GONE);
                public_menu_line4.setVisibility(View.GONE);
            } else if (mItemMenus.size() == 2) {
                public_menu_line1.setVisibility(View.VISIBLE);
                public_menu_line2.setVisibility(View.GONE);
                public_menu_line3.setVisibility(View.GONE);
                public_menu_line4.setVisibility(View.GONE);
            } else if (mItemMenus.size() == 3) {
                public_menu_line1.setVisibility(View.VISIBLE);
                public_menu_line2.setVisibility(View.VISIBLE);
                public_menu_line3.setVisibility(View.GONE);
                public_menu_line4.setVisibility(View.GONE);
            } else if (mItemMenus.size() == 4) {
                public_menu_line1.setVisibility(View.VISIBLE);
                public_menu_line2.setVisibility(View.VISIBLE);
                public_menu_line3.setVisibility(View.VISIBLE);
                public_menu_line4.setVisibility(View.GONE);
            } else if (mItemMenus.size() == 5) {
                public_menu_line1.setVisibility(View.VISIBLE);
                public_menu_line2.setVisibility(View.VISIBLE);
                public_menu_line3.setVisibility(View.VISIBLE);
                public_menu_line4.setVisibility(View.VISIBLE);
            }

            int itemHh = getResources().getDimensionPixelSize(R.dimen.public_pupwindow_menu_item_height);
            int itemHhh = getResources().getDimensionPixelSize(R.dimen.public_pupwindow_menu_item_height_p);
            int offset = getResources().getDimensionPixelSize(R.dimen.public_pupwindow_offset);
            int offset1 = getResources().getDimensionPixelSize(R.dimen.public_pupwindow_offset1);
            int offset2 = getResources().getDimensionPixelSize(R.dimen.dip_2);
            window.setWidth(public_menu1.getWidth());
            if (mItemMenus.size() == 1) {
                window.setHeight(itemHh * mItemMenus.size() + offset);
            } else
                window.setHeight(itemHh * mItemMenus.size());
            window.setBackgroundDrawable(getResources().getDrawable(R.drawable.transparent));
            window.setContentView(contentView);
            window.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                }
            });
            int x = 0;
            int y = 0;
            window.setFocusable(true);
            window.setTouchable(true);
            window.setOutsideTouchable(true);
            window.setAnimationStyle(R.style.PopupAnimation);
            v.getLocationOnScreen(xy);
            if (mMenuLists.size() == 1) {
                x = xy[0] - offset1 - offset2;
            } else if (mMenuLists.size() == 2) {
                x = xy[0] - public_menu1.getWidth() / 2 - offset1 - offset2;
            } else if (mMenuLists.size() == 3) {
                x = xy[0] - public_menu1.getWidth() / 2;
            }
            if (mItemMenus.size() == 1) {
                y = xy[1] - itemHh * (mItemMenus.size()) - offset;
            } else {
                y = xy[1] - itemHh * (mItemMenus.size());
            }
            window.showAsDropDown(v, 0, 0);
        } else if (mLocalItemMenus != null) {
            if (mLocalItemMenus.size() > 0 && mLocalItemMenus.get(0) != null) {
                public_item_menu_name1.setText(mLocalItemMenus.get(0).getMenu_title());
                public_item_menu_name1_ll.setVisibility(View.VISIBLE);
                public_item_menu_name1_ll.setTag(0);
                public_item_menu_name1_ll.setOnClickListener(mMenuClickListener);
            }
            if (mLocalItemMenus.size() > 1 && mLocalItemMenus.get(1) != null) {
                public_item_menu_name2.setText(mLocalItemMenus.get(1).getMenu_title());
                public_item_menu_name2_ll.setVisibility(View.VISIBLE);
                public_item_menu_name2_ll.setTag(1);
                public_item_menu_name2_ll.setOnClickListener(mMenuClickListener);
            }

            if (mLocalItemMenus.size() > 2 && mLocalItemMenus.get(2) != null) {
                public_item_menu_name3.setText(mLocalItemMenus.get(2).getMenu_title());
                public_item_menu_name3_ll.setVisibility(View.VISIBLE);
                public_item_menu_name3_ll.setTag(2);
                public_item_menu_name3_ll.setOnClickListener(mMenuClickListener);
            }
            if (mLocalItemMenus.size() > 3 && mLocalItemMenus.get(3) != null) {
                public_item_menu_name4.setText(mLocalItemMenus.get(3).getMenu_title());
                public_item_menu_name4_ll.setVisibility(View.VISIBLE);
                public_item_menu_name4_ll.setTag(3);
                public_item_menu_name4_ll.setOnClickListener(mMenuClickListener);
            }
            if (mLocalItemMenus.size() > 4 && mLocalItemMenus.get(4) != null) {
                public_item_menu_name5.setText(mLocalItemMenus.get(4).getMenu_title());
                public_item_menu_name5_ll.setVisibility(View.VISIBLE);
                public_item_menu_name5_ll.setTag(4);
                public_item_menu_name5_ll.setOnClickListener(mMenuClickListener);
            }
            switch (mLocalItemMenus.size()) {
                case 5:
                    public_menu_line4.setVisibility(View.VISIBLE);
                case 4:
                    public_menu_line3.setVisibility(View.VISIBLE);
                case 3:
                    public_menu_line2.setVisibility(View.VISIBLE);
                case 2:
                    public_menu_line1.setVisibility(View.VISIBLE);
                    break;
            }

            if (mLocalItemMenus.size() == 1) {
                public_menu_line1.setVisibility(View.GONE);
                public_menu_line2.setVisibility(View.GONE);
                public_menu_line3.setVisibility(View.GONE);
                public_menu_line4.setVisibility(View.GONE);
            } else if (mLocalItemMenus.size() == 2) {
                public_menu_line1.setVisibility(View.VISIBLE);
                public_menu_line2.setVisibility(View.GONE);
                public_menu_line3.setVisibility(View.GONE);
                public_menu_line4.setVisibility(View.GONE);
            } else if (mLocalItemMenus.size() == 3) {
                public_menu_line1.setVisibility(View.VISIBLE);
                public_menu_line2.setVisibility(View.VISIBLE);
                public_menu_line3.setVisibility(View.GONE);
                public_menu_line4.setVisibility(View.GONE);
            } else if (mLocalItemMenus.size() == 4) {
                public_menu_line1.setVisibility(View.VISIBLE);
                public_menu_line2.setVisibility(View.VISIBLE);
                public_menu_line3.setVisibility(View.VISIBLE);
                public_menu_line4.setVisibility(View.GONE);
            } else if (mLocalItemMenus.size() == 5) {
                public_menu_line1.setVisibility(View.VISIBLE);
                public_menu_line2.setVisibility(View.VISIBLE);
                public_menu_line3.setVisibility(View.VISIBLE);
                public_menu_line4.setVisibility(View.VISIBLE);
            }

            int itemHh = getResources().getDimensionPixelSize(R.dimen.public_pupwindow_menu_item_height);
            int itemHhh = getResources().getDimensionPixelSize(R.dimen.public_pupwindow_menu_item_height_p);
            int offset = getResources().getDimensionPixelSize(R.dimen.public_pupwindow_offset);
            int offset1 = getResources().getDimensionPixelSize(R.dimen.public_pupwindow_offset1);
            int offset2 = getResources().getDimensionPixelSize(R.dimen.dip_2);
            window.setWidth(public_menu1.getWidth());
            if (mLocalItemMenus.size() == 1) {
                window.setHeight(itemHh * mLocalItemMenus.size() + offset);
            } else
                window.setHeight(itemHh * mLocalItemMenus.size());
            window.setBackgroundDrawable(getResources().getDrawable(R.drawable.transparent));
            window.setContentView(contentView);
            window.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                }
            });
            int x = 0;
            int y = 0;
            window.setFocusable(true);
            window.setTouchable(true);
            window.setOutsideTouchable(true);
            window.setAnimationStyle(R.style.PopupAnimation);
            v.getLocationOnScreen(xy);
            if (platformMenu.size() == 1) {
                x = xy[0] - offset1 - offset2;
            } else if (platformMenu.size() == 2) {
                x = xy[0] - public_menu1.getWidth() / 2 - offset1 - offset2;
            } else if (platformMenu.size() == 3) {
                x = xy[0] - public_menu1.getWidth() - offset1 - offset2;
            }
            if (platformMenu.size() == 1) {
                y = xy[1] - itemHh * (mLocalItemMenus.size()) - offset;
            } else {
                y = xy[1] - itemHh * (mLocalItemMenus.size());
            }
            window.showAsDropDown(v, 0, 0);
        }

    }

    /**
     * @param platformMenu
     * @param i
     * @param v
     * 是否显示子菜单
     */
    private void isShowSubMenu(ArrayList<PlatformMenu> platformMenu, int i, View v) {
        PlatformMenu parentMenu = platformMenu.get(i);
        if (parentMenu.getMenu_type() == 1) {
            openBrowser(parentMenu.getMenu_commandid(), parentMenu.getMenu_title());
        } else if (parentMenu.getMenu_type() == 2) {
        } else if (parentMenu.getMenu_type() == 3) {
        } else if (parentMenu.getMenu_type() == 0) {
            BaseToast.show(getContext(), getString(R.string.gettign_wait));
            ComposeMessageActivityControl.rcsImMenuMsgSendP(((PublicAccountChatPresenter)mPresenter).mAddress, parentMenu.getMenu_commandid());
        } else {
            mLocalItemMenus = parentMenu.getSubMenuList();
            if (mLocalItemMenus == null || mLocalItemMenus.isEmpty())
                return;
            if (window != null && window.isShowing()) {
                window.dismiss();
                boolean isNeedShow = (Boolean) v.getTag(R.id.public_menu1);
                if (!isNeedShow) {
                    makePopupWindow(v);
                }
            } else {
                makePopupWindow(v);
            }
        }
    }

    /**
     * @param url webview 查看链接
     * @param name
     */
    public void openBrowser(String url, String name) {
        if (url == null || "".equals(url))
            return;
        WebConfig config = new WebConfig.Builder()
                .enableShare()
                .title(name)
                .build(url);
        EnterPriseProxy.g.getUiInterface()
                .gotoEnterpriseH5Activity(getActivity(), config);
    }

    private void showPublicPlatMenuBtn(boolean show) {
        View inputDividerTop = mRootView.findViewById(R.id.input_divider_top);
        View inputDividerInside = mRootView.findViewById(R.id.input_divider_inside);
        View inputAndMenu = mRootView.findViewById(R.id.input_and_menu);
        if (show) {
            mPublicPlatMenuBtn.setVisibility(View.VISIBLE);
            inputDividerTop.setVisibility(View.VISIBLE);
            inputDividerInside.setVisibility(View.VISIBLE);
            inputAndMenu.setBackgroundColor(getResources().getColor(R.color.color_f5f5f5));
        } else {
            mPublicPlatMenuBtn.setVisibility(View.GONE);
            inputDividerTop.setVisibility(View.GONE);
            inputDividerInside.setVisibility(View.GONE);
            inputAndMenu.setBackgroundColor(getResources().getColor(R.color.transparent));
        }

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    @Override
    public void onCreateOptionsMenu(android.view.Menu menu, MenuInflater inflater) {
        Log.d(TAG, "onNewIntent: "+ menu.size());
        menu.clear();
        if (!isPreConversation) {
            inflater.inflate(R.menu.menu_publicaccount_detail, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Bundle bundle;
        bundle = getArguments();
        int i1 = item.getItemId();
        if (i1 == R.id.action_setting) {
            hideKeyboardAndTryHideGifView();
//                GroupSettingActivity.startForResult(getActivity(), 100, bundle);
            // startForResult与singletast冲突
            startDetailActivity();

        }
        return false;
    }




    private void startDetailActivity(){
        Intent intent = new Intent(getActivity(), PublicAccountDetailActivity.class);
        String pa_uuid = getArguments().getString("address");
        String name = getArguments().getString("person");
        Log.d("cxh", "search  pa_uuid===" + pa_uuid);
        intent.putExtra("pa_uuid", pa_uuid);
        intent.putExtra("name", name);
        intent.putExtra("isSearch", false);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    public void sendSuperMessage(String msg) {

    }

    @Override
    public void hideToolBarMenu() {

    }

    @Override
    public void showToolBarMenu() {

    }
}