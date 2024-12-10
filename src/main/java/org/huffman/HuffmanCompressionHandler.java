package org.huffman;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HuffmanCompressionHandler {
    private final int n;
    // frequencies of groups of bytes
    // TODO: Can't use List<Byte> in hashmap
    private Map<List<Byte>, Integer> freq;
    private File inputFile;
    private List<Byte> minimumBytesGroup;
    private int minimumBytesGroupLength;
    private int maximumBytesGroupLength;

    public HuffmanCompressionHandler(int n, File inputFile) {
        this.n = n;
        freq = new HashMap<>();
        this.inputFile = inputFile;
        minimumBytesGroupLength = Integer.MAX_VALUE;
        maximumBytesGroupLength = Integer.MIN_VALUE;
    }

    void calculateFrequency(byte[] chunk, int read) {
        // TODO: Last group is not added, if totSize not multiple of n
        for (int i = 0; i < Math.min(chunk.length, read) - n + 1; i++) {
            List<Byte> group = new ArrayList<>();
            for (int j = i; j < i + n; j++) {
                group.add(chunk[j]);
            }
            if (group.size() < minimumBytesGroupLength) {
                minimumBytesGroupLength = group.size();
                minimumBytesGroup = group;
            }
            maximumBytesGroupLength = Math.max(maximumBytesGroupLength, group.size());
            if (freq.containsKey(group)) {
                freq.put(group, freq.get(group) + 1);
            }
            else {
                freq.put(group, 1);
            }
        }
    }

    public Map<List<Byte>, Integer> getFrequencyMap() {
        return freq;
    }

    public void writeCompressedFile(Map<List<Byte>, List<Boolean>> codewordTable, int paddingLength) throws IOException {
        String path = inputFile.getParentFile().getAbsolutePath() + File.separator;
        String outputFileName = "21010217." + n + "." + inputFile.getName() + ".hc";
        File outputFile = new File(path + outputFileName);
        FileOutputStream fos = new FileOutputStream(outputFile);
        writeHeader(fos, codewordTable, paddingLength);
        writeData(fos, codewordTable);
        fos.close();
    }

    private void writeHeader(FileOutputStream fos, Map<List<Byte>, List<Boolean>> codewordTable, int paddingLength) throws IOException {
        // n
        writeIntegerToFile(fos, n);
        // number of entries
        writeIntegerToFile(fos, codewordTable.size());
        // number of padded 0 bits at the end of the file
        writeIntegerToFile(fos, paddingLength);

        writeAllKeysInMap(fos, codewordTable);
        writeAllValuesInMap(fos, codewordTable);
    }

    private void writeAllValuesInMap(FileOutputStream fos, Map<List<Byte>, List<Boolean>> codewordTable) throws IOException {
        List<Boolean> currentBits = new ArrayList<>();
        // save the minimum bytes group for the last
        for (Map.Entry<List<Byte>, List<Boolean>> entry : codewordTable.entrySet()){
            if ((maximumBytesGroupLength != minimumBytesGroupLength) &&
                    (minimumBytesGroupLength == entry.getKey().size())) {
                continue;
            }
            currentBits.addAll(entry.getValue());
            while (currentBits.size() >= 8) {
                convertFirst8BitsToByte(fos, currentBits);
            }

        }
        if (maximumBytesGroupLength != minimumBytesGroupLength) {
            currentBits.addAll(codewordTable.get(minimumBytesGroup));
            while (currentBits.size() >= 8) {
                convertFirst8BitsToByte(fos, currentBits);
            }
        }
        // pad with zeros if not empty then convert to byte
        if (currentBits.size() != 0 && currentBits.size() < 8) {
            for (int i = 0; i < 8 - currentBits.size(); i++) {
                currentBits.add(false);
            }
            convertFirst8BitsToByte(fos, currentBits);
        }
    }

    private void writeData(FileOutputStream fos, Map<List<Byte>, List<Boolean>> codewordTable) throws IOException {
        FileInputStream fis = new FileInputStream(inputFile);
        byte[] chunk = new byte[512 * n];
        int read = 0;
        while ( (read = fis.read(chunk)) > 0) {
            rewriteChunkUsingCodes(fos, chunk, codewordTable, read);
        }
        fis.close();
    }

    private void rewriteChunkUsingCodes(FileOutputStream fos, byte[] chunk,
                                        Map<List<Byte>, List<Boolean>> codewordTable, int read) throws IOException {
        List<Byte> current = new ArrayList<>();
        List<Boolean> currentBits = new ArrayList<>();
        for (int i = 0; i < Math.min(chunk.length, read); ++i) {
            current.add(chunk[i]);
            if (codewordTable.containsKey(current)){
                // fos.write(convertFromByteList(codewordTable.get(current)));
                currentBits.addAll(codewordTable.get(current));
                while (currentBits.size() >= 8) {
                    convertFirst8BitsToByte(fos, currentBits);
                }
                current.clear();
            }
        }
        // pad with zeros if not empty then convert to byte
        if (currentBits.size() != 0 && currentBits.size() < 8) {
            for (int i = 0; i < 8 - currentBits.size(); i++) {
                currentBits.add(false);
            }
            convertFirst8BitsToByte(fos, currentBits);
        }
    }

    private void convertFirst8BitsToByte(FileOutputStream fos, List<Boolean> currentBits) throws IOException {
        // TODO: Remove first 8 and write them using fos as a byte
        byte value = 0;
        for (int j = 0; j < 8; j++) {
            // if it's a set bit
            if (currentBits.get(j)) {
                value |= (1<<(7-j));
            }
        }
        fos.write(value);
        currentBits.subList(0, 8).clear();
    }

    private void writeAllKeysInMap(FileOutputStream fos, Map<List<Byte>,
            List<Boolean>> codewordTable) throws IOException {
        // save the minimum bytes group for the last
        for (Map.Entry<List<Byte>, List<Boolean>> entry : codewordTable.entrySet()){
            if ((maximumBytesGroupLength != minimumBytesGroupLength) &&
                    (minimumBytesGroupLength == entry.getKey().size())) {
                continue;
            }
            fos.write(convertFromByteList(entry.getKey()));
        }
        if (maximumBytesGroupLength != minimumBytesGroupLength) {
            fos.write(convertFromByteList(minimumBytesGroup));
        }
    }

    private void writeIntegerToFile(FileOutputStream fos, int number) throws IOException {
        byte[] numberBytes = new byte[]{(byte) (number >> 24), (byte) (number >> 16),
                                        (byte) (number >> 8), (byte) number};
        fos.write(numberBytes);
    }

    private byte[] convertFromByteList(List<Byte> byteList) {
        byte[] byteArray = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            byteArray[i] = byteList.get(i);
        }
        return byteArray;
    }
}
