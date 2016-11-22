package com.gz.gamecity.login.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import com.alibaba.fastjson.JSONObject;
import com.gz.gamecity.login.bean.GameServer;
import com.gz.gamecity.login.bean.Player;
import com.gz.gamecity.login.db.PlayerDao;
import com.gz.gamecity.login.logic.LogicHandler;
import com.gz.gamecity.login.msg.ClientMsg;
import com.gz.gamecity.login.protocol.ProtocolsField;
import com.gz.websocket.msg.BaseMsg;

import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

public class PlayerLoginService implements LogicHandler {
	
	public static final AttributeKey<Player> NETTY_CHANNEL_KEY = AttributeKey.valueOf("player");
	
	private static HashMap<String, Player> map_loginPlayer=new HashMap<String, Player>();
	
	private static PlayerLoginService instance;
	
	public static synchronized PlayerLoginService getInstance() {
		if(instance ==null)
			instance = new PlayerLoginService();
		return instance;
	}
	
	private PlayerLoginService()
	{
		
	}

	@Override
	public void handleMsg(BaseMsg msg) {
//		System.out.println(msg);
		ClientMsg cMsg=(ClientMsg)msg;
		int subCode = cMsg.getJson().getIntValue("subCode");
		switch (subCode) {
		case ProtocolsField.C2l_login.subCode_value:
			handleLogin(cMsg);
			break;

		default:
			break;
		}
	}

	private void handleLogin(ClientMsg cMsg) {
		String uuid = cMsg.getJson().getString(ProtocolsField.C2l_login.UUID);
		Player player = PlayerDao.getPlayer(uuid);
		if(player == null){
			player = handleRegist(uuid);
		}
		player.setToken(UUID.randomUUID().toString());
		player.setChannel(cMsg.getChannel());
		map_loginPlayer.put(uuid, player);
		Attribute<Player> att= cMsg.getChannel().attr(NETTY_CHANNEL_KEY);
		att.setIfAbsent(player);
		
		// TODO 返回客户端数据
		
		JSONObject json=new JSONObject();
		json.put(ProtocolsField.MAINCODE, ProtocolsField.L2c_login.mainCode_value);
		json.put(ProtocolsField.SUBCODE, ProtocolsField.L2c_login.subCode_value);
		json.put(ProtocolsField.L2c_login.TOKEN, player.getToken());
		Collection<GameServer> servers=GameServerService.getInstance().getMap_server().values();
		JSONObject[] serverArray=new JSONObject[servers.size()];
		int i=0;
		for (GameServer server : servers) {
			JSONObject j=new JSONObject();
			j.put(ProtocolsField.L2c_login.Serverlist.NAME, server.getName()); 
			j.put(ProtocolsField.L2c_login.Serverlist.ADDRESS, server.getHost());
			j.put(ProtocolsField.L2c_login.Serverlist.PORT, server.getClientPort());
			j.put(ProtocolsField.L2c_login.Serverlist.STATUS, server.getStatus());
			serverArray[i]=j;
			i++;
		}
		json.put(ProtocolsField.L2c_login.SERVERLIST, serverArray);
		player.write(json);
	}

	private Player handleRegist(String uuid) {
		Player player = Player.createPlayer(uuid);
		PlayerDao.insertPlayer(player);
		return player;
	}
	
	//
	public void Logout(){
		
	}
}
