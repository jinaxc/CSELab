package FileSystem.File.Default;
import FileSystem.Exception.FileException.FileIdWithManagerIdFormatException;
import FileSystem.Manager.Default.FileManagerId;
import FileSystem.Util.Id;
import FileSystem.Util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author : chara
 */
public class FileIdWithManagerId implements Id {
    private final static Logger LOGGER = LogManager.getLogger(FileIdWithManagerId.class);
    private final FileId fileId;
    private final FileManagerId fileManagerId;

    public FileIdWithManagerId(FileId fileId, FileManagerId fileManagerId) {
        this.fileId = fileId;
        this.fileManagerId = fileManagerId;
    }

    public FileIdWithManagerId(String idString) throws FileIdWithManagerIdFormatException {
        String[] split = idString.split(Properties.FILE_MANAGER_ID_AND_FILE_ID_SPLITTER + "");
        if(split.length != 2){
            LOGGER.error("invalid id {}",idString);
            throw new FileIdWithManagerIdFormatException("wrong format { " + idString + " }");
        }
        try{
            this.fileManagerId = new FileManagerId(Integer.parseInt(split[0].substring(2)));
            this.fileId = new FileId(Integer.parseInt(split[1].substring(1)));
        }catch (NumberFormatException e){
            LOGGER.error("invalid id {}",idString);
            throw new FileIdWithManagerIdFormatException(e.getMessage());
        }
    }


    public FileId getFileId() {
        return fileId;
    }

    public FileManagerId getFileManagerId() {
        return fileManagerId;
    }

    @Override
    public String getIdString() {
        return fileManagerId.getIdString() + Properties.FILE_MANAGER_ID_AND_FILE_ID_SPLITTER + fileId.getIdString();
    }

    @Override
    public long getId() {
        return fileId.getId();
    }
}
