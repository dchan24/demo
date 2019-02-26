package com.cmicc.module_message.ui.model.impls;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.chinamobile.app.yuliao_business.provider.Conversations.GroupInfo;

/**
 * Created by Dchan on 2017/5/25.
 * 加载群人数
 */

public class GroupChatInfoModel implements  LoaderManager.LoaderCallbacks<Cursor>{
	private static int ID = 0;
	private LoaderManager mLoaderManager;
	private String mAddress;
	private Context mContext;
	GroupChatInfoListener mListener;
	public GroupChatInfoModel(int id ,Context context ,LoaderManager loaderManager , String address,GroupChatInfoListener listener) {
		this.ID = id;
		this.mLoaderManager = loaderManager;
		this.mAddress = address;
		mContext = context;
		mListener = listener;
		loaderManager.initLoader(ID, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String whrere = "address = ? ";
		CursorLoader loader = new CursorLoader(mContext , GroupInfo.CONTENT_URI ,new String[]{"member_count", "person"},whrere ,new String[]{mAddress},null);
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if(loader.getId() != ID){
			return;
		}
		if(mListener != null){
			mListener.onLoadFinished(data);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mLoaderManager.restartLoader(ID, null, this);
	}

	public interface GroupChatInfoListener{
		public void onLoadFinished(Cursor cursor);
	}
}
