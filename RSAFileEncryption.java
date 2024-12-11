package com.company;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.security.spec.X509EncodedKeySpec; 
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import javax.crypto.*;
import java.util.Scanner;

public class RSAFileEncryption {

    // Generate RSA Key Pair
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(2048);
        return keyPairGen.generateKeyPair();
    }

    // Encrypt data using the public key
    public static byte[] encryptData(byte[] data, PublicKey publicKey) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    // Decrypt data using the private key
    public static byte[] decryptData(byte[] encryptedData, PrivateKey privateKey) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(encryptedData);
    }

    // Write data to a file
    public static void writeToFile(byte[] data, Path path) throws IOException {
        Files.write(path, data);
    }

    // Read data from a file
    public static byte[] readFromFile(Path path) throws IOException {
        return Files.readAllBytes(path);
    }

    // Convert public key to PEM format
    public static String getPEMPublicKey(PublicKey publicKey) {
        String encoded = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        return "-----BEGIN PUBLIC KEY-----\n" +
                wrapText(encoded) +
                "\n-----END PUBLIC KEY-----";
    }

    // Convert private key to PEM format
    public static String getPEMPrivateKey(PrivateKey privateKey) {
        String encoded = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        return "-----BEGIN PRIVATE KEY-----\n" +
                wrapText(encoded) +
                "\n-----END PRIVATE KEY-----";
    }

    // Helper function to wrap Base64 text in 64-character lines (standard PEM format)
    public static String wrapText(String text) {
        StringBuilder wrapped = new StringBuilder();
        int offset = 0;
        while (offset < text.length()) {
            int end = Math.min(offset + 64, text.length());
            wrapped.append(text, offset, end).append("\n");
            offset = end;
        }
        return wrapped.toString();
    }

    // Read PEM key from file and return the PublicKey object
    public static PublicKey readPublicKeyFromFile(Path publicKeyFile) throws Exception {
        String pem = new String(readFromFile(publicKeyFile));
        String base64Encoded = pem.replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "").replace("\n", "");
        byte[] decodedKey = Base64.getDecoder().decode(base64Encoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey));  // Now this works
    }

    // Read PEM key from file and return the PrivateKey object
    public static PrivateKey readPrivateKeyFromFile(Path privateKeyFile) throws Exception {
        String pem = new String(readFromFile(privateKeyFile));
        String base64Encoded = pem.replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "").replace("\n", "");
        byte[] decodedKey = Base64.getDecoder().decode(base64Encoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decodedKey));  // Now this works
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try {
            // Prompt user to choose encryption or decryption
            System.out.println("Choose an option:");
            System.out.println("1. Encrypt a file");
            System.out.println("2. Decrypt a file");
            int choice = scanner.nextInt();
            scanner.nextLine();  // Consume the newline character

            if (choice == 1) {
                // Encryption process
                System.out.println("Enter the file path to encrypt:");
                String filePath = scanner.nextLine();
                Path inputFile = Paths.get(filePath);

                // Generate RSA Key Pair
                KeyPair keyPair = generateKeyPair();
                PublicKey publicKey = keyPair.getPublic();
                PrivateKey privateKey = keyPair.getPrivate();

                // Define file paths for encrypted file and keys
//                Path encryptedFile = Paths.get("C:\\Users\\Dell\\IdeaProjects\\First_prj\\src\\com\\company\\encryptedFile.txt");
                Path encryptedFile = Paths.get("encryptedFile.txt");
//                Path publicKeyFile = Paths.get("C:\\Users\\Dell\\IdeaProjects\\First_prj\\src\\com\\company\\publicKey.pem");
                Path publicKeyFile = Paths.get("publicKey.pem");
//                Path privateKeyFile = Paths.get("C:\\Users\\Dell\\IdeaProjects\\First_prj\\src\\com\\company\\privateKey.pem");
                Path privateKeyFile = Paths.get("privateKey.pem");

                if (!Files.exists(inputFile)) {
                    System.err.println("Input file not found: " + inputFile.toAbsolutePath());
                    return;
                }

                // Read the file data
                byte[] fileData = readFromFile(inputFile);

                // Encrypt data using public key
                byte[] encryptedData = encryptData(fileData, publicKey);
                writeToFile(encryptedData, encryptedFile);

                // Save the public and private keys to files in PEM format
                String publicKeyPEM = getPEMPublicKey(publicKey);
                String privateKeyPEM = getPEMPrivateKey(privateKey);

                // Write the keys to the files
                writeToFile(publicKeyPEM.getBytes(), publicKeyFile);
                writeToFile(privateKeyPEM.getBytes(), privateKeyFile);

                System.out.println("Encryption complete. Encrypted file and keys saved.");
            } else if (choice == 2) {
                // Decryption process
                System.out.println("Enter the file path to decrypt:");
                String filePath = scanner.nextLine();
                Path encryptedFile = Paths.get(filePath);

                // Ask for the path to the private key
                System.out.println("Enter the private key file path (in PEM format):");
                String privateKeyPath = scanner.nextLine();
                Path privateKeyFile = Paths.get(privateKeyPath);

                if (!Files.exists(encryptedFile)) {
                    System.err.println("Encrypted file not found: " + encryptedFile.toAbsolutePath());
                    return;
                }

                if (!Files.exists(privateKeyFile)) {
                    System.err.println("Private key file not found: " + privateKeyFile.toAbsolutePath());
                    return;
                }

                // Read the private key from the file
                PrivateKey privateKey = readPrivateKeyFromFile(privateKeyFile);

                // Decrypt data using private key
                byte[] encryptedData = readFromFile(encryptedFile);
                byte[] decryptedData = decryptData(encryptedData, privateKey);

                // Define decrypted file path
                Path decryptedFile = Paths.get("decryptedFile.txt");
//                Path decryptedFile = Paths.get("C:\\Users\\Dell\\IdeaProjects\\First_prj\\src\\com\\company\\decryptedFile.txt");
                writeToFile(decryptedData, decryptedFile);

                System.out.println("Decryption complete. Decrypted file saved.");
            } else {
                System.out.println("Invalid choice.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}
