package com.gz.gamecity.login.handler.impl;


import org.apache.log4j.Logger;

import com.gz.gamecity.login.LSMsgReceiver;
import com.gz.gamecity.login.bean.GameServer;
import com.gz.websocket.msg.ProtocolMsg;
import com.gz.websocket.protocol.server.ProtocolServerMsgHandler;

import io.netty.channel.Channel;
import io.netty.util.Attribute;

public class GameServerMsgHandler implements ProtocolServerMsgHandler {
	private static final Logger log = Logger.getLogger(GameServerMsgHandler.class);
	@Override
	public void onMsgReceived(ProtocolMsg msg) {
		msg.parse();
		LSMsgReceiver.getInstance().addMsg(msg);
		
	}

	@Override
	public void onSessionClosed(Channel channel) {
		Attribute<GameServer> attr = channel.attr(GameServer.NETTY_CHANNEL_KEY);  
		GameServer gs = attr.get(); 
		if(gs!=null){
			gs.setStatus(GameServer.STATUS_OFFLINE);
			log.info(gs.getHost()+"连接已经断开");
		}
	}


}
