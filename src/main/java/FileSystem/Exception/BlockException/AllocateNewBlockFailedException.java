package FileSystem.Exception.BlockException;

/**
 * @author : chara
 * throws only when newBlock failed for many times
 */
public class AllocateNewBlockFailedException extends BlockException{
    public AllocateNewBlockFailedException(String message) {
        super(message);
    }
}
