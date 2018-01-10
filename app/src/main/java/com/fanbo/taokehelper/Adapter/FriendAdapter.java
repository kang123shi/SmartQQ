package com.fanbo.taokehelper.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.fanbo.taokehelper.R;
import com.scienjus.smartqq.model.Friend;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2018/1/7.
 */

public class FriendAdapter extends BaseAdapter {
    private Context context;
    private List<Friend> friends;
    private LayoutInflater mInflater;
    private List<Friend> beChooseFriendList ;
    public interface OnChooseListener{
        void CBChooseListener(List<Friend> friendList);
    }

    public void setOnChooseListener(OnChooseListener onChooseListener) {
        this.onChooseListener = onChooseListener;
    }

    public OnChooseListener onChooseListener ;
    public FriendAdapter(Context context, List<Friend> friends) {
        this.context = context;
        this.friends = friends;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        beChooseFriendList =new ArrayList<>();
    }

    @Override
    public int getCount() {
        return friends.size();
    }

    @Override
    public Object getItem(int i) {
        return friends.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder holder ;
        if (view == null) {
            view = mInflater.inflate(R.layout.qqitem, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }else {
            holder = (ViewHolder) view.getTag();
        }
        holder.qqName.setText(friends.get(i).getNickname());
        holder.cbItem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                  if (isChecked==true){
                      beChooseFriendList.add(friends.get(i));
                  }else {
                      for (int j = 0; j <beChooseFriendList.size() ; j++) {
                          if (beChooseFriendList.get(j).getUserId()==friends.get(i).getUserId()){
                              beChooseFriendList.remove(j);
                          }
                      }
                  }
                  onChooseListener.CBChooseListener(beChooseFriendList);
            }
        });

        return view;
    }

    static class ViewHolder {
        @BindView(R.id.qq_name)
        TextView qqName;
        @BindView(R.id.cb_item)
        CheckBox cbItem;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
