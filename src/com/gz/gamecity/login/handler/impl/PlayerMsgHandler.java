package com.gz.gamecity.login.handler.impl;



import com.gz.gamecity.login.LSMsgReceiver;
import com.gz.gamecity.login.msg.ClientMsg;
import com.gz.websocket.msg.BaseMsg;
import com.gz.websocket.server.ServerMsgHandler;

import io.netty.channel.Channel;


public class PlayerMsgHandler implements ServerMsgHandler{

	@Override
	public void onMsgReceived(BaseMsg msg) {
		ClientMsg cMsg=new ClientMsg(msg);
		cMsg.parse();
		LSMsgReceiver.getInstance().addMsg(cMsg);
	}

	@Override
	public void onSessionClosed(Channel channel) {
		
		
	}


}
