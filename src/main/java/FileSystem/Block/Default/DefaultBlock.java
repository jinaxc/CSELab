package FileSystem.Block.Default;

import FileSystem.Application.Application;
import FileSystem.Block.Block;
import FileSystem.Block.BlockData;
import FileSystem.Block.BlockMeta;
import FileSystem.Exception.BlockIndexIdWithManagerIdFormatException;
import FileSystem.Manager.BlockManager;
import FileSystem.ReaderWriter.AtomicBlockWriter;
import FileSystem.Util.Properties;
import FileSystem.Util.EncryptUtil;
import FileSystem.Util.Id;
import FileSystem.ReaderWriter.ReaderWriterUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * @author : chara
 */
public class DefaultBlock implements Block {
    private final static Logger LOGGER = LogManager.getLogger(Block.class);
    private final String blockMetaPath;
    private final String blockDataPath;

    private final BlockManager blockManager;

    private final Id id;

    public DefaultBlock(String blockMetaPath, String blockDataPath, BlockManager blockManager, Id id,byte[] data) throws IOException {
        this.blockMetaPath = blockMetaPath;
        this.blockDataPath = blockDataPath;
        this.blockManager = blockManager;
        this.id = id;

        /*
            depends on the purpose, the code here may be deleted. Now it assumes that when first created, the block should be allocated
         */
        String check = EncryptUtil.md5(data);
        BlockMeta blockMeta = new DefaultBlockMeta(Properties.BLOCK_SIZE, data.length, check);
        BlockData blockData = new DefaultBlockData(data);
        try {
            AtomicBlockWriter.writeBlockMetaAndDataAtomic(blockMeta,blockMetaPath,blockData,blockDataPath);
        } catch (IOException e) {
            LOGGER.error("initialize block failed");
            throw e;
        }
        try {
            Application.bufferManager.putBuffer(new BlockIndexIdWithManagerId(blockManager.getId().getIdString() + "-" + id.getIdString()),data);
        } catch (BlockIndexIdWithManagerIdFormatException e) {
            //impossible catch
        }
    }

    public DefaultBlock(String blockMetaPath, String blockDataPath, BlockManager blockManager, Id id) {
        this.blockMetaPath = blockMetaPath;
        this.blockDataPath = blockDataPath;
        this.blockManager = blockManager;
        this.id = id;
    }

    @Override
    public Id getIndexId() {
        return id;
    }

    @Override
    public BlockManager getBlockManager() {
        return blockManager;
    }

    @Override
    public int getRealSize() throws IOException {
        try {
            return ReaderWriterUtil.readBlockMeta(blockMetaPath).getRealSize();
        } catch (IOException e) {
            LOGGER.error("getRealSize failed, {}",e.getMessage());
            throw e;
        }
    }

    @Override
    public String getCheck() throws IOException {
        try {
            return ReaderWriterUtil.readBlockMeta(blockMetaPath).getCheck();
        } catch (IOException e) {
            LOGGER.error("getRealSize failed, {}",e.getMessage());
            throw e;
        }
    }

    /**
     * the data returned must be valid or will be null
     * @return null if the data is invalid
     * @throws IOException when read failed
     */
    @Override
    public byte[] read() throws IOException {

        try {
            byte[] bytes = ReaderWriterUtil.readBlockData(blockDataPath).getData();
            if(!getCheck().equals(EncryptUtil.md5(bytes))){
                return null;
            }else{
                return bytes;
            }
        } catch (IOException e) {
            LOGGER.error("read failed, {}",e.getMessage());
            throw e;
        }
    }

    @Override
    public int blockSize() throws IOException{
        try {
            return ReaderWriterUtil.readBlockMeta(blockMetaPath).getSize();
        } catch (IOException e) {
            LOGGER.error("readBlockSize failed due to read failed");
            throw e;
        }
    }
}
