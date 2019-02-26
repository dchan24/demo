package com.cmicc.module_message.ui.adapter.message;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.module.WebConfig;
import com.app.module.proxys.moduleenterprise.EnterPriseProxy;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.chinamobile.app.yuliao_business.model.Message;
import com.chinamobile.app.yuliao_common.application.App;
import com.cmcc.cmrcs.android.glide.GlideApp;
import com.cmicc.module_message.ui.adapter.MessageChatListAdapter;
import com.cmicc.module_message.ui.constract.BaseChatContract;
import com.cmicc.module_message.R;

import static android.support.v7.widget.RecyclerView.NO_POSITION;

public class MultiPicTextHolder extends BaseViewHolder {

	public FrameLayout fl_complex_main;
	private View ll_complex_1;
    private View ll_complex_2;
    private View ll_complex_3;
    private View ll_complex_4;
    private View ll_complex_5;
    private View ll_complex_6;
    private View ll_complex_7;
    private View ll_complex_8;
    private TextView tv_complex_main;
    private TextView tv_complex_1;
    private TextView tv_complex_2;
    private TextView tv_complex_3;
    private TextView tv_complex_4;
    private TextView tv_complex_5;
    private TextView tv_complex_6;
    private TextView tv_complex_7;
    private TextView tv_complex_8;
    private ImageView iv_complex_main;
    private ImageView iv_complex_1;
    private ImageView iv_complex_2;
    private ImageView iv_complex_3;
    private ImageView iv_complex_4;
    private ImageView iv_complex_5;
    private ImageView iv_complex_6;
    private ImageView iv_complex_7;
    private ImageView iv_complex_8;

	public MultiPicTextHolder(View itemView, Activity activity , MessageChatListAdapter adapter , BaseChatContract.Presenter presenter) {
		super(itemView ,activity ,adapter ,presenter);
		fl_complex_main = itemView.findViewById(R.id.fl_complex_main);
		ll_complex_1 = itemView.findViewById(R.id.pp_complex_1);
		ll_complex_2 = itemView.findViewById(R.id.pp_complex_2);
		ll_complex_3 = itemView.findViewById(R.id.pp_complex_3);
		ll_complex_4 = itemView.findViewById(R.id.pp_complex_4);
		ll_complex_5 = itemView.findViewById(R.id.pp_complex_5);
		ll_complex_6 = itemView.findViewById(R.id.pp_complex_6);
		ll_complex_7 = itemView.findViewById(R.id.pp_complex_7);
		ll_complex_8 = itemView.findViewById(R.id.pp_complex_8);
		tv_complex_main = itemView.findViewById(R.id.pp_tv_text_main);
		tv_complex_1 = itemView.findViewById(R.id.pp_complex_tv_1);
		tv_complex_2 = itemView.findViewById(R.id.pp_complex_tv_2);
		tv_complex_3 = itemView.findViewById(R.id.pp_complex_tv_3);
		tv_complex_4 = itemView.findViewById(R.id.pp_complex_tv_4);
		tv_complex_5 = itemView.findViewById(R.id.pp_complex_tv_5);
		tv_complex_6 = itemView.findViewById(R.id.pp_complex_tv_6);
		tv_complex_7 = itemView.findViewById(R.id.pp_complex_tv_7);
		tv_complex_8 = itemView.findViewById(R.id.pp_complex_tv_8);
		iv_complex_main = itemView.findViewById(R.id.pp_iv_image_main);
		iv_complex_1 = itemView.findViewById(R.id.pp_complex_iv_1);
		iv_complex_2 = itemView.findViewById(R.id.pp_complex_iv_2);
		iv_complex_3 = itemView.findViewById(R.id.pp_complex_iv_3);
		iv_complex_4 = itemView.findViewById(R.id.pp_complex_iv_4);
		iv_complex_5 = itemView.findViewById(R.id.pp_complex_iv_5);
		iv_complex_6 = itemView.findViewById(R.id.pp_complex_iv_6);
		iv_complex_7 = itemView.findViewById(R.id.pp_complex_iv_7);
		iv_complex_8 = itemView.findViewById(R.id.pp_complex_iv_8);

		OnPicTextClickListener onPicTextClickListener = new OnPicTextClickListener();
		fl_complex_main.setOnClickListener(onPicTextClickListener);
		ll_complex_1.setOnClickListener(onPicTextClickListener);
		ll_complex_2.setOnClickListener(onPicTextClickListener);
		ll_complex_3.setOnClickListener(onPicTextClickListener);
		ll_complex_4.setOnClickListener(onPicTextClickListener);
		ll_complex_5.setOnClickListener(onPicTextClickListener);
		ll_complex_6.setOnClickListener(onPicTextClickListener);
		ll_complex_7.setOnClickListener(onPicTextClickListener);
		ll_complex_8.setOnClickListener(onPicTextClickListener);

		fl_complex_main.setOnLongClickListener(new OnMsgContentLongClickListener());
		ll_complex_1.setOnLongClickListener(new OnMsgContentLongClickListener());
		ll_complex_2.setOnLongClickListener(new OnMsgContentLongClickListener());
		ll_complex_3.setOnLongClickListener(new OnMsgContentLongClickListener());
		ll_complex_4.setOnLongClickListener(new OnMsgContentLongClickListener());
		ll_complex_5.setOnLongClickListener(new OnMsgContentLongClickListener());
		ll_complex_6.setOnLongClickListener(new OnMsgContentLongClickListener());
		ll_complex_7.setOnLongClickListener(new OnMsgContentLongClickListener());
		ll_complex_8.setOnLongClickListener(new OnMsgContentLongClickListener());

		itemView.setOnLongClickListener(null);
		itemView.setOnClickListener(null);
	}

    public void bindMultiPicText(Message msg) {
        String mTitles = msg.getSubTitle();
        String mPaths = msg.getSubImgPath();
        String mUrls = msg.getSubUrl();
        String[] mTitleList = null;
        String[] mPathList = null;
        String[] mUrlList = null;
        int count = -1;
        if (mTitles != null) {
            if (mTitles.contains("|||")) {
                mTitleList = mTitles.split("\\|\\|\\|");
            } else {
                mTitleList = mTitles.split(",");
            }
        }
        if (mPaths != null) {
            mPathList = mPaths.split(",");
        }
        if (mUrls != null) {
            mUrlList = mUrls.split(",");
        }
        if (mPathList == null || mTitleList == null || mUrlList == null || mPathList.length == 0 || mTitleList.length == 0 || mUrlList.length == 0) {
            return;
        }
        RequestOptions options = new RequestOptions().error(R.drawable.pf_composing_small)
                .diskCacheStrategy(DiskCacheStrategy.DATA).dontAnimate().dontTransform();

        int min = Math.min(mUrlList.length, mTitleList.length);
        count = Math.min(min, mPathList.length) - 1;
        ll_complex_8.setVisibility(View.GONE);
        ll_complex_7.setVisibility(View.GONE);
        ll_complex_6.setVisibility(View.GONE);
        ll_complex_5.setVisibility(View.GONE);
        ll_complex_4.setVisibility(View.GONE);
        ll_complex_3.setVisibility(View.GONE);
        ll_complex_2.setVisibility(View.GONE);
        ll_complex_1.setVisibility(View.GONE);
        fl_complex_main.setVisibility(View.GONE);
        switch (count) {
            case 8:
                String subImgPath8 = mPathList[8];
                ll_complex_8.setVisibility(View.VISIBLE);
                ll_complex_8.setTag(R.id.pp_complex_8, mUrlList[8]);
                ll_complex_8.setTag(R.id.pp_complex_iv_8, subImgPath8);
                tv_complex_8.setTag(R.id.pp_complex_tv_8, mTitleList[8]);
                    GlideApp.with(App.getAppContext())
                            .load(subImgPath8)
                            .apply(options)
                            .into(iv_complex_8);
                tv_complex_8.setText(mTitleList[8]);
            case 7:
                String subImgPath7 = mPathList[7];
                ll_complex_7.setVisibility(View.VISIBLE);
                ll_complex_7.setTag(R.id.pp_complex_7, mUrlList[7]);
                ll_complex_7.setTag(R.id.pp_complex_iv_7, subImgPath7);
                tv_complex_7.setTag(R.id.pp_complex_tv_7, mTitleList[7]);
                ll_complex_7.setTag(this);
                GlideApp.with(App.getAppContext())
                        .load(subImgPath7)
                        .apply(options)
                        .into(iv_complex_7);
                tv_complex_7.setText(mTitleList[7]);
            case 6:
                String subImgPath6 = mPathList[6];
                ll_complex_6.setVisibility(View.VISIBLE);
                ll_complex_6.setTag(R.id.pp_complex_6, mUrlList[6]);
                ll_complex_6.setTag(R.id.pp_complex_iv_6, subImgPath6);
                tv_complex_6.setTag(R.id.pp_complex_tv_6, mTitleList[6]);
                ll_complex_6.setTag(this);
                GlideApp.with(App.getAppContext())
                        .load(subImgPath6)
                        .apply(options)
                        .into(iv_complex_6);
                tv_complex_6.setText(mTitleList[6]);
            case 5:
                String subImgPath5 = mPathList[5];
                ll_complex_5.setVisibility(View.VISIBLE);
                ll_complex_5.setTag(R.id.pp_complex_5, mUrlList[5]);
                ll_complex_5.setTag(R.id.pp_complex_iv_5, subImgPath5);
                tv_complex_5.setTag(R.id.pp_complex_tv_5, mTitleList[5]);
                ll_complex_5.setTag(this);
                GlideApp.with(App.getAppContext())
                        .load(subImgPath5)
                        .apply(options)
                        .into(iv_complex_5);
                tv_complex_5.setText(mTitleList[5]);
            case 4:
                String subImgPath4 = mPathList[4];
                ll_complex_4.setVisibility(View.VISIBLE);
                ll_complex_4.setTag(R.id.pp_complex_4, mUrlList[4]);
                ll_complex_4.setTag(R.id.pp_complex_iv_4, subImgPath4);
                ll_complex_4.setTag(this);
                GlideApp.with(App.getAppContext())
                        .load(subImgPath4)
                        .apply(options)
                        .into(iv_complex_4);
                tv_complex_4.setText(mTitleList[4]);
                tv_complex_4.setTag(R.id.pp_complex_tv_4, mTitleList[4]);
            case 3:
                String subImgPath3 = mPathList[3];
                ll_complex_3.setVisibility(View.VISIBLE);
                ll_complex_3.setTag(R.id.pp_complex_3, mUrlList[3]);
                ll_complex_3.setTag(R.id.pp_complex_iv_3, subImgPath3);
                ll_complex_3.setTag(this);
                GlideApp.with(App.getAppContext())
                        .load(subImgPath3)
                        .apply(options)
                        .into(iv_complex_3);
                tv_complex_3.setText(mTitleList[3]);
                tv_complex_3.setTag(R.id.pp_complex_tv_3, mTitleList[3]);
            case 2:
                String subImgPath2 = mPathList[2];
                ll_complex_2.setVisibility(View.VISIBLE);
                ll_complex_2.setTag(R.id.pp_complex_2, mUrlList[2]);
                ll_complex_2.setTag(R.id.pp_complex_iv_2, subImgPath2);
                ll_complex_2.setTag(this);
                GlideApp.with(App.getAppContext())
                        .load(subImgPath2)
                        .apply(options)
                        .into(iv_complex_2);
                tv_complex_2.setText(mTitleList[2]);
                tv_complex_2.setTag(R.id.pp_complex_tv_2, mTitleList[2]);
            case 1:
                String subImgPath1 = mPathList[1];
                ll_complex_1.setVisibility(View.VISIBLE);
                ll_complex_1.setTag(R.id.pp_complex_1, mUrlList[1]);
                ll_complex_1.setTag(R.id.pp_complex_iv_1, subImgPath1);
                ll_complex_1.setTag(this);
                GlideApp.with(App.getAppContext())
                        .load(subImgPath1)
                        .apply(options)
                        .into(iv_complex_1);                
                tv_complex_1.setText(mTitleList[1]);
                tv_complex_1.setTag(R.id.pp_complex_tv_1, mTitleList[1]);
            case 0:
                String subImgPath0 = mPathList[0];
                fl_complex_main.setVisibility(View.VISIBLE);
                fl_complex_main.setTag(R.id.fl_complex_main, mUrlList[0]);
                fl_complex_main.setTag(R.id.pp_iv_image_main, subImgPath0);
                fl_complex_main.setTag(this);
                GlideApp.with(App.getAppContext())
                        .load(subImgPath0)
                        .apply(options)
                        .into(iv_complex_main);                
                tv_complex_main.setText(mTitleList[0]);
                tv_complex_main.setTag(R.id.pp_tv_text_main, mTitleList[0]);
        }
    }

    public class OnPicTextClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position == NO_POSITION) {
                return;
            }
            Message msg = adapter.getItem(adapter.canLoadMore() ? position - 1 : position);
            MultiPicTextHolder holder = (MultiPicTextHolder) v.getTag();
            String url;
            String image_path;
            String author;
            int i = v.getId();
            if (i == R.id.fl_complex_main) {
                try {
                    url = holder.fl_complex_main.getTag(R.id.fl_complex_main).toString();
                    image_path = holder.fl_complex_main.getTag(R.id.pp_iv_image_main).toString();
                    String mAuthors = msg.getAuthor();
                    author = "";
                    if (!TextUtils.isEmpty(mAuthors)) {
                        author = mAuthors.split(",")[0];
                    }
                    startWebByUrl(url, author, image_path, author, msg, msg.getMsgId() + "0");
                } catch (Exception e) {
                    // TODO: handle exception
                }
            } else if (i == R.id.pp_complex_1) {
                try {
                    url = holder.ll_complex_1.getTag(R.id.pp_complex_1).toString();
                    image_path = holder.ll_complex_1.getTag(R.id.pp_complex_iv_1).toString();
                    String mAuthors = msg.getAuthor();
                    author = "";
                    if (!TextUtils.isEmpty(mAuthors)) {
                        author = mAuthors.split(",")[1];
                    }
                    startWebByUrl(url, author, image_path, author, msg, msg.getMsgId() + "1");
                } catch (Exception e) {
                    // TODO: handle exception
                }
            } else if (i == R.id.pp_complex_2) {
                try {
                    url = holder.ll_complex_2.getTag(R.id.pp_complex_2).toString();
                    image_path = holder.ll_complex_2.getTag(R.id.pp_complex_iv_2).toString();
                    String mAuthors = msg.getAuthor();
                    author = "";
                    if (!TextUtils.isEmpty(mAuthors)) {
                        author = mAuthors.split(",")[2];
                    }
                    startWebByUrl(url, author, image_path, author, msg, msg.getMsgId() + "2");
                } catch (Exception e) {
                    // TODO: handle exception
                }
            } else if (i == R.id.pp_complex_3) {
                try {
                    url = holder.ll_complex_3.getTag(R.id.pp_complex_3).toString();
                    image_path = holder.ll_complex_3.getTag(R.id.pp_complex_iv_3).toString();
                    String mAuthors = msg.getAuthor();
                    author = "";
                    if (!TextUtils.isEmpty(mAuthors)) {
                        author = mAuthors.split(",")[3];
                    }
                    startWebByUrl(url, author, image_path, author, msg, msg.getMsgId() + "3");
                } catch (Exception e) {
                    // TODO: handle exception
                }
            } else if (i == R.id.pp_complex_4) {
                try {
                    url = holder.ll_complex_4.getTag(R.id.pp_complex_4).toString();
                    image_path = holder.ll_complex_4.getTag(R.id.pp_complex_iv_4).toString();
                    String mAuthors = msg.getAuthor();
                    author = "";
                    if (!TextUtils.isEmpty(mAuthors)) {
                        author = mAuthors.split(",")[4];
                    }
                    startWebByUrl(url, author, image_path, author, msg, msg.getMsgId() + "4");
                } catch (Exception e) {
                    // TODO: handle exception
                }
            } else if (i == R.id.pp_complex_5) {
                try {
                    url = holder.ll_complex_5.getTag(R.id.pp_complex_5).toString();
                    image_path = holder.ll_complex_5.getTag(R.id.pp_complex_iv_5).toString();
                    String mAuthors = msg.getAuthor();
                    author = "";
                    if (!TextUtils.isEmpty(mAuthors)) {
                        author = mAuthors.split(",")[5];
                    }
                    startWebByUrl(url, author, image_path, author, msg, msg.getMsgId() + "5");
                } catch (Exception e) {
                    // TODO: handle exception
                }
            } else if (i == R.id.pp_complex_6) {
                try {
                    url = holder.ll_complex_6.getTag(R.id.pp_complex_6).toString();
                    image_path = holder.ll_complex_6.getTag(R.id.pp_complex_iv_6).toString();
                    String mAuthors = msg.getAuthor();
                    author = "";
                    if (!TextUtils.isEmpty(mAuthors)) {
                        author = mAuthors.split(",")[6];
                    }
                    startWebByUrl(url, author, image_path, author, msg, msg.getMsgId() + "6");
                } catch (Exception e) {
                    // TODO: handle exception
                }
            } else if (i == R.id.pp_complex_7) {
                try {
                    url = holder.ll_complex_7.getTag(R.id.pp_complex_7).toString();
                    image_path = holder.ll_complex_7.getTag(R.id.pp_complex_iv_7).toString();
                    String mAuthors = msg.getAuthor();
                    author = "";
                    if (!TextUtils.isEmpty(mAuthors)) {
                        author = mAuthors.split(",")[7];
                    }
                    startWebByUrl(url, author, image_path, author, msg, msg.getMsgId() + "7");
                } catch (Exception e) {
                    // TODO: handle exception
                }
            } else if (i == R.id.pp_complex_8) {
                try {
                    url = holder.ll_complex_8.getTag(R.id.pp_complex_8).toString();
                    image_path = holder.ll_complex_8.getTag(R.id.pp_complex_iv_8).toString();
                    String mAuthors = msg.getAuthor();
                    author = "";
                    if (!TextUtils.isEmpty(mAuthors)) {
                        author = mAuthors.split(",")[8];
                    }
                    startWebByUrl(url, author, image_path, author, msg, msg.getMsgId() + "8");
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
        }
    }

    // 跳转url
    public void startWebByUrl(String url, String name, String imagePath, String author, Message message, String msgId) {
        if (TextUtils.isEmpty(url) || "null".equals(url)) {
            return;
        }
        WebConfig webConfig = new WebConfig.Builder()
                .enableRequestToken(false)
                .enableShare()
                .title(name)
                .build(url);
        EnterPriseProxy.g.getUiInterface().jumpToBrowser(mContext, webConfig);
    }
}
