package com.gz.gamecity.login;

import java.util.concurrent.ConcurrentHashMap;

import com.gz.gamecity.bean.Player;
import com.gz.gamecity.login.bean.GameServer;
import com.gz.gamecity.login.service.gameserver.GameServerService;
import com.gz.gamecity.protocol.Protocols;
import com.gz.util.DelayCache;
import com.gz.websocket.msg.ProtocolMsg;

public class PlayerManager {
	
	private static final long loginCacheTime = 10*60*1000l;
	
	
	private static PlayerManager instance;

	private ConcurrentHashMap<String,Player> onlinePlayers;
	
	private DelayCache<String, Player> loginCache;
	
	public static synchronized PlayerManager getInstance() {
		if(instance==null)
			instance=new PlayerManager();
		return instance;
	}
	
	private PlayerManager(){
		onlinePlayers=new ConcurrentHashMap<>();
		loginCache=new DelayCache<String, Player>();
	}
	
	public boolean playerOnline(Player player,GameServer gs){
		Player p =onlinePlayers.get(player.getUuid());
		//如果在线玩家队列中没有
		if(p==null){
			onlinePlayers.put(player.getUuid(), player);
			return true;
		}
		else{ 
			// 如果在在线玩家队列中就看登录的是否是同一个服务器,如果是同一个服务器就可以登录
			if(p.getServerId() == gs.getServerId()){
				return true;
			}
		}
		
		return false;
	}
	
	public Player getOnlinePlayer(String uuid){
		return onlinePlayers.get(uuid);
	}
	
	public Player playerOffline(String uuid){
		Player player = onlinePlayers.remove(uuid);
		return player;
	}
	
	public void playerLogin(Player player){
		loginCache.put(player.getUuid()+player.getGameToken(), player, loginCacheTime);
	}
	
	public Player getLoginPlayer(String key){
		return loginCache.getV(key);
	}
	
	
	
}
