package FileSystem.Controller;

import FileSystem.Block.Block;
import FileSystem.Block.Default.BlockIndexIdWithManagerId;
import FileSystem.Manager.BlockManager;
import FileSystem.Manager.Default.BlockManagerId;

public interface BlockManagerController {
    public Block getBlock(BlockIndexIdWithManagerId id);
    public Block newBlock(byte[] bytes);
    public BlockManager getBlockManager(BlockManagerId id);
}
