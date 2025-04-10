package com.cp.aitg.config;

import com.binance.connector.client.SpotClient;
import com.binance.connector.client.impl.SpotClientImpl;
import com.binance.connector.client.utils.ProxyAuth;
import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.binance.connector.client.WebSocketStreamClient;
import com.binance.connector.client.impl.WebSocketStreamClientImpl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;

@Configuration
public class WebSocketConfig {

    @Bean
    public WebSocketStreamClient webSocketStreamClient() {
        return new WebSocketStreamClientImpl();
    }

    @Bean
    public SpotClient binanceClient() {
        SpotClientImpl spotClient = new SpotClientImpl(System.getProperty("binance.api.key"),
                System.getProperty("binance.api.secret"));

//        spotClient.setProxy(proxyAuth());
        return spotClient;
    }

    public ProxyAuth proxyAuth() {
        // 创建代理对象
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 33210));


       return new ProxyAuth(proxy, new Authenticator() {
            @Nullable
            @Override
            public Request authenticate(@Nullable Route route, @NotNull Response response) throws IOException {
                return null;
            }
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("username", "password".toCharArray());
            }
        });
    }
}
