package com.gz.gamecity.login.sdkverify;

import java.util.concurrent.LinkedBlockingDeque;

import org.apache.log4j.Logger;

import com.gz.gamecity.login.LSMsgReceiver;
import com.gz.gamecity.login.PlayerMsgSender;
import com.gz.gamecity.login.config.ConfigField;
import com.gz.gamecity.protocol.Protocols;
import com.gz.util.Config;
import com.gz.util.HttpXmlClient;
import com.gz.websocket.msg.ClientMsg;

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
				url.append("?token=").append(msg.getJson().getString(Protocols.C2l_login.SDKTOKEN)).append("&");
				url.append("?uuid=").append(msg.getJson().getString(Protocols.C2l_login.UUID)).append("&");
				url.append("?appId=").append(Config.instance().getSValue(ConfigField.APPID));
				String result=HttpXmlClient.get(url.toString());

				System.out.println(result);
				if(result!=null && result.indexOf("status\":1")>0){
					verifySuc(msg);
				}else{
					verifyFailed(msg);
				}
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
	
	private void verifySuc(ClientMsg msg){
		msg.getJson().put(Protocols.SUBCODE, Protocols.Inner_login.subCode_value);
		LSMsgReceiver.getInstance().addMsg(msg);
	}
	
	private void verifyFailed(ClientMsg msg){
		msg.getJson().put(Protocols.SUBCODE, Protocols.L2c_login.subCode_value);
		msg.getJson().put(Protocols.ERRORCODE, "账号验证失败");
		PlayerMsgSender.getInstance().addMsg(msg);
	}
	
//	private void 

	
    
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
