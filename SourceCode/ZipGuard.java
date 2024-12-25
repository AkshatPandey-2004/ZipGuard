import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import ZIPGUARD.*;

public class ZipGuard {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\t\t\t=========================================");
        System.out.println("\t\t\t                 ZIP GUARD               ");
        System.out.println("\t\t\t=========================================");

        System.out.println("Welcome to Zip Guard - Your Secure File Management Tool!");
        System.out.println("With Zip Guard, you can compress and decompress files using Huffman coding, or encrypt and decrypt files with AES or RSA encryption.");
        System.out.println("Simply choose an option from the menu below, provide the required file paths, and let Zip Guard handle the rest.");
        System.out.println("Ensure your files are accessible, and keep your key files secure during encryption and decryption.");
        System.out.println();
        System.out.println("1: Compress a file using Huffman coding");
        System.out.println("2: Decompress a file using Huffman coding");
        System.out.println("3: Encrypt a file using AES");
        System.out.println("4: Decrypt a file using AES");
        System.out.println("5: Encrypt a file using RSA");
        System.out.println("6: Decrypt a file using RSA");
        System.out.print("Choice: ");
        int ch = scanner.nextInt();
        scanner.nextLine(); // Consume newline after integer input
        if (ch == 1) {
        
            System.out.println("Enter the file path to compress:");
            String inputFilePath = scanner.nextLine();
            HuffmanUtils.compressFile(inputFilePath);
        }
        else if (ch == 2) {
            System.out.print("Enter the compressed file path: ");
            String compressedFilePath = scanner.nextLine();
            System.out.print("Enter the tree file path: ");
            String treeFilePath = scanner.nextLine();
            HuffmanUtils.decompressFile(compressedFilePath, treeFilePath);
        }
        else if (ch == 3) {
            AesUtils obj = new AesUtils();
            System.out.println("Enter the file path to encrypt:");
            Path inputFile = Paths.get(scanner.nextLine());
            
            if (!Files.exists(inputFile)) {
                System.err.println("File not found: " + inputFile.toAbsolutePath());
                return;
            }

            String fileName = inputFile.getFileName().toString();
            String baseName = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;

            // Check if inputFile has a parent directory, otherwise use current directory
            Path parentDir = inputFile.getParent() != null ? inputFile.getParent() : Paths.get(".");

            // Generate paths for encrypted file and AES key file
            Path encryptedFile = parentDir.resolve("AesEncrypted_" + baseName + ".txt");
            Path aesKeyFile = parentDir.resolve("AesKey_" + baseName + ".pem");
            
            AesUtils.encryptFile(inputFile, encryptedFile, aesKeyFile);
        }
        else if (ch == 4) {
            AesUtils obj = new AesUtils();
            System.out.println("Enter the file path to decrypt:");
            Path inputFile = Paths.get(scanner.nextLine());
            
            if (!Files.exists(inputFile)) {
                System.err.println("File not found: " + inputFile.toAbsolutePath());
                return;
            }

            String fileName = inputFile.getFileName().toString();
            String baseName = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;

            // Check if inputFile has a parent directory, otherwise use current directory
            Path parentDir = inputFile.getParent() != null ? inputFile.getParent() : Paths.get(".");

            System.out.println("Enter the AES key file path:");
                Path aesKeyFile = Paths.get(scanner.nextLine());

                if (!Files.exists(aesKeyFile)) {
                    System.err.println("AES key file not found: " + aesKeyFile.toAbsolutePath());
                    return;
                }

                Path decryptedFile = inputFile.getParent().resolve(fileName.replace("AesEncrypted_", "AesDecrypted_"));
                AesUtils.decryptFile(inputFile, decryptedFile, aesKeyFile);
        }
        else if (ch == 5) {
            System.out.println("Enter the file path to encrypt:");
            String filePath = scanner.nextLine();
            Path inputFile = Paths.get(filePath);
            RsaUtils.encryptFile(inputFile);
        }
        else if (ch == 6) {
            System.out.println("Enter the file path to decrypt:");
            String filePath = scanner.nextLine();
            Path inputFile = Paths.get(filePath);
            System.out.println("Enter the private key file path (in PEM format):");
            String privateKeyPath = scanner.nextLine();
            Path keyFile = Paths.get(privateKeyPath);
            RsaUtils.decryptFile(inputFile, keyFile);
        }
        else {
            System.out.println("INVALID");
        }
    }
}
