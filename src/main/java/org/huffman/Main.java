package org.huffman;

import java.io.*;
import java.util.List;
import java.util.Map;


public class Main {
    public static void main(String[] args) throws IOException {
        String option = "d"; // compress or decompress
        // String inputPath = "D:\\CSE - Department\\Level 3\\First Semester\\Design and Analysis of Algorithms\\Prog Assignments\\Lab2\\HuffmanCoding\\test_cases\\jetbrains-toolbox-2.5.2.35332.exe";
        // String inputPath = "D:\\CSE - Department\\Level 3\\First Semester\\Design and Analysis of Algorithms\\Prog Assignments\\Lab2\\HuffmanCoding\\test_cases\\21010217.1.jetbrains-toolbox-2.5.2.35332.exe.hc";
        // String inputPath = "D:\\CSE - Department\\Level 3\\First Semester\\Design and Analysis of Algorithms\\Prog Assignments\\Lab2\\HuffmanCoding\\test_cases\\gbbct10.seq";
        String inputPath = "D:\\CSE - Department\\Level 3\\First Semester\\Design and Analysis of Algorithms\\Prog Assignments\\Lab2\\HuffmanCoding\\test_cases\\21010217.1.gbbct10.seq.hc";
        // String inputPath = "D:\\CSE - Department\\Level 3\\First Semester\\Design and Analysis of Algorithms\\Prog Assignments\\Lab2\\HuffmanCoding\\test_cases\\Algorithms - Lectures 7 and 8 (Greedy algorithms).pdf";
        // String inputPath = "D:\\CSE - Department\\Level 3\\First Semester\\Design and Analysis of Algorithms\\Prog Assignments\\Lab2\\HuffmanCoding\\test_cases\\21010217.5.Algorithms - Lectures 7 and 8 (Greedy algorithms).pdf.hc";

        // String inputPath = "D:\\CSE - Department\\Level 3\\First Semester\\Design and Analysis of Algorithms\\Prog Assignments\\Lab2\\HuffmanCoding\\test_cases\\21010217.1.Algorithms - Lectures 7 (Greedy algorithms).pdf.hc";
        // String inputPath = "D:\\CSE - Department\\Level 3\\First Semester\\Design and Analysis of Algorithms\\Prog Assignments\\Lab2\\HuffmanCoding\\test_cases\\hello.txt";
        // String inputPath = "D:\\CSE - Department\\Level 3\\First Semester\\Design and Analysis of Algorithms\\Prog Assignments\\Lab2\\HuffmanCoding\\test_cases\\21010217.1.hello.txt.hc";

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
        new HuffmanCompressionHandler(n, inputPath).process();
    }

    private static void decompress(String inputPath) throws IOException {
        new HuffmanDecompressionHandler(inputPath).process();
    }
}