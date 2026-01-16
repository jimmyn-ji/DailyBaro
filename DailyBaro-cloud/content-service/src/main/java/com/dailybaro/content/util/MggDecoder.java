package com.dailybaro.content.util;

import java.io.*;

public class MggDecoder {
    /**
     * 调用命令行工具将mgg转为mp3，返回mp3字节流
     */
    public static byte[] decodeToMp3(File mggFile) throws Exception {
        String toolPath = "/usr/local/bin/ncmdump"; // 根据实际情况修改
        File mp3File = File.createTempFile("decode_", ".mp3");
        ProcessBuilder pb = new ProcessBuilder(toolPath, mggFile.getAbsolutePath(), "-o", mp3File.getAbsolutePath());
        pb.redirectErrorStream(true);
        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) throw new RuntimeException("mgg转码失败，exitCode=" + exitCode);
        byte[] mp3Bytes = java.nio.file.Files.readAllBytes(mp3File.toPath());
        mp3File.delete();
        return mp3Bytes;
    }

    /**
     * 使用ffmpeg将ogg转为mp3
     */
    public static byte[] decodeOggToMp3(File oggFile) throws Exception {
        String ffmpeg = "/usr/local/bin/ffmpeg"; // 根据实际情况修改
        File mp3File = File.createTempFile("decode_", ".mp3");
        ProcessBuilder pb = new ProcessBuilder(ffmpeg, "-y", "-i", oggFile.getAbsolutePath(), "-codec:a", "libmp3lame", mp3File.getAbsolutePath());
        pb.redirectErrorStream(true);
        Process process = pb.start();
        int exit = process.waitFor();
        if (exit != 0) throw new RuntimeException("ogg转码失败，exitCode=" + exit);
        byte[] mp3Bytes = java.nio.file.Files.readAllBytes(mp3File.toPath());
        mp3File.delete();
        return mp3Bytes;
    }
}
