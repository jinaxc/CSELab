package FileSystem.Manager.Default;

import FileSystem.Exception.InitiationFailedException;
import FileSystem.File.Default.DefaultFile;
import FileSystem.File.Default.FileId;
import FileSystem.File.File;
import FileSystem.Manager.FileManager;
import FileSystem.Util.Id;
import FileSystem.Util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : chara
 */
public class DefaultFileManager implements FileManager {
    private final static Logger LOGGER = LogManager.getLogger(FileManager.class);

    private Map<Long, File> files;
    private final FileManagerId id;

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
    public FileManagerId getId() {
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
    public File newFile(Id fileId) {
        //TODO maybe should throw an exception
        if(files.get(fileId.getId()) != null){
            return null;
        }
        String pathPrefix = Properties.FILE_PATH + id.getIdString() + "/" + fileId.getIdString();
        File file;
        try {
            file = new DefaultFile(pathPrefix,this, (FileId) fileId);
            file.initializeFile();
            files.put(fileId.getId(),file);
        } catch (IOException e) {
            LOGGER.error("newFile failed");
            return null;
        }
        return file;
    }

    @Override
    public List<File> listFiles() {
        return new ArrayList<>(files.values());
    }
}
