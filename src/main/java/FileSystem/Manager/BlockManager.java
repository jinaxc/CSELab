package FileSystem.Manager;

import FileSystem.Block.Block;
import FileSystem.Util.Id;

public interface BlockManager {
    Block getBlock(Id indexId);
    Block newBlock(byte[] b);
    Id getId();
    default Block newEmptyBlock(int blockSize) {
        return newBlock(new byte[blockSize]);
    }
}
