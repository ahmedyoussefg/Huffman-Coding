package org.huffman;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HuffmanDecompressionHandler {
    private File inputFile;
    private int n;
    private int minimumGroupLength;
    private int numberOfEntries;
    private int totCodeLength;
    private int[] lengthsOfHuffmanCodings;
    private Map<String, Integer> mapValuesToIndex;
    // private List<Byte> ans = new ArrayList<>();
    private byte[][] keysInTable;
    private int payloadLength;
    private int payloadWritten;
    public HuffmanDecompressionHandler(File inputFile) {
        this.inputFile = inputFile;
    }

    public int readInteger(BufferedInputStream bis) throws IOException {
        byte[] numberBytes = new byte[4];
        int read = bis.read(numberBytes);
        if (read != 4) throw new IOException("Couldn't read integers");
        return getInteger(numberBytes);
    }

    private static int getInteger(byte[] numberBytes) {
        int number = 0;
        for (int i = 0; i < 4; i++) {
            number |= ((numberBytes[i] & 0xFF) << (24 - 8*i));
        }
        return number;
    }

    public int getN() {
        return n;
    }

    public void process(BufferedInputStream bis) throws IOException {
        // Method to read N, minimumGroupLength, NumberOfEntries, totCodeLength, payloadLen, lengthsOfHuffmanCodings
        System.out.println("Reading header metadata..");
        n = readInteger(bis);

        minimumGroupLength = readInteger(bis);

        numberOfEntries = readInteger(bis);

        totCodeLength = readInteger(bis);

        payloadLength = readInteger(bis);

        System.out.println("Reading keys and lengths of codings");
        readKeysAndLengthsOfHuffmanCodings(bis);

        mapValuesToIndex = new HashMap<>();

        System.out.println("Reading huffman codings");
        ChunkBitLocation chunkBitLocation = readValuesOfHuffmanCodings(bis);

        System.out.println("Decompressing the payload data..");
        readAndWriteData(bis, chunkBitLocation);
    }

    private void readKeysAndLengthsOfHuffmanCodings(BufferedInputStream bis) throws IOException {
        lengthsOfHuffmanCodings = new int[numberOfEntries];
        keysInTable = new byte[numberOfEntries][];

        byte[] chunkForMinimumGroup = new byte[minimumGroupLength];
        byte[] chunkForLength = new byte[4];
        int read = bis.read(chunkForMinimumGroup);
        if (read != minimumGroupLength) throw new IOException("Couldn't read the minimum group.");
        keysInTable[0] = chunkForMinimumGroup;
        read = bis.read(chunkForLength);
        if (read != 4) throw new IOException("Couldn't read the length of minimum group's code.");
        lengthsOfHuffmanCodings[0] = getInteger(chunkForLength);
        for (int i = 1; i < numberOfEntries; i++){
            // Read group
            byte[] chunkForGroup = new byte[n];
            read = bis.read(chunkForGroup);
            keysInTable[i] = chunkForGroup;
            if (read != n) throw new IOException("Couldn't read keys of HuffmanCodewordTable");

            // Read Length
            read = bis.read(chunkForLength);
            if (read != 4) throw new IOException("Couldn't read lengths of HuffmanCodes");
            lengthsOfHuffmanCodings[i]=getInteger(chunkForLength);
        }
    }
    private ChunkBitLocation readValuesOfHuffmanCodings(BufferedInputStream bis) throws IOException {
        byte[] chunk = new byte[8192];
        int indexInChunk = 0;
        int indexInsideByte = 0;
        int read = 0;
        boolean firstTime = true;
        for (int i = 0; i < numberOfEntries; i++) {
            int neededBits = lengthsOfHuffmanCodings[i];
            StringBuilder currentBits = new StringBuilder();
            for (int j = 0; j < neededBits; j++) {
                if (indexInChunk == Math.min(read, chunk.length) || firstTime) {
                    read = bis.read(chunk);
                    indexInChunk = 0;
                    indexInsideByte = 0;
                    firstTime = false;
                }
                currentBits.append(getIthByte(indexInsideByte, chunk[indexInChunk]));

                indexInsideByte++;
                if (indexInsideByte == 8) {
                    indexInChunk++;
                    indexInsideByte=0;
                }
            }
            mapValuesToIndex.put(currentBits.toString(), i);
        }
        int paddedToValues = (8 - (totCodeLength%8))%8;
        for (int steps = 0; steps < paddedToValues; steps++) {
            indexInsideByte++;
            if (indexInsideByte == 8) {
                indexInChunk++;
                indexInsideByte=0;
            }
        }
        return new ChunkBitLocation(chunk, indexInChunk, indexInsideByte, read);
    }

    private String getIthByte(int indexInsideByte, byte chunk) {
        return ((chunk >> (7 - indexInsideByte) & 1) == 1 ? "1" : "0");
    }

    private void readAndWriteData(BufferedInputStream bis, ChunkBitLocation chunkBitLocation) throws IOException {
        String outputFilePath = inputFile.getParentFile().getAbsolutePath() + File.separator
                                + "extracted." + inputFile.getName().replace(".hc","");
        File outputFile = new File(outputFilePath);
        FileOutputStream fos = new FileOutputStream(outputFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        StringBuilder currentBits = new StringBuilder();
        currentBits = getCurrentBits(bos, currentBits, chunkBitLocation.chunk, chunkBitLocation.read,
                chunkBitLocation.indexInChunk, chunkBitLocation.indexInsideByte);
        if (payloadWritten >= payloadLength) {
            bos.flush();
            bos.close();
            return;
        }
        byte[] chunk = new byte[8192];
        int read = 0;
        while ( (read = bis.read(chunk)) > 0) {
            currentBits = getCurrentBits(bos, currentBits, chunk, read, 0, 0);
            if (payloadWritten >= payloadLength) {
                break;
            }
        }
        // System.out.println(ans.subList(0,ans.size()).toString());

        bos.flush();
        bos.close();
    }

    private StringBuilder getCurrentBits(BufferedOutputStream bos, StringBuilder currentBits, byte[] chunk, int read,
                                         int initialChunkIndex, int initialBitIndex) throws IOException {
        for (int j = initialChunkIndex; j < Math.min(chunk.length, read); j++) {
            for (int k = initialBitIndex; k < 8; k++) {
                if (j== 153 && k==2) {
                    // TODO: Fix the bug here!
                    System.out.println("HELP");
                }
                currentBits.append(getIthByte(k, chunk[j]));
                if (mapValuesToIndex.containsKey(currentBits.toString())) {
                    byte[] b = keysInTable[mapValuesToIndex.get(currentBits.toString())];
                    // ans.add(b[0]);
                    payloadWritten += b.length;
                    bos.write(b);
                    currentBits = new StringBuilder();
                }
                if (payloadWritten >= payloadLength) {
                    return currentBits;
                }
            }
        }
        return currentBits;
    }

    private class ChunkBitLocation {
        private final int indexInChunk;
        private final int indexInsideByte;
        private final byte[] chunk;
        private final int read;
        public ChunkBitLocation(byte[] chunk, int indexInChunk, int indexInsideByte, int read) {
            this.chunk = chunk;
            this.indexInChunk = indexInChunk;
            this.indexInsideByte = indexInsideByte;
            this.read = read;
        }
    }
}
