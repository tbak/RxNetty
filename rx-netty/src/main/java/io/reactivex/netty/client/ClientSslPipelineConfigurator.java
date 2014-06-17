package io.reactivex.netty.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.reactivex.netty.pipeline.PipelineConfigurator;

import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.security.KeyStore;

/**
 * @author Tomasz Bak
 */
public class ClientSslPipelineConfigurator<I, O> implements PipelineConfigurator<I, O> {

    public enum SecurityLevel {
        TRUSTED_SERVER,
        UNSECURE
    }

    private SslContext sslCtx;

    ClientSslPipelineConfigurator(SslContext sslCtx) {
        this.sslCtx = sslCtx;
    }

    @Override
    public void configureNewPipeline(ChannelPipeline pipeline) {
        Channel ch = pipeline.channel();
        pipeline.addFirst(sslCtx.newHandler(ch.alloc()));
    }

    public static class ClientSslContextBuilder {
        private SecurityLevel securityLevel;
        private TrustManagerFactory trustManagerFactory;
        private File certChainFile;

        public ClientSslContextBuilder withSecurityLevel(SecurityLevel securityLevel) {
            this.securityLevel = securityLevel;
            return this;
        }

        public ClientSslContextBuilder withTrustManagerFactory(TrustManagerFactory trustManagerFactory) {
            this.trustManagerFactory = trustManagerFactory;
            return this;
        }

        public ClientSslPipelineConfigurator build() {
            if (null == securityLevel) {
                throw new IllegalStateException("no security level defined for ClientSslContext");
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

        private ClientSslPipelineConfigurator buildTrustedServer() {
            SslContext sslCtx;
            try {
                TrustManagerFactory tmf = trustManagerFactory;
                if (tmf == null) {
                    KeyStore jks = KeyStore.getInstance("JKS");
                    jks.load(null);
                    tmf = TrustManagerFactory.getInstance("SunX509");
                    tmf.init(jks);
                }
                sslCtx = SslContext.newClientContext(tmf);
            } catch (Exception e) {
                throw new IllegalStateException("Cannot create SSL context with JDK keystore", e);
            }
            return new ClientSslPipelineConfigurator(sslCtx);
        }

        private ClientSslPipelineConfigurator buildUnsecure() {
            SslContext sslCtx;
            try {
                sslCtx = SslContext.newClientContext(InsecureTrustManagerFactory.INSTANCE);
            } catch (SSLException e) {
                throw new IllegalStateException("Cannot create SSL context with InsecureTrustManagerFactory", e);
            }
            return new ClientSslPipelineConfigurator(sslCtx);
        }
    }

}
