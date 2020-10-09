package FileSystem.Buffer;

import FileSystem.Util.Id;

import java.io.IOException;

/**
 *
 * @param <T1> the dataType in the buffer
 */
public interface Buffer<T1> {
    void put(T1 data);
    T1 get();
    boolean isWritable();
    boolean isValid();
}
