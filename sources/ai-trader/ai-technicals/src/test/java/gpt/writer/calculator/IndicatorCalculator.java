package gpt.writer.calculator;

import technicals.model.TechCandle;

public interface IndicatorCalculator<T extends TechCandle> {
    T[] calculate(TechCandle[] candles);
}