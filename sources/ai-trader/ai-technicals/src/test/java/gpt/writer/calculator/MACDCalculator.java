package gpt.writer.calculator;

import technicals.indicators.oscillator.MACD;
import technicals.model.TechCandle;
import technicals.model.oscillator.MACDEntry;

public class MACDCalculator implements IndicatorCalculator<MACDEntry> {
    @Override
    public MACDEntry[] calculate(TechCandle[] candles) {
        return MACD.calculate(candles, 12, 26, 9);
    }
}