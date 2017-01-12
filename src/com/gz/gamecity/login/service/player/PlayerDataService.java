package com.gz.gamecity.login.service.player;

import com.alibaba.fastjson.JSONObject;
import com.gz.gamecity.bean.Mail;
import com.gz.gamecity.bean.Player;
import com.gz.gamecity.login.PlayerManager;
import com.gz.gamecity.login.bean.GameServer;
import com.gz.gamecity.login.logic.LogicHandler;
import com.gz.gamecity.login.service.db.DBService;
import com.gz.gamecity.login.service.gameserver.GameServerService;
import com.gz.gamecity.protocol.Protocols;
import com.gz.websocket.msg.BaseMsg;
import com.gz.websocket.msg.ProtocolMsg;

public class PlayerDataService implements LogicHandler {

	@Override
	public void handleMsg(BaseMsg msg) {
		ProtocolMsg pMsg = (ProtocolMsg) msg;
		if (!GameServerService.getInstance().checkServer(pMsg)) {
			return;
		}
		int subCode = pMsg.getJson().getIntValue(Protocols.SUBCODE);
		switch (subCode) {
		case Protocols.G2l_coinChange.subCode_value:
			handlePlayerCoinChange(pMsg);
			break;
		case Protocols.G2l_data_change.subCode_value:
			handlePlayerDataChange(pMsg);
			break;
		case Protocols.G2l_mail_list.subCode_value:
			handleReqMailList(pMsg);
			break;
		case Protocols.G2l_new_mail.subCode_value:
			handleNewMail(pMsg);
			break;
		case Protocols.G2l_open_mail.subCode_value:
			handleOpenMail(pMsg);
			break;
		case Protocols.G2l_take_mail.subCode_value:
			handleTakeMail(pMsg);
			break;
		case Protocols.G2l_delete_mail.subCode_value:
			handleDelMail(pMsg);
			break;
		case Protocols.G2l_player_gift_list.subCode_value:
			handlePlayerGiftList(pMsg);
			break;
		}
	}

	private void handlePlayerGiftList(ProtocolMsg pMsg) {
		GameServer gs = GameServerService.getInstance().getGameServer(pMsg);
		pMsg.put(Protocols.MAINCODE, Protocols.DB_player_gift_list.mainCode_value);
		pMsg.put(Protocols.SUBCODE, Protocols.DB_player_gift_list.subCode_value);
		pMsg.put(Protocols.DB_player_gift_list.SERVERID, gs.getServerId());
		DBService.getInstance().addMsg(pMsg.getJson());
	}

	private void handleDelMail(ProtocolMsg pMsg) {
		pMsg.put(Protocols.MAINCODE, Protocols.DB_login_delete_mail.mainCode_value);
		pMsg.put(Protocols.SUBCODE, Protocols.DB_login_delete_mail.subCode_value);
		DBService.getInstance().addMsg(pMsg.getJson());
	}

	private void handleTakeMail(ProtocolMsg pMsg) {
		pMsg.put(Protocols.MAINCODE, Protocols.DB_login_take_mail.mainCode_value);
		pMsg.put(Protocols.SUBCODE, Protocols.DB_login_take_mail.subCode_value);
		DBService.getInstance().addMsg(pMsg.getJson());
	}

	private void handleOpenMail(ProtocolMsg pMsg) {
		pMsg.put(Protocols.MAINCODE, Protocols.DB_login_open_mail.mainCode_value);
		pMsg.put(Protocols.SUBCODE, Protocols.DB_login_open_mail.subCode_value);
		DBService.getInstance().addMsg(pMsg.getJson());
	}

	private void handleNewMail(ProtocolMsg pMsg) {
		pMsg.put(Protocols.MAINCODE, Protocols.DB_login_new_mail.mainCode_value);
		pMsg.put(Protocols.SUBCODE, Protocols.DB_login_new_mail.subCode_value);
		DBService.getInstance().addMsg(pMsg.getJson());
	}

	private void handleReqMailList(ProtocolMsg pMsg) {
		GameServer gs =GameServerService.getInstance().getGameServer(pMsg);
		String uuid=pMsg.getJson().getString(Protocols.G2l_mail_list.UUID);
		JSONObject _j = new JSONObject();
		_j.put(Protocols.MAINCODE, Protocols.DB_login_mail_list.mainCode_value);
		_j.put(Protocols.SUBCODE, Protocols.DB_login_mail_list.subCode_value);
		_j.put(Protocols.DB_login_mail_list.PLAYER_UUID, uuid);
		_j.put(Protocols.DB_login_mail_list.SERVERID, gs.getServerId());
		DBService.getInstance().addMsg(_j);
	}

	private void handlePlayerCoinChange(ProtocolMsg pMsg) {
		String uuid = pMsg.getJson().getString(Protocols.G2l_coinChange.UUID);
		String log_uuid = pMsg.getJson().getString(Protocols.G2l_coinChange.LOG_UUID);
		long coin = pMsg.getJson().getLong(Protocols.G2l_coinChange.COIN);
		long change = pMsg.getJson().getLong(Protocols.G2l_coinChange.CHANGE);
		int type = pMsg.getJson().getIntValue(Protocols.G2l_coinChange.TYPE);
		Player player = PlayerManager.getInstance().getOnlinePlayer(uuid);
		if (player != null) {
			player.setCoin(coin);
		}
		// PlayerDao.updatePlayerCoin(player);
		JSONObject _j = new JSONObject();
		_j.put(Protocols.MAINCODE, Protocols.DB_login_update_player_coin.mainCode_value);
		_j.put(Protocols.SUBCODE, Protocols.DB_login_update_player_coin.subCode_value);
		_j.put(Protocols.DB_login_update_player_coin.PLAYER_UUID, uuid);
		_j.put(Protocols.DB_login_update_player_coin.COIN, coin);
		DBService.getInstance().addMsg(_j);

		JSONObject j = new JSONObject();
		j.put(Protocols.MAINCODE, Protocols.DB_login_coin_change.mainCode_value);
		j.put(Protocols.SUBCODE, Protocols.DB_login_coin_change.subCode_value);
		j.put(Protocols.DB_login_coin_change.PLAYER_UUID, uuid);
		j.put(Protocols.DB_login_coin_change.LOG_UUID, log_uuid);
		j.put(Protocols.DB_login_coin_change.COIN, coin);
		j.put(Protocols.DB_login_coin_change.CHANGE, change);
		j.put(Protocols.DB_login_coin_change.TYPE, type);
		DBService.getInstance().addMsg(j);
	}

	private void handlePlayerDataChange(ProtocolMsg pMsg) {
		String uuid = pMsg.getJson().getString(Protocols.G2l_coinChange.UUID);
//		JSONObject data = pMsg.getJson().getJSONObject(Protocols.G2l_data_change.DATA);
		Player player = PlayerManager.getInstance().getOnlinePlayer(uuid);
		StringBuffer sb=new StringBuffer("");
		String name = pMsg.getJson().getString(Protocols.G2l_data_change.NAME);
		if(name!=null){
			if(player!=null)
				player.setName(name);
			sb.append("name='").append(name).append("',");
		}
		
		String head_str = pMsg.getJson().getString(Protocols.G2l_data_change.HEAD);
		if(head_str!=null){
			int head = pMsg.getJson().getIntValue(Protocols.G2l_data_change.HEAD);
			if(player!=null)
				player.setHead(head);
			sb.append("head='").append(head).append("',");
		}
		
		String vip_str = pMsg.getJson().getString(Protocols.G2l_data_change.VIP);
		if(vip_str!=null){
			int vip = pMsg.getJson().getIntValue(Protocols.G2l_data_change.VIP);
			if(player!=null)
				player.setVip(vip);
			sb.append("head=").append(vip).append(",");
		}
		
		String charge_total_str = pMsg.getJson().getString(Protocols.G2l_data_change.CHARGE_TOTAL);
		if(charge_total_str!=null){
			long charge_total = pMsg.getJson().getLongValue(Protocols.G2l_data_change.CHARGE_TOTAL);
			if(player!=null)
				player.setCharge_total(charge_total);
			sb.append("charge_total=").append(charge_total).append(",");
		}
		
		String sex_str = pMsg.getJson().getString(Protocols.G2l_data_change.SEX);
		if(sex_str!=null){
			byte sex = pMsg.getJson().getByteValue(Protocols.G2l_data_change.SEX);
			if(player!=null)
				player.setSex(sex);
			sb.append("sex=").append(sex).append(",");
		}
		
		String lvl_str = pMsg.getJson().getString(Protocols.G2l_data_change.LVL);
		if(lvl_str!=null){
			int lvl = pMsg.getJson().getIntValue(Protocols.G2l_data_change.LVL);
			if(player!=null)
				player.setLvl(lvl);
			sb.append("lvl=").append(lvl).append(",");
		}
		
		String finance_str = pMsg.getJson().getString(Protocols.G2l_data_change.FINANCE);
		if(finance_str!=null){
			int finance = pMsg.getJson().getIntValue(Protocols.G2l_data_change.FINANCE);
			if(player!=null)
				player.setFinance(finance);
			sb.append("finance=").append(finance).append(",");
		}
		
		String sign = pMsg.getJson().getString(Protocols.G2l_data_change.FINANCE);
		if(sign!=null){
			if(player!=null)
				player.setSign(sign);
			sb.append("sign='").append(sign).append("',");
		}
		
		String charm_str = pMsg.getJson().getString(Protocols.G2l_data_change.CHARM);
		if(charm_str!=null){
			int charm = pMsg.getJson().getIntValue(Protocols.G2l_data_change.CHARM);
			if(player!=null)
				player.setCharm(charm);
			sb.append("charm=").append(charm).append(",");
		}
		JSONObject j = pMsg.getJson();
		j.put(Protocols.MAINCODE, Protocols.DB_data_change.mainCode_value);
		j.put(Protocols.SUBCODE, Protocols.DB_data_change.subCode_value);
		DBService.getInstance().addMsg(j);
	}

	@Override
	public int getMainCode() {
		return Protocols.MainCode.PLAYER_DATA_LOGIN;
	}

}
