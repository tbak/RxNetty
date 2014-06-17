package io.reactivex.netty.pipeline;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import javax.net.ssl.SSLException;
import java.io.File;
import java.security.cert.CertificateException;

/**
 * @author Tomasz Bak
 */
public class ServerSslPipelineConfigurator<I, O> extends SslPipelineConfigurator<I, O> {

    ServerSslPipelineConfigurator(SslContext sslCtx) {
        super(sslCtx);
    }

    public static class ServerSslPipelineConfiguratorBuilder extends AbstractSslPipelineConfiguratorBuilder<ServerSslPipelineConfiguratorBuilder> {
        private File certChainFile;
        private File keyFile;
        private String keyPassword;

        public ServerSslPipelineConfiguratorBuilder withCertChainFile(File certChainFile) {
            this.certChainFile = certChainFile;
            return this;
        }

        public ServerSslPipelineConfiguratorBuilder withKeyFile(File keyFile) {
            this.keyFile = keyFile;
            return this;
        }

        public ServerSslPipelineConfiguratorBuilder withKeyPassword(String keyPassword) {
            this.keyPassword = keyPassword;
            return this;
        }

        @Override
        protected SslPipelineConfigurator buildTrustedServer() {
            if (null == certChainFile) {
                throw new IllegalStateException("Certificate chain file not configured");
            }
            if (null == keyFile) {
                throw new IllegalStateException("Private key file not configured");
            }
            SslContext sslContext;
            try {
                sslContext = SslContext.newServerContext(certChainFile, keyFile, keyPassword);
            } catch (SSLException e) {
                throw new IllegalStateException("Cannot create SSL context with the provided certificate/private key files", e);
            }
            return new ServerSslPipelineConfigurator(sslContext);
        }

        @Override
        protected SslPipelineConfigurator buildUnsecure() {
            SelfSignedCertificate selfSigned;
            try {
                selfSigned = new SelfSignedCertificate();
            } catch (CertificateException e) {
                throw new IllegalStateException("Cannot create self signed certificate", e);
            }
            SslContext sslContext;
            try {
                sslContext = SslContext.newServerContext(selfSigned.certificate(), selfSigned.privateKey());
            } catch (SSLException e) {
                throw new IllegalStateException("Cannot create SSL context with auto-generated self signed certificate", e);
            }
            return new ServerSslPipelineConfigurator(sslContext);
        }
    }

}
