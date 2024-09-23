package ssi.master.descrypto;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.util.Base64;

public class TripleDESUtils {

    private static final String ALGORITHM = "DESede";
    private SecretKey secretKey;

    public TripleDESUtils() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(168);
        this.secretKey = keyGen.generateKey();
    }

    public TripleDESUtils(byte[] key) {
        this.secretKey = new SecretKeySpec(key, ALGORITHM);
    }

    public String encrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedData = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    public String decrypt(String encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decodedData = Base64.getDecoder().decode(encryptedData);
        byte[] originalData = cipher.doFinal(decodedData);
        return new String(originalData);
    }

    public void encryptFile(File inputFile, File outputFile) throws Exception {
        Cipher cipher = Cipher.getInstance("DESede");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        try (FileInputStream fis = new FileInputStream(inputFile);
             FileOutputStream fos = new FileOutputStream(outputFile);
             CipherOutputStream cos = new CipherOutputStream(fos, cipher)) {
            byte[] buffer = new byte[64];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                cos.write(buffer, 0, bytesRead);
            }
        }
    }

    public void decryptFile(File inputFile, File outputFile) throws Exception {
        Cipher cipher = Cipher.getInstance("DESede");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        try (FileInputStream fis = new FileInputStream(inputFile);
             FileOutputStream fos = new FileOutputStream(outputFile);
             CipherOutputStream cos = new CipherOutputStream(fos, cipher)) {
            byte[] buffer = new byte[64];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                cos.write(buffer, 0, bytesRead);
            }
        }
    }

    public byte[] getKey() {
        return secretKey.getEncoded();
    }

    public void saveKeyToFile(String filename) throws IOException {
        String projectRoot = System.getProperty("user.dir");
        File keyFile = new File(projectRoot, filename);

        try (FileOutputStream fos = new FileOutputStream(keyFile);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(secretKey.getEncoded());
        }
    }

    public static SecretKey loadKeyFromFile(String filename) throws IOException, ClassNotFoundException {
        try (FileInputStream fis = new FileInputStream(filename);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            byte[] keyBytes = (byte[]) ois.readObject();
            return new SecretKeySpec(keyBytes, ALGORITHM);
        }
    }
}
