package com.example.win.easy.value_object;

import java.io.File;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SongVO {

    String name;

    String songFileUrl;

    String songFilePath;

    File avatarFile;


    public boolean songFileHasBeenDownloaded(){return songFilePath!=null;}
    public boolean songFileCanBeDownloaded(){return songFileUrl!=null;}
}
