package com.github.conanchen.gedit.payment.config.alipay.alipayUnit;

/**
 * Created by ZhouZeshao on 2018/1/15.
 *
 * 发起支付宝支付请求的业务请求参数,这里的参数仅仅是基本信息参数,如果需要更多的参数请在这里追加.
 * 为了entity能够成功的装换为map,请无比增加get,set
 */
public class AliPayRequest {

    private String total_amount;

    private String subject;

    private String product_code = "QUICK_MSECURITY_PAY";

    private String body;

    private String out_trade_no;
    //订单的最长确认支付时间,默认为15d,可用数值为1m~15d,(m-分钟,h-小时,d-天,1c-当天)该值不支持小数.例如1.5h请转换为90m
    //如果值为1c,那么订单将在当天对外的0点关闭.
    private String timeout_express;
    // 商品的类型,目前支持两种 0:虚拟类物品,(如果使用该类型,那么将无法使用花呗支付) 1:实物类
    private String goods_type;
    // 优惠参数,该参数需要先与支付宝协商
    private String promo_params;


    public AliPayRequest(String total_amount, String subject, String out_trade_no,String body) {
        this.total_amount = total_amount;
        this.subject = subject;
        this.out_trade_no = out_trade_no;
        this.body = body;
    }

    public AliPayRequest(String total_amount, String subject, String body, String out_trade_no, String timeout_express, String goods_type) {
        this.total_amount = total_amount;
        this.subject = subject;
        this.body = body;
        this.out_trade_no = out_trade_no;
        this.timeout_express = timeout_express;
        this.goods_type = goods_type;
    }

    public AliPayRequest(String total_amount, String subject, String body, String out_trade_no, String timeout_express, String goods_type, String promo_params) {
        this.total_amount = total_amount;
        this.subject = subject;
        this.body = body;
        this.out_trade_no = out_trade_no;
        this.timeout_express = timeout_express;
        this.goods_type = goods_type;
        this.promo_params = promo_params;
    }

    public String getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(String total_amount) {
        this.total_amount = total_amount;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }


    public String getProduct_code() {
        return product_code;
    }

    public void setProduct_code(String product_code) {
        this.product_code = product_code;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getOut_trade_no() {
        return out_trade_no;
    }

    public void setOut_trade_no(String out_trade_no) {
        this.out_trade_no = out_trade_no;
    }

    public String getTimeout_express() {
        return timeout_express;
    }

    public void setTimeout_express(String timeout_express) {
        this.timeout_express = timeout_express;
    }

    public String getGoods_type() {
        return goods_type;
    }

    public void setGoods_type(String goods_type) {
        this.goods_type = goods_type;
    }

    public String getPromo_params() {
        return promo_params;
    }

    public void setPromo_params(String promo_params) {
        this.promo_params = promo_params;
    }
}
