package com.github.conanchen.gedit.payment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by ZhouZeshao on 2018/1/15.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Points {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid",strategy = "uuid")
    @Column(columnDefinition = "char(32)")
    private String uuid;
    //用户的id 理论上来讲,每个用户这里只有一天数据
    @Column(columnDefinition = "char(32)")
    private String userId;
    //总共收入的积分数量
    @Column(columnDefinition = "integer(11)")
    private Integer rollIn;
    //中共支出积分的数量
    @Column(columnDefinition = "integer(11)")
    private Integer rollOut;
    //当前可用的积分数量
    @Column(columnDefinition = "integer(11)")
    private Integer usable;
    //当前冻结的积分数量
    @Column(columnDefinition = "integer(11)")
    private Integer freeze;
    @Column(columnDefinition = "bigInt(20)")
    @Version
    private Long version;
    //创建时间
    @Column(columnDefinition = "datetime")
    private Date createDate;
    //更新时间
    @Column(columnDefinition = "datetime")
    private Date updateDate;

}
