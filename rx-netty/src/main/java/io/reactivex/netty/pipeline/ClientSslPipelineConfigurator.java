package io.reactivex.netty.pipeline;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;

/**
 * @author Tomasz Bak
 */
public class ClientSslPipelineConfigurator<I, O> extends SslPipelineConfigurator<I, O> {

    ClientSslPipelineConfigurator(SslContext sslCtx) {
        super(sslCtx);
    }

    public static class ClientSslPipelineConfiguratorBuilder extends AbstractSslPipelineConfiguratorBuilder<ClientSslPipelineConfiguratorBuilder> {
        private TrustManagerFactory trustManagerFactory;

        public ClientSslPipelineConfiguratorBuilder withTrustManagerFactory(TrustManagerFactory trustManagerFactory) {
            this.trustManagerFactory = trustManagerFactory;
            return this;
        }

        @Override
        protected SslPipelineConfigurator buildTrustedServer() {
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

        @Override
        protected SslPipelineConfigurator buildInsecure() {
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
