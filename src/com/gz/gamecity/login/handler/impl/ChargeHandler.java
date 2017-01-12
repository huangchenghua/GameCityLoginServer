package com.gz.gamecity.login.handler.impl;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.Charsets;

import com.gz.http.HttpDecoderAndEncoder;
import com.gz.http.HttpServerMsgHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

public class ChargeHandler implements HttpServerMsgHandler {

	@Override
	public String getPath() {
		return "/game/charge";
	}

	@Override
	public void doPost(ChannelHandlerContext ctx, FullHttpRequest request) {
		String jsonStr = request.content().toString(Charsets.toCharset(CharEncoding.UTF_8));
		System.out.println(jsonStr);
		HttpDecoderAndEncoder.Response(ctx, request, "success");
	}

	@Override
	public void doGet(ChannelHandlerContext ctx, FullHttpRequest request) {
		HttpDecoderAndEncoder.Response(ctx, request, "error");
	}

}
