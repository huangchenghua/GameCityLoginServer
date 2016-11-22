package com.gz.gamecity.login.bean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class Player {
	public static final String NAME="name";
	public static final String UUID="uuid";
	public static final String COIN="coin";
	
	private String name;
	private long coin;
	private String token;
	private String uuid;
	private Channel channel;
	
	
	public Channel getChannel() {
		return channel;
	}
	public void setChannel(Channel channel) {
		this.channel = channel;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getCoin() {
		return coin;
	}
	public void setCoin(long coin) {
		this.coin = coin;
	}
	
	public static Player createPlayer(String uuid){
		Player player = new Player();
		player.setUuid(uuid);
		player.setCoin(50000);
		player.setName("游客");
		return player;
	}
	public void write(JSONObject json){
		channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(json)));
	}
}
