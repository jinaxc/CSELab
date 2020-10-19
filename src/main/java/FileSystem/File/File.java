package FileSystem.File;

import FileSystem.Exception.BlockException.AllocateNewBlockFailedException;
import FileSystem.Exception.FileException.CorruptedFileException;
import FileSystem.Exception.FileException.IllegalCursorException;
import FileSystem.Manager.FileManager;
import FileSystem.Util.Id;

import java.io.IOException;

public interface File {
    int MOVE_CURR = 0;
    int MOVE_HEAD = 1;
    int MOVE_TAIL = 2;

    void initializeFile() throws IOException;

    Id getFileId();
    FileManager getFileManager();
    byte[] read(int length) throws CorruptedFileException, IOException;
    void write(byte[] b) throws CorruptedFileException, IOException, AllocateNewBlockFailedException;
//    default long pos() {
//        return move(0, MOVE_CURR);
//    }
    long pos();
    long move(long offset, int where) throws IllegalCursorException, IOException, CorruptedFileException;
    //使⽤buffer的同学需要实现
    void close();
    long size() throws IOException,  CorruptedFileException;
    void setSize(long newSize) throws IOException, CorruptedFileException, AllocateNewBlockFailedException;
}
