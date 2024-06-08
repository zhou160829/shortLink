package com.zhou.shortlink.result;

import com.zhou.shortlink.util.FoxUtil;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class R extends LinkedHashMap<String, Object> implements Serializable {

    // 序列化版本号
    @Serial
    private static final long serialVersionUID = 1L;

    // 预定的状态码
    public static final int CODE_SUCCESS = 200;
    public static final int CODE_ERROR = 500;

    /**
     * 构建
     */
    public R() {
    }

    /**
     * 构建
     *
     * @param code 状态码
     * @param msg  信息
     * @param data 数据
     */
    public R(int code, String msg, Object data) {
        this.setCode(code);
        this.setMsg(msg);
        this.setData(data);
    }

    /**
     * 根据 Map 快速构建
     *
     * @param map /
     */
    public R(Map<String, ?> map) {
        this.setMap(map);
    }

    /**
     * 获取code
     *
     * @return code
     */
    public Integer getCode() {
        return (Integer) this.get("code");
    }

    /**
     * 获取msg
     *
     * @return msg
     */
    public String getMsg() {
        return (String) this.get("msg");
    }

    /**
     * 获取data
     *
     * @return data
     */
    public Object getData() {
        return this.get("data");
    }

    /**
     * 给code赋值，连缀风格
     *
     * @param code code
     * @return 对象自身
     */
    public R setCode(int code) {
        this.put("code", code);
        return this;
    }

    /**
     * 给msg赋值，连缀风格
     *
     * @param msg msg
     * @return 对象自身
     */
    public R setMsg(String msg) {
        this.put("msg", msg);
        return this;
    }

    /**
     * 给data赋值，连缀风格
     *
     * @param data data
     * @return 对象自身
     */
    public R setData(Object data) {
        this.put("data", data);
        return this;
    }

    /**
     * 写入一个值 自定义key, 连缀风格
     *
     * @param key  key
     * @param data data
     * @return 对象自身
     */
    public R set(String key, Object data) {
        this.put(key, data);
        return this;
    }

    /**
     * 获取一个值 根据自定义key
     *
     * @param <T> 要转换为的类型
     * @param key key
     * @param cs  要转换为的类型
     * @return 值
     */
    public <T> T get(String key, Class<T> cs) {
        return FoxUtil.getValueByType(get(key), cs);
    }

    /**
     * 写入一个Map, 连缀风格
     *
     * @param map map
     * @return 对象自身
     */
    public R setMap(Map<String, ?> map) {
        for (String key : map.keySet()) {
            this.put(key, map.get(key));
        }
        return this;
    }


    // ============================  静态方法快速构建  ==================================

    // 构建成功
    public static R ok() {
        return new R(CODE_SUCCESS, "ok", null);
    }

    public static R ok(String msg) {
        return new R(CODE_SUCCESS, msg, null);
    }

    public static R code(int code) {
        return new R(code, null, null);
    }

    public static R data(Object data) {
        return new R(CODE_SUCCESS, "ok", data);
    }

    // 构建失败
    public static R error() {
        return new R(CODE_ERROR, "error", null);
    }

    public static R error(String msg) {
        return new R(CODE_ERROR, msg, null);
    }

    // 构建指定状态码
    public static R get(int code, String msg, Object data) {
        return new R(code, msg, data);
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "{"
                + "\"code\": " + this.getCode()
                + ", \"msg\": " + transValue(this.getMsg())
                + ", \"data\": " + transValue(this.getData())
                + "}";
    }

    /**
     * 转换 value 值：
     * 如果 value 值属于 String 类型，则在前后补上引号
     * 如果 value 值属于其它类型，则原样返回
     *
     * @param value 具体要操作的值
     * @return 转换后的值
     */
    private String transValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return "\"" + value + "\"";
        }
        return String.valueOf(value);
    }
}

