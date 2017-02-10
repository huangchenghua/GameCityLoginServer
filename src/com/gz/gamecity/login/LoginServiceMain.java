package com.gz.gamecity.login;


import javax.naming.NameNotFoundException;

import com.gz.dbpools.ConnectionFactory;
import com.gz.gamecity.bean.Player;
import com.gz.gamecity.delay.InnerDelayManager;
import com.gz.gamecity.login.config.ConfigField;
import com.gz.gamecity.login.db.JedisConnectionPool;
import com.gz.gamecity.login.handler.impl.PlayerMsgHandler;
import com.gz.gamecity.login.handler.impl.ChargeHandler;
import com.gz.gamecity.login.handler.impl.GameServerMsgHandler;
import com.gz.gamecity.login.handler.impl.PlayerLoginHandler;
import com.gz.gamecity.login.sdkverify.SdkVerify;
import com.gz.gamecity.login.service.charts.ChartsService;
import com.gz.gamecity.login.service.db.DBService;
import com.gz.gamecity.login.service.gameserver.GameServerService;
import com.gz.gamecity.login.service.gm.GameNoticeManager;
import com.gz.gamecity.login.service.gm.GmService;
import com.gz.gamecity.login.service.player.PlayerDataService;
import com.gz.gamecity.login.service.player.PlayerLoginService;
import com.gz.gamecity.login.service.player.PlayerVerifyService;
import com.gz.gamecity.login.service.charts.ChartsService;
import com.gz.http.HttpServer;
import com.gz.util.Config;
import com.gz.util.SensitivewordFilter;
import com.gz.websocket.protocol.server.ProtocolServer;
import com.gz.websocket.server.WebSocketServer;

public class LoginServiceMain {
	private static LoginServiceMain instance;
	
	public static synchronized LoginServiceMain getInstance() {
		if(instance ==null)
			instance = new LoginServiceMain();
		return instance;
	}
	private LoginServiceMain(){
		
	}
	
	public void loadConfig(){
		SensitivewordFilter.getInstance();
		Config.instance().init();
	}
	
	public void startLogic()
	{
		PlayerManager.getInstance();
		InnerDelayManager.getInstance();
		MaintainManager.getInstance().startMaintain();
		GameNoticeManager.getInstance();
		LSMsgReceiver.getInstance().registHandler(PlayerLoginService.getInstance());
		LSMsgReceiver.getInstance().registHandler(GameServerService.getInstance());
		LSMsgReceiver.getInstance().registHandler(new PlayerVerifyService());
		LSMsgReceiver.getInstance().registHandler(new PlayerDataService());
		LSMsgReceiver.getInstance().registHandler(new GmService());
		
		LSMsgReceiver.getInstance().registHandler(ChartsService.getInstance());
		
		LSMsgReceiver.getInstance().start();
	}
	
	public void initDB(){
		JedisConnectionPool.init(Config.instance().getSValue(ConfigField.DB_HOST),
				Config.instance().getIValue(ConfigField.DB_PORT),
				Config.instance().getSValue(""));
		try {
			ConnectionFactory.lookup("login");
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		DBService.getInstance().start();
	}
	
	public void initWebsocket(){
		
		GameServerMsgSender.getInstance().start();
		
		PlayerMsgSender.getInstance().start();
		
		GmMsgSender.getInstance().start();
		
		SdkVerify.getInstance().start();
		
		final HttpServer httpServer = new HttpServer();
		httpServer.addHandler(new PlayerLoginHandler());
		httpServer.addHandler(new ChargeHandler());
		Thread t = new Thread(){
			@Override
			public void run() {
				try {
					httpServer.run(Config.instance().getIValue(ConfigField.HTTP_PORT));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		t.start();
		
		
		final WebSocketServer webSocketServer=new WebSocketServer(new PlayerMsgHandler());
		Thread t1 = new Thread(){
			@Override
			public void run() {
				try {
					webSocketServer.run(Config.instance().getIValue(ConfigField.WEBSOCKET_PORT));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		t1.start();
		
		
		final ProtocolServer protocolServer = new ProtocolServer(new GameServerMsgHandler());
		
		Thread t2 = new Thread(){
			@Override
			public void run() {
				try {
					protocolServer.run(Config.instance().getIValue(ConfigField.PROTOCOL_PORT));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		t2.start();
		
	}
	
	public void startServer(){
		loadConfig();
		initDB();
		startLogic();
		initWebsocket();
	}
	
	public void stopServer(){
		// TODO 
	}
	
}
