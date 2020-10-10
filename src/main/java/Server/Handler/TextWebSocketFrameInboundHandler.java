package Server.Handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author : chara
 */
public class TextWebSocketFrameInboundHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private final static Logger LOGGER = LogManager.getLogger(TextWebSocketFrameInboundHandler.class);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler
                .HandshakeComplete) {
            ctx.pipeline().remove(HttpRequestHandler.class);


//            ctx.writeAndFlush(Unpooled.copiedBuffer("welcome to smart file system\n", CharsetUtil.UTF_8));
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        ByteBuf byteBuf = msg.content();
        byteBuf.retain();
        LOGGER.info("get websocket data from {}",ctx.channel().remoteAddress());
        ctx.fireChannelRead(byteBuf);
    }
}
