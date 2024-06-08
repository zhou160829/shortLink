package com.zhou.shortlink.constant;

public class RedisConstants {


    /**
     * 自动识别json对象白名单配置（仅允许解析的包名，范围越小越安全）
     */
    public static final String[] JSON_WHITELIST_STR = {"org.springframework", "com.zhou"};


    /**
     *
     */
    public static final String USER_INFO_KEY = "user_info";


    public static final String USER_INFO_KEY_LOGIN_COUNT = "3";


    public static final String SHORT_URL_KEY = "short:url";

    public static final String SHORT_NULL_URL_KEY = "short:url:null";

    public static final String SHORT_URL_KEY_LOCK = "short:url:lock";

    public static final String SHORT_URL_KEY_SCORE = "short:url:score";
}
