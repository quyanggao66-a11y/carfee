package com.example.carfee.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;

import java.nio.file.Path;

public class QrCodeUtil {

    public static void generate(String content, Path path) throws Exception {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(
                content,
                BarcodeFormat.QR_CODE,
                200,
                200
        );
        MatrixToImageWriter.writeToPath(matrix, "PNG", path);
    }
}

