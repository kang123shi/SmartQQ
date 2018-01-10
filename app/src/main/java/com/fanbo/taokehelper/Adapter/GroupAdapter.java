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
import com.scienjus.smartqq.model.Group;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2018/1/7.
 */

public class GroupAdapter extends BaseAdapter {
    private List<Group> groups;
    private Context context;
    private LayoutInflater mInflater;
    private List<Group> chooseGroup ;
    public interface GroupChooseListener{
        void ChooseGroupListener(List<Group> groups);
    }
    public void setGroupChooseListener(GroupChooseListener groupChooseListener) {
        this.groupChooseListener = groupChooseListener;
    }

    public GroupChooseListener groupChooseListener ;
    public GroupAdapter(Context context, List<Group> groups) {
        this.context = context;
        this.groups = groups;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        chooseGroup = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return groups.size();
    }

    @Override
    public Object getItem(int i) {
        return groups.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            view = mInflater.inflate(R.layout.qqitem, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        }else {
            viewHolder =(ViewHolder) view.getTag();
        }
       viewHolder.qqName.setText(groups.get(i).getName());
        viewHolder.cbItem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked==true){
                    chooseGroup.add(groups.get(i));
                }else {
                    for (int j = 0; j < chooseGroup.size(); j++) {
                        if (chooseGroup.get(j).getId()==groups.get(i).getId()){
                            chooseGroup.remove(j);
                        }
                    }
                }
                groupChooseListener.ChooseGroupListener(chooseGroup);
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
