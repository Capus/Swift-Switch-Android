package com.example.win.easy.view.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.example.win.easy.Constants;
import com.example.win.easy.R;
import com.example.win.easy.factory.__SongFactory;
import com.example.win.easy.repository.db.data_object.SongDO;
import com.example.win.easy.repository.db.data_object.SongListDO;
import com.example.win.easy.tool.DialogTool;
import com.example.win.easy.tool.UriProcessTool;
import com.example.win.easy.viewmodel.SimpleViewModel;
import com.example.win.easy.web.service.LoginService;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * <p>点app图标进来后见到的主界面</p>
 */
public class MainActivityFragment extends Fragment {
    @BindView(R.id.listview) QMUIGroupListView mGroupListContact;
    @BindView(R.id.topbar) QMUITopBar qmuiTopBar;

    private QMUICommonListItemView allSongItem;
    private QMUICommonListItemView allSongListItem;

    private ImageButton addSongBtn;
    private ImageButton addSongListBtn;
    private ImageButton cloud;
    private ImageButton music;

    private ViewModelProvider.Factory factory;
    private __SongFactory songFactory;
    private LoginService loginService;

    private SimpleViewModel viewModel;
    private LiveData<Integer> songAmount;
    private LiveData<Integer> songListAmount;
    private LiveData<List<SongDO>> allSongs;
    private LiveData<List<SongListDO>> allSongLists;
    private List<LiveData<List<SongDO>>> recordTable;

    public MainActivityFragment(ViewModelProvider.Factory factory,__SongFactory songFactory,LoginService loginService){
        this.factory=factory;
        this.songFactory=songFactory;
        this.loginService=loginService;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View thisView=inflater.inflate(R.layout.fragment_main_activity,container,false);
        ButterKnife.bind(this,thisView);
        //初始化界面
        initView();
        System.out.println("I am created view!");
        return thisView;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //注册数据监听
        registerData();
    }


    /**
     * 初始化界面及其相应监听
     */
    private void initView(){
        //绑定视图
        bindView();
        //初始化顶栏
        initTopBar();
        //初始化条目
        initItems();
        //初始化按钮
        initButtons();
    }


    /**
     * 绑定视图
     */
    private void bindView(){
        allSongItem = mGroupListContact.createItemView("我的歌曲");
        allSongListItem = mGroupListContact.createItemView("我的歌单");
        addSongBtn =new ImageButton(getContext());
        addSongListBtn =new ImageButton(getContext());
        cloud=new ImageButton(getContext());
        music=new ImageButton(getContext());
    }
    /**
     * 初始化Item
     */
    private void initItems(){
        allSongItem.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CUSTOM);
        allSongListItem.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CUSTOM);
        allSongItem.addAccessoryCustomView(addSongBtn);
        allSongItem.setOnClickListener(v -> {
            Navigation.findNavController(getView()).navigate(R.id.action_mainActivityFragment_to_allSongsFragment);
        });
        allSongListItem.addAccessoryCustomView(addSongListBtn);
        allSongListItem.setOnClickListener(v -> {
            Navigation.findNavController(getView()).navigate(R.id.action_mainActivityFragment_to_allSongListsFragment);
        });
        QMUIGroupListView.newSection(getContext())
                .addItemView(allSongItem,null)
                .addItemView(allSongListItem,null)
                .addTo(mGroupListContact);
    }

    /**
     * 初始化按钮
     */
    private void initButtons(){
        addSongBtn.setImageResource(R.drawable.ic_action_name);
        addSongListBtn.setImageResource(R.drawable.ic_action_name);
        addSongBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("audio/*");
            startActivityForResult(intent, Constants.READ_REQUEST_CODE);
        });
    }

    /**
     * 初始化顶栏
     */
    private void initTopBar(){
        cloud.setImageResource(R.drawable.ic_action_cloud);
        music.setImageResource(R.drawable.ic_action_music);
        qmuiTopBar.setTitle("我的");
        qmuiTopBar.addRightImageButton(R.drawable.ic_action_music,music.getId()).setOnClickListener(v -> Toast.makeText(getContext(),"音乐 ",Toast.LENGTH_LONG).show());
        qmuiTopBar.addLeftImageButton(R.drawable.ic_action_cloud,cloud.getId()).setOnClickListener(v -> {
            if (!loginService.hasLogin())
                Navigation.findNavController(getView()).navigate(MainActivityFragmentDirections.actionMainActivityFragmentToLoginFragment());
        });
    }

    /**
     * 注册MVVM数据
     */
    private void registerData(){
        viewModel= ViewModelProviders.of(this,factory).get(SimpleViewModel.class);
        songAmount=viewModel.getSongAmount();
        songListAmount=viewModel.getSongListAmount();
        allSongs=viewModel.getAllSongs();
        allSongLists=viewModel.getAllSongLists();
        songAmount.observe(this, integer -> {
            allSongItem.setDetailText(integer.toString());
        });
        songListAmount.observe(this,integer -> {
            allSongListItem.setDetailText(integer.toString());
        });
        allSongs.observe(this,songPojos -> {});
        allSongLists.observe(this, songListPojos -> { });
        recordTable=new ArrayList<>();
        if (allSongLists.getValue()!=null)
            for (SongListDO songListDO :allSongLists.getValue()){
                LiveData<List<SongDO>> record=viewModel.getAllSongsForSongList(songListDO);
                record.observe(this, songPojos -> { });
                recordTable.add(record);
            }
    }

    /**
     * 添加歌曲文件且选取好要添加的歌曲文件后，会触发该函数，该函数用于跳转，让用户选择添加歌曲到哪一个歌单
     * @param requestCode 触发该函数的intent的请求码
     * @param resultCode 对该intent响应的响应码
     * @param resultData 如果成功操作，返回的数据
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        //从查看文件夹Activity,即请求码为READ_REQUEST_CODE返回
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == Constants.READ_REQUEST_CODE
                && resultCode == Activity.RESULT_OK
                && resultData != null) {
            //将歌曲文件添加到歌单的对话框
            createDialogAddSongToSongList(resultData.getData());
        }
    }

    /**
     * 查看所有歌曲的对话框
     */
    public void createDialogSeeAllSongs(){
        List<String> songNames=new ArrayList<>();
        if (allSongs.getValue()!=null)
            for (SongDO songDO :allSongs.getValue())
                songNames.add(songDO.getName());
        DialogTool.createMenuDialog(
                getContext(),
                "所有歌曲",
                songNames.toArray(new String[0]),
                null,
                com.qmuiteam.qmui.R.style.QMUI_Dialog
        );
    }

    /**
     * 添加歌曲到歌单的对话框
     * @param uri 将添加的音乐文件的URI
     */
    public void createDialogAddSongToSongList(final Uri uri){
        if (allSongLists.getValue()!=null){
            List<String> songListNames=new ArrayList<>();
            for (SongListDO songListDO :allSongLists.getValue())
                songListNames.add(songListDO.getName());
            final QMUIDialog.MultiCheckableDialogBuilder builder=new QMUIDialog.MultiCheckableDialogBuilder(getContext());
            DialogTool.createMultiCheckDialog(
                    builder,
                    "添加到歌单..",
                    songListNames.toArray(new String[0]),
                    null,
                    "确定",
                    new AddSongToSongListListener(uri,builder),
                    null,
                    null,
                    com.qmuiteam.qmui.R.style.QMUI_Dialog
            );
        }else
            viewModel.insert(songFactory.create(new File(UriProcessTool.getPathByUri4kitkat(getContext(),uri))));
    }

    /**
     * 查看歌单的对话框
     */
    private void createDialogSeeSongList(){
        List<String> songListNames=new ArrayList<>();
        if(allSongLists.getValue()!=null)
            for (SongListDO songListDO :allSongLists.getValue())
                songListNames.add(songListDO.getName());
        DialogTool.createMenuDialog(
                getContext(),
                "所有歌单",
                songListNames.toArray(new String[0]),
                null,
//                new CheckSongListListener(),
                com.qmuiteam.qmui.R.style.QMUI_Dialog
        );
    }

    /**
     * 添加歌曲到歌单的监听器类
     */
    class AddSongToSongListListener implements QMUIDialogAction.ActionListener {

        private Uri uri;
        private QMUIDialog.MultiCheckableDialogBuilder builder;
        AddSongToSongListListener(Uri uri, QMUIDialog.MultiCheckableDialogBuilder builder){
            this.uri=uri;
            this.builder=builder;
        }
        @Override
        public void onClick(QMUIDialog dialog, int index) {
            File songFile=new File(UriProcessTool.getPathByUri4kitkat(builder.getBaseContext(),uri));
            List<SongListDO> songListDOS =new ArrayList<>();
            int[] indices=builder.getCheckedItemIndexes();
            for (int checkedIndex:indices)
                songListDOS.add(allSongLists.getValue().get(checkedIndex));
            viewModel.insertNewSongAndToSongLists(songFactory.create(songFile), songListDOS);
            Toast.makeText(builder.getBaseContext(),"添加成功", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        }
    }

}