package ssi.master.descrypto;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainController {
    @FXML
    private ImageView logoImage;

    @FXML
    private TextArea textAreaToEncrypt;

    @FXML
    private TextArea textAreaDecrypted;

    @FXML
    private Button encryptButton;

    @FXML
    private Button decryptButton;

    @FXML
    private Button generateKeyButton;

    @FXML
    private Button loadKeyButton;

    @FXML
    private Button saveKeyButton;

    @FXML
    private ComboBox<String> algorithmComboBox;

    @FXML
    private TextArea logsTextArea;

    @FXML
    private TextField fileToEncryptField;

    @FXML
    private Button browseButton;

    @FXML
    private Button encryptFileButton;

    @FXML
    private Button browseDecryptButton;

    @FXML
    private TextField fileToDecryptField;

    @FXML
    private Button decryptFileButton;

    @FXML
    private Button keyListButton;

    @FXML
    private Button encryptionHistoryButton;

    @FXML
    private Button saveToFileButton;

    @FXML
    private TextArea performanceTextArea;

    @FXML
    private Button testPerformanceButton;

    @FXML
    private Label algoUsedLabel;
    private DESUtils desUtils;
    private TripleDESUtils tripleDESUtils;
    private String selectedAlgorithm = "DES"; // Default algorithm

    @FXML
    private void initialize() {
        desUtils = null;
        tripleDESUtils = null;
        setButtonsDisabled(true);
        Image image = new Image(getClass().getResourceAsStream("/ssi/master/descrypto/images/logo-ensa.png"));
        logoImage.setImage(image);

        // Populate ComboBox with algorithm options
        algorithmComboBox.getItems().addAll("DES", "3DES");
        algorithmComboBox.setValue("DES"); // Default value

        algorithmComboBox.setOnAction(event -> selectedAlgorithm = algorithmComboBox.getValue());

        generateKeyButton.setOnAction(event -> generateKey());
        loadKeyButton.setOnAction(event -> loadKey());
        saveKeyButton.setOnAction(event -> saveKey());
        encryptButton.setOnAction(event -> encryptText());
        decryptButton.setOnAction(event -> decryptText());


        browseButton.setOnAction(event -> browseFileToEncrypt());
        encryptFileButton.setOnAction(event -> encryptFile());
        browseDecryptButton.setOnAction(event -> browseFileToDecrypt());
        decryptFileButton.setOnAction(event -> decryptFile());

        saveToFileButton.setOnAction(event -> saveLogsToFile());
        testPerformanceButton.setOnAction(event -> testPerformance());

        algoUsedLabel.setText("");
    }


    private void setButtonsDisabled(boolean disabled) {
        encryptButton.setDisable(disabled);
        decryptButton.setDisable(disabled);
        saveKeyButton.setDisable(disabled);
    }

    private void generateKey() {
        try {
            if ("DES".equals(selectedAlgorithm)) {
                desUtils = new DESUtils();
                algoUsedLabel.setText("DES");
            } else {
                tripleDESUtils = new TripleDESUtils();
                algoUsedLabel.setText("3DES");

            }
            logOperation("Generated key for " + selectedAlgorithm);
            showAlert("Success", "Key generated successfully!", AlertType.INFORMATION);
            setButtonsDisabled(false);
        } catch (Exception e) {
            showAlert("Error", "Failed to generate key: " + e.getMessage(), AlertType.ERROR);
        }
    }

    private void saveKey() {
        if (desUtils == null && tripleDESUtils == null) {
            showAlert("Warning", "No key to save. Please generate or load a key first.", AlertType.WARNING);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Key");
        File projectRoot = new File(System.getProperty("user.dir"));
        fileChooser.setInitialDirectory(projectRoot);

        File file = fileChooser.showSaveDialog(new Stage());
        if (file != null) {
            try {
                if (desUtils != null) {
                    desUtils.saveKeyToFile(file.getName());
                } else if (tripleDESUtils != null) {
                    tripleDESUtils.saveKeyToFile(file.getName());
                }
                logOperation("Saved key for " + selectedAlgorithm);
                showAlert("Success", "Key saved to " + file.getAbsolutePath(), AlertType.INFORMATION);
            } catch (IOException e) {
                showAlert("Error", "Failed to save key: " + e.getMessage(), AlertType.ERROR);
            }
        }
    }

    private void loadKey() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Key");
        File projectRoot = new File(System.getProperty("user.dir"));
        fileChooser.setInitialDirectory(projectRoot);

        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            try {
                SecretKey loadedKey;
                if ("DES".equals(selectedAlgorithm)) {
                    loadedKey = DESUtils.loadKeyFromFile(file.getAbsolutePath());
                    desUtils = new DESUtils(loadedKey.getEncoded());
                } else {
                    loadedKey = DESUtils.loadKeyFromFile(file.getAbsolutePath());
                    tripleDESUtils = new TripleDESUtils(loadedKey.getEncoded());
                }
                logOperation("Loaded key for " + selectedAlgorithm);
                showAlert("Success", "Key loaded from " + file.getAbsolutePath(), AlertType.INFORMATION);
                setButtonsDisabled(false);
            } catch (IOException | ClassNotFoundException e) {
                showAlert("Error", "Failed to load key: " + e.getMessage(), AlertType.ERROR);
            }
        }
    }

    private void encryptText() {
        if (desUtils == null && tripleDESUtils == null) {
            showAlert("Error", "No key loaded. Please load a key before encrypting.", AlertType.ERROR);
            return;
        }

        String textToEncrypt = textAreaToEncrypt.getText();
        if (textToEncrypt.isEmpty()) {
            showAlert("Warning", "No text to encrypt.", AlertType.WARNING);
            return;
        }

        try {
            String encryptedText;
            if ("DES".equals(selectedAlgorithm)) {
                encryptedText = desUtils.encrypt(textToEncrypt);
            } else {
                encryptedText = tripleDESUtils.encrypt(textToEncrypt);
            }
            textAreaToEncrypt.setText(encryptedText);
            logOperation("Encrypted text using " + selectedAlgorithm);
            showAlert("Success", "Text encrypted successfully!", AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Error", "Failed to encrypt text: " + e.getMessage(), AlertType.ERROR);
        }
    }

    private void decryptText() {
        if (desUtils == null && tripleDESUtils == null) {
            showAlert("Error", "No key loaded. Please load a key before decrypting.", AlertType.ERROR);
            return;
        }

        String textToDecrypt = textAreaDecrypted.getText();
        if (textToDecrypt.isEmpty()) {
            showAlert("Warning", "No text to decrypt.", AlertType.WARNING);
            return;
        }

        try {
            String decryptedText;
            if ("DES".equals(selectedAlgorithm)) {
                decryptedText = desUtils.decrypt(textToDecrypt);
            } else {
                decryptedText = tripleDESUtils.decrypt(textToDecrypt);
            }
            textAreaDecrypted.setText(decryptedText);
            logOperation("Decrypted text using " + selectedAlgorithm);
            showAlert("Success", "Text decrypted successfully!", AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Error", "Failed to decrypt text: " + e.getMessage(), AlertType.ERROR);
        }
    }

    private void encryptFile() {
        if (desUtils == null && tripleDESUtils == null) {
            showAlert("Error", "No key loaded. Please load a key before encrypting.", AlertType.ERROR);
            return;
        }

        File inputFile = new File(fileToEncryptField.getText());
        if (!inputFile.exists()) {
            showAlert("Error", "File does not exist.", AlertType.ERROR);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Encrypted File");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        File outputFile = fileChooser.showSaveDialog(new Stage());

        if (outputFile != null) {
            try {
                if ("DES".equals(selectedAlgorithm)) {
                    desUtils.encryptFile(inputFile, outputFile);
                } else {
                    tripleDESUtils.encryptFile(inputFile, outputFile);
                }
                showAlert("Success", "File encrypted successfully!", AlertType.INFORMATION);
                logsTextArea.appendText("Encrypted file using " + selectedAlgorithm + "\n");
            } catch (Exception e) {
                showAlert("Error", "Failed to encrypt file: " + e.getMessage(), AlertType.ERROR);
            }
        }
    }

    private void decryptFile() {
        if (desUtils == null && tripleDESUtils == null) {
            showAlert("Error", "No key loaded. Please load a key before decrypting.", AlertType.ERROR);
            return;
        }

        File inputFile = new File(fileToDecryptField.getText());
        if (!inputFile.exists()) {
            showAlert("Error", "File does not exist.", AlertType.ERROR);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Decrypted File");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        File outputFile = fileChooser.showSaveDialog(new Stage());

        if (outputFile != null) {
            try {
                if ("DES".equals(selectedAlgorithm)) {
                    desUtils.decryptFile(inputFile, outputFile);
                } else {
                    tripleDESUtils.decryptFile(inputFile, outputFile);
                }
                showAlert("Success", "File decrypted successfully!", AlertType.INFORMATION);
                logsTextArea.appendText("Decrypted file using " + selectedAlgorithm + "\n");
            } catch (Exception e) {
                showAlert("Error", "Failed to decrypt file: " + e.getMessage(), AlertType.ERROR);
            }
        }
    }

    private void browseFileToEncrypt() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose File to Encrypt");
        File projectRoot = new File(System.getProperty("user.dir"));
        fileChooser.setInitialDirectory(projectRoot);
        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            fileToEncryptField.setText(file.getAbsolutePath());
        }
    }

    private void browseFileToDecrypt() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose File to Decrypt");
        File projectRoot = new File(System.getProperty("user.dir"));
        fileChooser.setInitialDirectory(projectRoot);
        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            fileToDecryptField.setText(file.getAbsolutePath());
        }
    }

    private void saveLogsToFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Logs");
        File projectRoot = new File(System.getProperty("user.dir"));
        fileChooser.setInitialDirectory(projectRoot);

        File file = fileChooser.showSaveDialog(new Stage());
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(logsTextArea.getText());
                showAlert("Success", "Logs saved to " + file.getAbsolutePath(), AlertType.INFORMATION);
                logOperation("Saved logs to " + file.getAbsolutePath());
            } catch (IOException e) {
                showAlert("Error", "Failed to save logs: " + e.getMessage(), AlertType.ERROR);
            }
        }
    }

    private void testPerformance() {
        if (desUtils == null && tripleDESUtils == null) {
            showAlert("Error", "No key loaded. Please load a key before testing.", AlertType.ERROR);
            return;
        }

        String testText = "Sample text for performance testing";
        long startTime, endTime, durationEncrypt, durationDecrypt;

        try {
            String algorithm = algorithmComboBox.getValue();
            String encryptedText;

            // Test encryption
            startTime = System.nanoTime();
            if ("DES".equals(algorithm)) {
                encryptedText = desUtils.encrypt(testText);
            } else {
                encryptedText = tripleDESUtils.encrypt(testText);
            }
            endTime = System.nanoTime();
            durationEncrypt = endTime - startTime;

            // Test decryption
            startTime = System.nanoTime();
            if ("DES".equals(algorithm)) {
                desUtils.decrypt(encryptedText);
            } else {
                tripleDESUtils.decrypt(encryptedText);
            }
            endTime = System.nanoTime();
            durationDecrypt = endTime - startTime;

            // Display results
            performanceTextArea.setText(
                    algorithm + " Encryption Time: " + durationEncrypt + " ns\n" +
                            algorithm + " Decryption Time: " + durationDecrypt + " ns"
            );
        } catch (Exception e) {
            showAlert("Error", "Performance test failed: " + e.getMessage(), AlertType.ERROR);
        }
    }


    private void logOperation(String operation) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        logsTextArea.appendText("[" + timestamp + "] " + operation + "\n");
    }

    private void showAlert(String title, String message, AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
