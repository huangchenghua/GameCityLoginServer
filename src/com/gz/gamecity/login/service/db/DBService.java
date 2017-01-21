package com.gz.gamecity.login.service.db;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import com.alibaba.fastjson.JSONObject;
import com.gz.gamecity.bean.EventLogType;
import com.gz.gamecity.bean.Mail;
import com.gz.gamecity.bean.Player;
import com.gz.gamecity.login.GameServerMsgSender;
import com.gz.gamecity.login.GmMsgSender;
import com.gz.gamecity.login.PlayerManager;
import com.gz.gamecity.login.PlayerMsgSender;
import com.gz.gamecity.login.bean.GameServer;
import com.gz.gamecity.login.config.ConfigField;
import com.gz.gamecity.login.db.PlayerDataDao;
import com.gz.gamecity.login.handler.impl.PlayerMsgHandler;
import com.gz.gamecity.login.service.gameserver.GameServerService;
import com.gz.gamecity.protocol.Protocols;
import com.gz.util.Config;
import com.gz.util.DateUtil;
import com.gz.websocket.msg.ClientMsg;
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
		case Protocols.DB_login_new_mail.subCode_value:
			handleNewMail(j);
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

		case Protocols.DB_friend_add.subCode_value:
			handleFriendAdd(j);
			break;
		case Protocols.DB_friend_del.subCode_value:
			handleFriendDel(j);
			break;
		case Protocols.DB_friend_list.subCode_value:
			handleFriendList(j);
			break;
		case Protocols.DB_friend_other_info.subCode_value:
			handleFriendOtherInfo(j);
			break;

		case Protocols.DB_search_player.subCode_value:
			handleSearchPlayer(j);
			break;
		case Protocols.DB_freeze.subCode_value:
			handleFreezePlayer(j);
			break;
		case Protocols.DB_silent.subCode_value:
			handleSilent(j);
			break;
		case Protocols.DB_clean_mail.subCode_value:
			handleCleanMail();
			break;
		case Protocols.DB_gm_add_coin.subCode_value:
			handleGmAddCoin(j);
			break;
		case Protocols.DB_player_signin.subCode_value:	
			handlePlayerSignin(j);
			break;
		case Protocols.DB_charge_record.subCode_value:
			handleCharge(j);
			break;
		case Protocols.DB_req_charge_list.subCode_value:
			handleReqChargeList(j);
			break;
		case Protocols.DB_player_gift_change.subCode_value:
			handlePlayerGiftChange(j);
			break;
		case Protocols.DB_unfreeze.subCode_value:
			handleUnfreeze(j);
			break;
		case Protocols.DB_unsilent.subCode_value:
			handleUnsilent(j);
			break;
		default:
			break;
		}
	}
	
	private void handleUnsilent(JSONObject j) {
		String uuid = j.getString(Protocols.DB_unsilent.UUID);
		dao.unsilentPlayer(uuid);
	}

	private void handleUnfreeze(JSONObject j) {
		String uuid = j.getString(Protocols.DB_unfreeze.UUID);
		dao.unfreezePlayer(uuid);
	}

	private void handlePlayerGiftChange(JSONObject j) {
		dao.updatePlayerGift(j);
	}

	private void handleReqChargeList(JSONObject j) {
		ClientMsg msg = new ClientMsg();
		String gm_uuid = j.getString(Protocols.DB_req_charge_list.GM_UUID);
		String uuid = j.getString(Protocols.DB_req_charge_list.UUID);
		Player gm=PlayerManager.getInstance().getOnlineGm().get(gm_uuid);
		if(gm==null)
			return;
		msg.setChannel(gm.getChannel());
		msg.put(Protocols.MAINCODE, Protocols.L2gm_req_charge.mainCode_value);
		msg.put(Protocols.SUBCODE, Protocols.L2gm_req_charge.subCode_value);
		msg.put(Protocols.L2gm_req_charge.LIST, dao.getChargeRecordList(uuid));
		PlayerMsgSender.getInstance().addMsg(msg);
	}

	private void handleCharge(JSONObject j) {
		dao.recordCharge(j);
	}

	private void handlePlayerSignin(JSONObject j) {
		String uuid = j.getString(Protocols.DB_player_signin.UUID);
		String date = j.getString(Protocols.DB_player_signin.DATE);
		int days = j.getIntValue(Protocols.DB_player_signin.SIGN_DAYS);
		dao.playerSignin(uuid, date, days);
	}

	private void handleGmAddCoin(JSONObject j) {
		String uuid = j.getString(Protocols.DB_gm_add_coin.UUID);
		Player player = dao.getPlayer(uuid);
		if(player==null) return;
		long change = j.getLongValue(Protocols.DB_gm_add_coin.COIN);
		long coin = player.getCoin()+change;
		if(coin<0)coin=0;
		if(coin>Config.instance().getLValue(ConfigField.MAX_COIN))
			coin=Config.instance().getLValue(ConfigField.MAX_COIN);
		dao.recordCoinChange(uuid, coin, change, EventLogType.gm_add.getType(), UUID.randomUUID().toString());
		dao.updatePlayerCoin(uuid, coin);
	}

	private void handleCleanMail() {
		Calendar c = DateUtil.getMailCleanTime();
		String datetime = DateUtil.getDateTime(DateUtil.DEFAULT_PATTERN, c);
		dao.cleanMail(datetime);
	}

	private void handleFriendOtherInfo(JSONObject j) {
		/*
		<field name="uuid_self" type="string"/>
		<field name="uuid" type="string"/>
		<field name="lv" type="int"/>
		<field name="name" type="string"/>
		<field name="head" type="int"/>
		<field name="vip" type="int"/>
		<field name="finance" type="int"/>
		<field name="coin" type="long"/>
		<field name="charm" type="int"/>
		<field name="sign" type="string"/>
		 */
		String strName = j.getString(Protocols.G2l_friend_other_info.NAME);
		List<Player> list = dao.searchPlayer(strName);
		
		int nServerId = j.getInteger(Protocols.G2l_friend_other_info.SERVER_ID);
		GameServer gs = GameServerService.getInstance().getGameServer(nServerId);
		if (gs == null)
			return;
		ProtocolMsg msg = new ProtocolMsg();
		
		if (list.isEmpty()) {
			msg.put(Protocols.ERRORCODE, "找不到玩家");
		} else {
			Player player = list.get(0);
			msg.put(Protocols.L2g_friend_other_info.UUID, player.getUuid());
			msg.put(Protocols.L2g_friend_other_info.SEX, player.getSex());
			msg.put(Protocols.L2g_friend_other_info.LV, player.getLvl());
			msg.put(Protocols.L2g_friend_other_info.NAME, player.getName());
			msg.put(Protocols.L2g_friend_other_info.HEAD, player.getHead());
			msg.put(Protocols.L2g_friend_other_info.VIP, player.getVip());
			msg.put(Protocols.L2g_friend_other_info.FINANCE, player.getFinance());
			msg.put(Protocols.L2g_friend_other_info.COIN, player.getCoin());
			msg.put(Protocols.L2g_friend_other_info.CHARM, player.getCharm());
			msg.put(Protocols.L2g_friend_other_info.SIGN, player.getSign());
		}
		
		msg.put(Protocols.MAINCODE, Protocols.L2g_friend_other_info.mainCode_value);
		msg.put(Protocols.SUBCODE, Protocols.L2g_friend_other_info.subCode_value);
		msg.put(Protocols.L2g_friend_other_info.UUID_SELF, j.getString(Protocols.G2l_friend_other_info.UUID_SELF));
		msg.setChannel(gs.getChannel());
		GameServerMsgSender.getInstance().addMsg(msg);
	}
	
	private void handleFriendList(JSONObject j) {
		String strUuid = j.getString(Protocols.G2l_friend_list.UUID);
		JSONObject[] szObj = dao.getFriendList(strUuid);

		int nServerId = j.getInteger(Protocols.G2l_friend_list.SERVER_ID);
		GameServer gs = GameServerService.getInstance().getGameServer(nServerId);
		if (gs == null)
			return;
		
		ProtocolMsg msg = new ProtocolMsg();

		if (szObj != null)
			msg.put(Protocols.L2g_friend_list.PLAYER_LIST, szObj);
		
		msg.put(Protocols.MAINCODE, Protocols.L2g_friend_list.mainCode_value);
		msg.put(Protocols.SUBCODE, Protocols.L2g_friend_list.subCode_value);
		msg.put(Protocols.L2g_friend_list.UUID, strUuid);
		msg.setChannel(gs.getChannel());
		GameServerMsgSender.getInstance().addMsg(msg);
	}
	
	private void handleFriendDel(JSONObject j) {
		String strUuid = j.getString(Protocols.G2l_friend_del.UUID);
		String strUuidOther = j.getString(Protocols.G2l_friend_del.UUID_OTHER);
		int nCnt = dao.deleteFriend(strUuid, strUuidOther);
		int nServerId = j.getInteger(Protocols.G2l_friend_add.SERVER_ID);
		GameServer gs = GameServerService.getInstance().getGameServer(nServerId);
		if (gs == null)
			return;
		
		ProtocolMsg msg = new ProtocolMsg();

		msg.put(Protocols.L2g_friend_del.UUID_OTHER, strUuidOther);
		
		msg.put(Protocols.MAINCODE, Protocols.L2g_friend_del.mainCode_value);
		msg.put(Protocols.SUBCODE, Protocols.L2g_friend_del.subCode_value);
		msg.put(Protocols.L2g_friend_del.RET, nCnt);
		msg.put(Protocols.L2g_friend_del.UUID, strUuid);
		msg.setChannel(gs.getChannel());
		GameServerMsgSender.getInstance().addMsg(msg);
	}

	private void handleFriendAdd(JSONObject j) {
		int nCnt = dao.insertFriend(j);
		int nServerId = j.getInteger(Protocols.G2l_friend_add.SERVER_ID);
		GameServer gs = GameServerService.getInstance().getGameServer(nServerId);
		if (gs == null)
			return;
		
		ProtocolMsg msg = new ProtocolMsg();
		msg.put(Protocols.MAINCODE, Protocols.L2g_friend_add.mainCode_value);
		msg.put(Protocols.SUBCODE, Protocols.L2g_friend_add.subCode_value);
		
		msg.put(Protocols.L2g_friend_add.RET, nCnt);
		msg.put(Protocols.L2g_friend_add.UUID_MY, j.getString(Protocols.G2l_friend_add.UUID_MY));
		msg.put(Protocols.L2g_friend_add.UUID_OTHER, j.getString(Protocols.G2l_friend_add.UUID_OTHER));
		
		msg.setChannel(gs.getChannel());
		GameServerMsgSender.getInstance().addMsg(msg);
		
	}
	

	private void handleSilent(JSONObject j) {
		String uuid = j.getString(Protocols.DB_silent.UUID);
		dao.silentPlayer(uuid);
	}

	private void handleFreezePlayer(JSONObject j) {
		String uuid = j.getString(Protocols.DB_freeze.UUID);
		dao.freezePlayer(uuid);
	}

	private void handleSearchPlayer(JSONObject j) {
		List<Player> list=dao.searchPlayer(j.getString(Protocols.DB_search_player.NAME));
		ClientMsg msg = new ClientMsg();
		String uuid = j.getString(Protocols.DB_search_player.GM_UUID);
		Player gm=PlayerManager.getInstance().getOnlineGm().get(uuid);
		if(gm==null)
			return;
		msg.setChannel(gm.getChannel());
		msg.put(Protocols.MAINCODE, Protocols.L2gm_search_player.mainCode_value);
		msg.put(Protocols.SUBCODE, Protocols.L2gm_search_player.subCode_value);
		JSONObject[] ps=new JSONObject[list.size()];
		for (int i = 0; i < ps.length; i++) {
			ps[i] = new JSONObject();
			ps[i].put(Protocols.L2gm_search_player.Player_info.NAME, list.get(i).getName());
			ps[i].put(Protocols.L2gm_search_player.Player_info.UUID, list.get(i).getUuid());
			ps[i].put(Protocols.L2gm_search_player.Player_info.COIN, list.get(i).getCoin());
			ps[i].put(Protocols.L2gm_search_player.Player_info.HEAD, list.get(i).getHead());
			ps[i].put(Protocols.L2gm_search_player.Player_info.VIP, list.get(i).getVip());
			ps[i].put(Protocols.L2gm_search_player.Player_info.CHARGE_TOTAL, list.get(i).getCharge_total());
			ps[i].put(Protocols.L2gm_search_player.Player_info.SEX, list.get(i).getSex());
			ps[i].put(Protocols.L2gm_search_player.Player_info.LVL, list.get(i).getLvl());
			ps[i].put(Protocols.L2gm_search_player.Player_info.FINANCE, list.get(i).getFinance());
			ps[i].put(Protocols.L2gm_search_player.Player_info.CHARM, list.get(i).getCharm());
			ps[i].put(Protocols.L2gm_search_player.Player_info.SILENT, list.get(i).isSilent());
			ps[i].put(Protocols.L2gm_search_player.Player_info.FROZEN, list.get(i).isFrozen());
		}
		msg.put(Protocols.L2gm_search_player.PLAYER_INFO, ps);
		GmMsgSender.getInstance().addMsg(msg); 
	}

	private void handleNewMail(JSONObject j) {
		dao.addNewMail(j);
		
	}

	private void handleGiftList(JSONObject j) {
		String uuid = j.getString(Protocols.DB_player_gift_list.UUID);
		int[] info=dao.getPlayerGiftList(uuid);
		if(info==null){
			dao.insertPlayerGiftList(uuid);
			info = new int[8];
		}
		ProtocolMsg msg = new ProtocolMsg();
		msg.put(Protocols.MAINCODE, Protocols.L2g_player_gift_list.mainCode_value);
		msg.put(Protocols.SUBCODE, Protocols.L2g_player_gift_list.subCode_value);
		msg.put(Protocols.L2g_player_gift_list.COUNT, info);
		msg.put(Protocols.L2g_player_gift_list.UUID, uuid);
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
		String mailId = j.getString(Protocols.DB_login_take_mail.MAILID);
		Mail mail = dao.getMail(mailId);
		if (mail == null)
			return;
		String player_uuid = j.getString(Protocols.DB_login_take_mail.UUID);
		if (mail.getPlayer_uuid().equals(player_uuid) && !mail.isTaken()) {
			String attachments = mail.getAttachments();
			if (attachments != null && !attachments.equals("")) {
				GameServer gs = GameServerService.getInstance()
						.getGameServer(j.getIntValue(Protocols.DB_login_take_mail.SERVERID));
				ProtocolMsg msg = new ProtocolMsg();
				msg.setChannel(gs.getChannel());
				msg.put(Protocols.MAINCODE, Protocols.L2g_take_mail.mainCode_value);
				msg.put(Protocols.SUBCODE, Protocols.L2g_take_mail.subCode_value);
				msg.put(Protocols.L2g_take_mail.UUID, player_uuid);
				msg.put(Protocols.L2g_take_mail.ATTACHMENTS, attachments);
				GameServerMsgSender.getInstance().addMsg(msg);
				dao.takeMail(mailId);
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
