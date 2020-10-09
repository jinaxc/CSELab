package FileSystem.File;

import FileSystem.Block.Default.BlockIndexIdWithManagerId;

import java.util.List;

/**
 * @author : chara
 */
public interface FileMetaData {
    void setSize(long size);
    long getSize();
    long getBlockSize();
    List<List<BlockIndexIdWithManagerId>> getBlocks();
}
