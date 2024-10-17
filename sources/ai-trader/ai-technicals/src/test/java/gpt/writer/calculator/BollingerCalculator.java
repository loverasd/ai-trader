package gpt.writer.calculator;

import technicals.indicators.volatility.Bollinger;
import technicals.model.TechCandle;
import technicals.model.indicators.BollingerEntry;

public class BollingerCalculator implements IndicatorCalculator<BollingerEntry> {
    @Override
    public BollingerEntry[] calculate(TechCandle[] candles) {
        return Bollinger.calculate(candles, 20, 2);
    }
}