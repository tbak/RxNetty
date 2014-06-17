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
import io.reactivex.netty.channel.ObservableConnection;
import io.reactivex.netty.pipeline.PipelineConfigurators;
import io.reactivex.netty.pipeline.SslClientPipelineConfigurator;
import rx.Observable;
import rx.functions.Func1;

import javax.net.ssl.SSLException;

import static io.reactivex.netty.examples.tcp.ssl.SslHelloWorldServer.DEFAULT_PORT;

/**
 * @author Tomasz Bak
 */
public class SslHelloWorldClient {

    private final int port;

    public SslHelloWorldClient(int port) {
        this.port = port;
    }

    public void sendHelloRequest() throws SSLException {
        SslClientPipelineConfigurator pipelineConfigurator = new SslClientPipelineConfigurator(PipelineConfigurators.textOnlyConfigurator());
        Object msg = RxNetty.createTcpClient("localhost", port, pipelineConfigurator).connect().flatMap(new Func1<ObservableConnection<String, String>, Observable<?>>() {
            @Override
            public Observable<?> call(final ObservableConnection<String, String> observableConnection) {
                System.out.println("Sending WELCOME");
                observableConnection.writeAndFlush("WELCOME");
                return observableConnection.getInput().map(new Func1<Object, String>() {
                    @Override
                    public String call(Object o) {
                        System.out.println("received echo reply: " + o);
                        return o.toString();
                    }
                });
            }
        }).toBlocking().last();

        System.out.println("Got message: " + msg);
    }

    public static void main(String[] args) {
        try {
            new SslHelloWorldClient(DEFAULT_PORT).sendHelloRequest();
        } catch (SSLException e) {
            e.printStackTrace();
        }
    }
}
