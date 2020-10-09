package FileSystem.Manager.Default;

import FileSystem.Block.Block;
import FileSystem.Block.Default.BlockIndexId;
import FileSystem.Block.Default.DefaultBlock;

import FileSystem.Exception.InitiationFailedException;
import FileSystem.Manager.BlockManager;

import FileSystem.Util.Properties;

import FileSystem.Util.Id;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : chara
 */
public class DefaultBlockManager implements BlockManager {
    private final static Logger LOGGER = LogManager.getLogger(BlockManager.class);
    private Map<Long,Block> blocks;
    private long count;
    private Id id;

    public DefaultBlockManager(Id id) throws InitiationFailedException {
        this.id = id;
        this.blocks = new HashMap<>();
        java.io.File f = new java.io.File(Properties.BLOCK_PATH + id.getIdString());
        if(!f.exists()){
            if(!f.mkdir()){
                throw new InitiationFailedException("can't mkdir");
            }
        }
    }

    public void setCount(long count) {
        this.count = count;
    }

    public void setBlocks(Map<Long, Block> blocks) {
        this.blocks = blocks;
    }

    @Override
    public Id getId() {
        return id;
    }

    @Override
    public Block getBlock(Id indexId) {
        return blocks.get(indexId.getId());
    }

    /**
     *
     * @param b the bytes to write into the new block
     * @return null if create new block failed
     */
    @Override
    public Block newBlock(byte[] b) {
        long next = ++count;
        String pathPrefix = Properties.BLOCK_PATH + "/" + id.getIdString() + "/b" + next;
        Block block = null;
        try {
            block = new DefaultBlock(pathPrefix + ".meta",pathPrefix + ".data",
                    this,new BlockIndexId(next),b);
        } catch (IOException e) {
            LOGGER.error("newBlock failed");
            return null;
        }
        blocks.put(block.getIndexId().getId(),block);
        return block;
    }
}
