package com.eric.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Consumer;

public class NetUtils {

    public interface DownloadProgressCallback {
        void onProgress(long downloaded, long total);
        void onFileChanged(String fileName);
    }

    // 修改现有的downloadArtifact方法，添加回调参数
    public static void downloadArtifact(String url, String destination, DownloadProgressCallback callback) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(30000);

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("HTTP " + responseCode + ": " + connection.getResponseMessage());
        }

        long fileSize = connection.getContentLengthLong();
        String fileName = getFileNameFromUrl(url);

        if (callback != null) {
            callback.onFileChanged(fileName);
        }

        try (InputStream inputStream = connection.getInputStream();
             FileOutputStream outputStream = new FileOutputStream(destination)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalRead = 0;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalRead += bytesRead;

                if (callback != null && fileSize > 0) {
                    callback.onProgress(totalRead, fileSize);
                }
            }
        }

        connection.disconnect();
    }

    // 保持原有方法向后兼容
    public static void downloadArtifact(String url, String destination) throws Exception {
        downloadArtifact(url, destination, null);
    }

    private static String getFileNameFromUrl(String url) {
        int lastSlash = url.lastIndexOf('/');
        if (lastSlash != -1 && lastSlash < url.length() - 1) {
            return url.substring(lastSlash + 1);
        }
        return "unknown";
    }
}