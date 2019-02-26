package com.cmicc.module_message.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chinamobile.app.yuliao_business.model.GroupMember;
import com.chinamobile.app.yuliao_business.util.Type;
import com.chinamobile.app.utils.RxAsyncHelper;
import com.chinamobile.app.yuliao_common.utils.LogF;
import com.cmcc.cmrcs.android.glide.GlidePhotoLoader;
import com.chinamobile.app.yuliao_common.utils.PinyinUtils;
import com.cmcc.cmrcs.android.rx.ConvListCursorOnSubscribe;
import com.cmcc.cmrcs.android.ui.adapter.BaseCustomCursorAdapter;
import com.cmicc.module_message.ui.adapter.GroupMemberListAdapter.GMListViewHolder;
import com.cmcc.cmrcs.android.ui.utils.NickNameUtils;
import com.cmicc.module_message.R;
import com.cmicc.module_message.ui.constract.GroupMemberListContract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import rx.functions.Func1;

/**
 * Created by Dchan on 2017/5/18.
 */

public class GroupMemberListAdapter extends BaseCustomCursorAdapter<GMListViewHolder,GroupMember> {
	private static final String TAG = GroupMemberListAdapter.class.getSimpleName();
	private Context mContext;
	AdapterDataChangeListener mListener;
	GroupMemberListContract.Presenter mPresenter;
	ArrayList<String> mListForDelete = null;//用于批量删除的数据源
	boolean isClose =false;
	public GroupMemberListAdapter(Context context , GroupMemberListContract.Presenter presenter) {
		super(GroupMember.class);
		mContext = context;
		mPresenter =presenter;
		mListForDelete = new ArrayList<>();
		RxAsyncHelper rxAsyncHelper = new RxAsyncHelper(ConvListCursorOnSubscribe.createCursorObservable(this));
		rxAsyncHelper.debound(200).runInThread(new Func1<Cursor ,List<GroupMember>>() {
			@Override
			public List<GroupMember> call(Cursor o) {
				LogF.e(TAG ,"change cursor:"+o+",Thread:"+Thread.currentThread().getName());
				return changeCursorToDataInThread();
			}
		}).runOnMainThread(new Func1<List<GroupMember> ,String>() {
			@Override
			public String call(List<GroupMember> list) {
				LogF.e(TAG ,"change cursor: main thread:"+Thread.currentThread().getName());
				if(isClose){
					return null;
				}
				mDataList.clear();
				mDataList.addAll(list);
				onDataSetChanged();
				notifyDataSetChanged();
				if(mListener != null){
					mListener.onDataChange();
				}
				return null;
			}
		}).subscribe();
	}

	/**
	 * 获取群成员的名字和号码，用逗号分开
	 * @return
	 */
	public ArrayList<String> getAllMemberInString(){
		return mListForDelete;
	}

	public void setClose(boolean isClose){
		this.isClose = isClose;
	}

	private List<GroupMember> changeCursorToDataInThread() {
		if(mCursor == null){
			LogF.d(TAG ,"cursor is null");
			return null;
		}
		mListForDelete.clear();
		long begin = System.currentTimeMillis();
		LogF.d(TAG ,"cursor is ok");
		List<GroupMember> dataList = new ArrayList<GroupMember>();
		if (!mCursor.isClosed() && mCursor.moveToFirst()) {
			do {
				GroupMember member = getValueFromCursor(mCursor);
				if(member == null){
					continue;
				}
				String person = NickNameUtils.getNickName(mContext ,member.getAddress() ,member.getGroupId());
				member.setPerson(person);
				if(member.getType() == Type.TYPE_LEVEL_ORDER){
					mPresenter.setGroupOwner(member);
				}else{
					StringBuilder sb = new StringBuilder(member.getAddress());
					sb.append(",").append(member.getPerson()).append(",").append(member.getId());
					mListForDelete.add(sb.toString());
				}
				if(person.length() > 0){
					String pinyin = PinyinUtils.getInstance(mContext).getPinyin(person);
					String pinyin2 = PinyinUtils.getInstance(mContext).getPinyinsString(person);
					if ( !"".equals(pinyin) ) {
						member.setPinyin(pinyin);
						member.setPinyin2(pinyin2);
						String sortString = pinyin.substring(0, 1).toUpperCase();
						if (sortString.matches("[A-Z]")) {
							member.setLetter(sortString);
						} else {
							member.setLetter("#");
						}
					}
				}
				dataList.add(member);
			}while (!mCursor.isClosed() &&mCursor.moveToNext());
		}
		if(dataList.size() > 1){
			Collections.sort(dataList, pinyinComparator);
		}
		LogF.d(TAG ,"DATA LIST SIZE:"+mDataList.size()+",time:"+(System.currentTimeMillis()-begin)+",begin:"+begin);
		return dataList;
	}

	@Override
	public GMListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new GMListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_member_list_layout,null));
	}

	@Override
	public void onBindViewHolder(GMListViewHolder holder, int position) {
		super.onBindViewHolder(holder, position);
		LogF.d(TAG ,"onBindViewHolder:"+position);
		GroupMember groupMember = getItem(position);
		holder.position = position;
//		holder.mMemberName.setText(NickNameUtils.getNickName(mContext ,groupMember.getAddress() ,groupMember.getGroupId()));
		holder.mMemberName.setText(groupMember.getPerson());
		holder.mAlphabetIndexTextView.setText(groupMember.getLetter());
		if(position > 0){
			if(getItem(position-1).getLetter().equals(getItem(position).getLetter())){
				holder.mAlphabetIndexTextView.setVisibility(View.GONE);
			}else{
				holder.mAlphabetIndexTextView.setVisibility(View.VISIBLE);
			}
		}else{
			holder.mAlphabetIndexTextView.setVisibility(View.VISIBLE);
		}
		GlidePhotoLoader.getInstance(mContext).loadPhoto(mContext ,holder.mcontactImage ,groupMember.getAddress());
	}

	@Override
	public void onDataSetChanged() {

	}

	@Override
	public synchronized void setCursor(Cursor cursor) {
		mCursor = cursor;
		if(mOnCursorChangeListener != null){
			LogF.e(TAG ,"change cursor:!!!!!!!!!");
			mOnCursorChangeListener.onCursorChange(mCursor);
		}
	}

	public void setOnAdapterDataChangeListener(AdapterDataChangeListener listener){
		mListener = listener;
	}

	public List<GroupMember> getData() {
		return mDataList;
	}

	public void sortData(){
		if(mDataList.size() > 1){
			Collections.sort(mDataList, pinyinComparator);
		}
	}

	public interface AdapterDataChangeListener{
		public void onDataChange();
	}

	class GMListViewHolder extends  RecyclerView.ViewHolder implements OnClickListener,OnLongClickListener{
		TextView mMemberName;
		TextView mAlphabetIndexTextView;
//		View mLayoutContactIndex;//字母索引的外层layout
		ImageView mcontactImage; //头像
		int position;
		public GMListViewHolder(View itemView) {
			super(itemView);
			itemView.setOnClickListener(this);
			itemView.setOnLongClickListener(this);
			mMemberName = (TextView) itemView.findViewById(R.id.tv_name);
			mAlphabetIndexTextView = (TextView) itemView.findViewById(R.id.tv_alphabet);
//			mLayoutContactIndex = itemView.findViewById(R.id.layout_contact_index);
			mcontactImage = (ImageView) itemView.findViewById(R.id.iv_head);
		}

		@Override
		public void onClick(View v) {
			mPresenter.clickItem(getItem(position));
		}

		@Override
		public boolean onLongClick(View v) {
			mPresenter.longClickItem(getItem(position));
			return false;
		}
	}

	GroupInfoPinyinComparator pinyinComparator = new GroupInfoPinyinComparator();
	class GroupInfoPinyinComparator implements Comparator<GroupMember> {

		public int compare(GroupMember o1, GroupMember o2) {
			if (o1.getLetter().equals("@") || o2.getLetter().equals("#")) {
				return -1;
			} else if (o1.getLetter().equals("#")
					|| o2.getLetter().equals("@")) {
				return 1;
			} else {
				return o1.getLetter().compareTo(o2.getLetter());
			}
		}

	}
}
