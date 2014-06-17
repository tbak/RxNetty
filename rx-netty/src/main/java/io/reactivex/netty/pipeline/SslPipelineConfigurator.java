package io.reactivex.netty.pipeline;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.SslContext;

/**
 * @author Tomasz Bak
 */
public class SslPipelineConfigurator<I, O> implements PipelineConfigurator<I, O> {
    public static enum SecurityLevel {
        TRUSTED_SERVER,
        UNSECURE
    }

    protected SslContext sslCtx;

    public SslPipelineConfigurator(SslContext sslCtx) {
        this.sslCtx = sslCtx;
    }

    @Override
    public void configureNewPipeline(ChannelPipeline pipeline) {
        Channel ch = pipeline.channel();
        pipeline.addFirst(sslCtx.newHandler(ch.alloc()));
    }

    public abstract static class AbstractSslPipelineConfiguratorBuilder<T extends AbstractSslPipelineConfiguratorBuilder> {
        private SecurityLevel securityLevel;

        public T withSecurityLevel(SecurityLevel securityLevel) {
            this.securityLevel = securityLevel;
            return (T) this;
        }

        public SslPipelineConfigurator build() {
            if (null == securityLevel) {
                throw new IllegalStateException("no security level defined for SSL pipeline configurator");
            }
            switch (securityLevel) {
                case TRUSTED_SERVER: {
                    return buildTrustedServer();
                }
                case UNSECURE: {
                    return buildUnsecure();
                }
            }
            throw new IllegalStateException("unrecognized security level " + securityLevel);
        }

        protected abstract SslPipelineConfigurator buildTrustedServer();

        protected abstract SslPipelineConfigurator buildUnsecure();
    }

}
