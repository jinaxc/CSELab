package FileSystem.Buffer;

import FileSystem.Block.Default.BlockIndexIdWithManagerId;
import FileSystem.Block.Default.DefaultBlockData;
import FileSystem.Block.Default.DefaultBlockMeta;
import FileSystem.Exception.BlockIndexIdWithManagerIdFormatException;
import FileSystem.Exception.BufferException.WriteBufferToBlockException;
import FileSystem.ReaderWriter.AtomicBlockWriter;
import FileSystem.Util.EncryptUtil;
import FileSystem.Util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : chara
 */
public class BlockBufferManager implements BufferManager<BlockBuffer, BlockIndexIdWithManagerId,byte[]> {
    private final static Logger LOGGER = LogManager.getLogger(BlockBufferManager.class);
    private final Map<String, BlockBuffer> buffers;
    private final int maxCount;
    private int currentCount;

    public BlockBufferManager(int maxCount) {
        this.buffers = new HashMap<>();
        this.maxCount = maxCount;
        currentCount = 0;
    }

    @Override
    public BlockBuffer getBuffer(BlockIndexIdWithManagerId id) {
        return buffers.get(id.getIdString());
    }

    @Override
    public boolean containsBuffer(BlockIndexIdWithManagerId id) {
        return buffers.containsKey(id.getIdString()) && buffers.get(id.getIdString()).isValid();
    }

    @Override
    public boolean putBuffer(BlockIndexIdWithManagerId id,byte[] data){
        BlockBuffer blockBuffer = buffers.get(id.getIdString());
        if(blockBuffer == null){
            if(currentCount < maxCount){
                BlockBuffer buffer = new BlockBuffer();
                buffer.put(data);
                buffers.put(id.getIdString(),buffer);
                currentCount++;
                return true;
            }else{
                for(Map.Entry<String, BlockBuffer> entry : buffers.entrySet()){
                    if(entry.getValue().isWritable()){
                        BlockBuffer buffer = entry.getValue();
                        buffer.put(data);
                        buffers.remove(entry.getKey());
                        buffers.put(id.getIdString(),buffer);
                        return true;
                    }
                }
                try {
                    startWrite();
                } catch (WriteBufferToBlockException e) {
                    LOGGER.debug("put buffer failed due to {}",e.getMessage());
                    return false;
                }
                return putBufferWithoutStartWrite(id, data);
            }
        }else{
            if(blockBuffer.isWritable()){
                blockBuffer.put(data);
                return true;
            }else{
                try {
                    startWrite();
                } catch (WriteBufferToBlockException e) {
                    LOGGER.debug("put buffer failed due to {}",e.getMessage());
                    return false;
                }
                return putBufferWithoutStartWrite(id, data);
            }
        }
    }

    public boolean putBufferWithoutStartWrite(BlockIndexIdWithManagerId id,byte[] data){
        BlockBuffer blockBuffer = buffers.get(id.getIdString());
        if(blockBuffer == null){
            if(currentCount < maxCount){
                BlockBuffer buffer = new BlockBuffer();
                buffer.put(data);
                buffers.put(id.getIdString(),buffer);
                currentCount++;
                return true;
            }else{
                for(Map.Entry<String, BlockBuffer> entry : buffers.entrySet()){
                    if(entry.getValue().isWritable()){
                        BlockBuffer buffer = entry.getValue();
                        buffer.put(data);
                        buffers.remove(entry.getKey());
                        buffers.put(id.getIdString(),buffer);
                        return true;
                    }
                }
                return false;
            }
        }else{
            if(blockBuffer.isWritable()){
                blockBuffer.put(data);
                return true;
            }else{
                return false;
            }
        }
    }


    /**
     * because a block cant be modified, thus a buffer for a block cant be rewritten too
     * @param id id
     * @param data data
     * @return true if write success
     */
    @Override
    public boolean writeBuffer(BlockIndexIdWithManagerId id, byte[] data) {
        return false;
//        BlockBuffer blockBuffer = buffers.get(id.getIdString());
//        if(blockBuffer == null){
//            if(currentCount < maxCount){
//                BlockBuffer buffer = new BlockBuffer();
//                buffer.put(data);
//                buffers.put(id.getIdString(),buffer);
//                buffer.setWritten(true);
//                currentCount++;
//                return true;
//            }else{
//                for(Map.Entry<String, BlockBuffer> entry : buffers.entrySet()){
//                    if(entry.getValue().isWritable()){
//                        BlockBuffer buffer = entry.getValue();
//                        buffer.put(data);
//                        buffer.setWritten(true);
//                        buffers.remove(entry.getKey());
//                        buffers.put(id.getIdString(),buffer);
//                        return true;
//                    }
//                }
//                try {
//                    startWrite();
//                } catch (WriteBufferToBlockException e) {
//                    LOGGER.debug("write block failed due to {}",e.getMessage());
//                    return false;
//                }
//                return writeBufferWithoutStartWrite(id, data);
//            }
//        }else{
//            if(blockBuffer.isWritable()){
//                blockBuffer.put(data);
//                blockBuffer.setWritten(true);
//                return true;
//            }else{
//                try {
//                    startWrite();
//                } catch (WriteBufferToBlockException e) {
//                    LOGGER.debug("write block failed due to {}",e.getMessage());
//                    return false;
//                }
//                return writeBufferWithoutStartWrite(id, data);
//            }
//        }
    }

    public boolean writeBufferWithoutStartWrite(BlockIndexIdWithManagerId id,byte[] data){
        return false;
//        BlockBuffer blockBuffer = buffers.get(id.getIdString());
//        if(blockBuffer == null){
//            if(currentCount < maxCount){
//                BlockBuffer buffer = new BlockBuffer();
//                buffer.put(data);
//                buffer.setWritten(true);
//                buffers.put(id.getIdString(),buffer);
//                currentCount++;
//                return true;
//            }else{
//                for(Map.Entry<String, BlockBuffer> entry : buffers.entrySet()){
//                    if(entry.getValue().isWritable()){
//                        BlockBuffer buffer = entry.getValue();
//                        buffer.put(data);
//                        buffer.setWritten(true);
//                        buffers.remove(entry.getKey());
//                        buffers.put(id.getIdString(),buffer);
//                        return true;
//                    }
//                }
//                return false;
//            }
//        }else{
//            if(blockBuffer.isWritable()){
//                blockBuffer.put(data);
//                blockBuffer.setWritten(true);
//                return true;
//            }else{
//                return false;
//            }
//        }
    }

    public void startWrite() throws WriteBufferToBlockException {
        Exception exception = null;
        for(Map.Entry<String, BlockBuffer> entry : buffers.entrySet()){
            if(!entry.getValue().isWritable()){
                BlockBuffer buffer = entry.getValue();
                byte[] bytes = buffer.get();
                buffer.setWritten(false);
                try {
                    BlockIndexIdWithManagerId id = new BlockIndexIdWithManagerId(entry.getKey());
                    String pathPrefix = Properties.BLOCK_PATH + "/" + id.getBlockManagerId().getIdString() + "/" + id.getBlockIndexId().getIdString();
                    AtomicBlockWriter.writeBlockMetaAndDataAtomic(
                            new DefaultBlockMeta(bytes.length,Properties.BLOCK_SIZE, EncryptUtil.md5(bytes)),
                            pathPrefix + ".meta",
                            new DefaultBlockData(bytes),
                            pathPrefix + ".data"
                            );
                } catch (BlockIndexIdWithManagerIdFormatException | IOException e) {
                    exception = e;//make sure that all the buffers are tested to write out;
                }

            }
        }
        if(exception != null){
            throw new WriteBufferToBlockException(exception.getMessage());
        }
    }
}
