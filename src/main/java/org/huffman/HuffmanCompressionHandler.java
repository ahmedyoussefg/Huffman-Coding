package org.huffman;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
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
                // System.out.print(chunk[j] +", ");
            }
            // there's trailing space
            group.deleteCharAt(group.length() - 1);
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

    public void writeCompressedFile(Map<String, List<Boolean>> codewordTable, int totCodeLength) throws IOException {
        String path = inputFile.getParentFile().getAbsolutePath() + File.separator;
        String outputFileName = "21010217." + n + "." + inputFile.getName() + ".hc";
        File outputFile = new File(path + outputFileName);
        FileOutputStream fos = new FileOutputStream(outputFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        System.out.println("Writing Headers..");
        writeHeader(bos, codewordTable, totCodeLength);
        System.out.println("Writing Data in Compressed Form..");
        writeData(bos, codewordTable);
        bos.close();
    }

    private void writeHeader(BufferedOutputStream bos, Map<String, List<Boolean>> codewordTable, int totCodeLength) throws IOException {
        // n
        writeIntegerToFile(bos, n);
        writeIntegerToFile(bos, minimumBytesGroupLength);
        // number of entries
        writeIntegerToFile(bos, codewordTable.size());

        writeIntegerToFile(bos, totCodeLength);
        writeIntegerToFile(bos, (int) Files.size(Path.of(inputFile.getPath())));

        writeAllValueLengthsInMap(bos, codewordTable);
        writeAllKeysInMap(bos, codewordTable);

        writeAllValuesInMap(bos, codewordTable);
        bos.flush();
    }

    private void writeAllValuesInMap(BufferedOutputStream bos, Map<String, List<Boolean>> codewordTable) throws IOException {
        // first write the minimum group value
        List<Boolean> currentBits = new ArrayList<>(codewordTable.get(minimumBytesGroup));
        while (currentBits.size() >= 8) {
            convertFirst8BitsToByte(bos, currentBits);
        }
        for (Map.Entry<String, List<Boolean>> entry : codewordTable.entrySet()){
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

    private void writeData(BufferedOutputStream bos, Map<String, List<Boolean>> codewordTable) throws IOException {
        FileInputStream fis = new FileInputStream(inputFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        byte[] chunk = new byte[8192 * n];
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
                                        Map<String, List<Boolean>> codewordTable, int read, List<Boolean> currentBits) throws IOException {
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < Math.min(chunk.length, read); ++i) {
            current.append(chunk[i]);
            String tempCurrent = current.toString();
            current.append(" ");
            if (codewordTable.containsKey(tempCurrent)){
                // bos.write(convertFromByteList(codewordTable.get(current)));
                currentBits.addAll(codewordTable.get(tempCurrent));
                while (currentBits.size() >= 8) {
                    convertFirst8BitsToByte(bos, currentBits);
                }
                current = new StringBuilder();
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

    private void writeAllValueLengthsInMap(BufferedOutputStream bos, Map<String, List<Boolean>> codewordTable) throws IOException {
        // first add the minimum bytes group key
        writeIntegerToFile(bos, codewordTable.get(minimumBytesGroup).size());
        for (Map.Entry<String, List<Boolean>> entry : codewordTable.entrySet()){
            if (minimumBytesGroup.equals(entry.getKey())) {
                continue;
            }
            writeIntegerToFile(bos, entry.getValue().size());
        }
    }
    private void writeAllKeysInMap(BufferedOutputStream bos, Map<String, List<Boolean>> codewordTable) throws IOException {
        // first add the minimum bytes group key
        bos.write(convertFromByteString(minimumBytesGroup));
        for (Map.Entry<String, List<Boolean>> entry : codewordTable.entrySet()){
            if (minimumBytesGroup.equals(entry.getKey())) {
                continue;
            }
            bos.write(convertFromByteString(entry.getKey()));
        }
    }

    private void writeIntegerToFile(BufferedOutputStream bos, int number) throws IOException {
        byte[] numberBytes = new byte[]{(byte) (number >> 24), (byte) (number >> 16),
                                        (byte) (number >> 8), (byte) number};
        bos.write(numberBytes);
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
