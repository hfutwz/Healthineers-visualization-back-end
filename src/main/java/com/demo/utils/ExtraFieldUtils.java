package com.demo.utils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;

public class ExtraFieldUtils {
    private static final String API_KEY = "";
    /**
     * 获取季节
     * @param date
     * @return
     */
    public static int getSeason(LocalDate date) {
        int month = date.getMonthValue();
        if (month >= 3 && month <= 5) {
            return 0; // 春
        } else if (month >= 6 && month <= 8) {
            return 1; // 夏
        } else if (month >= 9 && month <= 11) {
            return 2; // 秋
        } else {
            return 3; // 冬
        }
    }

    /**
     * 获取时间段
     * @param admissionTime
     * @return
     */
    public static Integer getTimePeriod(String admissionTime) {
        if (admissionTime == null || admissionTime.trim().length() != 4) {
            return null; // 不满足4位，自动跳过
        }
        try {
            int time = Integer.parseInt(admissionTime);
            int hour = time / 100;
            // 根据题意定义时间段
            if (hour >= 19 || hour < 7) {
                return 0; // 夜间19:00-7:00
            } else if (hour >= 7 && hour < 9) {
                return 1; // 早高峰7:00-9:00
            } else if (hour >= 9 && hour < 11) {
                return 2; // 上午9:00-11:00
            } else if (hour >= 11 && hour < 13) {
                return 3; // 午高峰11:00-13:00
            } else if (hour >= 13 && hour < 17) {
                return 4; // 下午13:00-17:00
            } else if (hour >= 17 && hour < 19) {
                return 5; // 晚高峰17:00-19:00
            }
        } catch (NumberFormatException e) {
            // 转化异常，跳过
            return null;
        }
        return null;
    }
    /**
     * 根据地址调用高德地图API获取经纬度
     * @param address 地址字符串("上海市"+address)
     * @return double数组[经度, 纬度]，失败返回null
     */
    public static double[] getLngLatFromAddress(String address) {
        try {
            String fullAddress = "上海市" + address; // 拼接
            String urlStr = "https://restapi.amap.com/v3/geocode/geo"
                    + "?address=" + java.net.URLEncoder.encode(fullAddress, "UTF-8")
                    + "&key=" + API_KEY;
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                return null;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            // 解析返回json
            JSONObject json = new JSONObject(sb.toString());
            if ("1".equals(json.getString("status")) && Integer.parseInt(json.getString("count")) > 0) {
                JSONObject geocode = json.getJSONArray("geocodes").getJSONObject(0);
                String location = geocode.getString("location");
                String[] parts = location.split(",");
                double lng = Double.parseDouble(parts[0]);
                double lat = Double.parseDouble(parts[1]);
                return new double[]{lng, lat};
            } else {
                return null; // 未找到或失败
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null; // 异常也返回null
        }
    }

    /**
     * 判断地址是否有效
     * @param addr
     * @return
     */
    public static boolean isValidAddress(String addr) {
        if (addr == null || addr.trim().isEmpty()) return false;
        String trimmed = addr.trim();
        // 过滤无效关键词
        String[] invalids = { "(跳过)", "*", "无", "家", "家中", "家门口", "住所", "小区", "居民楼", "别墅", "家庭", "nan", "" };
        for (String invalid : invalids) {
            if (trimmed.contains(invalid)) return false;
        }
        return true;
    }

    /**
     * 经纬度是否有效
     * @param lng
     * @param lat
     * @return
     */
    public static boolean isValidLngLat(double lng, double lat) {
        // 经度有效范围：-180 ~ 180
        // 纬度有效范围：-90 ~ 90
        return lng >= -180 && lng <= 180 && lat >= -90 && lat <= 90;
    }

    // 测试调用
    public static void main(String[] args) {
        String address = "11号线上海西站";
        double[] lngLat = getLngLatFromAddress(address);
        if (lngLat != null) {
            System.out.println("经度：" + lngLat[0] + "，纬度：" + lngLat[1]);
        } else {
            System.out.println("获取经纬度失败");
        }
    }
}
