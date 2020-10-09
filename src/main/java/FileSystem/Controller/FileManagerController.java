package FileSystem.Controller;

import FileSystem.File.Default.FileIdWithManagerId;
import FileSystem.File.File;
import FileSystem.Util.Id;

/**
 * @author : chara
 */
public interface FileManagerController {
    File getFile(FileIdWithManagerId fileId);
    File newFile(Id fileId);
    File newFile(Id fileId,Id fileManagerId);
}
