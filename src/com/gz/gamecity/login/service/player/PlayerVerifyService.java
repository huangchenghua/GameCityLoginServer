package com.gz.gamecity.login.service.player;

import com.gz.gamecity.bean.Player;
import com.gz.gamecity.login.GameServerMsgSender;
import com.gz.gamecity.login.PlayerManager;
import com.gz.gamecity.login.bean.GameServer;
import com.gz.gamecity.login.logic.LogicHandler;
import com.gz.gamecity.login.service.gameserver.GameServerService;
import com.gz.gamecity.protocol.Protocols;
import com.gz.websocket.msg.BaseMsg;
import com.gz.websocket.msg.ProtocolMsg;

public class PlayerVerifyService implements LogicHandler {

	@Override
	public void handleMsg(BaseMsg msg) {
		ProtocolMsg pMsg = (ProtocolMsg) msg;
		int subCode = pMsg.getJson().getIntValue(Protocols.SUBCODE);
		switch (subCode){
			case Protocols.G2l_playerVerify.subCode_value:
				if(GameServerService.getInstance().checkServer(pMsg))
					verifyGameToken(pMsg);
				break;
			case Protocols.G2l_playerLogout.subCode_value:
				if(GameServerService.getInstance().checkServer(pMsg))
					handlePlayerLogout(pMsg);
				break;
		}
	}
	
	private void verifyGameToken(ProtocolMsg pMsg) {
		String uuid=pMsg.getJson().getString(Protocols.G2l_playerVerify.UUID);
		String gameToken=pMsg.getJson().getString(Protocols.G2l_playerVerify.GAMETOKEN);
		Player player=PlayerLoginService.getInstance().checkGameToken(uuid, gameToken);
		pMsg.getJson().put(Protocols.SUBCODE, Protocols.L2g_playerVerify.subCode_value);
		if(player!=null){ //验证通过
			pMsg.getJson().put(Protocols.L2g_playerVerify.NAME, player.getName());
			pMsg.getJson().put(Protocols.L2g_playerVerify.COIN, player.getCoin());
			GameServer gs=GameServerService.getInstance().getGameServer(pMsg);
			player.setServerId(gs.getServerId());
			PlayerManager.getInstance().playerOnline(player);
		}else{
			pMsg.getJson().put(Protocols.ERRORCODE, "验证失败");
		}
		GameServerMsgSender.getInstance().addMsg(pMsg);
	}

	private void handlePlayerLogout(ProtocolMsg pMsg) {
		String uuid = pMsg.getJson().getString(Protocols.G2l_playerLogout.UUID);
		PlayerManager.getInstance().playerOffline(uuid);
	}

	@Override
	public int getMainCode() {
		return Protocols.MainCode.PLAYERVERIFY;
	}

}
