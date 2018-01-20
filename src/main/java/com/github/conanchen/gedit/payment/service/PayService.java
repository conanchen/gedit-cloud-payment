package com.github.conanchen.gedit.payment.service;

import com.github.conanchen.gedit.payment.PaymentEnum.PaymentStatusEnum;
import com.github.conanchen.gedit.payment.config.alipay.alipayConfig.AlipayConfig;
import com.github.conanchen.gedit.payment.config.alipay.alipayUnit.AlipayCore;
import com.github.conanchen.gedit.payment.config.alipay.alipayUnit.RSA;
import com.github.conanchen.gedit.payment.config.wxpay.weixinPayConfig.WeixinPayConfig;
import com.github.conanchen.gedit.payment.model.Payment;
import com.github.conanchen.gedit.payment.model.PointsItem;
import com.github.conanchen.gedit.payment.repository.PaymentRepository;
import com.github.conanchen.gedit.payment.repository.PointsItemRepository;
import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConfig;
import com.github.wxpay.sdk.WXPayUtil;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by ZhouZeshao on 2018/1/16.
 */
@Service

public class PayService {
    private static final Logger log = LoggerFactory.getLogger(PayService.class);
    private static Gson gson = new Gson();
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private PointsItemRepository itemRepository;

    public void aliPayNotify(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("支付宝异步通知开始");
        String flagStr = "success";
        Map<String,String> requestMap = parseAliPayRequest(request);
        log.info("支付宝异步通知参数:{}",gson.toJson(requestMap));
        String returnSign = requestMap.get("sign");
        log.info("支付宝异步通知签名:{}",returnSign);
        requestMap.remove("sign");//将sign和sign_type从参数中移除,因为验证sign的时候该两个值不参与
        requestMap.remove("sign_type");
        String  requestStr = AlipayCore.createLinkString(requestMap);
        if(RSA.verify(requestStr,returnSign, AlipayConfig.ali_public_key,"utf-8")){
            String orderNo = requestMap.get("out_trade_no");
            if(requestMap.get("trade_status").equals("TRADE_SUCCESS")){
                String channelId  = requestMap.get("trade_no");
                updatePaymentAndItem(orderNo,channelId);
            }else if(requestMap.get("trade_status").equals("TRADE_FINISHED")){
                updateTradeFinish(orderNo);
            }
            PrintWriter out=response.getWriter();
            out.print(flagStr);
            out.flush();//请不要修改或删除
            log.info("支付宝异步通知处理完毕:{}",flagStr);
        }else{
            flagStr = "fail";
            PrintWriter out = response.getWriter();
            out.print(flagStr);
            out.flush();
            log.info("支付宝异步通知处理完毕:{}",flagStr);
        }
    }

    public void wxPayNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.info("微信同步通知开始");
        WeixinPayConfig config = new WeixinPayConfig();
        WXPay wxpay = new WXPay(config);
        String resXml = "";
        String xmlStr = getWXPayXml(request);
        Map<String, String> notifyMap = WXPayUtil.xmlToMap(xmlStr);  // 转换成map
        if (wxpay.isPayResultNotifySignatureValid(notifyMap)) {
            String orderNo = notifyMap.get("out_trade_no");
            String transactionId = notifyMap.get("transaction_id");
            updatePaymentAndItem(orderNo,transactionId);
            resXml = "<xml><return_code><![CDATA[SUCCESS]]></return_code>"
                    + "<return_msg><![CDATA[OK]]></return_msg></xml> ";
            log.info("微信同步通知成功:{}","success");
        }else{
            log.info("支付失败,错误信息：参数错误");
            resXml = "<xml><return_code><![CDATA[FAIL]]></return_code>"
                    + "<return_msg><![CDATA[参数错误]]></return_msg></xml> ";
            log.info("微信同步通知失败:{}","fail");
        }
        BufferedOutputStream out = new BufferedOutputStream(
                response.getOutputStream());
        out.write(resXml.getBytes());
        out.flush();
        out.close();
        log.info("微信同步通知完成");
    }

    public Map<String,String> parseAliPayRequest(HttpServletRequest request){
        Map<String, String> params = new HashMap();
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
            params.put(name, valueStr);
        }
        return params;
    }

    private void updatePaymentAndItem(String orderNo,String channelId){
        Payment payment = paymentRepository.findByOrderNo(orderNo);
        if(payment != null){
            if(payment.getStatus().equals(PaymentStatusEnum.NEW.getCode())){
                payment.setStatus(PaymentStatusEnum.INPROGRESS.getCode());
                payment.setChannelOrderId(channelId);
                paymentRepository.save(payment);
                PointsItem pointsItem = itemRepository.findByOrderNo(orderNo);
                pointsItem.setStatus(PaymentStatusEnum.INPROGRESS.getCode());
                itemRepository.save(pointsItem);
            }
        }
    }

    private void updateTradeFinish(String orderNo){
        Payment payment = paymentRepository.findByOrderNo(orderNo);
        if(payment != null){
            payment.setStatus(PaymentStatusEnum.OK.getCode());
            paymentRepository.save(payment);
        }
    }

    private String getWXPayXml(HttpServletRequest request) throws IOException {
        StringBuffer sb = new StringBuffer();
        InputStream inputStream = request.getInputStream();
        String s ;
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        while ((s = in.readLine()) != null){
            sb.append(s);
        }
        in.close();
        inputStream.close();
        return sb.toString();
    }
}
