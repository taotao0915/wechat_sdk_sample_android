package com.longjun.moneyzz;


import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import org.json.JSONObject;

import com.longjun.moneyzz.uikit.ConstantUtil;
import com.longjun.moneyzz.uikit.WXRequestUtil;
import com.tencent.mm.opensdk.constants.Build;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class PayActivity extends Activity {

//	private IWXAPI api;
	private Map<String,String> mMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pay);

//		api = WXAPIFactory.createWXAPI(this, ConstantUtil.APP_ID);

		Button appayBtn = (Button) findViewById(R.id.appay_btn);
		appayBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
                IWXAPI msgApi = WXAPIFactory.createWXAPI(PayActivity.this, null);
                // 将该app注册到微信
                msgApi.registerApp(ConstantUtil.APP_ID);
				mMap = WXRequestUtil.sendPayment("商城-订单支付", genOutTradNo(), 0.01);
				Log.i("TAG","统一下单返回信息:"+mMap.toString());
				Button payBtn = (Button) findViewById(R.id.appay_btn);
				payBtn.setEnabled(false);
				Toast.makeText(PayActivity.this, "获取订单中...", Toast.LENGTH_SHORT).show();
				if(mMap!=null && mMap.size()>0){
					if(mMap.get("return_code").equals("SUCCESS")&&mMap.get("result_code").equals("SUCCESS")){
						PayReq req = new PayReq();
						req.appId			= ConstantUtil.APP_ID;
						req.partnerId		= ConstantUtil.MCH_ID;//商户号
						req.prepayId		= mMap.get("prepay_id");
						req.nonceStr		= WXRequestUtil.NonceStr();
						req.timeStamp		= String.valueOf(System.currentTimeMillis()/1000);
						req.packageValue	= "Sign=WXPay";
						req.sign			= WXRequestUtil.getSignBeforePay(req.appId,req.partnerId,req.prepayId,req.packageValue,req.nonceStr,req.timeStamp);
//						req.sign           = mMap.get("sign");
						Toast.makeText(PayActivity.this, "正常调起支付", Toast.LENGTH_SHORT).show();
						Log.i("TAG", "调起支付时的sign值:"+req.sign);
						Log.i("TAG","检查你请求参数是否有误:"+req.checkArgs()+"");//这是检查你请求参数是否有误，如果打印是false，请检查你的请求参数是否是上面的几个
						Log.i("TAG",msgApi.sendReq(req)+""); //这表示能够调起api，一般上面那个成功，这个没有问题。两个Log如果打印都是true，恭喜你离成功不远了
						// 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
//                        msgApi.sendReq(req);
					}else{
						Toast.makeText(PayActivity.this, mMap.get("return_msg"), Toast.LENGTH_SHORT).show();
					}
				}

//		        try{
//					byte[] buf = Util.httpGet(url);
//					if (buf != null && buf.length > 0) {
//						String content = new String(buf);
//						Log.e("get server pay params:",content);
//			        	JSONObject json = new JSONObject(content);
//						if(null != json && !json.has("retcode") ){
//							PayReq req = new PayReq();
//							//req.appId = "wxf8b4f85f3a794e77";  // 测试用appId
//							req.appId			= ConstantUtil.APP_ID;
////							req.partnerId		= json.getString("partnerid");
//							req.partnerId		= ConstantUtil.MCH_ID;//商户号
//							req.prepayId		= json.getString("prepayid");
//							req.nonceStr		= json.getString("noncestr");
//							req.timeStamp		= json.getString("timestamp");
//							req.packageValue	= json.getString("package");
//							req.sign			= json.getString("sign");
//							req.extData			= "app data"; // optional
//							Toast.makeText(PayActivity.this, "正常调起支付", Toast.LENGTH_SHORT).show();
//							// 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
//							api.sendReq(req);
//						}else{
//				        	Log.d("PAY_GET", "返回错误"+json.getString("retmsg"));
//				        	Toast.makeText(PayActivity.this, "返回错误"+json.getString("retmsg"), Toast.LENGTH_SHORT).show();
//						}
//					}else{
//			        	Log.d("PAY_GET", "服务器请求错误");
//			        	Toast.makeText(PayActivity.this, "服务器请求错误", Toast.LENGTH_SHORT).show();
//			        }
//		        }catch(Exception e){
//		        	Log.e("PAY_GET", "异常："+e.getMessage());
//		        	Toast.makeText(PayActivity.this, "异常："+e.getMessage(), Toast.LENGTH_SHORT).show();
//		        }
				payBtn.setEnabled(true);
			}
		});
		Button checkPayBtn = (Button) findViewById(R.id.check_pay_btn);
//		checkPayBtn.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				boolean isPaySupported = api.getWXAppSupportAPI() >= Build.PAY_SUPPORTED_SDK_INT;
//				Toast.makeText(PayActivity.this, String.valueOf(isPaySupported), Toast.LENGTH_SHORT).show();
//			}
//		});
	}

	//生成订单号,测试用，在客户端生成
	private String genOutTradNo() {
		Random random = new Random();
//		return "dasgfsdg1234"; //订单号写死的话只能支付一次，第二次不能生成订单
		return MD5.getMessageDigest(String.valueOf(random.nextInt(10000)));
	}

}
