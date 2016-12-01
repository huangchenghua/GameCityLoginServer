package com.gz.gamecity.login.bean;

import com.alibaba.fastjson.JSONObject;
import com.gz.websocket.msg.ProtocolMsg;

import io.netty.channel.Channel;

public class GameServer {
	public static final byte STATUS_OFFLINE =0;
	public static final byte STATUS_ONLINE=1;
	
	private int serverId;
	private String host;
	private String name;
	private byte status = STATUS_OFFLINE;
	private Channel channel;
	/**
	 * 这个端口是给游戏客户端的，告诉游戏服在哪个端口开放
	 */
	private int clientPort;
	public int getClientPort() {
		return clientPort;
	}
	public void setClientPort(int clientPort) {
		this.clientPort = clientPort;
	}
	public Channel getChannel() {
		return channel;
	}
	public void setChannel(Channel channel) {
		this.channel = channel;
	}
	public byte getStatus() {
		return status;
	}
	public void setStatus(byte status) {
		this.status = status;
	}
	public int getServerId() {
		return serverId;
	}
	public void setServerId(int serverId) {
		this.serverId = serverId;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void wirite(JSONObject json){
		ProtocolMsg msg = new ProtocolMsg();
		String body = json.toJSONString();
		msg.setContent(body);
		channel.writeAndFlush(msg);
	}
	
	public boolean isOnline(){
		return status == STATUS_ONLINE;
	}
}
