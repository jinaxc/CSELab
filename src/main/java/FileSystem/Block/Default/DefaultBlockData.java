package FileSystem.Block.Default;

import FileSystem.Block.BlockData;

/**
 * @author : chara
 */
public class DefaultBlockData implements BlockData {
    byte[] data;

    public DefaultBlockData(byte[] data) {
        this.data = data;
    }

    @Override
    public byte[] getData() {
        return data;
    }
}
