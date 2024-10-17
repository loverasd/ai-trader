package gpt.writer;

import gpt.writer.calculator.*;
import gpt.writer.strategy.*;
import technicals.model.TechCandle;

import java.util.HashMap;
import java.util.Map;

public class IndicatorFactory {
    private static final Map<String, IndicatorCalculator> calculators = new HashMap<>();
    private static final Map<String, WriteStrategy<? extends TechCandle>> writeStrategies = new HashMap<>();

    static {
        calculators.put("Bollinger", new BollingerCalculator());
        calculators.put("MACD", new MACDCalculator());
        calculators.put("MoneyFlowIndex", new MoneyFlowIndexCalculator());
        calculators.put("VWAP", new VWAPCalculator());
        calculators.put("RSI", new RSICalculator());
        // Add other calculators here

        writeStrategies.put("Bollinger", new BollingerWriteStrategy());
        writeStrategies.put("MACD", new MACDWriteStrategy());
        writeStrategies.put("MoneyFlowIndex", new IndicatorWriteStrategy());  // Generic strategy
        writeStrategies.put("VWAP", new VWAPWriteStrategy());  // Generic strategy
        writeStrategies.put("RSI", new RsiWriteStrategy());
        // Add other write strategies here
    }
    public static IndicatorCalculator getCalculator(String indicator) {
        return calculators.get(indicator);
    }

    public static WriteStrategy<? extends TechCandle> getWriteStrategy(String indicator) {
        return writeStrategies.get(indicator);
    }
}
