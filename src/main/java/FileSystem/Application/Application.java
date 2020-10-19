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
        commandFormatHelpers.put("newFile","new-file||newFile file_id\n\texample:new-file 3");
        commandFormatHelpers.put("read","read file_id length\n\texample:read fm0-f1 5");
        commandFormatHelpers.put("write","write file_id data\n\texample:write fm0-f1 hello");
        commandFormatHelpers.put("pos","pos file_id\n\texample:pos fm0-f1");
        commandFormatHelpers.put("move","move file_id index cursor_place(0 denotes the current cursor,1 denotes head of the file,2 denote tail of the file)\n\texample:move fm0-f1 0 1");
        commandFormatHelpers.put("size","size file_id\n\texample:size fm0-f1");
        commandFormatHelpers.put("close","close file_id\n\texample:close fm0-f1");
        commandFormatHelpers.put("setSize","set-size||setSize file_id new_size\n\texample:set-size fm0-f1 1");
        commandFormatHelpers.put("smartCat","smart-cat||smartCat file_id\n\texample:smart-cat fm0-f1");
        commandFormatHelpers.put("smartHex","smart-hex||smartHex block_id\n\texample:smart-hex bm0-b1");
        commandFormatHelpers.put("smartWrite","smart-write||smartWrite file_id index(from the head of the file) data\n\texample:set-write fm0-f1 4 hello");
        commandFormatHelpers.put("smartCopy","smart-copy||smartCopy file_id_from file_id_to\n\texample:smart-copy fm0-f1 fm0-f2");
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
            } catch (FileExistedException e) {
                returnValue += errorOutput("file existed" + e.getMessage()) + "\n";
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
                    StringBuilder toWrite = new StringBuilder();
                    for(int i = 3;i < commandAndArgs.length;i++){
                        toWrite.append(commandAndArgs[i]);
                        if(i != commandAndArgs.length - 1){
                            toWrite.append(" ");
                        }
                    }
                    returnValue += UserUtils.smartWrite(file, index, toWrite.toString()) + "\n";
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
        File file = null;
        try {
            file = fileManagerController.newFile(new FileId(id));
        } catch (FileExistedException e) {
            returnValue += errorOutput("file already exited" + e.getMessage()) + "\n";
            returnValue += printCommandExample("newFile");
            return returnValue;
        }
        if(file == null){
            returnValue += errorOutput("create file fail") + "\n";
            returnValue += printCommandExample("newFile");
        }else{
            returnValue += file.getFileManager().getId().getIdString() + "-" + file.getFileId().getIdString();
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
                returnValue += new String(read);
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
        } catch (CorruptedFileException e) {
            returnValue += errorOutput("file corrupted -> " + e.getMessage()) + "\n";
            returnValue += printCommandExample("move");
        } catch (IOException e) {
            returnValue += errorOutput("read failed -> " + e.getMessage()) + "\n";
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
    public String listFiles(){
        StringBuilder returnValue = new StringBuilder();
        Map<FileManagerId, List<File>> fileManagerIdListMap = fileManagerController.listFiles();
        for(Map.Entry<FileManagerId, List<File>> entry : fileManagerIdListMap.entrySet()){
            returnValue.append(entry.getKey().getIdString()).append(":").append("\n");
            for(File file : entry.getValue()){
                returnValue.append("\t").append(file.getFileId().getIdString()).append("\n");
            }
        }
        return returnValue.toString();
    }

    public String printHelp(){
        return "commands :\n" +
               "\t new-file : create new file with the given name(name must be a number)\n" +
               "\t list-files : list all the files classified by fileManager\n" +
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
