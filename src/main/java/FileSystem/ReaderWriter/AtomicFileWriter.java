package FileSystem.ReaderWriter;

import FileSystem.File.FileMetaData;

import java.io.IOException;

/**
 * @author : chara
 * not really atomic(
 */
public class AtomicFileWriter {
    public static void writeFileMeta(FileMetaData fileMetaData, String path) throws IOException {
        try {
            ReaderWriterUtil.writeFileMetaData(fileMetaData,path);
        } catch (IOException e){
            java.io.File f = new java.io.File(path);
            if(f.exists()){
                for(int i = 0;i < 5;i++){
                    if(f.delete())
                        break;
                }
            }
            throw e;
        }
    }
}
