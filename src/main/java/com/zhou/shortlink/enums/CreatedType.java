package com.zhou.shortlink.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CreatedType implements BaseEnum {
    INTERFACE(0, "接口创建"), CONSOLE(1, "控制台创建"),
    ;
    private final int value;
    private final String desc;

    public static CreatedType of(Integer value) {
        if (value == null) {
            return null;
        }
        for (CreatedType commonStatus : values()) {
            if (commonStatus.getValue() == value) {
                return commonStatus;
            }
        }
        return null;
    }

    public static String desc(Integer value) {
        CreatedType status = of(value);
        return status.getDesc();
    }
}