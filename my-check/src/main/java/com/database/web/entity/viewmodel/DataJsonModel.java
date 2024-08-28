package com.database.web.entity.viewmodel;

import lombok.Getter;
import lombok.Setter;


/**
 * (DataJson)VIEW MODEL
 *
 * @author roni chang
 * @since 2024-01-08 11:10:00
 */
@Setter
@Getter
public class DataJsonModel {
    private UserDatabasesModel sourceDB;
    private UserDatabasesModel targetDB;
}

