package FileSystem.Block.Default;

import FileSystem.Exception.BlockException.BlockIndexIdWithManagerIdFormatException;
import FileSystem.Manager.Default.BlockManagerId;
import FileSystem.Util.Id;
import FileSystem.Util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author : chara
 */
public class BlockIndexIdWithManagerId implements Id {
    private final static Logger LOGGER = LogManager.getLogger(BlockIndexIdWithManagerId.class);
    private final Id blockIndexId;
    private final Id blockManagerId;

    public BlockIndexIdWithManagerId(Id blockIndexId, Id blockManagerId) {
        this.blockIndexId = blockIndexId;
        this.blockManagerId = blockManagerId;
    }

    public Id getBlockIndexId() {
        return blockIndexId;
    }

    public Id getBlockManagerId() {
        return blockManagerId;
    }

    public BlockIndexIdWithManagerId(String idString) throws BlockIndexIdWithManagerIdFormatException {
        String[] split = idString.split(Properties.BLOCK_MANAGER_ID_AND_BLOCK_ID_SPLITTER + "");
        if(split.length != 2){
            LOGGER.error("invalid id {}",idString);
            throw new BlockIndexIdWithManagerIdFormatException("invalid id");
        }
        try{
            this.blockManagerId = new BlockManagerId(Integer.parseInt(split[0].substring(2)));
            this.blockIndexId = new BlockIndexId(Integer.parseInt(split[1].substring(1)));
        }catch (NumberFormatException e){
            LOGGER.error("invalid id {}",idString);
            throw new BlockIndexIdWithManagerIdFormatException(e.getMessage());
        }

    }

    @Override
    public String getIdString() {
        return blockManagerId.getIdString() + Properties.BLOCK_MANAGER_ID_AND_BLOCK_ID_SPLITTER + "" + blockIndexId.getIdString();
    }

    @Override
    public long getId() {
        return blockIndexId.getId();
    }
}
