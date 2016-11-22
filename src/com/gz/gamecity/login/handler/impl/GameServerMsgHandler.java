package com.gz.gamecity.login.handler.impl;


import com.gz.gamecity.login.LSMsgReceiver;
import com.gz.websocket.msg.ProtocolMsg;
import com.gz.websocket.protocol.server.ProtocolServerMsgHandler;

import io.netty.channel.Channel;

public class GameServerMsgHandler implements ProtocolServerMsgHandler {

	@Override
	public void onMsgReceived(ProtocolMsg msg) {
		msg.parse();
		LSMsgReceiver.getInstance().addMsg(msg);
		
	}

	@Override
	public void onSessionClosed(Channel channel) {
		// TODO Auto-generated method stub
		
	}


}
