package com.gz.gamecity.login.service.gm;

import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gz.gamecity.delay.DelayMsg;
import com.gz.gamecity.delay.InnerDelayManager;
import com.gz.gamecity.login.GameServerMsgSender;
import com.gz.gamecity.login.PlayerMsgSender;
import com.gz.gamecity.login.bean.GameServer;
import com.gz.gamecity.login.service.gameserver.GameServerService;
import com.gz.gamecity.protocol.Protocols;
import com.gz.util.DateUtil;
import com.gz.websocket.msg.ClientMsg;
import com.gz.websocket.msg.ProtocolMsg;

public class GameNoticeManager {
//	private static final Logger log = Logger.getLogger(GameNoticeManager.class);
	private static GameNoticeManager instance;
	private static ConcurrentHashMap<String, GameNotice> map_msg=new ConcurrentHashMap<>();

	public static synchronized GameNoticeManager getInstance() {
		if(instance == null)
			instance =new GameNoticeManager();
		return instance;
	}
	
	private GameNoticeManager(){
		
	}
	
	public void addGameNotice(ClientMsg cMsg){
		JSONObject json = cMsg.getJson();
		cMsg.put(Protocols.SUBCODE, Protocols.L2gm_send_game_notice.subCode_value);
		GameNotice gn = new GameNotice();
		gn.content = json.getString(Protocols.Gm2l_send_game_notice.CONTENT);
		if(gn.content==null || gn.content.trim().equals("") || gn.content.length()>50){
			cMsg.put(Protocols.ERRORCODE, "消息内容有问题");
			PlayerMsgSender.getInstance().addMsg(cMsg);
			return;
		}
		
		gn.interval = json.getLongValue(Protocols.Gm2l_send_game_notice.INTERVAL);
		if(gn.interval<10){
			cMsg.put(Protocols.ERRORCODE, "间隔时间太短，不能超过10秒");
			PlayerMsgSender.getInstance().addMsg(cMsg);
			return;
		}
		
		String startTime = json.getString(Protocols.Gm2l_send_game_notice.STARTTIME);
		Calendar c_start = DateUtil.parse(startTime);
		gn.startTime = c_start.getTimeInMillis();
		gn.startStr = startTime;
		
		String endTime = json.getString(Protocols.Gm2l_send_game_notice.ENDTIME);
		Calendar c_end = DateUtil.parse(endTime);
		gn.endTime = c_end.getTimeInMillis();
		gn.endStr = endTime;
		
		long delayTime = gn.startTime - System.currentTimeMillis();
		if (delayTime < 1000)
			delayTime = 1000l;
		if(gn.startTime > gn.endTime){
			cMsg.put(Protocols.ERRORCODE, "起始或者结束时间有错误");
			PlayerMsgSender.getInstance().addMsg(cMsg);
			return;
		}
		
		DelayMsg msg = new DelayMsg(delayTime){
			@Override
			public void onTimeUp() {
				sendGameNotice(this.id);
			}
		};

		gn.uuid = msg.getId();
		cMsg.put(Protocols.L2gm_send_game_notice.UUID, gn.uuid);
		PlayerMsgSender.getInstance().addMsg(cMsg);
		map_msg.put(gn.uuid, gn);
		InnerDelayManager.getInstance().addDelayItem(msg);
	}
	
	private void sendGameNotice(String uuid){
		GameNotice gn = map_msg.remove(uuid);
		if(gn == null)
			return;
		if(gn.endTime<System.currentTimeMillis())
			return;
		
		
		JSONObject j = new JSONObject();
		j.put(Protocols.MAINCODE, Protocols.L2g_sendGameNotice.mainCode_value);
		j.put(Protocols.SUBCODE, Protocols.L2g_sendGameNotice.subCode_value);
		j.put(Protocols.L2g_sendGameNotice.CONTENT, gn.content);
		
		HashMap<String, GameServer> servers = GameServerService.getInstance().getMap_server();
		for (GameServer gs : servers.values()) {
			if(gs.isOnline()){
				ProtocolMsg msg = new ProtocolMsg();
				msg.setJson(j);
				msg.setChannel(gs.getChannel());
				GameServerMsgSender.getInstance().addMsg(msg);
			}
		}
		
		
		long delayTime = gn.interval * 1000;
		DelayMsg delayMsg = new DelayMsg(delayTime){
			@Override
			public void onTimeUp() {
				sendGameNotice(this.id);
			}
		};
		delayMsg.setId(gn.uuid);
		map_msg.put(gn.uuid, gn);
		InnerDelayManager.getInstance().addDelayItem(delayMsg);
	}
	
	public void getNoticeList(ClientMsg msg){
		msg.put(Protocols.SUBCODE, Protocols.L2gm_game_notice_list.subCode_value);
		JSONObject[] arr = new JSONObject[map_msg.size()];
		int i=0;
		for (GameNotice gn : map_msg.values()) {
			JSONObject j = new JSONObject();
			j.put(Protocols.L2gm_game_notice_list.List.CONTENT, gn.content);
			j.put(Protocols.L2gm_game_notice_list.List.UUID, gn.uuid);
			j.put(Protocols.L2gm_game_notice_list.List.INTERVAL, gn.interval);
			j.put(Protocols.L2gm_game_notice_list.List.ENDTIME, gn.endStr);
			j.put(Protocols.L2gm_game_notice_list.List.STARTTIME, gn.startStr);
			arr[i] = j;
			i++;
		}
		msg.put(Protocols.L2gm_game_notice_list.LIST, arr);
		PlayerMsgSender.getInstance().addMsg(msg);
	}
	
	public void delNotice(ClientMsg msg){
		msg.put(Protocols.SUBCODE, Protocols.L2gm_del_game_notice.subCode_value);
		String uuid = msg.getJson().getString(Protocols.Gm2l_del_game_notice.UUID);
		map_msg.remove(uuid);
		PlayerMsgSender.getInstance().addMsg(msg);
	}
	
}



class GameNotice{
	public String content;
	public String uuid;
	public long startTime;
	public long endTime;
	public String startStr;
	public String endStr;
	public long interval;
}
