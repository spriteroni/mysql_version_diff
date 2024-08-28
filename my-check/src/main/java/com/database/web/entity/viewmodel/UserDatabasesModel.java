package com.database.web.entity.viewmodel;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


/**
 * (UserDatabasesModel)VIEW MODEL
 *
 * @author roni chang
 * @since 2024-01-08 11:10:00
 */
public class UserDatabasesModel {
    @Getter
    @Setter
    private List<String> dbNames;
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

