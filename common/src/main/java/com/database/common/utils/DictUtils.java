package com.database.common.utils;

import org.springframework.stereotype.Component;
import com.database.common.constant.Constants;

/**
 * 字典工具类

 */
@Component
public class DictUtils
{
    /**
     * 分隔符
     */
    public static final String SEPARATOR = ",";



    /**
     * 获取cache name
     * 
     * @return 缓存名
     */
    public static String getCacheName()
    {
        return Constants.SYS_DICT_CACHE;
    }

    /**
     * 设置cache key
     * 
     * @param configKey 参数键
     * @return 缓存键key
     */
    public static String getCacheKey(String configKey)
    {
        return Constants.SYS_DICT_KEY + configKey;
    }
}
