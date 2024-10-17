package gpt.writer.calculator;

import technicals.indicators.volume.VolumeWeightedAveragePrice;
import technicals.model.TechCandle;
import technicals.model.indicators.VWAPEntry;

public class VWAPCalculator implements IndicatorCalculator<VWAPEntry> {
    @Override
    public VWAPEntry[] calculate(TechCandle[] candles) {
        VWAPEntry[] calculate = VolumeWeightedAveragePrice.calculate(candles);
        return calculate;
    }

}