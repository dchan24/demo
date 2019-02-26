package com.cmicc.module_message.ui.view;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.cmicc.module_message.R;

public class SendAudioNotNetDialog extends Dialog {
    private TextView mConfitmBtn;

    public SendAudioNotNetDialog(@NonNull Context context) {
        super(context, com.cmic.module_base.R.style.dialog_style);
        setContentView(R.layout.dialog_send_audio_not_net_layout);
        mConfitmBtn = (TextView) findViewById(R.id.confirm);
        mConfitmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

    }




}
