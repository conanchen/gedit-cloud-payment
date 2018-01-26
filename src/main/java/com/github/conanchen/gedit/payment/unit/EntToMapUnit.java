package com.github.conanchen.gedit.payment.unit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class EntToMapUnit {

    public static  <T> Map<String,String> EntToMap(Object model, Class<T> t){
        Map<String,String> map = null;
        try{
            Field[] fields =  t.getDeclaredFields();
            if(fields.length > 0 && map == null)
                map = new HashMap<String,String>();
            for(Field f:fields){
                String name = f.getName();
                name = name.substring(0,1).toUpperCase()+name.substring(1); //将属性的首字符大写，方便构造get，set方法
                Method m = model.getClass().getMethod("get"+name);
                String value = String.valueOf(m.invoke(model));
                if(map!=null && value!=null && !value.equals("null"))
                    map.put(f.getName(), value);
                else
                    continue;
            }
            if(t.getSuperclass()!=null){
                EntToMap(model, t.getSuperclass());
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return map;
    }
}
