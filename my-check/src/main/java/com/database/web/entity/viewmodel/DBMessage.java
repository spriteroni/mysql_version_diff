package com.database.web.entity.viewmodel;

import com.database.common.enums.CheckItemKey;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DBMessage {
    public DBMessage() {}

    public DBMessage(String sqlState, String message, CheckItemKey checkItem) {
        this.sqlState = sqlState;
        this.message = message;
        this.checkCode = checkItem.getCode();
        this.checkContent=checkItem.getInfo();
        this.setKeyStr(checkItem.name());
    }
    /**
     * SQL 状态类型SQLState，这是SQL标准中定义的
     */
    private String sqlState;
    private String message;
    private String keyStr;
    private String checkCode;
    private String checkContent;
    private String dbType;
    private String dbVersion;
    private List<String> dbItems;
    private List<DBKVModel> msgList;

    /**
     * 错误码,厂商定义的
     */
    private Integer vendorCode;
    
}
