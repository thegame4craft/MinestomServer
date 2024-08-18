package de.thegame4craft.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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

    public static void createFolderIfNotExists(File folder) {
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                throw new RuntimeException("Failed to create folder: " + folder.getAbsolutePath());
            }
        }
    }

    public static void createFileIfNotExists(File file, String content) {
        if(!file.exists()) {
            try {
                Files.writeString(file.toPath(), "{}");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
