package com.github.conanchen.gedit.payment.repository;

import com.github.conanchen.gedit.payment.model.Payment;
import com.github.conanchen.gedit.payment.model.Points;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.io.Serializable;

/**
 * Created by ZhouZeshao on 2018/1/15.
 */
public interface PaymentRepository<T,String extends Serializable> extends JpaRepository<Payment,String>,JpaSpecificationExecutor<Payment> {

    Payment findByOrderNo(String orderNo);

}
