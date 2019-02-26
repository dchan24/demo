package com.cmicc.module_message.ui.presenter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.chinamobile.app.yuliao_business.logic.common.LogicActions;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_business.util.MessageUtils;
import com.chinamobile.app.yuliao_business.util.Status;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.yuliao_common.utils.FileUtil;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.chinamobile.app.yuliao_core.util.NumberUtils;
import com.chinamobile.icloud.im.sync.model.RawContact;
import com.chinamobile.icloud.im.vcard.ReadVCardAndAddContacts;
import com.cmicc.module_message.ui.constract.ExchangeVcardContract;
import com.cmcc.cmrcs.android.ui.logic.common.UIObserver;
import com.cmcc.cmrcs.android.ui.logic.common.UIObserverManager;
import com.cmcc.cmrcs.android.ui.utils.LoginUtils;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.fragment.ExchangeVcardFragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by tianshuai on 2017/7/20.
 */

public class ExchangeVcardPresenterImpl implements ExchangeVcardContract.Presenter {

    private String TAG = "ExchangeVcardPresenterImpl" ;

    private Context mContext;
    private ExchangeVcardContract.View mView;

    int type ;
    String fileName ;
    String pcFileType ;
    String pcFileName ;
    long fileLengthe ;
    String sizeDescript ;
    String myCradbody ;
    String applicantCardbody ;

    public ExchangeVcardPresenterImpl(Context context, ExchangeVcardContract.View view) {
        this.mContext = context;
        mView = view;

        ArrayList<Integer> actions = new ArrayList<Integer>();

        actions.add(LogicActions.CARD_FILE_AGREE_EXCHANG_SENG_VIST_INTERFACE_FAIL); // 同意名片交换，发送文件访问接口发送失败
        actions.add(LogicActions.CARD_FILE_AGREE_EXCHANG_SENG_VIST_INTERFACE_OK);
        actions.add(LogicActions.CARD_FILE_AGREE_EXCHANG_SENG_FAIL);
        actions.add(LogicActions.CARD_FILE_AGREE_EXCHG_SENG_OK);

        UIObserverManager.getInstance().registerObserver(mUIObserver, actions);

    }


    private UIObserver mUIObserver = new UIObserver() {
        @Override
        protected void onReceiveAction(int action, Intent intent) {
            if(mView instanceof ExchangeVcardFragment && ((ExchangeVcardFragment)mView).isUiShow()) { // 在栈顶，说明在名片详情页同意的名片交换
                if (action == LogicActions.CARD_FILE_AGREE_EXCHANG_SENG_VIST_INTERFACE_FAIL) { // 同意交换名片发送文件访问接口失败
                    LogF.d(TAG, "名片交换访问接口失败");
                    if (mView instanceof ExchangeVcardFragment) {
                        ((ExchangeVcardFragment) mView).cradAgreeFial(); // 名片同意失败
                    }
                } else if (action == LogicActions.CARD_FILE_AGREE_EXCHANG_SENG_VIST_INTERFACE_OK) { // 同意交换名片发送文件访问接口成功
                    // 把发送的名片的信息先发送到这里，等名片信息发送成功或失败的接口下，再把信息发送到这里来对比。判断是发送失败还是成功
                    LogF.d(TAG, "名片交换访问接口成功");
                    // 同意交换名片访问发送接口成功那里传来
                    type = intent.getIntExtra(LogicActions.MS_ITYPE, -1);
                    fileName = intent.getStringExtra(LogicActions.FILE_NAME);
                    pcFileType = intent.getStringExtra(LogicActions.FILE_TYPE);
                    pcFileName = intent.getStringExtra("pcFileName");
                    fileLengthe = intent.getLongExtra("fileLengthe", 0);
                    sizeDescript = intent.getStringExtra("sizeDescript");
                    myCradbody = intent.getStringExtra("myCradbody");
                    applicantCardbody = intent.getStringExtra("applicantCardbody");
                } else if (action == LogicActions.CARD_FILE_AGREE_EXCHANG_SENG_FAIL) { //同意名片交换发送失败
                    String cradFn = intent.getStringExtra(LogicActions.FILE_NAME);
                    if (!TextUtils.isEmpty(fileName) && fileName.equals(cradFn)) { // 同意交换名片发送失败
                        LogF.d(TAG, "名片交换失败");
                        if (mView instanceof ExchangeVcardFragment) {
                            ((ExchangeVcardFragment) mView).cradAgreeFial(); // 名片同意失败
                        }
                    }
                } else if (action == LogicActions.CARD_FILE_AGREE_EXCHG_SENG_OK) { // 同意名片交换成功
                    String cradFn = intent.getStringExtra(LogicActions.FILE_NAME); // 文件的名字
                    String addess = intent.getStringExtra(LogicActions.MESSAGE_ADRESS); // 接受人的地址
                    String msgID = intent.getStringExtra(LogicActions.IMDN_MESSAG_ID); // 消息id
                    if (!TextUtils.isEmpty(fileName) && fileName.equals(cradFn)) { // 同意交换名片发送成功
                        LogF.d(TAG, "名片交换成功 msgID " + msgID);
                        insertMessage(type, fileName, pcFileType, pcFileName, fileLengthe, sizeDescript, myCradbody, applicantCardbody, addess, msgID);
                        if (mView instanceof ExchangeVcardFragment) {
                            ((ExchangeVcardFragment) mView).updateHint(addess);
                        }
                    }
                }
            }
        }
    };


    @Override
    public void start() {

    }

    @Override
    public void buildEntries(String vcardString) {
        RawContact scanContact = ReadVCardAndAddContacts.createdVcardStringToContact(mContext.getApplicationContext(), vcardString);
        if (scanContact != null) {
            if (TextUtils.isEmpty(scanContact.getStructuredName().getFamilyName())
                    && TextUtils.isEmpty(scanContact.getStructuredName().getGivenName())
                    && !TextUtils.isEmpty(scanContact.getStructuredName().getDisplayName())) {
                int nameLength = scanContact.getStructuredName().getDisplayName().length();
                String dispName = scanContact.getStructuredName().getDisplayName();
                if (nameLength == 1) {
                    scanContact.getStructuredName().setFamilyName(dispName);
                } else if (nameLength > 1) {
                    scanContact.getStructuredName().setGivenName(dispName.substring(1, nameLength));
                    scanContact.getStructuredName().setFamilyName(dispName.substring(0, 1));
                }

            }
            mView.showInfo(scanContact);
        }
    }

    /**
     * 插入消息
     */
    private void insertMessage(int type , String fileName , String pcFileType,String pcFileName ,long fileLengthe,
                               String sizeDescript,String myCradbody,String applicantCardbody ,String addess , String msgID){

        String userPhone = LoginUtils.getInstance().getLoginUserName() ;
        userPhone = NumberUtils.getDialablePhoneWithCountryCode(userPhone);
        long date = System.currentTimeMillis() ;

        //先插入申请方的名片信息
        int idOne = -1 ;
        Message message = new Message();  // 插入一条提示信息，不经过菊粉SDK
        message.setId(idOne);
        message.setType(Type.TYPE_MSG_CARD_RECV);
        message.setBody(applicantCardbody);
        message.setAddress(NumberUtils.getPhone(addess));
        message.setSendAddress(addess);
        message.setDate(date - 400 );
        message.setSeen(true);
        message.setRead(true);
        message.setStatus(Status.STATUS_OK);
        Uri uri = MessageUtils.insertMessage(mContext, message);
        idOne = MessageUtils.getIdFromUri(uri);
        message.setId(idOne);
        MessageUtils.updateMessage(mContext, message);


        //插入自己的名片信息
        int idTwo = -1 ;
        Message messageTo = new Message();
        messageTo.setId(idTwo);
        messageTo.setType(type);
        if(!TextUtils.isEmpty(myCradbody) && myCradbody.contains("AGREE:YES")){
            myCradbody = myCradbody.replace("AGREE:YES" , "");
            // 还原自己的发送的名片个，防止转发同意后自己的名片，接受方判断有AGREE:YES就会走来自同意的逻辑。
            String filePath = FileUtil.createMessageFilePath(fileName, FileUtil.LocalPath.TYPE_FILE);
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(new File(filePath));
                fos.write(myCradbody.getBytes());
                fos.flush();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        messageTo.setBody(myCradbody); // 自己的名片信息
        messageTo.setAddress(NumberUtils.getPhone(addess));
        messageTo.setSendAddress(userPhone );
        messageTo.setExtFileName(fileName);
        messageTo.setExtSizeDescript(sizeDescript);
        messageTo.setExtFilePath(pcFileName);
        messageTo.setExtFileSize(fileLengthe);
        messageTo.setSeen(true);
        messageTo.setRead(true);
        messageTo.setMsgId(msgID);
        messageTo.setStatus(Status.STATUS_OK);
        messageTo.setDate(date);
        Uri uriM = MessageUtils.insertMessage(mContext, messageTo);
        idTwo = MessageUtils.getIdFromUri(uriM);
        messageTo.setId(idTwo);
        MessageUtils.updateMessage(mContext, messageTo);

        // 插入一条提示信息【已与对方交换名片，打个招呼吧】
        int idThr = -1 ;
        Message messageHint = new Message();
        messageHint.setBody(mContext.getResources().getString(R.string.exchange_calling_card_say_hello));
        messageHint.setType(Type.TYPE_MSG_SYSTEM_TEXT);
        messageHint.setAddress(NumberUtils.getPhone(addess));
        messageHint.setSendAddress(userPhone );
        messageHint.setDate(date+400); // 时间
        messageHint.setId(idThr);
        messageHint.setSeen(true);
        messageHint.setRead(true);
        messageHint.setStatus(Status.STATUS_OK);
        Uri uris = MessageUtils.insertMessage(mContext, messageHint);
        idThr = MessageUtils.getIdFromUri(uris);
        messageHint.setId(idThr);
        MessageUtils.updateMessage(mContext, messageHint);
    }

}
