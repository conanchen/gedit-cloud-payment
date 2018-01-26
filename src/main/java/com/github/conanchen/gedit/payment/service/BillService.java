package com.github.conanchen.gedit.payment.service;

import com.github.conanchen.gedit.payment.PaymentEnum.BillEnum;
import com.github.conanchen.gedit.payment.model.PaymentBill;
import com.github.conanchen.gedit.payment.repository.PaymentBillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class BillService {

    @Autowired
    private PaymentBillRepository billRepository;

    public void addPaymentBill(Integer actualPay,String  channelId,String orderNo,BillEnum billtype){
        PaymentBill.PaymentBillBuilder paymentBillBuilder = PaymentBill.builder();
        paymentBillBuilder.amount(actualPay);
        paymentBillBuilder.channelOrderId(channelId);
        paymentBillBuilder.orderNo(orderNo);
        paymentBillBuilder.type(billtype.getCode());
        paymentBillBuilder.createDate(new Date());
        billRepository.save(paymentBillBuilder.build());
    }
}
