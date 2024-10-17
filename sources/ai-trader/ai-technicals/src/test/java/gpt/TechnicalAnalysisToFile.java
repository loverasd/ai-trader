package gpt;

import exchanges.binance.BinanceApiClient;
import exchanges.binance.BinanceCandle;
import exchanges.binance.BinanceIntervalType;
import exchanges.binance.BinanceUtils;
import exchanges.kucoin.KucoinApiClient;
import exchanges.kucoin.KucoinCandle;
import exchanges.kucoin.KucoinCandleUtils;
import exchanges.kucoin.KucoinIntervalType;
import technicals.indicators.oscillator.Stochastic;
import technicals.indicators.volatility.Bollinger;
import technicals.model.TechCandle;
import technicals.model.indicators.BollingerEntry;
import technicals.model.oscillator.StochasticEntry;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;


public class TechnicalAnalysisToFile {

    public static void main(String[] args) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("/Users/chenpan/IdeaProjects/ai-trader/sources/ai-trader/ai-technicals/src/test/java/gpt/output.txt"));
            
            // Example 1: Bollinger Bands
            List<BinanceCandle> lstBinanceCandles = BinanceApiClient.getKlines("BTCUSDT", BinanceIntervalType._15m, 50);
            TechCandle[] binanceCandles = BinanceUtils.toCandleArray(lstBinanceCandles);
            BollingerEntry[] bollingerEntries = Bollinger.calculate(binanceCandles, 20, 2);
            writeEntriesToFile(writer, "Bollinger", "BTCUSDT", "1 day", bollingerEntries);
            
//            // Example 2: Stochastic
//            List<KucoinCandle> lstKucoinCandle = KucoinApiClient.getKlines("BTC-USDT", KucoinIntervalType._1d, 30);
//            TechCandle[] kucoinCandles = KucoinCandleUtils.toCandleArray(lstKucoinCandle);
//            StochasticEntry[] stochasticEntries = Stochastic.calculate(kucoinCandles, 14, 1, 3);
//            writeEntriesToFile(writer, "Stochastic", "BTC-USDT", "1 day", stochasticEntries);
//
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeEntriesToFile(BufferedWriter writer, String indicator, String pair, String interval, Object[] entries) throws IOException {
        writer.write("Indicator: " + indicator + "\n");
        writer.write("Pair: " + pair + "\n");
        writer.write("Interval: " + interval + "\n");
        writer.write("Entries:\n");
        for (Object entry : entries) {
            writer.write(entry == null ? "null" : entry.toString());
            writer.write("\n");
        }
        writer.write("\n========================\n\n");
    }
}
