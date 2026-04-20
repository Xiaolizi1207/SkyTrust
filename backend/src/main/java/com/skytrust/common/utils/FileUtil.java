package com.skytrust.common.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * 文件操作工具类
 *
 * @author SkyTrust Team
 */
public class FileUtil {

    private static final int BUFFER_SIZE = 8192; // 8KB缓冲区

    private FileUtil() {
        // 工具类，防止实例化
    }

    // ==================== 文件路径相关方法 ====================

    /**
     * 获取文件扩展名（不含点）
     *
     * @param fileName 文件名
     * @return 扩展名
     */
    public static String getFileExtension(String fileName) {
        if (StringUtil.isEmpty(fileName)) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * 获取文件名（不含扩展名）
     *
     * @param fileName 文件名
     * @return 文件名（不含扩展名）
     */
    public static String getFileNameWithoutExtension(String fileName) {
        if (StringUtil.isEmpty(fileName)) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return fileName;
        }
        return fileName.substring(0, lastDotIndex);
    }

    /**
     * 生成唯一文件名（UUID + 时间戳 + 扩展名）
     *
     * @param originalFileName 原始文件名
     * @return 唯一文件名
     */
    public static String generateUniqueFileName(String originalFileName) {
        if (StringUtil.isEmpty(originalFileName)) {
            return UUID.randomUUID().toString();
        }
        String extension = getFileExtension(originalFileName);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        if (StringUtil.isEmpty(extension)) {
            return timestamp + "_" + uuid;
        } else {
            return timestamp + "_" + uuid + "." + extension;
        }
    }

    /**
     * 生成按日期分层的文件路径（如：2026/04/04/）
     *
     * @return 日期分层路径
     */
    public static String generateDatePath() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd/"));
    }

    /**
     * 生成完整的文件存储路径
     *
     * @param baseDir      基础目录
     * @param originalName 原始文件名
     * @return 完整文件路径
     */
    public static String generateFilePath(String baseDir, String originalName) {
        String datePath = generateDatePath();
        String fileName = generateUniqueFileName(originalName);
        return Paths.get(baseDir, datePath, fileName).toString();
    }

    /**
     * 标准化路径（将反斜杠替换为正斜杠，去除多余斜杠）
     *
     * @param path 路径
     * @return 标准化后的路径
     */
    public static String normalizePath(String path) {
        if (StringUtil.isEmpty(path)) {
            return path;
        }
        // 替换反斜杠为正斜杠
        path = path.replace('\\', '/');
        // 去除多余斜杠
        while (path.contains("//")) {
            path = path.replace("//", "/");
        }
        // 去除末尾斜杠（除非是根目录）
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    // ==================== 文件读写相关方法 ====================

    /**
     * 读取文件内容为字符串
     *
     * @param filePath 文件路径
     * @return 文件内容
     */
    public static String readFileToString(String filePath) {
        if (StringUtil.isEmpty(filePath)) {
            return null;
        }
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("读取文件失败: " + filePath, e);
        }
    }

    /**
     * 读取文件内容为字节数组
     *
     * @param filePath 文件路径
     * @return 字节数组
     */
    public static byte[] readFileToBytes(String filePath) {
        if (StringUtil.isEmpty(filePath)) {
            return null;
        }
        try {
            return Files.readAllBytes(Paths.get(filePath));
        } catch (IOException e) {
            throw new RuntimeException("读取文件失败: " + filePath, e);
        }
    }

    /**
     * 将字符串写入文件
     *
     * @param filePath 文件路径
     * @param content  内容
     * @return 是否成功
     */
    public static boolean writeStringToFile(String filePath, String content) {
        if (StringUtil.isEmpty(filePath)) {
            return false;
        }
        try {
            createParentDirs(filePath);
            Files.write(Paths.get(filePath), content.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            throw new RuntimeException("写入文件失败: " + filePath, e);
        }
    }

    /**
     * 将字节数组写入文件
     *
     * @param filePath 文件路径
     * @param bytes    字节数组
     * @return 是否成功
     */
    public static boolean writeBytesToFile(String filePath, byte[] bytes) {
        if (StringUtil.isEmpty(filePath) || bytes == null) {
            return false;
        }
        try {
            createParentDirs(filePath);
            Files.write(Paths.get(filePath), bytes);
            return true;
        } catch (IOException e) {
            throw new RuntimeException("写入文件失败: " + filePath, e);
        }
    }

    /**
     * 追加内容到文件
     *
     * @param filePath 文件路径
     * @param content  内容
     * @return 是否成功
     */
    public static boolean appendToFile(String filePath, String content) {
        if (StringUtil.isEmpty(filePath)) {
            return false;
        }
        try {
            createParentDirs(filePath);
            Files.write(Paths.get(filePath), content.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            throw new RuntimeException("追加文件失败: " + filePath, e);
        }
    }

    /**
     * 读取文件的每一行
     *
     * @param filePath 文件路径
     * @return 行列表
     */
    public static List<String> readLines(String filePath) {
        if (StringUtil.isEmpty(filePath)) {
            return new ArrayList<>();
        }
        try {
            return Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("读取文件行失败: " + filePath, e);
        }
    }

    // ==================== 文件操作相关方法 ====================

    /**
     * 创建父目录
     *
     * @param filePath 文件路径
     */
    public static void createParentDirs(String filePath) {
        if (StringUtil.isEmpty(filePath)) {
            return;
        }
        Path path = Paths.get(filePath);
        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new RuntimeException("创建目录失败: " + parent, e);
            }
        }
    }

    /**
     * 创建目录
     *
     * @param dirPath 目录路径
     * @return 是否成功
     */
    public static boolean createDirectory(String dirPath) {
        if (StringUtil.isEmpty(dirPath)) {
            return false;
        }
        try {
            Files.createDirectories(Paths.get(dirPath));
            return true;
        } catch (IOException e) {
            throw new RuntimeException("创建目录失败: " + dirPath, e);
        }
    }

    /**
     * 删除文件或目录
     *
     * @param path 路径
     * @return 是否成功
     */
    public static boolean delete(String path) {
        if (StringUtil.isEmpty(path)) {
            return false;
        }
        try {
            Files.deleteIfExists(Paths.get(path));
            return true;
        } catch (IOException e) {
            throw new RuntimeException("删除失败: " + path, e);
        }
    }

    /**
     * 递归删除目录
     *
     * @param dirPath 目录路径
     * @return 是否成功
     */
    public static boolean deleteDirectory(String dirPath) {
        if (StringUtil.isEmpty(dirPath)) {
            return false;
        }
        try {
            Files.walkFileTree(Paths.get(dirPath), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
            return true;
        } catch (IOException e) {
            throw new RuntimeException("删除目录失败: " + dirPath, e);
        }
    }

    /**
     * 复制文件
     *
     * @param sourcePath 源文件路径
     * @param targetPath 目标文件路径
     * @return 是否成功
     */
    public static boolean copyFile(String sourcePath, String targetPath) {
        if (StringUtil.isEmpty(sourcePath) || StringUtil.isEmpty(targetPath)) {
            return false;
        }
        try {
            createParentDirs(targetPath);
            Files.copy(Paths.get(sourcePath), Paths.get(targetPath), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            throw new RuntimeException("复制文件失败: " + sourcePath + " -> " + targetPath, e);
        }
    }

    /**
     * 移动文件
     *
     * @param sourcePath 源文件路径
     * @param targetPath 目标文件路径
     * @return 是否成功
     */
    public static boolean moveFile(String sourcePath, String targetPath) {
        if (StringUtil.isEmpty(sourcePath) || StringUtil.isEmpty(targetPath)) {
            return false;
        }
        try {
            createParentDirs(targetPath);
            Files.move(Paths.get(sourcePath), Paths.get(targetPath), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            throw new RuntimeException("移动文件失败: " + sourcePath + " -> " + targetPath, e);
        }
    }

    /**
     * 检查文件是否存在
     *
     * @param filePath 文件路径
     * @return 是否存在
     */
    public static boolean exists(String filePath) {
        if (StringUtil.isEmpty(filePath)) {
            return false;
        }
        return Files.exists(Paths.get(filePath));
    }

    /**
     * 检查是否是文件
     *
     * @param filePath 文件路径
     * @return 是否是文件
     */
    public static boolean isFile(String filePath) {
        if (StringUtil.isEmpty(filePath)) {
            return false;
        }
        return Files.isRegularFile(Paths.get(filePath));
    }

    /**
     * 检查是否是目录
     *
     * @param dirPath 目录路径
     * @return 是否是目录
     */
    public static boolean isDirectory(String dirPath) {
        if (StringUtil.isEmpty(dirPath)) {
            return false;
        }
        return Files.isDirectory(Paths.get(dirPath));
    }

    /**
     * 获取文件大小（字节）
     *
     * @param filePath 文件路径
     * @return 文件大小
     */
    public static long getFileSize(String filePath) {
        if (StringUtil.isEmpty(filePath)) {
            return 0;
        }
        try {
            return Files.size(Paths.get(filePath));
        } catch (IOException e) {
            throw new RuntimeException("获取文件大小失败: " + filePath, e);
        }
    }

    /**
     * 获取文件大小（格式化显示）
     *
     * @param filePath 文件路径
     * @return 格式化的大小
     */
    public static String getFileSizeFormatted(String filePath) {
        long size = getFileSize(filePath);
        return formatFileSize(size);
    }

    /**
     * 格式化文件大小
     *
     * @param size 文件大小（字节）
     * @return 格式化的大小
     */
    public static String formatFileSize(long size) {
        if (size <= 0) {
            return "0B";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.2f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    /**
     * 获取文件最后修改时间
     *
     * @param filePath 文件路径
     * @return 最后修改时间
     */
    public static LocalDateTime getLastModifiedTime(String filePath) {
        if (StringUtil.isEmpty(filePath)) {
            return null;
        }
        try {
            return LocalDateTime.ofInstant(Files.getLastModifiedTime(Paths.get(filePath)).toInstant(),
                    ZoneId.systemDefault());
        } catch (IOException e) {
            throw new RuntimeException("获取文件修改时间失败: " + filePath, e);
        }
    }

    // ==================== MultipartFile相关方法 ====================

    /**
     * 保存MultipartFile到本地
     *
     * @param file     MultipartFile
     * @param savePath 保存路径
     * @return 是否成功
     */
    public static boolean saveMultipartFile(MultipartFile file, String savePath) {
        if (file == null || file.isEmpty() || StringUtil.isEmpty(savePath)) {
            return false;
        }
        try {
            createParentDirs(savePath);
            file.transferTo(new File(savePath));
            return true;
        } catch (IOException e) {
            throw new RuntimeException("保存MultipartFile失败: " + savePath, e);
        }
    }

    /**
     * 获取MultipartFile的字节数组
     *
     * @param file MultipartFile
     * @return 字节数组
     */
    public static byte[] getMultipartFileBytes(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("获取MultipartFile字节失败", e);
        }
    }

    /**
     * 获取MultipartFile的内容类型
     *
     * @param file MultipartFile
     * @return 内容类型
     */
    public static String getMultipartFileContentType(MultipartFile file) {
        if (file == null) {
            return null;
        }
        return file.getContentType();
    }

    /**
     * 检查MultipartFile是否是图片
     *
     * @param file MultipartFile
     * @return 是否是图片
     */
    public static boolean isImage(MultipartFile file) {
        if (file == null) {
            return false;
        }
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    /**
     * 检查MultipartFile是否是PDF
     *
     * @param file MultipartFile
     * @return 是否是PDF
     */
    public static boolean isPdf(MultipartFile file) {
        if (file == null) {
            return false;
        }
        String contentType = file.getContentType();
        return "application/pdf".equals(contentType);
    }

    /**
     * 检查文件大小是否超过限制
     *
     * @param file   MultipartFile
     * @param maxSize 最大大小（字节）
     * @return 是否超过限制
     */
    public static boolean isFileSizeExceeded(MultipartFile file, long maxSize) {
        if (file == null) {
            return false;
        }
        return file.getSize() > maxSize;
    }

    // ==================== 压缩解压相关方法 ====================

    /**
     * 压缩文件或目录
     *
     * @param sourcePath 源文件或目录路径
     * @param zipPath    压缩文件路径
     * @return 是否成功
     */
    public static boolean zip(String sourcePath, String zipPath) {
        if (StringUtil.isEmpty(sourcePath) || StringUtil.isEmpty(zipPath)) {
            return false;
        }
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath))) {
            Path source = Paths.get(sourcePath);
            if (Files.isDirectory(source)) {
                Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        String entryName = source.relativize(file).toString().replace('\\', '/');
                        zos.putNextEntry(new ZipEntry(entryName));
                        Files.copy(file, zos);
                        zos.closeEntry();
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        if (!dir.equals(source)) {
                            String entryName = source.relativize(dir).toString().replace('\\', '/') + "/";
                            zos.putNextEntry(new ZipEntry(entryName));
                            zos.closeEntry();
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            } else {
                zos.putNextEntry(new ZipEntry(source.getFileName().toString()));
                Files.copy(source, zos);
                zos.closeEntry();
            }
            return true;
        } catch (IOException e) {
            throw new RuntimeException("压缩文件失败: " + sourcePath + " -> " + zipPath, e);
        }
    }

    /**
     * 解压文件
     *
     * @param zipPath    压缩文件路径
     * @param targetPath 目标目录路径
     * @return 是否成功
     */
    public static boolean unzip(String zipPath, String targetPath) {
        if (StringUtil.isEmpty(zipPath) || StringUtil.isEmpty(targetPath)) {
            return false;
        }
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath))) {
            createDirectory(targetPath);
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path entryPath = Paths.get(targetPath, entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    createParentDirs(entryPath.toString());
                    Files.copy(zis, entryPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
            return true;
        } catch (IOException e) {
            throw new RuntimeException("解压文件失败: " + zipPath + " -> " + targetPath, e);
        }
    }

    // ==================== 其他工具方法 ====================

    /**
     * 获取文件的Content-Disposition头值（用于下载）
     *
     * @param fileName 文件名
     * @return Content-Disposition头值
     */
    public static String getContentDisposition(String fileName) {
        if (StringUtil.isEmpty(fileName)) {
            return "attachment";
        }
        try {
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                    .replace("+", "%20");
            return "attachment; filename=\"" + encodedFileName + "\"; filename*=utf-8''" + encodedFileName;
        } catch (UnsupportedEncodingException e) {
            return "attachment; filename=\"" + fileName + "\"";
        }
    }

    /**
     * 获取临时文件路径
     *
     * @param prefix 前缀
     * @param suffix 后缀
     * @return 临时文件路径
     */
    public static String getTempFilePath(String prefix, String suffix) {
        try {
            File tempFile = File.createTempFile(prefix, suffix);
            return tempFile.getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException("创建临时文件失败", e);
        }
    }

    /**
     * 清理临时文件
     *
     * @param filePath 临时文件路径
     * @return 是否成功
     */
    public static boolean cleanupTempFile(String filePath) {
        if (StringUtil.isEmpty(filePath)) {
            return false;
        }
        try {
            Files.deleteIfExists(Paths.get(filePath));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 获取目录下的所有文件
     *
     * @param dirPath 目录路径
     * @return 文件列表
     */
    public static List<String> listFiles(String dirPath) {
        List<String> fileList = new ArrayList<>();
        if (StringUtil.isEmpty(dirPath) || !isDirectory(dirPath)) {
            return fileList;
        }
        try {
            Files.walk(Paths.get(dirPath), 1)
                    .filter(Files::isRegularFile)
                    .forEach(path -> fileList.add(path.toString()));
            return fileList;
        } catch (IOException e) {
            throw new RuntimeException("列出文件失败: " + dirPath, e);
        }
    }

    /**
     * 获取目录下的所有文件（递归）
     *
     * @param dirPath 目录路径
     * @return 文件列表
     */
    public static List<String> listFilesRecursive(String dirPath) {
        List<String> fileList = new ArrayList<>();
        if (StringUtil.isEmpty(dirPath) || !isDirectory(dirPath)) {
            return fileList;
        }
        try {
            Files.walk(Paths.get(dirPath))
                    .filter(Files::isRegularFile)
                    .forEach(path -> fileList.add(path.toString()));
            return fileList;
        } catch (IOException e) {
            throw new RuntimeException("递归列出文件失败: " + dirPath, e);
        }
    }
}