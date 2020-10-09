package FileSystem.Util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author : https://segmentfault.com/a/1190000021745696
 */
public class EncryptUtil {
    public static String md5(byte[] bytes){
        char hexDigits[] = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        try{
            byte[] input = bytes;
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input);
            byte[] digest = md.digest();
            int j = digest.length;
            char result[] = new char[j * 2];
            int k = 0;
            for(int i = 0;i < j;i++){
                byte byte0 = digest[i];
                result[k++] = hexDigits[byte0 >>> 4 & 0xf];
                result[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(result);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String md5(String str){
        return md5(str.getBytes());
    }
}
