package FileSystem.Manager.Default;

import FileSystem.Exception.FileManagerIdFormatException;
import FileSystem.Util.Id;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author : chara
 */
public class FileManagerId implements Id {
    private final static Logger LOGGER = LogManager.getLogger(FileManagerId.class);
    private long fileManagerIdNumber;

    public FileManagerId(long fileManagerIdNumber) {
        this.fileManagerIdNumber = fileManagerIdNumber;
    }

    public FileManagerId(String fileManagerIdStr) throws FileManagerIdFormatException {
        if(fileManagerIdStr.length() < 2){
            LOGGER.error("invalid id {}",fileManagerIdStr);
            throw new FileManagerIdFormatException("invalid blockManagerId format");
        }
        String number = fileManagerIdStr.substring(2);
        try{
            this.fileManagerIdNumber = Integer.parseInt(number);
        }catch (NumberFormatException e){
            LOGGER.error("invalid id {}",fileManagerIdStr);
            throw e;
        }

    }

    @Override
    public String getIdString() {
        return "fm" + fileManagerIdNumber;
    }

    @Override
    public long getId() {
        return fileManagerIdNumber;
    }
}
