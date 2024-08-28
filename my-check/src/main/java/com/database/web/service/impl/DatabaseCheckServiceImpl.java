package com.database.web.service.impl;

import com.database.web.entity.viewmodel.DBKVModel;
import com.database.web.entity.viewmodel.DBMessage;
import com.database.common.enums.VersionCompareType;
import com.database.common.utils.StringUtils;
import com.database.common.utils.dbtool.JDBCUtil;
import com.database.web.entity.viewmodel.UserDatabasesModel;
import com.database.web.service.DatabaseCheckService;
import org.springframework.stereotype.Service;
import com.database.common.enums.CheckItemKey;

import java.sql.*;
import java.util.*;


@Service("databaseCheckService")
public class DatabaseCheckServiceImpl implements DatabaseCheckService {

    /**
     * @param sourceDB
     * @return
     */
    @Override
    public DBMessage checkMysqlVersion(UserDatabasesModel sourceDB) {
        JDBCUtil.SourceHost = "jdbc:mysql://"+sourceDB.getHost()+":"+sourceDB.getPort();
        JDBCUtil.SourceUsername= sourceDB.getUserName();
        JDBCUtil.SourcePassword= sourceDB.getPwd();
        String version = "";
        DBMessage dbMessage = new DBMessage();
        try (Connection conn = JDBCUtil.getSourceConnection()) {
            if (conn!= null) {
                Statement stmt = conn.createStatement();
                //检查Mysql版本
                ResultSet rs = stmt.executeQuery("select version()");
                dbMessage.setSqlState("000000");
                dbMessage.setMessage("connect to datebase success");
                while (rs.next()) {
                    dbMessage.setDbVersion(rs.getString(1));
                    dbMessage.setDbType("MySQL");
                }
                //检查PolarDB版本
                rs = stmt.executeQuery("show variables like 'polardb_version'");
                while (rs.next()) {
                    version = rs.getString(2);
                    if (StringUtils.isNotEmpty(version)) {
                        dbMessage.setDbType("PolarDB MySQL");
                    }
                }
                JDBCUtil.close(conn,stmt,rs);
            } else {
                dbMessage.setSqlState("000001");
                dbMessage.setMessage("connection is null");
            }

        } catch (SQLException e) {
            dbMessage.setSqlState(e.getSQLState());
            dbMessage.setMessage(e.getMessage());
            dbMessage.setVendorCode(e.getErrorCode());

        }
        return dbMessage;
    }

    /**
     * @param sourceVersion
     * @param targetVersion
     * @return
     */
    @Override
    public DBMessage compareMysqlVersion(String sourceVersion, String targetVersion) {
        DBMessage dbMessage = new DBMessage("000000","",CheckItemKey.CHECK_VERSION);
        if(StringUtils.isNotEmpty(sourceVersion)&&StringUtils.isNotEmpty(targetVersion)){
            sourceVersion=StringUtils.replace(sourceVersion, "-log", "");
            targetVersion=StringUtils.replace(targetVersion, "-log", "");
            String compared = VersionCompareType.compareVersion(sourceVersion, targetVersion);
            dbMessage.setSqlState("000000");
            dbMessage.setMessage(compared);
        }else{
            dbMessage.setSqlState("000001");
            dbMessage.setMessage("Invalid version number");
        }

        return dbMessage;
    }

    /**
     * @param sourceDB
     * @return
     */
    @Override
    public DBMessage getDatabases(UserDatabasesModel sourceDB) {
        JDBCUtil.SourceHost = "jdbc:mysql://"+sourceDB.getHost()+":"+sourceDB.getPort();
        JDBCUtil.SourceUsername= sourceDB.getUserName();
        JDBCUtil.SourcePassword= sourceDB.getPwd();
        List<String> items = new ArrayList<>();
        DBMessage dbMessage = new DBMessage();
        try (Connection conn = JDBCUtil.getSourceConnection()) {
            if (conn!= null) {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME NOT IN('information_schema','performance_schema','mysql','__recycle_bin__','sys')");
                dbMessage.setSqlState("000000");
                dbMessage.setMessage("数据库连接成功");
                while (rs.next()) {
                    dbMessage.setDbVersion(sourceDB.getDbVersion());
                    dbMessage.setDbType("MySQL");
                    items.add(rs.getString(1));
                }
                dbMessage.setDbItems(items);
                JDBCUtil.close(conn,stmt,rs);
            }   else {
                dbMessage.setSqlState("000001");
                dbMessage.setMessage("连接串为空");
            }
        } catch (SQLException e) {
            dbMessage.setSqlState(e.getSQLState());
            dbMessage.setMessage(e.getMessage());
            dbMessage.setVendorCode(e.getErrorCode());

        }
        return dbMessage;
    }

    /**
     * 源数据源和目标数据源检查	检测源数据库与目标数据库的所有项
     *
     * @param sourceDB
     * @param targetDB
     * @return
     */
    @Override
    public List<DBMessage> checkInAll(UserDatabasesModel sourceDB, UserDatabasesModel targetDB) {
        JDBCUtil.SourceHost = "jdbc:mysql://"+sourceDB.getHost()+":"+sourceDB.getPort();
        JDBCUtil.SourceUsername= sourceDB.getUserName();
        JDBCUtil.SourcePassword= sourceDB.getPwd();
        JDBCUtil.TargetHost = "jdbc:mysql://"+targetDB.getHost()+":"+targetDB.getPort();
        JDBCUtil.TargetUsername= targetDB.getUserName();
        JDBCUtil.TargetPassword= targetDB.getPwd();
        List<DBMessage> checkListResult = new ArrayList<>();

        try (Connection sourceConn = JDBCUtil.getSourceConnection()) {
            try (Connection targetConn = JDBCUtil.getTargetConnection()){
                if (sourceConn!= null && targetConn!= null) {
                    checkListResult.add(new DBMessage("000000","",CheckItemKey.SOURCE_CONNECTION));
                    checkListResult.add(new DBMessage("000000","",CheckItemKey.TARGET_CONNECTION));
                    Statement sourceStmt = sourceConn.createStatement();
                    Statement targetStmt = targetConn.createStatement();
                    //检查源数据库账号权限
                    DBMessage AuthorityMsg = checkSourceAuthority(sourceStmt);
                    checkListResult.add(AuthorityMsg);
    //                //触发器存在性检查
    //                DBMessage TriggerMsg = checkTargetTrigger(sourceStmt, targetStmt);
    //                checkListResult.add(TriggerMsg);
                    //非InnoDB引擎分区表检查
                    DBMessage PartitionMsg =  checkTablePartition(sourceStmt);
                    checkListResult.add(PartitionMsg);
                    //检查源数据库和目标数据库的时区是否一致
                    DBMessage TimezoneMsg = checkTimeZone(sourceStmt, targetStmt);
                    checkListResult.add(TimezoneMsg);
                    //检查源数据库和目标数据库的版本是否兼容
                    DBMessage versionMsg = checkVersionMigration(sourceDB.getDbVersion(), targetDB.getDbVersion());
                    checkListResult.add(versionMsg);
                    //检查源数据库表是否有主键
                    DBMessage primaryKeyMsg = checkSourceTable(sourceConn.getMetaData(),sourceDB.getDbNames());
                    checkListResult.add(primaryKeyMsg);
                    //检查目标数据源的数据库中库名，表名和表字段是否有关键字占用
                    DBMessage keywordsCheck = keywordsCheck(sourceStmt,targetStmt,sourceDB.getDbNames(),targetDB.getDbVersion());
                    checkListResult.add(keywordsCheck);
                    //检查源数据库和目标数据库的字符集是否兼容
                    DBMessage characterMsg = checkCharacterSet(sourceStmt,targetStmt);
                    checkListResult.add(characterMsg);
                    //检查源数据库和目标数据库的排序规则是否兼容
                    DBMessage orderbyMsg = checkOrderRule(sourceStmt,targetStmt);
                    checkListResult.add(orderbyMsg);
                    //检查源数据库和目标数据库的 SQL_MODE 是否兼容
                    DBMessage sqlModeMsg = checkSqlMode(sourceStmt,targetStmt);
                    checkListResult.add(sqlModeMsg);
                    //检查源数据库和目标数据库的大小写敏感参数配置是否兼容
                    DBMessage upperLowerCaseParameterMsg = checkUperAndLowerParameter(sourceStmt,targetStmt);
                    checkListResult.add(upperLowerCaseParameterMsg);
                    //检查源数据库和目标数据库的 explicit_defaults_for_timestamp 是否兼容
                    DBMessage explicitDefaultsForTimestampMsg = checkExplicitDefaultsForTimestamp(sourceStmt,targetStmt);
                    checkListResult.add(explicitDefaultsForTimestampMsg);
                    //检查 View，Trigger，Procedure，Function，Event 的 Definer 在目标库是否存在
                    DBMessage definerMsg = checkDefiner(sourceConn,targetConn,sourceDB.getDbNames());
                    checkListResult.add(definerMsg);
                    //检查待复制对象在目标数据库中是否已存在
                    DBMessage dataExistsMsg = checkTargetExists(sourceStmt,targetConn,sourceDB.getDbNames());
                    checkListResult.add(dataExistsMsg);
                    //检查待复制对象在目标数据库中是否已存在数据
                    DBMessage tableMsg = checkDataExists(targetConn,sourceDB.getDbNames());
                    checkListResult.add(tableMsg);
                    //检查源数据源和目标数据源全局参数中的默认值一致性
                    DBMessage wholeParameterFitMsg = checkWholeParameterFit(sourceStmt,targetStmt);
                    checkListResult.add(wholeParameterFitMsg);
                    //检查源数据源和目标数据源全局参数中的默认值一致性
                    DBMessage validatePasswordCheck = validatePasswordCheck(sourceStmt,targetStmt,sourceDB.getUserName(),targetDB.getUserName());
                    checkListResult.add(validatePasswordCheck);

                    JDBCUtil.close(sourceConn, sourceStmt, null);
                    JDBCUtil.close(sourceConn, sourceStmt, null);


                }else {
                    DBMessage dbMessage = new DBMessage();
                    dbMessage.setSqlState("000001");
                    if (sourceConn == null){
                        dbMessage.setCheckCode(CheckItemKey.SOURCE_CONNECTION.getCode());
                        dbMessage.setCheckContent(CheckItemKey.SOURCE_CONNECTION.getInfo());
                        dbMessage.setMessage("请检查源数据源网关状态、实例是否可达、用户名及密码准确性");
                    }
                    else if (targetConn == null){
                        dbMessage.setCheckCode(CheckItemKey.TARGET_CONNECTION.getCode());
                        dbMessage.setCheckContent(CheckItemKey.TARGET_CONNECTION.getInfo());
                        dbMessage.setMessage("请检查目标数据源网关状态、实例是否可达、用户名及密码准确性");
                    }
                    checkListResult.add(dbMessage);
                }


            }
            catch (SQLException e) {
                DBMessage dberror = new DBMessage();
                dberror.setSqlState(e.getSQLState());
                dberror.setMessage("请检查目标数据源网关状态、实例是否可达、用户名及密码准确性");
                dberror.setCheckCode(CheckItemKey.TARGET_CONNECTION.getCode());
                dberror.setCheckContent(CheckItemKey.TARGET_CONNECTION.getInfo());
                checkListResult.add(dberror);
            }
        } catch (SQLException e) {
            DBMessage dberror = new DBMessage();
            dberror.setMessage("请检查源数据源网关状态、实例是否可达、用户名及密码准确性");
            dberror.setCheckCode(CheckItemKey.SOURCE_CONNECTION.getCode());
            dberror.setCheckContent(CheckItemKey.SOURCE_CONNECTION.getInfo());
            dberror.setSqlState(e.getSQLState());
            checkListResult.add(dberror);
        }
        return checkListResult;
    }

    /**
     * 源数据源和目标数据源时区检查	检测源数据库与目标数据库的时区是否一致
     *
     * @param sourceStmt
     * @param targetStmt
     * @return
     */
    @Override
    public DBMessage checkTimeZone(Statement sourceStmt,Statement targetStmt) throws SQLException {
        DBMessage dbMessage = new DBMessage("000000","",CheckItemKey.TIMEZONE);
        ResultSet rs1 = sourceStmt.executeQuery("SHOW VARIABLES LIKE '%time_zone%';");
        ResultSet rs2 = targetStmt.executeQuery("SHOW VARIABLES LIKE '%time_zone%';");

        while (rs1.next() && rs2.next()) {
            DBKVModel dbkvModel = new DBKVModel();
            if (rs1.getString(1).equals("time_zone") && rs2.getString(1).equals("time_zone")) {
                if (rs1.getString(2).equals(rs2.getString(2))) {
                    dbMessage.setMessage("时区相同");
                    dbMessage.setSqlState("000000");
                } else {
                    dbMessage.setMessage("时区不同");
                    dbkvModel.setSourceDBMsg(rs1.getString(2));
                    dbkvModel.setTargetDBMsg(rs2.getString(2));
                    dbMessage.setMsgList(Collections.singletonList(dbkvModel));
                    dbMessage.setSqlState("000001");
                }
            } else {
                dbMessage.setMessage("时区不同");
                dbkvModel.setSourceDBMsg(rs1.getString(2));
                dbkvModel.setTargetDBMsg(rs2.getString(2));
                dbMessage.setMsgList(Collections.singletonList(dbkvModel));
                dbMessage.setSqlState("000001");
            }
        }
        return dbMessage;
    }


    /**
     * 检查源数据库的账号权限是否满足要求
     *
     * @param sourceStmt
     * @return
     */
    @Override
    public DBMessage checkSourceAuthority(Statement sourceStmt) throws SQLException {
        HashMap<String, String> requiredPrivileges = new HashMap<>();
        requiredPrivileges.put("SELECT", "读取源数据库中的数据需要SELECT权限");
        requiredPrivileges.put("SHOW VIEW", "视图迁移需要SHOW VIEW权限");
        requiredPrivileges.put("RELOAD", "执行 FLUSH 操作,需要RELOAD权限");
        requiredPrivileges.put("LOCK TABLES", "如果使用 mysqldump 进行备份，需要LOCK TABLES权限");
        requiredPrivileges.put("REPLICATION CLIENT ", "如果使用 MySQL 复制进行迁移，需要 REPLICATION CLIENT 权限");
        requiredPrivileges.put("CREATE", "需要创建表或数据库的CREATE权限");
        requiredPrivileges.put("ALTER", "需要修改表结构需要ALTER权限");
//        requiredPrivileges.put("DROP", "需要删除表或数据库，需要DROP权限");
        requiredPrivileges.put("SUPER", "设置全局变量需要SUPER权限");
        Set<String> keys =  requiredPrivileges.keySet();
        Set<String> compares =  requiredPrivileges.keySet();

        DBMessage dbMessage = new DBMessage("000000","",CheckItemKey.SOURCE_AUTHORITY);

        ResultSet rs = sourceStmt.executeQuery("SHOW GRANTS FOR CURRENT_USER;");
        if(rs.next()){
            String[] grants =rs.getString(1).replace("GRANT ", "").trim().split(",");
            Set<String> current_grants = new HashSet<>(Arrays.asList(grants));
            keys.removeAll(current_grants);
            compares.removeAll(keys);
            if(!compares.isEmpty()){
                dbMessage.setSqlState("000001");
                dbMessage.setMessage("源数据库账号权限不满足迁移条件，缺少以下权限");
                List<DBKVModel> msgList = new ArrayList<>();
                for (String key : compares){
                    DBKVModel dbkvModel = new DBKVModel();
                    dbkvModel.setSourceDBMsg(requiredPrivileges.get(key));
                    msgList.add(dbkvModel);
                }
                dbMessage.setMsgList(msgList);
            }else{
                dbMessage.setSqlState("000000");
                dbMessage.setMessage("源数据库账号权限满足迁移条件");
            }
        }
        return dbMessage;
    }

    /**  待定
     * 检查表有无主键。
     * 检查没有和MySQL 5.6、5.7版本的系统数据库、MySQL 8.0版本新增的INNODB_开头的词典表名冲突。
     * @param sourceMeta
     * @param dbNames
     * @return
     */
    @Override
    public DBMessage checkSourceTable(DatabaseMetaData sourceMeta,List<String> dbNames) throws SQLException {
        DBMessage dbMessage = new DBMessage("000000","",CheckItemKey.SOURCE_PRIMARY_KEY);
        List<DBKVModel> msgList = new ArrayList<>();

        for (String dbName:dbNames) {
            ResultSet rs_all_tables = sourceMeta.getTables(dbName, null, "%", new String[]{"TABLE"});
            while (rs_all_tables.next()) {
                String tableName = rs_all_tables.getString("TABLE_NAME");
                // 检查表是否有主键
                ResultSet primaryKeys = sourceMeta.getPrimaryKeys(dbName, null, tableName);

                while (primaryKeys.next()) {
                    DBKVModel dbkvModel = new DBKVModel();
                    dbkvModel.setSourceDBMsg(dbName+"."+tableName);
                    msgList.add(dbkvModel);
                }
            }
        }


        if (!msgList.isEmpty()) {
            dbMessage.setSqlState("000001");
            dbMessage.setMessage("存在无主键表");
            dbMessage.setMsgList(msgList);
        } else {
            dbMessage.setSqlState("000000");
            dbMessage.setMessage("不存在无主键表");
        }

        return dbMessage;
    }

    /** 待定
     * 检查表和存储过程单个ENUM或SET列元素的长度不超过255个字符或1020个字节
     *
     * @param sourceStmt
     * @return
     */
    @Override
    public DBMessage checkSourceTableAndProcess(Statement sourceStmt) {
        return null;
    }

    /**
     * TARGET_TRIGGER("7","检测目标数据库的触发器是否存在"),
     *
     * @param sourceStmt
     * @param targetStmt
     * @return
     */
    @Override
    public DBMessage checkTargetTrigger(Statement sourceStmt,Statement targetStmt) throws SQLException {
//        ResultSet rs1 = sourceStmt.executeQuery("SELECT trigger_name FROM information_schema.triggers;");
//        ResultSet rs2 = targetStmt.executeQuery("SELECT trigger_name FROM information_schema.triggers;");
//        List<String> sourceTrigger = new ArrayList<>();
//        StringBuilder sameTriggerName = new StringBuilder();
        DBMessage dbMessage = new DBMessage();
//        while (rs1.next()) {
//            sourceTrigger.add(rs1.getString(1));
//        }
//
//        while (rs2.next()){
//            if(sourceTrigger.contains(rs2.getString(1))){
//               sameTriggerName.append(rs2.getString(1)).append("\r");
//            }
//        }
//
//        if(sameTriggerName.length()>0){
//            dbMessage.setSqlState("000001");
//            dbMessage.setMessage("目标数据库存在和源数据库相同名称的触发器：\r"+sameTriggerName.toString());
//        }
//        else {
//            dbMessage.setSqlState("000000");
//            dbMessage.setMessage("目标数据库不存在和源数据库相同名称的触发器");
//        }

        return dbMessage;
    }

    /**
     * CHECK_VERSION("8","检测源数据库与目标数据库的版本是否兼容"),
     *
     * @param sourceVersion
     * @param targetVersion
     * @return
     */
    @Override
    public DBMessage checkVersionMigration(String sourceVersion,String targetVersion) {
        DBMessage dbMessage = new DBMessage("000000","",CheckItemKey.CHECK_VERSION);
        if(StringUtils.isNotEmpty(sourceVersion)&&StringUtils.isNotEmpty(targetVersion)){
            sourceVersion=StringUtils.replace(sourceVersion, "-log", "");
            targetVersion=StringUtils.replace(targetVersion, "-log", "");
            String compared = VersionCompareType.compareVersion(sourceVersion, targetVersion);
            switch (compared) {
                case "GT":
                    dbMessage.setSqlState("000000");
                    dbMessage.setMessage("源数据库版本小于目标数据库版本，向下兼容");
                    break;
                case "LT":
                    dbMessage.setSqlState("000001");
                    dbMessage.setMessage("源数据库版本大于目标数据库版本，会有兼容问题");
                    break;
                case "EQ":
                    dbMessage.setSqlState("000000");
                    dbMessage.setMessage("版本相同，即兼容");
            }
        }else{
            dbMessage.setSqlState("000001");
            dbMessage.setMessage("版本号格式不正确");
        }

        return dbMessage;
    }

    /**
     * CHECK_CHARACTOR_SET("9","检测源数据库与目标数据库的字符集是否兼容")
     *
     * @param sourceStmt
     * @param targetStmt
     * @return
     */
    @Override
    public DBMessage checkCharacterSet(Statement sourceStmt, Statement targetStmt) throws SQLException {
        DBMessage dbMessage = new DBMessage("000000","",CheckItemKey.CHECK_CHARACTOR_SET);

        ResultSet rs1 = sourceStmt.executeQuery("SHOW VARIABLES LIKE 'character_set_database';");
        ResultSet rs2 = targetStmt.executeQuery("SHOW VARIABLES LIKE 'character_set_database';");
        while (rs1.next() && rs2.next()) {
            if(rs1.getString(2).equals(rs2.getString(2))){
                dbMessage.setMessage("字符集相同");
                dbMessage.setSqlState("000000");
            }else{
                DBKVModel dbkvModel = new DBKVModel();
                dbkvModel.setSourceDBMsg(rs1.getString(2));
                dbkvModel.setTargetDBMsg(rs2.getString(2));
                dbMessage.setMsgList(Collections.singletonList(dbkvModel));
                dbMessage.setMessage("字符集不同，MySQL 8.0版本的默认字符集为utf8mb4。 MySQL 8.0版本和PolarDB MySQL版的character_set_server值均默认为utf8，您可以根据业务需求进行调整。为支持Unicode，推荐您将使用的utf8mb3字符集转换为使用utf8mb4字符集。");
                dbMessage.setSqlState("000001");
            }
        }
        return dbMessage;
    }

    /**
     * CHECK_ORDER_RULE("10","检测源数据库与目标数据库的排序规则是否兼容")
     *
     * @param sourceStmt
     * @param targetStmt
     * @return
     */
    @Override
    public DBMessage checkOrderRule(Statement sourceStmt, Statement targetStmt) throws SQLException {
        DBMessage dbMessage = new DBMessage("000000","",CheckItemKey.CHECK_ORDER_RULE);

        ResultSet rs1 = sourceStmt.executeQuery("SHOW VARIABLES LIKE 'collation_database';");
        ResultSet rs2 = targetStmt.executeQuery("SHOW VARIABLES LIKE 'collation_database';");
        while (rs1.next() && rs2.next()) {
            if(rs1.getString(2).equals(rs2.getString(2))){
                dbMessage.setMessage("排序校对规则相同");
                dbMessage.setSqlState("000000");
            }else{
                DBKVModel dbkvModel = new DBKVModel();
                dbkvModel.setSourceDBMsg(rs1.getString(2));
                dbkvModel.setTargetDBMsg(rs2.getString(2));
                dbMessage.setMsgList(Collections.singletonList(dbkvModel));
                dbMessage.setMessage("排序校对规则不同，排序规则和字符集不同，它只和排序有关，其中ai表示口音不敏感，即排序时e，è，é，ê和ë之间没有区别；ci表示不区分大小写，即排序时p和P之间没有区别。");
                dbMessage.setSqlState("000001");
            }
        }
        return dbMessage;
    }

    /**
     * CHECK_SQL_MODE("11","检测源数据库与目标数据库的 SQL_MODE 是否兼容"),
     *
     * @param sourceStmt
     * @param targetStmt
     * @return
     */
    @Override
    public DBMessage checkSqlMode(Statement sourceStmt, Statement targetStmt) throws SQLException {
        DBMessage dbMessage = new DBMessage("000000","",CheckItemKey.CHECK_SQL_MODE);

        ResultSet rs1 = sourceStmt.executeQuery("SELECT @@GLOBAL.sql_mode;");
        ResultSet rs2 = targetStmt.executeQuery("SELECT @@GLOBAL.sql_mode;");
        while (rs1.next() && rs2.next()) {
            if(rs1.getString(1).equals(rs2.getString(1))){
                dbMessage.setMessage("源数据库与目标数据库的SQL_MODE兼容");
                dbMessage.setSqlState("000000");
            }else{
                DBKVModel dbkvModel = new DBKVModel();
                dbkvModel.setSourceDBMsg(rs1.getString(1));
                dbkvModel.setTargetDBMsg(rs2.getString(1));
                dbMessage.setMsgList(Collections.singletonList(dbkvModel));
                dbMessage.setMessage("源数据库与目标数据库的SQL_MODE不同，为避免8.0版本的集群启动失败，请通过NO_AUTO_CREATE_USER从MySQL选项文件的系统变量sql_mode设置中删除所有集群。\n" +
                        "您的系统变量设置中不得定义过时的SQL模式，否则sql_mode会引起许多不同的行为，在版本升级时需要确认对齐。需要取消的配置项如下：DB2, MAXDB, MSSDL, MYSQL323, MYSQL40, ORACLE, POSTGRESQL, NO_FIELD_OPTIONS, NO_KEY_OPTI");
                dbMessage.setSqlState("000001");
            }
        }

        return dbMessage;
    }

    /**
     * UPPER_LOWER_CASE_PARAMETER("12","检测源数据库与目标数据库的大小写敏感参数配置是否兼容")
     *
     * @param sourceStmt
     * @param targetStmt
     * @return
     */
    @Override
    public DBMessage checkUperAndLowerParameter(Statement sourceStmt, Statement targetStmt) throws SQLException{
        DBMessage dbMessage = new DBMessage("000000","",CheckItemKey.UPPER_LOWER_CASE_PARAMETER);

        ResultSet rs1 = sourceStmt.executeQuery("SHOW VARIABLES LIKE 'lower_case_table_names';");
        ResultSet rs2 = targetStmt.executeQuery("SHOW VARIABLES LIKE 'lower_case_table_names';");
        if (rs1.next() && rs2.next()) {
            if(rs1.getString(2).equals(rs2.getString(2))){
                dbMessage.setMessage("源数据库与目标数据库的大小写敏感参数配置兼容");
                dbMessage.setSqlState("000000");
            }else{
                DBKVModel dbkvModel = new DBKVModel();
                dbkvModel.setSourceDBMsg("lower_case_table_names:"+rs1.getString(2));
                dbkvModel.setTargetDBMsg("lower_case_table_names:"+rs2.getString(2));
                dbMessage.setMsgList(Collections.singletonList(dbkvModel));
                dbMessage.setMessage("源数据库与目标数据库的大小写敏感参数配置不同，从MySQL 8.0.11版本开始，禁止lower_case_table_names使用与服务器初始化时不同的设置来启动服务器。各种数据字典、表字段使用的排序规则基于lower_case_table_names服务器初始化时定义的设置，使用不同的设置重新启动服务器时，会导致标识符的排序和比较方式引入不一致。在PolarDB MySQL版 8.0版本中，集群区分大小写无法在初始化完成后再次更改，您需要在购买 8.0版本的集群时选定集群是否区分大小写。");
                dbMessage.setSqlState("000001");
            }
        }

        return dbMessage;
    }

    /**
     * EXPLICIT_DEFAULTS_FOR_TIMESTAMP("13","检测源数据库与目标数据库的 explicit_defaults_for_timestamp 是否兼容"),
     *
     * @param sourceStmt
     * @param targetStmt
     * @return
     */
    @Override
    public DBMessage checkExplicitDefaultsForTimestamp(Statement sourceStmt, Statement targetStmt) throws SQLException{
        DBMessage dbMessage = new DBMessage("000000","",CheckItemKey.EXPLICIT_DEFAULTS_FOR_TIMESTAMP);

        ResultSet rs1 = sourceStmt.executeQuery("SHOW VARIABLES LIKE 'explicit_defaults_for_timestamp';");
        ResultSet rs2 = targetStmt.executeQuery("SHOW VARIABLES LIKE 'explicit_defaults_for_timestamp';");
        while (rs1.next() && rs2.next()) {
            if(rs1.getString(2).equals(rs2.getString(2))){
                dbMessage.setMessage("TIMESTAMP列的默认值的设置方式兼容");
                dbMessage.setSqlState("000000");
            }else{
                DBKVModel dbkvModel = new DBKVModel();
                dbkvModel.setSourceDBMsg("explicit_defaults_for_timestamp:"+rs1.getString(2));
                dbkvModel.setTargetDBMsg("explicit_defaults_for_timestamp:"+rs2.getString(2));
                dbMessage.setMsgList(Collections.singletonList(dbkvModel));
                dbMessage.setMessage("TIMESTAMP列的默认值的设置方式不同，从MySQL 8.0开始，MySQL官方将explicit_defaults_for_timestamp的默认值从OFF修改成ON。目前PolarDB MySQL版 8.0中该参数的默认值仍然遵循5.6和5.7版本为OFF。\n" +
                        "如果迁移过程中不希望自动添加NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP，则需要将该参数设置成ON。");
                dbMessage.setSqlState("000001");
            }
        }

        return dbMessage;
    }

    /**
     * CHECK_FUNCTION("14","检查源数据库获取存储过程和函数的定义是否已授予权限")
     *
     * @param sourceStmt
     * @return
     */
    @Override
    public DBMessage checkSourceFunction(Statement sourceStmt) throws SQLException {
        DBMessage dbMessage = new DBMessage();
//        String requiredPermission = "EXECUTE"; // 替换为需要检查的权限
//        Statement sourceStmt = sourceConn.createStatement();
//        ResultSet rs_version = sourceStmt.executeQuery("SELECT VERSION();");
//        ResultSet rs_user = sourceStmt.executeQuery("SELECT CURRENT_USER()");
//        while (rs_version.next() && rs_user.next()) {
//            int masterVersion = Integer.parseInt(rs_version.getString(1).split("/.")[0]);
//            String current_user = rs_user.getString(1);
//            ResultSet rs_procs= null;
//            if (masterVersion >5) {
//                String sql ="SELECT DEFINER,ROUTINE_NAME FROM information_schema.routines WHERE routine_schema = ? ";
//                PreparedStatement pstmt = sourceConn.prepareStatement(sql);
//                pstmt.setString(1, dbName);
//                rs_procs = pstmt.executeQuery();
//                while(rs_procs.next()){
//                    String definer = rs_procs.getString(1);
//                    String routine_name = rs_procs.getString(2);
//                    ResultSet authkeys = sourceStmt.executeQuery("SHOW GRANTS FOR " + definer + ";");
//                    boolean hasPermission = false;
//                    while(authkeys.next()){
//                        if (authkeys.getString(1).contains(requiredPermission)) {
//                            hasPermission = true;
//                            break;
//                        }
//                    }
//                    dbMessage.setMessage("源数据库获取存储过程和函数的定义已授权");
//                }
//
//                dbMessage.setMessage("源数据库获取存储过程和函数的定义已授予权限");
//                dbMessage.setSqlState("000000");
//            } else {
//                dbMessage.setMessage("源数据库获取存储过程和函数的定义未授予权限");
//            }
//        }

        return null;
    }

    /**
     * DEFINER_EXISTS("15","检查 View，Trigger，Procedure，Function，Event 的 Definer 在目标库是否存在")
     *
     * @param sourceConn
     * @param targetConn
     * @return
     */
    @Override
    public DBMessage checkDefiner(Connection sourceConn, Connection targetConn,List<String> dbNames) throws SQLException{
        DBMessage dbMessage = new DBMessage("000000","",CheckItemKey.DEFINER_EXISTS);
        HashMap<String,String> sqlMap = new HashMap<>();
        sqlMap.put("View","SELECT table_name FROM information_schema.views WHERE table_schema = ?");
        sqlMap.put("Trigger","SELECT trigger_name FROM information_schema.triggers WHERE event_object_schema = ?");
        sqlMap.put("Procedure","SELECT routine_name FROM information_schema.routines WHERE routine_type = 'PROCEDURE' AND routine_schema = ?");
        sqlMap.put("Function","SELECT routine_name FROM information_schema.routines WHERE routine_type = 'FUNCTION' AND routine_schema = ?");
        sqlMap.put("Event","SELECT event_name FROM information_schema.events WHERE event_schema = ?");

        Iterator<Map.Entry<String,String>> iterator = sqlMap.entrySet().iterator();
        List<DBKVModel> msgList = new ArrayList<>();
        for (String dbName : dbNames){
            while(iterator.hasNext()){
                Map.Entry<String, String> entry = iterator.next();
                PreparedStatement source_pstmt = sourceConn.prepareStatement(entry.getValue());
                source_pstmt.setString(1, dbName);
                PreparedStatement target_pstmt = targetConn.prepareStatement(entry.getValue());
                target_pstmt.setString(1, dbName);
                ResultSet source_result = source_pstmt.executeQuery();
                ResultSet target_result = target_pstmt.executeQuery();
                List<String> source_list = new ArrayList<>();

                while (source_result.next()) {
                    source_list.add(source_result.getString(1));
                }

                List<String> target_list = new ArrayList<>();
                while (target_result.next()) {
                    target_list.add(target_result.getString(1));
                }

                source_list.retainAll(target_list);

                if(!source_list.isEmpty()){
                    for (String source_item : source_list){
                        DBKVModel dbkvModel = new DBKVModel();
                        dbkvModel.setSourceDBMsg(dbName+"."+entry.getKey()+"."+source_item);
                        dbkvModel.setTargetDBMsg(dbName+"."+entry.getKey()+"."+source_item);
                        msgList.add(dbkvModel);
                    }
                }
            }
        }

        if(!msgList.isEmpty()){
            dbMessage.setSqlState("000001");
            dbMessage.setMsgList(msgList);
            dbMessage.setMessage("源库中的View/Trigger/Procedure/Function/Event在目标库中存在同名对象。");
        }else{
            dbMessage.setSqlState("000000");
            dbMessage.setMessage("源库中的对象在目标库中不存在");
        }

        return dbMessage;
    }

    /**
     * 目标库同名对象存在性检查
     * TARGET_EXISTS("17","检查待复制对象在目标数据库中是否已存在")
     *
     * @param sourceStmt
     * @param targetConn
     * @return
     */
    @Override
    public DBMessage checkTargetExists(Statement sourceStmt, Connection targetConn,List<String> dbNames) throws SQLException {
        Statement targetStmt = targetConn.createStatement();
        ResultSet dbs = targetStmt.executeQuery("SHOW DATABASES;");

        List<DBKVModel> msgList = new ArrayList<>();
        DBMessage dbMessage = new DBMessage("000000","",CheckItemKey.TARGET_EXISTS);

        for (String dbName : dbNames){
            while (dbs.next()) {
                String db = dbs.getString(1);

                if(db.equals(dbName)){
                    dbMessage.setSqlState("000001");
                    DBKVModel dbkvModel = new DBKVModel();
                    dbkvModel.setSourceDBMsg(dbName);
                    dbkvModel.setTargetDBMsg(dbName);
                    msgList.add(dbkvModel);
                    PreparedStatement target_pstmt=targetConn.prepareStatement("SHOW TABLES FROM ?;");
                    target_pstmt.setString(1, dbName);
                    ResultSet target_tables = target_pstmt.executeQuery();
                    List<String> target_list = new ArrayList<>();
                    while (target_tables.next()) {
                        target_list.add(target_tables.getString(1));
                    }

                    ResultSet source_tables = sourceStmt.executeQuery("SHOW TABLES;");
                    List<String> source_list = new ArrayList<>();
                    while (source_tables.next()){
                        source_list.add(source_tables.getString(1));
                    }

                    source_list.retainAll(target_list);
                    if(!source_list.isEmpty()){
                        for (String source_item : source_list){
                            DBKVModel kvModel = new DBKVModel();
                            kvModel.setSourceDBMsg(dbName+"."+source_item);
                            kvModel.setTargetDBMsg(dbName+"."+source_item);
                            msgList.add(kvModel);
                        }
                    }
                }
            }
        }

        if(msgList.isEmpty()){
            dbMessage.setSqlState("000000");
            dbMessage.setMessage("目标数据库源中没有同名数据库或表");
        }else
        {
            dbMessage.setSqlState("000001");
            dbMessage.setMessage("目标数据库源中存在有同名数据库或表，建议迁移同名库和表，避免被覆盖。");
            dbMessage.setMsgList(msgList);
        }

        return dbMessage;
    }

    /**
     * 目标库数据存在性检查
     * TARGET_EXISTS_DATA("16","检查待复制对象在目标数据库中是否已存在数据")
     *
     * @param targetConn
     * @param dbNames
     * @return
     */
    @Override
    public DBMessage checkDataExists(Connection targetConn,List<String> dbNames) throws SQLException {
        DBMessage dbMessage = new DBMessage("000000","",CheckItemKey.TARGET_EXISTS_DATA);

        List<DBKVModel> msgList = new ArrayList<>();

        Statement db_stmt = targetConn.createStatement();
        ResultSet dbs = db_stmt.executeQuery("SHOW DATABASES;");
        List<String> dbList = new ArrayList<>();
        while (dbs.next()) {
            String db = dbs.getString(1);
            if(!dbList.contains(db)){
                dbList.add(db);
            }
        }
        dbNames.retainAll(dbList);
        for (String dbName:dbNames) {
            PreparedStatement target_pstmt = targetConn.prepareStatement("SHOW TABLES FROM ?;");
            target_pstmt.setString(1, dbName);
            ResultSet target_tables = target_pstmt.executeQuery();
            ResultSetMetaData metaData = target_tables.getMetaData();
            int columnCount = metaData.getColumnCount();
            if (columnCount == 1) {
                while (target_tables.next()) {
                    PreparedStatement target_tables_pstmt = targetConn.prepareStatement("SELECT COUNT(*) FROM ?");
                    target_tables_pstmt.setString(1, StringUtils.format("{}.{}", dbName, target_tables.getString(1)));
                    ResultSet rs = target_tables_pstmt.executeQuery();
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        if (count > 0) {
                            DBKVModel kvModel = new DBKVModel();
                            kvModel.setSourceDBMsg(dbName+"."+target_tables.getString(1));
                            kvModel.setTargetDBMsg(dbName+"."+target_tables.getString(1));
                            msgList.add(kvModel);
//                            failBuilder.append("表").append(target_tables.getString(1)).append("在目标数据库:").append(dbName).append("中存在数据").append(" \r\n ");
                        }
                    }
                }
            }
        }
        if (!msgList.isEmpty()){
            dbMessage.setSqlState("000001");
            dbMessage.setMessage("目标数据库源中存在同名表并且包含数据，建议迁移相关数据，避免被覆盖遗失。");
            dbMessage.setMsgList(msgList);
        }else{
            dbMessage.setSqlState("000000");
            dbMessage.setMessage("目标数据库源中没有同名表");
        }
        return dbMessage;
    }



    /**
     * 分区表检查
     * PARTITION_CHECK("18","检查源数据库确保只有InnoDB引擎的分区表，检查表的分区不在共享InnoDB tablespaces表空间中")
     *
     * @param sourceStmt
     * @return
     */
    @Override
    public DBMessage checkTablePartition(Statement sourceStmt) throws SQLException{;
        DBMessage dbMessage = new DBMessage("000000","",CheckItemKey.PARTITION_CHECK);
        ResultSet dbs = sourceStmt.executeQuery(" SELECT TABLE_SCHEMA, TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE ENGINE NOT IN ('innodb','ndbcluster') AND CREATE_OPTIONS LIKE '%partitioned';");
        List<DBKVModel> msgList = new ArrayList<>();
        while (dbs.next()){
            DBKVModel kvModel = new DBKVModel();
            kvModel.setSourceDBMsg(dbs.getString(1)+"."+dbs.getString(2));
            msgList.add(kvModel);
        }
        if (!msgList.isEmpty()){
            dbMessage.setSqlState("000001");
            dbMessage.setMessage("源数据库存在非InnoDB引擎的分区表,在8.0版本中，创建MyISAM类型的分区将导致使用没有此类型支持的存储引擎的分区表创建语句失败并出现错误（ER_CHECK_NOT_IMPLEMENTED，建议将有关表从MyISAM转换至InnoDB引擎。");
            dbMessage.setMsgList(msgList);
        }
        else{
            dbMessage.setSqlState("000000");
            dbMessage.setMessage("源数据库不存在非InnoDB引擎的分区表");
        }

        return dbMessage;
    }

    /**
     * 全局参数检查
     * WHOLE_PARAMETER_FIT("19","检查源数据源和目标数据源全局参数中的默认值一致性")
     *
     * @param sourceStmt
     * @param targetStmt
     * @return
     */
    @Override
    public DBMessage checkWholeParameterFit(Statement sourceStmt, Statement targetStmt) throws SQLException{
        DBMessage dbMessage = new DBMessage("000000","",CheckItemKey.WHOLE_PARAMETER_FIT);
        ResultSet source_rs = sourceStmt.executeQuery("SHOW VARIABLES;");
        ResultSet target_rs = targetStmt.executeQuery("SHOW VARIABLES;");
        HashMap<String,String> source_list = new HashMap<>();;
        HashMap<String,String> target_list = new HashMap<>();
        HashMap<String,String> diff_map = new HashMap<>();
        List<DBKVModel> msgList = new ArrayList<>();
        while (source_rs.next()) {
            source_list.put(source_rs.getString(1),source_rs.getString(2));
        }

        while (target_rs.next()) {
            target_list.put(target_rs.getString(1),target_rs.getString(2));
        }

        //长度为source_list.size()，会遗漏 target_list.size()
        for (String key : source_list.keySet()) {
            if (target_list.containsKey(key)) {
                if (!target_list.get(key).equals(source_list.get(key))) {
                    diff_map.put(key+"="+source_list.get(key),key+"="+target_list.get(key));
                }
            }else{
                diff_map.put(key+"="+source_list.get(key),"不存在");
            }

        }
        //长度为target_list.size()，会补充遗漏的
        for (String key : target_list.keySet()) {
            if (source_list.containsKey(key)) {
                if (!source_list.get(key).equals(target_list.get(key))) {
                    //保证和source_list的diff_map顺序一致
                    diff_map.put(key+"="+source_list.get(key),key+"="+target_list.get(key));
                }
            }else{
                diff_map.put("不存在",key+"="+target_list.get(key));
            }

        }

        if(!diff_map.isEmpty()){
            for(String key:diff_map.keySet())
            {
                DBKVModel dbkvModel = new DBKVModel();
                dbkvModel.setSourceDBMsg(key);
                dbkvModel.setTargetDBMsg(diff_map.get(key));
                msgList.add(dbkvModel);
            }
            dbMessage.setSqlState("000001");
            dbMessage.setMessage("目标数据库中全局参数与源数据库不一致，建议核对相关参数，保证数据迁移后能够稳定运行。");
            dbMessage.setMsgList(msgList);
        }else{
            dbMessage.setSqlState("000000");
            dbMessage.setMessage("目标数据库中全局参数与源数据库一致");
        }

        return dbMessage;
    }
    private Set<String> RESERVED_KEYWORDS = new HashSet<>(Arrays.asList("ACTION", "ADD", "ADMIN", "ALL", "ALTER", "ANALYZE", "AND", "AS", "ASC", "ASENSITIVE", "BEFORE", "BETWEEN", "BIGINT", "BINARY", "BLOB", "BOTH", "BY", "CALL", "CASCADE", "CASE", "CHANGE", "CHAR", "CHARACTER", "CHECK", "COLLATE", "COLUMN", "CONDITION", "CONNECTION", "CONSTRAINT", "CONTINUE", "CONVERT", "CREATE", "CROSS", "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "CURSOR", "DATABASE", "DATABASES", "DAY_HOUR", "DAY_MICROSECOND", "DAY_MINUTE", "DAY_SECOND", "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DELAYED", "DELETE", "DESC", "DESCRIBE", "DETERMINISTIC", "DISTINCT", "DISTINCTROW", "DIV", "DOUBLE", "DROP", "DUAL", "EACH", "EAGLEYE", "ELSE", "ELSEIF", "ENCLOSED", "ESCAPED", "EXISTS", "EXIT", "EXPLAIN", "FALSE", "FETCH", "FLOAT", "FLOAT4", "FLOAT8", "FOR", "FORCE", "FOREIGN", "FROM", "FULLTEXT", "GOTO", "GRANT", "GROUP", "GUEST", "HAVING", "HIGH_PRIORITY", "HOUR_MICROSECOND", "HOUR_MINUTE", "HOUR_SECOND", "IF", "IGNORE", "IN", "INDEX", "INFILE", "INFORMATION_SCHEMA", "INNER", "INOUT", "INSENSITIVE", "INSERT", "INT", "INT1", "INT2", "INT3", "INT4", "INT8", "INTEGER", "INTERVAL", "INTO", "IS", "ITERATE", "JOIN", "KEY", "KEYS", "KILL", "LABEL", "LEADING", "LEAVE", "LEFT", "LIKE", "LIMIT", "LINEAR", "LINES", "LOAD", "LOCALTIME", "LOCALTIMESTAMP", "LOCK", "LONG", "LONGBLOB", "LONGTEXT", "LOOP", "LOW_PRIORITY", "MATCH", "MEDIUMBLOB", "MEDIUMINT", "MEDIUMTEXT", "MIDDLEINT", "MINUTE_MICROSECOND", "MINUTE_SECOND", "MOD", "MODIFIES", "MYSQL", "NATURAL", "NO_WRITE_TO_BINLOG", "NOT", "NULL", "NUMERIC", "ON", "OPTIMIZE", "OPTION", "OPTIONALLY", "OR", "ORDER", "OUT", "OUTER", "OUTFILE", "PERFORMANCE_SCHEMA", "PRECISION", "PRIMARY", "PROCEDURE", "PURGE", "RAID0", "RANGE", "READ", "READS", "REAL", "REFERENCES", "REGEXP", "RELEASE", "RENAME", "REPEAT", "REPLACE", "REPLICATOR", "REQUIRE", "RESTRICT", "RETURN", "REVOKE", "RIGHT", "RLIKE", "ROOT", "SCHEMA", "SCHEMAS", "SECOND_MICROSECOND", "SELECT", "SENSITIVE", "SEPARATOR", "SET", "SHOW", "SMALLINT", "SPATIAL", "SPECIFIC", "SQL", "SQL_BIG_RESULT", "SQL_CALC_FOUND_ROWS", "SQL_SMALL_RESULT", "SQLEXCEPTION", "SQLSTATE", "SQLWARNING", "SSL", "STARTING", "STRAIGHT_JOIN", "TABLE", "TERMINATED", "TEST", "THEN", "TINYBLOB", "TINYINT", "TINYTEXT", "TO", "TRAILING", "TRIGGER", "TRUE", "UNDO", "UNION", "UNIQUE", "UNLOCK", "UNSIGNED", "UPDATE", "USAGE", "USE", "USING", "UTC_DATE", "UTC_TIME", "UTC_TIMESTAMP", "VALUES", "VARBINARY", "VARCHAR", "VARCHARACTER", "VARYING", "WHEN", "WHERE", "WHILE", "WITH", "WRITE", "X509", "XOR", "XTRABAK", "YEAR_MONTH", "ZEROFILL"));
    @Override
    public DBMessage keywordsCheck(Statement sourceStmt, Statement targetStmt,List<String> dbName,String targetVersion) throws SQLException {
        DBMessage dbMessage = new DBMessage("000000", "", CheckItemKey.KEYWORDS_CHECK);
        List<DBKVModel> msgList = new ArrayList<>();

        DatabaseMetaData metaData = sourceStmt.getConnection().getMetaData();

        String targetVersionNew = targetVersion.substring(0, 5);
        if (targetVersionNew.equals("8.0.1")||targetVersionNew.equals("8.0.2")){
            Set<String> keywords = new HashSet<>();
            String query = "SELECT word FROM information_schema.KEYWORDS";
            try (ResultSet resultSet = targetStmt.executeQuery(query)) {
                while (resultSet.next()) {
                    String keyword = resultSet.getString("word");
                    keywords.add(keyword);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            RESERVED_KEYWORDS=keywords;
        }

        for (String catalog : dbName) {
            // 检查库名是否为保留关键字
            if (RESERVED_KEYWORDS.contains(catalog.toUpperCase())) {
                StringBuilder sb = new StringBuilder();
                sb.append(catalog).append("\n");
                DBKVModel dbkvModel = new DBKVModel();
                if (targetVersionNew.equals("8.0.1")||targetVersionNew.equals("8.0.2")){
                    dbkvModel.setTargetDBMsg("information_schema.KEYWORDS."+catalog.toUpperCase());
                }else {
                    dbkvModel.setTargetDBMsg("官方提供关键字列:"+catalog.toUpperCase());
                }

                dbkvModel.setSourceDBMsg(sb.toString());
                msgList.add(dbkvModel);
            }

            // 查询给定库的所有表和列
            ResultSet rsTables = metaData.getTables(catalog, null, "%", new String[]{"TABLE"});
            while (rsTables.next()) {
                String tableName = rsTables.getString("TABLE_NAME");
                if (RESERVED_KEYWORDS.contains(tableName.toUpperCase())) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(catalog).append(".").append(tableName).append("\n");
                    DBKVModel dbkvModel = new DBKVModel();
                    if (targetVersionNew.equals("8.0.1")||targetVersionNew.equals("8.0.2")){
                        dbkvModel.setTargetDBMsg("information_schema.KEYWORDS."+tableName.toUpperCase());
                    }else {
                        dbkvModel.setTargetDBMsg("官方提供关键字列:"+tableName.toUpperCase());
                    }
                    dbkvModel.setSourceDBMsg(sb.toString());
                    msgList.add(dbkvModel);
                }

                // 检查列名称
                ResultSet rsColumns = metaData.getColumns(catalog, null, tableName, "%");
                while (rsColumns.next()) {
                    String columnName = rsColumns.getString("COLUMN_NAME");
                    if (RESERVED_KEYWORDS.contains(columnName.toUpperCase())) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(catalog).append(".").append(tableName).append(".").append(columnName).append("\n");
                        DBKVModel dbkvModel = new DBKVModel();
                        if (targetVersionNew.equals("8.0.1")||targetVersionNew.equals("8.0.2")){
                            dbkvModel.setTargetDBMsg("information_schema.KEYWORDS."+columnName.toUpperCase());
                        }else {
                            dbkvModel.setTargetDBMsg("官方提供关键字列:"+columnName.toUpperCase());
                        }
                        dbkvModel.setSourceDBMsg(sb.toString());
                        msgList.add(dbkvModel);
                    }
                }
                rsColumns.close();
            }
            rsTables.close();
        }


            // 如果找到保留关键字，则添加到列表
            if (msgList.size() > 0) {
                dbMessage.setSqlState("000001");
                dbMessage.setMessage("有引用目标数据库中关键字(库名.表名.列名)，建议核对相关参数，保证数据迁移后能够稳定运行。");
                dbMessage.setMsgList(msgList);
            } else {
                dbMessage.setSqlState("000000");
                dbMessage.setMessage("没有引用目标数据库中关键字");
            }

            return dbMessage;
        }

    @Override
    public DBMessage validatePasswordCheck(Statement sourceStmt, Statement targetStmt, String sourceUserName,String targetUserName) throws SQLException {
        DBMessage dbMessage = new DBMessage("000000", "", CheckItemKey.VALIDATE_PASSWORD_CHECK);
        List<DBKVModel> msgList = new ArrayList<>();
        String sourcePlugin = null;
        String targetPlugin = null;
        String sourceQuery = "SELECT plugin FROM mysql.user WHERE User = '" + sourceUserName + "'";
        ResultSet sourceRs = sourceStmt.executeQuery(sourceQuery);
        if (sourceRs.next()) {
            sourcePlugin = sourceRs.getString("plugin");
        }

        // 查询目标数据库用户的插件
        String targetQuery = "SELECT plugin FROM mysql.user WHERE User = '" + targetUserName + "'";
        ResultSet targetRs = targetStmt.executeQuery(targetQuery);
        if (targetRs.next()) {
            targetPlugin = targetRs.getString("plugin");
        }

        if (sourcePlugin != null && sourcePlugin.equals(targetPlugin)) {
            dbMessage.setSqlState("000000");
            dbMessage.setMessage("源数据源的当前用户和目标数据源的当前用户密码默认策略一致");

        } else {
            DBKVModel dbkvModel = new DBKVModel();
            dbkvModel.setSourceDBMsg(sourcePlugin);
            dbkvModel.setTargetDBMsg(targetPlugin);
            msgList.add(dbkvModel);
            dbMessage.setSqlState("000001");
            dbMessage.setMessage("源数据源的当前用户和目标数据源的当前用户密码默认策略不一致，建议核对相关参数，保证数据迁移后能够稳定运行。");
            dbMessage.setMsgList(msgList);
        }

        return dbMessage;
}
}
