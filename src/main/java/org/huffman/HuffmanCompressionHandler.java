package org.huffman;

import java.io.*;
import java.util.*;

public class HuffmanCompressionHandler {
    private final int n;
    // frequencies of groups of bytes
    // TODO: Can't use List<Byte> in hashmap
    private Map<String, Integer> freq;
    private File inputFile;
    private String minimumBytesGroup;
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
        for (int i = 0; i < Math.min(chunk.length, read); i+=n) {
            StringBuilder group = new StringBuilder("");
            int groupSize = 0;
            for (int j = i; j < Math.min(i + n, Math.min(chunk.length, read)); j++) {
                group.append(chunk[j]).append(" ");
                groupSize++;
            }
            // there's trailing space
            group.deleteCharAt(group.length() - 1);
            groupSize--;
            if (groupSize < minimumBytesGroupLength) {
                minimumBytesGroupLength = groupSize;
                minimumBytesGroup = group.toString();
            }
            maximumBytesGroupLength = Math.max(maximumBytesGroupLength, groupSize);
            if (freq.containsKey(group.toString())) {
                freq.put(group.toString(), freq.get(group.toString()) + 1);
            }
            else {
                freq.put(group.toString(), 1);
            }
        }
    }

    public Map<String, Integer> getFrequencyMap() {
        return freq;
    }

    public void writeCompressedFile(Map<String, List<Boolean>> codewordTable, int paddingLength) throws IOException {
        String path = inputFile.getParentFile().getAbsolutePath() + File.separator;
        String outputFileName = "21010217." + n + "." + inputFile.getName() + ".hc";
        File outputFile = new File(path + outputFileName);
        FileOutputStream fos = new FileOutputStream(outputFile);
        writeHeader(fos, codewordTable, paddingLength);
        writeData(fos, codewordTable);
        fos.close();
    }

    private void writeHeader(FileOutputStream fos, Map<String, List<Boolean>> codewordTable, int paddingLength) throws IOException {
        // n
        writeIntegerToFile(fos, n);
        // number of entries
        writeIntegerToFile(fos, codewordTable.size());
        // number of padded 0 bits at the end of the file
        writeIntegerToFile(fos, paddingLength);

        writeAllKeysInMap(fos, codewordTable);
        writeAllValuesInMap(fos, codewordTable);
    }

    private void writeAllValuesInMap(FileOutputStream fos, Map<String, List<Boolean>> codewordTable) throws IOException {
        List<Boolean> currentBits = new ArrayList<>();
        // first write the minimum group value (if exist)
        if (maximumBytesGroupLength != minimumBytesGroupLength) {
            currentBits.addAll(codewordTable.get(minimumBytesGroup));
            while (currentBits.size() >= 8) {
                convertFirst8BitsToByte(fos, currentBits);
            }
        }
        for (Map.Entry<String, List<Boolean>> entry : codewordTable.entrySet()){
            if ((maximumBytesGroupLength != minimumBytesGroupLength) &&
                    (entry.getKey().equals(minimumBytesGroup))) {
                continue;
            }
            currentBits.addAll(entry.getValue());
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

    private void writeData(FileOutputStream fos, Map<String, List<Boolean>> codewordTable) throws IOException {
        FileInputStream fis = new FileInputStream(inputFile);
        byte[] chunk = new byte[512 * n];
        int read = 0;
        while ( (read = fis.read(chunk)) > 0) {
            rewriteChunkUsingCodes(fos, chunk, codewordTable, read);
        }
        fis.close();
    }

    private void rewriteChunkUsingCodes(FileOutputStream fos, byte[] chunk,
                                        Map<String, List<Boolean>> codewordTable, int read) throws IOException {
        String current = "";
        List<Boolean> currentBits = new ArrayList<>();
        for (int i = 0; i < Math.min(chunk.length, read); ++i) {
            current += chunk[i];
            String tempCurrent = current;
            current += " ";
            if (codewordTable.containsKey(tempCurrent)){
                // fos.write(convertFromByteList(codewordTable.get(current)));
                currentBits.addAll(codewordTable.get(tempCurrent));
                while (currentBits.size() >= 8) {
                    convertFirst8BitsToByte(fos, currentBits);
                }
                current = "";
            }
        }
        // pad with zeros if not empty then convert to byte
        if (currentBits.size() != 0 && currentBits.size() < 8) {
            int size = currentBits.size();
            for (int i = 0; i < 8 - size; i++) {
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

    private void writeAllKeysInMap(FileOutputStream fos, Map<String, List<Boolean>> codewordTable) throws IOException {
        // first add the minimum bytes group key
        if (maximumBytesGroupLength != minimumBytesGroupLength) {
            fos.write(convertFromByteString(minimumBytesGroup));
        }
        for (Map.Entry<String, List<Boolean>> entry : codewordTable.entrySet()){
            if ((maximumBytesGroupLength != minimumBytesGroupLength) &&
                    (minimumBytesGroup.equals(entry.getKey()))) {
                continue;
            }
            fos.write(convertFromByteString(entry.getKey()));
        }
    }

    private void writeIntegerToFile(FileOutputStream fos, int number) throws IOException {
        byte[] numberBytes = new byte[]{(byte) (number >> 24), (byte) (number >> 16),
                                        (byte) (number >> 8), (byte) number};
        fos.write(numberBytes);
    }

    private byte[] convertFromByteString(String byteString) {
        String[] byteStrings = byteString.split(" ");

        byte[] byteArray = new byte[byteStrings.length];
        for (int i = 0; i < byteStrings.length; i++) {
            byteArray[i] = Byte.parseByte(byteStrings[i]);
        }
        return byteArray;
    }
}
