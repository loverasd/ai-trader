package gpt.writer.strategy;

import technicals.model.oscillator.RsiEntry;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class RsiWriteStrategy implements WriteStrategy<RsiEntry> {
    @Override
    public void writeToFile(String filename, RsiEntry[] entries) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (RsiEntry entry : entries) {
                writer.write(entry.toString());
                writer.newLine();
            }
        }
    }
}