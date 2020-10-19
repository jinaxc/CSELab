package FileSystem.Application;

import FileSystem.Block.Block;
import FileSystem.Block.Default.BlockIndexId;
import FileSystem.Block.Default.BlockIndexIdWithManagerId;
import FileSystem.Buffer.BlockBufferManager;
import FileSystem.Exception.BlockException.AllocateNewBlockFailedException;
import FileSystem.Exception.BlockException.BlockIndexIdWithManagerIdFormatException;
import FileSystem.Block.Default.DefaultBlock;
import FileSystem.Controller.BlockManagerController;
import FileSystem.Controller.DefaultBlockManagerController;
import FileSystem.Controller.DefaultFileManagerController;
import FileSystem.Controller.FileManagerController;
import FileSystem.Exception.*;
import FileSystem.Exception.BlockException.BlockManagerIdFormatException;
import FileSystem.Exception.FileException.*;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author : chara
 */
public class Application {
    private final static Logger LOGGER = LogManager.getLogger(Application.class);
    public static BlockManagerController blockManagerController;
    public static FileManagerController fileManagerController;
    public static BlockBufferManager bufferManager;
    private static Map<String,String> commandFormatHelpers;


    private static void initialize() throws InitiationFailedException {
        System.out.println("please make sure that there aren't directories named Block and FileMetaData in this directory(directories made by this app is OK)");
        initializeBlockController();
        initializeFileController();
        bufferManager = new BlockBufferManager(10);
        commandFormatHelpers = new HashMap<>();
        commandFormatHelpers.put("newFile","new-file||newFile file_id\n\texample : new-file 3");
        commandFormatHelpers.put("read","read file_id length\n\texample : read fm0-f1 5");
        commandFormatHelpers.put("write","write file_id data\n\texample : write fm0-f1 hello");
        commandFormatHelpers.put("pos","pos file_id\n\texample : pos fm0-f1");
        commandFormatHelpers.put("move","move file_id index cursor_place(0 denotes the current cursor,1 denotes head of the file,2 denote tail of the file)\n\texample : move fm0-f1 0 1");
        commandFormatHelpers.put("size","size file_id\n\texample : size fm0-f1");
        commandFormatHelpers.put("close","close file_id\n\texample : close fm0-f1");
        commandFormatHelpers.put("setSize","set-size||setSize file_id new_size\n\texample : set-size fm0-f1 1");
        commandFormatHelpers.put("smartCat","smart-cat||smartCat file_id\n\texample : smart-cat fm0-f1");
        commandFormatHelpers.put("smartHex","smart-hex||smartHex block_id\n\texample : smart-hex bm0-b1");
        commandFormatHelpers.put("smartWrite","smart-write||smartWrite file_id index(from the head of the file) data\n\texample : smart-write fm0-f1 4 hello");
        commandFormatHelpers.put("smartCopy","smart-copy||smartCopy file_id_from file_id_to\n\texample : smart-copy fm0-f1 fm0-f2");
    }

    private static void initializeFileController() throws InitiationFailedException {
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

    private static void initializeBlockController() throws InitiationFailedException {
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

    /**
     * the fileForm must exists, fileTo may be created
     * @param commandAndArgs the args
     */
    private static void smartCopy(String[] commandAndArgs) {
        if (commandAndArgs.length < 3) {
            printCommandExample("smartCopy");
            defaultOutput();
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
                errorOutput("invalid fileIdFormat -> " + e.getMessage());
                printCommandExample("smartCopy");
                return;
            } catch (FileExistedException e) {
                errorOutput("file existed" + e.getMessage());
                printCommandExample("smartCopy");
                return;
            }
            if (fileFrom == null) {
                errorOutput("file not exist");
            } else {
                try {
                    UserUtils.smartCopy(fileFrom, fileTo);
                } catch (IOException e) {
                    errorOutput("IO failed -> " + e.getMessage());
                    printCommandExample("smartCopy");
                } catch (CorruptedFileException e) {
                    errorOutput("file corrupted -> " + e.getMessage());
                    printCommandExample("smartCopy");
                } catch (AllocateNewBlockFailedException e) {
                    errorOutput("block allocate failed -> " + e.getMessage());
                    printCommandExample("smartCopy");
                }
            }
        }
    }

    private static void smartWrite(String[] commandAndArgs) {
        if (commandAndArgs.length < 4) {
            printCommandExample("smartWrite");
            defaultOutput();
        } else {
            String indexStr = commandAndArgs[2];
            int index;
            String fileId = commandAndArgs[1];
            File file;
            try {
                index = Integer.parseInt(indexStr);
                file = fileManagerController.getFile(new FileIdWithManagerId(fileId));
            } catch (NumberFormatException e) {
                errorOutput("index not valid -> " + e.getMessage());
                printCommandExample("smartWrite");
                return;
            }catch (FileIdWithManagerIdFormatException e){
                errorOutput("invalid fileIdFormat -> " + e.getMessage());
                printCommandExample("smartWrite");
                return;
            }
            if (file == null) {
                errorOutput("file not exist");
            } else {
                try {
                    StringBuilder toWrite = new StringBuilder();
                    for(int i = 3;i < commandAndArgs.length;i++){
                        toWrite.append(commandAndArgs[i]);
                        if(i != commandAndArgs.length - 1){
                            toWrite.append(" ");
                        }
                    }
                    UserUtils.smartWrite(file, index, toWrite.toString());
                } catch (IllegalCursorException e) {
                    errorOutput("illegal cursor place -> " + e.getMessage());
                    printCommandExample("smartWrite");
                } catch (AllocateNewBlockFailedException e) {
                    errorOutput("block allocate failed -> " + e.getMessage());
                    printCommandExample("smartWrite");
                } catch (IOException e) {
                    errorOutput("IO failed -> " + e.getMessage());
                    printCommandExample("smartWrite");
                } catch (CorruptedFileException e) {
                    errorOutput("file corrupted -> " + e.getMessage());
                    printCommandExample("smartWrite");
                }
            }
        }
    }

    private static void smartHex(String[] commandAndArgs) {
        if (commandAndArgs.length < 2) {
            printCommandExample("smartHex");
            defaultOutput();
        } else {
            String blockId = commandAndArgs[1];
            Block block;
            try{
                block = blockManagerController.getBlock(new BlockIndexIdWithManagerId(blockId));
            }catch (BlockIndexIdWithManagerIdFormatException e){
                errorOutput("invalid blockIdFormat -> " + e.getMessage());
                printCommandExample("smartHex");
                return;
            }
            if (block == null) {
                errorOutput("block not exist");
            } else {
                try {
                    UserUtils.smartHex(block);
                } catch (IOException e) {
                    printCommandExample("smartHex");
                    errorOutput("read failed -> " + e.getMessage());
                }
            }
        }
    }

    private static void smartCat(String[] commandAndArgs) {
        if (commandAndArgs.length < 2) {
            printCommandExample("smartCat");
            defaultOutput();
        } else {
            String fileId = commandAndArgs[1];
            File file;
            try{
                file = fileManagerController.getFile(new FileIdWithManagerId(fileId));
            }catch (FileIdWithManagerIdFormatException e){
                errorOutput("invalid fileIdFormat -> " + e.getMessage());
                printCommandExample("smartCat");
                return;
            }
            if (file == null) {
                errorOutput("file not exist");
            } else {
                try {
                    UserUtils.smartCat(file);
                } catch (IOException e) {
                    errorOutput("read failed -> " + e.getMessage());
                    printCommandExample("smartCat");
                } catch (CorruptedFileException e) {
                    errorOutput("file corrupted -> " + e.getMessage());
                    printCommandExample("smartCat");
                }
            }
        }
    }

    private static void newFile(String[] commandAndArgs) {
        if (commandAndArgs.length < 2) {
            printCommandExample("newFile");
            defaultOutput();
            return;
        }
        String fileId = commandAndArgs[1];
        int id;
        try{
            id = Integer.parseInt(fileId);
        }catch (NumberFormatException e){
            errorOutput("invalid fileIdFormat -> " + e.getMessage());
            printCommandExample("newFile");
            return;
        }
        File file = null;
        try {
            file = fileManagerController.newFile(new FileId(id));
        } catch (FileExistedException e) {
            errorOutput("file already exited ->" + e.getMessage());
            printCommandExample("newFile");
            return;
        }
        if(file == null){
            errorOutput("create file fail");
            printCommandExample("newFile");
        }else{
            System.out.println(file.getFileManager().getId().getIdString() + "-" + file.getFileId().getIdString());
        }
    }

    private static void read(String[] commandAndArgs) {
        if (commandAndArgs.length < 3) {
            printCommandExample("read");
            defaultOutput();
            return;
        }
        String fileId = commandAndArgs[1];
        String lengthStr = commandAndArgs[2];
        long length;
        File file;
        try{
            file = fileManagerController.getFile(new FileIdWithManagerId(fileId));
            length = Long.parseLong(lengthStr);
        }catch (NumberFormatException e){
            errorOutput("invalid length -> " + e.getMessage());
            printCommandExample("read");
            return;
        } catch (FileIdWithManagerIdFormatException e){
            errorOutput("invalid fileIdFormat -> " + e.getMessage());
            printCommandExample("read");
            return;
        }
        if (file == null) {
            errorOutput("file not exist");
        } else {
            try {
                byte[] bytes = file.read((int) length);
                System.out.println(new String(bytes));
                if(bytes.length < length){
                    System.out.println("not enough bytes left");
                }
            } catch (IOException e) {
                errorOutput("read failed -> " + e.getMessage());
                printCommandExample("read");
            } catch (CorruptedFileException e) {
                errorOutput("file corrupted -> " + e.getMessage());
                printCommandExample("read");
            }
        }
    }

    private static void write(String[] commandAndArgs) {
        if (commandAndArgs.length < 3) {
            printCommandExample("write");
            defaultOutput();
            return;
        }

        String fileId = commandAndArgs[1];
        File file;
        try{
            file = fileManagerController.getFile(new FileIdWithManagerId(fileId));
        }catch (FileIdWithManagerIdFormatException e){
            errorOutput("invalid fileIdFormat -> " + e.getMessage());
            printCommandExample("write");
            return;
        }
        if (file == null) {
            errorOutput("file not exist");
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
                errorOutput("read failed -> " + e.getMessage());
                printCommandExample("write");
            } catch (CorruptedFileException e) {
                errorOutput("file corrupted -> " + e.getMessage());
                printCommandExample("write");
            } catch (AllocateNewBlockFailedException e) {
                errorOutput("fail -> " + e.getMessage());
                printCommandExample("write");
            }

        }
    }

    private static void pos(String[] commandAndArgs) {
        if (commandAndArgs.length < 2) {
            printCommandExample("pos");
            defaultOutput();
            return;
        }
        String fileId = commandAndArgs[1];
        File file;
        try{
            file = fileManagerController.getFile(new FileIdWithManagerId(fileId));
            System.out.println(file.pos());
        }catch (FileIdWithManagerIdFormatException e){
            errorOutput("invalid fileIdFormat -> " + e.getMessage());
            printCommandExample("pos");
        }
    }


    private static void move(String[] commandAndArgs) {
        if (commandAndArgs.length < 4) {
            printCommandExample("move");
            defaultOutput();
            return;
        }
        String fileId = commandAndArgs[1];
        File file;
        try{
            file = fileManagerController.getFile(new FileIdWithManagerId(fileId));
        }catch (FileIdWithManagerIdFormatException e){
            errorOutput("invalid fileIdFormat -> " + e.getMessage());
            printCommandExample("move");
            return;
        }
        int index;
        int cursor;
        try{
            index = Integer.parseInt(commandAndArgs[2]);
            cursor = Integer.parseInt(commandAndArgs[3]);
            file.move(index,cursor);
        }catch (NumberFormatException e){
            errorOutput("invalid positionFormat -> " + e.getMessage());
            printCommandExample("move");
        } catch (IllegalCursorException e) {
            errorOutput("illegal position -> " + e.getMessage());
            printCommandExample("move");
        } catch (CorruptedFileException e) {
            errorOutput("file corrupted -> " + e.getMessage());
            printCommandExample("move");
        } catch (IOException e) {
            errorOutput("read failed -> " + e.getMessage());
            printCommandExample("move");
        }
    }

    private static void size(String[] commandAndArgs) {
        if (commandAndArgs.length < 2) {
            printCommandExample("size");
            defaultOutput();
            return;
        }
        String fileId = commandAndArgs[1];
        File file;
        long size;
        try{
            file = fileManagerController.getFile(new FileIdWithManagerId(fileId));
            size = file.size();
            System.out.println(size);
        }catch (FileIdWithManagerIdFormatException e){
            errorOutput("invalid fileIdFormat -> " + e.getMessage());
            printCommandExample("size");
        } catch (CorruptedFileException e) {
            errorOutput("file corrupted -> " + e.getMessage());
            printCommandExample("size");
        } catch (IOException e) {
            errorOutput("read failed -> " + e.getMessage());
            printCommandExample("size");
        }
    }

    private static void close(String[] commandAndArgs) {
        if (commandAndArgs.length < 2) {
            printCommandExample("close");
            defaultOutput();
            return;
        }
        String fileId = commandAndArgs[1];
        File file;
        try{
            file = fileManagerController.getFile(new FileIdWithManagerId(fileId));
            file.close();
        }catch (FileIdWithManagerIdFormatException e){
            errorOutput("invalid fileIdFormat -> " + e.getMessage());
            printCommandExample("close");
        }
    }

    private static void setSize(String[] commandAndArgs) {
        if (commandAndArgs.length < 3) {
            printCommandExample("setSize");
            defaultOutput();
            return;
        }
        String fileId = commandAndArgs[1];
        String lengthStr = commandAndArgs[2];
        long length;
        File file;
        try{
            length = Long.parseLong(lengthStr);
            file = fileManagerController.getFile(new FileIdWithManagerId(fileId));
        }catch (FileIdWithManagerIdFormatException e){
            errorOutput("invalid fileIdFormat -> " + e.getMessage());
            printCommandExample("setSize");
            return;
        }catch (NumberFormatException e){
            errorOutput("invalid length -> " + e.getMessage());
            printCommandExample("setSize");
            return;
        }
        if (file == null) {
            errorOutput("file not exist");
        } else {
            try {
                file.setSize(length);
            } catch (IOException e) {
                errorOutput("read failed -> " + e.getMessage());
                printCommandExample("setSize");
            } catch (CorruptedFileException e) {
                errorOutput("file corrupted -> " + e.getMessage());
                printCommandExample("setSize");
            } catch (AllocateNewBlockFailedException e) {
                errorOutput("fail -> " + e.getMessage());
                printCommandExample("setSize");
            }
        }
    }

    private static void printCommandExample(String commandName){
        System.out.println(commandName + " : " + commandFormatHelpers.get(commandName));
    }


    private static void printHelp(){
        System.out.println("commands : ");
        System.out.println("\t new-file : create new file with the given name(name must be a number)");
        System.out.println("\t read : read data from a file with given length");
        System.out.println("\t write : write data to a file");
        System.out.println("\t pos : show the cursor of a file");
        System.out.println("\t move : move the cursor of a file");
        System.out.println("\t size : get the size of a file");
        System.out.println("\t close : close a file");
        System.out.println("\t set-size : set the size of a file(extra bytes would be 0x00)");
        System.out.println("\t smart-cat : read all the data start from the cursor");
        System.out.println("\t smart-write : write to a specific place of a file");
        System.out.println("\t smart-hex : read the data of a block in the form of hex numbers");
        System.out.println("\t smart-copy : copy the data of a file to another file(depend on their current cursor)");

    }

    private static void defaultOutput(){
        System.out.println("invalid command, use -help for help");
    }

    private static void errorOutput(String info){
        System.out.println(info + ", use -help for help");
    }

    private static void shutUpGracefully(Reader reader){
        try {
            reader.close();
        } catch (IOException e) {
            LOGGER.warn("close standard input reader failed");
        }
    }

    public static void main(String[] args){
        try {
            initialize();
        } catch (InitiationFailedException e) {
            System.out.println("system initialize failed");
            return;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("welcome to smart file system");
        boolean shutDownFlag = false;
        while(!shutDownFlag){
            System.out.print("~smart: ");
            String str;
            try {
                str = reader.readLine();
            } catch (IOException e) {
                LOGGER.fatal("read command failed");
                shutUpGracefully(reader);
                shutDownFlag = true;
                continue;
            }
            String[] commandAndArgs = str.split(" ");
            switch (commandAndArgs[0]) {
                case "quit" -> {
                    shutUpGracefully(reader);
                    shutDownFlag = true;
                }
                case "new-file","newFile" -> newFile(commandAndArgs);
                case "read" -> read(commandAndArgs);
                case "write" -> write(commandAndArgs);
                case "pos" -> pos(commandAndArgs);
                case "move" -> move(commandAndArgs);
                case "size" -> size(commandAndArgs);
                case "close" -> close(commandAndArgs);
                case "set-size","setSize" -> setSize(commandAndArgs);
                case "smart-cat", "smartCat" -> smartCat(commandAndArgs);
                case "smart-hex", "smartHex" -> smartHex(commandAndArgs);
                case "smart-write", "smartWrite" -> smartWrite(commandAndArgs);
                case "smart-copy", "smartCopy" -> smartCopy(commandAndArgs);
                case "-help" -> printHelp();
                default -> {
                    defaultOutput();
                    printHelp();
                }
            }
        }
    }

}
