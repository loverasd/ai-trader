package gpt.writer.strategy;

import technicals.model.indicators.BollingerEntry;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class BollingerWriteStrategy implements WriteStrategy<BollingerEntry> {
    @Override
    public void writeToFile(String filename, BollingerEntry[] entries) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (BollingerEntry entry : entries) {
                writer.write(entry.toString());
                writer.newLine();
            }
        }
    }
}