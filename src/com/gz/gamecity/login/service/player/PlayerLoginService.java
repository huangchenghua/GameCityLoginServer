package com.gz.gamecity.login.service.player;

import java.util.Collection;
import java.util.Random;
import java.util.UUID;

import com.alibaba.fastjson.JSONObject;
import com.gz.gamecity.bean.Player;
import com.gz.gamecity.login.PlayerManager;
import com.gz.gamecity.login.bean.GameServer;
import com.gz.gamecity.login.config.ConfigField;
import com.gz.gamecity.login.db.PlayerDao;
import com.gz.gamecity.login.db.PlayerDataDao;
import com.gz.gamecity.login.logic.LogicHandler;
import com.gz.gamecity.login.sdkverify.SdkVerify;
import com.gz.gamecity.login.service.gameserver.GameServerService;
import com.gz.gamecity.protocol.Protocols;
import com.gz.http.HttpDecoderAndEncoder;
import com.gz.util.Config;
import com.gz.websocket.msg.BaseMsg;
import com.gz.websocket.msg.ClientMsg;
import com.gz.websocket.msg.HttpMsg;


public class PlayerLoginService implements LogicHandler {
	
	private static final String str_random="abcdefghijklmnpqrstuvwxyzABCDEFGHIJKLMNPQRSTUVWXYZ123456789";
	
	private static PlayerLoginService instance;
	private PlayerDataDao dao=new PlayerDataDao();
	
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
		HttpMsg cMsg=(HttpMsg)msg;
		int subCode = cMsg.getJson().getIntValue("subCode");
		switch (subCode) {
		case Protocols.C2l_login.subCode_value:
			handlePlayerLogin(cMsg);
			break;
		case Protocols.Inner_sdk_verify_suc.subCode_value:
			handleLogin(cMsg);
			break;
		case Protocols.C2l_login_test.subCode_value:
			handleLoginTest(cMsg);
			break;
		default:
			break;
		}
	}


	private void handleLoginTest(HttpMsg hMsg) {
		handleLogin(hMsg);
	}

	private void handlePlayerLogin(HttpMsg hMsg){
		SdkVerify.getInstance().addMsg(hMsg);
	}
	
	private void handleLogin(HttpMsg hMsg) {
		String uuid = hMsg.getJson().getString(Protocols.Inner_sdk_verify_suc.UUID);
		
		GameServer gs = null;
		//先从当前在线玩家的缓存中去查找
		Player player = PlayerManager.getInstance().getOnlinePlayer(uuid);
		if(player!=null){
			//如果玩家是已经在线，就只发送当前进入的服务器列表
			gs = GameServerService.getInstance().getGameServer(player.getServerId());
		}else{
			player = dao.getPlayer(uuid);
			if(player == null){
				player = handleRegist(uuid);
			}
		}
		
		
		player.setGameToken(UUID.randomUUID().toString());
//		player.setChannel(hMsg.getChannel());
		PlayerManager.getInstance().playerLogin(player);
//		PlayerLoginCache.getInstance().put(uuid, player, 1000*60*10);
		
		
		JSONObject json=new JSONObject();
		json.put(Protocols.MAINCODE, Protocols.L2c_login.mainCode_value);
		json.put(Protocols.SUBCODE, Protocols.L2c_login.subCode_value);
		json.put(Protocols.L2c_login.GAMETOKEN, player.getGameToken());
		Collection<GameServer> servers=GameServerService.getInstance().getMap_server().values();
		if(gs ==null){
			JSONObject[] serverArray=new JSONObject[servers.size()];
			int i=0;
			for (GameServer server : servers) {
				JSONObject j=new JSONObject();
				j.put(Protocols.L2c_login.Serverlist.NAME, server.getName()); 
				j.put(Protocols.L2c_login.Serverlist.ADDRESS, server.getGame_address());
				j.put(Protocols.L2c_login.Serverlist.PORT, server.getClientPort());
				j.put(Protocols.L2c_login.Serverlist.STATUS, server.getStatus());
				serverArray[i]=j;
				i++;
			}
			json.put(Protocols.L2c_login.SERVERLIST, serverArray);
		}else{
			JSONObject[] serverArray=new JSONObject[1];
			serverArray[0]= new JSONObject();
			serverArray[0].put(Protocols.L2c_login.Serverlist.NAME, gs.getName()); 
			serverArray[0].put(Protocols.L2c_login.Serverlist.ADDRESS, gs.getGame_address());
			serverArray[0].put(Protocols.L2c_login.Serverlist.PORT, gs.getClientPort());
			serverArray[0].put(Protocols.L2c_login.Serverlist.STATUS, gs.getStatus());
			json.put(Protocols.L2c_login.SERVERLIST, serverArray);
		}
		
		hMsg.clear();
		hMsg.setJson(json);
		HttpDecoderAndEncoder.Response(hMsg.getCtx(), hMsg.getRequest(), json.toJSONString());
		hMsg.getChannel().close();
	}

	private Player handleRegist(String uuid) {
		Player player = createPlayer(uuid);
		dao.insertPlayer(player);
		return player;
	}
	
	public Player checkGameToken(String uuid,String gameToken){
//		Player player=PlayerLoginCache.getInstance().getV(uuid);
		Player player = PlayerManager.getInstance().getLoginPlayer(uuid+gameToken);
		if(player!=null && player.getGameToken().equals(gameToken)){
			return player;
		}
		return null;
	}

	@Override
	public int getMainCode() {
		return Protocols.MainCode.PLAYER_LOGIN;
	}
	
	
	private Player createPlayer(String uuid) {
		Player player = new Player();
		player.setUuid(uuid);
		player.setName(getRandomName());
		player.setSex((byte) 2);
		player.setCoin(Config.instance().getLValue(ConfigField.PLAYER_INIT_COIN));
		player.setHead(1);
		player.setLvl(1);
		player.setFinance(0);
		player.setVip(0);
		player.setCharm(0);
		player.setSign("");
		player.setCharge_total(0);
		player.setHeads(new int[]{1,2,3,4,5,6,7,8});
		return player;
	}
	
	private static final String getRandomName(){
		StringBuffer sb = new StringBuffer("用户");
		Random r=new Random();
		for(int i=0;i<8;i++){
			int index = r.nextInt(str_random.length());
			sb.append(str_random.charAt(index));
		}
//		System.out.println(sb.toString());
		
		return sb.toString();
	}
	
}
