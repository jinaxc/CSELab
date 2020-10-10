package FileSystem.Util;

/**
 * @author : chara
 */
public class Properties {
    public static final boolean APPLICATION_MODE = true;
    public static final int BLOCK_SIZE = 2;
    public static final int BACK_UP_COUNT = 3;
    public static final char BLOCK_ID_SPLITTER = ';';
    public static final char FILE_MANAGER_ID_AND_FILE_ID_SPLITTER = '-';
    public static final char BLOCK_MANAGER_ID_AND_BLOCK_ID_SPLITTER = '-';
    public static final String BLOCK_PATH = APPLICATION_MODE ? "./BLOCK/" : "src/main/resources/Block/";
    public static final String FILE_PATH = APPLICATION_MODE ? "./FileMeta/" :"src/main/resources/FileMeta/";

}
