package com.github.conanchen.gedit.payment.PaymentEnum;

/**
 * Created by ZhouZeshao on 2017/11/17.
 */
public enum AliPayChannelsEnum {
    balance(1,"balance"),//余额支付
    moneyFund(2,"moneyFund"),//余额宝
    coupon(3,"coupon"),//红包
    pcredit(3,"pcredit"),//花呗
    pcreditpayInstallment(4,"pcreditpayInstallment"),//花呗分期
    creditCard(5,"creditCard"),//信用卡
    creditCardExpress(6,"creditCardExpress"),//信用卡快捷
    creditCardCartoon(7,"creditCardCartoon"),//信用卡卡通
    credit_group(8,"credit_group"),//信用支付类型（包含信用卡卡通、信用卡快捷、花呗、花呗分期）
    debitCardExpress(9,"debitCardExpress"),//借记卡快捷
    mcard(10,"mcard"),//商户预存卡
    pcard(11,"pcard"),//个人预存卡
    promotion(12,"promotion"),//优惠（包含实时优惠+商户优惠）
    voucher(13,"voucher"),//	营销券
    point(14,"point"),//积分
    mdiscount(15,"mdiscount"),//商户优惠
    bankPay(16,"bankPay");//	网银

    private Integer code;

   private String channel;

   AliPayChannelsEnum(Integer code, String channel){
       this.code = code;
       this.channel = channel;
   }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}
