package gpt.writer;

import exchanges.binance.BinanceApiClient;
import exchanges.binance.BinanceCandle;
import exchanges.binance.BinanceIntervalType;
import exchanges.binance.BinanceUtils;
import gpt.writer.calculator.IndicatorCalculator;
import gpt.writer.strategy.WriteStrategy;
import technicals.model.TechCandle;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TechnicalAnalysisToFile {
    final static String path = "/Users/chenpan/Desktop/btc-data";



    public static void main(String[] args) {
       String newPath = path + File.separator+ LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)+"-";

        try {
            File directory = new File(newPath);
            // 如果目录不存在，创建目录
            if (!directory.exists()) {
                directory.mkdirs();
            }
            List<BinanceCandle> lstBinanceCandles = BinanceApiClient.getKlines("BTCUSDT", BinanceIntervalType._4h, 300);
            TechCandle[] binanceCandles = BinanceUtils.toCandleArray(lstBinanceCandles);

            processIndicator("Bollinger", binanceCandles, newPath+"bollinger.txt");
            processIndicator("MACD", binanceCandles, newPath+"macd.txt");
            processIndicator("MoneyFlowIndex", binanceCandles, newPath+"moneyflowindex.txt");
            processIndicator("VWAP", binanceCandles, newPath+"vwap.txt");
            processIndicator("RSI", binanceCandles, newPath+"rsi.txt");
            // Add other indicators to process here

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private static <T extends TechCandle> void processIndicator(String indicator, TechCandle[] candles, String filename) throws IOException {
        IndicatorCalculator<T> calculator = (IndicatorCalculator<T>) IndicatorFactory.getCalculator(indicator);
        WriteStrategy<T> writeStrategy = (WriteStrategy<T>) IndicatorFactory.getWriteStrategy(indicator);
        if (calculator != null && writeStrategy != null) {
            T[] entries = calculator.calculate(candles);
            writeStrategy.writeToFile(filename, entries);
        } else {
            System.err.println("No calculator or write strategy found for indicator: " + indicator);
        }
    }
}
