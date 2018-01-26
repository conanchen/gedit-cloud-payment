package com.github.conanchen.gedit.payment.config.alipay.alipayConfig;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Component;

/* *
 *类名：AlipayConfig
 *功能：基础配置类
 *详细：设置帐户有关信息及返回路径
 *版本：3.3
 *日期：2017-06-02
	
 *提示：如何获取安全校验码和合作身份者ID
 *1.用您的签约支付宝账号登录支付宝网站(www.alipay.com)
 *2.点击“商家服务”(https://b.alipay.com/order/myOrder.htm)
 *3.点击“查询合作者身份(PID)”、“查询安全校验码(Key)”

 *安全校验码查看时，输入支付密码后，页面呈灰色的现象，怎么办？
 *解决方法：
 *1、检查浏览器配置，不让浏览器做弹框屏蔽设置
 *2、更换浏览器或电脑，重新登录查询。
 */
@PropertySources(
		{@PropertySource(value = "config/alipay.properties")}
)
@Component
public class AlipayConfig {

	//↓↓↓↓↓↓↓↓↓↓请在这里配置您的基本信息↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
	// 合作身份者ID，以2088开头由16位纯数字组成的字符串
	@Value("${partner}")
	public String partner;
	// 商户的私钥
	@Value("${private_key}")
	public String private_key;
	
	// 支付宝的公钥，无需修改该值
	@Value("${ali_public_key}")
	public String ali_public_key;

	@Value("${app_id}")
	public String app_id;
	//↑↑↑↑↑↑↑↑↑↑请在这里配置您的基本信息↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑


	// 调试用，创建TXT日志文件夹路径
	public static final String log_path = "D:\\";

	// 字符编码格式 目前支持 gbk 或 utf-8
	public static final String input_charset = "utf-8";
	
	// 签名方式 不需修改
	public static final String sign_type = "RSA";
//	public static final String service = "create_direct_pay_by_user";
	// 收款支付宝账号，以2088开头由16位纯数字组成的字符串，一般情况下收款账号就是签约账号
	@Value("${partner}")
	public String seller_id;
	public static final String payment_type = "1";
	
	
	
	// 服务器异步通知页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
//	public static String notify_url = "http://114.55.7.206:9080/api/alipayUrl";

	// 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
//	public static String return_url = "http://return_url.jsp";
	
	//↑↑↑↑↑↑↑↑↑↑请在这里配置您的基本信息↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
	
	//↓↓↓↓↓↓↓↓↓↓ 请在这里配置防钓鱼信息，如果没开通防钓鱼功能，为空即可 ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
		
		// 防钓鱼时间戳  若要使用请调用类文件submit中的query_timestamp函数
		public static String anti_phishing_key = "";
		
		// 客户端的IP地址 非局域网的外网IP地址，如：221.0.0.1
		public static String exter_invoke_ip = "";
		// MD5密钥，安全检验码，由数字和字母组成的32位字符串，查看地址：https://b.alipay.com/order/pidAndKey.htm
//		public static String key = "";
			
	//↑↑↑↑↑↑↑↑↑↑请在这里配置防钓鱼信息，如果没开通防钓鱼功能，为空即可 ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑

}
