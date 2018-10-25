package io.ipfs.api.originstamp;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Provides tools for automatically timestamping of data that is uploaded to IPFS. OriginStamp is a blockchain-based
 * trusted timestamping service, that can be used to prove the authenticity of any digital content. Instead of
 * submitting the original file, OriginStamp simply uses the SHA-256 of your file to preserve the privacy of your data.
 * <p>
 * https://originstamp.com
 *
 * @author Thomas Hepp
 */
public class OriginStamp {
    private static final String ORIGINSTAMP_ENDPOINT = "https://api.originstamp.com/v3/timestamp/create";
    private static final String ORIGINSTAMP_STATUS_ENDPOINT = "https://api.originstamp.com/v3/timestamp/";
    private final int TIMEOUT = 5000;

    /**
     * constructor which creates a new instance of the current class and hands over the api key parameter, that is
     * necessary for the OriginStamp APi Key
     *
     * @param pApiKey
     */
    public OriginStamp(String pApiKey) {
        this.checkApiKey(pApiKey);
        this.apiKey = pApiKey;
    }

    private final String apiKey;

    /**
     * Formal check whether the api key is valid or not
     *
     * @param apiKey
     * @throws IllegalArgumentException the format of the api key is not correct. Please contact https://originstamp.com
     *                                  for a valid api key
     */
    private void checkApiKey(String apiKey) throws IllegalArgumentException {
        UUID.fromString(apiKey);
    }

    /**
     * calculates the SHA256 hash from a byte array and returns it in HEX
     *
     * @param pBytes to be converted
     * @return hex string
     * @throws NoSuchAlgorithmException implementation error, used wrong identifier
     */
    public static String getSHA256(byte[] pBytes) throws NoSuchAlgorithmException {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        sha.update(pBytes);
        byte[] digest = sha.digest();
        return convertToHex(digest);
    }

    /**
     * converts a byte array to its hex string representation
     *
     * @param pBytes to be converted
     * @return hex string
     */
    private static String convertToHex(byte[] pBytes) {
        StringBuilder hexString = new StringBuilder();

        // for each byte
        for (byte pByte : pBytes) {
            String hex = Integer.toHexString(0xff & pByte);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString();
    }

    /**
     * Creates a tamper-proof blockchain timestamp with OriginStamp. The data is not sent to OriginStamp, but the SHA-256 of the original file
     *
     * @param pInput
     */
    public void timestamp(byte[] pInput) throws NoSuchAlgorithmException, IOException {
        // determine sha 256
        String hash = getSHA256(pInput);

        // execute
        this.executeRequest(hash);
    }

    /**
     * Checks the OriginStamp API if there is valid timestamp for a certain currency
     *
     * @param currency
     * @param pHash
     * @return
     * @throws IOException
     */
    public Optional<Date> getTimestamp(CryptoCurrency currency, String pHash) throws IOException {
        // execute
        String response = this.getStatus(pHash);

        // convert
        TimestampResponseDTO responseDTO = new Gson().fromJson(response, TimestampResponseDTO.class);

        // looking for timestamp information
        if (responseDTO == null) {
            return Optional.empty();
        } else if (responseDTO.getErrorMessage() != null) {
            // an error occurred
            return Optional.empty();
        } else if (responseDTO.getData() == null) {
            return Optional.empty();
        } else if (responseDTO.getData().getTimestampDataDTOList().isEmpty()) {
            return Optional.empty();
        } else {
            // checking timestamp data
            for (TimestampResponseDTO.Timestamp.TimestampDataDTO t : responseDTO.getData().getTimestampDataDTOList()) {
                // checking currency
                if (currency.getId() == t.getCurrency()) {
                    // checking the timestamp status
                    if (t.getStatus() == TimestampStatus.TIMESTAMP.getId()) {
                        // checking currency
                        return Optional.ofNullable(new Date(t.getTimestamp()));
                    } else {
                        // no timestamp for currency
                        return Optional.empty();
                    }
                }
            }
        }
        return Optional.empty();
    }


    /**
     * Executes the status request to read the timestamp status from OriginStamp
     *
     * @param pHash
     * @return
     * @throws IOException
     */
    private String getStatus(String pHash) throws IOException {
        // define endpoint
        URL url = new URL(ORIGINSTAMP_STATUS_ENDPOINT + "/" + pHash);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Authorization", this.apiKey);
        connection.setRequestProperty("User-Agent", "Java IPFS Client");
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-length", "0");
        connection.setUseCaches(false);
        connection.setAllowUserInteraction(false);
        connection.setConnectTimeout(TIMEOUT);
        connection.setReadTimeout(TIMEOUT);
        connection.connect();

        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        br.close();
        return sb.toString();
    }

    /**
     * Executes a POST request with the request parameters
     *
     * @param pHash
     * @throws IOException
     */
    private void executeRequest(String pHash) throws IOException {
        // define endpoint
        URL url = new URL(ORIGINSTAMP_ENDPOINT);

        // building request body
        Map<String, Object> params = getRequestParameters(pHash);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Authorization", this.apiKey);
        connection.setRequestProperty("User-Agent", "Java IPFS Client");

        OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
        wr.write(new Gson().toJson(params));
        wr.close();
    }

    /**
     * creates the request body for the OriginStamp POST request
     *
     * @param pHash
     * @return
     */
    private static Map<String, Object> getRequestParameters(String pHash) {
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("comment", null);
        requestBody.put("url", null);
        requestBody.put("hash", pHash);
        requestBody.put("notifications", new ArrayList<>());

        return requestBody;
    }

    /**
     * contains the timestamp status information
     */
    public enum TimestampStatus {
        NO_TIMESTAMP(0), TIMESTAMP(3);

        int currencyId;

        TimestampStatus(int currencyId) {
            this.currencyId = currencyId;
        }

        private int getId() {
            return this.currencyId;
        }
    }

    /***
     * enum class for defining the timestamp provider
     */
    public enum CryptoCurrency {
        BITCOIN(0), ETHEREUM(1);
        private int currencyId;

        CryptoCurrency(int currencyId) {
            this.currencyId = currencyId;
        }

        private int getId() {
            return this.currencyId;
        }
    }
}
