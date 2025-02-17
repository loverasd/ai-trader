package binance.futures.impl.my;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import binance.futures.commons.BinanceException;
import binance.futures.commons.ResponseStatus;
import binance.futures.config.ApiConstants;
import binance.futures.enums.IntervalType;
import binance.futures.model.Candle;
import binance.futures.model.Depth;
import binance.futures.model.ExchangeInfo;
import binance.futures.model.FundingRate;
import binance.futures.model.PremiumIndex;
import binance.futures.model.SymbolTicker;

public class UnsignedClient {

    // 创建单例的 HttpClient 和 ObjectMapper 实例
    private static final HttpClient httpClient = HttpClient.newBuilder().build();
    private static final ObjectMapper mapper = new ObjectMapper();
    // 创建单例的 Client 实例
    private static final Client client = ClientBuilder.newClient();

    // 构建 URI 方法，接受路径和查询参数
    private static URI buildUri(String path, String... queryParams) {
        WebTarget target = client.target(ApiConstants.BASE_URL).path(path);
        for (int i = 0; i < queryParams.length; i += 2) {
            target = target.queryParam(queryParams[i], queryParams[i + 1]);
        }
        return target.getUri();
    }

    // 处理响应并转换为指定类型的方法
    private static <T> T handleResponse(HttpResponse<String> response, Class<T> responseType) throws Exception {
        if (response.statusCode() != 200) {
            ResponseStatus responseStatus = ResponseStatus.from(response.body());
            throw new BinanceException(responseStatus.getCode() + " : " + responseStatus.getMsg());
        }
        return mapper.readValue(response.body(), responseType);
    }
    // 处理响应并转换为指定类型列表的方法
    private static <T> List<T> handleResponse(HttpResponse<String> response, TypeReference<List<T>> typeReference) throws Exception {
        if (response.statusCode() != 200) {
            ResponseStatus responseStatus = ResponseStatus.from(response.body());
            throw new BinanceException(responseStatus.getCode() + " : " + responseStatus.getMsg());
        }
        return mapper.readValue(response.body(), typeReference);
    }

    // 获取 Kline 数据的方法，异步返回 Kline 列表
    public static CompletableFuture<List<Candle>> getKlines(String symbol, IntervalType interval, int limit) {
        URI uri = buildUri("/fapi/v2/klines", "symbol", symbol, "interval", interval.getCode(), "limit", String.valueOf(limit));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        return httpClient.sendAsync(request, BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        String[][] lst = handleResponse(response, String[][].class);
                        List<Candle> lstResult = new ArrayList<>();
                        for (String[] entry : lst) {
                            Candle kline = new Candle();
                            kline.setOpenTime(Long.valueOf(entry[0]));
                            kline.setOpenPrice(new BigDecimal(entry[1]));
                            kline.setHighPrice(new BigDecimal(entry[2]));
                            kline.setLowPrice(new BigDecimal(entry[3]));
                            kline.setClosePrice(new BigDecimal(entry[4]));
                            kline.setVolume(new BigDecimal(entry[5]));
                            kline.setQuoteVolume(new BigDecimal(entry[7]));
                            kline.setCount(Long.valueOf(entry[8]));
                            lstResult.add(kline);
                        }
                        return lstResult;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    // 获取交易所信息的方法，异步返回 ExchangeInfo 对象
    public static CompletableFuture<ExchangeInfo> getExchangeInformation() {
        URI uri = buildUri("/fapi/v2/exchangeInfo");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        return httpClient.sendAsync(request, BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        return handleResponse(response, ExchangeInfo.class);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    // 获取所有交易对的24小时价格变化数据的方法，异步返回 SymbolTicker 列表
    public static CompletableFuture<List<SymbolTicker>> getSymbolTickers() {
        URI uri = buildUri("/fapi/v2/ticker/24hr");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        return httpClient.sendAsync(request, BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        return handleResponse(response, new TypeReference<List<SymbolTicker>>() {});
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public static CompletableFuture<List<FundingRate>> getFundingRate(String symbol, Integer limit) {
        URI uri = buildUri("/fapi/v2/fundingRate", "symbol", symbol, "limit", String.valueOf(limit));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        return httpClient.sendAsync(request, BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        return handleResponse(response, new TypeReference<List<FundingRate>>() {});
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public static CompletableFuture<List<PremiumIndex>> getPremiumIndex(String symbol) {
        URI uri = buildUri("/fapi/v2/premiumIndex", "symbol", symbol);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        return httpClient.sendAsync(request, BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        return handleResponse(response, new TypeReference<List<PremiumIndex>>() {});
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public static CompletableFuture<Depth> getDepth(String symbol) {
        return getDepth(symbol, ApiConstants.MAX_DEPTH_LIMIT);
    }

    public static CompletableFuture<Depth> getDepth(String symbol, int limit) {
        URI uri = buildUri("/fapi/v2/depth", "symbol", symbol, "limit", String.valueOf(limit));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        return httpClient.sendAsync(request, BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        return handleResponse(response, Depth.class);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
