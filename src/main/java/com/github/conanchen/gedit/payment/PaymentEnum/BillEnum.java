package com.github.conanchen.gedit.payment.PaymentEnum;

public enum BillEnum {
    CONSUME(1,"顾客买单收入"),
    WITHDRAWAL(2,"门店用户体现");

    private Integer code;

    private String msg;

    BillEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
