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

package io.reactivex.netty.examples.http.local;

import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalEventLoopGroup;
import io.netty.channel.local.LocalServerChannel;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.protocol.http.client.HttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientBuilder;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import io.reactivex.netty.protocol.http.server.HttpServer;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import rx.Observable;
import rx.functions.Func1;

/**
 * This example illustrates how to use {@link io.netty.channel.local.LocalChannel}.
 *
 *
 * @author Tomasz Bak
 */
public class LocalChannelExample {

    public final LocalAddress LOCAL_ADDRESS = new LocalAddress("local.1");
    private HttpServer<ByteBuf, ByteBuf> server;

    public void setupServer() {
        server = RxNetty.newHttpServerBuilder(-1, new RequestHandler<ByteBuf, ByteBuf>() {
            @Override
            public Observable<Void> handle(HttpServerRequest<ByteBuf> request, final HttpServerResponse<ByteBuf> response) {
                response.writeString("Welcome!!");
                return response.close(false);
            }
        })
                .channel(LocalServerChannel.class)
                .localAddress(LOCAL_ADDRESS)
                .eventLoop(new LocalEventLoopGroup())
                .build();
        server.start();
    }

    public void submitRequest() {
        HttpClient<ByteBuf, ByteBuf> builder = new HttpClientBuilder<ByteBuf, ByteBuf>(null, -1)
                .channel(LocalChannel.class)
                .remoteAddress(LOCAL_ADDRESS)
                .eventloop(new LocalEventLoopGroup())
                .build();
        Observable<String> observable = builder.submit(HttpClientRequest.createGet("/hello")).flatMap(new Func1<HttpClientResponse<ByteBuf>, Observable<String>>() {
            @Override
            public Observable<String> call(HttpClientResponse<ByteBuf> httpClientResponse) {
                return httpClientResponse.getContent().map(new Func1<ByteBuf, String>() {
                    @Override
                    public String call(ByteBuf byteBuf) {
                        return byteBuf.toString(Charset.defaultCharset());
                    }
                });
            }
        });
        System.out.println("Reply from server: " + observable.toBlocking().single());
    }

    private void shutdown() {
        if (server != null) {
            try {
                server.shutdown();
            } catch (InterruptedException e) {
                // IGNORE
            }
        }
    }

    public static void main(String[] args) {
        LocalChannelExample example = new LocalChannelExample();
        example.setupServer();
        example.submitRequest();
        example.shutdown();
    }
}
