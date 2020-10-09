package FileSystem.Manager.Default;

import FileSystem.Block.Default.BlockIndexIdWithManagerId;
import FileSystem.Exception.BlockManagerIdFormatException;
import FileSystem.Util.Id;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author : chara
 */
public class BlockManagerId implements Id {
    private final static Logger LOGGER = LogManager.getLogger(BlockManagerId.class);
    private long blockManagerIdNumber;

    public BlockManagerId(long blockManagerIdNumber) {
        this.blockManagerIdNumber = blockManagerIdNumber;
    }

    public BlockManagerId(String blockManagerIdStr) throws BlockManagerIdFormatException {
        if(blockManagerIdStr.length() < 2){
            LOGGER.error("invalid id {}",blockManagerIdStr);
            throw new BlockManagerIdFormatException("invalid blockManagerId format");
        }
        String number = blockManagerIdStr.substring(2);
        try{
            this.blockManagerIdNumber = Integer.parseInt(number);
        }catch (NumberFormatException e){
            LOGGER.error("invalid id {}",blockManagerIdStr);
            throw e;
        }

    }


    @Override
    public String getIdString() {
        return "bm" + blockManagerIdNumber;
    }

    @Override
    public long getId() {
        return blockManagerIdNumber;
    }
}
