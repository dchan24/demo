package com.cmicc.module_message.ui.activity;

import android.app.ProgressDialog;
import android.graphics.Typeface;
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
import android.widget.TextView;
import android.widget.Toast;

import com.chinamobile.app.utils.AndroidUtil;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_core.cmccauth.AuthWrapper;
import com.chinamobile.app.yuliao_core.db.LoginDaoImpl;
import com.chinamobile.app.yuliao_core.util.NumberUtils;
import com.cmcc.cmrcs.android.ui.activities.BaseActivity;
import com.cmicc.module_message.ui.adapter.NoRcsGroupMemberAdapter;
import com.cmicc.module_message.ui.data.NoRcsUser;
import com.cmcc.cmrcs.android.ui.utils.WrapContentLinearLayoutManager;
import com.cmcc.cmrcs.android.widget.BaseToast;
import com.cmicc.module_message.R;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by LY on 2018/5/22.
 * 企业群未使用人员界面
 */

public class NoRcsGroupMemberActivity extends BaseActivity implements NoRcsGroupMemberAdapter.OnItemClick{

    private String TAG = "NoRcsGroupMemberActivity" ;

    public static String GROUPID = "GROUPID";
    public static String GROUPURI = "GROUPURI" ;
    public static String GROUPNAME = "GROUPNAME" ;

    private ArrayList<String> datas = new ArrayList<>();
    private ArrayList<NoRcsUser> adapterDatas = new ArrayList<>();

    private TextView titleView;
    private RecyclerView mRecyclerView; // 列表
    private TextView inviteView; // 邀请他们使用
    private LinearLayout smsInviteLl; // 短信邀请一栏
    private TextView selectAndCancle; // 取消和全选
    private TextView smsInvite; // 短信邀请

    private NoRcsGroupMemberAdapter mAdapter;
    private String mToken = "";
    private Handler mHandler ;
    private ProgressDialog mProgressDialog;

    private String groupID ;
    private String groupURI ;
    private String groupName ;

    private LinearLayout stipLl ;
    private ImageView stipImage ;
    private TextView stipText ;

    private Toast toast ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_norsc_groupmember_layout);
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
        titleView.setText(getResources().getString(R.string.not_used_group_member));
        titleView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        findViewById(R.id.select_rl).setVisibility(View.GONE);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        inviteView = (TextView) findViewById(R.id.invite_them_to_use);
        smsInviteLl = (LinearLayout) findViewById(R.id.sms_invite_ll);
        selectAndCancle = (TextView) findViewById(R.id.select_and_cancle);
        smsInvite = (TextView) findViewById(R.id.sms_invite);
        inviteView.setVisibility(View.GONE); // 一开始不显示，请求数据成功了才显示
        smsInviteLl.setVisibility(View.GONE);
        inviteView.setOnClickListener(this);
        selectAndCancle.setOnClickListener(this);
        smsInvite.setOnClickListener(this);

        stipLl = (LinearLayout) findViewById(R.id.stip_ll);
        stipImage = (ImageView) findViewById(R.id.stip_image);
        stipText = (TextView) findViewById(R.id.stip_text);
    }

    @Override
    protected void init() {
        groupID = getIntent().getStringExtra(GROUPID);
        groupURI = getIntent().getStringExtra(GROUPURI);
        groupName =  getIntent().getStringExtra(GROUPNAME);
        if(TextUtils.isEmpty(groupID) || TextUtils.isEmpty(groupURI) || TextUtils.isEmpty(groupName)){
            return;
        }

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage(getString(R.string.wait_please));
        mProgressDialog.setCancelable(true);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setIndeterminate(false);

        mRecyclerView.setLayoutManager(new WrapContentLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mAdapter = new NoRcsGroupMemberAdapter(this, groupID);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClick(this);

        toast = new Toast(this);
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message message){
                if(message.what == 1000 ){
                    requestData(); // 请求数据
                }else if(message.what == 1001){
                    toast.cancel();
                    finish();
                }
            }
        };

        if (!AndroidUtil.isNetworkConnected(this)) {
            stipLl.setVisibility(View.VISIBLE);
            stipText.setText(getResources().getString(R.string.data_error_retry));
            stipImage.setBackgroundResource(R.drawable.network_anomaly);
        }else{
            getToken() ;
        }
    }


    @Override
    public void onClick(View v) {
        int id = v.getId() ;
        if( id == R.id.invite_them_to_use && mAdapter!=null){ // 邀请他们使用
            if(datas.size() == 0 ){
                Toast.makeText( this , "请选择需邀请人" ,Toast.LENGTH_SHORT ).show();
                return;
            }
            if (!AndroidUtil.isNetworkConnected(this)) {
                BaseToast.show(R.string.network_disconnect);
            }else{
                smsInvite.setEnabled(false); //
                // 请求接口 发短信
                ArrayList<String> ps = mAdapter.getSelectPhons() ;
                String s = getXML(datas);
                if(!TextUtils.isEmpty(s)){
                    s = "<?xml version='1.0'?>"+ s;
                }
                LogF.d("NoRcsGroupMemberActivity" , " s = "+ s );
                sendSMS( s );
            }
//            mAdapter.setIsCanChoose(true);   // 表示可以点击了
//            mAdapter.setState(true); // 全选
//            selectAndCancle.setTag("cancle"); // 取消全选
//            selectAndCancle.setText(R.string.cancel_total_selection); // 取消全选
//            inviteView.setVisibility(View.GONE);
//            smsInviteLl.setVisibility(View.VISIBLE);
        }else if(id == R.id.select_and_cancle && mAdapter!=null){  //  全选和取消群选
            if("cancle".equals(v.getTag())){     // "取消全选"  点击
                mAdapter.setState(false); // 取消全选
                selectAndCancle.setText(R.string.total_selection); // 全选
                selectAndCancle.setTag("ts");
            }else if("ts".equals(v.getTag())){   // "全选" 点击
                mAdapter.setState(true); // 全选
                selectAndCancle.setText(R.string.cancel_total_selection);  // 取消全选
                selectAndCancle.setTag("cancle");
            }
        }else if(id == R.id.sms_invite && mAdapter!=null){  // 短信邀请使用
            if(mAdapter.getSelectPhons().size() == 0 ){
                Toast.makeText( this , "请选择需邀请人" ,Toast.LENGTH_SHORT ).show();
                return;
            }
            if (!AndroidUtil.isNetworkConnected(this)) {
                BaseToast.show(R.string.network_disconnect);
            }else{
                smsInvite.setEnabled(false); //
                // 请求接口 发短信
                ArrayList<String> ps = mAdapter.getSelectPhons() ;
                String s = getXML(ps);
                if(!TextUtils.isEmpty(s)){
                    s = "<?xml version='1.0'?>"+ s;
                }
                LogF.d("NoRcsGroupMemberActivity" , " s = "+ s );
                sendSMS( s );
            }
        }
    }

    @Override
    public void onItemClick( String phone ) {
        if( mAdapter != null && mAdapter.getSelectPhons() != null ){
            if(datas.size() == mAdapter.getSelectPhons().size()){ // 全选了
                selectAndCancle.setText(R.string.cancel_total_selection);  // 取消全选
                selectAndCancle.setTag("cancle");
            }else{ // 没有全选
                selectAndCancle.setText(R.string.total_selection); // 全选
                selectAndCancle.setTag("ts");
            }
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
                LogF.d(TAG , "getToken errorCode : "+ errorCode );
                NoRcsGroupMemberActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgress();
                        stipLl.setVisibility(View.VISIBLE);
                        stipText.setText(getResources().getString(R.string.data_error_retry));
                        stipImage.setBackgroundResource(R.drawable.load_exception);
                    }
                });
            }
            @Override
            public void onSuccess(String account, String password) {
            }
            @Override
            public void onSuccess(String token) {
                mToken = token ;
                mHandler.sendEmptyMessage(1000);
                LogF.d(TAG , "getToken token : "+ token );
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
        String url =String.format("https://ndm.fetiononline.com/public-group/global/%s/index.xml/~~/public-group/list/getNoRcs/" ,groupURI )+"entry%5b@type=%221%22%5d"; //
        LogF.d(TAG , "requestData url : "+ url );
        Request.Builder builder = new Request.Builder().url(url).get();
        builder.addHeader("Authorization","UA token=\""+mToken+"\"");
        builder.addHeader("X-3GPP-Intended-Identity","tel:"+loginUser);
        builder.addHeader("ClientType","APP");
        builder.addHeader("SourceData","ZIYANG");
        builder.addHeader("Host","ndm.fetiononline.com"); //   ndm.fetiononline.com
        builder.addHeader("Content-Length","0");
        Request request = builder.build();
        OkHttpClient client = new OkHttpClient();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call , IOException e) {
                LogF.d(TAG, "requestData onFailure IOException e: "+e.getMessage());
                NoRcsGroupMemberActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgress();
                        stipLl.setVisibility(View.VISIBLE);
                        stipText.setText(getResources().getString(R.string.data_error_retry));
                        stipImage.setBackgroundResource(R.drawable.load_exception);
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                int code = response.code() ;
                String rset = response.body().string() ;
                LogF.d(TAG, "requestData onResponse code : "+code + "  rset : "+ rset);
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
                                if("entry".equals(name)){
                                    //获取entry的属性
                                    NamedNodeMap nnm = node.getAttributes();
                                    //获取uri属性，由于只有一个属性，所以取0
                                    Node n = nnm.item(0);
                                    String textContent = n.getTextContent();
                                    LogF.d(TAG, "requestData onResponse name : "+name + "  textContent : "+ textContent);
                                    if(!TextUtils.isEmpty(textContent) && textContent.startsWith("tel:")){
                                        textContent = textContent.substring(4,textContent.length());
                                        datas.add(textContent);
                                        adapterDatas.add(new NoRcsUser( textContent ,false));
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        LogF.d(TAG, "requestData onResponse Exception ");
                    }finally {
                        NoRcsGroupMemberActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideProgress();
                                LogF.d(TAG, "requestData onResponse adapterDatas size : "+adapterDatas.size());
                                if(adapterDatas.size()>0){
                                    stipLl.setVisibility(View.GONE);
                                    mAdapter.setDatas(adapterDatas);
                                    inviteView.setVisibility(View.VISIBLE); // 有数据才显示
                                }else{
                                    stipLl.setVisibility(View.VISIBLE);
                                    stipText.setText(getResources().getString(R.string.all_into_the_group));
                                    stipImage.setBackgroundResource(R.drawable.load_exception);
                                }
                            }
                        });
                    }
                }else{
                    NoRcsGroupMemberActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideProgress();
                            stipLl.setVisibility(View.VISIBLE);
                            stipText.setText(getResources().getString(R.string.data_error));
                            stipImage.setBackgroundResource(R.drawable.load_exception);
                        }
                    });
                }
            }
        });
    }

    private void hideProgress(){
        if(NoRcsGroupMemberActivity.this.isFinishing() || NoRcsGroupMemberActivity.this.isDestroyed()
                || mProgressDialog == null)
            return;
        try {
            mProgressDialog.hide();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 发送短信
     */
    private void sendSMS( String body){
        String loginUser = LoginDaoImpl.getInstance().queryLoginUser(this); // 获取登录的手机号码
        if(TextUtils.isEmpty(loginUser) || TextUtils.isEmpty(body) ){
            smsInvite.setEnabled(true); //
            return;
        }
        mProgressDialog.show();
        loginUser = NumberUtils.getDialablePhoneWithCountryCode(loginUser);
        String url =String.format("https://ndm.fetiononline.com/public-group/global/%s/index.xml/~~/public-group/list/getNoRcs/sendInviteSms" ,groupURI );
        LogF.e("NoRcsGroupMemberActivity", "sms url = "+url);
        MediaType mediaType = MediaType.parse("application/xml; charset=utf-8");
        RequestBody requestBody = RequestBody.create(mediaType ,body);
        Request.Builder requestBuilder = new Request.Builder().url(url).put(requestBody);
        requestBuilder.addHeader("Authorization","UA token=\""+mToken+"\"");
        requestBuilder.addHeader("X-3GPP-Intended-Identity","tel:"+loginUser);
        requestBuilder.addHeader("Content-Type","application/public-group+xml; charset=\"utf-8\"");
        requestBuilder.addHeader("ClientType","APP");
        requestBuilder.addHeader("SourceData","ZIYANG");
        requestBuilder.addHeader("Host","ndm.fetiononline.com");
        requestBuilder.addHeader("Content-Length","130");
        Request request = requestBuilder.build() ;
        OkHttpClient httpClient = new OkHttpClient();
        Call call = httpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                hideProgress();
                LogF.d(TAG, "sendSMS onFailure IOException e :  "+e.getMessage());
                NoRcsGroupMemberActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        smsInvite.setEnabled(true);
                        Toast.makeText( NoRcsGroupMemberActivity.this , "邀请短信发送失败" ,Toast.LENGTH_SHORT ).show();
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                NoRcsGroupMemberActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        smsInvite.setEnabled(true);
                        hideProgress();
                    }
                });
                int code =  response.code() ;
                String body = response.body().string() ;
                LogF.d(TAG, "sendSMS onResponse code :  "+code + "  body : "+ body );
                if( code == 200 ){
                    NoRcsGroupMemberActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toast.makeText( NoRcsGroupMemberActivity.this , "邀请短信已发送" ,Toast.LENGTH_SHORT ).show();
                        }
                    });
                    // 插入数据 关闭当前界面
                    //com.chinamobile.app.yuliao_business.model.Message msg = new com.chinamobile.app.yuliao_business.model.Message();
                    //msg.setAddress(groupID);
                    //long time = currentTimeMillis(-1);
                    //msg.setDate(time);
                    //msg.setTimestamp(time);
                    //msg.setType(Type.TYPE_MSG_SYSTEM_TEXT);
                    //msg.setStatus(Status.STATUS_OK);
                    //msg.setBody(NoRcsGroupMemberActivity.this.getResources().getString(R.string.invite_sms_sended));
                    //insert( NoRcsGroupMemberActivity.this, msg);
                    mHandler.sendEmptyMessageDelayed(1001 , 1000 * 2 );
                }else{
                    NoRcsGroupMemberActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText( NoRcsGroupMemberActivity.this , "邀请短信发送失败" ,Toast.LENGTH_SHORT ).show();
                        }
                    });
                }
            }
        });
    }

    /**
     * 拼接请求的body
     * @param ps
     * @return
     */
    private String getXML(ArrayList<String> ps ){
        StringWriter stringWriter = new StringWriter();
        try {
            // 获取XmlSerializer对象
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlSerializer xmlSerializer = factory.newSerializer();
            // 设置输出流对象
            xmlSerializer.setOutput(stringWriter);
            //xmlSerializer.startDocument("utf-8", true);
            xmlSerializer.startTag(null, "public-group");
            xmlSerializer.attribute("","xmlns","com:feinno:public-group");
            xmlSerializer.attribute("","xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance");
            xmlSerializer.startTag(null, "list");
            xmlSerializer.attribute("","name", URLEncoder.encode(groupName, "UTF-8"));
            xmlSerializer.attribute("","uri",groupURI);
            for(int i = 0 ; i < ps.size() ; i++ ){
                xmlSerializer.startTag(null, "entry");
                xmlSerializer.attribute("","uri","tel:"+ps.get(i));
                xmlSerializer.startTag(null, "display-name");
                xmlSerializer.text(ps.get(i));
                xmlSerializer.endTag(null, "display-name");
                xmlSerializer.endTag(null, "entry");
            }
            xmlSerializer.endTag(null, "list");
            xmlSerializer.endTag(null, "public-group");
            xmlSerializer.endDocument();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return stringWriter.toString();
    }
}
