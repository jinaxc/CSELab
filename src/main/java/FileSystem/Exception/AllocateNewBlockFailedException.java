package FileSystem.Exception;

/**
 * @author : chara
 * throws only when newBlock failed for many times
 */
public class AllocateNewBlockFailedException extends Exception{
    public AllocateNewBlockFailedException(String message) {
        super(message);
    }
}
