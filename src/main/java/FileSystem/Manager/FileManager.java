package FileSystem.Manager;

import FileSystem.Exception.FileException.FileExistedException;
import FileSystem.File.File;
import FileSystem.Util.Id;

public interface FileManager {
    File getFile(Id fileId);
    File newFile(Id fileId) throws FileExistedException;
    Id getId();
}
