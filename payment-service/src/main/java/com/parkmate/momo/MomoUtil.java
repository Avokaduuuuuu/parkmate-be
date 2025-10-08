package com.parkmate.momo;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeSet;

@Slf4j
public class MomoUtil {
    // Function 1: Tạo raw signature string từ params
    public static String createRawSignature(Map<String, String> params) {
        // TODO: Bạn sẽ làm gì ở đây?
        // Hint: Sắp xếp keys, nối chuỗi
        TreeSet<String> keys = new TreeSet<>(params.keySet());
        StringBuilder rawSignature = new StringBuilder();
        for (String key : keys) {
            rawSignature.append(key).append("=").append(params.get(key)).append("&");
        }

        return rawSignature.substring(0, rawSignature.length() - 1);
    }

    // Function 2: Hash bằng HMAC SHA256
    public static String hmacSHA256(String data, String secretKey) throws NoSuchAlgorithmException, InvalidKeyException {
        // TODO: Bạn sẽ làm gì ở đây?
        // Hint: Dùng Mac class
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKeySpec);
        byte[] rawHmac = mac.doFinal(data.getBytes());
        return bytesToHex(rawHmac);
    }

    // Function 3: Verify signature từ MoMo
    public static boolean verifySignature(String receivedSignature,
                                          Map<String, String> params,
                                          String secretKey) {
        // TODO: Bạn sẽ làm gì ở đây?
        // Hint: Tạo lại signature rồi so sánh
        if (receivedSignature == null || receivedSignature.isEmpty()) {
            return false;
        }
        if (params == null || params.isEmpty()) {
            return false;
        }
        if (secretKey == null || secretKey.isEmpty()) {
            return false;
        }

        try {
            String rawSignature = createRawSignature(params);
            String calculatedSignature = hmacSHA256(rawSignature, secretKey);
            return calculatedSignature.equals(receivedSignature);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.info("Error verifying signature: {}", e.getMessage());
            return false;
        }

    }

    private static String bytesToHex(byte[] bytes) {

        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }
}




