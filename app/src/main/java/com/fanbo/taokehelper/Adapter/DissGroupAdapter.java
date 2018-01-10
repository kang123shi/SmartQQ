package com.fanbo.taokehelper.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.fanbo.taokehelper.R;
import com.scienjus.smartqq.model.Discuss;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2018/1/7.
 */

public class DissGroupAdapter extends BaseAdapter {
    private Context context;
    private List<Discuss> friends;
    private LayoutInflater mInflater;
    private List<Discuss> chooseDiscuss ;
    public interface OnDissGroupListener{
        void DissChooseListener(List<Discuss> discussList);
    }

    public void setOnDissGroupLIstener(OnDissGroupListener onDissGroupLIstener) {
        this.onDissGroupLIstener = onDissGroupLIstener;
    }

    public OnDissGroupListener onDissGroupLIstener ;
    public DissGroupAdapter(Context context, List<Discuss> friends) {
        this.context = context;
        this.friends = friends;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        chooseDiscuss = new ArrayList<>();
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
            holder =new ViewHolder(view);
            view.setTag(holder);
        }else {
            holder =(ViewHolder) view.getTag();
        }
        holder.qqName.setText(friends.get(i).getName());
        holder.cbItem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked==true){
                   chooseDiscuss.add(friends.get(i));
                }else {
                    for (int j = 0; j < chooseDiscuss.size(); j++) {
                        if (chooseDiscuss.get(j).getId()==friends.get(i).getId()){
                            chooseDiscuss.remove(j);
                        }
                    }
                }
                onDissGroupLIstener.DissChooseListener(chooseDiscuss);
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
