package com.gz.gamecity.login.service.player;

import com.gz.gamecity.bean.Player;
import com.gz.gamecity.login.GameServerMsgSender;
import com.gz.gamecity.login.PlayerManager;
import com.gz.gamecity.login.bean.GameServer;
import com.gz.gamecity.login.config.StringConst;
import com.gz.gamecity.login.logic.LogicHandler;
import com.gz.gamecity.login.service.gameserver.GameServerService;
import com.gz.gamecity.protocol.Protocols;
import com.gz.websocket.msg.BaseMsg;
import com.gz.websocket.msg.ProtocolMsg;

public class PlayerVerifyService implements LogicHandler {

	@Override
	public void handleMsg(BaseMsg msg) {
		ProtocolMsg pMsg = (ProtocolMsg) msg;
		if(!GameServerService.getInstance().checkServer(pMsg))
		{
			return;
		}
		int subCode = pMsg.getJson().getIntValue(Protocols.SUBCODE);
		switch (subCode){
			case Protocols.G2l_playerVerify.subCode_value:
				verifyGameToken(pMsg);
				break;
			case Protocols.G2l_playerLogout.subCode_value:
				handlePlayerLogout(pMsg);
				break;
		}
	}
	
	private void verifyGameToken(ProtocolMsg pMsg) {
		GameServer gs=GameServerService.getInstance().getGameServer(pMsg);
		String uuid=pMsg.getJson().getString(Protocols.G2l_playerVerify.UUID);
		String gameToken=pMsg.getJson().getString(Protocols.G2l_playerVerify.GAMETOKEN);
		Player player=PlayerLoginService.getInstance().checkGameToken(uuid, gameToken);
		pMsg.put(Protocols.SUBCODE, Protocols.L2g_playerVerify.subCode_value);
		if(player==null) // token验证失败
		{
			pMsg.put(Protocols.ERRORCODE, StringConst.str2);
			GameServerMsgSender.getInstance().addMsg(pMsg);
			return;
		}
		if(player.isFrozen()){
			pMsg.put(Protocols.ERRORCODE, StringConst.str3);
			GameServerMsgSender.getInstance().addMsg(pMsg);
			return;
		}
		//判断是否已经在线
		Player p = PlayerManager.getInstance().getOnlinePlayer(uuid);
		if(p!=null){ //如果在线队列中已经存在
			// 这里原先是允许在没下线之前登录同一个服务器的，但是有个大问题：玩家的金钱是已哪个为准？如果同意登录：登录服会发送游戏币给游戏服，
			//但是游戏服中的玩家可能在牌局中还没有结算，之后还是会通知登录服更改游戏币，所以数据同步存在问题。
			//既然这种问题存在就干脆必须等待游戏服务器主动告诉登录服玩家下线，然后玩家才可以登录游戏
			if(p.getServerId()!=gs.getServerId()){
				pMsg.put(Protocols.ERRORCODE, StringConst.str1);
				GameServerMsgSender.getInstance().addMsg(pMsg);
				return;
			}
		}
		pMsg.put(Protocols.L2g_playerVerify.UUID, uuid);
		pMsg.put(Protocols.L2g_playerVerify.GAMETOKEN, gameToken);
		pMsg.put(Protocols.L2g_playerVerify.NAME, player.getName());
		pMsg.put(Protocols.L2g_playerVerify.SEX, player.getSex());
		pMsg.put(Protocols.L2g_playerVerify.COIN, player.getCoin());
		pMsg.put(Protocols.L2g_playerVerify.HEAD, player.getHead());
		pMsg.put(Protocols.L2g_playerVerify.LVL, player.getLvl());
		pMsg.put(Protocols.L2g_playerVerify.EXP, player.getExp());
		pMsg.put(Protocols.L2g_playerVerify.CHARGE_TOTAL, player.getCharge_total());
		pMsg.put(Protocols.L2g_playerVerify.CHARM, player.getCharm());
		pMsg.put(Protocols.L2g_playerVerify.FINANCE, player.getFinance());
		pMsg.put(Protocols.L2g_playerVerify.SIGN, player.getSign());
		pMsg.put(Protocols.L2g_playerVerify.VIP, player.getVip());
		pMsg.put(Protocols.L2g_playerVerify.FROZEN, player.isFrozen());
		pMsg.put(Protocols.L2g_playerVerify.SILENT, player.isSilent());
		pMsg.put(Protocols.L2g_playerVerify.LASTSIGNDATE, player.getLastSignDate());
		pMsg.put(Protocols.L2g_playerVerify.SIGNDAYS, player.getSignDays());
		pMsg.put(Protocols.L2g_playerVerify.SIGNED, player.isSigned());
		pMsg.put(Protocols.L2g_playerVerify.ALMS_CNT, player.getAlmsCnt());
		pMsg.put(Protocols.L2g_playerVerify.ALMS_TIME, player.getAlmsTime());
		pMsg.put(Protocols.L2g_playerVerify.HEADS, player.getHeads());
		player.setServerId(gs.getServerId());
		PlayerManager.getInstance().playerOnline(player, gs);
		GameServerMsgSender.getInstance().addMsg(pMsg);
		
	}

	private void handlePlayerLogout(ProtocolMsg pMsg) {
		String uuid = pMsg.getJson().getString(Protocols.G2l_playerLogout.UUID);
		String gameToken = pMsg.getJson().getString(Protocols.G2l_playerLogout.GAMETOKEN);
		Player player = PlayerManager.getInstance().getOnlinePlayer(uuid);
		if(player!=null && gameToken.equals(player.getGameToken())){
			GameServer gs = GameServerService.getInstance().getGameServer(pMsg);
			if(gs.getServerId() == player.getServerId()) //这里必须判断一下是否是在同一个服务器，应为踢下线操作可能在不同的服务器之间发生
				PlayerManager.getInstance().playerOffline(uuid);
		}
	}

	@Override
	public int getMainCode() {
		return Protocols.MainCode.PLAYERVERIFY;
	}

}
