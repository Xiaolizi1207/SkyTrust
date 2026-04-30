package com.skytrust.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * 二维码生成控制器
 */
@Tag(name = "二维码", description = "二维码生成接口")
@RestController
@RequestMapping("/api/qrcode")
public class QRCodeController {

    @Operation(summary = "生成二维码图片")
    @GetMapping(produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] generateQRCode(
            @Parameter(description = "小程序页面路径") @RequestParam(defaultValue = "pages/index/index") String path,
            @Parameter(description = "图片尺寸") @RequestParam(defaultValue = "300") int size) throws Exception {

        String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8.name());
        if (!decodedPath.startsWith("pages/")) {
            decodedPath = "pages/index/index";
        }

        BitMatrix bitMatrix = new MultiFormatWriter().encode(
                decodedPath, BarcodeFormat.QR_CODE, size, size);
        BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return baos.toByteArray();
    }
}
