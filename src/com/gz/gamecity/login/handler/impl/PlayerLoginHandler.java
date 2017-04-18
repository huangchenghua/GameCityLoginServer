package com.gz.gamecity.login.handler.impl;


import java.util.List;
import java.util.Map;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.Charsets;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.gz.gamecity.login.LSMsgReceiver;
import com.gz.gamecity.protocol.Protocols;
import com.gz.http.HttpDecoderAndEncoder;
import com.gz.http.HttpServerMsgHandler;
import com.gz.websocket.msg.HttpMsg;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

public class PlayerLoginHandler implements HttpServerMsgHandler{

	private static final Logger log=Logger.getLogger(PlayerLoginHandler.class);

	@Override
	public String getPath() {
		
		return "/game/login";
	}

	@Override
	public void doPost(ChannelHandlerContext ctx, FullHttpRequest request, Map<String, String> parmMap) {
		String jsonStr = request.content().toString(Charsets.toCharset(CharEncoding.UTF_8));
		QueryStringDecoder queryDecoder = new QueryStringDecoder(jsonStr, false);
		JSONObject json=new JSONObject();
		Map<String, List<String>> uriAttributes = queryDecoder.parameters();
		for (Map.Entry<String, List<String>> attr : uriAttributes.entrySet()) {
			for (String attrVal : attr.getValue()) {
				json.put(attr.getKey(),attrVal);
			}
		}
//		ClientMsg msg = new ClientMsg();
//		msg.setChannel(ctx.channel());
//		msg.setJson(json);
//		msg.setMainCode(json.getIntValue(Protocols.MAINCODE));
//		LSMsgReceiver.getInstance().addMsg(msg);
		
		HttpMsg hMsg=new HttpMsg();
		hMsg.setCtx(ctx);
		hMsg.setRequest(request);
		hMsg.setChannel(ctx.channel());
		hMsg.setContent(jsonStr);
		hMsg.parse();
		hMsg.setMainCode(json.getIntValue(Protocols.MAINCODE));
		LSMsgReceiver.getInstance().addMsg(hMsg);
	}

	@Override
	public void doGet(ChannelHandlerContext ctx, FullHttpRequest request, Map<String, String> parmMap) {
		HttpDecoderAndEncoder.Response(ctx, request, "error");
		
	}

}
