package com.hintsight.n2he;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import static com.hintsight.n2he.Parameters.getPolydegree;
import static com.hintsight.n2he.Parameters.setFeatureLength;

public class LogisticRegression {

    public static void predict(String filepath, int numberOfUsers, int numberOfRows, int id) {
        int[][] creditCardDetails = new int[numberOfUsers][numberOfRows];

        try {
            // read in csv tabular data
            FileReader fileReader = new FileReader(filepath);
            CSVReader csvReader = new CSVReaderBuilder(fileReader).withSkipLines(1).build();
            String[] nextRecord;
            int row = 0;
            int col = 0;

            while ((nextRecord = csvReader.readNext()) != null) {
                String[] values = nextRecord[0].split(" ");
                for (String value : values) {
                    float valueF = Float.parseFloat(value);
                    creditCardDetails[row][col] = (int) valueF;
                    col++;
                }
                row++;
                col = 0;
            }
        } catch (CsvValidationException | IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Read in csv tabular data");

        // ============================= ENCRYPTION =============================
        System.out.println();
        System.out.println("ENCRYPTION ======================");
        //read in public key
        String pkFilePath = "src/rlwe_rlpk.txt";
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(pkFilePath));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        scanner.useDelimiter(" ");
        long[][] publicKey = new long[2][getPolydegree()];
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < getPolydegree(); j++) {
                if (!scanner.hasNext())
                    break;
                publicKey[i][j] = Long.parseLong(scanner.next());
            }
            if (!scanner.hasNext())
                break;
            scanner.nextLine(); //to remove '\n'
        }
        scanner.close();
        System.out.println("Read in RLWE public key.");

        //encrypt tabular data
        setFeatureLength(23);
        System.out.printf("Encrypting tabular data of user id %d\n", id);
        long[][] encryptedData = Encryption.encrypt(creditCardDetails[id-1], publicKey);

        //POST request to cloud server
        Map<String, Object> postData = new HashMap<String, Object>();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyy_HH:mm:ss:SSS");
        String formattedNow = now.format(formatter);
        String username = "LR";

        postData.put("id", formattedNow);
        postData.put("name", username);
        postData.put("feature_vector", encryptedData);

        String serverPostUri = "<SERVER_URL>";
        try {
            NetworkManager.postJSON(serverPostUri, postData);
            System.out.printf("POST request success for username [%s] with id [%s]\n",
                    username, formattedNow);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }


        // ============================= DECRYPTION =============================
        System.out.println();
        System.out.println("DECRYPTION ======================");
        //read in secret key
        String skFilePath = "src/rlwe_rlsk.txt";
        try {
            scanner = new Scanner(new File(skFilePath));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        scanner.useDelimiter(" ");
        int[] secretKey = new int[getPolydegree()];
        for (int i = 0; i < getPolydegree(); i++) {
            if (!scanner.hasNext())
                break;
            secretKey[i] = Integer.parseInt(scanner.next());
        }
        scanner.close();
        System.out.println("Read in secret key.");

        //GET request from cloud server
        String serverGetUri = "<SERVER_URL>"
                + postData.get("name") + "_" + postData.get("id") + ".json";
        UserEncryptedResult encryptedResult;
        HttpResponse<String> response;
        ObjectMapper objectMapper = new ObjectMapper();
        int statusCode;
        int triesCount = 50;
        int millisecondsToSleep = 50;

        try {
            do {
                response = NetworkManager.getJSON(serverGetUri);
                statusCode = response.statusCode();
                triesCount -= 1;
                try {
                    TimeUnit.MILLISECONDS.sleep(millisecondsToSleep);
                    System.out.println("retry GET request");
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            } while (triesCount > 0 & statusCode != 200);

            if (statusCode == 200) {
                String jsonData = response.body();
                encryptedResult = objectMapper.readValue(jsonData, UserEncryptedResult.class);
            } else {
                throw new IOException("failed to complete GET request");
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        //decrypt result
        long[] encryptedInput = new long[getPolydegree()+1];
        for (int i = 0; i < (getPolydegree()+1); i++)
            encryptedInput[i] = encryptedResult.getResult()[i];


        long decryptedInput = Decryption.lwe64Dec(encryptedInput, secretKey, getPolydegree());
        System.out.println("decryption result of input = " + (decryptedInput));

        System.out.println();
        System.out.println("RESULT ======================");
        if (decryptedInput < 20)
            System.out.println("prediction SAFE");
        else
            System.out.println("prediction RISKY");
    }
}
