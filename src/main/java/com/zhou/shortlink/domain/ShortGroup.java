package com.zhou.shortlink.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import com.zhou.shortlink.enums.DeleteFlag;
import lombok.Data;

/**
 * 
 * @TableName short_group
 */
@TableName(value ="short_group")
@Data
public class ShortGroup implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     * 分组id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 分组名
     */
    private String name;

    /**
     * 创建人id
     */
    private Long createUserId;

    /**
     * 创建人用户名
     */
    private String createUserName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

    /**
     * 分组排序
     */
    private Integer sortOrder;

    /**
     * 删除标识 0：未删除 1：已删除
     */
    private DeleteFlag delFlag;


}