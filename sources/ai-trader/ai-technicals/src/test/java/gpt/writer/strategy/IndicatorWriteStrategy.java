package gpt.writer.strategy;

import technicals.model.indicators.IndicatorEntry;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class IndicatorWriteStrategy implements WriteStrategy<IndicatorEntry> {
    @Override
    public void writeToFile(String filename, IndicatorEntry[] entries) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (IndicatorEntry entry : entries) {
                writer.write(entry.toString());
                writer.newLine();
            }
        }
    }
}