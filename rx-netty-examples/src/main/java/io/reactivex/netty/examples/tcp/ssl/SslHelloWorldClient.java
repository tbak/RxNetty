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

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.channel.ObservableConnection;
import io.reactivex.netty.client.RxClient;
import rx.Observable;
import rx.functions.Func1;

import java.nio.charset.Charset;

import static io.reactivex.netty.examples.tcp.ssl.SslHelloWorldServer.DEFAULT_PORT;

/**
 * @author Tomasz Bak
 */
public class SslHelloWorldClient {

    static final String WELCOME_TEXT = "WELCOME";

    private final int port;

    public SslHelloWorldClient(int port) {
        this.port = port;
    }

    public String sendHelloRequest() throws Exception {
        RxClient<ByteBuf, ByteBuf> rxClient = RxNetty.createSslUnsecureTcpClient("localhost", port);

        String msg = rxClient.connect().flatMap(new Func1<ObservableConnection<ByteBuf, ByteBuf>, Observable<String>>() {
            @Override
            public Observable<String> call(final ObservableConnection<ByteBuf, ByteBuf> observableConnection) {
                System.out.println("Sending WELCOME");
                observableConnection.writeStringAndFlush(WELCOME_TEXT);
                return observableConnection.getInput().map(new Func1<ByteBuf, String>() {
                    @Override
                    public String call(ByteBuf content) {
                        String s = content.toString(Charset.defaultCharset());
                        System.out.println("received echo reply: " + s);
                        return s;
                    }
                });
            }
        }).toBlocking().last();

        System.out.println("Got message: " + msg);

        return msg;
    }

    public static void main(String[] args) {
        try {
            new SslHelloWorldClient(DEFAULT_PORT).sendHelloRequest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
