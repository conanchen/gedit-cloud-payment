package com.github.conanchen.gedit.payment.config.alipay.alipayUnit;

import com.alipay.api.AlipayConstants;
import com.github.conanchen.gedit.payment.PaymentEnum.AliPayChannelsEnum;
import com.github.conanchen.gedit.payment.config.alipay.alipayConfig.AlipayConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by ZhouZeshao on 2018/1/15.
 */
@Component
public class AliPayUtil {

    @Autowired
    private AlipayConfig alipayConfig;

    public String payChannels(Integer [] array){
        List<Integer> list = Arrays.asList(array);
        StringBuffer stringBuffer = new StringBuffer();
        for (AliPayChannelsEnum e : AliPayChannelsEnum.values()) {
            if(list.contains(e.getCode())){
                stringBuffer.append(e.getChannel()).append(",");
            };
        }
        stringBuffer.deleteCharAt(stringBuffer.length() -1);
        return stringBuffer.toString();
    }

    public Map<String,String> builderAliPay(String notify_url){
        Map<String,String> map= new HashMap<String,String>();
        map.put("notify_url", notify_url);
        map.put("charset", "utf-8");
        map.put("method",  "alipay.trade.app.pay");
        map.put("format", AlipayConstants.FORMAT_JSON);
        SimpleDateFormat sy = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strDate= sy.format(new Date());
        map.put("timestamp", strDate);
        map.put("app_id", alipayConfig.app_id);
        map.put("version", "1.0");
        map.put("sign_type", "RSA");
//        map.put("extend_params",JSON.toJSONString(builderExtendParams()));
        return map;
    }

    /**
     * 是否需要实名认证
     * @param flag
     * @return
     */
    private Map<String,String> builderExtendParams(boolean flag){
        Map<String,String> jsonObject = new HashMap();
//        jsonObject.put("sys_service_provider_id","");
        jsonObject.put("needBuyerRealnamed",flag ? "T" : "F");
//        jsonObject.put("TRANS_MEMO","测试充值");
        return jsonObject;
    }

    public String createSign(Map<String,String> map){
        String content = AlipayCore.createLinkString(map);
        String string = RSA.sign(content, alipayConfig.private_key, "utf-8");
        map.put("sign",string);
       return AlipayCore.getSignEncodeUrl(map, true);
    }

}
