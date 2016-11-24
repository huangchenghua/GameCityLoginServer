package com.gz.gamecity.login.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import com.gz.gamecity.login.GameServerMsgSender;
import com.gz.gamecity.login.bean.GameServer;
import com.gz.gamecity.login.logic.LogicHandler;
import com.gz.gamecity.protocol.Protocols;
import com.gz.websocket.msg.BaseMsg;
import com.gz.websocket.msg.ProtocolMsg;

import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

public class GameServerService implements LogicHandler {
	
	private static final Logger log=Logger.getLogger(GameServerService.class);
	
	private static final AttributeKey<GameServer> NETTY_CHANNEL_KEY = AttributeKey.valueOf("gameServer");
	
	private static GameServerService instance;
	
	private HashMap<Integer, GameServer> map_server=new HashMap<Integer, GameServer>();
	
	public HashMap<Integer, GameServer> getMap_server() {
		return map_server;
	}

	private ArrayList<GameServer> list_server = new ArrayList<>();
	
	public static synchronized GameServerService getInstance() {
		if(instance ==null)
			instance = new GameServerService();
		return instance;
	}
	
	private GameServerService()
	{
		loadServerConfig();
	}
	
	private void loadServerConfig() {
		String xmlpath = "./conf/game_server.xml";
		SAXBuilder builder = new SAXBuilder(false);
		try {
			Document doc = builder.build(xmlpath);
			Element servers = doc.getRootElement();
			List serverlist = servers.getChildren("server");
			for (Iterator iter = serverlist.iterator(); iter.hasNext();) {
				Element server = (Element) iter.next();
				String name = server.getAttributeValue("name");
				System.out.println(name);
				int serverId = Integer.parseInt(server.getChildText("serverId"));
				String host = server.getChildText("host");
				GameServer gs = new GameServer();
				gs.setHost(host);
				gs.setName(name);
				gs.setServerId(serverId);
				map_server.put(serverId, gs);
				list_server.add(gs);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void handleMsg(BaseMsg msg) {
		ProtocolMsg pMsg = (ProtocolMsg) msg;
		int subCode = pMsg.getJson().getIntValue(Protocols.SUBCODE);
		switch (subCode){
			case Protocols.G2l_login.subCode_value:
				int serverId = pMsg.getJson().getIntValue(Protocols.G2l_login.SERVERID);
				GameServer gs = map_server.get(serverId);
				if(gs==null)
					msg.getChannel().close();
				String remoteAdd = msg.getChannel().remoteAddress().toString().substring(1);
				remoteAdd = remoteAdd.substring(0, remoteAdd.indexOf(':'));
				if(!remoteAdd.equals(gs.getHost()))
					msg.getChannel().close();
				gs.setChannel(msg.getChannel());
				gs.setStatus(GameServer.STATUS_ONLINE);
				Attribute<GameServer> att= msg.getChannel().attr(NETTY_CHANNEL_KEY);
				att.setIfAbsent(gs);
				log.info("游戏服务器'"+gs.getName()+"'连接成功");
				pMsg.getJson().put(Protocols.SUBCODE, Protocols.L2g_login.subCode_value);
				pMsg.getJson().put(Protocols.L2g_login.OPT, 1);
				GameServerMsgSender.getInstance().addMsg(pMsg);
				break;
		}
			
		
		
	}

}
