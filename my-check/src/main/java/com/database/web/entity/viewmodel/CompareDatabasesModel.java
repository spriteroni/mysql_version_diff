package com.database.web.entity.viewmodel;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


/**
 * (CompareDatabasesModel) VIEW MODEL
 *
 * @author roni chang
 * @since 2024-01-08 11:10:00
 */
public class CompareDatabasesModel  {
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
}

