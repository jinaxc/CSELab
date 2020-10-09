package FileSystem.Block.Default;

import FileSystem.Util.Id;

/**
 * @author : chara
 */
public class BlockIndexId implements Id {

    private long blockIndexId;

    public BlockIndexId(long blockIndexId) {
        this.blockIndexId = blockIndexId;
    }

    @Override
    public long getId() {
        return blockIndexId;
    }

    @Override
    public String getIdString() {
        return "b" + blockIndexId;
    }
}
