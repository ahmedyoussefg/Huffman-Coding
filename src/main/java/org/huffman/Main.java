package org.huffman;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        // TODO: Test on different n's
        // TODO: Change payload data length and calculate it while decompressing (read header - inputfile.size)
        // TODO: Optimize time!! (it's very slow for large files)
        // TODO: Extract jar and add command line args
        String option = "c"; // compress or decompress
        // String inputPath = "D:\\CSE - Department\\Level 3\\First Semester\\Design and Analysis of Algorithms\\Prog Assignments\\Lab2\\HuffmanCoding\\test_cases\\jetbrains-toolbox-2.5.2.35332.exe";
        // String inputPath = "D:\\CSE - Department\\Level 3\\First Semester\\Design and Analysis of Algorithms\\Prog Assignments\\Lab2\\HuffmanCoding\\test_cases\\21010217.1.jetbrains-toolbox-2.5.2.35332.exe.hc";
        String inputPath = "D:\\CSE - Department\\Level 3\\First Semester\\Design and Analysis of Algorithms\\Prog Assignments\\Lab2\\HuffmanCoding\\test_cases\\gbbct10.seq";
        if ("c".equals(option)) {
            String nChar = "1";
            int n = Integer.parseInt(nChar);
            // compress
            compress(n, inputPath);
        } else {
            // decompress
            decompress(inputPath);
        }
    }

    private static void compress(int n, String inputPath) throws IOException, InterruptedException {
        // Read file in chunks
        File inputFile = new File(inputPath);
        HuffmanCompressionHandler huffmanCompressionHandler = new HuffmanCompressionHandler(n, inputFile);
        FileInputStream fis = new FileInputStream(inputFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        byte[] chunk = new byte[64 * 1024 * n];
        long now = System.currentTimeMillis();
        int read = 0;
        // ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        while ( (read = bis.read(chunk)) > 0) {
            // huffmanCompressionHandler.calculateFrequency(chunk, read);
            // byte[] finalChunk = Arrays.copyOf(chunk, read);
            // pool.execute (() -> huffmanCompressionHandler.calculateFrequency(finalChunk));
            huffmanCompressionHandler.calculateFrequency(chunk, read);
        }
        // pool.shutdown();
        // pool.awaitTermination(120, TimeUnit.SECONDS);
        System.out.println((System.currentTimeMillis()-now)/1000.0);
        // HuffmanCode huffmanCode = new HuffmanCode();
        // System.out.println("Building Huffman Tree..");
        // huffmanCode.buildTree(huffmanCompressionHandler.getFrequencyMap());
        // System.out.println("Building Huffman Codeword Table..");
        // Map<String, List<Boolean>> codewordTable = huffmanCode.buildCodewordTable();
        // huffmanCompressionHandler.writeCompressedFile(codewordTable, huffmanCode.getTotalCodeLength());
        bis.close();
    }

    private static void decompress(String inputPath) throws IOException {
        File inputFile = new File(inputPath);
        HuffmanDecompressionHandler huffmanDecompressionHandler = new HuffmanDecompressionHandler(inputFile);
        FileInputStream fis = new FileInputStream(inputFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        huffmanDecompressionHandler.process(bis);
        bis.close();
    }
}