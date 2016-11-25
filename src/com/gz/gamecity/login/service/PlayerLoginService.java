package com.gz.gamecity.login.service;

import java.util.Collection;
import java.util.UUID;

import com.alibaba.fastjson.JSONObject;
import com.gz.gamecity.bean.Player;
import com.gz.gamecity.login.bean.GameServer;
import com.gz.gamecity.login.db.PlayerDao;
import com.gz.gamecity.login.logic.LogicHandler;
import com.gz.gamecity.login.sdkverify.SdkVerify;
import com.gz.gamecity.protocol.Protocols;
import com.gz.websocket.msg.BaseMsg;
import com.gz.websocket.msg.ClientMsg;


public class PlayerLoginService implements LogicHandler {
	
	
	
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
		case Protocols.C2l_login.subCode_value:
			handlePlayerLogin(cMsg);
			break;
		case Protocols.Inner_login.subCode_value:
			handleLogin(cMsg);
			break;
		default:
			break;
		}
	}


	private void handlePlayerLogin(ClientMsg cMsg){
		SdkVerify.getInstance().addMsg(cMsg);
	}
	
	private void handleLogin(ClientMsg cMsg) {
		String uuid = cMsg.getJson().getString(Protocols.Inner_login.UUID);
		Player player = PlayerDao.getPlayer(uuid);
		if(player == null){
			player = handleRegist(uuid);
		}
		player.setGameToken(UUID.randomUUID().toString());
		player.setChannel(cMsg.getChannel());
		PlayerLoginCache.getInstance().put(uuid, player, 1000*60*10);
		
		// TODO 返回客户端数据
		
		JSONObject json=new JSONObject();
		json.put(Protocols.MAINCODE, Protocols.L2c_login.mainCode_value);
		json.put(Protocols.SUBCODE, Protocols.L2c_login.subCode_value);
		json.put(Protocols.L2c_login.GAMETOKEN, player.getGameToken());
		Collection<GameServer> servers=GameServerService.getInstance().getMap_server().values();
		JSONObject[] serverArray=new JSONObject[servers.size()];
		int i=0;
		for (GameServer server : servers) {
			JSONObject j=new JSONObject();
			j.put(Protocols.L2c_login.Serverlist.NAME, server.getName()); 
			j.put(Protocols.L2c_login.Serverlist.ADDRESS, server.getHost());
			j.put(Protocols.L2c_login.Serverlist.PORT, server.getClientPort());
			j.put(Protocols.L2c_login.Serverlist.STATUS, server.getStatus());
			serverArray[i]=j;
			i++;
		}
		json.put(Protocols.L2c_login.SERVERLIST, serverArray);
		cMsg.clear();
		cMsg.setJson(json);
		cMsg.sendSelf();
		cMsg.getChannel().close();
	}

	private Player handleRegist(String uuid) {
		Player player = Player.createPlayer(uuid);
		PlayerDao.insertPlayer(player);
		return player;
	}
	
	public Player checkGameToken(String uuid,String gameToken){
		Player player=PlayerLoginCache.getInstance().getV(uuid);
		if(player!=null && player.getGameToken().equals(gameToken)){
			return player;
		}
		return null;
	}
	
}
