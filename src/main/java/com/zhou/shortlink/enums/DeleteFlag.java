package com.zhou.shortlink.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DeleteFlag implements BaseEnum {
    NO_DELETE(0, "未删除"), DELETE(1, "已删除"),
    ;
    @JsonValue
    @EnumValue
    private final int value;
    private final String desc;

    public static DeleteFlag of(Integer value) {
        if (value == null) {
            return null;
        }
        for (DeleteFlag commonStatus : values()) {
            if (commonStatus.getValue() == value) {
                return commonStatus;
            }
        }
        return null;
    }

    public static String desc(Integer value) {
        DeleteFlag status = of(value);
        return status.getDesc();
    }
}