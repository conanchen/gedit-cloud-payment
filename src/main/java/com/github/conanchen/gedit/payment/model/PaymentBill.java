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
 * 任何与第三方产生且已经完成的实际支付都将在这里产生一笔账单！
 * 这里记录的账单是所有经过平台的进入或者支出记录
 * 如果是同一笔交易产生了多次经过平台的出入帐,那么应该进行分开记录
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentBill {
    @Id
    @Column(columnDefinition = "char(32)")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid")
    private String uuid;

    @Column(columnDefinition = "varchar(20) comment 平台内部的订单号,需要与交易订单号对应")
    private String orderNo;
    @Column(columnDefinition = "varchar(32) comment 对外的第三方订单号")
    private String channelOrderId;
    //该笔账单的类型 1支出，2收入
    @Column(columnDefinition = "integer(11) comment 金额收入为+支出为-")
    private Integer amount;
    @Column(columnDefinition = "integer(11) comment 类型 该笔交易实在那种情况下产生的")
    private Integer type;
    @Column(columnDefinition = "datetime")
    private Date createDate;

}
