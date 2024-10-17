package gpt.writer.strategy;

import technicals.model.TechCandle;
import technicals.model.indicators.BollingerEntry;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public interface WriteStrategy<T extends TechCandle> {
    void writeToFile(String filename, T[] entries) throws IOException;
}





// Similarly implement other WriteStrategy classes for different indicators
