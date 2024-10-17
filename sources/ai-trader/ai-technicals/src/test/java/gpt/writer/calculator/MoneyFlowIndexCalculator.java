package gpt.writer.calculator;

import technicals.indicators.volume.MoneyFlowIndex;
import technicals.model.TechCandle;
import technicals.model.indicators.IndicatorEntry;

public class MoneyFlowIndexCalculator implements IndicatorCalculator<IndicatorEntry> {
    @Override
    public IndicatorEntry[] calculate(TechCandle[] candles) {
        return MoneyFlowIndex.calculate(candles, 14);  // Example period: 14
    }
}