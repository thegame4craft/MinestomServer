package de.thegame4craft.util;

import java.io.File;

public class FileSystemHelper {
    public static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    public static void crateFolderIfNotExists(File folder) {
        if(!folder.exists()) {
            if(!folder.mkdirs()) {
                throw new RuntimeException("Failed to create folder: " + folder.getAbsolutePath());
            }
        }
    }
}
