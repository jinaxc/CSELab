package FileSystem.ReaderWriter;

import FileSystem.Block.BlockData;
import FileSystem.Block.BlockMeta;
import FileSystem.Exception.InitiationFailedException;
import FileSystem.Util.Properties;

import java.io.IOException;

/**
 * @author : chara
 * not really atomic(
 */
public class AtomicBlockWriter {
    /**
     * try to delete the file created when write failed
     * @param blockMeta
     * @param metaPath
     * @param blockData
     * @param dataPath
     * @throws IOException
     */
    public static void writeBlockMetaAndDataAtomic(BlockMeta blockMeta, String metaPath, BlockData blockData, String dataPath) throws IOException {
        try{
            ReaderWriterUtil.writeBlockMeta(blockMeta,metaPath);
            ReaderWriterUtil.writeBlockData(blockData,dataPath);
        } catch (IOException e){
            java.io.File f1 = new java.io.File(metaPath);
            java.io.File f2 = new java.io.File(dataPath);
            if(f1.exists()){
                for(int i = 0;i < 5;i++){
                    if(f1.delete())
                        break;
                }
            }
            if(f2.exists()){
                for(int i = 0;i < 5;i++){
                    if(f2.delete())
                        break;
                }
            }
            throw e;
        }

    }
}
