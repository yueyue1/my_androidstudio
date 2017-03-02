package com.example.biu.bluelock;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BIU on 2016/7/19.
 */
public class FrdFregment extends Fragment {
    private ExpandableListView listView;
    private List<String> Group;
    private List<List<String>> Child;
    private List<String> ChildFirst;
    private List<String> ChildSecond;
    private List<String> ChildThird;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tabo2, container, false);
        listView = (ExpandableListView) v.findViewById(R.id.tab02_Expandlvi);
        Group = new ArrayList<String>();
        Group.add("我的设备");
        Group.add("我的好友");
        Group.add("我的白名单");
        ChildFirst = new ArrayList<String>();
        ChildFirst.add("我的电脑");
        ChildFirst.add("我的手机");
        ChildFirst.add("我的打印机");

        ChildSecond= new ArrayList<String>();
        ChildSecond.add("伊布");
        ChildSecond.add("皮卡丘");
        ChildSecond.add("喵喵");

        ChildThird= new ArrayList<String>();
        ChildThird.add("我的伊布");
        ChildThird.add("我的皮卡丘");
        ChildThird.add("我的喵喵");

        Child = new ArrayList<List<String>>();
        Child.add(ChildFirst);
        Child.add(ChildSecond);
        Child.add(ChildThird);

        listView.setAdapter( new MyExpandableAdaper(getActivity()));
        return v;

    }
    class  MyExpandableAdaper extends BaseExpandableListAdapter{
        private Context context;

        public MyExpandableAdaper(Context context) {
            this.context = context;
        }
        @Override
        public int getGroupCount() {
            return Group.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return Child.get(groupPosition).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return Group.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return Child.get(groupPosition).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            GroupHolder groupHolder = null;
            if (convertView == null){convertView = (View) getActivity().getLayoutInflater().from(context).inflate(
                    R.layout.tab02_group, null);      //把界面放到缓冲区
                groupHolder = new GroupHolder();          //实例化我们创建的这个类
                groupHolder.txt = (TextView) convertView.findViewById(R.id.notice_item_date);  //实例化类里的TextView
                convertView.setTag(groupHolder);                                 //给view对象一个标签，告诉计算机我们已经在缓冲区里放了一个view，下回直                                                                               //接来拿就行了
            } else {
                groupHolder = (GroupHolder) convertView.getTag();     //然后他就直接来拿
            }
            groupHolder.txt.setText(Group.get(groupPosition));//最后在相应的group里设置他相应的Text
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ItemHolder itemHolder = null;
            if (convertView == null) {
                convertView = (View) getActivity().getLayoutInflater().from(context).inflate(
                        R.layout.tab02_child, null);
                itemHolder = new ItemHolder();
                itemHolder.txt = (TextView) convertView.findViewById(R.id.group);
                convertView.setTag(itemHolder);
            } else {
                itemHolder = (ItemHolder) convertView.getTag();
            }
            itemHolder.txt.setText(Child.get(groupPosition).get(
                    childPosition));
            return convertView;
        }


        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
    class GroupHolder {
        public TextView txt;
        public ImageView img;
    }

    class ItemHolder {
        public ImageView img;
        public TextView txt;
    }


}
