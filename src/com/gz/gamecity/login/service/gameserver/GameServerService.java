package com.gz.gamecity.login.service.gameserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import com.gz.gamecity.bean.Player;
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
				int port =Integer.parseInt(server.getChildText("port"));
				GameServer gs = new GameServer();
				gs.setHost(host);
				gs.setName(name);
				gs.setServerId(serverId);
				gs.setClientPort(port);
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
				handleGameServerLogin(pMsg);
				break;
			
		}
	}
	
	

	private void handleGameServerLogin(ProtocolMsg pMsg){
		int serverId = pMsg.getJson().getIntValue(Protocols.G2l_login.SERVERID);
		GameServer gs = map_server.get(serverId);
		System.out.println("remote="+pMsg.getChannel().remoteAddress());
		String remoteAdd = pMsg.getChannel().remoteAddress().toString().substring(1);
		remoteAdd = remoteAdd.substring(0, remoteAdd.indexOf(':'));
		if(gs==null){
			log.warn("未知游戏服请求登录  ip="+remoteAdd);
			pMsg.getChannel().close();
			return;
		}
		if(gs.isOnline()){
			log.warn("重复的游戏服请求登录  ip="+remoteAdd);
			pMsg.getChannel().close();
			return;
		}
		if(!remoteAdd.equals("127.0.0.1") && !remoteAdd.equals(gs.getHost())){
			pMsg.closeChannel();;
			log.warn("非法IP地址请求,ip="+remoteAdd);
			return;
		}
		gs.setChannel(pMsg.getChannel());
		gs.setStatus(GameServer.STATUS_ONLINE);
		Attribute<GameServer> att= pMsg.getChannel().attr(NETTY_CHANNEL_KEY);
		att.setIfAbsent(gs);
		log.info("游戏服务器'"+gs.getName()+"'连接成功");
		pMsg.getJson().put(Protocols.SUBCODE, Protocols.L2g_login.subCode_value);
		pMsg.getJson().put(Protocols.L2g_login.OPT, 1);
		GameServerMsgSender.getInstance().addMsg(pMsg);
	}

	
	public boolean checkServer(ProtocolMsg pMsg){
		Attribute<GameServer> attr = pMsg.getChannel().attr(NETTY_CHANNEL_KEY);  
		GameServer gs = attr.get(); 
		if(gs!=null){
			return true;
		}
		pMsg.getChannel().close();
		return false;
	}

	@Override
	public int getMainCode() {
		
		return Protocols.MainCode.GAMESERVER_LOGIN;
	}
	
	public GameServer getGameServer(ProtocolMsg msg){
		Attribute<GameServer> attr = msg.getChannel().attr(NETTY_CHANNEL_KEY);  
		GameServer gs = attr.get(); 
		return gs;
	}
}