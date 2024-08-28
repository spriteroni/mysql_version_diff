package com.database.web.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.io.Serializable;



/**
 * (UserDatabases)实体类
 *
 * @author roni chang
 * @since 2024-01-08 11:10:00
 */
public class UserDatabases implements Serializable {
    private static final long serialVersionUID = 165455168880452392L;

    @Getter
    @Setter
    private Integer id;
    @Getter
    @Setter
    private String dbName;
    @Getter
    @Setter
    private String userName;
    @Getter
    @Setter
    private String host;
    @Getter
    @Setter
    private Integer port;
    @Getter
    @Setter
    private String pwd;
    @Getter
    @Setter
    private String dbType;
    @Getter
    @Setter
    private String dbVersion;
    @Getter
    @Setter
    private String dbStatus;
    @Getter
    @Setter
    private Date createTime;
    @Getter
    @Setter
    private Date updateTime;
    @Getter
    @Setter
    private Integer salt;
}

