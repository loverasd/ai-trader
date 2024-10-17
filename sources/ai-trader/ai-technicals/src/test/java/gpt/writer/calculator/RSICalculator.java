package gpt.writer.calculator;

import technicals.indicators.oscillator.RelativeStrengthIndex;
import technicals.model.TechCandle;
import technicals.model.oscillator.RsiEntry;

public class RSICalculator implements IndicatorCalculator<RsiEntry> {
    @Override
    public RsiEntry[] calculate(TechCandle[] candles) {
        return RelativeStrengthIndex.calculate(candles, 14);  // Example period: 14
    }
}