package com.zhou.shortlink.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EnableStatus implements BaseEnum {
    ENABLE(0, "启用"),
    DISABLE(1, "禁用"),
    ;
    @JsonValue
    @EnumValue
    private final int value;
    private final String desc;

    public static EnableStatus of(Integer value) {
        if (value == null) {
            return null;
        }
        for (EnableStatus commonStatus : values()) {
            if (commonStatus.getValue() == value) {
                return commonStatus;
            }
        }
        return null;
    }

    public static String desc(Integer value) {
        EnableStatus status = of(value);
        return status.getDesc();
    }
}