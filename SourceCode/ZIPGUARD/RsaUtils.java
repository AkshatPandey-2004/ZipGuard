package ZIPGUARD;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import javax.crypto.*;
import java.util.Scanner;

public class RsaUtils {

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
    private static void writeToFile(byte[] data, Path path) throws IOException {
        Files.write(path, data);
    }

    // Read data from a file
    private static byte[] readFromFile(Path path) throws IOException {
        return Files.readAllBytes(path);
    }

    // Convert public key to PEM format
    private static String getPEMPublicKey(PublicKey publicKey) {
        String encoded = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        return "-----BEGIN PUBLIC KEY-----\n" +
                wrapText(encoded) +
                "\n-----END PUBLIC KEY-----";
    }

    // Convert private key to PEM format
    private static String getPEMPrivateKey(PrivateKey privateKey) {
        String encoded = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        return "-----BEGIN PRIVATE KEY-----\n" +
                wrapText(encoded) +
                "\n-----END PRIVATE KEY-----";
    }

    // Helper function to wrap Base64 text in 64-character lines (standard PEM format)
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

    // Read PEM key from file and return the PublicKey object
    private static PublicKey readPublicKeyFromFile(Path publicKeyFile) throws Exception {
        String pem = new String(readFromFile(publicKeyFile));
        String base64Encoded = pem.replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "").replace("\n", "");
        byte[] decodedKey = Base64.getDecoder().decode(base64Encoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey));
    }

    // Read PEM key from file and return the PrivateKey object
    private static PrivateKey readPrivateKeyFromFile(Path privateKeyFile) throws Exception {
        String pem = new String(readFromFile(privateKeyFile));
        String base64Encoded = pem.replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "").replace("\n", "");
        byte[] decodedKey = Base64.getDecoder().decode(base64Encoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decodedKey));
    }

    // Encrypt the file and generate keys
    public static void encryptFile(Path filePath) throws Exception {
        // Generate RSA Key Pair
        KeyPair keyPair = generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        String fileName = filePath.getFileName().toString();
        String baseName = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
        Path encryptedFile = filePath.getParent().resolve("RsaEncrypted_" + fileName);
        Path publicKeyFile = filePath.getParent().resolve("RsaPublicKey_" + baseName + ".pem");
        Path privateKeyFile = filePath.getParent().resolve("RsaPrivateKey_" + baseName + ".pem");

        // Read the file data
        byte[] fileData = readFromFile(filePath);

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
    }

    // Decrypt the file
    public static void decryptFile(Path filePath, Path keyFilePath) throws Exception {
        // Read the private key from the file
        PrivateKey privateKey = readPrivateKeyFromFile(keyFilePath);

        // Decrypt data using private key
        byte[] encryptedData = readFromFile(filePath);
        byte[] decryptedData = decryptData(encryptedData, privateKey);

        String fileName = filePath.getFileName().toString().replace("RsaEncrypted_", "");
        Path decryptedFile = filePath.getParent().resolve("RsaDecrypted_" + fileName);

        writeToFile(decryptedData, decryptedFile);

        System.out.println("Decryption complete. Decrypted file saved.");
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try {
            // Prompt user to choose encryption or decryption
            System.out.println("Choose an option:");
            System.out.println("1. Encrypt a file");
            System.out.println("2. Decrypt a file");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            System.out.println("Enter the file path:");
            String filePath = scanner.nextLine();
            Path inputFile = Paths.get(filePath);

            Path keyFile = null;
            if (choice == 2) { // For decryption, prompt for private key
                System.out.println("Enter the private key file path (in PEM format):");
                String privateKeyPath = scanner.nextLine();
                keyFile = Paths.get(privateKeyPath);
            }

            // Call the corresponding function based on user choice
            if (choice == 1) {
                encryptFile(inputFile);
            } else if (choice == 2) {
                decryptFile(inputFile, keyFile);
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
