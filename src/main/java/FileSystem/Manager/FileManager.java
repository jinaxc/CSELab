package FileSystem.Manager;

import FileSystem.File.File;
import FileSystem.Manager.Default.FileManagerId;
import FileSystem.Util.Id;

import java.util.List;

public interface FileManager {
    File getFile(Id fileId);
    File newFile(Id fileId);
    FileManagerId getId();
    List<File> listFiles();
}
