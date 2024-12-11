package org.huffman;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        // TODO: Test on different n's
        // TODO: Change payload data length and calculate it while decompressing (read header - inputfile.size)
        // TODO: Optimize time!! (it's very slow for large files)
        // TODO: Extract jar and add command line args
        String option = "d"; // compress or decompress
        // String inputPath = "D:\\CSE - Department\\Level 3\\First Semester\\Design and Analysis of Algorithms\\Prog Assignments\\Lab2\\HuffmanCoding\\test_cases\\ProgAsg2.pdf";
        // String inputPath = "D:\\CSE - Department\\Level 3\\First Semester\\Design and Analysis of Algorithms\\Prog Assignments\\Lab2\\HuffmanCoding\\test_cases\\21010217.1.ProgAsg2.pdf.hc";
        // String inputPath = "D:\\CSE - Department\\Level 3\\First Semester\\Design and Analysis of Algorithms\\Prog Assignments\\Lab2\\HuffmanCoding\\test_cases\\hello.txt";
        // String inputPath = "D:\\CSE - Department\\Level 3\\First Semester\\Design and Analysis of Algorithms\\Prog Assignments\\Lab2\\HuffmanCoding\\test_cases\\21010217.1.hello.txt.hc";

        // String inputPath = "D:\\CSE - Department\\Level 3\\First Semester\\Design and Analysis of Algorithms\\Prog Assignments\\Lab2\\HuffmanCoding\\test_cases\\biggerTest.txt";
        // String inputPath = "D:\\CSE - Department\\Level 3\\First Semester\\Design and Analysis of Algorithms\\Prog Assignments\\Lab2\\HuffmanCoding\\test_cases\\21010217.1.biggerTest.txt.hc";

        // String inputPath = "D:\\CSE - Department\\Level 3\\First Semester\\Design and Analysis of Algorithms\\Prog Assignments\\Lab2\\HuffmanCoding\\test_cases\\smallerTest.txt";
        // String inputPath = "D:\\CSE - Department\\Level 3\\First Semester\\Design and Analysis of Algorithms\\Prog Assignments\\Lab2\\HuffmanCoding\\test_cases\\21010217.1.smallerTest.txt.hc";

        // String inputPath = "D:\\CSE - Department\\Level 3\\First Semester\\Design and Analysis of Algorithms\\Prog Assignments\\Lab2\\HuffmanCoding\\test_cases\\Algorithms - Lectures 7 (Greedy algorithms).pdf";
        // String inputPath = "D:\\CSE - Department\\Level 3\\First Semester\\Design and Analysis of Algorithms\\Prog Assignments\\Lab2\\HuffmanCoding\\test_cases\\21010217.1.Algorithms - Lectures 7 (Greedy algorithms).pdf.hc";
        String inputPath = "D:\\CSE - Department\\Level 3\\First Semester\\Design and Analysis of Algorithms\\Prog Assignments\\Lab2\\HuffmanCoding\\test_cases\\21010217.1.gbbct10.seq.hc";
        // String inputPath = "D:\\CSE - Department\\Level 3\\First Semester\\Design and Analysis of Algorithms\\Prog Assignments\\Lab2\\HuffmanCoding\\test_cases\\21010217.1.Lec W09 Networks F24 - sghanem.pdf.hc";
        // String inputPath = "D:\\CSE - Department\\Level 3\\First Semester\\Design and Analysis of Algorithms\\Prog Assignments\\Lab2\\HuffmanCoding\\test_cases\\Lec W09 Networks F24 - sghanem.pdf";

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

    private static void compress(int n, String inputPath) throws IOException {
        // Read file in chunks
        File inputFile = new File(inputPath);
        HuffmanCompressionHandler huffmanCompressionHandler = new HuffmanCompressionHandler(n, inputFile);
        FileInputStream fis = new FileInputStream(inputFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        byte[] chunk = new byte[8192 * n];
        int read = 0;
        while ( (read = bis.read(chunk)) > 0) {
            huffmanCompressionHandler.calculateFrequency(chunk, read);
        }
        HuffmanCode huffmanCode = new HuffmanCode();
        System.out.println("Building Huffman Tree..");
        huffmanCode.buildTree(huffmanCompressionHandler.getFrequencyMap());
        System.out.println("Building Huffman Codeword Table..");
        Map<String, List<Boolean>> codewordTable = huffmanCode.buildCodewordTable();
        huffmanCompressionHandler.writeCompressedFile(codewordTable, huffmanCode.getTotalCodeLength());
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