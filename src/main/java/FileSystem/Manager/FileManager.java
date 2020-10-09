package FileSystem.Manager;

import FileSystem.File.File;
import FileSystem.Util.Id;

public interface FileManager {
    File getFile(Id fileId);
    File newFile(Id fileId);
    Id getId();
}
