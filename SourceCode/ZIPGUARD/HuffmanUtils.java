package ZIPGUARD;

import java.io.*;
import java.util.*;

public class HuffmanUtils {

    private static class Node implements Comparable<Node> {
        char ch;
        int frequency;
        Node left, right;

        Node(char ch, int frequency) {
            this.ch = ch;
            this.frequency = frequency;
            left = right = null;
        }

        Node(int frequency, Node left, Node right) {
            this.ch = '\0';
            this.frequency = frequency;
            this.left = left;
            this.right = right;
        }

        @Override
        public int compareTo(Node node) {
            return this.frequency - node.frequency;
        }
    }

    private static class BitInputStream implements AutoCloseable {
        private FileInputStream in;
        private int currentByte;
        private int numBitsRemaining;

        public BitInputStream(FileInputStream in) {
            this.in = in;
            this.currentByte = 0;
            this.numBitsRemaining = 0;
        }

        public int readBit() throws IOException {
            if (numBitsRemaining == 0) {
                currentByte = in.read();
                if (currentByte == -1) {
                    return -1;
                }
                numBitsRemaining = 8;
            }

            numBitsRemaining--;
            return (currentByte >> numBitsRemaining) & 1;
        }

        @Override
        public void close() throws IOException {
            in.close();
        }
    }

    private static class BitOutputStream implements AutoCloseable {
        private FileOutputStream out;
        private int currentByte;
        private int numBitsFilled;

        public BitOutputStream(FileOutputStream out) {
            this.out = out;
            this.currentByte = 0;
            this.numBitsFilled = 0;
        }

        public void writeBit(boolean bit) throws IOException {
            if (bit) {
                currentByte |= (1 << (7 - numBitsFilled));
            }
            numBitsFilled++;
            if (numBitsFilled == 8) {
                out.write(currentByte);
                numBitsFilled = 0;
                currentByte = 0;
            }
        }

        @Override
        public void close() throws IOException {
            if (numBitsFilled > 0) {
                out.write(currentByte);
            }
            out.close();
        }
    }

    private static Map<Character, String> huffmanCodeMap = new HashMap<>();
    private static Map<Character, Integer> frequencyMap = new HashMap<>();

    private static void generateCodes(Node root, String code) {
        if (root == null) {
            return;
        }

        if (root.left == null && root.right == null) {
            huffmanCodeMap.put(root.ch, code);
        }

        generateCodes(root.left, code + "0");
        generateCodes(root.right, code + "1");
    }

    private static Node buildHuffmanTree() {
        PriorityQueue<Node> pq = new PriorityQueue<>();

        for (Map.Entry<Character, Integer> entry : frequencyMap.entrySet()) {
            pq.offer(new Node(entry.getKey(), entry.getValue()));
        }

        while (pq.size() > 1) {
            Node left = pq.poll();
            Node right = pq.poll();

            int sum = left.frequency + right.frequency;
            pq.offer(new Node(sum, left, right));
        }

        return pq.peek();
    }

    private static void writeTree(Node root, BitOutputStream out) throws IOException {
        if (root == null) {
            return;
        }

        if (root.left == null && root.right == null) { // Leaf node
            out.writeBit(true); // Leaf node indicator
            for (int i = 7; i >= 0; i--) { // Write character as 8 bits
                out.writeBit(((root.ch >> i) & 1) == 1);
            }
        } else { // Internal node
            out.writeBit(false); // Internal node indicator
            writeTree(root.left, out); // Write left subtree
            writeTree(root.right, out); // Write right subtree
        }
    }

    private static Node readTree(BitInputStream in) throws IOException {
        int bit = in.readBit();
        if (bit == -1) {
            return null;
        }

        if (bit == 1) { // Leaf node
            int ch = 0;
            for (int i = 0; i < 8; i++) {
                bit = in.readBit();
                if (bit == -1) {
                    return null;
                }
                ch = (ch << 1) | bit;
            }
            return new Node((char) ch, 0);
        } else { // Internal node
            Node left = readTree(in);
            Node right = readTree(in);
            return new Node(0, left, right);
        }
    }

    public static void compressFile(String inputFilePath) throws IOException {
        StringBuilder inputStringBuilder = new StringBuilder();
        String fileNameWithoutExtension = inputFilePath.substring(0, inputFilePath.lastIndexOf('.'));
        String compressedFilePath = fileNameWithoutExtension + "_compressed.dat";

        try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath))) {
            int ch;
            while ((ch = br.read()) != -1) {
                inputStringBuilder.append((char) ch);
                frequencyMap.put((char) ch, frequencyMap.getOrDefault((char) ch, 0) + 1);
            }
        }

        Node root = buildHuffmanTree();
        generateCodes(root, "");

        StringBuilder encodedStringBuilder = new StringBuilder();
        for (char ch : inputStringBuilder.toString().toCharArray()) {
            encodedStringBuilder.append(huffmanCodeMap.get(ch));
        }

        try (BitOutputStream bitOut = new BitOutputStream(new FileOutputStream(compressedFilePath));
             BitOutputStream treeOut = new BitOutputStream(new FileOutputStream(fileNameWithoutExtension + "_tree.dat"))) {

            writeTree(root, treeOut); // Write the Huffman tree to a separate file

            for (char bit : encodedStringBuilder.toString().toCharArray()) {
                bitOut.writeBit(bit == '1');
            }
        }

        System.out.println("File compressed successfully as: " + compressedFilePath);
    }

    public static void decompressFile(String compressedFilePath, String treeFilePath) throws IOException {
        String fileNameWithoutExtension = compressedFilePath.substring(0, compressedFilePath.lastIndexOf('_'));
        String decompressedFilePath = fileNameWithoutExtension + "_decompressed.txt";

        try (BitInputStream bitIn = new BitInputStream(new FileInputStream(compressedFilePath));
             BitInputStream treeIn = new BitInputStream(new FileInputStream(treeFilePath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(decompressedFilePath))) {

            Node root = readTree(treeIn); // Read the Huffman tree
            Node current = root;
            int bit;

            while ((bit = bitIn.readBit()) != -1) {
                current = (bit == 0) ? current.left : current.right;

                if (current.left == null && current.right == null) {
                    writer.write(current.ch);
                    current = root; // Reset to root after leaf node
                }
            }
        }

        System.out.println("File decompressed successfully as: " + decompressedFilePath);
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Choose an operation (compress/decompress):");
        String operation = scanner.nextLine();

        if (operation.equalsIgnoreCase("compress")) {
            System.out.print("Enter the file path to compress: ");
            String inputFilePath = scanner.nextLine();
            compressFile(inputFilePath);
        } else if (operation.equalsIgnoreCase("decompress")) {
            System.out.print("Enter the compressed file path: ");
            String compressedFilePath = scanner.nextLine();
            System.out.print("Enter the tree file path: ");
            String treeFilePath = scanner.nextLine();
            decompressFile(compressedFilePath, treeFilePath);
        } else {
            System.out.println("Invalid operation.");
        }

        scanner.close();
    }
}
