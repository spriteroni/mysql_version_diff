package com.database.common.enums;

import lombok.Getter;

@Getter
public enum CheckItemKey {
    TIMEZONE ("源数据源和目标数据源时区检查","检测源数据库与目标数据库的时区是否一致"),
    SOURCE_CONNECTION("源数据源连接检查","检查源数据源网关状态、实例是否可达、用户名及密码准确性"),
    TARGET_CONNECTION("目标数据源连接检查","检查目标数据源网关状态、实例是否可达、用户名及密码准确性"),
    SOURCE_AUTHORITY("源库权限检查","检查源数据库的账号权限是否满足要求"),
    SOURCE_PRIMARY_KEY("源数据源表检查","检查表有无主键"),
    SOURCE_TABLE_PROCESS("源数据源表和存储过程长度检查","检查表和存储过程单个ENUM或SET列元素的长度不超过255个字符或1020个字节"),
    TARGET_TRIGGER("目标数据源触发器检查","检测目标数据库的触发器是否存在"),
    CHECK_VERSION("源数据源和目标数据源版本检查","检测源数据库与目标数据库的版本是否兼容"),
    CHECK_CHARACTOR_SET("源数据源和目标数据源字符集检查","检测源数据库与目标数据库的字符集是否兼容"),
    CHECK_ORDER_RULE("源数据源和目标数据源排序规则检查","检测源数据库与目标数据库的排序规则是否兼容"),
    CHECK_SQL_MODE("源数据源和目标数据源 SQL_MODE 检查","检测源数据库与目标数据库的 SQL_MODE 是否兼容"),
    UPPER_LOWER_CASE_PARAMETER("源数据源和目标数据源大小写敏感参数配置检查","检测源数据库与目标数据库的大小写敏感参数配置是否兼容"),
    EXPLICIT_DEFAULTS_FOR_TIMESTAMP("源数据源和目标数据源 explicit_defaults_for_timestamp 检查","检测源数据库与目标数据库的 explicit_defaults_for_timestamp 是否兼容"),
    CHECK_FUNCTION("源数据库存储过程及函数权限检查","检查源数据库获取存储过程和函数的定义是否已授予权限"),
    DEFINER_EXISTS("非表对象的 Definer 存在性检查","检查 View，Trigger，Procedure，Function，Event 的 Definer 在目标库是否存在"),
    TARGET_EXISTS_DATA("目标库数据存在性检查","检查待复制对象在目标数据库中是否已存在数据"),
    TARGET_EXISTS("目标库同名对象存在性检查", "检查待复制对象在目标数据库中是否已存在"),
    PARTITION_CHECK("源数据库分区表检查", "检查源数据库确保只有InnoDB引擎的分区表"),
    WHOLE_PARAMETER_FIT("源数据源和目标数据源全局参数兼容性检查","检查源数据源和目标数据源全局参数中的默认值一致性"),
    KEYWORDS_CHECK("目标数据源关键字检查","检查源数据源库名，表名和表字段是否是与目标数据源关键字占用有冲突"),
    VALIDATE_PASSWORD_CHECK("源数据源和目标数据源密码默认策略检查","检查源数据源的当前用户和目标数据源的当前用户密码默认策略一致性");

    private final String code;
    private final String info;


    CheckItemKey(String code, String info)
    {
        this.code = code;
        this.info = info;
    }
}
