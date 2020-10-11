package Server.Handler;

import FileSystem.Application.Application;
import FileSystem.Exception.InitiationFailedException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.CharsetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * @author jinaxCai
 */
public class CommandHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private final static Logger LOGGER = LogManager.getLogger(CommandHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        CharSequence charSequence = msg.readCharSequence(msg.readableBytes(), CharsetUtil.UTF_8);
        String command = charSequence.toString();
        LOGGER.info("receive message from {}, message is {}",ctx.channel().remoteAddress(),command);
        handleCommand(command,ctx);
    }

    private void handleCommand(String command,ChannelHandlerContext ctx){
        String[] commandAndArgs = command.split(" ");
        Application app;
        app = Application.getApplication();
        switch (commandAndArgs[0]) {
            case "quit":
                ctx.channel().close();
                break;
            case "new-file":
            case "newFile":
                ctx.writeAndFlush(wrapWebSocketFrame(app.newFile(commandAndArgs)));
                break;
            case "list-files":
            case "listFiles":
                ctx.writeAndFlush(wrapWebSocketFrame(app.listFiles()));
                break;
            case "read":
                ctx.writeAndFlush(wrapWebSocketFrame(app.read(commandAndArgs)));
                break;
            case "write":
                ctx.writeAndFlush(wrapWebSocketFrame(app.write(commandAndArgs)));
                break;
            case "pos":
                ctx.writeAndFlush(wrapWebSocketFrame(app.pos(commandAndArgs)));
                break;
            case "move":
                ctx.writeAndFlush(wrapWebSocketFrame(app.move(commandAndArgs)));
                break;
            case "size":
                ctx.writeAndFlush(wrapWebSocketFrame(app.size(commandAndArgs)));
                break;
            case "close":
                ctx.writeAndFlush(wrapWebSocketFrame(app.close(commandAndArgs)));
                break;
            case "set-size":
            case "setSize":
                ctx.writeAndFlush(wrapWebSocketFrame(app.setSize(commandAndArgs)));
                break;
            case "smart-cat":
            case "smartCat":
                ctx.writeAndFlush(wrapWebSocketFrame(app.smartCat(commandAndArgs)));
                break;
            case "smart-hex":
            case "smartHex":
                ctx.writeAndFlush(wrapWebSocketFrame((app.smartHex(commandAndArgs))));
                break;
            case "smart-write":
            case "smartWrite":
                ctx.writeAndFlush(wrapWebSocketFrame(app.smartWrite(commandAndArgs)));
                break;
            case "smart-copy":
            case "smartCopy":
                ctx.writeAndFlush(wrapWebSocketFrame(app.smartCopy(commandAndArgs)));
                break;
            case "-help":
                ctx.writeAndFlush(wrapWebSocketFrame(app.printHelp()));
                break;
            default:
                ctx.writeAndFlush(wrapWebSocketFrame(app.defaultOutput() + app.printHelp()));
                break;
        }
    }

    private TextWebSocketFrame wrapWebSocketFrame(String data){
        return new TextWebSocketFrame(data);
    }



}
