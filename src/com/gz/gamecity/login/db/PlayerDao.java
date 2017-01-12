package com.gz.gamecity.login.db;

import java.util.HashMap;
import java.util.List;

import com.gz.gamecity.bean.Player;

import redis.clients.jedis.Jedis;

public class PlayerDao extends JBaseDao {
	private static final String player_data_prefix = "player";

	public static Player getPlayer(String uuid) {
		Jedis jedis = null;
		try {
			jedis = getConn();
			String key_player = player_data_prefix + uuid;
			if (jedis.exists(key_player)) {
				List<String> list = jedis.hmget(player_data_prefix + uuid, Player.NAME, Player.COIN);
				if (list != null) {
					Player player = new Player();
					player.setUuid(uuid);
					player.setName(list.get(0));
					player.setCoin(Long.parseLong(list.get(1)));
					return player;
				}
			}
		} finally {
			closeConn(jedis);
		}
		return null;
	}

	public static void insertPlayer(Player player) {
		Jedis jedis = null;
		try {
			jedis = getConn();
			String key_player = player_data_prefix + player.getUuid();
			HashMap<String, String> map = new HashMap<>();
			map.put(Player.NAME, player.getName());
			map.put(Player.COIN, String.valueOf(player.getCoin()));
			jedis.hmset(key_player, map);
		} finally {
			closeConn(jedis);
		}
	}
	
	public static void updatePlayerCoin(Player player){
		Jedis jedis = null;
		try {
			jedis = getConn();
			String key_player = player_data_prefix + player.getUuid();
			if(jedis.exists(key_player)){
				jedis.hset(key_player, Player.COIN, String.valueOf(player.getCoin()));
			}
		} finally {
			closeConn(jedis);
		}
	}
}
