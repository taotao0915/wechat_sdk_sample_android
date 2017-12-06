package com.longjun.moneyzz.uikit;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.security.KeyStore;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;


import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import android.util.Base64;
import android.util.Log;

import com.longjun.moneyzz.MD5;
import com.longjun.moneyzz.Util;

/* 
 * 用户发起统一下单请求
 * 作者：董志平
 */
public class WXRequestUtil {
    /*
     * 发起支付请求
     * body 商品描述
     * out_trade_no 订单号
     * total_fee    订单金额        单位  元
     * product_id   商品ID
     */
    public static Map<String,String> sendPayment(String body,String out_trade_no,double total_fee){
        String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
        String xml = WXParamGenerate(body,out_trade_no,total_fee);
        String res = httpsRequest(url,"POST",xml);

        Map<String,String> data = null;
        try {
            data = doXMLParse(res);
        } catch (Exception e) {
        }
        return data;
    }

    public static String NonceStr(){
        String res = Base64Util.getBase64(Math.random()+"::"+new Date().toString()).substring(0, 30);
        return res;
    }

    public static String GetIp() {
        InetAddress ia=null;
        try {
            ia=InetAddress.getLocalHost();
            String localip=ia.getHostAddress();
            return localip;
        } catch (Exception e) {
            return null;
        }
    }

    public static String GetSign(Map<String,String> param){
        String StringA =  Util.formatUrlMap(param, false, false);
        String stringSignTemp = MD5.getMessageDigest(StringA+"&key="+ConstantUtil.API_KEY).toUpperCase();
        Log.i("TAG","stringA="+StringA);
        Log.i("TAG","stringSignTemp="+stringSignTemp);
        Log.i("TAG","校验sign值前的xml="+GetMapToXML(param));
        return stringSignTemp;
    }

    //Map转xml数据
    public static String GetMapToXML(Map<String,String> param){
        StringBuffer sb = new StringBuffer();
        sb.append("<xml>");
        for (Map.Entry<String,String> entry : param.entrySet()) {
            sb.append("<"+ entry.getKey() +">");
            sb.append(entry.getValue());
            sb.append("</"+ entry.getKey() +">");
        }
        sb.append("</xml>");
        Log.i("TAG", "Map转xml数据:"+sb.toString());
        return sb.toString();
    }


    //微信统一下单参数设置
    public static String WXParamGenerate(String description,String out_trade_no,double total_fee){
        int fee = (int)(total_fee * 100.00);
        Map<String,String> param = new HashMap<String,String>();
        param.put("appid", ConstantUtil.APP_ID);
        param.put("mch_id", ConstantUtil.MCH_ID);
        param.put("nonce_str",NonceStr());
        param.put("body", description);
        param.put("out_trade_no",out_trade_no);
        param.put("total_fee", fee+"");
        param.put("spbill_create_ip", GetIp());
        param.put("notify_url", ConstantUtil.WEIXIN_NOTIFY);
        param.put("trade_type", "APP");

        String sign = GetSign(param);

        param.put("sign", sign);
        return GetMapToXML(param);
    }

    //调起支付再获取sign值
    public static String getSignBeforePay(String appId,String partnerId,String prepayId,String packageValue ,String nonceStr,String timeStamp){
        Map<String,String> param = new HashMap<String,String>();
        param.put("appid", appId);
        param.put("partnerid", partnerId);
        param.put("prepayid",prepayId);
        param.put("package", packageValue);
        param.put("noncestr",nonceStr);
        param.put("timestamp", timeStamp);
        String sign = GetSign(param);

        return sign;
    }

    //发起微信支付请求
    public static String httpsRequest(String requestUrl, String requestMethod, String outputStr) {
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            // 设置请求方式（GET/POST）
            conn.setRequestMethod(requestMethod);
            conn.setRequestProperty("content-type", "application/x-www-form-urlencoded");
            // 当outputStr不为null时向输出流写数据
            if (null != outputStr) {
                OutputStream outputStream = conn.getOutputStream();
                // 注意编码格式
                outputStream.write(outputStr.getBytes("UTF-8"));
                outputStream.close();
            }
            // 从输入流读取返回内容
            InputStream inputStream = conn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String str = null;
            StringBuffer buffer = new StringBuffer();
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            // 释放资源
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
            inputStream = null;
            conn.disconnect();
            return buffer.toString();
        } catch (ConnectException ce) {
            System.out.println("连接超时：{}"+ ce);
        } catch (Exception e) {
            System.out.println("https请求异常：{}"+ e);
        }
        return null;
    }



    //xml解析
    public static Map<String, String> doXMLParse(String strxml) throws Exception {
        strxml = strxml.replaceFirst("encoding=\".*\"", "encoding=\"UTF-8\"");
        if(null == strxml || "".equals(strxml)) {
            return null;
        }

        Map<String,String> m = new HashMap<String,String>();
        InputStream in = new ByteArrayInputStream(strxml.getBytes("UTF-8"));
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(in);
        Element root = doc.getRootElement();
        List list = root.getChildren();
        Iterator it = list.iterator();
        while(it.hasNext()) {
            Element e = (Element) it.next();
            String k = e.getName();
            String v = "";
            List children = e.getChildren();
            if(children.isEmpty()) {
                v = e.getTextNormalize();
            } else {
                v = getChildrenText(children);
            }

            m.put(k, v);
        }

        //关闭流
        in.close();
        return m;
    }

    public static String getChildrenText(List children) {
        StringBuffer sb = new StringBuffer();
        if(!children.isEmpty()) {
            Iterator it = children.iterator();
            while(it.hasNext()) {
                Element e = (Element) it.next();
                String name = e.getName();
                String value = e.getTextNormalize();
                List list = e.getChildren();
                sb.append("<" + name + ">");
                if(!list.isEmpty()) {
                    sb.append(getChildrenText(list));
                }
                sb.append(value);
                sb.append("</" + name + ">");
            }
        }
        return sb.toString();
    }
}  