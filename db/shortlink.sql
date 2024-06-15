create table if not exists link
(
    id               bigint auto_increment comment 'ID'
    primary key,
    domain           varchar(128)                null comment '域名',
    short_uri        varchar(8) collate utf8_bin null comment '短链接',
    full_short_url   varchar(128)                null comment '完整短链接',
    origin_url       varchar(1024)               null comment '原始链接',
    group_id         int                         null comment '分组ID',
    favicon          varchar(256)                null comment '网站图标',
    enable_status    tinyint(1)                  null comment '启用标识 0：启用 1：未启用',
    created_type     tinyint(1) default 1        null comment '创建类型 0：接口创建 1：控制台创建',
    valid_date_type  tinyint(1)                  null comment '有效期类型 0：永久有效 1：自定义',
    valid_date       datetime                    null comment '有效期',
    `describe`       varchar(1024)               null comment '描述',
    total_pv         int                         null comment '历史PV',
    total_uv         int                         null comment '历史UV',
    total_uip        int                         null comment '历史UIP',
    create_user_id   int                         null comment '创建人id',
    create_user_name varchar(10)                 null comment '创建人姓名',
    create_time      datetime                    null comment '创建时间',
    update_time      datetime                    null comment '修改时间',
    del_time         bigint     default 0        null comment '删除时间戳',
    del_flag         tinyint(1) default 0        null comment '删除标识 0：未删除 1：已删除',
    constraint `idx_unique_full-short-url`
    unique (full_short_url, del_time)
    );

create table if not exists link_logs
(
    id             bigint auto_increment comment 'ID'
    primary key,
    full_short_url varchar(255) null comment '完整短链接',
    user_id        bigint       null comment '用户id',
    ip             varchar(64)  null comment 'IP',
    browser        varchar(64)  null comment '浏览器',
    os             varchar(64)  null comment '操作系统',
    network        varchar(64)  null comment '访问网络',
    device         varchar(64)  null comment '访问设备',
    create_time    datetime     null comment '创建时间',
    update_time    datetime     null comment '修改时间',
    del_flag       tinyint(1)   null comment '0-未删除,1-已删除'
    );

create index idx_full_short_url
    on link_logs (full_short_url);

create table if not exists link_today
(
    id             bigint auto_increment comment 'ID'
    primary key,
    full_short_url varchar(128)  null comment '短链接',
    date           date          null comment '日期',
    today_pv       int default 0 null comment '今日PV',
    today_uv       int default 0 null comment '今日UV',
    today_uip      int default 0 null comment '今日IP数',
    create_time    datetime      null comment '创建时间',
    update_time    datetime      null comment '修改时间',
    del_flag       tinyint(1)    null,
    constraint idx_unique_today_stats
    unique (full_short_url, date)
    );

create table if not exists short_group
(
    id               int auto_increment comment '分组id'
    primary key,
    name             char(20)          null comment '分组名',
    create_user_id   int               null comment '创建人id',
    create_user_name varchar(10)       null comment '创建人用户名',
    create_time      datetime          null comment '创建时间',
    update_time      datetime          null comment '修改时间',
    sort_order       int               null comment '分组排序',
    del_flag         tinyint default 0 null comment '删除标识 0：未删除 1：已删除'
    );

create table if not exists user
(
    id            bigint auto_increment comment 'ID'
    primary key,
    username      char(20)     null comment '用户名',
    password      varchar(512) null comment '密码',
    real_name     varchar(256) null comment '真实姓名',
    phone         varchar(128) null comment '手机号',
    mail          varchar(512) null comment '邮箱',
    deletion_time bigint       null comment '注销时间戳',
    create_time   datetime     null comment '创建时间',
    update_time   datetime     null comment '修改时间',
    del_flag      tinyint(1)   null comment '删除标识 0：未删除 1：已删除',
    constraint idx_unique_username
    unique (username)
    );

