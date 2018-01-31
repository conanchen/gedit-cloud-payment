package com.github.conanchen.gedit.payment.unit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class RedisSetMap {

    @Autowired
    private RedisTemplate redisTemplate;

    public int setCacheMap(String key,Map<String, String> dataMap)
    {
        if(null != dataMap)
        {
            HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
            for (Map.Entry<String, String> entry : dataMap.entrySet()) {
                /*System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());  */
                if(hashOperations != null){
                    hashOperations.put(key,entry.getKey(),entry.getValue());
                } else{
                    return 0;
                }
            }
        } else{
            return 0;
        }
        return dataMap.size();
    }

    /**
     * 获得缓存的Map
     * @param key
     * @param /*hashOperation
     * @return
     */
    public Map<String, String> getCacheMap(String key/*,HashOperations<String,String,T> hashOperation*/)
    {
        Map<String, String> map = redisTemplate.opsForHash().entries(key);
        /*Map<String, T> map = hashOperation.entries(key);*/
        return map;
    }


    /**
     * 缓存Map
     * @param key
     * @param dataMap
     * @return
     */
    public void setCacheIntegerMap(String key,Map<Integer, Object> dataMap)
    {
        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        if(null != dataMap)
        {
            for (Map.Entry<Integer, Object> entry : dataMap.entrySet()) {
                /*System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());  */
                hashOperations.put(key,entry.getKey(),entry.getValue());
            }

        }
    }


    /**
     * 获得缓存的Map
     * @param key
     * @param /hashOperation
     * @return
     */
    public Map<Object, Object> getCacheIntegerMap(String key/*,HashOperations<String,String,T> hashOperation*/){
        Map<Object, Object> map = redisTemplate.opsForHash().entries(key);
        /*Map<String, T> map = hashOperation.entries(key);*/
        return map;
    }


    /**
     * 从hash中删除指定的存储
     *
     * @param key
     * @return 状态码，1成功，0失败
     * */
    public long deleteMap(String key) {
        redisTemplate.setEnableTransactionSupport(true);
        return redisTemplate.opsForHash().delete(key);
    }

    /**
     * 设置过期时间
     * @param key
     * @param time
     * @param unit
     * @return
     */
    public boolean expire(String key, long time, TimeUnit unit) {
        return redisTemplate.expire(key, time, unit);
    }

}
