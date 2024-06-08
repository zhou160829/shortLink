package com.zhou.shortlink.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhou.shortlink.enums.DeleteFlag;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 
 * @TableName link_today
 */
@TableName(value ="link_today")
@Data
@Builder
@Accessors(chain = true)
public class LinkToday implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 短链接
     */
    private String fullShortUrl;

    /**
     * 日期
     */
    private LocalDate date;

    /**
     * 今日PV
     */
    private Long todayPv;

    /**
     * 今日UV
     */
    private Long todayUv;

    /**
     * 今日IP数
     */
    private Long todayUip;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

    /**
     * 修改时间
     */
    private DeleteFlag delFlag;


}