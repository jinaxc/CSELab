package Server.Handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author : chara
 */
public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final static Logger LOGGER = LogManager.getLogger(CommandHandler.class);
    private final String wsUri;
    public HttpRequestHandler(String s) {
        this.wsUri = s;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if (wsUri.equalsIgnoreCase(request.uri())) {
            LOGGER.info("receive webSocket handshake request");
            ctx.fireChannelRead(request.retain());
        }else{
            LOGGER.info("receive normal http request");
            DefaultFullHttpResponse defaultFullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.OK,
                    Unpooled.copiedBuffer("failed to create webSocket connection", CharsetUtil.UTF_8));
            defaultFullHttpResponse.headers()
                    .set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN)
                    .set(HttpHeaderNames.CONTENT_LENGTH, defaultFullHttpResponse.content().readableBytes())
                    .set(HttpHeaderNames.CONNECTION,HttpHeaderValues.CLOSE);

            ChannelFuture f = ctx.writeAndFlush(defaultFullHttpResponse);
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }
}
