package com.zhou.shortlink.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum ValiDateStatus implements BaseEnum{
    FOREVER(0, "永久有效"),
    CUSTOM(1, "自定义"),
    ;
    private final int value;
    private final String desc;

    public static ValiDateStatus of(Integer value) {
        if (value == null) {
            return null;
        }
        for (ValiDateStatus commonStatus : values()) {
            if (commonStatus.getValue() == value) {
                return commonStatus;
            }
        }
        return null;
    }

    public static String desc(Integer value) {
        ValiDateStatus status = of(value);
        return status.getDesc();
    }
}