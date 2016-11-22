package com.gz.gamecity.login.service;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import com.gz.gamecity.login.bean.GameServer;
import com.gz.gamecity.login.logic.LogicHandler;
import com.gz.gamecity.login.protocol.ProtocolsField;
import com.gz.websocket.msg.BaseMsg;
import com.gz.websocket.msg.ProtocolMsg;

public class GameServerService implements LogicHandler {

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
				String email = server.getAttributeValue("email");
				System.out.println(email);
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
		int subCode = pMsg.getJson().getIntValue(ProtocolsField.SUBCODE);
		switch (subCode){
			case ProtocolsField.G2l_login.subCode_value:
				int serverId = pMsg.getJson().getIntValue(ProtocolsField.G2l_login.SERVERID);
				GameServer gs = map_server.get(serverId);
				if(gs==null)
					msg.getChannel().close();
				String remoteAdd = msg.getChannel().remoteAddress().toString().substring(1);
				remoteAdd = remoteAdd.substring(0, remoteAdd.indexOf(':'));
				if(!remoteAdd.equals(gs.getHost()))
					msg.getChannel().close();
				
				break;
		}
			
		
		
	}

}
