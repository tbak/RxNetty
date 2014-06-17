package io.reactivex.netty.pipeline;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import javax.net.ssl.SSLException;
import java.util.Map;

/**
 * @author Tomasz Bak
 */
public class SslClientPipelineConfigurator<I, O> implements PipelineConfigurator<I, O> {

    private final SslContext sslCtx;
    private final PipelineConfigurator mainPipelineConfigurator;

    public SslClientPipelineConfigurator(PipelineConfigurator mainPipelineConfigurator) throws SSLException {
        this.mainPipelineConfigurator = mainPipelineConfigurator;
        this.sslCtx = SslContext.newClientContext(InsecureTrustManagerFactory.INSTANCE);
    }

    @Override
    public void configureNewPipeline(ChannelPipeline pipeline) {
        pipeline.addLast(new SslClientChannelInitializer(sslCtx));
        mainPipelineConfigurator.configureNewPipeline(pipeline);
    }

    private static class SslClientChannelInitializer extends ChannelInitializer<SocketChannel> {
        private SslContext sslCtx;

        public SslClientChannelInitializer(SslContext sslCtx) {
            this.sslCtx = sslCtx;
        }

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();

            pipeline.addFirst(sslCtx.newHandler(ch.alloc()));

            for (Map.Entry<String, ChannelHandler> e : pipeline) {
                System.out.println(e.getKey() + "=" + e.getValue());
            }
        }
    }
}
