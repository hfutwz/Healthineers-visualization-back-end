package com.demo.utils;

import com.demo.entity.InjuryRecord;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 经纬度获取工具类
 * 根据地址描述（injuryLocationDesc）调用高德地图API获取经纬度信息
 * 
 * <p>功能特性：
 * <ul>
 *   <li>地址标准化处理（去除无效标记、统一格式）</li>
 *   <li>内存缓存机制（避免重复API调用）</li>
 *   <li>重试机制（最多重试2次）</li>
 *   <li>双API策略（地理编码API + 地点搜索API）</li>
 *   <li>请求限流（每次请求间隔0.25秒）</li>
 * </ul>
 * 
 * @author system
 */
public final class LongitudeLatitudeUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(LongitudeLatitudeUtils.class);
    
    /**
     * 城市前缀：上海市
     */
    private static final String CITY_PREFIX = "上海市";
    
    /**
     * 请求间隔时间（毫秒）：0.25秒
     */
    private static final long REQUEST_INTERVAL_MS = 250;
    
    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY_COUNT = 2;
    
    /**
     * HTTP连接超时时间（毫秒）
     */
    private static final int CONNECT_TIMEOUT_MS = 8000;
    
    /**
     * HTTP读取超时时间（毫秒）
     */
    private static final int READ_TIMEOUT_MS = 8000;
    
    /**
     * 无效地址标记集合
     */
    private static final Set<String> INVALID_ADDRESS_TOKENS = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList(
            "(跳过)", "*", "0", "无", "nan", "", "家", "家中", "自行", "家中摔倒", 
            "家门口", "住所", "小区", "居民楼", "别墅", "家庭", "不详", "(空)", "未知"
        ))
    );
    
    /**
     * 地址缓存（内存缓存，应用重启后会清空）
     * Key: 标准化后的地址，Value: [经度, 纬度]
     */
    private static final Map<String, double[]> ADDRESS_CACHE = new ConcurrentHashMap<>();
    
    /**
     * 私有构造函数，防止实例化
     */
    private LongitudeLatitudeUtils() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }
    
    /**
     * 批量更新记录的经纬度字段
     * 
     * @param records 需要更新的记录列表，不能为null
     * @param apiKey 高德地图API Key，不能为null或空
     * @param city 城市名称（如"上海"），不能为null或空
     * @throws IllegalArgumentException 如果参数无效
     */
    public static void updateLongitudeLatitude(List<InjuryRecord> records, String apiKey, String city) {
        if (records == null || records.isEmpty()) {
            logger.debug("记录列表为空，跳过经纬度更新");
            return;
        }
        
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("高德地图API Key不能为空");
        }
        
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("城市名称不能为空");
        }
        
        // 收集需要更新的地址（去重）
        Set<String> uniqueAddresses = new HashSet<>();
        Map<String, List<InjuryRecord>> addressToRecords = new HashMap<>();
        
        for (InjuryRecord record : records) {
            if (record == null) {
                continue;
            }
            
            String address = record.getInjuryLocationDesc();
            if (isValidAddress(address)) {
                String normalized = normalizeAddress(address);
                if (normalized != null && !normalized.isEmpty()) {
                    uniqueAddresses.add(normalized);
                    addressToRecords.computeIfAbsent(normalized, k -> new ArrayList<>()).add(record);
                }
            }
        }
        
        logger.info("开始更新经纬度信息，共 {} 个唯一地址", uniqueAddresses.size());
        
        int successCount = 0;
        int failCount = 0;
        
        // 为每个唯一地址获取经纬度
        for (String normalizedAddress : uniqueAddresses) {
            double[] lngLat = getLongitudeLatitude(normalizedAddress, apiKey, city);
            
            if (lngLat != null && lngLat.length == 2) {
                // 更新所有使用该地址的记录
                List<InjuryRecord> recordsForAddress = addressToRecords.get(normalizedAddress);
                if (recordsForAddress != null) {
                    for (InjuryRecord record : recordsForAddress) {
                        record.setLongitude(lngLat[0]);
                        record.setLatitude(lngLat[1]);
                        successCount++;
                    }
                }
            } else {
                failCount++;
                logger.debug("无法获取地址的经纬度: {}", normalizedAddress);
            }
            
            // 避免请求过快
            try {
                Thread.sleep(REQUEST_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("线程被中断，停止经纬度更新", e);
                break;
            }
        }
        
        logger.info("经纬度更新完成，成功 {} 条，失败 {} 条", successCount, failCount);
    }
    
    /**
     * 标准化地址
     * 去除无效标记、统一格式、添加城市前缀
     * 
     * @param address 原始地址，可以为null
     * @return 标准化后的地址，如果地址无效则返回null
     */
    public static String normalizeAddress(String address) {
        if (address == null) {
            return null;
        }
        
        String s = address.trim();
        if (s.isEmpty() || INVALID_ADDRESS_TOKENS.contains(s)) {
            return null;
        }
        
        // 去除括号内容
        s = s.replaceAll("[（(][^）)]*[）)]", "");
        
        // 去除标点和空白
        s = s.replaceAll("[，,。.；;、\\s]+", "");
        
        // 处理重复的"上海市"
        s = s.replace("上海市上海市", "上海市");
        
        if (s.isEmpty() || INVALID_ADDRESS_TOKENS.contains(s)) {
            return null;
        }
        
        // 统一"上海市"前缀
        if (s.startsWith("上海") && !s.startsWith(CITY_PREFIX)) {
            s = CITY_PREFIX + s.substring(2);
        }
        if (!s.startsWith(CITY_PREFIX)) {
            s = CITY_PREFIX + s;
        }
        
        return s;
    }
    
    /**
     * 判断地址是否有效
     * 
     * @param address 地址字符串，可以为null
     * @return true 如果地址有效，false 如果地址无效
     */
    public static boolean isValidAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return false;
        }
        String trimmed = address.trim();
        return !INVALID_ADDRESS_TOKENS.contains(trimmed);
    }
    
    /**
     * 获取经纬度（带缓存和重试）
     * 
     * @param normalizedAddress 标准化后的地址，不能为null
     * @param apiKey 高德地图API Key，不能为null
     * @param city 城市名称，不能为null
     * @return 经纬度数组 [经度, 纬度]，如果获取失败则返回null
     */
    private static double[] getLongitudeLatitude(String normalizedAddress, String apiKey, String city) {
        if (normalizedAddress == null || normalizedAddress.isEmpty()) {
            return null;
        }
        
        // 先检查缓存
        if (ADDRESS_CACHE.containsKey(normalizedAddress)) {
            logger.debug("从缓存获取经纬度: {}", normalizedAddress);
            return ADDRESS_CACHE.get(normalizedAddress);
        }
        
        // 尝试地理编码API
        double[] result = geocodeAddress(normalizedAddress, apiKey, city);
        if (result != null) {
            ADDRESS_CACHE.put(normalizedAddress, result);
            return result;
        }
        
        // 如果地理编码失败，尝试地点搜索API（去掉"上海市"前缀）
        String keyword = normalizedAddress.startsWith(CITY_PREFIX) 
            ? normalizedAddress.substring(CITY_PREFIX.length()) 
            : normalizedAddress;
        
        result = searchPlace(keyword, apiKey, city);
        if (result != null) {
            ADDRESS_CACHE.put(normalizedAddress, result);
            return result;
        }
        
        return null;
    }
    
    /**
     * 调用高德地理编码API获取经纬度
     * 
     * @param address 地址，不能为null
     * @param apiKey 高德地图API Key，不能为null
     * @param city 城市名称，不能为null
     * @return 经纬度数组 [经度, 纬度]，如果获取失败则返回null
     */
    private static double[] geocodeAddress(String address, String apiKey, String city) {
        for (int attempt = 0; attempt <= MAX_RETRY_COUNT; attempt++) {
            try {
                String urlStr = "https://restapi.amap.com/v3/geocode/geo"
                    + "?key=" + URLEncoder.encode(apiKey, "UTF-8")
                    + "&address=" + URLEncoder.encode(address, "UTF-8")
                    + "&city=" + URLEncoder.encode(city, "UTF-8")
                    + "&citylimit=true"
                    + "&batch=false";
                
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
                conn.setReadTimeout(READ_TIMEOUT_MS);
                conn.setRequestMethod("GET");
                
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    String responseBody = readResponse(conn);
                    if (responseBody != null) {
                        double[] result = parseGeocodeResponse(responseBody);
                        if (result != null) {
                            return result;
                        }
                    }
                } else {
                    logger.debug("地理编码API返回非200状态码: {}", responseCode);
                }
            } catch (Exception e) {
                if (attempt < MAX_RETRY_COUNT) {
                    logger.debug("地理编码API调用失败，重试中... (尝试 {}/{}): {}", 
                        attempt + 1, MAX_RETRY_COUNT + 1, e.getMessage());
                    try {
                        Thread.sleep(REQUEST_INTERVAL_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        logger.warn("重试等待被中断", ie);
                        return null;
                    }
                } else {
                    logger.debug("地理编码API调用失败（已重试{}次）: {}", MAX_RETRY_COUNT + 1, e.getMessage());
                }
            }
        }
        return null;
    }
    
    /**
     * 调用高德地点搜索API获取经纬度
     * 
     * @param keyword 搜索关键词，不能为null
     * @param apiKey 高德地图API Key，不能为null
     * @param city 城市名称，不能为null
     * @return 经纬度数组 [经度, 纬度]，如果获取失败则返回null
     */
    private static double[] searchPlace(String keyword, String apiKey, String city) {
        for (int attempt = 0; attempt <= MAX_RETRY_COUNT; attempt++) {
            try {
                String urlStr = "https://restapi.amap.com/v3/place/text"
                    + "?key=" + URLEncoder.encode(apiKey, "UTF-8")
                    + "&keywords=" + URLEncoder.encode(keyword, "UTF-8")
                    + "&city=" + URLEncoder.encode(city, "UTF-8")
                    + "&citylimit=true"
                    + "&offset=1"
                    + "&page=1";
                
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
                conn.setReadTimeout(READ_TIMEOUT_MS);
                conn.setRequestMethod("GET");
                
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    String responseBody = readResponse(conn);
                    if (responseBody != null) {
                        double[] result = parsePlaceSearchResponse(responseBody);
                        if (result != null) {
                            return result;
                        }
                    }
                } else {
                    logger.debug("地点搜索API返回非200状态码: {}", responseCode);
                }
            } catch (Exception e) {
                if (attempt < MAX_RETRY_COUNT) {
                    logger.debug("地点搜索API调用失败，重试中... (尝试 {}/{}): {}", 
                        attempt + 1, MAX_RETRY_COUNT + 1, e.getMessage());
                    try {
                        Thread.sleep(REQUEST_INTERVAL_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        logger.warn("重试等待被中断", ie);
                        return null;
                    }
                } else {
                    logger.debug("地点搜索API调用失败（已重试{}次）: {}", MAX_RETRY_COUNT + 1, e.getMessage());
                }
            }
        }
        return null;
    }
    
    /**
     * 读取HTTP响应内容
     * 
     * @param conn HTTP连接
     * @return 响应内容字符串，如果读取失败则返回null
     */
    private static String readResponse(HttpURLConnection conn) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (Exception e) {
            logger.debug("读取HTTP响应失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 解析地理编码API响应
     * 
     * @param responseBody 响应内容JSON字符串
     * @return 经纬度数组 [经度, 纬度]，如果解析失败则返回null
     */
    private static double[] parseGeocodeResponse(String responseBody) {
        try {
            JSONObject json = new JSONObject(responseBody);
            if (!"1".equals(json.optString("status"))) {
                logger.debug("地理编码API返回失败状态: {}", json.optString("info"));
                return null;
            }
            
            int count = json.optInt("count", 0);
            if (count == 0) {
                logger.debug("地理编码API未找到结果");
                return null;
            }
            
            JSONArray geocodes = json.getJSONArray("geocodes");
            if (geocodes.length() == 0) {
                return null;
            }
            
            JSONObject geocode = geocodes.getJSONObject(0);
            String location = geocode.optString("location");
            return parseLocationString(location);
        } catch (Exception e) {
            logger.debug("解析地理编码API响应失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 解析地点搜索API响应
     * 
     * @param responseBody 响应内容JSON字符串
     * @return 经纬度数组 [经度, 纬度]，如果解析失败则返回null
     */
    private static double[] parsePlaceSearchResponse(String responseBody) {
        try {
            JSONObject json = new JSONObject(responseBody);
            if (!"1".equals(json.optString("status"))) {
                logger.debug("地点搜索API返回失败状态: {}", json.optString("info"));
                return null;
            }
            
            int count = json.optInt("count", 0);
            if (count == 0) {
                logger.debug("地点搜索API未找到结果");
                return null;
            }
            
            JSONArray pois = json.getJSONArray("pois");
            if (pois.length() == 0) {
                return null;
            }
            
            JSONObject poi = pois.getJSONObject(0);
            String location = poi.optString("location");
            return parseLocationString(location);
        } catch (Exception e) {
            logger.debug("解析地点搜索API响应失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 解析位置字符串（格式：经度,纬度）
     * 
     * @param location 位置字符串，格式为"经度,纬度"
     * @return 经纬度数组 [经度, 纬度]，如果解析失败则返回null
     */
    private static double[] parseLocationString(String location) {
        if (location == null || location.trim().isEmpty()) {
            return null;
        }
        
        String[] parts = location.split(",");
        if (parts.length != 2) {
            logger.debug("位置字符串格式错误: {}", location);
            return null;
        }
        
        try {
            double lng = Double.parseDouble(parts[0].trim());
            double lat = Double.parseDouble(parts[1].trim());
            return new double[]{lng, lat};
        } catch (NumberFormatException e) {
            logger.debug("解析位置坐标失败: {}, 错误: {}", location, e.getMessage());
            return null;
        }
    }
    
    /**
     * 清空地址缓存
     * 用于测试或需要强制刷新缓存的场景
     */
    public static void clearCache() {
        ADDRESS_CACHE.clear();
        logger.info("地址缓存已清空");
    }
    
    /**
     * 获取当前缓存大小
     * 
     * @return 缓存中的地址数量
     */
    public static int getCacheSize() {
        return ADDRESS_CACHE.size();
    }
}

