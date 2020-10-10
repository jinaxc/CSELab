package FileSystem.Application;

import FileSystem.Block.Block;
import FileSystem.Block.Default.BlockIndexId;
import FileSystem.Block.Default.BlockIndexIdWithManagerId;
import FileSystem.Buffer.BlockBufferManager;
import FileSystem.Exception.BlockIndexIdWithManagerIdFormatException;
import FileSystem.Block.Default.DefaultBlock;
import FileSystem.Controller.BlockManagerController;
import FileSystem.Controller.DefaultBlockManagerController;
import FileSystem.Controller.DefaultFileManagerController;
import FileSystem.Controller.FileManagerController;
import FileSystem.Exception.*;
import FileSystem.File.Default.DefaultFile;
import FileSystem.File.Default.FileId;
import FileSystem.File.Default.FileIdWithManagerId;
import FileSystem.File.File;
import FileSystem.Manager.BlockManager;
import FileSystem.Manager.Default.BlockManagerId;
import FileSystem.Manager.Default.DefaultBlockManager;
import FileSystem.Manager.Default.DefaultFileManager;
import FileSystem.Manager.Default.FileManagerId;
import FileSystem.Manager.FileManager;
import FileSystem.Util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author : chara
 */
public class Application {
    private final static Logger LOGGER = LogManager.getLogger(Application.class);
    private static Application application;
    public BlockManagerController blockManagerController;
    public FileManagerController fileManagerController;
    public BlockBufferManager bufferManager;
    private Map<String,String> commandFormatHelpers;

    private Application(){
    }

    public static synchronized void startApplication() throws InitiationFailedException {
        if(application == null) {
            application = new Application();
            application.initialize();
        }
    }

    public static synchronized Application getApplication(){
        return application;
    }

    private void initialize() throws InitiationFailedException {
//        returnValue += "please make sure that there aren't directories named Block and FileMetaData in this directory(directories made by this app is OK)";
        initializeBlockController();
        initializeFileController();

        bufferManager = new BlockBufferManager(10);
        commandFormatHelpers = new HashMap<>();
        commandFormatHelpers.put("newFile","new-file||newFile file_id");
        commandFormatHelpers.put("read","read file_id length");
        commandFormatHelpers.put("write","write file_id data");
        commandFormatHelpers.put("pos","pos file_id");
        commandFormatHelpers.put("move","move file_id index cursor_place(0 denotes the current cursor,1 denotes head of the file,2 denote tail of the file)");
        commandFormatHelpers.put("size","size file_id");
        commandFormatHelpers.put("close","close file_id");
        commandFormatHelpers.put("setSize","set-size||setSize file_id new_size");
        commandFormatHelpers.put("smartCat","smart-cat||smartCat file_id");
        commandFormatHelpers.put("smartHex","smart-hex||smartHex block_id");
        commandFormatHelpers.put("smartWrite","smart-write||smartWrite file_id index(from the head of the file) data");
        commandFormatHelpers.put("smartCopy","smart-copy||smartCopy file_id_from file_id_to");
    }

    private void initializeFileController() throws InitiationFailedException {
        try {
            java.io.File fileDir = new java.io.File(Properties.FILE_PATH);
            java.io.File[] fmDirs = fileDir.listFiles();
            if(fmDirs != null && fmDirs.length != 0){
                List<FileManager> fileManagerList = new ArrayList<>();
                //add managers to the controller
                for(java.io.File fmDir : fmDirs){
                    DefaultFileManager manager;
                    try{
                        manager = new DefaultFileManager(new FileManagerId(fmDir.getName()));
                    }catch (IllegalArgumentException | FileManagerIdFormatException e){
                        throw new InitiationFailedException(e.getMessage());
                    }
                    java.io.File[] fileFiles = fmDir.listFiles();
                    //see if the manager has any block
                    if(fileFiles != null){
                        Map<Long, File> files = new HashMap<>();
                        String relativePath = Properties.FILE_PATH + fmDir.getName() + "/f";
                        List<String> fileNames = Arrays.stream(fileFiles)
                                .map((file -> file.getName().substring(1)))
                                .sorted()
                                .collect(Collectors.toList());
                        //add blocks to the manager
                        for(String fileName : fileNames){
                            File file = new DefaultFile(relativePath + fileName,manager,new FileId(Integer.parseInt(fileName)));
                            files.put((long) Integer.parseInt(fileName),file);
                        }
                        manager.setFiles(files);
                    }
                    fileManagerList.add(manager);
                }
                fileManagerController = new DefaultFileManagerController(fileManagerList);
            }else{
                fileManagerController = new DefaultFileManagerController(5);
            }
        } catch (InitiationFailedException e) {
            LOGGER.fatal("initialize failed, {}",e.getMessage());
            throw e;
        }
    }

    private void initializeBlockController() throws InitiationFailedException {
        try {
            java.io.File blockDir = new java.io.File(Properties.BLOCK_PATH);
            java.io.File[] bmDirs = blockDir.listFiles();
            if(bmDirs != null && bmDirs.length != 0){
                List<BlockManager> blockManagerList = new ArrayList<>();
                //add managers to the controller
                for(java.io.File bmDir : bmDirs){
                    DefaultBlockManager manager;
                    try{
                        manager = new DefaultBlockManager(new BlockManagerId(bmDir.getName()));
                    }catch (IllegalArgumentException | BlockManagerIdFormatException e){
                        throw new InitiationFailedException(e.getMessage());
                    }
                    java.io.File[] blockFiles = bmDir.listFiles();
                    //see if the manager has any block
                    if(blockFiles != null){
                        Map<Long, Block> blocks = new HashMap<>();
                        String relativePath = Properties.BLOCK_PATH + bmDir.getName() + "/b";
                        List<String> dataNames = Arrays.stream(blockFiles)
                                .filter((file -> file.getName().endsWith(".data")))
                                .map((file -> file.getName().split("\\.")[0].substring(1)))
                                .sorted()
                                .collect(Collectors.toList());
                        List<String> metaNames = Arrays.stream(blockFiles)
                                .filter((file -> file.getName().endsWith(".meta")))
                                .map((file -> file.getName().split("\\.")[0].substring(1)))
                                .sorted()
                                .collect(Collectors.toList());
                        //add blocks to the manager
                        for(String blockName : dataNames){
                            if(metaNames.contains(blockName)){
                                Block b = new DefaultBlock(relativePath + blockName + ".meta",relativePath + blockName + ".data"
                                        ,manager,new BlockIndexId(Integer.parseInt(blockName)));
                                blocks.put((long) Integer.parseInt(blockName),b);
                            }
                        }
                        manager.setCount(dataNames.size() > 0 ? Integer.parseInt(dataNames.get(dataNames.size() - 1)) : 0);
                        manager.setBlocks(blocks);
                    }
                    blockManagerList.add(manager);
                }
                blockManagerController = new DefaultBlockManagerController(blockManagerList);
            }else{
                blockManagerController = new DefaultBlockManagerController(5);
            }
        } catch (InitiationFailedException e) {
            LOGGER.fatal("initialize failed, {}",e.getMessage());
            throw e;
        }
    }

//    /**
//     * the fileForm must exists, fileTo may be created
//     * @param commandAndArgs the args
//     */
//    private static void smartCopy(String[] commandAndArgs) {
//        if (commandAndArgs.length < 3) {
//            printCommandExample("smartCopy");
//            defaultOutput();
//        } else {
//            String fileId1 = commandAndArgs[1];
//            File fileFrom;
//            String fileId2 = commandAndArgs[2];
//            File fileTo;
//            try{
//                fileFrom = fileManagerController.getFile(new FileIdWithManagerId(fileId1));
//                fileTo = fileManagerController.getFile(new FileIdWithManagerId(fileId2));
//                if(fileTo == null){
//                    FileIdWithManagerId fileToId = new FileIdWithManagerId(fileId2);
//                    fileTo = fileManagerController.newFile(fileToId.getFileId(),fileToId.getFileManagerId());
//                }
//            }catch (FileIdWithManagerIdFormatException e){
//                errorOutput("invalid fileIdFormat -> " + e.getMessage());
//                printCommandExample("smartCopy");
//                return;
//            }
//            if (fileFrom == null) {
//                errorOutput("file not exist");
//            } else {
//                try {
//                    UserUtils.smartCopy(fileFrom, fileTo);
//                } catch (IOException e) {
//                    errorOutput("IO failed -> " + e.getMessage());
//                    printCommandExample("smartCopy");
//                } catch (CorruptedFileException e) {
//                    errorOutput("file corrupted -> " + e.getMessage());
//                    printCommandExample("smartCopy");
//                } catch (AllocateNewBlockFailedException e) {
//                    errorOutput("block allocate failed -> " + e.getMessage());
//                    printCommandExample("smartCopy");
//                }
//            }
//        }
//    }
//
//    private static void smartWrite(String[] commandAndArgs) {
//        if (commandAndArgs.length < 4) {
//            printCommandExample("smartWrite");
//            defaultOutput();
//        } else {
//            String indexStr = commandAndArgs[2];
//            int index;
//            String fileId = commandAndArgs[1];
//            File file;
//            try {
//                index = Integer.parseInt(indexStr);
//                file = fileManagerController.getFile(new FileIdWithManagerId(fileId));
//            } catch (NumberFormatException e) {
//                errorOutput("index not valid -> " + e.getMessage());
//                printCommandExample("smartWrite");
//                return;
//            }catch (FileIdWithManagerIdFormatException e){
//                errorOutput("invalid fileIdFormat -> " + e.getMessage());
//                printCommandExample("smartWrite");
//                return;
//            }
//            if (file == null) {
//                errorOutput("file not exist");
//            } else {
//                try {
//                    UserUtils.smartWrite(file, index, commandAndArgs[3]);
//                } catch (IllegalCursorException e) {
//                    errorOutput("illegal cursor place -> " + e.getMessage());
//                } catch (AllocateNewBlockFailedException e) {
//                    errorOutput("block allocate failed -> " + e.getMessage());
//                } catch (IOException e) {
//                    errorOutput("IO failed -> " + e.getMessage());
//                } catch (CorruptedFileException e) {
//                    errorOutput("file corrupted -> " + e.getMessage());
//                }finally {
//                    printCommandExample("smartWrite");
//                }
//            }
//        }
//    }
//
//    private static void smartHex(String[] commandAndArgs) {
//        if (commandAndArgs.length < 2) {
//            printCommandExample("smartHex");
//            defaultOutput();
//        } else {
//            String blockId = commandAndArgs[1];
//            Block block;
//            try{
//                block = blockManagerController.getBlock(new BlockIndexIdWithManagerId(blockId));
//            }catch (BlockIndexIdWithManagerIdFormatException e){
//                errorOutput("invalid blockIdFormat -> " + e.getMessage());
//                printCommandExample("smartHex");
//                return;
//            }
//            if (block == null) {
//                errorOutput("block not exist");
//            } else {
//                try {
//                    UserUtils.smartHex(block);
//                } catch (IOException e) {
//                    printCommandExample("smartHex");
//                    errorOutput("read failed -> " + e.getMessage());
//                }
//            }
//        }
//    }
//
//    private static void smartCat(String[] commandAndArgs) {
//        if (commandAndArgs.length < 2) {
//            printCommandExample("smartCat");
//            defaultOutput();
//        } else {
//            String fileId = commandAndArgs[1];
//            File file;
//            try{
//                file = fileManagerController.getFile(new FileIdWithManagerId(fileId));
//            }catch (FileIdWithManagerIdFormatException e){
//                errorOutput("invalid fileIdFormat -> " + e.getMessage());
//                printCommandExample("smartCat");
//                return;
//            }
//            if (file == null) {
//                errorOutput("file not exist");
//            } else {
//                try {
//                    UserUtils.smartCat(file);
//                } catch (IOException e) {
//                    errorOutput("read failed -> " + e.getMessage());
//                    printCommandExample("smartCat");
//                } catch (CorruptedFileException e) {
//                    errorOutput("file corrupted -> " + e.getMessage());
//                    printCommandExample("smartCat");
//                }
//            }
//        }
//    }
//
//    private static void newFile(String[] commandAndArgs) {
//        if (commandAndArgs.length < 2) {
//            printCommandExample("newFile");
//            defaultOutput();
//            return;
//        }
//        String fileId = commandAndArgs[1];
//        int id;
//        try{
//            id = Integer.parseInt(fileId);
//        }catch (NumberFormatException e){
//            errorOutput("invalid fileIdFormat -> " + e.getMessage());
//            printCommandExample("newFile");
//            return;
//        }
//        File file = fileManagerController.newFile(new FileId(id));
//        if(file == null){
//            errorOutput("create file fail");
//            printCommandExample("newFile");
//        }else{
//            System.out.println(file.getFileManager().getId().getIdString() + "-" + file.getFileId().getIdString());
//        }
//    }
//
//    private static void read(String[] commandAndArgs) {
//        if (commandAndArgs.length < 3) {
//            printCommandExample("read");
//            defaultOutput();
//            return;
//        }
//        String fileId = commandAndArgs[1];
//        String lengthStr = commandAndArgs[2];
//        long length;
//        File file;
//        try{
//            file = fileManagerController.getFile(new FileIdWithManagerId(fileId));
//            length = Long.parseLong(lengthStr);
//        }catch (NumberFormatException e){
//            errorOutput("invalid length -> " + e.getMessage());
//            printCommandExample("read");
//            return;
//        } catch (FileIdWithManagerIdFormatException e){
//            errorOutput("invalid fileIdFormat -> " + e.getMessage());
//            printCommandExample("read");
//            return;
//        }
//        if (file == null) {
//            errorOutput("file not exist");
//        } else {
//            try {
//                file.read((int) length);
//            } catch (IOException e) {
//                errorOutput("read failed -> " + e.getMessage());
//                printCommandExample("read");
//            } catch (CorruptedFileException e) {
//                errorOutput("file corrupted -> " + e.getMessage());
//                printCommandExample("read");
//            }
//        }
//    }
//
//    private static void write(String[] commandAndArgs) {
//        if (commandAndArgs.length < 3) {
//            printCommandExample("write");
//            defaultOutput();
//            return;
//        }
//
//        String fileId = commandAndArgs[1];
//        File file;
//        try{
//            file = fileManagerController.getFile(new FileIdWithManagerId(fileId));
//        }catch (FileIdWithManagerIdFormatException e){
//            errorOutput("invalid fileIdFormat -> " + e.getMessage());
//            printCommandExample("write");
//            return;
//        }
//        if (file == null) {
//            errorOutput("file not exist");
//        } else {
//            StringBuilder toWrite = new StringBuilder();
//            for(int i = 2;i < commandAndArgs.length;i++){
//                toWrite.append(commandAndArgs[i]);
//            }
//            try {
//                file.write(toWrite.toString().getBytes());
//            } catch (IOException e) {
//                errorOutput("read failed -> " + e.getMessage());
//                printCommandExample("write");
//            } catch (CorruptedFileException e) {
//                errorOutput("file corrupted -> " + e.getMessage());
//                printCommandExample("write");
//            } catch (AllocateNewBlockFailedException e) {
//                errorOutput("fail -> " + e.getMessage());
//                printCommandExample("write");
//            }
//
//        }
//    }
//
//    private static void pos(String[] commandAndArgs) {
//        if (commandAndArgs.length < 2) {
//            printCommandExample("pos");
//            defaultOutput();
//            return;
//        }
//        String fileId = commandAndArgs[1];
//        File file;
//        try{
//            file = fileManagerController.getFile(new FileIdWithManagerId(fileId));
//            System.out.println(file.pos());
//        }catch (FileIdWithManagerIdFormatException e){
//            errorOutput("invalid fileIdFormat -> " + e.getMessage());
//            printCommandExample("pos");
//        }
//    }
//
//
//    private static void move(String[] commandAndArgs) {
//        if (commandAndArgs.length < 4) {
//            printCommandExample("move");
//            defaultOutput();
//            return;
//        }
//        String fileId = commandAndArgs[1];
//        File file;
//        try{
//            file = fileManagerController.getFile(new FileIdWithManagerId(fileId));
//        }catch (FileIdWithManagerIdFormatException e){
//            errorOutput("invalid fileIdFormat -> " + e.getMessage());
//            printCommandExample("move");
//            return;
//        }
//        int index;
//        int cursor;
//        try{
//            index = Integer.parseInt(commandAndArgs[2]);
//            cursor = Integer.parseInt(commandAndArgs[3]);
//            file.move(index,cursor);
//        }catch (NumberFormatException e){
//            errorOutput("invalid positionFormat -> " + e.getMessage());
//            printCommandExample("move");
//        } catch (IllegalCursorException e) {
//            errorOutput("illegal position -> " + e.getMessage());
//            printCommandExample("move");
//        }
//    }
//
//    private static void size(String[] commandAndArgs) {
//        if (commandAndArgs.length < 2) {
//            printCommandExample("size");
//            defaultOutput();
//            return;
//        }
//        String fileId = commandAndArgs[1];
//        File file;
//        long size;
//        try{
//            file = fileManagerController.getFile(new FileIdWithManagerId(fileId));
//            size = file.size();
//            System.out.println(size);
//        }catch (FileIdWithManagerIdFormatException e){
//            errorOutput("invalid fileIdFormat -> " + e.getMessage());
//            printCommandExample("size");
//        } catch (CorruptedFileException e) {
//            errorOutput("file corrupted -> " + e.getMessage());
//            printCommandExample("size");
//        } catch (IOException e) {
//            errorOutput("read failed -> " + e.getMessage());
//            printCommandExample("size");
//        }
//    }
//
//    private static void close(String[] commandAndArgs) {
//        if (commandAndArgs.length < 2) {
//            printCommandExample("close");
//            defaultOutput();
//            return;
//        }
//        String fileId = commandAndArgs[1];
//        File file;
//        try{
//            file = fileManagerController.getFile(new FileIdWithManagerId(fileId));
//            file.close();
//        }catch (FileIdWithManagerIdFormatException e){
//            errorOutput("invalid fileIdFormat -> " + e.getMessage());
//            printCommandExample("close");
//        }
//    }
//
//    private static void setSize(String[] commandAndArgs) {
//        if (commandAndArgs.length < 3) {
//            printCommandExample("setSize");
//            defaultOutput();
//            return;
//        }
//        String fileId = commandAndArgs[1];
//        String lengthStr = commandAndArgs[2];
//        long length;
//        File file;
//        try{
//            length = Long.parseLong(lengthStr);
//            file = fileManagerController.getFile(new FileIdWithManagerId(fileId));
//        }catch (FileIdWithManagerIdFormatException e){
//            errorOutput("invalid fileIdFormat -> " + e.getMessage());
//            printCommandExample("setSize");
//            return;
//        }catch (NumberFormatException e){
//            errorOutput("invalid length -> " + e.getMessage());
//            printCommandExample("setSize");
//            return;
//        }
//        if (file == null) {
//            errorOutput("file not exist");
//        } else {
//            try {
//                file.setSize(length);
//            } catch (IOException e) {
//                errorOutput("read failed -> " + e.getMessage());
//                printCommandExample("setSize");
//            } catch (CorruptedFileException e) {
//                errorOutput("file corrupted -> " + e.getMessage());
//                printCommandExample("setSize");
//            } catch (AllocateNewBlockFailedException e) {
//                errorOutput("fail -> " + e.getMessage());
//                printCommandExample("setSize");
//            }
//        }
//    }
//
//    private static void printCommandExample(String commandName){
//        System.out.println(commandName + " : " + commandFormatHelpers.get(commandName));
//    }
//
//
//    private static void printHelp(){
//        System.out.println("commands : ");
//        System.out.println("\t new-file : create new file with the given name(name must be a number)");
//        System.out.println("\t read : read data from a file with given length");
//        System.out.println("\t write : write data to a file");
//        System.out.println("\t pos : show the cursor of a file");
//        System.out.println("\t move : move the cursor of a file");
//        System.out.println("\t size : get the size of a file");
//        System.out.println("\t close : close a file");
//        System.out.println("\t set-size : set the size of a file(extra bytes would be 0x00)");
//        System.out.println("\t smart-cat : read all the data start from the cursor");
//        System.out.println("\t smart-write : write to a specific place of a file");
//        System.out.println("\t smart-hex : read the data of a block in the form of hex numbers");
//        System.out.println("\t smart-copy : copy the data of a file to another file(depend on their current cursor)");
//
//    }
//
//    private static void defaultOutput(){
//        System.out.println("invalid command, use -help for help");
//    }
//
//    private static void errorOutput(String info){
//        System.out.println(info + ", use -help for help");
//    }
//
//    private static void shutUpGracefully(Reader reader){
//        try {
//            reader.close();
//        } catch (IOException e) {
//            LOGGER.warn("close standard input reader failed");
//        }
//    }
//
//    public static void main(String[] args){
//        try {
//            initialize();
//        } catch (InitiationFailedException e) {
//            System.out.println("system initialize failed");
//            return;
//        }
//
//        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
//        System.out.println("welcome to smart file system");
//        boolean shutDownFlag = false;
//        while(!shutDownFlag){
//            System.out.print("~smart: ");
//            String str;
//            try {
//                str = reader.readLine();
//            } catch (IOException e) {
//                LOGGER.fatal("read command failed");
//                shutUpGracefully(reader);
//                shutDownFlag = true;
//                continue;
//            }
//            String[] commandAndArgs = str.split(" ");
//            switch (commandAndArgs[0]) {
//                case "quit" -> {
//                    shutUpGracefully(reader);
//                    shutDownFlag = true;
//                }
//                case "new-file","newFile" -> newFile(commandAndArgs);
//                case "read" -> read(commandAndArgs);
//                case "write" -> write(commandAndArgs);
//                case "pos" -> pos(commandAndArgs);
//                case "move" -> move(commandAndArgs);
//                case "size" -> size(commandAndArgs);
//                case "close" -> close(commandAndArgs);
//                case "set-size","setSize" -> setSize(commandAndArgs);
//                case "smart-cat", "smartCat" -> smartCat(commandAndArgs);
//                case "smart-hex", "smartHex" -> smartHex(commandAndArgs);
//                case "smart-write", "smartWrite" -> smartWrite(commandAndArgs);
//                case "smart-copy", "smartCopy" -> smartCopy(commandAndArgs);
//                case "-help" -> printHelp();
//                default -> {
//                    defaultOutput();
//                    printHelp();
//                }
//            }
//        }
//    }

    /**
     * the fileForm must exists, fileTo may be created
     * @param commandAndArgs the args
     */
    public String smartCopy(String[] commandAndArgs) {
        String returnValue = "";
        if (commandAndArgs.length < 3) {
            returnValue += printCommandExample("smartCopy") + "\n";
            returnValue += defaultOutput();
        } else {
            String fileId1 = commandAndArgs[1];
            File fileFrom;
            String fileId2 = commandAndArgs[2];
            File fileTo;
            try{
                fileFrom = fileManagerController.getFile(new FileIdWithManagerId(fileId1));
                fileTo = fileManagerController.getFile(new FileIdWithManagerId(fileId2));
                if(fileTo == null){
                    FileIdWithManagerId fileToId = new FileIdWithManagerId(fileId2);
                    fileTo = fileManagerController.newFile(fileToId.getFileId(),fileToId.getFileManagerId());
                }
            }catch (FileIdWithManagerIdFormatException e){
                returnValue += errorOutput("invalid fileIdFormat -> " + e.getMessage()) + "\n";
                returnValue += printCommandExample("smartCopy");
                return returnValue;
            }
            if (fileFrom == null) {
                returnValue += errorOutput("file not exist");
            } else {
                try {
                    returnValue += UserUtils.smartCopy(fileFrom, fileTo);
                } catch (IOException e) {
                    returnValue += errorOutput("IO failed -> " + e.getMessage()) + "\n";
                    returnValue += printCommandExample("smartCopy");
                } catch (CorruptedFileException e) {
                    returnValue += errorOutput("file corrupted -> " + e.getMessage()) + "\n";
                    returnValue += printCommandExample("smartCopy");
                } catch (AllocateNewBlockFailedException e) {
                    returnValue += errorOutput("block allocate failed -> " + e.getMessage()) + "\n";
                    returnValue +=printCommandExample("smartCopy");
                }
            }
        }
        return  returnValue;
    }

    public String smartWrite(String[] commandAndArgs) {
        String returnValue = "";
        if (commandAndArgs.length < 4) {
            returnValue += printCommandExample("smartWrite") + "\n";
            returnValue += defaultOutput();
            return returnValue;
        } else {
            String indexStr = commandAndArgs[2];
            int index;
            String fileId = commandAndArgs[1];
            File file;
            try {
                index = Integer.parseInt(indexStr);
                file = fileManagerController.getFile(new FileIdWithManagerId(fileId));
            } catch (NumberFormatException e) {
                returnValue += errorOutput("index not valid -> " + e.getMessage()) + "\n";
                returnValue += printCommandExample("smartWrite");
                return returnValue;
            }catch (FileIdWithManagerIdFormatException e){
                returnValue += errorOutput("invalid fileIdFormat -> " + e.getMessage()) + "\n";
                returnValue += printCommandExample("smartWrite");
                return returnValue;
            }
            if (file == null) {
                returnValue += errorOutput("file not exist");
            } else {
                try {
                    returnValue += UserUtils.smartWrite(file, index, commandAndArgs[3]) + "\n";
                } catch (IllegalCursorException e) {
                    returnValue += errorOutput("illegal cursor place -> " + e.getMessage()) + "\n";
                    returnValue += printCommandExample("smartWrite");
                } catch (AllocateNewBlockFailedException e) {
                    returnValue += errorOutput("block allocate failed -> " + e.getMessage()) + "\n";
                    returnValue += printCommandExample("smartWrite");
                } catch (IOException e) {
                    returnValue += errorOutput("IO failed -> " + e.getMessage()) + "\n";
                    returnValue += printCommandExample("smartWrite");
                } catch (CorruptedFileException e) {
                    returnValue += errorOutput("file corrupted -> " + e.getMessage()) + "\n";
                    returnValue += printCommandExample("smartWrite");
                }
            }
            return returnValue;
        }
    }

    public String smartHex(String[] commandAndArgs) {
        String returnValue = "";
        if (commandAndArgs.length < 2) {
            returnValue += printCommandExample("smartHex") + "\n";
            returnValue += defaultOutput();
            return returnValue;
        } else {
            String blockId = commandAndArgs[1];
            Block block;
            try{
                block = blockManagerController.getBlock(new BlockIndexIdWithManagerId(blockId));
            }catch (BlockIndexIdWithManagerIdFormatException e){
                returnValue += errorOutput("invalid blockIdFormat -> " + e.getMessage()) + "\n";
                returnValue += printCommandExample("smartHex");
                return returnValue;
            }
            if (block == null) {
                returnValue += errorOutput("block not exist");
            } else {
                try {
                    returnValue += UserUtils.smartHex(block);
                } catch (IOException e) {
                    returnValue += printCommandExample("smartHex") + "\n";
                    returnValue += errorOutput("read failed -> " + e.getMessage());
                }
            }
            return returnValue;
        }
    }

    public String smartCat(String[] commandAndArgs) {
        String returnValue = "";
        if (commandAndArgs.length < 2) {
            returnValue += printCommandExample("smartCat") + "\n";
            returnValue += defaultOutput();
        } else {
            String fileId = commandAndArgs[1];
            File file;
            try{
                file = fileManagerController.getFile(new FileIdWithManagerId(fileId));
            }catch (FileIdWithManagerIdFormatException e){
                returnValue += errorOutput("invalid fileIdFormat -> " + e.getMessage()) + "\n";
                returnValue += printCommandExample("smartCat");
                return returnValue;
            }
            if (file == null) {
                returnValue += errorOutput("file not exist");
            } else {
                try {
                    returnValue += UserUtils.smartCat(file);
                } catch (IOException e) {
                    returnValue += errorOutput("read failed -> " + e.getMessage()) + "\n";
                    returnValue += printCommandExample("smartCat");
                } catch (CorruptedFileException e) {
                    returnValue += errorOutput("file corrupted -> " + e.getMessage()) + "\n";
                    returnValue += printCommandExample("smartCat");
                }
            }
        }
        return returnValue;
    }

    public String newFile(String[] commandAndArgs) {
        String returnValue = "";
        if (commandAndArgs.length < 2) {
            returnValue += printCommandExample("newFile") + "\n";
            returnValue += defaultOutput();
            return returnValue;
        }
        String fileId = commandAndArgs[1];
        int id;
        try{
            id = Integer.parseInt(fileId);
        }catch (NumberFormatException e){
            returnValue += errorOutput("invalid fileIdFormat -> " + e.getMessage()) + "\n";
            returnValue += printCommandExample("newFile");
            return returnValue;
        }
        File file = fileManagerController.newFile(new FileId(id));
        if(file == null){
            returnValue += errorOutput("create file fail") + "\n";
            returnValue += printCommandExample("newFile");
        }else{
            returnValue += file.getFileManager().getId().getIdString() + "-" + file.getFileId().getIdString() + "\n";
        }
        return returnValue;
    }

    public String read(String[] commandAndArgs) {
        String returnValue = "";
        if (commandAndArgs.length < 3) {
            returnValue += printCommandExample("read") + "\n";
            returnValue += defaultOutput();
            return returnValue;
        }
        String fileId = commandAndArgs[1];
        String lengthStr = commandAndArgs[2];
        long length;
        File file;
        try{
            file = fileManagerController.getFile(new FileIdWithManagerId(fileId));
            length = Long.parseLong(lengthStr);
        }catch (NumberFormatException e){
            returnValue += errorOutput("invalid length -> " + e.getMessage()) + "\n";
            returnValue += printCommandExample("read");
            return returnValue;
        } catch (FileIdWithManagerIdFormatException e){
            returnValue += errorOutput("invalid fileIdFormat -> " + e.getMessage()) + "\n";
            returnValue += printCommandExample("read");
            return returnValue;
        }
        if (file == null) {
            returnValue += errorOutput("file not exist");
        } else {
            try {
                byte[] read = file.read((int) length);
                returnValue += read;
                if(read.length < length){
                    returnValue +=("\n" + " not enough bytes left");
                }
            } catch (IOException e) {
                returnValue += errorOutput("read failed -> " + e.getMessage()) + "\n";
                returnValue += printCommandExample("read");
            } catch (CorruptedFileException e) {
                returnValue += errorOutput("file corrupted -> " + e.getMessage()) + "\n";
                returnValue += printCommandExample("read");
            }
        }
        return  returnValue;
    }

    public String write(String[] commandAndArgs) {
        String returnValue = "";
        if (commandAndArgs.length < 3) {
            returnValue += printCommandExample("write") + "\n";
            returnValue += defaultOutput();
            return returnValue;
        }

        String fileId = commandAndArgs[1];
        File file;
        try{
            file = fileManagerController.getFile(new FileIdWithManagerId(fileId));
        }catch (FileIdWithManagerIdFormatException e){
            returnValue += errorOutput("invalid fileIdFormat -> " + e.getMessage()) + "\n";
            returnValue += printCommandExample("write");
            return returnValue;
        }
        if (file == null) {
            returnValue += errorOutput("file not exist");
        } else {
            StringBuilder toWrite = new StringBuilder();
            for(int i = 2;i < commandAndArgs.length;i++){
                toWrite.append(commandAndArgs[i]);
                if(i != commandAndArgs.length - 1){
                    toWrite.append(" ");
                }
            }

            try {
                file.write(toWrite.toString().getBytes());
            } catch (IOException e) {
                returnValue += errorOutput("read failed -> " + e.getMessage()) + "\n";
                returnValue += printCommandExample("write");
            } catch (CorruptedFileException e) {
                returnValue += errorOutput("file corrupted -> " + e.getMessage()) + "\n";
                returnValue += printCommandExample("write");
            } catch (AllocateNewBlockFailedException e) {
                returnValue += errorOutput("fail -> " + e.getMessage()) + "\n";
                returnValue += printCommandExample("write");
            }
        }
        return returnValue;
    }

    public String pos(String[] commandAndArgs) {
        String returnValue = "";
        if (commandAndArgs.length < 2) {
            returnValue += printCommandExample("pos") + "\n";
            returnValue += defaultOutput();
            return returnValue;
        }
        String fileId = commandAndArgs[1];
        File file;
        try{
            file = fileManagerController.getFile(new FileIdWithManagerId(fileId));
            returnValue += file.pos();
        }catch (FileIdWithManagerIdFormatException e){
            returnValue += errorOutput("invalid fileIdFormat -> " + e.getMessage()) + "\n";
            returnValue += printCommandExample("pos");
        }
        return returnValue;
    }


    public String move(String[] commandAndArgs) {
        String returnValue = "";
        if (commandAndArgs.length < 4) {
            returnValue += printCommandExample("move") + "\n";
            returnValue += defaultOutput();
            return returnValue;
        }
        String fileId = commandAndArgs[1];
        File file;
        try{
            file = fileManagerController.getFile(new FileIdWithManagerId(fileId));
        }catch (FileIdWithManagerIdFormatException e){
            returnValue += errorOutput("invalid fileIdFormat -> " + e.getMessage()) + "\n";
            returnValue += printCommandExample("move");
            return returnValue;
        }
        int index;
        int cursor;
        try{
            index = Integer.parseInt(commandAndArgs[2]);
            cursor = Integer.parseInt(commandAndArgs[3]);
            file.move(index,cursor);
        }catch (NumberFormatException e){
            returnValue += errorOutput("invalid positionFormat -> " + e.getMessage()) + "\n";
            returnValue += printCommandExample("move");
        } catch (IllegalCursorException e) {
            returnValue += errorOutput("illegal position -> " + e.getMessage()) + "\n";
            returnValue += printCommandExample("move");
        }
        return returnValue;
    }

    public String size(String[] commandAndArgs) {
        String returnValue = "";
        if (commandAndArgs.length < 2) {
            returnValue += printCommandExample("size") + "\n";
            returnValue += defaultOutput();
            return returnValue;
        }
        String fileId = commandAndArgs[1];
        File file;
        long size;
        try{
            file = fileManagerController.getFile(new FileIdWithManagerId(fileId));
            size = file.size();
            returnValue += size;
        }catch (FileIdWithManagerIdFormatException e){
            returnValue += errorOutput("invalid fileIdFormat -> " + e.getMessage()) + "\n";
            returnValue += printCommandExample("size");
        } catch (CorruptedFileException e) {
            returnValue += errorOutput("file corrupted -> " + e.getMessage()) + "\n";
            returnValue += printCommandExample("size");
        } catch (IOException e) {
            returnValue += errorOutput("read failed -> " + e.getMessage()) + "\n";
            returnValue += printCommandExample("size");
        }
        return returnValue;
    }

    public String close(String[] commandAndArgs) {
        String returnValue = "";
        if (commandAndArgs.length < 2) {
            returnValue += printCommandExample("close") + "\n";
            returnValue += defaultOutput();
            return returnValue;
        }
        String fileId = commandAndArgs[1];
        File file;
        try{
            file = fileManagerController.getFile(new FileIdWithManagerId(fileId));
            file.close();
        }catch (FileIdWithManagerIdFormatException e){
            returnValue += errorOutput("invalid fileIdFormat -> " + e.getMessage()) + "\n";
            returnValue += printCommandExample("close");
        }
        return returnValue;
    }

    public String setSize(String[] commandAndArgs) {
        String returnValue = "";
        if (commandAndArgs.length < 3) {
            printCommandExample("setSize");
            returnValue += defaultOutput();
            return returnValue;
        }
        String fileId = commandAndArgs[1];
        String lengthStr = commandAndArgs[2];
        long length;
        File file;
        try{
            length = Long.parseLong(lengthStr);
            file = fileManagerController.getFile(new FileIdWithManagerId(fileId));
        }catch (FileIdWithManagerIdFormatException e){
            returnValue += errorOutput("invalid fileIdFormat -> " + e.getMessage()) + "\n";
            returnValue += printCommandExample("setSize");
            return returnValue;
        }catch (NumberFormatException e){
            returnValue += errorOutput("invalid length -> " + e.getMessage()) + "\n";
            returnValue += printCommandExample("setSize");
            return returnValue;
        }
        if (file == null) {
            returnValue += errorOutput("file not exist");
        } else {
            try {
                file.setSize(length);
            } catch (IOException e) {
                returnValue += errorOutput("read failed -> " + e.getMessage()) + "\n";
                returnValue += printCommandExample("setSize");
            } catch (CorruptedFileException e) {
                returnValue += errorOutput("file corrupted -> " + e.getMessage()) + "\n";
                returnValue += printCommandExample("setSize");
            } catch (AllocateNewBlockFailedException e) {
                returnValue += errorOutput("fail -> " + e.getMessage()) + "\n";
                returnValue += printCommandExample("setSize");
            }
        }
        return returnValue;
    }

    public String printHelp(){
        return "commands :\n" +
               "\t new-file : create new file with the given name(name must be a number)\n" +
               "\t read : read data from a file with given length\n" +
               "\t write : write data to a file\n" +
               "\t pos : show the cursor of a file\n" +
               "\t move : move the cursor of a file\n" +
               "\t size : get the size of a file\n" +
               "\t close : close a file\n" +
               "\t set-size : set the size of a file(extra bytes would be 0x00)\n" +
               "\t smart-cat : read all the data start from the cursor\n" +
               "\t smart-write : write to a specific place of a file\n" +
               "\t smart-hex : read the data of a block in the form of hex numbers\n" +
               "\t smart-copy : copy the data of a file to another file(depend on their current cursor)";
    }

    public String defaultOutput(){
        return "invalid command, use -help for help";
    }

    private String printCommandExample(String commandName){
        return commandName + " : " + commandFormatHelpers.get(commandName);
    }

    private String errorOutput(String info){
        return info + ", use -help for help";
    }


}
