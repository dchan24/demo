package com.cmicc.module_message.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.chinamobile.app.yuliao_business.logic.common.LogicActions;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_core.cmccauth.AuthWrapper;
import com.chinamobile.app.yuliao_core.db.LoginDaoImpl;
import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.yuliao_core.util.NumberUtils;
import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmicc.module_message.ui.adapter.NoNentryGroupAdapter;
import com.cmcc.cmrcs.android.ui.control.GroupChatControl;
import com.cmcc.cmrcs.android.ui.logic.common.UIObserver;
import com.cmcc.cmrcs.android.ui.logic.common.UIObserverManager;
import com.cmcc.cmrcs.android.ui.utils.WrapContentLinearLayoutManager;
import com.cmicc.module_message.R;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Created by LY on 2018/5/23.
 * 普通群成员邀请界面
 */

public class NonEntryGroupAtivity extends BaseActivity {

    private String TAG = "NonEntryGroupAtivity" ;

    public static String GROUPID = "GROUPID";
    public static String GROUPURI = "GROUPURI" ;
    public static String GROUPNAME = "GROUPNAME" ;

    private TextView titleView;
    private RecyclerView mRecyclerView; // 列表
    private RelativeLayout invitesAgainRl ;
    private TextView inviteView; // 邀请他们使用
    private Handler mHandle ;
    private NoNentryGroupAdapter mAdapter ;

    private ProgressDialog mProgressDialog;

    private String groupID ;
    private String groupURI ;
    private String mToken = "";

    private LinearLayout stipLl ;
    private ImageView stipImage ;
    private TextView stipText ;

    private ArrayList<String> datas = new ArrayList<>();

    private Toast toast ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nonentry_group_layout);
    }

    @Override
    protected void findViews() {
        findViewById(R.id.left_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        titleView = (TextView) findViewById(R.id.select_picture_custom_toolbar_title_text);
        titleView.setText(getResources().getString(R.string.inviting_staff));
        findViewById(R.id.select_rl).setVisibility(View.GONE);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        invitesAgainRl = (RelativeLayout) findViewById(R.id.invites_again_rl);
        inviteView = (TextView) findViewById(R.id.invites_again);
        invitesAgainRl.setVisibility(View.GONE); // 一开始先不显示 等到数据请求下来在显示
        inviteView.setOnClickListener(this);

        stipLl = (LinearLayout) findViewById(R.id.stip_ll);
        stipImage = (ImageView) findViewById(R.id.stip_image);
        stipText = (TextView) findViewById(R.id.stip_text);
    }

    @Override
    protected void init() {
        toast = new Toast(this);
        groupID = getIntent().getStringExtra(GROUPID);
        groupURI = getIntent().getStringExtra(GROUPURI);
        if(TextUtils.isEmpty(groupID) || TextUtils.isEmpty(groupURI) ){
            return;
        }
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage(getString(R.string.wait_please));
        mProgressDialog.setCancelable(true);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setIndeterminate(false);

        mRecyclerView.setLayoutManager(new WrapContentLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mAdapter = new NoNentryGroupAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mHandle = new Handler(){
            @Override
            public void handleMessage(Message message){
                if(message.what == 1000 ){
                    requestData();
                }else if(message.what == 1001){
                    toast.cancel();
                    inviteView.setEnabled(true);
                    finish();
                }
            }
        };

        // 添加群成员
        ArrayList<Integer> actions = new ArrayList<>();
        actions.add(LogicActions.GROUP_CHAT_ADD_PARTICIPANT);          // 添加一个成员
        actions.add(LogicActions.GROUP_CHAT_ADD_PARTICIPANT_FAIL);     // 添加一个成员失败
        actions.add(LogicActions.GROUP_CHAT_ADD_PARTICIPANT_LIST);     // 添加一组群成员
        actions.add(LogicActions.GROUP_CHAT_ADD_PARTICIPANT_LIST_FAIL);// 添加一组群成员失败
        actions.add(LogicActions.GROUP_CHAT_ADD_PARTICIPANT_OK_CB);    // 添加成员成功回调,邀请的人已经加入群才回调
        actions.add(LogicActions.GROUP_CHAT_ADD_PARTICIPANT_FAIL_CB);  // 添加成员失败回调
        actions.add(LogicActions.GROUP_CHAT_ERROR_NETWORK);            // 网络失败
        UIObserverManager.getInstance().registerObserver(mUIObserver, actions);

        if (!AndroidUtil.isNetworkConnected(this)) {
            stipLl.setVisibility(View.VISIBLE);
            stipText.setText(getResources().getString(R.string.data_error_retry));
            stipImage.setBackgroundResource(R.drawable.network_anomaly);
        }else{
            getToken() ;
        }
    }

    /**
     * 获取token
     */
    private void getToken(){
        mProgressDialog.show();
        AuthWrapper.getInstance(this).getRcsAuth(new AuthWrapper.RequestTokenListener() {
            @Override
            public void onFail(int errorCode) {
                LogF.d(TAG, "getToken errorCode : "+ errorCode);
                NonEntryGroupAtivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog.dismiss();
                        stipLl.setVisibility(View.VISIBLE);
                        stipText.setText(getResources().getString(R.string.data_error_retry));
                        stipImage.setBackgroundResource(R.drawable.network_anomaly);
                    }
                });
            }
            @Override
            public void onSuccess(String account, String password) {
            }
            @Override
            public void onSuccess(String token) {
                LogF.d(TAG, "getToken token : "+ token);
                mToken = token ;
                mHandle.sendEmptyMessage(1000);
            }
        });
    }

    /**
     * 请求数据
     */
    private void requestData(){
        String loginUser = LoginDaoImpl.getInstance().queryLoginUser(this); // 获取登录的手机号码
        if(TextUtils.isEmpty(loginUser)){
            return;
        }
        loginUser = NumberUtils.getDialablePhoneWithCountryCode(loginUser);
        //"entry%5b@type=%222%22%5d  用转码 @ 和 = 不能转 转了就得不到结果
        String  url = String.format("https://ndm.fetiononline.com/public-group/global/%s/index.xml/~~/public-group/list/getNoRcs/" ,groupURI )+ "entry%5b@type=%222%22%5d" ;
        LogF.d(TAG, "requestData url : "+ url);
        Request.Builder builder = new Request.Builder().url(url).get();
        builder.addHeader("Authorization","UA token=\""+mToken+"\"");
        builder.addHeader("X-3GPP-Intended-Identity","tel:"+loginUser);
        builder.addHeader("ClientType","APP");
        builder.addHeader("SourceData","ZIYANG");
        builder.addHeader("Host","ndm.fetiononline.com"); // ndm.fetiononline.com
        builder.addHeader("Content-Length","0");
        Request request = builder.build();
        OkHttpClient client = new OkHttpClient();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call , IOException e) {
                LogF.d(TAG, "requestData onFailure IOException e : "+ e.getMessage());
                NonEntryGroupAtivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog.dismiss();
                        stipLl.setVisibility(View.VISIBLE);
                        stipText.setText(getResources().getString(R.string.data_error_retry));
                        stipImage.setBackgroundResource(R.drawable.network_anomaly);
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                int code = response.code() ;
                String rset = response.body().string() ;
                LogF.d(TAG, "requestData onResponse code : "+ code + "  rset : "+ rset);
                if(code == 200 ){
                    try {
                        StringReader sr = new StringReader(rset);
                        InputSource is = new InputSource(sr);
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder builder;
                        builder = factory.newDocumentBuilder();
                        Document doc = builder.parse(is);
                        Element rootElement = doc.getDocumentElement();
                        NodeList nodeList = rootElement.getElementsByTagName("entry");
                        if(nodeList != null && nodeList.getLength() > 0 ){
                            for( int i = 0 ; i < nodeList.getLength() ; i++ ){
                                Node node = nodeList.item(i) ;
                                String name = node.getNodeName();
                                LogF.d(TAG, "requestData onResponse name : " + name );
                                if("entry".equals(name)){
                                    //获取entry的属性
                                    NamedNodeMap nnm = node.getAttributes();
                                    //获取uri属性，由于只有一个属性，所以取0
                                    Node n = nnm.item(0);
                                    String textContent = n.getTextContent();
                                    if(!TextUtils.isEmpty(textContent) && textContent.startsWith("tel:")){
                                        textContent = textContent.substring(4,textContent.length());
                                        textContent = NumberUtils.getNumForStore(textContent);
                                        datas.add(textContent);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        LogF.d(TAG, "requestData onResponse Exception " );
                    }finally {
                        NonEntryGroupAtivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressDialog.dismiss();
                                LogF.d(TAG, "requestData onResponse datas size : "+ datas.size() );
                                if(datas.size()>0){
                                    stipLl.setVisibility(View.GONE);
                                    mAdapter.setDatas(datas);
                                    invitesAgainRl.setVisibility(View.VISIBLE); // 有数据才显示
                                }else{
                                    stipLl.setVisibility(View.VISIBLE);
                                    stipText.setText(getResources().getString(R.string.all_into_the_group));
                                    stipImage.setBackgroundResource(R.drawable.load_exception);
                                }
                            }
                        });
                    }
                }else{
                    NonEntryGroupAtivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressDialog.dismiss();
                            stipLl.setVisibility(View.VISIBLE);
                            stipText.setText(getResources().getString(R.string.data_error));
                            stipImage.setBackgroundResource(R.drawable.data_exception);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int id = v.getId();
        if( id == R.id.invites_again ){ // 邀请
            addGroupMember();
        }
    }

    /**
     * 添加群成员
     */
    private void addGroupMember(){
        if (!AndroidUtil.isNetworkConnected(this)) {
            Toast.makeText(this, getString(R.string.network_disconnect), Toast.LENGTH_SHORT).show();
            return;
        }
        if(datas.size()<=0){
            Toast.makeText(this, getString(R.string.no_inviting_people), Toast.LENGTH_SHORT).show();
            return;
        }
        StringBuilder sb = new StringBuilder();
        for(int i = 0 ; i < datas.size() ; i++ ){
            if(i == datas.size()-1){
                sb.append(datas.get(i));
            }else{
                sb.append(datas.get(i)).append(";");
            }
        }
        LogF.d(TAG, "requestData addGroupMember sb : "+ sb.toString() );
        inviteView.setEnabled(false);
        mProgressDialog.show();
        GroupChatControl.rcsImSessAddPartp(groupID, sb.toString());
    }

    private UIObserver mUIObserver = new UIObserver() {
        @Override
        protected void onReceiveAction(int action, Intent intent) {
            final String Id = intent.getStringExtra(LogicActions.GROUP_CHAT_ID);
            LogF.d(TAG, "onReceiveAction action = " + action + ", Id = " + Id );
            if (groupID.equals(Id)) {
                switch (action) {
                    // 成功的处理
//                    case LogicActions.GROUP_CHAT_ADD_PARTICIPANT: // 添加一个群成员
//                    case LogicActions.GROUP_CHAT_ADD_PARTICIPANT_LIST:  // 添加一组群成员
                    case LogicActions.GROUP_CHAT_ADD_PARTICIPANT_OK_CB:  // 添加成员成功回调,邀请的人已经加入群才回调
                        NonEntryGroupAtivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressDialog.dismiss();
                                toast.makeText( NonEntryGroupAtivity.this , NonEntryGroupAtivity.this.getResources().getString(R.string.group_inviting_sended) ,Toast.LENGTH_SHORT ).show();
                            }
                        });
                        mHandle.sendEmptyMessageDelayed(1001 , 1000 * 2 );
                        break;
                    // 失败的处理
                    case LogicActions.GROUP_CHAT_ADD_PARTICIPANT_FAIL: // 添加一个成员失败
                    case LogicActions.GROUP_CHAT_ADD_PARTICIPANT_LIST_FAIL:// 添加一组群成员失败
                    case LogicActions.GROUP_CHAT_ADD_PARTICIPANT_FAIL_CB: // 添加成员失败回调
                        NonEntryGroupAtivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                inviteView.setEnabled(true);
                                mProgressDialog.dismiss();
                                Toast.makeText( NonEntryGroupAtivity.this , NonEntryGroupAtivity.this.getResources().getString(R.string.group_inviting_sended_fail) ,Toast.LENGTH_SHORT ).show();
                            }
                        });
                        break;
                    // 失败的处理
                    case LogicActions.GROUP_CHAT_ERROR_NETWORK:
                        NonEntryGroupAtivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                inviteView.setEnabled(true);
                                mProgressDialog.dismiss();
                                Toast.makeText( NonEntryGroupAtivity.this , NonEntryGroupAtivity.this.getResources().getString(R.string.bad_network_no_page) ,Toast.LENGTH_SHORT ).show();
                            }
                        });
                        break;
                    default:
                }
            }
        }
    };
}
