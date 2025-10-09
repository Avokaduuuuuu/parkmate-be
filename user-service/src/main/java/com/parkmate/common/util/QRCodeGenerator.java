package com.parkmate.common.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for generating QR codes
 */
@Slf4j
public class QRCodeGenerator {

    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 300;
    private static final String IMAGE_FORMAT = "PNG";

    /**
     * Generate QR code as Base64 string
     *
     * @param content Content to encode in QR code
     * @return Base64 encoded PNG image string
     */
    public static String generateQRCodeBase64(String content) {
        return generateQRCodeBase64(content, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Generate QR code as Base64 string with custom size
     *
     * @param content Content to encode in QR code
     * @param width   QR code width in pixels
     * @param height  QR code height in pixels
     * @return Base64 encoded PNG image string
     */
    public static String generateQRCodeBase64(String content, int width, int height) {
        try {
            // Configure QR code parameters
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            // Generate QR code matrix
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            // Convert to image and encode as Base64
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, IMAGE_FORMAT, outputStream);
            byte[] qrCodeBytes = outputStream.toByteArray();

            // Return Base64 encoded string with data URI prefix for direct use in HTML
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(qrCodeBytes);

        } catch (WriterException | IOException e) {
            log.error("Error generating QR code for content: {}", content, e);
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    /**
     * Generate QR code as raw Base64 string (without data URI prefix)
     *
     * @param content Content to encode in QR code
     * @return Raw Base64 encoded PNG image string
     */
    public static String generateQRCodeBase64Raw(String content) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, DEFAULT_WIDTH, DEFAULT_HEIGHT, hints);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, IMAGE_FORMAT, outputStream);
            byte[] qrCodeBytes = outputStream.toByteArray();

            return Base64.getEncoder().encodeToString(qrCodeBytes);

        } catch (WriterException | IOException e) {
            log.error("Error generating QR code for content: {}", content, e);
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }
}