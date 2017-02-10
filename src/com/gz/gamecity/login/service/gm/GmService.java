package com.gz.gamecity.login.service.gm;

import java.util.UUID;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gz.gamecity.bean.Player;
import com.gz.gamecity.login.GameServerMsgSender;
import com.gz.gamecity.login.GmMsgSender;
import com.gz.gamecity.login.PlayerManager;
import com.gz.gamecity.login.PlayerMsgSender;
import com.gz.gamecity.login.bean.GameServer;
import com.gz.gamecity.login.logic.LogicHandler;
import com.gz.gamecity.login.service.db.DBService;
import com.gz.gamecity.login.service.gameserver.GameServerService;
import com.gz.gamecity.protocol.Protocols;
import com.gz.websocket.msg.BaseMsg;
import com.gz.websocket.msg.ClientMsg;
import com.gz.websocket.msg.ProtocolMsg;

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.Attribute;

public class GmService implements LogicHandler {

	@Override
	public void handleMsg(BaseMsg bMsg) {
		ClientMsg msg = (ClientMsg)bMsg;
		int subCode = msg.getJson().getIntValue(Protocols.SUBCODE);
		System.out.println("收到GM消息"+subCode);
		switch (subCode) {
		case Protocols.Gm2l_login.subCode_value:
			handleGmLogin(msg);
			break;
		case Protocols.Gm2l_player_count.subCode_value:
			handlePlayerCount(msg);
			break;
		case Protocols.Gm2l_search_player.subCode_value:
			handleSearchPlayer(msg);
			break;
		case Protocols.Gm2l_freeze.subCode_value:
			handleFreeze(msg);
			break;
		case Protocols.Gm2l_silent.subCode_value:
			handleSilent(msg);
			break;
		case Protocols.Gm2l_add_coin.subCode_value:
			handleCoin(msg);
			break;
		case Protocols.Gm2l_req_charge.subCode_value:
			handleCharge(msg);
			break;
		case Protocols.Gm2l_unfreeze.subCode_value:
			handleUnfreeze(msg);
			break;
		case Protocols.Gm2l_unsilent.subCode_value:
			handleUnsilent(msg);
			break;
		case Protocols.Gm2l_search_frozen_player.subCode_value:
			handleSearchFrozenPlayer(msg);
			break;
		case Protocols.Gm2l_search_silent_player.subCode_value:
			handleSearchSilentPlayer(msg);
			break;
		case Protocols.Gm2l_send_game_notice.subCode_value:
			handleSendGameNotice(msg);
			break;
		case Protocols.Gm2l_game_notice_list.subCode_value:
			handleNoticeList(msg);
			break;
		case Protocols.Gm2l_del_game_notice.subCode_value:
			handleDelNotice(msg);
			break;
		default:
			break;
		}

	}

	private void handleDelNotice(ClientMsg msg) {
		GameNoticeManager.getInstance().delNotice(msg);
	}

	private void handleNoticeList(ClientMsg msg) {
		GameNoticeManager.getInstance().getNoticeList(msg);
		
	}

	private void handleSendGameNotice(ClientMsg msg) {
		GameNoticeManager.getInstance().addGameNotice(msg);
	}

	private void handleSearchSilentPlayer(ClientMsg msg) {
		Player gm = getGmFromMsg(msg);
		if(gm!=null){
			msg.put(Protocols.MAINCODE, Protocols.DB_search_silent_player.mainCode_value);
			msg.put(Protocols.SUBCODE, Protocols.DB_search_silent_player.subCode_value);
			msg.put(Protocols.DB_search_silent_player.GM_UUID, gm.getUuid());
			DBService.getInstance().addMsg(msg.getJson());
		}
	}

	private void handleSearchFrozenPlayer(ClientMsg msg) {
		Player gm = getGmFromMsg(msg);
		if(gm!=null){
			msg.put(Protocols.MAINCODE, Protocols.DB_search_frozen_player.mainCode_value);
			msg.put(Protocols.SUBCODE, Protocols.DB_search_frozen_player.subCode_value);
			msg.put(Protocols.DB_search_frozen_player.GM_UUID, gm.getUuid());
			DBService.getInstance().addMsg(msg.getJson());
		}
	}

	private void handleUnsilent(ClientMsg msg) {
		String uuid = msg.getJson().getString(Protocols.Gm2l_unsilent.UUID);
		Player player = PlayerManager.getInstance().getOnlinePlayer(uuid);
		if(player!=null){
			player.setSilent(true);
			GameServer gs = GameServerService.getInstance().getGameServer(player.getServerId());
			ProtocolMsg pMsg = new ProtocolMsg();
			pMsg.put(Protocols.MAINCODE, Protocols.L2g_unsilent.mainCode_value);
			pMsg.put(Protocols.SUBCODE, Protocols.L2g_unsilent.subCode_value);
			pMsg.put(Protocols.L2g_unsilent.UUID, uuid);
			pMsg.setChannel(gs.getChannel());
			GameServerMsgSender.getInstance().addMsg(pMsg);
		}
		msg.put(Protocols.MAINCODE, Protocols.L2gm_unsilent.mainCode_value);
		msg.put(Protocols.SUBCODE, Protocols.L2gm_unsilent.subCode_value);
		PlayerMsgSender.getInstance().addMsg(msg);
		
		
		JSONObject j = new JSONObject();
		j.put(Protocols.MAINCODE, Protocols.DB_unsilent.mainCode_value);
		j.put(Protocols.SUBCODE, Protocols.DB_unsilent.subCode_value);
		j.put(Protocols.DB_unsilent.UUID, uuid);
		DBService.getInstance().addMsg(j);
	}

	private void handleUnfreeze(ClientMsg msg) {
		msg.put(Protocols.MAINCODE, Protocols.L2gm_unfreeze.mainCode_value);
		msg.put(Protocols.SUBCODE, Protocols.L2gm_unfreeze.subCode_value);
		PlayerMsgSender.getInstance().addMsg(msg);
		String uuid = msg.getJson().getString(Protocols.Gm2l_unfreeze.UUID);
		JSONObject j = new JSONObject();
		j.put(Protocols.MAINCODE, Protocols.DB_unfreeze.mainCode_value);
		j.put(Protocols.SUBCODE, Protocols.DB_unfreeze.subCode_value);
		j.put(Protocols.DB_unfreeze.UUID, uuid);
		DBService.getInstance().addMsg(j);
	}

	private void handleCharge(ClientMsg msg) {
		msg.put(Protocols.MAINCODE, Protocols.DB_req_charge_list.mainCode_value);
		msg.put(Protocols.SUBCODE, Protocols.DB_req_charge_list.subCode_value);
		Player gm = getGmFromMsg(msg);
		msg.put(Protocols.DB_req_charge_list.GM_UUID, gm.getUuid());
		DBService.getInstance().addMsg(msg.getJson());
	}

	private void handleCoin(ClientMsg msg) {
		String uuid = msg.getJson().getString(Protocols.Gm2l_add_coin.UUID);
		long coin = msg.getJson().getLongValue(Protocols.Gm2l_add_coin.COIN);
		Player player = PlayerManager.getInstance().getOnlinePlayer(uuid);
		if(player!=null){
			GameServer gs = GameServerService.getInstance().getGameServer(player.getServerId());
			ProtocolMsg pMsg = new ProtocolMsg();
			pMsg.put(Protocols.MAINCODE, Protocols.L2g_gm_add_coin.mainCode_value);
			pMsg.put(Protocols.SUBCODE, Protocols.L2g_gm_add_coin.subCode_value);
			pMsg.put(Protocols.L2g_gm_add_coin.UUID, uuid);
			pMsg.put(Protocols.L2g_gm_add_coin.COIN, coin);
			pMsg.setChannel(gs.getChannel());
			GameServerMsgSender.getInstance().addMsg(pMsg);
		}else{
			JSONObject j = new JSONObject();
			j.put(Protocols.MAINCODE, Protocols.DB_gm_add_coin.mainCode_value);
			j.put(Protocols.SUBCODE, Protocols.DB_gm_add_coin.subCode_value);
			j.put(Protocols.DB_gm_add_coin.UUID, uuid);
			j.put(Protocols.DB_gm_add_coin.COIN, coin);
			DBService.getInstance().addMsg(j);
		}
		msg.put(Protocols.SUBCODE, Protocols.L2gm_add_coin.subCode_value);
		GmMsgSender.getInstance().addMsg(msg);
	}

	private void handleSilent(ClientMsg msg) {
		String uuid = msg.getJson().getString(Protocols.Gm2l_silent.UUID);
		Player player = PlayerManager.getInstance().getOnlinePlayer(uuid);
		if(player!=null){
			player.setSilent(true);
			GameServer gs = GameServerService.getInstance().getGameServer(player.getServerId());
			ProtocolMsg pMsg = new ProtocolMsg();
			pMsg.put(Protocols.MAINCODE, Protocols.L2g_silent.mainCode_value);
			pMsg.put(Protocols.SUBCODE, Protocols.L2g_silent.subCode_value);
			pMsg.put(Protocols.Gm2l_unsilent.UUID, uuid);
			pMsg.setChannel(gs.getChannel());
			GameServerMsgSender.getInstance().addMsg(pMsg);
		}
		msg.put(Protocols.MAINCODE, Protocols.L2gm_silent.mainCode_value);
		msg.put(Protocols.SUBCODE, Protocols.L2gm_silent.subCode_value);
		PlayerMsgSender.getInstance().addMsg(msg);
		
		
		JSONObject j = new JSONObject();
		j.put(Protocols.MAINCODE, Protocols.DB_silent.mainCode_value);
		j.put(Protocols.SUBCODE, Protocols.DB_silent.subCode_value);
		j.put(Protocols.DB_silent.UUID, uuid);
		DBService.getInstance().addMsg(j);
	}

	private void handleFreeze (ClientMsg msg) {
		String uuid = msg.getJson().getString(Protocols.Gm2l_freeze.UUID);
		Player player = PlayerManager.getInstance().getOnlinePlayer(uuid);
		if(player!=null){
			player.setFrozen(true);
			GameServer gs = GameServerService.getInstance().getGameServer(player.getServerId());
			ProtocolMsg pMsg = new ProtocolMsg();
			pMsg.put(Protocols.MAINCODE, Protocols.L2g_kickPlayer.mainCode_value);
			pMsg.put(Protocols.SUBCODE, Protocols.L2g_kickPlayer.subCode_value);
			pMsg.put(Protocols.L2g_kickPlayer.UUID, uuid);
			pMsg.setChannel(gs.getChannel());
			GameServerMsgSender.getInstance().addMsg(pMsg);
		}
		msg.put(Protocols.MAINCODE, Protocols.L2gm_freeze.mainCode_value);
		msg.put(Protocols.SUBCODE, Protocols.L2gm_freeze.subCode_value);
		PlayerMsgSender.getInstance().addMsg(msg);
		
		JSONObject j = new JSONObject();
		j.put(Protocols.MAINCODE, Protocols.DB_freeze.mainCode_value);
		j.put(Protocols.SUBCODE, Protocols.DB_freeze.subCode_value);
		j.put(Protocols.DB_freeze.UUID, uuid);
		DBService.getInstance().addMsg(j);
	}

	private void handleSearchPlayer(ClientMsg msg) {
		String name = msg.getJson().getString(Protocols.Gm2l_search_player.NAME);
		if(name==null || name.equals("")){
			msg.put(Protocols.ERRORCODE, "请输入名字");
			GmMsgSender.getInstance().addMsg(msg);
			return;
		}
		Player gm = getGmFromMsg(msg);
		if(gm!=null){
			msg.put(Protocols.MAINCODE, Protocols.DB_search_player.mainCode_value);
			msg.put(Protocols.SUBCODE, Protocols.DB_search_player.subCode_value);
			msg.put(Protocols.DB_search_player.GM_UUID, gm.getUuid());
			DBService.getInstance().addMsg(msg.getJson());
		}
	}

	private void handlePlayerCount(ClientMsg msg) {
		int count = PlayerManager.getInstance().getOnlinePlayers().size();
		msg.put(Protocols.SUBCODE, Protocols.L2gm_player_count.subCode_value);
		msg.put(Protocols.L2gm_player_count.COUNT, count);
		GmMsgSender.getInstance().addMsg(msg);
	}

	private void handleGmLogin(ClientMsg msg) {
		msg.put(Protocols.SUBCODE, Protocols.L2gm_login.subCode_value);
		if(!checkGMLogin(msg)){
			msg.put(Protocols.ERRORCODE, "用户名货密码错误");
			msg.getChannel().writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(msg.getJson())));
			return;
		}
		Player gm = new Player();
		gm.setChannel(msg.getChannel());
		gm.setUuid(UUID.randomUUID().toString());
		gm.setName(msg.getJson().getString(Protocols.Gm2l_login.NAME));
		Attribute<Player> att= msg.getChannel().attr(Player.NETTY_CHANNEL_KEY);
		att.set(gm);
		GmMsgSender.getInstance().addMsg(msg);
		PlayerManager.getInstance().getOnlineGm().put(gm.getUuid(), gm);
	}

	@Override
	public int getMainCode() {
		return Protocols.MainCode.GM_TOOL;
	}

	private boolean checkGMLogin(ClientMsg msg){
		String name = msg.getJson().getString(Protocols.Gm2l_login.NAME);
		String owd = msg.getJson().getString(Protocols.Gm2l_login.PASSWORD);
		// TODO 验证
		return true;
	}
	
	private Player getGmFromMsg(ClientMsg msg){
		Attribute<Player> att= msg.getChannel().attr(Player.NETTY_CHANNEL_KEY);
		if(att!=null)
			return att.get();
		return null;
	}
}
