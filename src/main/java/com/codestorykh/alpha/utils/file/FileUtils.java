package com.codestorykh.alpha.utils.file;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class FileUtils {

    private static final int BUFFER_SIZE = 8192;
    private static final String[] IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"};
    private static final String[] DOCUMENT_EXTENSIONS = {".pdf", ".doc", ".docx", ".txt", ".rtf", ".odt"};
    private static final String[] VIDEO_EXTENSIONS = {".mp4", ".avi", ".mov", ".wmv", ".flv", ".webm"};
    private static final String[] AUDIO_EXTENSIONS = {".mp3", ".wav", ".flac", ".aac", ".ogg", ".wma"};

    /**
     * Check if file exists
     */
    public static boolean exists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    /**
     * Check if path is a directory
     */
    public static boolean isDirectory(String path) {
        return Files.isDirectory(Paths.get(path));
    }

    /**
     * Check if path is a regular file
     */
    public static boolean isFile(String path) {
        return Files.isRegularFile(Paths.get(path));
    }

    /**
     * Create directory if it doesn't exist
     */
    public static void createDirectoryIfNotExists(String directoryPath) {
        try {
            Path path = Paths.get(directoryPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            log.error("Failed to create directory: {}", directoryPath, e);
            throw new RuntimeException("Failed to create directory: " + directoryPath, e);
        }
    }

    /**
     * Get file size in bytes
     */
    public static long getFileSize(String filePath) {
        try {
            return Files.size(Paths.get(filePath));
        } catch (IOException e) {
            log.error("Failed to get file size: {}", filePath, e);
            return -1;
        }
    }

    /**
     * Get human readable file size
     */
    public static String getHumanReadableFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * Get file extension
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * Get file name without extension
     */
    public static String getFileNameWithoutExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return fileName;
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return fileName;
        }
        
        return fileName.substring(0, lastDotIndex);
    }

    /**
     * Check if file is an image
     */
    public static boolean isImage(String fileName) {
        String extension = getFileExtension(fileName);
        for (String ext : IMAGE_EXTENSIONS) {
            if (ext.equals("." + extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if file is a document
     */
    public static boolean isDocument(String fileName) {
        String extension = getFileExtension(fileName);
        for (String ext : DOCUMENT_EXTENSIONS) {
            if (ext.equals("." + extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if file is a video
     */
    public static boolean isVideo(String fileName) {
        String extension = getFileExtension(fileName);
        for (String ext : VIDEO_EXTENSIONS) {
            if (ext.equals("." + extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if file is an audio file
     */
    public static boolean isAudio(String fileName) {
        String extension = getFileExtension(fileName);
        for (String ext : AUDIO_EXTENSIONS) {
            if (ext.equals("." + extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Read file content as string
     */
    public static String readFileAsString(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            log.error("Failed to read file: {}", filePath, e);
            throw new RuntimeException("Failed to read file: " + filePath, e);
        }
    }

    /**
     * Write string content to file
     */
    public static void writeStringToFile(String content, String filePath) {
        try {
            Files.write(Paths.get(filePath), content.getBytes());
        } catch (IOException e) {
            log.error("Failed to write file: {}", filePath, e);
            throw new RuntimeException("Failed to write file: " + filePath, e);
        }
    }

    /**
     * Copy file
     */
    public static void copyFile(String sourcePath, String targetPath) {
        try {
            Files.copy(Paths.get(sourcePath), Paths.get(targetPath), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Failed to copy file from {} to {}", sourcePath, targetPath, e);
            throw new RuntimeException("Failed to copy file", e);
        }
    }

    /**
     * Move file
     */
    public static void moveFile(String sourcePath, String targetPath) {
        try {
            Files.move(Paths.get(sourcePath), Paths.get(targetPath), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Failed to move file from {} to {}", sourcePath, targetPath, e);
            throw new RuntimeException("Failed to move file", e);
        }
    }

    /**
     * Delete file
     */
    public static boolean deleteFile(String filePath) {
        try {
            return Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            log.error("Failed to delete file: {}", filePath, e);
            return false;
        }
    }

    /**
     * Delete directory and all its contents
     */
    public static boolean deleteDirectory(String directoryPath) {
        try {
            Path path = Paths.get(directoryPath);
            if (Files.exists(path)) {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
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
            }
            return false;
        } catch (IOException e) {
            log.error("Failed to delete directory: {}", directoryPath, e);
            return false;
        }
    }

    /**
     * List all files in directory
     */
    public static List<String> listFiles(String directoryPath) {
        try {
            return Files.list(Paths.get(directoryPath))
                    .filter(Files::isRegularFile)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Failed to list files in directory: {}", directoryPath, e);
            return new ArrayList<>();
        }
    }

    /**
     * List all directories in directory
     */
    public static List<String> listDirectories(String directoryPath) {
        try {
            return Files.list(Paths.get(directoryPath))
                    .filter(Files::isDirectory)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Failed to list directories in directory: {}", directoryPath, e);
            return new ArrayList<>();
        }
    }

    /**
     * List all files and directories in directory
     */
    public static List<String> listAll(String directoryPath) {
        try {
            return Files.list(Paths.get(directoryPath))
                    .map(Path::toString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Failed to list all in directory: {}", directoryPath, e);
            return new ArrayList<>();
        }
    }

    /**
     * Find files by extension
     */
    public static List<String> findFilesByExtension(String directoryPath, String extension) {
        try {
            return Files.walk(Paths.get(directoryPath))
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith("." + extension.toLowerCase()))
                    .map(Path::toString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Failed to find files by extension in directory: {}", directoryPath, e);
            return new ArrayList<>();
        }
    }

    /**
     * Calculate MD5 hash of file
     */
    public static String calculateMD5(String filePath) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            
            try (InputStream is = Files.newInputStream(Paths.get(filePath))) {
                while ((bytesRead = is.read(buffer)) != -1) {
                    md.update(buffer, 0, bytesRead);
                }
            }
            
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            log.error("Failed to calculate MD5 for file: {}", filePath, e);
            throw new RuntimeException("Failed to calculate MD5 for file: " + filePath, e);
        }
    }

    /**
     * Calculate SHA-256 hash of file
     */
    public static String calculateSHA256(String filePath) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            
            try (InputStream is = Files.newInputStream(Paths.get(filePath))) {
                while ((bytesRead = is.read(buffer)) != -1) {
                    md.update(buffer, 0, bytesRead);
                }
            }
            
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            log.error("Failed to calculate SHA-256 for file: {}", filePath, e);
            throw new RuntimeException("Failed to calculate SHA-256 for file: " + filePath, e);
        }
    }

    /**
     * Get file creation time
     */
    public static long getFileCreationTime(String filePath) {
        try {
            BasicFileAttributes attrs = Files.readAttributes(Paths.get(filePath), BasicFileAttributes.class);
            return attrs.creationTime().toMillis();
        } catch (IOException e) {
            log.error("Failed to get file creation time: {}", filePath, e);
            return -1;
        }
    }

    /**
     * Get file last modified time
     */
    public static long getFileLastModifiedTime(String filePath) {
        try {
            return Files.getLastModifiedTime(Paths.get(filePath)).toMillis();
        } catch (IOException e) {
            log.error("Failed to get file last modified time: {}", filePath, e);
            return -1;
        }
    }

    /**
     * Check if file is empty
     */
    public static boolean isEmpty(String filePath) {
        try {
            return Files.size(Paths.get(filePath)) == 0;
        } catch (IOException e) {
            log.error("Failed to check if file is empty: {}", filePath, e);
            return true;
        }
    }

    /**
     * Get file permissions
     */
    public static String getFilePermissions(String filePath) {
        try {
            Path path = Paths.get(filePath);
            StringBuilder permissions = new StringBuilder();
            
            if (Files.isReadable(path)) permissions.append("r");
            else permissions.append("-");
            
            if (Files.isWritable(path)) permissions.append("w");
            else permissions.append("-");
            
            if (Files.isExecutable(path)) permissions.append("x");
            else permissions.append("-");
            
            return permissions.toString();
        } catch (Exception e) {
            log.error("Failed to get file permissions: {}", filePath, e);
            return "---";
        }
    }
} 