package com.database.common.enums;

import com.database.common.utils.StringUtils;

import java.util.List;

/**
 *  版本比较类型
 */
public enum VersionCompareType
{
    /**
     * 相等
     */
    EQ,

    /**
     * 小于
     */
    LT,

    /**
     * 大于
     */
    GT;

    public static String compareVersion(String sourceVersion,String targetVersion) {
        if(sourceVersion.equals(targetVersion)) {
            return EQ.name();
        }
        String[] sourceVersionList = StringUtils.split(sourceVersion, ".");
        String[] targetVersionList = StringUtils.split(targetVersion, ".");
        for(int i = 0; i < sourceVersionList.length; i++) {
            if(Integer.parseInt(sourceVersionList[i]) < Integer.parseInt(targetVersionList[i])) {
                return GT.name();
            } else if(Integer.parseInt(sourceVersionList[i]) > Integer.parseInt(targetVersionList[i])) {
                return LT.name();
            }
        }
        return EQ.name();
    }
}
