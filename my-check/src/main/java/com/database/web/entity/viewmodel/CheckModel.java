package com.database.web.entity.viewmodel;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CheckModel {
    private String keyStr;
    private String checkCode;
    private String checkContent;
    private String sqlState;
    private String message;
    private List<DBKVModel> msgList;
}
