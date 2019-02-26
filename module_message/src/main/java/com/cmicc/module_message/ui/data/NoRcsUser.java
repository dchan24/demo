package com.cmicc.module_message.ui.data;

import com.chinamobile.app.yuliao_business.model.PublcformConversation;

/**
 * Created by LY on 2018/5/22.
 */

public class NoRcsUser {

    private String phone ;
    private boolean isBeChosen ;

    public NoRcsUser(){

    }

    public NoRcsUser(String phone , boolean  isBeChosen ){
        this.phone = phone;
        this.isBeChosen = isBeChosen ;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isBeChosen() {
        return isBeChosen;
    }

    public void setBeChosen(boolean beChosen) {
        isBeChosen = beChosen;
    }
}
