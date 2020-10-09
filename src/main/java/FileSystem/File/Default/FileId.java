package FileSystem.File.Default;

import FileSystem.Util.Id;

/**
 * @author : chara
 */
public class FileId implements Id {

    private long fileIdNumber;

    public FileId(long fileIdNumber) {
        this.fileIdNumber = fileIdNumber;
    }

    @Override
    public long getId() {
        return fileIdNumber;
    }

    @Override
    public String getIdString() {
        return "f" + fileIdNumber;
    }
}
