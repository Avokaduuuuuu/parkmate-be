package com.parkmate.common;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class QRCodeGenerator {

    /**
     * Generate QR code image from VietQR string and return as Base64
     *
     * @param content VietQR string (EMVCo format)
     * @param width QR code width (pixels)
     * @param height QR code height (pixels)
     * @return Base64 encoded PNG image
     */
    public String generateQRCodeBase64(String content, int width, int height)
            throws WriterException, IOException {

        // Configure QR code generation
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1); // Border size

        // Generate QR code matrix
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(
                content,
                BarcodeFormat.QR_CODE,
                width,
                height,
                hints
        );

        // Convert to PNG image
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

        // Convert to Base64
        byte[] imageBytes = outputStream.toByteArray();
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        log.debug("Generated QR code - size: {} bytes", imageBytes.length);

        return base64Image;
    }

    /**
     * Generate with default size (500x500)
     */
    public String generateQRCodeBase64(String content)
            throws WriterException, IOException {
        return generateQRCodeBase64(content, 500, 500);
    }
}