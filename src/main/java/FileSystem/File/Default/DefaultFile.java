package FileSystem.File.Default;

import FileSystem.Application.Application;
import FileSystem.Block.Block;
import FileSystem.Block.Default.BlockIndexIdWithManagerId;
import FileSystem.Buffer.BlockBuffer;
import FileSystem.Exception.BlockException.BlockIndexIdWithManagerIdFormatException;
import FileSystem.Exception.BlockException.AllocateNewBlockFailedException;
import FileSystem.Exception.FileException.CorruptedFileException;
import FileSystem.Exception.FileException.IllegalCursorException;
import FileSystem.File.File;
import FileSystem.File.FileMetaData;
import FileSystem.Manager.FileManager;
import FileSystem.ReaderWriter.AtomicFileWriter;
import FileSystem.Util.Id;
import FileSystem.Util.Properties;
import FileSystem.ReaderWriter.ReaderWriterUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;

/**
 * @author : chara
 */
public class DefaultFile implements File {

    private final static Logger LOGGER = LogManager.getLogger(DefaultFile.class);

    private final String fileMetaDataPath;
    private long cur;
    private final FileManager fileManager;
    private final Id id;

    public DefaultFile(String fileMetaDataPath, FileManager fileManager, Id id) {
        this.fileMetaDataPath = fileMetaDataPath;
        this.id = id;
        this.cur = 0;
        this.fileManager = fileManager;

    }

    @Override
    public void initializeFile() throws IOException {
        FileMetaData fileMetaData = new DefaultFileMetaData(0, Properties.BLOCK_SIZE, new LinkedList<>());
        try {
            AtomicFileWriter.writeFileMeta(fileMetaData,fileMetaDataPath);
        } catch (IOException e) {
            LOGGER.error("initialize file failed");
            throw e;
        }
    }

    @Override
    public Id getFileId() {
        return id;
    }

    @Override
    public FileManager getFileManager() {
        return fileManager;
    }

    /**
     * @fixed{there is a problem with when data is not enough to fill the array of length @param{length}}
     *
     * @param length the length to read
     * @return the data read, maybe the length is shorter than @param{length} length when there is not enough data remaining
     */
    @Override
    public byte[] read(int length) throws CorruptedFileException, IOException{
        FileMetaData fileMetaData = null;
        try {
            fileMetaData = ReaderWriterUtil.readFileMetaData(fileMetaDataPath);
        } catch (IOException e) {
            LOGGER.error("read failed due to read fileMeta failed");
            throw e;
        } catch(BlockIndexIdWithManagerIdFormatException e){
            throw new CorruptedFileException(e.getMessage());
        }
        byte[] result = new byte[length];
        int pos = 0;
        int[] blockCountAndStartPlace = getBlockCountAndStartPlace(cur, fileMetaData);
        if(cur >= fileMetaData.getSize()){
            return new byte[0];
        }
        int blockCount = blockCountAndStartPlace[0];
        int startPos = blockCountAndStartPlace[1];
        //TODO there maybe bug here, need attention
        try{
            while(pos < length && cur < fileMetaData.getSize()){
                boolean flag = false;//will be true if some block is valid
                for(BlockIndexIdWithManagerId blockIndexIdWithManagerId : fileMetaData.getBlocks().get(blockCount)){
                    byte[] bytes;
                    /*
                        add buffer support
                     */
                    BlockBuffer buffer = Application.bufferManager.getBuffer(blockIndexIdWithManagerId);
                    if(buffer != null){
                        bytes = buffer.get();
                    }else{
                        Block block = Application.blockManagerController.getBlock(blockIndexIdWithManagerId);
                        bytes = block.read();
                    }
                    if(bytes == null){
                        continue;
                    }
                    flag = true;
                    for(int i = startPos;i < bytes.length && pos < length;i++){
                        result[pos++] = bytes[i];
                        cur++;
                    }
                    blockCount += 1;
                    break;
                }
                if(!flag){
                    throw new CorruptedFileException("file " + id.getIdString() + " is corrupted");
                }
                startPos = 0;//after the first block,all start will be the start of the block;
            }
        } catch (IOException e) {
            LOGGER.error("read failed due to read block failed");
            throw e;
        }

        if(pos < length){
            byte[] temp = new byte[pos];
            if (pos >= 0) System.arraycopy(result, 0, temp, 0, pos);
            result = temp;
        }
        return result;
    }

    @Override
    public void write(byte[] b) throws IOException, AllocateNewBlockFailedException, CorruptedFileException{
        FileMetaData fileMetaData;
        try {
            fileMetaData = ReaderWriterUtil.readFileMetaData(fileMetaDataPath);
        } catch (IOException e) {
            LOGGER.error("write failed, {}",e.getMessage());
            throw e;
        } catch (BlockIndexIdWithManagerIdFormatException e) {
            throw new CorruptedFileException(e.getMessage());
        }
        int[] blockCountAndStartPlace = getBlockCountAndStartPlace(cur, fileMetaData);
        int blockCount = blockCountAndStartPlace[0];
        List<List<BlockIndexIdWithManagerId>> blocks = fileMetaData.getBlocks();
        if(blockCount >= blocks.size()){
            int pos = 0;
            while(pos < b.length){
                List<BlockIndexIdWithManagerId> list = new ArrayList<>();
                byte[] temp = new byte[Math.min(Properties.BLOCK_SIZE,b.length - pos)];
                for(int i = 0;i < temp.length;i++){
                    temp[i] = b[pos++];
                    cur++;
                }
                allocateBlock(list,temp);
                blocks.add(list);
            }
        }else{
            long curTemp = blockCountAndStartPlace[1];
//            long curTemp = cur - blockCount * Properties.BLOCK_SIZE;
//            cur = blockCount * Properties.BLOCK_SIZE;//safe move;
            cur = cur - curTemp;
            try {
                byte[] bytes = read(Properties.BLOCK_SIZE);
                blocks.remove(blockCount);
                int pos1 = 0;//for the array bytes
                int pos2 = 0;//for the array b
                int read = 0;//record how much bytes have been read
                while(pos2 < b.length || pos1 < bytes.length){
                    List<BlockIndexIdWithManagerId> list = new ArrayList<>();
                    byte[] temp = new byte[Math.min(Properties.BLOCK_SIZE,b.length + bytes.length - pos1 - pos2)];
                    for(int i = 0;i < temp.length;i++){
                        if(i + read < curTemp){
                            temp[i] = bytes[pos1++];
                        }else if((i + read - curTemp) < b.length){
                            temp[i] = b[pos2++];
                        }else{
                            temp[i] = bytes[pos1++];
                        }
                    }
                    allocateBlock(list, temp);
                    // in the while loop,the blockCount is already not in use for the position of the previous block,
                    // thus use it as a pointer in the list
                    blocks.add(blockCount++,list);
                    read += temp.length;
                }
            } catch (CorruptedFileException e) {
                LOGGER.error("file corrupted, {}",e.getMessage());
                throw e;
            } catch (IOException e) {
                LOGGER.error("write failed");
                throw e;
            }
        }
        /**TODO
         * need to deal with the exception to maintain consistency
         * if any exception occurs, this two wont be executed thus wont modify the file
         * currently, if write the fileMeta failed then problem occurs
        **/
        fileMetaData.setSize(fileMetaData.getSize() + b.length);
        ReaderWriterUtil.writeFileMetaData(fileMetaData,fileMetaDataPath);

    }

    private void allocateBlock(List<BlockIndexIdWithManagerId> list, byte[] temp) throws AllocateNewBlockFailedException {
        for(int j = 0, k = 0; j < Properties.BACK_UP_COUNT && k < Properties.BACK_UP_COUNT * 2; j++,k++){
            Block newBlock = Application.blockManagerController.newBlock(temp);
            if(newBlock == null){
                if(j == 0 && k == Properties.BACK_UP_COUNT * 2 - 1){
                    LOGGER.error("allocate new Block failed");
                    throw new AllocateNewBlockFailedException("allocate block failed");
                }
                j--;
                continue;
            }
            list.add(new BlockIndexIdWithManagerId(newBlock.getIndexId(),newBlock.getBlockManager().getId()));
        }
    }


    /**
     *
     * @param cursor the cursor
     * @param fileMetaData to get the blocks
     * @return int[2]
     * the first is the block the current cursor is in;
     * the second is the place in the block(if cursor is not in any block,then cursor will be returned)
     * @throws IOException
     */
    private int[] getBlockCountAndStartPlace(long cursor,FileMetaData fileMetaData) throws IOException {
        int blockCount = 0;
        int total = 0;
        List<List<BlockIndexIdWithManagerId>> blocks = fileMetaData.getBlocks();
        if(blocks.size() == 0){
            return new int[]{0, (int) cursor};
        }
        int lastBlockSize = getLastBlockSize(blockCount, blocks);
        total += lastBlockSize;
        while(total <= cursor){
            blockCount++;
            if(blocks.size() == blockCount){
                return new int[]{blockCount, (int) cursor};
            }
            lastBlockSize = getLastBlockSize(blockCount, blocks);
            total += lastBlockSize;
        }
        return new int[]{blockCount, (int) (cursor - total + lastBlockSize)};
    }

    private int getLastBlockSize(int blockCount, List<List<BlockIndexIdWithManagerId>> blocks) throws IOException {
        int lastBlockSize = -1;
        for(BlockIndexIdWithManagerId id : blocks.get(blockCount)){
            if(Application.bufferManager.containsBuffer(id)){
                lastBlockSize = Application.bufferManager.getBuffer(id).get().length;
                break;
            }
        }
        if(lastBlockSize == -1){
            //TODO
            //here assumes that meta is always right
           lastBlockSize = Application.blockManagerController.getBlock(blocks.get(blockCount).get(0)).getRealSize();
        }
        return lastBlockSize;
    }


    @Override
    public long pos() {
        return cur;
    }

    @Override
    public long move(long offset, int where) throws IllegalCursorException, IOException, CorruptedFileException {
        switch (where){
            case MOVE_CURR:
                if(cur + offset > size() || cur + offset < 0){
                    throw new IllegalCursorException("illegal cursor place");
                }
                cur = cur + offset;
                break;
            case MOVE_TAIL:
                long pos = size() + offset;
                if(offset > 0 || pos < 0){
                    throw new IllegalCursorException("illegal cursor place");
                }
                cur = pos;
                break;
            case MOVE_HEAD:
                if(offset > size() || offset < 0){
                    throw new IllegalCursorException("illegal cursor place");
                }
                cur = offset;
                break;
            default:
                throw new IllegalCursorException("illegal cursor place");
        }
        return cur;
    }

    @Override
    public void close() {

    }

    /**
     *
     * @return the size of the file
     * @throws IOException when read failed
     */
    @Override
    public long size() throws IOException,  CorruptedFileException {
        try {
            FileMetaData fileMetaData = ReaderWriterUtil.readFileMetaData(fileMetaDataPath);
            return fileMetaData.getSize();
        } catch (IOException e) {
            LOGGER.error("getSize failed due to read fileMeta failed");
            throw e;
        } catch (BlockIndexIdWithManagerIdFormatException e) {
            throw new CorruptedFileException(e.getMessage());
        }
    }

    @Override
    public void setSize(long newSize) throws IOException, CorruptedFileException, AllocateNewBlockFailedException {
        long size = 0;
        try {
            size = size();
        } catch (IOException e) {
            LOGGER.error("setSize failed due to read IO fail");
            throw e;
        } catch (CorruptedFileException e) {
            LOGGER.error("setSize failed due to fileMeta corrupted");
            throw e;
        }
        if(newSize > size){
            long curTemp = cur;
            cur = size;
            long left = newSize - size;
            while(left > 0){
                byte[] bytes = new byte[Math.min(Properties.BLOCK_SIZE,(int)(newSize - size))];
                try {
                    write(bytes);
                } catch (IOException | CorruptedFileException | AllocateNewBlockFailedException e) {
                    LOGGER.error("setSize failed,{}",e.getMessage());
                    throw e;
                }
                left -= bytes.length;
            }
            cur = curTemp;
        }else {
            if (newSize != size) {
                if(cur > newSize){
                    cur = newSize;
                }
                FileMetaData fileMetaData = null;
                try {
                    fileMetaData = ReaderWriterUtil.readFileMetaData(fileMetaDataPath);
                } catch (IOException e) {
                    LOGGER.error("setSize failed,{}",e.getMessage());
                    throw e;
                } catch (BlockIndexIdWithManagerIdFormatException e) {
                    LOGGER.error("setSize failed,{}",e.getMessage());
                    throw new CorruptedFileException(e.getMessage());
                }
                List<List<BlockIndexIdWithManagerId>> blocks = fileMetaData.getBlocks();
                int[] blockCountAndStartPlace = getBlockCountAndStartPlace(newSize, fileMetaData);
                while(blockCountAndStartPlace[0] + 1 < blocks.size()){
                    blocks.remove(blockCountAndStartPlace[0] + 1);
                }
                fileMetaData.setSize(newSize);
                ReaderWriterUtil.writeFileMetaData(fileMetaData,fileMetaDataPath);
            }
        }
    }
}
