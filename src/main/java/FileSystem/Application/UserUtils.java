package FileSystem.Application;

import FileSystem.Block.Block;
import FileSystem.Exception.AllocateNewBlockFailedException;
import FileSystem.Exception.CorruptedFileException;
import FileSystem.Exception.IllegalCursorException;
import FileSystem.File.File;
import FileSystem.Util.Properties;

import java.io.IOException;

/**
 * @author : chara
 * the methods here should only be invoded by Application.main when interacting with User
 * if others want to use these methods, he must make sure that the file or block argument have already been checked
 */
public class UserUtils {
    public static String smartCat(File file) throws IOException, CorruptedFileException{
//        try {
//            file.move(0,1);
//        } catch (IllegalCursorException e) {
//            //Impossible catch
//        }
        StringBuilder returnValue = new StringBuilder();
        long left = file.size();
        while(left > 0){
            byte[] bytes = file.read(Properties.BLOCK_SIZE);
            returnValue.append(new String(bytes));
            if(bytes.length == 0){
//                returnValue.append("\n" +
//                                   " not enough bytes left\040\n");
                return returnValue.toString();
            }
            left -= bytes.length;
        }
        return returnValue.toString();
    }
    public static String smartHex(Block block) throws IOException {
        byte[] bytes = block.read();
        String strHex;
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            strHex = Integer.toHexString(aByte & 0xFF);
            sb.append((strHex.length() == 1) ? "0" + strHex : strHex); // 每个字节由两个字符表示，位数不够，高位补0
        }
        return sb.toString() + "\n";
    }

    public static String smartWrite(File file, int index,String data) throws IllegalCursorException, AllocateNewBlockFailedException, IOException, CorruptedFileException {
        file.move(index,File.MOVE_HEAD);
        file.write(data.getBytes());
        return "";
    }
    public static String smartCopy(File from, File to) throws IOException, CorruptedFileException, AllocateNewBlockFailedException {
        byte[] bytes = from.read(Properties.BLOCK_SIZE);
        while(bytes.length > 0){
            to.write(bytes);
            bytes = from.read(Properties.BLOCK_SIZE);
        }
        return "";
    }
}
