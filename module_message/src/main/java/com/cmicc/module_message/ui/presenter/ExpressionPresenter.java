package com.cmicc.module_message.ui.presenter;

import android.content.Context;

import com.cmicc.module_message.ui.constract.ExpressionContract;
import com.cmcc.cmrcs.android.widget.emoji.EmojiEntity;
import com.cmcc.cmrcs.android.widget.emoji.ExpressionDatas;

import java.util.Arrays;
import java.util.List;

/**
 * Created by GuoXietao on 2017/5/3.
 */

public class ExpressionPresenter implements ExpressionContract.IPresenter {
    private final Context mContexgt;
    private ExpressionContract.IView mView;
    private List<EmojiEntity> mArrayList;

    public static int EM_SIZE = 21; // 表情数量
    public static int NUMCOLUMNS = 7; // 表情列数
    private int mPageCount;

    public ExpressionPresenter(Context c) {
        mContexgt = c;
    }

    @Override
    public int getPageCount() {
        return mPageCount;
    }

    @Override
    public List<EmojiEntity> getEmojiArray() {
        return mArrayList;
    }

    @Override
    public void setView(ExpressionContract.IView v) {
        mView = v;
    }

    @Override
    public void initData() {
        mArrayList = Arrays.asList(ExpressionDatas.DATA);
        mPageCount = (int) Math.ceil(mArrayList.size() / EM_SIZE);
    }

    @Override
    public void initEmojiBData() {
        mArrayList = Arrays.asList(ExpressionDatas.BDATA);
        mPageCount = (int) Math.ceil(mArrayList.size() / EM_SIZE);
    }

}
