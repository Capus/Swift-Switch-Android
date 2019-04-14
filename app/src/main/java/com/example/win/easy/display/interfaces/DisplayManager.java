package com.example.win.easy.display.interfaces;

import android.media.MediaPlayer;

import com.example.win.easy.display.DisplayMode;
import com.example.win.easy.display.SongList;
import com.example.win.easy.song.Song;

public interface DisplayManager {

    //我觉得next和previous应该为公有，而changesong应该为私有，改了一下
    void next(int currentSongIndex, MediaPlayer mediaPlayer);

    void previous(int currentSongIndex, MediaPlayer mediaPlayer);

    void setMode(DisplayMode mode);

    void setDisplayList(SongList list);

    void restartWith(Song song, MediaPlayer mediaPlayer);//根据具体实现，我修改了一下参数
}
