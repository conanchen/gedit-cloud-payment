package com.github.conanchen.gedit.payment.PaymentEnum;

/**
 * Created by ZhouZeshao on 2018/1/15.
 */
public enum PaymentStatusEnum {
    NEW("0","创建"),
    INPROGRESS("50","INPROGRESS"),
    FAILED("51","FAILED"),
    OK("52","OK"),
    END("53","TRADEEND");

    private String code;

    private String msg;

    PaymentStatusEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
