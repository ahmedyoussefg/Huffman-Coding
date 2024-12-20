package org.huffman;

import java.io.*;
import java.util.*;

public class HuffmanCompressionHandler {
    private final int n;
    // frequencies of groups of bytes
    private Map<ByteArrayWrapper, Integer> freq;
    private File inputFile;
    private ByteArrayWrapper minimumBytesGroup;
    private int minimumBytesGroupLength;
    private final long inputFileSize;
    byte[] group;
    StringBuilder groupBuilder;
    ByteArrayWrapper groupWrapper;
    public HuffmanCompressionHandler(int n, String inputPath) {
        this.n = n;
        freq = new HashMap<>();
        this.inputFile = new File(inputPath);
        this.inputFileSize = inputFile.length();
        minimumBytesGroupLength = Integer.MAX_VALUE;
        groupBuilder = new StringBuilder();
        groupWrapper = new ByteArrayWrapper();
        group = new byte[n];
    }
    void process() throws IOException {
        long startProcessTime = System.currentTimeMillis();
        // Read file in chunks
        FileInputStream fis = new FileInputStream(inputFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        byte[] chunk = new byte[1024 * 1024 * n];
        long nowFreqTime = System.currentTimeMillis();
        int read = 0;
        System.out.println("Calculating Frequencies..");
        while ( (read = bis.read(chunk)) > 0) {
            this.calculateFrequency(chunk, read);
        }
        System.out.print("Time taken calculating frequencies (sec): ");
        System.out.println((System.currentTimeMillis()-nowFreqTime)/1000.0);
        HuffmanCode huffmanCode = new HuffmanCode();
        System.out.println("Building Huffman Tree..");
        huffmanCode.buildTree(this.getFrequencyMap());
        System.out.println("Building Huffman Codeword Table..");
        Map<ByteArrayWrapper, List<Boolean>> codewordTable = huffmanCode.buildCodewordTable();
        this.writeCompressedFile(codewordTable, huffmanCode.getTotalCodeLength());
        System.out.print("> Total Time Taken To Compress The File (sec): ");
        System.out.println((System.currentTimeMillis()-startProcessTime)/1000.0);
        bis.close();
    }
    void calculateFrequency(byte[] chunk, int read)  {
        for (int i = 0; i < read; i+=n) {
            int groupSize = 0;
            for (int j = i; j < Math.min(i + n, read); j++) {
                group[groupSize++] = chunk[j];
            }
            byte[] groupWrapperData = Arrays.copyOf(group, groupSize);
            ByteArrayWrapper groupKey = new ByteArrayWrapper(groupWrapperData);
            if (groupSize != n) {
                minimumBytesGroupLength = groupSize;
                minimumBytesGroup = groupKey;
            }
            if (minimumBytesGroupLength == Integer.MAX_VALUE) {
                minimumBytesGroupLength = n;
                minimumBytesGroup = groupKey;
            }
            freq.merge(groupKey, 1, Integer::sum);
        }
    }

    public Map<ByteArrayWrapper, Integer> getFrequencyMap() {
        return freq;
    }

    public void writeCompressedFile(Map<ByteArrayWrapper, List<Boolean>> codewordTable, int totCodeLength) throws IOException {
        String path = inputFile.getParentFile().getAbsolutePath() + File.separator;
        String outputFileName = "21010217." + n + "." + inputFile.getName() + ".hc";
        File outputFile = new File(path + outputFileName);
        FileOutputStream fos = new FileOutputStream(outputFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        System.out.println("Writing Headers..");
        writeHeader(bos, codewordTable, totCodeLength);
        System.out.println("Writing Data in Compressed Form..");
        long writeDataTime= System.currentTimeMillis();
        System.out.println("Number Of Entries in hashmap = " + freq.size());
        writeData(bos, codewordTable);
        System.out.print("Time taken writing compressed data (sec): ");
        System.out.println((System.currentTimeMillis() - writeDataTime)/1000.0);
        System.out.println("> Compression Ratio: " + (double)outputFile.length()/inputFileSize);
        bos.close();
    }

    private void writeHeader(BufferedOutputStream bos, Map<ByteArrayWrapper, List<Boolean>> codewordTable, int totCodeLength) throws IOException {
        // n
        writeIntegerToFile(bos, n);
        writeIntegerToFile(bos, minimumBytesGroupLength);
        // number of entries
        writeIntegerToFile(bos, codewordTable.size());

        writeIntegerToFile(bos, totCodeLength);
        writeIntegerToFile(bos, (int) inputFileSize);

        writeAllValueLengthsInMap(bos, codewordTable);
        writeAllKeysInMap(bos, codewordTable);

        writeAllValuesInMap(bos, codewordTable);
        bos.flush();
    }

    private void writeAllValuesInMap(BufferedOutputStream bos, Map<ByteArrayWrapper, List<Boolean>> codewordTable) throws IOException {
        // first write the minimum group value
        List<Boolean> currentBits = new ArrayList<>(codewordTable.get(minimumBytesGroup));
        while (currentBits.size() >= 8) {
            convertFirst8BitsToByte(bos, currentBits);
        }
        for (Map.Entry<ByteArrayWrapper, List<Boolean>> entry : codewordTable.entrySet()){
            if (entry.getKey().equals(minimumBytesGroup)) {
                continue;
            }
            currentBits.addAll(entry.getValue());
            while (currentBits.size() >= 8) {
                convertFirst8BitsToByte(bos, currentBits);
            }

        }
        // pad with zeros if not empty then convert to byte
        if (currentBits.size() != 0 && currentBits.size() < 8) {
            int size = currentBits.size();
            for (int i = 0; i < 8 - size; i++) {
                currentBits.add(false);
            }
            convertFirst8BitsToByte(bos, currentBits);
        }
    }

    private void writeData(BufferedOutputStream bos, Map<ByteArrayWrapper, List<Boolean>> codewordTable) throws IOException {
        FileInputStream fis = new FileInputStream(inputFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        byte[] chunk = new byte[1024 * 1024 * n];
        int read = 0;
        // System.out.println("========WRITEDATA==========");
        List<Boolean> currentBits = new ArrayList<>();
        while ( (read = bis.read(chunk)) > 0) {
            currentBits = rewriteChunkUsingCodes(bos, chunk, codewordTable, read, currentBits);
        }
        // pad with zeros if not empty then convert to byte
        if (currentBits.size() != 0 && currentBits.size() < 8) {
            int size = currentBits.size();
            for (int i = 0; i < 8 - size; i++) {
                currentBits.add(false);
            }
            convertFirst8BitsToByte(bos, currentBits);
        }
        bos.flush();
        bis.close();
    }

    private List<Boolean> rewriteChunkUsingCodes(BufferedOutputStream bos, byte[] chunk,
                                                 Map<ByteArrayWrapper, List<Boolean>> codewordTable, int read, List<Boolean> currentBits) throws IOException {
        for (int i = 0; i < read; i += n) {
            int groupSize = 0;
            for (int j = i; j < Math.min(i + n, read); j++) {
                group[groupSize++] = chunk[j];
            }
            if (groupSize == n) {
                groupWrapper.setData(group);
                currentBits.addAll(codewordTable.get(groupWrapper));
            } else {
                currentBits.addAll(codewordTable.get(minimumBytesGroup));
            }
            while (currentBits.size() >= 8) {
                convertFirst8BitsToByte(bos, currentBits);
            }
        }

        return currentBits;
    }

    private void convertFirst8BitsToByte(BufferedOutputStream bos, List<Boolean> currentBits) throws IOException {
        byte value = 0;
        for (int j = 0; j < 8; j++) {
            // if it's a set bit
            if (currentBits.get(j)) {
                value |= (1<<(7-j));
            }
        }
        // System.out.println(value);
        bos.write(value);
        currentBits.subList(0, 8).clear();
    }

    private void writeAllValueLengthsInMap(BufferedOutputStream bos, Map<ByteArrayWrapper, List<Boolean>> codewordTable) throws IOException {
        // first add the minimum bytes group key
        writeIntegerToFile(bos, codewordTable.get(minimumBytesGroup).size());
        for (Map.Entry<ByteArrayWrapper, List<Boolean>> entry : codewordTable.entrySet()){
            if (minimumBytesGroup.equals(entry.getKey())) {
                continue;
            }
            writeIntegerToFile(bos, entry.getValue().size());
        }
    }
    private void writeAllKeysInMap(BufferedOutputStream bos, Map<ByteArrayWrapper, List<Boolean>> codewordTable) throws IOException {
        // first add the minimum bytes group key
        bos.write(minimumBytesGroup.getData());
        for (Map.Entry<ByteArrayWrapper, List<Boolean>> entry : codewordTable.entrySet()){
            if (minimumBytesGroup.equals(entry.getKey())) {
                continue;
            }
            bos.write(entry.getKey().getData());
        }
    }

    private void writeIntegerToFile(BufferedOutputStream bos, int number) throws IOException {
        byte[] numberBytes = new byte[]{(byte) (number >> 24), (byte) (number >> 16),
                                        (byte) (number >> 8), (byte) number};
        bos.write(numberBytes);
    }
}

