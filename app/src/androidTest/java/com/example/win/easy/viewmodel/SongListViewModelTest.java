package com.example.win.easy.viewmodel;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import com.example.win.easy.repository.db.data_object.SongDO;
import com.example.win.easy.repository.db.data_object.SongListDO;
import com.example.win.easy.repository.repo.Repo;
import com.example.win.easy.value_object.SongListVO;
import com.example.win.easy.value_object.SongVO;
import com.example.win.easy.value_object.VOUtil;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static com.example.win.easy.enumeration.DataSource.Local;
import static com.example.win.easy.enumeration.DataSource.WangYiYun;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4ClassRunner.class)
public class SongListViewModelTest {

    /**
     * <p>准备好数据，测试ViewModel返回的是否与预期的结果一致</p>
     * <p>依旧设了个调用者来当作数据的Observer</p>
     */
    @Test
    public void testGetAll() {
        assertEquals(expectedAllSongListVO,mockInvoker.getCurrentData());
    }

    /**
     * <p>同上，测试获取某个歌单的歌曲列表</p>
     */
    @Test
    public void testSongsOfCertainSongList() {
        assertEquals(expectedSongsOfTestSongListVO,mockInvoker.getSongsIn(testSongListVO));
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        mockVOUtil();

        setUpTestData();
        setUpTestViewModel();

        mockRepo();
        mockInvoker();
    }

    private void mockVOUtil() {
        when(voUtil.toVO(any(SongListDO.class))).thenAnswer(invocation -> {
            SongListDO songListDO=invocation.getArgument(0);
            return SongListVO.builder()
                    .id(songListDO.getId())
                    .name(songListDO.getName())
                    .avatarPath(songListDO.getAvatarPath())
                    .avatarUrl(songListDO.getAvatarUrl())
                    .build();
        });
        when(voUtil.toVO(any(SongDO.class))).thenAnswer(invocation -> {
            SongDO songDO=invocation.getArgument(0);
            return SongVO.builder()
                    .id(songDO.getId())
                    .name(songDO.getName())
                    .songFileUrl(songDO.getSongUrl())
                    .songFilePath(songDO.getSongPath())
                    .avatarPath(songDO.getAvatarPath())
                    .avatarUrl(songDO.getAvatarUrl())
                    .build();
        });
        when(voUtil.toDO(any(SongListVO.class))).thenReturn(SongListDO.builder().build());
    }

    private void setUpTestData() {
        SongListDO songListDO1= SongListDO.builder().id(78L).name("衬衫的价格是").name("九傍十五便士").source(Local).avatarUrl("/abcd.jpg").avatarPath("没了").uid(3L).remoteId(88L).build();
        SongListDO songListDO2= SongListDO.builder().id(99L).name("所以你选择").name("B项").source(Local).avatarUrl("/efgh.jpg").avatarPath("真没了").uid(3L).remoteId(6L).build();
        List<SongListDO> allSongListDO=new ArrayList<>();
        allSongListDO.add(songListDO1);
        allSongListDO.add(songListDO2);
        allSongListDOLiveData.postValue(allSongListDO);

        SongListVO songListVO1=voUtil.toVO(songListDO1);
        SongListVO songListVO2=voUtil.toVO(songListDO2);
        expectedAllSongListVO.add(songListVO1);
        expectedAllSongListVO.add(songListVO2);

        SongDO songDO1=SongDO.builder().id(77777L).name("球球你了").source(Local).build();
        SongDO songDO2=SongDO.builder().id(88888L).name("饶了我吧").source(WangYiYun).build();
        songsInReo.add(songDO1);
        songsInReo.add(songDO2);

        SongVO songVO1=voUtil.toVO(songDO1);
        SongVO songVO2=voUtil.toVO(songDO2);
        expectedSongsOfTestSongListVO.add(songVO1);
        expectedSongsOfTestSongListVO.add(songVO2);
    }

    private void setUpTestViewModel(){
        songListViewModel=new SongListViewModelImpl(repo,voUtil);
    }

    private void mockRepo() {
        doReturn(allSongListDOLiveData).when(repo).getAllSongList();
        when(repo.songsOf(any(SongListDO.class))).thenReturn(songsInReo);
    }

    private void mockInvoker() {
        mockInvoker=new MockInvoker(lifecycleOwner,songListViewModel);
        doReturn(mockInvoker).when(lifecycleOwner).getLifecycle();
        mockInvoker.initDataObservation();
        mockInvoker.handleLifecycleEvent(Lifecycle.Event.ON_START);
    }

    @Rule public InstantTaskExecutorRule instantTaskExecutorRule=new InstantTaskExecutorRule();

    private MutableLiveData<List<SongListDO>> allSongListDOLiveData =new MutableLiveData<>();
    private List<SongListVO> expectedAllSongListVO=new ArrayList<>();

    private SongListVO testSongListVO=SongListVO.builder().build();
    private List<SongVO> expectedSongsOfTestSongListVO=new ArrayList<>();
    private List<SongDO> songsInReo =new ArrayList<>();

    @Mock Repo repo;
    @Mock VOUtil voUtil;
    private SongListViewModel songListViewModel;

    @Mock LifecycleOwner lifecycleOwner;
    private MockInvoker mockInvoker;
}

class MockInvoker extends LifecycleRegistry {

    private LiveData<List<SongListVO>> allSongListLiveData;
    private LifecycleOwner myLifecycleOwner;
    private SongListViewModel songListViewModel;

    MockInvoker(@NonNull LifecycleOwner provider, SongListViewModel songListViewModel) {
        super(provider);
        this.myLifecycleOwner =provider;
        this.songListViewModel=songListViewModel;
        allSongListLiveData =songListViewModel.getAll();
    }

    void initDataObservation(){
        allSongListLiveData.observe(myLifecycleOwner, allSongList->{});
    }

    List<SongListVO> getCurrentData(){
        return allSongListLiveData.getValue();
    }
    List<SongVO> getSongsIn(SongListVO songListVO){
        return songListViewModel.songsOf(songListVO);
    }

}