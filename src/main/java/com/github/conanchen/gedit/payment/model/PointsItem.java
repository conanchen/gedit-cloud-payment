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
 * Created by ZhouZeshao on 2018/1/16.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PointsItem {

    @Id
    @GenericGenerator(name = "uuid",strategy = "uuid")
    @GeneratedValue(generator = "uuid")
    @Column(columnDefinition = "char(32)")
    private String uuid;

    @Column(columnDefinition = "char(32)  comment '用户的id'")
    private String userId;

    @Column(columnDefinition = "integer(4) comment '积分类型,1消费2获得'")
    private Integer type;

    @Column(columnDefinition = "integer(11) comment '该笔积分的数量.如果是支出则存入负值'")
    private Integer points;

    @Column(columnDefinition = "char(20) comment '备注,不能大于20字'")
    private String remark;

    @Column(columnDefinition = "char(20)")
    private String orderNo;
    @Column(columnDefinition = "char(6)")
    private String status;

    @Column(columnDefinition = "datetime")
    private Date createDate;
    @Column(columnDefinition = "datetime")
    private Date updateDate;


}
