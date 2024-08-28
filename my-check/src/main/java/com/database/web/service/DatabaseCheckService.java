package com.database.web.service;

import com.database.web.entity.viewmodel.DBMessage;
import com.database.web.entity.viewmodel.UserDatabasesModel;

import java.sql.*;
import java.util.List;


/**
 * (DatabaseCheckService)服务接口
 *
 * @author roni chang
 * @since 2024-01-08 11:10:03
 */
public interface DatabaseCheckService {


    /**
     * 获取Mysql版本
     * @param UserDatabasesModel
     * @return
     */
    public DBMessage checkMysqlVersion(UserDatabasesModel UserDatabasesModel);

    /**
     * 比较版本
     * @param sourceVersion
     * @param targetVersion
     * @return
     */
    public DBMessage compareMysqlVersion(String sourceVersion,String targetVersion);

    /**
     * 获取除系统数据库外所有数据库
     * @param UserDatabasesModel
     * @return
     */
    public DBMessage getDatabases(UserDatabasesModel UserDatabasesModel);


    /**
     * 源数据源和目标数据源检查	检测源数据库与目标数据库的所有项
     * @param sourceDB
     * @param targetDB
     * @return
     */
    public List<DBMessage> checkInAll(UserDatabasesModel sourceDB, UserDatabasesModel targetDB);

    /**
     * 源数据源和目标数据源时区检查	检测源数据库与目标数据库的时区是否一致
     * @param sourceStmt
     * @param targetStmt
     * @return
     */
    public DBMessage checkTimeZone(Statement sourceStmt, Statement targetStmt) throws SQLException;


    /**
     * 检查源数据库的账号权限是否满足要求
     * @param sourceStmt
     * @return
     */
    public DBMessage checkSourceAuthority(Statement sourceStmt) throws SQLException;

    /**
     * 检查表有无主键。
     * 检查没有和MySQL 5.6、5.7版本的系统数据库、MySQL 8.0版本新增的INNODB_开头的词典表名冲突。
     * @param sourceMeta
     * @param dbNames
     * @return
     */
    public DBMessage checkSourceTable(DatabaseMetaData sourceMeta,List<String> dbNames) throws SQLException;

    /**
     * 检查表和存储过程单个ENUM或SET列元素的长度不超过255个字符或1020个字节
     * @param sourceStmt
     * @return
     */
    public DBMessage checkSourceTableAndProcess(Statement sourceStmt) throws SQLException;

    /**
     * TARGET_TRIGGER("7","检测目标数据库的触发器是否存在"),
     *  @param sourceStmt
     *  @param targetStmt
     * @return
     */
    public DBMessage checkTargetTrigger(Statement sourceStmt,Statement targetStmt) throws SQLException;

    /**
     *  CHECK_VERSION("8","检测源数据库与目标数据库的版本是否兼容"),
     * @param sourceVersion
     * @param targetVersion
     * @return
     */
    public DBMessage checkVersionMigration(String sourceVersion,String targetVersion);


    /**
     * CHECK_CHARACTOR_SET("9","检测源数据库与目标数据库的字符集是否兼容")
     * @param sourceStmt
     * @param targetStmt
     * @return
     */
    public DBMessage checkCharacterSet(Statement sourceStmt, Statement targetStmt) throws SQLException;

    /**
     * CHECK_ORDER_RULE("10","检测源数据库与目标数据库的排序规则是否兼容")
     * @param sourceStmt
     * @param targetStmt
     * @return
     */
    public DBMessage checkOrderRule(Statement sourceStmt, Statement targetStmt) throws SQLException;

    /**
     * CHECK_SQL_MODE("11","检测源数据库与目标数据库的 SQL_MODE 是否兼容"),
     * @param sourceStmt
     * @param targetStmt
     * @return
     */
    public DBMessage checkSqlMode(Statement sourceStmt, Statement targetStmt) throws SQLException;

    /**
     * UPPER_LOWER_CASE_PARAMETER("12","检测源数据库与目标数据库的大小写敏感参数配置是否兼容")
     * @param sourceStmt
     * @param targetStmt
     * @return
     */
    public DBMessage checkUperAndLowerParameter(Statement sourceStmt, Statement targetStmt) throws SQLException;


    /**
     * EXPLICIT_DEFAULTS_FOR_TIMESTAMP("13","检测源数据库与目标数据库的 explicit_defaults_for_timestamp 是否兼容"),
     * @param sourceStmt
     * @param targetStmt
     * @return
     */
    public DBMessage checkExplicitDefaultsForTimestamp(Statement sourceStmt, Statement targetStmt) throws SQLException;

    /**
     * CHECK_FUNCTION("14","检查源数据库获取存储过程和函数的定义是否已授予权限")
     * @param sourceStmt
     * @return
     */
    public DBMessage checkSourceFunction(Statement sourceStmt) throws SQLException;

    /**
     * DEFINER_EXISTS("15","检查 View，Trigger，Procedure，Function，Event 的 Definer 在目标库是否存在")
     * @param sourceConn
     * @param targetConn
     * @return
     */
    public DBMessage checkDefiner(Connection sourceConn, Connection targetConn,List<String> dbNames) throws SQLException;

    /**
     * 目标库数据存在性检查
     * TARGET_EXISTS_DATA("16","检查待复制对象在目标数据库中是否已存在数据")
     * @param targetConn
     * @param dbNames
     * @return
     */
    public DBMessage checkDataExists(Connection targetConn,List<String> dbNames) throws SQLException;


    /**
     * 目标库同名对象存在性检查
     * TARGET_EXISTS("17","检查待复制对象在目标数据库中是否已存在")
     * @param sourceStmt
     * @param targetConn
     * @param dbNames
     * @return
     */
    public DBMessage checkTargetExists(Statement sourceStmt, Connection targetConn, List<String> dbNames) throws SQLException;

    /**
     * 分区表检查
     * PARTITION_CHECK("18","检查源数据库确保只有InnoDB引擎的分区表，检查表的分区不在共享InnoDB tablespaces表空间中")
     * @param sourceStmt
     * @return
     */
    public DBMessage checkTablePartition(Statement sourceStmt) throws SQLException;

    /**
     * 全局参数检查
     * WHOLE_PARAMETER_FIT("19","检查源数据源和目标数据源全局参数中的默认值一致性")
     * @param sourceStmt
     * @param targetStmt
     * @return
     */
    public DBMessage checkWholeParameterFit(Statement sourceStmt, Statement targetStmt) throws SQLException;

    /**
     * 全局参数检查
     * WHOLE_PARAMETER_FIT("20","目标数据源关键字检查")
     * @param sourceStmt
     * @param targetStmt
     * @return
     */
    public DBMessage keywordsCheck(Statement sourceStmt, Statement targetStmt,List<String> dbName,String targetVersion) throws SQLException;
    /**
     * 全局参数检查
     * VALIDATE_PASSWORD_CHECK("20","源数据源和目标数据源密码规则检查")
     * @param sourceStmt
     * @param targetStmt
     * @return
     */
    public DBMessage validatePasswordCheck(Statement sourceStmt, Statement targetStmt, String sourceUserName,String targetUserName) throws SQLException;

}
