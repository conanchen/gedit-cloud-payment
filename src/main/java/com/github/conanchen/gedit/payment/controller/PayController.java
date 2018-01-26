package com.github.conanchen.gedit.payment.controller;

import com.github.conanchen.gedit.payment.service.PayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by ZhouZeshao on 2018/1/16.
 */
@RestController
public class PayController {

    @Autowired
    private PayService payService;

    @RequestMapping(value = "aliPay/notify",method = RequestMethod.POST)
    public void aliPayNotify(HttpServletRequest request, HttpServletResponse response)throws IOException {
        payService.aliPayNotify(request,response);
    }

    @RequestMapping(value = "wxPay/notify",method = RequestMethod.POST)
    public void wxPayNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {
        payService.wxPayNotify(request,response);
    }


}
