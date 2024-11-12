package com.hintsight.n2he;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import static com.hintsight.n2he.Parameters.getFeatureLength;
import static com.hintsight.n2he.Parameters.getPolydegree;

public class FacialVerification {

    public static void verify() {
        Scanner scanner = null;
        //read in pre-processed features
        String featuresFilePath = "src/features.txt";
        try {
            scanner = new Scanner(new File(featuresFilePath));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        scanner.useDelimiter(" ");
        int[] features = new int[getFeatureLength()];
        for (int i = 0; i < getFeatureLength(); i++) {
            if (!scanner.hasNext())
                break;
            features[i] = Integer.parseInt(scanner.next());
        }
        scanner.close();
        System.out.println("Read in pre-processed facial features.");

        // ============================= ENCRYPTION =============================
        System.out.println("ENCRYPTION ======================");
        //read in public key
        String pkFilePath = "src/rlwe_pk.txt";
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

        //encrypt facial features
        long[][] encryptedData = Encryption.encrypt(features, publicKey);

        //POST request to cloud server
        Map<String, Object> postData = new HashMap<String, Object>();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyy_HH:mm:ss:SSS");
        String formattedNow = now.format(formatter);
        String username = "WangXiangning";

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
        String skFilePath = "src/rlwe_sk.txt";
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
        int bias1 = 870;
        int bias2 = -870;
        long[] encryptedInput1 = new long[getPolydegree()+1];
        long[] encryptedInput2 = new long[getPolydegree()+1];
        for (int i = 0; i < (getPolydegree()+1); i++)
            encryptedInput1[i] = encryptedResult.getResult()[i];
        for (int j = 0; j < (getPolydegree()+1); j++)
            encryptedInput2[j] = encryptedResult.getResult()[j+getPolydegree()+1];

        long decryptedInput1 = Decryption.lwe64Dec(encryptedInput1, secretKey, getPolydegree());
        System.out.println("decryption result of input1 = " + (decryptedInput1 + bias1));

        long decryptedInput2 = Decryption.lwe64Dec(encryptedInput2, secretKey, getPolydegree());
        System.out.println("decryption result of input2 = " + (decryptedInput2 + bias2));

        System.out.println();
        System.out.println("RESULT ======================");
        if (decryptedInput1+bias1 < decryptedInput2+bias2)
            System.out.println("verification SUCCESS");
        else
            System.out.println("verification FAILED");
    }

}
