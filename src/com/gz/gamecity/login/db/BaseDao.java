package com.gz.gamecity.login.db;

import com.gz.gamecity.login.config.ConfigField;
import com.gz.util.Config;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public abstract class BaseDao {
	
	protected static Jedis getConn() {
		try {
			Jedis jedis = JedisConnectionPool.getJedisConn();
			jedis.select(Config.instance().getIValue(ConfigField.DB_INDEX));
			return jedis;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	protected static void closeConn(Jedis jedis) {
		try {
			if(jedis!=null)
				jedis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
