package com.company;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Scanner;

public class AESFileEncryption {

    // Generate AES Secret Key
    public static SecretKey generateAESKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);  // AES key size (can be 128, 192, or 256)
        return keyGenerator.generateKey();
    }

    // Encrypt data using AES key
    public static byte[] encryptData(byte[] data, SecretKey secretKey) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }

    // Decrypt data using AES key
    public static byte[] decryptData(byte[] encryptedData, SecretKey secretKey) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
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

    // Convert AES SecretKey to PEM format (Base64 encoded string)
    public static String getPEMAESKey(SecretKey secretKey) {
        String encoded = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        return "-----BEGIN AES KEY-----\n" +
                wrapText(encoded) +
                "\n-----END AES KEY-----";
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

    // Read AES key from PEM file and return the SecretKey object
    public static SecretKey readAESKeyFromFile(Path keyFile) throws Exception {
        String pem = new String(readFromFile(keyFile));
        String base64Encoded = pem.replace("-----BEGIN AES KEY-----", "").replace("-----END AES KEY-----", "").replace("\n", "");
        byte[] decodedKey = Base64.getDecoder().decode(base64Encoded);
        return new SecretKeySpec(decodedKey, "AES");  // Create SecretKey from decoded bytes
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

                // Generate AES Key
                SecretKey secretKey = generateAESKey();

                // Define file paths for encrypted file and key
                Path encryptedFile = Paths.get("encryptedFileAES.txt");
//                Path encryptedFile = Paths.get("C:\\Users\\Dell\\IdeaProjects\\First_prj\\src\\com\\company\\encryptedFileAES.txt");
                Path aesKeyFile = Paths.get("aesKey.pem");
//                Path aesKeyFile = Paths.get("C:\\Users\\Dell\\IdeaProjects\\First_prj\\src\\com\\company\\aesKey.pem");

                if (!Files.exists(inputFile)) {
                    System.err.println("Input file not found: " + inputFile.toAbsolutePath());
                    return;
                }

                // Read the file data
                byte[] fileData = readFromFile(inputFile);

                // Encrypt data using AES key
                byte[] encryptedData = encryptData(fileData, secretKey);
                writeToFile(encryptedData, encryptedFile);

                // Save the AES key to file in PEM format
                String aesKeyPEM = getPEMAESKey(secretKey);
                writeToFile(aesKeyPEM.getBytes(), aesKeyFile);

                System.out.println("Encryption complete. Encrypted file and AES key saved.");
            } else if (choice == 2) {
                // Decryption process
                System.out.println("Enter the file path to decrypt:");
                String filePath = scanner.nextLine();
                Path encryptedFile = Paths.get(filePath);

                // Ask for the path to the AES key
                System.out.println("Enter the AES key file path (in PEM format):");
                String keyFilePath = scanner.nextLine();
                Path aesKeyFile = Paths.get(keyFilePath);

                if (!Files.exists(encryptedFile)) {
                    System.err.println("Encrypted file not found: " + encryptedFile.toAbsolutePath());
                    return;
                }

                if (!Files.exists(aesKeyFile)) {
                    System.err.println("AES key file not found: " + aesKeyFile.toAbsolutePath());
                    return;
                }

                // Read the AES key from the file
                SecretKey secretKey = readAESKeyFromFile(aesKeyFile);

                // Decrypt data using AES key
                byte[] encryptedData = readFromFile(encryptedFile);
                byte[] decryptedData = decryptData(encryptedData, secretKey);

                // Define decrypted file path
                Path decryptedFile = Paths.get("decryptedFileAES.txt");
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
