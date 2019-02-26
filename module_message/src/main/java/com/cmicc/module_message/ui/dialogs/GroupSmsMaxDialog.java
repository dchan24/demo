package com.cmicc.module_message.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.cmicc.module_message.R;
import com.lzy.okgo.db.TableEntity;

/**
 * Created by LY on 2018/6/25.
 */

public class GroupSmsMaxDialog extends Dialog {

    private TextView stipText ;
    private TextView iKoneText ;

    public GroupSmsMaxDialog(@NonNull Context context) {
        super(context , R.style.login_dialog_style);
        setContentView(R.layout.dialog_groupsms_maxnumber_layout);
        stipText = (TextView) findViewById(R.id.stip_text);
        iKoneText = (TextView) findViewById(R.id.i_know_text);
    }

    public GroupSmsMaxDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected GroupSmsMaxDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }


    public void setStipText(String stipString){
        stipText.setText(stipString);
    }


    public TextView getStipText() {
        return stipText;
    }

    public void setStipText(TextView stipText) {
        this.stipText = stipText;
    }

    public TextView getiKoneText() {
        return iKoneText;
    }

    public void setiKoneText(TextView iKoneText) {
        this.iKoneText = iKoneText;
    }
}
