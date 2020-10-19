package FileSystem.Application;

import FileSystem.Block.Block;
import FileSystem.Exception.BlockException.AllocateNewBlockFailedException;
import FileSystem.Exception.FileException.CorruptedFileException;
import FileSystem.Exception.FileException.IllegalCursorException;
import FileSystem.File.File;
import FileSystem.Util.Properties;

import java.io.IOException;

/**
 * @author : chara
 * the methods here should only be invoded by Application.main when interacting with User
 * if others want to use these methods, he must make sure that the file or block argument have already been checked
 */
public class UserUtils {
    public static void smartCat(File file) throws IOException, CorruptedFileException{
//        try {
//            file.move(0,1);
//        } catch (IllegalCursorException e) {
//            //Impossible catch
//        }
        long left = file.size();
        while(left > 0){
            byte[] bytes = file.read(Properties.BLOCK_SIZE);
            System.out.print(new String(bytes));
            if(bytes.length == 0){
//                System.out.println();
//                System.out.println("not enough bytes left");
                return;
            }
            left -= bytes.length;
        }
        System.out.println();
    }
    public static void smartHex(Block block) throws IOException {
        byte[] bytes = block.read();
        String strHex;
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            strHex = Integer.toHexString(aByte & 0xFF);
            sb.append((strHex.length() == 1) ? "0" + strHex : strHex); // 每个字节由两个字符表示，位数不够，高位补0
        }
        System.out.println(sb.toString().trim());
    }

    public static void smartWrite(File file, int index,String data) throws IllegalCursorException, AllocateNewBlockFailedException, IOException, CorruptedFileException {
        file.move(index,File.MOVE_HEAD);
        file.write(data.getBytes());
    }
    public static void smartCopy(File from, File to) throws IOException, CorruptedFileException, AllocateNewBlockFailedException {
        byte[] bytes = from.read(Properties.BLOCK_SIZE);
        while(bytes.length > 0){
            to.write(bytes);
            bytes = from.read(Properties.BLOCK_SIZE);
        }
    }
}
