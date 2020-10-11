package FileSystem.Controller;

import FileSystem.Exception.FileExistedException;
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
    File newFile(Id fileId) throws FileExistedException;
    File newFile(Id fileId,Id fileManagerId) throws FileExistedException;
    Map<FileManagerId,List<File>> listFiles();
}
