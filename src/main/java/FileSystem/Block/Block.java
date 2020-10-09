package FileSystem.Block;

import FileSystem.Manager.BlockManager;
import FileSystem.Util.Id;

import java.io.IOException;

public interface Block {
    Id getIndexId();
    BlockManager getBlockManager();
    byte[] read() throws IOException;
    int getRealSize() throws IOException;
    String getCheck() throws IOException;
    int blockSize() throws IOException;
}
