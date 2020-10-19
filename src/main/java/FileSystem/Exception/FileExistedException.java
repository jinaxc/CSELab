package FileSystem.Exception;

/**
 * @author : chara
 */
public class FileExistedException extends Exception{
    public FileExistedException() {
    }

    public FileExistedException(String message) {
        super(message);
    }
}
