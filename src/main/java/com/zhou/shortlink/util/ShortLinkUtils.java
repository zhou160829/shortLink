package com.zhou.shortlink.util;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.codec.digest.DigestUtils;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;

public class ShortLinkUtils {

    public static final String[] chars = new String[]{"a", "b", "c", "d", "e", "f", "g", "h",
            "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t",
            "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H",
            "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
            "U", "V", "W", "X", "Y", "Z"};


    /**
     * 一个长链接URL转换为4个短KEY
     * 思路：
     * 1)将长网址md5生成32位签名串,分为4段, 每段8个字节;
     * 2)对这四段循环处理, 取8个字节, 将他看成16进制串与0x3fffffff(30位1)与操作, 即超过30位的忽略处理;
     * 3)这30位分成6段, 每5位的数字作为字母表的索引取得特定字符, 依次进行获得6位字符串;
     * 4)总的md5串可以获得4个6位串; 取里面的任意一个就可作为这个长url的短url地址;
     * 当我们点击这6个字母的链接后，我们又可以跳转到原始的真实链接地址。
     */
    public static String shortUrl(String url) {
        String md5Hex = DigestUtils.md5Hex(url);

        String hex = md5Hex;

        String[] resUrl = new String[4];

        for (int i = 0; i < 4; i++) {
            String sTempSubString = hex.substring(i * 8, i * 8 + 8);

            // 与 全1为1，计算结果还是原来的值
            long lHexLong = 0x3FFFFFFF & Long.parseLong(sTempSubString, 16);

            StringBuilder outChars = new StringBuilder();
            for (int j = 0; j < 6; j++) {
                long index = 0x0000003D & lHexLong;

                // 按照下标index把从chars数组取得的字符逐个相加
                outChars.append(chars[(int) index]);
                //每次循环按位移5位，因为30位的二进制，分6次循环，即每次右移5位
                lHexLong = lHexLong >> 5;
            }
            // 把字符串存入对应索引的输出数组,会产生一组6位字符串
            resUrl[i] = outChars.toString();
        }

        return resUrl[new Random().nextInt(4)];
    }

    //说明此工具类下面方法来自于https://gitee.com/nageoffer/shortlink/blob/main/project/src/main/java/com/nageoffer/shortlink/project/toolkit/LinkUtil.java

    /**
     * 获取短链接缓存有效期时间
     *
     * @param validDate 有效期时间
     * @return 有限期时间戳
     */
    public static long getLinkCacheValidTime(LocalDateTime validDate) {
        if (validDate == null) {
            return -1L;
        } else {
            Duration between = LocalDateTimeUtil.between(LocalDateTime.now(), validDate);
            return between.toSeconds();
        }
    }

    /**
     * 获取用户真实IP
     *
     * @param request 请求
     * @return 用户真实IP
     */
    public static String getActualIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

    /**
     * 获取用户访问操作系统
     *
     * @param request 请求
     * @return 访问操作系统
     */
    public static String getOs(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent.toLowerCase().contains("windows")) {
            return "Windows";
        } else if (userAgent.toLowerCase().contains("mac")) {
            return "Mac OS";
        } else if (userAgent.toLowerCase().contains("linux")) {
            return "Linux";
        } else if (userAgent.toLowerCase().contains("android")) {
            return "Android";
        } else if (userAgent.toLowerCase().contains("iphone") || userAgent.toLowerCase().contains("ipad")) {
            return "iOS";
        } else {
            return "Unknown";
        }
    }

    /**
     * 获取用户访问浏览器
     *
     * @param request 请求
     * @return 访问浏览器
     */
    public static String getBrowser(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent.toLowerCase().contains("edg")) {
            return "Microsoft Edge";
        } else if (userAgent.toLowerCase().contains("chrome")) {
            return "Google Chrome";
        } else if (userAgent.toLowerCase().contains("firefox")) {
            return "Mozilla Firefox";
        } else if (userAgent.toLowerCase().contains("safari")) {
            return "Apple Safari";
        } else if (userAgent.toLowerCase().contains("opera")) {
            return "Opera";
        } else if (userAgent.toLowerCase().contains("msie") || userAgent.toLowerCase().contains("trident")) {
            return "Internet Explorer";
        } else {
            return "Unknown";
        }
    }

    /**
     * 获取用户访问设备
     *
     * @param request 请求
     * @return 访问设备
     */
    public static String getDevice(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent.toLowerCase().contains("mobile")) {
            return "Mobile";
        }
        return "PC";
    }

    /**
     * 获取用户访问网络
     *
     * @param request 请求
     * @return 访问设备
     */
    public static String getNetwork(HttpServletRequest request) {
        String actualIp = getActualIp(request);
        // 这里简单判断IP地址范围，您可能需要更复杂的逻辑
        // 例如，通过调用IP地址库或调用第三方服务来判断网络类型
        return actualIp.startsWith("192.168.") || actualIp.startsWith("10.") ? "WIFI" : "Mobile";
    }

    /**
     * 获取原始链接中的域名
     * 如果原始链接包含 www 开头的话需要去掉
     *
     * @param url 创建或者修改短链接的原始链接
     * @return 原始链接中的域名
     */
    public static String extractDomain(String url) {
        String domain = null;
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (StrUtil.isNotBlank(host)) {
                domain = host;
                if (domain.startsWith("www.")) {
                    domain = host.substring(4);
                }
            }
        } catch (Exception ignored) {
        }
        return domain;
    }
}

