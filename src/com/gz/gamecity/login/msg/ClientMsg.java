package com.gz.gamecity.login.msg;

import com.alibaba.fastjson.JSONObject;
import com.gz.websocket.msg.BaseMsg;

public class ClientMsg extends BaseMsg {
	private JSONObject json;
	
	
	public JSONObject getJson() {
		return json;
	}


	public void setJson(JSONObject json) {
		this.json = json;
	}


	public void parse(){
		json = JSONObject.parseObject(content);
		mainCode = json.getIntValue("mainCode");
	}
	
	public ClientMsg(BaseMsg msg){
		this.channel =msg.getChannel();
		this.content = msg.getContent();
		
	}
	
}
