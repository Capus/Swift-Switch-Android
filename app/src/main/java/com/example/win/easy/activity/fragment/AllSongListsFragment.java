package com.example.win.easy.activity.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.example.win.easy.R;
import com.example.win.easy.repository.db.pojo.SongListPojo;
import com.example.win.easy.repository.db.pojo.SongXSongList;
import com.example.win.easy.viewmodel.SimpleViewModel;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于展示所有歌单的Fragment
 */
public class AllSongListsFragment extends ListFragment {

    private SimpleViewModel viewModel;
    private LiveData<List<SongListPojo>> allSongLists;
    private LiveData<List<SongXSongList>> allRelation;
    private QMUIGroupListView.Section section;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View thisView=super.onCreateView(inflater,container,savedInstanceState);
        //设置标题
        setTopBarTitle("所有歌单");
        //右上角按钮用于创建新的歌单
        setRightImageButtonOnClickListener(v->{
            //TODO 点击按钮创建歌单
            Toast.makeText(getContext(),"待实现：点击添加歌单",Toast.LENGTH_SHORT).show();
        });
        return thisView;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //注册数据监听
        viewModel= ViewModelProviders.of(this).get(SimpleViewModel.class);
        allSongLists=viewModel.getAllSongLists();
        allRelation=viewModel.getAllRelation();
        allRelation.observe(this,songXSongLists->update(allSongLists.getValue()==null?new ArrayList<>():allSongLists.getValue(),songXSongLists));
        allSongLists.observe(this,songListPojos -> update(songListPojos,allRelation.getValue()==null?new ArrayList<>():allRelation.getValue()));
    }

    /**
     * 根据最新的歌单及关系数据刷新视图
     * @param songListPojos 最新的歌单数据
     * @param allRelation 最新的关系数据
     */
    public void update(List<SongListPojo> songListPojos,List<SongXSongList> allRelation){
        //每次刷新时都重新创建section
        if (section!=null)
            section.removeFrom(groupListView);
        section=QMUIGroupListView.newSection(getContext());
        //对每个歌单都生成一个itemView
        for (SongListPojo songListPojo:songListPojos) {
            QMUICommonListItemView itemView=groupListView.createItemView(LinearLayout.VERTICAL);
            //显示歌单名字
            itemView.setText(songListPojo.name);
            //显示歌单默认头像，如果后续发现有下载好的头像，则替换
            itemView.setImageDrawable(getResources().getDrawable(R.drawable.ase16));
            //item右侧显示歌单内歌曲数量
            itemView.setDetailText(String.valueOf(sizeOf(songListPojo,allRelation)));
            //item最右侧显示">"
            itemView.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
            //若歌单已下载自己的头像，则发起一个解码的异步任务（因为解码耗时较长，会阻塞主线程）,解码结束后自动更新头像
            if (songListPojo.avatarPath!=null)
                new DecodeImageAsyncTask(itemView,getResources()).execute(songListPojo.avatarPath);
            //设置item点击监听
            itemView.setOnClickListener(v -> {
                //TODO 触发切换Fragment
                Toast.makeText(getContext(),"待实现：触发切换Fragment",Toast.LENGTH_SHORT).show();
            });
            section.addItemView(itemView,null);
        }
        section.addTo(groupListView);
    }

    /**
     * 根据ManyToMany的关系列表计算某个歌单中歌曲的数量
     * @param songListPojo 歌单
     * @param allRelation 关系表
     * @return 该歌单中歌曲的数量
     */
    private int sizeOf(SongListPojo songListPojo,List<SongXSongList> allRelation){
        int size=0;
        for (SongXSongList songXSongList:allRelation) {
            if (songXSongList.songListId == songListPojo.id)
                size++;
        }
        return size;
    }
}