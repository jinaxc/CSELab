package FileSystem.File.Default;

import FileSystem.Block.Default.BlockIndexIdWithManagerId;
import FileSystem.File.FileMetaData;

import java.util.List;

/**
 * @author : chara
 */
public class DefaultFileMetaData implements FileMetaData {

    private long size;

    private long blockSize;

    private List<List<BlockIndexIdWithManagerId>> blocks;

    public DefaultFileMetaData(long size, long blockSize, List<List<BlockIndexIdWithManagerId>> blocks) {
        this.size = size;
        this.blockSize = blockSize;
        this.blocks = blocks;
    }

    @Override
    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public long getBlockSize() {
        return blockSize;
    }

    @Override
    public List<List<BlockIndexIdWithManagerId>> getBlocks() {
        return blocks;
    }
}
