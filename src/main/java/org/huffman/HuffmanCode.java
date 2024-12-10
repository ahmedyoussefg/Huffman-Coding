package org.huffman;

import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class HuffmanCode {
    private HuffmanTreeNode huffmanTreeRoot;
    private Map<String, List<Boolean>> codewordTable;
    private int totCodeLength = 0;
    private int payload = 0;
    public void buildTree(Map<String, Integer> freq) {
        PriorityQueue<HuffmanTreeNode> pq = new PriorityQueue<>(Comparator.comparingInt(HuffmanTreeNode::getFreq));
        for (Map.Entry<String, Integer> entry : freq.entrySet()){
            pq.add(new HuffmanTreeNode(entry.getKey(), entry.getValue()));
        }
        while (pq.size() >= 2) {
            HuffmanTreeNode newNode = new HuffmanTreeNode();
            newNode.left = pq.poll();
            newNode.right = pq.poll();
            newNode.freq = newNode.left.freq + newNode.right.freq;
            pq.add(newNode);
        }
        huffmanTreeRoot = pq.poll();
    }
    public Map<String, List<Boolean>> buildCodewordTable() {
        codewordTable = new HashMap<>();
        dfs(huffmanTreeRoot, new ArrayList<>());
        return codewordTable;
    }

    private void dfs(HuffmanTreeNode node, List<Boolean> currentCode) {
        if (node.left == null && node.right == null) {
            // leaf node
            codewordTable.put(node.value, new ArrayList<>(currentCode));
            totCodeLength += currentCode.size();
            payload += currentCode.size() * node.freq;
            return;
        }
        if (node.left != null) {
            currentCode.add(false);
            dfs(node.left, currentCode);
            currentCode.remove(currentCode.size() - 1);
        }
        if (node.right != null) {
            currentCode.add(true);
            dfs(node.right, currentCode);
            currentCode.remove(currentCode.size() - 1);
        }
    }

    public int getTotalCodeLength() {
        // paddingInValues = (8 - (totCodeLength % 8))%8;
        return totCodeLength;
    }
    public int getPayloadLength() {
        return payload;
    }


    private static class HuffmanTreeNode {
        private int freq;
        private final String value;
        private HuffmanTreeNode left;
        private HuffmanTreeNode right;
        public HuffmanTreeNode(){
            value = null;
        }
        public HuffmanTreeNode(String value, int freq) {
            this.value=value;
            this.freq = freq;
            this.left = null;
            this.right = null;
        }
        public int getFreq() {
            return freq;
        }
    }
}
