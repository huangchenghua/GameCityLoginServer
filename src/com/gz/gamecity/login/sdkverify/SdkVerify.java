package com.gz.gamecity.login.sdkverify;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.gz.gamecity.login.config.ConfigField;
import com.gz.gamecity.login.msg.ClientMsg;
import com.gz.gamecity.login.protocol.ProtocolsField;
import com.gz.util.Config;
import com.gz.util.HttpXmlClient;
import com.gz.websocket.msg.BaseMsg;

public class SdkVerify extends Thread{
	private static final Logger log=Logger.getLogger(SdkVerify.class);
	private static SdkVerify instance;
	
	private LinkedBlockingDeque<ClientMsg> queue=new LinkedBlockingDeque<ClientMsg>();
	
	public static synchronized SdkVerify getInstance() {
		if(instance==null)
			instance=new SdkVerify();
		return instance;
	}
	
	private SdkVerify(){
		
	}

	@Override
	public void run() {
		while(true){
			try {
				ClientMsg msg = queue.take();
				msg.sendSelf();
				StringBuffer url=new StringBuffer(Config.instance().getSValue(ConfigField.SDKVERIFYURL));
				url.append("?token=").append(msg.getJson().getString(ProtocolsField.C2l_login.TOKEN)).append("&");
				url.append("?uuid=").append(msg.getJson().getString(ProtocolsField.C2l_login.UUID)).append("&");
				url.append("?appId=").append(Config.instance().getSValue(ConfigField.APPID));
				String result=HttpXmlClient.get(url.toString());

				System.out.println(result);
//				String result = http(url, params);
				
			} catch (Exception e) {
				// TODO 这里如果发送异常就要与客户端断开连接
				e.printStackTrace();
			}
		}
	}
	
	public void addMsg(ClientMsg msg){
		try {
			queue.put(msg);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	

	
    
//	public static void main(String[] args) {
//		String url = "http://uc.hi7.cn/api/v1/user/login/validate/access_token";
//		Map<String, String> params=new HashMap<String,String>();
//		params.put("token", "e7555f29f22b56c113cca31ed37a2db0");
//		url = url+"?";
//		for (String key : params.keySet()) {
//			url=url+key+"="+params.get(key)+"&";
//		}
//		url=url.substring(0,url.length()-1);
//		String result=HttpXmlClient.get(url);
//		System.out.println(result);
//	}
}
