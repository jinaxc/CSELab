package FileSystem.Buffer;

import FileSystem.Exception.BufferException.WriteBufferToBlockException;
import FileSystem.Util.Id;

/**
 * @author : chara
 */

/**
 *
 * @param <T1> BufferType
 * @param <T2> the Id the identify the data in the buffer
 * @param <T3> the real dataType in the buffer
 */
public interface BufferManager<T1 extends Buffer<T3>,T2 extends Id,T3> {
    T1 getBuffer(T2 id);
    /**
     * should return false if the buffer is invalid
     * @param id to find the buffer
     * @return true if contains the buffer
     */
    boolean containsBuffer(T2 id);
    boolean putBuffer(T2 id,T3 data) throws WriteBufferToBlockException;
    boolean writeBuffer(T2 id,T3 data);
}
