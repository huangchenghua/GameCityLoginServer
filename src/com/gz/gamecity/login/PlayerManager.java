package com.gz.gamecity.login;

import java.util.concurrent.ConcurrentHashMap;

import com.gz.gamecity.bean.Player;
import com.gz.util.DelayCache;

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
	
	public void playerOnline(Player player){
		Player p =onlinePlayers.get(player.getUuid());
		if(p!=null){
			// TODO 这里表示玩家已经在线，新上线的要踢掉之前的
		}
		
		onlinePlayers.put(player.getUuid(), player);
	}
	
	public Player getOnlinePlayer(String uuid){
		return onlinePlayers.get(uuid);
	}
	
	public Player playerOffline(String uuid){
		Player player = onlinePlayers.remove(uuid);
		return player;
	}
	
	public void playerLogin(Player player){
		loginCache.put(player.getUuid(), player, loginCacheTime);
	}
	
	public Player getLoginPlayer(String uuid){
		return loginCache.getV(uuid);
	}
	
	
	
}
