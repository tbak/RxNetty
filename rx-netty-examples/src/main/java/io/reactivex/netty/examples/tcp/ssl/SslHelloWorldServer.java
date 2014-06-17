/*
 * Copyright 2014 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.reactivex.netty.examples.tcp.ssl;

import io.reactivex.netty.RxNetty;
import io.reactivex.netty.channel.ConnectionHandler;
import io.reactivex.netty.channel.ObservableConnection;
import io.reactivex.netty.pipeline.PipelineConfigurators;
import io.reactivex.netty.pipeline.SslServerPipelineConfigurator;
import io.reactivex.netty.server.RxServer;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Func1;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;

/**
 * @author Tomasz Bak
 */
public final class SslHelloWorldServer {

    static final int DEFAULT_PORT = 8104;

    private int port;

    public SslHelloWorldServer(int port) {
        this.port = port;
    }

    public RxServer<String, String> createServer() throws CertificateException, SSLException {
        SslServerPipelineConfigurator pipelineConfigurator = new SslServerPipelineConfigurator(PipelineConfigurators.textOnlyConfigurator());

        RxServer<String, String> server = RxNetty.createTcpServer(port, pipelineConfigurator,
                new ConnectionHandler<String, String>() {
                    @Override
                    public Observable<Void> handle(final ObservableConnection<String, String> connection) {
                        System.out.println("New client connection established.");
                        return connection.getInput().map(new Func1<String, String>() {
                            @Override
                            public String call(String s) {
                                System.out.println("received: " + s);
                                return s;
                            }
                        }).flatMap(new Func1<String, Observable<Void>>() {
                            @Override
                            public Observable<Void> call(String s) {
                                System.out.println("sending echo reply: " + s);
                                return connection.writeAndFlush("echo>> " + s).doOnCompleted(new Action0() {
                                    @Override
                                    public void call() {
                                        connection.close();
                                    }
                                });
                            }
                        });
                    }
                });
        return server;
    }

    public static void main(final String[] args) {
        try {
            new SslHelloWorldServer(DEFAULT_PORT).createServer().startAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
