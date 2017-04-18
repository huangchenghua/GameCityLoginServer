package com.gz.gamecity.login.handler.impl;

import java.util.Map;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.Charsets;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.gz.gamecity.bean.Player;
import com.gz.gamecity.login.GameServerMsgSender;
import com.gz.gamecity.login.PlayerManager;
import com.gz.gamecity.login.bean.GameServer;
import com.gz.gamecity.login.config.ConfigField;
import com.gz.gamecity.login.service.db.DBService;
import com.gz.gamecity.login.service.gameserver.GameServerService;
import com.gz.gamecity.protocol.Protocols;
import com.gz.http.HttpDecoderAndEncoder;
import com.gz.http.HttpServerMsgHandler;
import com.gz.util.Config;
import com.gz.util.MD5Util;
import com.gz.websocket.msg.ProtocolMsg;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

public class ChargeHandler implements HttpServerMsgHandler {
	// http://192.168.1.83:8080/game/charge
	private static final Logger log = Logger.getLogger(ChargeHandler.class);
	private static final String appId_gamecity="gz9576b7277fdb5db2";
	private static final String appsecret="328b16f5b9d258fabfc76d89a461fc6c";
	@Override
	public String getPath() {
		return "/game/charge";
	}

	@Override
	public void doPost(ChannelHandlerContext ctx, FullHttpRequest request, Map<String, String> parmMap) {
		String jsonStr = request.content().toString(Charsets.toCharset(CharEncoding.UTF_8));
		System.out.println(jsonStr);
		log.info(jsonStr);
		JSONObject j = JSONObject.parseObject(jsonStr);
		HttpDecoderAndEncoder.Response(ctx, request, "success");
		
		
		int amount = j.getIntValue("amount");
		String appId = j.getString("appId");
		String notifyParameters = j.getString("notifyParameters");
		String orderId = j.getString("orderId");
		String payWay = j.getString("payWay");
		String status = j.getString("status");
		String uuid = j.getString("uuid");
		String sign = j.getString("sign");
		
		String s = "amount="+amount+"&appId="+appId_gamecity+"&notifyParameters="+notifyParameters+"&orderId="+orderId+"&payWay="+payWay+"&status="+status+"&uuid="+uuid+"&appsecret="+appsecret;
		String md5_str = MD5Util.string2MD5(s);
		if(!md5_str.equals(sign)){
			log.warn("md5楠岃瘉澶辫触");
			return;
		}
		j.put(Protocols.MAINCODE, Protocols.DB_charge_record.mainCode_value);
		j.put(Protocols.SUBCODE, Protocols.DB_charge_record.subCode_value);
		DBService.getInstance().addMsg(j);
		// TODO 鎵ц涓氬姟閫昏緫鎿嶄綔
		long coin = amount/100*Config.instance().getIValue(ConfigField.CHARGE_RATE);//amount鍗曚綅鏄垎锛岃浆鎹㈡垚鍏冿紝鐒跺悗鍐嶄箻閰嶇疆鐨勬瘮鐜�
		Player player = PlayerManager.getInstance().getOnlinePlayer(uuid);
		if(player!=null){
			GameServer gs = GameServerService.getInstance().getGameServer(player.getServerId());
			ProtocolMsg pMsg = new ProtocolMsg();
			pMsg.put(Protocols.MAINCODE, Protocols.L2g_player_charge.mainCode_value);
			pMsg.put(Protocols.SUBCODE, Protocols.L2g_player_charge.subCode_value);
			pMsg.put(Protocols.L2g_player_charge.UUID, player.getUuid());
			pMsg.put(Protocols.L2g_player_charge.COIN, coin);//amount鍗曚綅鏄垎锛岃浆鎹㈡垚鍏冿紝鐒跺悗鍐嶄箻閰嶇疆鐨勬瘮鐜�
			pMsg.setChannel(gs.getChannel());
			GameServerMsgSender.getInstance().addMsg(pMsg);
		}else{
			JSONObject json =new JSONObject();
			json.put(Protocols.MAINCODE, Protocols.DB_player_charge.mainCode_value);
			json.put(Protocols.SUBCODE, Protocols.DB_player_charge.subCode_value);
			json.put(Protocols.DB_player_charge.UUID, uuid);
			json.put(Protocols.DB_player_charge.COIN, coin);
			json.put(Protocols.DB_player_charge.AMOUNT, amount/100);
			DBService.getInstance().addMsg(json);
		}
	}

	@Override
	public void doGet(ChannelHandlerContext ctx, FullHttpRequest request, Map<String, String> parmMap) {
		HttpDecoderAndEncoder.Response(ctx, request, "error");
	}

}
