package aitrader.core.service.strategy;

import binance.BinanceApiClient;
import binance.BinanceCandle;
import binance.BinanceIntervalType;
import binance.BinanceUtils;
import technicals.model.TechCandle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 主类，运行交易策略
 */
public class TradingBot {
    public static void main(String[] args) throws Exception {
        // 加载或生成不同周期的K线数据
        List<TechCandle> candles15M = loadCandles("15m");
        List<TechCandle> candles30M = loadCandles("30m");
        List<TechCandle> candles1H = loadCandles("1h");
        List<TechCandle> candles4H = loadCandles("4h");
        List<TechCandle> candles1D = loadCandles("1d");

        // 初始化策略和账户余额
        TradingStrategy strategy = new TradingStrategy(100000); // 初始账户余额$100,000

        // 执行策略
        strategy.executeStrategy(candles15M, candles30M, candles1H, candles4H, candles1D);
    }


    private static List<TechCandle> loadCandles(String timeframe) throws Exception {
        // 从数据源加载历史数据，或生成模拟数据
        List<BinanceCandle> result = new ArrayList<>();
        switch (timeframe) {
            case "15m":
                result = BinanceApiClient.getKlines("BTCUSDT", BinanceIntervalType._15m, 500);
                break;
            case "30m":
                result = BinanceApiClient.getKlines("BTCUSDT", BinanceIntervalType._30m, 600);

                break;
            case "1h":
                result = BinanceApiClient.getKlines("BTCUSDT", BinanceIntervalType._1h, 600);

                break;
            case "4h":
                result = BinanceApiClient.getKlines("BTCUSDT", BinanceIntervalType._4h, 300);
                break;
            case "1d":
                result = BinanceApiClient.getKlines("BTCUSDT", BinanceIntervalType._1d, 300);

                break;
        }

        technicals.model.TechCandle[] candleArray = BinanceUtils.toCandleArray(result);

        return Arrays.asList(candleArray);
    }

}