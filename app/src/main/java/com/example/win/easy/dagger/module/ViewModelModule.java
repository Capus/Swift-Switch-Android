package com.example.win.easy.dagger.module;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.win.easy.value_object.SongListVO;
import com.example.win.easy.value_object.SongVO;
import com.example.win.easy.value_object.VOUtil;
import com.example.win.easy.repository.deprecated.repo.__SongRepository;
import com.example.win.easy.viewmodel.SimpleViewModel;
import com.example.win.easy.viewmodel.SongListViewModel;
import com.example.win.easy.viewmodel.SongViewModel;
import com.example.win.easy.factory.ViewModelFactory;
import com.example.win.easy.repository.deprecated.repo.__SongListRepository;
import com.example.win.easy.repository.deprecated.repo.__SongXSongListRepository;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import dagger.MapKey;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;

@Module
public class ViewModelModule {

    @Target(ElementType.METHOD)
    @Documented
    @MapKey
    @interface ViewModelKey{
        Class<? extends ViewModel> value();
    }

    @Provides
    @IntoMap
    @ViewModelKey(SimpleViewModel.class)
    static ViewModel simpleViewModel(__SongRepository songRepository,
                                     __SongListRepository songListRepository,
                                     __SongXSongListRepository songXSongListRepository){
        return new SimpleViewModel(songRepository,songListRepository,songXSongListRepository);
    }

    @Provides
    @IntoMap
    @ViewModelKey(SongViewModel.class)
    static ViewModel songViewModel(__SongRepository songRepository){
        return new SongViewModel(songRepository,new VOUtil());
    }

    @Provides
    @IntoMap
    @ViewModelKey(SongListViewModel.class)
    static ViewModel provideSongListViewModel(){
        return new SongListViewModel() {
            @Override
            public LiveData<List<SongVO>> songsOf(SongListVO songListVO) {
                return new MutableLiveData<>();
            }

            @Override
            public LiveData<List<SongListVO>> getAll() {
                MutableLiveData<List<SongListVO>> all=new MutableLiveData<>();
                List<SongListVO> list=new ArrayList<>();
                list.add(SongListVO.builder().id(1).name("从何时你也学会不要离群").build());
                list.add(SongListVO.builder().id(2).name("原来神仙鱼横渡大海会断魂").build());
                all.postValue(list);
                return all;
            }
        };
    }

    @Provides
    static ViewModelProvider.Factory provideViewModelFactory(Map<Class<? extends ViewModel>, Provider<ViewModel>> providerMap){
        return new ViewModelFactory(providerMap);
    }

}
