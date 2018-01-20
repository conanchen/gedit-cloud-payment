package com.github.conanchen.gedit.payment.repository;

import com.github.conanchen.gedit.payment.model.PointsItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serializable;

/**
 * Created by ZhouZeshao on 2018/1/16.
 */
public interface PointsItemRepository<T,String extends Serializable> extends JpaRepository<PointsItem,String> {

    PointsItem findByOrderNo(String orderNo);
}
