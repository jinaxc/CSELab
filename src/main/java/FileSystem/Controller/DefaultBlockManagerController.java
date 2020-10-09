package FileSystem.Controller;

import FileSystem.Block.Block;
import FileSystem.Block.Default.BlockIndexIdWithManagerId;
import FileSystem.Exception.InitiationFailedException;
import FileSystem.Manager.BlockManager;
import FileSystem.Manager.Default.BlockManagerId;
import FileSystem.Manager.Default.DefaultBlockManager;
import FileSystem.Manager.Default.DefaultFileManager;
import FileSystem.Manager.Default.FileManagerId;
import FileSystem.Util.Properties;

import java.util.List;
import java.util.Random;

/**
 * @author : chara
 */
public class DefaultBlockManagerController implements BlockManagerController {
    private final int COUNT;
    private BlockManager[] managers;
    private Random r = new Random();

    public DefaultBlockManagerController(int COUNT) throws InitiationFailedException {
        this.COUNT = COUNT;
        managers = new BlockManager[COUNT];
        java.io.File f = new java.io.File(Properties.BLOCK_PATH);
        if(!f.exists()){
            if(!f.mkdir()){
                throw new InitiationFailedException("can't mkdir");
            }
        }
        for(int i = 0;i < COUNT;i++){
            managers[i] = new DefaultBlockManager(new BlockManagerId(i));
        }
    }

    public DefaultBlockManagerController(List<BlockManager> managers){
        this.COUNT = managers.size();
        this.managers = managers.toArray(new BlockManager[0]);
    }

    @Override
    public Block getBlock(BlockIndexIdWithManagerId id) {
        return managers[(int) id.getBlockManagerId().getId()].getBlock(id.getBlockIndexId());
    }

    /**
     *
     * @param bytes
     * @return null if the block manager returns null
     */
    @Override
    public Block newBlock(byte[] bytes) {
        return managers[r.nextInt(5)].newBlock(bytes);
    }

    @Override
    public BlockManager getBlockManager(BlockManagerId id) {
        return managers[(int) id.getId()];
    }
}

