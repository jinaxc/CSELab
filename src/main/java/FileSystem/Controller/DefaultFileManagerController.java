package FileSystem.Controller;

import FileSystem.Exception.FileExistedException;
import FileSystem.Exception.InitiationFailedException;
import FileSystem.File.Default.FileIdWithManagerId;
import FileSystem.File.File;
import FileSystem.Manager.BlockManager;
import FileSystem.Manager.Default.DefaultFileManager;
import FileSystem.Manager.Default.FileManagerId;
import FileSystem.Manager.FileManager;
import FileSystem.Util.Id;
import FileSystem.Util.Properties;

import java.util.List;
import java.util.Random;

/**
 * @author : chara
 */
public class DefaultFileManagerController implements FileManagerController{
    private final int COUNT;
    private FileManager[] managers;
    private Random r = new Random();

    public DefaultFileManagerController(int COUNT) throws InitiationFailedException {
        this.COUNT = COUNT;
        managers = new FileManager[COUNT];
        java.io.File f = new java.io.File(Properties.FILE_PATH);
        if(!f.exists()){
            if(!f.mkdir()){
                throw new InitiationFailedException("can't mkdir");
            }
        }
        for(int i = 0;i < COUNT;i++){
            managers[i] = new DefaultFileManager(new FileManagerId(i));
        }
    }
    public DefaultFileManagerController(List<FileManager> managers){
        this.COUNT = managers.size();
        this.managers = managers.toArray(new FileManager[0]);
    }

    @Override
    public File getFile(FileIdWithManagerId fileId) {
        return managers[(int) fileId.getFileManagerId().getId()].getFile(fileId.getFileId());
    }

    /**
     *
     * @param fileId file name
     * @return null if the manager returns a null file
     */
    @Override
    public File newFile(Id fileId) throws FileExistedException {
        return managers[r.nextInt(COUNT)].newFile(fileId);
    }

    @Override
    public File newFile(Id fileId,Id fileManagerId) throws FileExistedException {
        return managers[(int) fileManagerId.getId()].newFile(fileId);
    }
}
