package com.github.conanchen.gedit.payment.config.wxpay.weixinPayUtil;

import com.github.conanchen.gedit.payment.config.wxpay.weixinPayConfig.WeixinPayConfig;
import com.github.wxpay.sdk.WXPayConfig;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by ZhouZeshao on 2018/1/16.
 */
@Component
public class WXPAySign {

    @Autowired
    private WXPayConfig wxPayConfig;

    public Map<String,String> createMapToSign(String body,String orderNo,String shouldPay,String spbillCreateIp,String notifyUrl,String tradeType){
        Map<String, String> data = new HashMap<String, String>();
        data.put("body",body);
        data.put("out_trade_no", orderNo);
        data.put("fee_type", "CNY");
        data.put("total_fee", shouldPay);
        data.put("spbill_create_ip", spbillCreateIp);
        data.put("notify_url", notifyUrl);
        data.put("trade_type", tradeType);  // 此处指定为APP支付
        return data;
    }
    public String WXPAY2Sign(String prepayid, SortedMap map_weixin) throws Exception {
        map_weixin.put("prepayid", prepayid);
        map_weixin.put("partnerid", wxPayConfig.getMchID());
        map_weixin.put("appid", wxPayConfig.getAppID());
        map_weixin.put("package", "Sign=WXPay");
        map_weixin.put("noncestr", WXPayUtil.generateNonceStr());
        map_weixin.put("timestamp", String.valueOf(System.currentTimeMillis()).substring(0, 10));
        return WXPayUtil.generateSignature(map_weixin,wxPayConfig.getKey());
    }
}
