package org.huffman;

import java.io.*;
import java.util.List;
import java.util.Map;


public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage for compression: java -jar huffman_<id>.jar c <absolute_path_to_input_file> <n>");
            System.out.println("Usage for decompression: java -jar huffman_<id>.jar d <absolute_path_to_input_file>");
            return;
        }

        String option = args[0]; // compress or decompress
        String inputPath = args[1];

        if ("c".equals(option)) {
            if (args.length < 3) {
                System.out.println("Usage for compression: java -jar huffman_<id>.jar c <absolute_path_to_input_file> <n>");
                return;
            }
            String nChar = args[2];
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