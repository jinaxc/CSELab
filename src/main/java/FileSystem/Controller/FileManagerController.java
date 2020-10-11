package FileSystem.Controller;

import FileSystem.File.Default.FileIdWithManagerId;
import FileSystem.File.File;
import FileSystem.Manager.Default.FileManagerId;
import FileSystem.Manager.FileManager;
import FileSystem.Util.Id;

import java.util.List;
import java.util.Map;

/**
 * @author : chara
 */
public interface FileManagerController {
    File getFile(FileIdWithManagerId fileId);
    File newFile(Id fileId);
    File newFile(Id fileId,Id fileManagerId);
    Map<FileManagerId,List<File>> listFiles();
}
