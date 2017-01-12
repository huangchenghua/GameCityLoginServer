package com.gz.gamecity.login.service.db;

import java.util.concurrent.LinkedBlockingQueue;

import com.alibaba.fastjson.JSONObject;
import com.gz.gamecity.bean.Mail;
import com.gz.gamecity.login.GameServerMsgSender;
import com.gz.gamecity.login.bean.GameServer;
import com.gz.gamecity.login.db.PlayerDataDao;
import com.gz.gamecity.login.service.gameserver.GameServerService;
import com.gz.gamecity.protocol.Protocols;
import com.gz.websocket.msg.ProtocolMsg;

public class DBService {
	private static DBService instance;
	
	private LinkedBlockingQueue<JSONObject> list = new LinkedBlockingQueue<>();
	private PlayerDataDao dao = new PlayerDataDao();
	public static synchronized DBService getInstance() {
		if(instance == null)
			instance = new DBService();
		return instance;
	}
	
	private DBService(){
		start();
	}
	
	public void addMsg(JSONObject j){
		try {
			list.put(j);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void start(){
		Thread t = new Thread(){
			@Override
			public void run() {
				while(true){
					try {
						JSONObject j = list.take();
						handleMsg(j);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}
			}
		};
		t.start();
	}
	
	private void handleMsg(JSONObject j){
		int subcode = j.getIntValue(Protocols.SUBCODE);
		switch (subcode) {
		case Protocols.DB_login_coin_change.subCode_value:
			String player_uuid = j.getString(Protocols.DB_game_coin_change.PLAYER_UUID);
			String log_uuid = j.getString(Protocols.DB_game_coin_change.LOG_UUID);
			long coin = j.getLongValue(Protocols.DB_game_coin_change.COIN);
			long change = j.getLongValue(Protocols.DB_game_coin_change.CHANGE);
			int type = j.getIntValue(Protocols.DB_game_coin_change.TYPE);
			dao.recordCoinChange(player_uuid, coin, change, type, log_uuid);
			break;
		case Protocols.DB_login_update_player_coin.subCode_value:
			player_uuid = j.getString(Protocols.DB_login_update_player_coin.PLAYER_UUID);
			coin = j.getLongValue(Protocols.DB_login_update_player_coin.COIN);
			dao.updatePlayerCoin(player_uuid, coin);
			break;
		case Protocols.DB_data_change.subCode_value:
			dao.updatePlayerData(j);
			break;
		case Protocols.DB_login_mail_list.subCode_value:
			handleReqMailList(j);
			break;
		case Protocols.DB_login_open_mail.subCode_value:
			handleOpenMail(j);
			break;
		case Protocols.DB_login_take_mail.subCode_value:
			handleTakeMail(j);
			break;
		case Protocols.DB_login_delete_mail.subCode_value:
			handleDelMail(j);
			break;
		case Protocols.DB_player_gift_list.subCode_value:
			handleGiftList(j);
			break;
		default:
			break;
		}
	}
	
	private void handleGiftList(JSONObject j) {
		String uuid = j.getString(Protocols.DB_player_gift_list.UUID);
		int[][] info=dao.getPlayerGiftList(uuid);
		if(info==null){
			dao.insertPlayerGiftList(uuid);
			info = new int[2][8];
		}
		ProtocolMsg msg = new ProtocolMsg();
		msg.put(Protocols.MAINCODE, Protocols.L2g_player_gift_list.mainCode_value);
		msg.put(Protocols.SUBCODE, Protocols.L2g_player_gift_list.subCode_value);
		msg.put(Protocols.L2g_player_gift_list.ID, info[0]);
		msg.put(Protocols.L2g_player_gift_list.COUNT, info[1]);
		int serverId = j.getIntValue(Protocols.DB_player_gift_list.SERVERID);
		GameServer gs = GameServerService.getInstance().getGameServer(serverId);
		msg.setChannel(gs.getChannel());
		GameServerMsgSender.getInstance().addMsg(msg);
	}

	private void handleDelMail(JSONObject j) {
		String ids = j.getString(Protocols.DB_login_delete_mail.MAILIDS);
		if(ids!=null && !ids.equals("")){
			dao.delMail(ids);
		}
	}

	private void handleTakeMail(JSONObject j) {
		String mailId=j.getString(Protocols.DB_login_take_mail.MAILID);
		Mail mail =dao.getMail(mailId);
		if(mail==null)
			return;
		String player_uuid = j.getString(Protocols.DB_login_take_mail.UUID);
		if(mail.getPlayer_uuid().equals(player_uuid) && !mail.isTaken()){
			String attachments = mail.getAttachments();
			if(attachments!=null && !attachments.equals("")){
				// TODO 给玩家发送东西
				
			}
		}
	}

	private void handleOpenMail(JSONObject j) {
		String mailId=j.getString(Protocols.DB_login_open_mail.MAILID);
		dao.openMail(mailId);
	}

	private void handleReqMailList(JSONObject j){
		String player_uuid = j.getString(Protocols.DB_login_mail_list.PLAYER_UUID);
		int serverId= j.getIntValue(Protocols.DB_login_mail_list.SERVERID);
		JSONObject[] list=dao.getPlayerMailList(player_uuid);
		ProtocolMsg msg = new ProtocolMsg();
		msg.put(Protocols.MAINCODE, Protocols.L2g_mail_list.mainCode_value);
		msg.put(Protocols.SUBCODE, Protocols.L2g_mail_list.subCode_value);
		msg.put(Protocols.L2g_mail_list.UUID, player_uuid);
		msg.put(Protocols.L2g_mail_list.MAIL_LIST, list);
		GameServer gs = GameServerService.getInstance().getGameServer(serverId);
		msg.setChannel(gs.getChannel());
		GameServerMsgSender.getInstance().addMsg(msg);
	}
	
	
}
