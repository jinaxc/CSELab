package FileSystem.Manager.Default;

import FileSystem.Exception.FileException.FileExistedException;
import FileSystem.Exception.InitiationFailedException;
import FileSystem.File.Default.DefaultFile;
import FileSystem.File.File;
import FileSystem.Manager.FileManager;
import FileSystem.Util.Id;
import FileSystem.Util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : chara
 */
public class DefaultFileManager implements FileManager {
    private final static Logger LOGGER = LogManager.getLogger(FileManager.class);

    private Map<Long, File> files;
    private final Id id;

    public DefaultFileManager(FileManagerId id) throws InitiationFailedException {
        this.id = id;
        files = new HashMap<>();
        java.io.File f = new java.io.File(Properties.FILE_PATH + id.getIdString());
        if(!f.exists()){
            if(!f.mkdir()){
                throw new InitiationFailedException("can't mkdir");
            }
        }
    }

    public void setFiles(Map<Long, File> files) {
        this.files = files;
    }

    @Override
    public Id getId() {
        return id;
    }

    @Override
    public File getFile(Id fileId) {
        return files.get(fileId.getId());
    }

    /**
     *
     * @param fileId file name
     * @return null if newFile failed
     */
    @Override
    public File newFile(Id fileId) throws FileExistedException {
        //TODO maybe should throw an exception
        if(files.get(fileId.getId()) != null){
            LOGGER.warn("file {} existed",fileId.getIdString());
            throw new FileExistedException("file already existed");
        }
        String pathPrefix = Properties.FILE_PATH + id.getIdString() + "/" + fileId.getIdString();
        File file;
        try {
            file = new DefaultFile(pathPrefix,this,fileId);
            file.initializeFile();
            files.put(fileId.getId(),file);
        } catch (IOException e) {
            LOGGER.error("newFile failed");
            return null;
        }
        return file;
    }
}
