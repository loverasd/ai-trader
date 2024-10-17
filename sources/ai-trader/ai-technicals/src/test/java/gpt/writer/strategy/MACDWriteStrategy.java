package gpt.writer.strategy;

import technicals.model.oscillator.MACDEntry;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class MACDWriteStrategy implements WriteStrategy<MACDEntry> {
    @Override
    public void writeToFile(String filename, MACDEntry[] entries) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (MACDEntry entry : entries) {
                writer.write(entry.toString());
                writer.newLine();
            }
        }
    }
}