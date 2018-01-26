package com.github.conanchen.gedit.payment.config.alipay.alipayUnit;

import com.alipay.api.AlipayConstants;
import com.github.conanchen.gedit.payment.config.alipay.alipayConfig.AlipayConfig;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;



/* *
 *类名：AlipayFunction
 *功能：支付宝接口公用函数类
 *详细：该类是请求、通知返回两个文件所调用的公用函数核心处理文件，不需要修改
 *版本：3.3
 *日期：2012-08-14
 */

public class AlipayCore {
    /** 
     * 除去数组中的空值和签名参数
     * @param sArray 签名参数组
     * @return 去掉空值与签名参数后的新签名参数组
     */
    public static Map<String, String> paraFilter(Map<String, String> sArray) {

        Map<String, String> result = new HashMap<String, String>();

        if (sArray == null || sArray.size() <= 0) {
            return result;
        }

        for (Map.Entry<String, String> entry: sArray.entrySet()) {
            String value = entry.getValue();
            String key  =entry.getKey();
            if (value == null || value.equals("") || key.equalsIgnoreCase("sign")
                || key.equalsIgnoreCase("sign_type")) {
                continue;
            }
            result.put(key, value);
        }

        return result;
    }

    /** 
     * 把数组所有元素排序，并按照“参数=参数值”的模式用“&”字符拼接成字符串
     * @param params 需要排序并参与字符拼接的参数组
     * @return 拼接后字符串
     */
    public static String createLinkString(Map<String, String> params) {

        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);

        String prestr = "";

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);

            if (i == keys.size() - 1) {//拼接时，不包括最后一个&字符
                prestr = prestr + key + "=" + value;
            } else {
                prestr = prestr + key + "=" + value + "&";
            }
        }

        return prestr;
    }



    
    public String signAllString(String [] array){
    	   StringBuffer sb = new StringBuffer("");
    	   for (int i = 0; i < array.length; i++) {
    	      if(i==(array.length-1)){
    	         sb.append(array[i]);
    	      }else{
    	         sb.append(array[i]+"&");
    	      }
    	   }
    	   System.out.println(sb.toString());
    	   String sign = "";
    	   try {
    	      sign = URLEncoder.encode(RSA.sign(sb.toString(), AlipayConfig.private_key, "utf-8"), "utf-8");//private_key私钥
    	   } catch (UnsupportedEncodingException e) {
    	      e.printStackTrace();
    	   }
    	   sb.append("&sign=\""+sign+"\"&");
    	   sb.append("sign_type=\"RSA\"");
    	   return sb.toString();
    	}
    
    public static String getSignEncodeUrl(Map<String, String> map, boolean isEncode) {
		String sign = map.get("sign");
		String encodedSign = "";
		if (!map.isEmpty()||map.size()>0) {
			map.remove("sign");
			List<String> keys = new ArrayList<String>(map.keySet());
			// key排序
			Collections.sort(keys);

			StringBuilder authInfo = new StringBuilder();

			boolean first = true;// 是否第一个
			for (String key: keys) {
				if (first) {
					first = false;
				} else {
					authInfo.append("&");
				}
				authInfo.append(key).append("=");
				if (isEncode) {
					try {
						authInfo.append(URLEncoder.encode(map.get(key), AlipayConstants.CHARSET_UTF8));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				} else {
					authInfo.append(map.get(key));
				}
			}

			try {
				encodedSign = authInfo.toString() + "&sign=" + URLEncoder.encode(sign, AlipayConstants.CHARSET_UTF8);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		return encodedSign.replaceAll("\\+", "%20");
	}


}
