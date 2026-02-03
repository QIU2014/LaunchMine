package io.github.qiu2014.launchmine.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * JSON 工具类，用于读取 JSON 文件并转换为列表
 */
public class JsonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 从文件路径读取 JSON 并转换为版本列表
     * @param filePath 文件路径
     * @return 版本信息列表
     */
    public static List<Map<String, Object>> readVersionListFromFile(String filePath) {
        try {
            File file = new File(filePath);
            return readVersionListFromFile(file);
        } catch (Exception e) {
            System.err.println("读取文件失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 从 File 对象读取 JSON 并转换为版本列表
     * @param file 文件对象
     * @return 版本信息列表
     */
    public static List<Map<String, Object>> readVersionListFromFile(File file) {
        try {
            Map<String, Object> jsonData = objectMapper.readValue(file,
                    new TypeReference<Map<String, Object>>() {});
            return extractVersionList(jsonData);
        } catch (Exception e) {
            System.err.println("解析 JSON 文件失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 从 URL 读取 JSON 并转换为版本列表
     * @param urlString URL 地址
     * @return 版本信息列表
     */
    public static List<Map<String, Object>> readVersionListFromUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            return readVersionListFromUrl(url);
        } catch (Exception e) {
            System.err.println("URL 格式错误: " + e.getMessage());
            return null;
        }
    }

    /**
     * 从 URL 对象读取 JSON 并转换为版本列表
     * @param url URL 对象
     * @return 版本信息列表
     */
    public static List<Map<String, Object>> readVersionListFromUrl(URL url) {
        try {
            Map<String, Object> jsonData = objectMapper.readValue(url,
                    new TypeReference<Map<String, Object>>() {});
            return extractVersionList(jsonData);
        } catch (Exception e) {
            System.err.println("从 URL 读取 JSON 失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 从 InputStream 读取 JSON 并转换为版本列表
     * @param inputStream 输入流
     * @return 版本信息列表
     */
    public static List<Map<String, Object>> readVersionListFromStream(InputStream inputStream) {
        try {
            Map<String, Object> jsonData = objectMapper.readValue(inputStream,
                    new TypeReference<Map<String, Object>>() {});
            return extractVersionList(jsonData);
        } catch (Exception e) {
            System.err.println("从流读取 JSON 失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 从 JSON 字符串读取并转换为版本列表
     * @param jsonString JSON 字符串
     * @return 版本信息列表
     */
    public static List<Map<String, Object>> readVersionListFromString(String jsonString) {
        try {
            Map<String, Object> jsonData = objectMapper.readValue(jsonString,
                    new TypeReference<Map<String, Object>>() {});
            return extractVersionList(jsonData);
        } catch (Exception e) {
            System.err.println("解析 JSON 字符串失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 提取版本列表
     * @param jsonData JSON 数据 Map
     * @return 版本信息列表
     */
    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> extractVersionList(Map<String, Object> jsonData) {
        try {
            return (List<Map<String, Object>>) jsonData.get("versions");
        } catch (Exception e) {
            System.err.println("提取版本列表失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 获取最新版本信息
     * @param filePath 文件路径
     * @return 最新版本信息 Map
     */
    public static Map<String, String> getLatestVersions(String filePath) {
        try {
            Map<String, Object> jsonData = objectMapper.readValue(new File(filePath),
                    new TypeReference<Map<String, Object>>() {});

            @SuppressWarnings("unchecked")
            Map<String, String> latest = (Map<String, String>) jsonData.get("latest");
            return latest;
        } catch (Exception e) {
            System.err.println("获取最新版本信息失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 按版本类型筛选版本
     * @param versions 版本列表
     * @param type 版本类型 ("release" 或 "snapshot")
     * @return 筛选后的版本列表
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> filterVersionsByType(List<Map<String, Object>> versions, String type) {
        if (versions == null || type == null) {
            return null;
        }

        return versions.stream()
                .filter(version -> type.equals(version.get("type")))
                .toList();
    }

    /**
     * 查找特定版本
     * @param versions 版本列表
     * @param versionId 版本 ID (如 "1.21.11", "26.1-snapshot-4")
     * @return 版本信息 Map，未找到则返回 null
     */
    public static Map<String, Object> findVersionById(List<Map<String, Object>> versions, String versionId) {
        if (versions == null || versionId == null) {
            return null;
        }

        return versions.stream()
                .filter(version -> versionId.equals(version.get("id")))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取所有版本 ID
     * @param versions 版本列表
     * @return 版本 ID 列表
     */
    public static List<String> getAllVersionIds(List<Map<String, Object>> versions) {
        if (versions == null) {
            return null;
        }

        return versions.stream()
                .map(version -> (String) version.get("id"))
                .toList();
    }

    /**
     * 统计不同类型版本的数量
     * @param versions 版本列表
     * @return 各类型版本数量统计 Map
     */
    public static Map<String, Long> countVersionsByType(List<Map<String, Object>> versions) {
        if (versions == null) {
            return null;
        }

        return versions.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        version -> (String) version.get("type"),
                        java.util.stream.Collectors.counting()
                ));
    }
    /**
     * 获取指定版本 ID 的 URL
     * @param versions 版本列表
     * @param versionId 版本 ID
     * @return 对应的 URL，未找到则返回 null
     */
    public static String getUrlForVersion(List<Map<String, Object>> versions, String versionId) {
        if (versions == null || versionId == null) {
            return null;
        }

        Map<String, Object> version = findVersionById(versions, versionId);
        if (version != null) {
            return (String) version.get("url");
        }
        return null;
    }

    /**
     * 获取指定版本 ID 的 URL（直接从文件）
     * @param filePath 文件路径
     * @param versionId 版本 ID
     * @return 对应的 URL
     */
    public static String getUrlForVersion(String filePath, String versionId) {
        List<Map<String, Object>> versions = readVersionListFromFile(filePath);
        return getUrlForVersion(versions, versionId);
    }

    /**
     * 获取所有版本的 URL 映射
     * @param versions 版本列表
     * @return 版本ID到URL的映射 Map
     */
    public static Map<String, String> getVersionUrls(List<Map<String, Object>> versions) {
        if (versions == null) {
            return null;
        }

        return versions.stream()
                .collect(java.util.stream.Collectors.toMap(
                        version -> (String) version.get("id"),
                        version -> (String) version.get("url")
                ));
    }

    /**
     * 获取特定类型版本的 URL 列表
     * @param versions 版本列表
     * @param type 版本类型 ("release" 或 "snapshot")
     * @return 版本ID到URL的映射 Map
     */
    public static Map<String, String> getVersionUrlsByType(List<Map<String, Object>> versions, String type) {
        List<Map<String, Object>> filteredVersions = filterVersionsByType(versions, type);
        return getVersionUrls(filteredVersions);
    }

    /**
     * 获取最新版本的名称（release 版本）
     * @param filePath 文件路径
     * @return 最新 release 版本的名称
     */
    public static String getLatestReleaseVersion(String filePath) {
        try {
            Map<String, Object> jsonData = objectMapper.readValue(new File(filePath),
                    new TypeReference<Map<String, Object>>() {});

            @SuppressWarnings("unchecked")
            Map<String, String> latest = (Map<String, String>) jsonData.get("latest");
            return latest != null ? latest.get("release") : null;
        } catch (Exception e) {
            System.err.println("获取最新版本失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 获取最新 snapshot 版本的名称
     * @param filePath 文件路径
     * @return 最新 snapshot 版本的名称
     */
    public static String getLatestSnapshotVersion(String filePath) {
        try {
            Map<String, Object> jsonData = objectMapper.readValue(new File(filePath),
                    new TypeReference<Map<String, Object>>() {});

            @SuppressWarnings("unchecked")
            Map<String, String> latest = (Map<String, String>) jsonData.get("latest");
            return latest != null ? latest.get("snapshot") : null;
        } catch (Exception e) {
            System.err.println("获取最新 snapshot 版本失败: " + e.getMessage());
            return null;
        }
    }
    /**
     * 获取最新 release 版本的完整信息
     * @param filePath 文件路径
     * @return 包含最新 release 版本所有信息的 Map
     */
    public static Map<String, Object> getLatestReleaseCompleteInfo(String filePath) {
        try {
            // 读取整个 JSON 文件
            Map<String, Object> jsonData = objectMapper.readValue(new File(filePath),
                    new TypeReference<Map<String, Object>>() {});

            // 获取最新版本名称
            @SuppressWarnings("unchecked")
            Map<String, String> latest = (Map<String, String>) jsonData.get("latest");
            if (latest == null) {
                return null;
            }
            String latestReleaseName = latest.get("release");

            // 获取版本列表
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> versions = (List<Map<String, Object>>) jsonData.get("versions");

            // 查找最新版本
            for (Map<String, Object> version : versions) {
                if (latestReleaseName.equals(version.get("id"))) {
                    return version; // 返回完整的版本信息
                }
            }

            return null;
        } catch (Exception e) {
            System.err.println("获取最新 release 信息失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 获取最新 release 版本的 URL
     * @param filePath 文件路径
     * @return 最新 release 版本的 URL
     */
    public static String getLatestReleaseUrl(String filePath) {
        Map<String, Object> latestInfo = getLatestReleaseCompleteInfo(filePath);
        if (latestInfo != null) {
            return (String) latestInfo.get("url");
        }
        return null;
    }
}