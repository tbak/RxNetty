package io.reactivex.netty.examples.tcp.ssl;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.server.RxServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static io.reactivex.netty.examples.tcp.ssl.SslHelloWorldClient.WELCOME_TEXT;
import static io.reactivex.netty.examples.tcp.ssl.SslHelloWorldServer.DEFAULT_PORT;

/**
 * @author Tomasz Bak
 */
public class SslHelloWorldTest {

    private RxServer<ByteBuf, ByteBuf> server;

    @Before
    public void setupHttpHelloServer() throws Exception {
        server = new SslHelloWorldServer(DEFAULT_PORT).createServer();
        server.start();
    }

    @After
    public void stopServer() throws InterruptedException {
        server.shutdown();
    }

    @Test
    public void testRequestReplySequence() throws Exception {
        SslHelloWorldClient client = new SslHelloWorldClient(DEFAULT_PORT);
        String reply = client.sendHelloRequest();
        Assert.assertTrue(reply.contains(WELCOME_TEXT));
    }
}
