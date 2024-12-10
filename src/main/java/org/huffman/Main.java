package org.huffman;

import java.io.*;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        String option = "c"; // compress or decompress
        String inputPath = "D:\\CSE - Department\\Level 3\\First Semester\\Design and Analysis of Algorithms\\Prog Assignments\\Lab2\\HuffmanCoding\\test_cases\\Algorithms - Lectures 7 (Greedy algorithms).pdf";
        String nChar = "1";
        int n = Integer.parseInt(nChar);
        if ("c".equals(option)) {
            // compress
            compress(n, inputPath);
        } else {
            // decompress
            decompress();
        }
    }

    private static void compress(int n, String inputPath) throws IOException {
        // Read file in chunks
        File inputFile = new File(inputPath);
        HuffmanCompressionHandler huffmanCompressionHandler = new HuffmanCompressionHandler(n, inputFile);
        FileInputStream fis = new FileInputStream(inputFile);
        byte[] chunk = new byte[512 * n];
        int read = 0;
        while ( (read = fis.read(chunk)) > 0) {
            huffmanCompressionHandler.calculateFrequency(chunk);
        }
        HuffmanCode huffmanCode = new HuffmanCode();
        huffmanCode.buildTree(huffmanCompressionHandler.getFrequencyMap());
        Map<List<Byte>, List<Byte>> codewordTable = huffmanCode.buildCodewordTable();
        huffmanCompressionHandler.writeCompressedFile(codewordTable);
        fis.close();
    }

    private static void decompress() {
    }
}