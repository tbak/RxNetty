package io.reactivex.netty.pipeline;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;
import java.util.Map;

/**
 * @author Tomasz Bak
 */
public class SslServerPipelineConfigurator<I, O> implements PipelineConfigurator<I, O> {

    private final SslContext sslCtx;
    private PipelineConfigurator<I, O> mainPipelineConfigurator;

    public SslServerPipelineConfigurator(PipelineConfigurator<I, O> mainPipelineConfigurator) throws CertificateException, SSLException {
        this(createInsecureSslContext(), mainPipelineConfigurator);
    }

    public SslServerPipelineConfigurator(SslContext sslCtx, PipelineConfigurator<I, O> mainPipelineConfigurator) {
        this.sslCtx = sslCtx;
        this.mainPipelineConfigurator = mainPipelineConfigurator;
    }

    @Override
    public void configureNewPipeline(ChannelPipeline pipeline) {
        pipeline.addLast(new SslServerChannelInitializer(sslCtx));
        mainPipelineConfigurator.configureNewPipeline(pipeline);
    }

    private static SslContext createInsecureSslContext() throws CertificateException, SSLException {
        SelfSignedCertificate ssc = new SelfSignedCertificate();
        return SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
    }

    private static class SslServerChannelInitializer extends ChannelInitializer<SocketChannel> {
        private SslContext sslCtx;

        public SslServerChannelInitializer(SslContext sslCtx) {
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
