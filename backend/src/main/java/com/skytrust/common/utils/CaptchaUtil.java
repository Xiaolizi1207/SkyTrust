package com.skytrust.common.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

/**
 * 图形验证码工具类
 * 使用Java2D生成随机验证码图片
 *
 * @author SkyTrust Team
 */
public class CaptchaUtil {

    private static final Random RANDOM = new SecureRandom();
    private static final char[] CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final Color[] COLORS = {
            new Color(0x1a, 0x73, 0xe8),
            new Color(0xea, 0x43, 0x35),
            new Color(0x34, 0xa8, 0x53),
            new Color(0xfb, 0xbc, 0x04),
            new Color(0x93, 0x3c, 0xde),
            new Color(0xe9, 0x1e, 0x63),
    };
    private static final Font[] FONTS = {
            new Font("Arial", Font.BOLD, 32),
            new Font("Courier New", Font.BOLD, 32),
            new Font("Verdana", Font.BOLD, 32),
            new Font("Tahoma", Font.BOLD, 32),
    };

    /**
     * 生成指定长度的随机验证码文本
     */
    public static String generateText(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS[RANDOM.nextInt(CHARS.length)]);
        }
        return sb.toString();
    }

    /**
     * 生成验证码图片并返回Base64编码
     *
     * @param captchaText 验证码文本
     * @param width       图片宽度
     * @param height      图片高度
     * @return CaptchaResult 包含验证码文本和Base64图片数据
     */
    public static CaptchaResult generate(String captchaText, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        try {
            // 抗锯齿
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // 填充背景
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);

            // 绘制干扰线
            g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < 6; i++) {
                int xs = RANDOM.nextInt(width);
                int ys = RANDOM.nextInt(height);
                int xe = xs + RANDOM.nextInt(40) - 20;
                int ye = ys + RANDOM.nextInt(40) - 20;
                g2d.setColor(COLORS[RANDOM.nextInt(COLORS.length)]);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                g2d.drawLine(xs, ys, xe, ye);
            }

            // 绘制干扰点
            for (int i = 0; i < 80; i++) {
                int x = RANDOM.nextInt(width);
                int y = RANDOM.nextInt(height);
                g2d.setColor(COLORS[RANDOM.nextInt(COLORS.length)]);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
                g2d.fillRect(x, y, 2, 2);
            }

            // 绘制验证码字符
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            int charWidth = width / captchaText.length();
            for (int i = 0; i < captchaText.length(); i++) {
                // 随机旋转
                AffineTransform orig = g2d.getTransform();
                double angle = RANDOM.nextDouble() * 0.6 - 0.3;
                int x = i * charWidth + charWidth / 4;
                int y = height - 8;
                g2d.setFont(FONTS[RANDOM.nextInt(FONTS.length)]);
                g2d.setColor(COLORS[RANDOM.nextInt(COLORS.length)]);
                g2d.translate(x + 5, y - 4);
                g2d.rotate(angle);
                g2d.drawString(String.valueOf(captchaText.charAt(i)), 0, 0);
                g2d.setTransform(orig);
            }

            // 绘制一条随机曲线（干扰）
            int x1 = RANDOM.nextInt(width / 3);
            int y1 = RANDOM.nextInt(height);
            int x2 = width - RANDOM.nextInt(width / 3);
            int y2 = RANDOM.nextInt(height);
            g2d.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.setColor(COLORS[RANDOM.nextInt(COLORS.length)]);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
            g2d.drawLine(x1, y1, x2, y2);

            // 转换为Base64
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());

            return new CaptchaResult(captchaText, "data:image/png;base64," + base64);
        } catch (IOException e) {
            throw new RuntimeException("生成验证码图片失败", e);
        } finally {
            g2d.dispose();
        }
    }

    /**
     * 验证码生成结果
     */
    @Data
    @AllArgsConstructor
    public static class CaptchaResult {
        /**
         * 验证码文本（仅用于服务端验证，不返回给客户端）
         */
        private String text;

        /**
         * Base64编码的图片数据
         */
        private String image;
    }
}
