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
    private Map<List<Boolean>, Integer> mapValuesToIndex;
    // private List<Byte> ans = new ArrayList<>();
    private byte[][] keysInTable;
    private int payloadLength;
    private int payloadWritten;
    public HuffmanDecompressionHandler(String inputPath) {
        this.inputFile = new File(inputPath);
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

    public void process() throws IOException {
        long startProcessTime = System.currentTimeMillis();
        FileInputStream fis = new FileInputStream(inputFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        // Method to read N, minimumGroupLength, NumberOfEntries, totCodeLength, payloadLen, lengthsOfHuffmanCodings
        System.out.println("Reading header metadata..");
        n = readInteger(bis);

        minimumGroupLength = readInteger(bis);

        numberOfEntries = readInteger(bis);

        totCodeLength = readInteger(bis);

        payloadLength = readInteger(bis);

        System.out.println("Reading lengths of codings..");
        readLengthsOfHuffmanCodings(bis);

        System.out.println("Reading keys of codings..");
        readKeysOfHuffmanCodings(bis);

        mapValuesToIndex = new HashMap<>();

        System.out.println("Reading huffman codings..");
        ChunkBitLocation chunkBitLocation = readValuesOfHuffmanCodings(bis);

        System.out.println("Decompressing the payload data..");
        readAndWriteData(bis, chunkBitLocation);

        System.out.print("> Total Time Taken To Decompress The File (sec): ");
        System.out.println((System.currentTimeMillis()-startProcessTime)/1000.0);
    }

    private void readLengthsOfHuffmanCodings(BufferedInputStream bis) throws IOException {
        lengthsOfHuffmanCodings = new int[numberOfEntries];
        byte[] chunk = new byte[4*numberOfEntries];
        // Read Lengths
        int read = bis.read(chunk);
        for (int i = 0; i < numberOfEntries; i++) {
            lengthsOfHuffmanCodings[i]=getInteger(new byte[] {
                    chunk[4*i], chunk[4*i + 1], chunk[4*i + 2], chunk[4*i + 3]
            });
        }
    }

    private void readKeysOfHuffmanCodings(BufferedInputStream bis) throws IOException {
        keysInTable = new byte[numberOfEntries][];

        byte[] chunkForMinimumGroup = new byte[minimumGroupLength];
        int read = bis.read(chunkForMinimumGroup);
        keysInTable[0] = chunkForMinimumGroup;
        for (int i = 1; i < numberOfEntries; i++){
            // Read group
            byte[] chunkForGroup = new byte[n];
            read = bis.read(chunkForGroup);
            keysInTable[i] = chunkForGroup;
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
            List<Boolean> currentBits = new ArrayList<>();
            for (int j = 0; j < neededBits; j++) {
                if (indexInChunk == Math.min(read, chunk.length) || firstTime) {
                    read = bis.read(chunk);
                    indexInChunk = 0;
                    indexInsideByte = 0;
                    firstTime = false;
                }
                currentBits.add(getIthBit(indexInsideByte, chunk[indexInChunk]));

                indexInsideByte++;
                if (indexInsideByte == 8) {
                    indexInChunk++;
                    indexInsideByte=0;
                }
            }
            mapValuesToIndex.put(currentBits, i);
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

    private boolean getIthBit(int indexInsideByte, byte chunk) {
        return ((chunk >> (7 - indexInsideByte) & 1) == 1);
    }

    private void readAndWriteData(BufferedInputStream bis, ChunkBitLocation chunkBitLocation) throws IOException {
        String outputFilePath = inputFile.getParentFile().getAbsolutePath() + File.separator
                                + "extracted." + inputFile.getName().replace(".hc","");
        File outputFile = new File(outputFilePath);
        FileOutputStream fos = new FileOutputStream(outputFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        List<Boolean> currentBits = new ArrayList<>();
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

    private List<Boolean> getCurrentBits(BufferedOutputStream bos, List<Boolean> currentBits, byte[] chunk, int read,
                                         int initialChunkIndex, int initialBitIndex) throws IOException {
        for (int j = initialChunkIndex; j < Math.min(chunk.length, read); j++) {
            for (int k = initialBitIndex; k < 8; k++) {
                currentBits.add(getIthBit(k, chunk[j]));
                if (mapValuesToIndex.containsKey(currentBits)) {
                    byte[] b = keysInTable[mapValuesToIndex.get(currentBits)];
                    // ans.add(b[0]);
                    payloadWritten += b.length;
                    bos.write(b);
                    currentBits.clear();
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
