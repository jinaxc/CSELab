package FileSystem.Buffer;

/**
 * @author : chara
 */
public class BlockBuffer implements Buffer<byte[]> {

    private boolean isWritten;
    private byte[] data;

    public BlockBuffer() {
    }

    public BlockBuffer(byte[] data) {
        this.data = data.clone();
    }

    public void setWritten(boolean written) {
        isWritten = written;
    }

    @Override
    public void put(byte[] data){
        this.data = data.clone();
    }


    @Override
    public byte[] get() {
        return data.clone();
    }

    @Override
    public boolean isWritable() {
        return isWritten;
    }


    @Override
    public boolean isValid() {
        return true;
    }
}
