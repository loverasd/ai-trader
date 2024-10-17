package gpt.writer.strategy;

import technicals.model.indicators.VWAPEntry;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class VWAPWriteStrategy implements WriteStrategy<VWAPEntry> {
    @Override
    public void writeToFile(String filename, VWAPEntry[] entries) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (VWAPEntry entry : entries) {
                writer.write(entry.toString());
                writer.newLine();
            }
        }
    }
}