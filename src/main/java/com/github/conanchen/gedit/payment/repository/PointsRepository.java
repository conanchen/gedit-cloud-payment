package com.github.conanchen.gedit.payment.repository;

import com.github.conanchen.gedit.payment.model.Points;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;

/**
 * Created by ZhouZeshao on 2018/1/15.
 */
public interface PointsRepository<T,String extends Serializable> extends JpaRepository<Points,String> {

    Points findByUserId(String  userId);

    @Modifying
    @Transactional
    @Query("update Points set usable = ?1 where uuid = ?2 and version = ?3")
    int updateByVersion(int usable,String uuid,Long version);

}
