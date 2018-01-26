package com.github.conanchen.gedit.payment.repository;

import com.github.conanchen.gedit.payment.model.PaymentBill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serializable;

public interface PaymentBillRepository<T,String extends Serializable> extends JpaRepository<PaymentBill,String> {

}
