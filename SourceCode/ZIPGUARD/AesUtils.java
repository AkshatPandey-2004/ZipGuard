package ZIPGUARD;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Scanner;

public class AesUtils {

    // Public method for encryption
    public static void encryptFile(Path inputFile, Path outputFile, Path keyFile) throws Exception {
        SecretKey secretKey = generateAESKey();
        byte[] inputData = readFromFile(inputFile);
        byte[] encryptedData = encryptData(inputData, secretKey);

        // Write encrypted data and key to files
        writeToFile(encryptedData, outputFile);
        String aesKeyPEM = getPEMAESKey(secretKey);
        writeToFile(aesKeyPEM.getBytes(), keyFile);

        System.out.println("Encryption complete. Encrypted file and AES key saved.");
    }

    // Public method for decryption
    public static void decryptFile(Path inputFile, Path outputFile, Path keyFile) throws Exception {
        SecretKey secretKey = readAESKeyFromFile(keyFile);
        byte[] encryptedData = readFromFile(inputFile);
        byte[] decryptedData = decryptData(encryptedData, secretKey);

        // Write decrypted data to the output file
        writeToFile(decryptedData, outputFile);
        System.out.println("Decryption complete. Decrypted file saved.");
    }

    // Private method to generate AES key
    private static SecretKey generateAESKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);
        return keyGenerator.generateKey();
    }

    // Private method to encrypt data
    private static byte[] encryptData(byte[] data, SecretKey secretKey) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }

    // Private method to decrypt data
    private static byte[] decryptData(byte[] encryptedData, SecretKey secretKey) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(encryptedData);
    }

    // Private method to write data to a file
    private static void writeToFile(byte[] data, Path path) throws IOException {
        Files.write(path, data);
    }

    // Private method to read data from a file
    private static byte[] readFromFile(Path path) throws IOException {
        return Files.readAllBytes(path);
    }

    // Private method to convert AES SecretKey to PEM format
    private static String getPEMAESKey(SecretKey secretKey) {
        String encoded = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        return "-----BEGIN AES KEY-----\n" + wrapText(encoded) + "\n-----END AES KEY-----";
    }

    // Private helper method to wrap Base64 text in 64-character lines
    private static String wrapText(String text) {
        StringBuilder wrapped = new StringBuilder();
        int offset = 0;
        while (offset < text.length()) {
            int end = Math.min(offset + 64, text.length());
            wrapped.append(text, offset, end).append("\n");
            offset = end;
        }
        return wrapped.toString();
    }

    // Private method to read AES key from PEM file
    private static SecretKey readAESKeyFromFile(Path keyFile) throws Exception {
        String pem = new String(readFromFile(keyFile));
        String base64Encoded = pem.replace("-----BEGIN AES KEY-----", "")
                                  .replace("-----END AES KEY-----", "")
                                  .replace("\n", "");
        byte[] decodedKey = Base64.getDecoder().decode(base64Encoded);
        return new SecretKeySpec(decodedKey, "AES");
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.println("Choose an option:");
            System.out.println("1. Encrypt a file");
            System.out.println("2. Decrypt a file");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            System.out.println("Enter the file path:");
            Path inputFile = Paths.get(scanner.nextLine());

            if (!Files.exists(inputFile)) {
                System.err.println("File not found: " + inputFile.toAbsolutePath());
                return;
            }

            String fileName = inputFile.getFileName().toString();
            String baseName = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;

            if (choice == 1) {
                Path encryptedFile = inputFile.getParent().resolve("AesEncrypted_" + baseName + ".txt");
                Path aesKeyFile = inputFile.getParent().resolve("AesKey_" + baseName + ".pem");
                encryptFile(inputFile, encryptedFile, aesKeyFile);
            } else if (choice == 2) {
                System.out.println("Enter the AES key file path:");
                Path aesKeyFile = Paths.get(scanner.nextLine());

                if (!Files.exists(aesKeyFile)) {
                    System.err.println("AES key file not found: " + aesKeyFile.toAbsolutePath());
                    return;
                }

                Path decryptedFile = inputFile.getParent().resolve(fileName.replace("AesEncrypted_", "AesDecrypted_"));
                decryptFile(inputFile, decryptedFile, aesKeyFile);
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
