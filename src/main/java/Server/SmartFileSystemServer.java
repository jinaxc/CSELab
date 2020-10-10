package Server;

import FileSystem.Application.Application;
import FileSystem.Exception.InitiationFailedException;
import Server.Handler.CommandHandler;
import Server.Handler.HttpRequestHandler;
import Server.Handler.TextWebSocketFrameInboundHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * @author jinaxCai
 */
public class SmartFileSystemServer {
    public static void main(String[] args) throws InterruptedException {
        try {
            Application.startApplication();
        } catch (InitiationFailedException e) {
            return;
        }
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup(1);
        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // (3)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
//                            pipeline.addLast(new SimpleChannelInboundHandler<ByteBuf>() {
//                                @Override
//                                protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
//                                    System.out.println("get info");
//                                    ctx.fireChannelRead(msg.retain());
//                                }
//                            });
//                            pipeline.addLast(new ChannelOutboundHandlerAdapter(){
//                                @Override
//                                public void flush(ChannelHandlerContext ctx) throws Exception {
//                                    ctx.writeAndFlush(Unpooled.copiedBuffer("~smart: ",CharsetUtil.UTF_8));
//                                    super.flush(ctx);
//                                }
//                            });
                            pipeline.addLast(new IdleStateHandler(60,60, 0));
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new HttpObjectAggregator(64 * 1024));
                            pipeline.addLast(new HttpRequestHandler("/ws"));
                            pipeline.addLast(new WebSocketServerProtocolHandler("/ws",null,true));
                            pipeline.addLast(new TextWebSocketFrameInboundHandler());
                            pipeline.addLast(new LineBasedFrameDecoder(1024));
                            pipeline.addLast(new CommandHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(8888).sync(); // (7)

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
//
    }
}
