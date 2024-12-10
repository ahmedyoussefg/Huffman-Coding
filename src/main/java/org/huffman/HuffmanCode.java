package org.huffman;

import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class HuffmanCode {
    private HuffmanTreeNode huffmanTreeRoot;
    private Map<List<Byte>, List<Byte>> codewordTable;
    public void buildTree(Map<List<Byte>, Integer> freq) {
        PriorityQueue<HuffmanTreeNode> pq = new PriorityQueue<>(Comparator.comparingInt(HuffmanTreeNode::getFreq));
        for (Map.Entry<List<Byte>, Integer> entry : freq.entrySet()){
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
    public Map<List<Byte>, List<Byte>> buildCodewordTable() {
        // TODO: should map to List<Bit> or something instead
        codewordTable = new HashMap<>();
        dfs(huffmanTreeRoot, new ArrayList<>());
        return codewordTable;
    }
    private void dfs(HuffmanTreeNode node, List<Byte> currentCode) {
        if (node.left == null && node.right == null) {
            // leaf node
            codewordTable.put(node.value, currentCode);
            return;
        }
        if (node.left != null) {
            currentCode.add((byte) 0);
            dfs(node.left, currentCode);
            currentCode.remove(currentCode.size() - 1);
        }
        if (node.right != null) {
            currentCode.add((byte) 1);
            dfs(node.right, currentCode);
            currentCode.remove(currentCode.size() - 1);
        }
    }
    private static class HuffmanTreeNode {
        private int freq;
        private final List<Byte> value;
        private HuffmanTreeNode left;
        private HuffmanTreeNode right;
        public HuffmanTreeNode(){
            value = null;
        }
        public HuffmanTreeNode(List<Byte> value, int freq) {
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
