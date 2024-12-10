package org.huffman;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HuffmanCompressionHandler {
    private final int n;
    // frequencies of groups of bytes
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

    void calculateFrequency(byte[] chunk) {
        // TODO: Last group is not added, if totSize not multiple of n
        for (int i = 0; i < chunk.length - n + 1; i++) {
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

    public void writeCompressedFile(Map<List<Byte>, List<Byte>> codewordTable) throws IOException {
        String path = inputFile.getParentFile().getAbsolutePath() + File.separator;
        String outputFileName = "21010217." + n + "." + inputFile.getName() + ".hc";
        File outputFile = new File(path + outputFileName);
        FileOutputStream fos = new FileOutputStream(outputFile);
        writeHeader(fos, codewordTable);
        writeData(fos, codewordTable);
        fos.close();
    }

    private void writeHeader(FileOutputStream fos, Map<List<Byte>, List<Byte>> codewordTable) throws IOException {
        writeIntegerToFile(fos, n);

        // write the minimum bytes group, if There was
        if (maximumBytesGroupLength != minimumBytesGroupLength) {
            writeIntegerToFile(fos, minimumBytesGroupLength);
            writeMapping(fos, minimumBytesGroup, codewordTable);
        }
        else {
            // write 0
            writeIntegerToFile(fos, 0);
            minimumBytesGroupLength = 0;
        }

        writeAllMappings(fos, codewordTable);
    }

    private void writeData(FileOutputStream fos, Map<List<Byte>, List<Byte>> codewordTable) throws IOException {
        FileInputStream fis = new FileInputStream(inputFile);
        byte[] chunk = new byte[512 * n];
        int read = 0;
        while ( (read = fis.read(chunk)) > 0) {
            rewriteChunkUsingCodes(fos, chunk, codewordTable);
        }
    }

    private void rewriteChunkUsingCodes(FileOutputStream fos, byte[] chunk,
                                        Map<List<Byte>, List<Byte>> codewordTable) throws IOException {
        List<Byte> current = new ArrayList<>();
        for (int i = 0; i < chunk.length; ++i) {
            current.add(chunk[i]);
            if (codewordTable.containsKey(current)){
                fos.write(convertFromByteList(codewordTable.get(current)));
                current.clear();
            }
        }
    }

    private void writeAllMappings(FileOutputStream fos, Map<List<Byte>, List<Byte>> codewordTable) throws IOException {
        for (Map.Entry<List<Byte>, List<Byte>> entry : codewordTable.entrySet()){   
            if (entry.getKey().size() == minimumBytesGroupLength)
                continue;
            writeMapping(fos, entry.getKey(), codewordTable);
        }
    }

    private void writeIntegerToFile(FileOutputStream fos, int number) throws IOException {
        byte[] numberBytes = new byte[]{(byte) (number >> 24), (byte) (number >> 16),
                                        (byte) (number >> 8), (byte) number};
        fos.write(numberBytes);
    }

    private void writeMapping(FileOutputStream fos, List<Byte> actualValue,
                             Map<List<Byte>, List<Byte>> codewordTable) throws IOException {
        fos.write(convertFromByteList(actualValue));
        List<Byte> huffmanCoded = codewordTable.get(actualValue);
        writeIntegerToFile(fos, huffmanCoded.size());
        fos.write(convertFromByteList(huffmanCoded));
    }
    private byte[] convertFromByteList(List<Byte> byteList) {
        byte[] byteArray = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            byteArray[i] = byteList.get(i);
        }
        return byteArray;
    }
}
