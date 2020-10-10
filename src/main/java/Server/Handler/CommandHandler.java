package Server.Handler;

import FileSystem.Application.Application;
import FileSystem.Exception.InitiationFailedException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;


/**
 * @author jinaxCai
 */
public class CommandHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    public void channelActive(ChannelHandlerContext ctx){
        ctx.writeAndFlush(Unpooled.copiedBuffer("welcome to smart file system\n",CharsetUtil.UTF_8));
        ctx.writeAndFlush(Unpooled.copiedBuffer("~smart: ",CharsetUtil.UTF_8));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        CharSequence charSequence = msg.readCharSequence(msg.readableBytes(), CharsetUtil.UTF_8);
        String command = charSequence.toString();
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
                ctx.writeAndFlush(Unpooled.copiedBuffer(app.newFile(commandAndArgs), CharsetUtil.UTF_8));
                break;
            case "read":
                ctx.writeAndFlush(Unpooled.copiedBuffer(app.read(commandAndArgs), CharsetUtil.UTF_8));
                break;
            case "write":
                ctx.writeAndFlush(Unpooled.copiedBuffer(app.write(commandAndArgs), CharsetUtil.UTF_8));
                break;
            case "pos":
                ctx.writeAndFlush(Unpooled.copiedBuffer(app.pos(commandAndArgs), CharsetUtil.UTF_8));
                break;
            case "move":
                ctx.writeAndFlush(Unpooled.copiedBuffer(app.move(commandAndArgs), CharsetUtil.UTF_8));
                break;
            case "size":
                ctx.writeAndFlush(Unpooled.copiedBuffer(app.size(commandAndArgs), CharsetUtil.UTF_8));
                break;
            case "close":
                ctx.writeAndFlush(Unpooled.copiedBuffer(app.close(commandAndArgs), CharsetUtil.UTF_8));
                break;
            case "set-size":
            case "setSize":
                ctx.writeAndFlush(Unpooled.copiedBuffer(app.setSize(commandAndArgs), CharsetUtil.UTF_8));
                break;
            case "smart-cat":
            case "smartCat":
                ctx.writeAndFlush(Unpooled.copiedBuffer(app.smartCat(commandAndArgs), CharsetUtil.UTF_8));
                break;
            case "smart-hex":
            case "smartHex":
                ctx.writeAndFlush(Unpooled.copiedBuffer(app.smartHex(commandAndArgs), CharsetUtil.UTF_8));
                break;
            case "smart-write":
            case "smartWrite":
                ctx.writeAndFlush(Unpooled.copiedBuffer(app.smartWrite(commandAndArgs), CharsetUtil.UTF_8));
                break;
            case "smart-copy":
            case "smartCopy":
                ctx.writeAndFlush(Unpooled.copiedBuffer(app.smartCopy(commandAndArgs), CharsetUtil.UTF_8));
                break;
            case "-help":
                ctx.writeAndFlush(Unpooled.copiedBuffer(app.printHelp(), CharsetUtil.UTF_8));
                break;
            default:
                ctx.writeAndFlush(Unpooled.copiedBuffer(app.defaultOutput() + app.printHelp(), CharsetUtil.UTF_8));
                break;
        }
    }



}
