package com.github.conanchen.gedit.payment.config.wxpay.weixinPayConfig;

import com.github.wxpay.sdk.WXPayConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import java.io.InputStream;

/**
 * @author  ZhouZeshao
 * @date 2017-5-31
 * 微信支付工具类,如无需要请不要修改
 * Created by ZhouZeshao on 2017/5/31.
 */

@PropertySources(
        {@PropertySource(value = "config/wxpay.properties")}
)
public class WeixinPayConfig implements WXPayConfig{



    @Override
    public String getAppID() {
        return appid;
    }

    @Override
    public String getMchID() {
        return mch_id;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public InputStream getCertStream() {
        return null;
    }

    @Override
    public int getHttpConnectTimeoutMs() {
        return 8000;
    }

    @Override
    public int getHttpReadTimeoutMs() {
        return 10000;
    }

    public String getNotify_url(){return notify_url;}
    @Value("${appid}")
    public String  appid;//appid
    @Value("${mch_id}")
    public String  mch_id;//商户id
    @Value("${notify_url}")
    public String notify_url;//支付完成后回调地址
    @Value("${trade_type}")
    public String trade_type;//支付方式
    @Value("${key}")
    public String key;

    public static final  String wUrl ="https://api.mch.weixin.qq.com/pay/unifiedorder";//预定单生成访问地址

    public static final  String post_method ="POST";//预定单生成借口调用方式--post

    public static final String get_method ="GET";//预定单生成借口调用方式--get
}
