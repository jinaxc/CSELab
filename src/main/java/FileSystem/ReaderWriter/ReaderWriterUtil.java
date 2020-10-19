package FileSystem.ReaderWriter;

import FileSystem.Block.BlockData;
import FileSystem.Block.BlockMeta;
import FileSystem.Block.Default.BlockIndexIdWithManagerId;
import FileSystem.Exception.BlockException.BlockIndexIdWithManagerIdFormatException;
import FileSystem.Block.Default.DefaultBlockData;
import FileSystem.Block.Default.DefaultBlockMeta;
import FileSystem.File.Default.DefaultFileMetaData;
import FileSystem.File.FileMetaData;
import FileSystem.Util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author : chara
 */
public class ReaderWriterUtil {

    private final static Logger LOGGER = LogManager.getLogger(ReaderWriterUtil.class);

    public static BlockMeta readBlockMeta(String path) throws IOException {
        BufferedReader br = null;
        try{
            br = new BufferedReader(new FileReader(path));
            int size = Integer.parseInt(br.readLine());
            int realSize = Integer.parseInt(br.readLine());
            String check = br.readLine();
            return new DefaultBlockMeta(size,realSize,check);
        } catch (IOException e) {
            LOGGER.error("read block meta failed, path is {}",path);
            throw e;
        }finally {
            if(br != null){
                try {
                    br.close();
                } catch (IOException e) {
                    LOGGER.warn("close reader for block meta failed, path is {}",path);
                }
            }
        }
    }
    public static BlockData readBlockData(String path) throws IOException {
        BufferedInputStream bis = null;
        try{
            bis = new BufferedInputStream(new FileInputStream(path));
            byte[] bytes = bis.readAllBytes();
            return new DefaultBlockData(bytes);
        } catch (IOException e) {
            LOGGER.error("read block data failed, path is {}",path);
            throw e;
        }finally {
            try {
                if(bis != null)
                    bis.close();
            } catch (IOException e) {
                LOGGER.warn("close reader for blockData, path is {}",path);
            }
        }
    }

    public static FileMetaData readFileMetaData(String path) throws IOException, BlockIndexIdWithManagerIdFormatException {
        BufferedReader br = null;
        try{
            br = new BufferedReader(new FileReader(path));
            int size = Integer.parseInt(br.readLine());
            int blockSize = Integer.parseInt(br.readLine());
            List<List<BlockIndexIdWithManagerId>> blocks = new LinkedList<>();
            String str;
            while((str = br.readLine()) != null){
                String[] split = str.split(Properties.BLOCK_ID_SPLITTER + "");
                List<BlockIndexIdWithManagerId> list = new ArrayList<>();
                for(String s : split){
                    BlockIndexIdWithManagerId newId;
                    try{
                        newId = new BlockIndexIdWithManagerId(s);
                    }catch (BlockIndexIdWithManagerIdFormatException e){
                        //TODO this exception needs to be dealt with
                        LOGGER.fatal("file meta broken, invalid block id");
                        throw e;
                    }
                    list.add(newId);
                }
                blocks.add(list);
            }
            return new DefaultFileMetaData(size,blockSize,blocks);
        } catch (IOException e) {
            LOGGER.error("read file meta failed, path is {}",path);
            throw e;
        }finally {
            if(br != null){
                try {
                    br.close();
                } catch (IOException e) {
                    LOGGER.warn("close reader for fileMeta failed, path is {}",path);
                }
            }
        }
    }


    public static void writeBlockMeta(BlockMeta blockMeta,String path) throws IOException {
        BufferedWriter writer = null;
        try {
            writer = (new BufferedWriter(new FileWriter(path)));
            writer.write(String.valueOf(blockMeta.getSize()));
            writer.newLine();
            writer.write(String.valueOf(blockMeta.getRealSize()));
            writer.newLine();
            writer.write(blockMeta.getCheck());
            writer.flush();
        } catch (IOException e) {
            LOGGER.error("write block meta failed, path is {}",path);
            throw e;
        }finally {
            try {
                if(writer != null)
                    writer.close();
            } catch (IOException e) {
                LOGGER.warn("close writer for blockMeta failed, path is {}",path);
            }
        }
    }

    public static void writeBlockData(BlockData blockData,String path) throws IOException {
        BufferedOutputStream bos = null;
        try{
            bos = new BufferedOutputStream(new FileOutputStream(path));
            bos.write(blockData.getData());
        } catch (IOException e) {
            LOGGER.error("write block data failed, path is {}",path);
            throw e;
        }finally {
            try {
                if(bos != null)
                    bos.close();
            } catch (IOException e) {
                LOGGER.warn("close writer for blockData failed, path is {}",path);
            }
        }
    }

    public static void writeFileMetaData(FileMetaData fileMetaData,String path) throws IOException {
        BufferedWriter writer = null;
        try {
            writer = (new BufferedWriter(new FileWriter(path)));
            writer.write(String.valueOf(fileMetaData.getSize()));
            writer.newLine();
            writer.write(String.valueOf(fileMetaData.getBlockSize()));
            writer.flush();
            for(List<BlockIndexIdWithManagerId> list: fileMetaData.getBlocks()){
                writer.newLine();
                StringBuilder sb = new StringBuilder();
                for(BlockIndexIdWithManagerId b : list){
                    sb.append(b.getIdString()).append(Properties.BLOCK_ID_SPLITTER + "");
                }
                writer.write(sb.toString());
                writer.flush();
            }
        } catch (IOException e) {
            LOGGER.error("write fileMeta failed, path is {}",path);
            throw e;
        }finally {
            try {
                if(writer != null)
                    writer.close();
            } catch (IOException e) {
                LOGGER.warn("close writer for fileMeta failed, path is {}",path);
            }
        }
    }
}
