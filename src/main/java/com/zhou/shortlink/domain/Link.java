package com.zhou.shortlink.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhou.shortlink.enums.CreatedType;
import com.zhou.shortlink.enums.DeleteFlag;
import com.zhou.shortlink.enums.EnableStatus;
import com.zhou.shortlink.enums.ValiDateStatus;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @TableName link
 */
@TableName(value = "link")
@Data
@Accessors(chain = true)
public class Link implements Serializable {

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 域名
     */
    private String domain;

    /**
     * 短链接
     */
    private String shortUri;

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 原始链接
     */
    private String originUrl;

    /**
     * 分组ID
     */
    private Integer groupId;

    /**
     * 网站图标
     */
    private String favicon;

    /**
     * 启用标识 0：启用 1：未启用
     */
    private EnableStatus enableStatus;

    /**
     * 创建类型 0：接口创建 1：控制台创建
     */
    private CreatedType createdType;

    /**
     * 有效期类型 0：永久有效 1：自定义
     */
    private ValiDateStatus validDateType;

    /**
     * 有效期
     */
    private LocalDateTime validDate;

    /**
     * 描述
     */
    @TableField("`describe`")
    private String describe;

    /**
     * 历史PV
     */
    private Integer totalPv;

    /**
     * 历史UV
     */
    private Integer totalUv;

    /**
     * 历史UIP
     */
    private Integer totalUip;

    /**
     * 创建人id
     */
    private Long createUserId;

    /**
     * 创建人姓名
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
     * 删除时间戳
     */
    private Long delTime;

    /**
     * 删除标识 0：未删除 1：已删除
     */
    private DeleteFlag delFlag;

}