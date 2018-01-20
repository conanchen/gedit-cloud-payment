package com.github.conanchen.gedit.payment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by ZhouZeshao on 2018/1/12.
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @Column(columnDefinition = "char(32)")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid")
    //订单id
    private String uuid;
    @Column(columnDefinition = "char(32)")
    //支付人id
    private String payerId;
    @Column(columnDefinition = "char(32)")
    //收款人id
    private String payeeId;
    @Column(columnDefinition = "char(32)")
    //收款门店id
    private String payeeStoreId;
    //收款店员id
    @Column(columnDefinition = "char(32)")
    private String payeeWorkerId;
    //应付金额
    @Column(columnDefinition = "integer(11)")
    private Integer shouldPay;
    //实付金额
    @Column(columnDefinition = "integer(11)")
    private Integer actualPay;
    //积分抵扣金额
    @Column(columnDefinition = "integer(11)")
    private Integer pointsPay;
    //返还积分
    @Column(columnDefinition = "integer(11)")
    private Integer points;
    @Column(columnDefinition = "char(6)")
    //支付状态
    private String status;
    //内部支付订单号
    @Column(columnDefinition = "varchar(32)")
    private String orderNo;

    //支付类型
    @Column(columnDefinition = "char(6)")
    private String channel;
    @Column(columnDefinition = "char(32)")
    //第三方支付订单号
    private String channelOrderId;
    @Column(columnDefinition = "datetime")
    private Date createDate;
    @Column(columnDefinition = "datetime")
    private Date updateDate;
}
