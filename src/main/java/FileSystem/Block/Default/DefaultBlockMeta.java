package FileSystem.Block.Default;

import FileSystem.Block.BlockMeta;

/**
 * @author : chara
 */
public class DefaultBlockMeta implements BlockMeta {

    private int size;
    private int realSize;
    private String checkSum;

    public DefaultBlockMeta(int size, int realSize, String checkSum) {
        this.size = size;
        this.realSize = realSize;
        this.checkSum = checkSum;
    }

    @Override
    public int getRealSize() {
        return realSize;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public String getCheck() {
        return checkSum;
    }
}
