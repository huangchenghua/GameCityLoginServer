package com.gz.gamecity.login;


import com.gz.gamecity.bean.Player;
import com.gz.gamecity.login.config.ConfigField;
import com.gz.gamecity.login.db.JedisConnectionPool;
import com.gz.gamecity.login.handler.impl.PlayerMsgHandler;
import com.gz.gamecity.login.handler.impl.GameServerMsgHandler;
import com.gz.gamecity.login.handler.impl.PlayerLoginHandler;
import com.gz.gamecity.login.sdkverify.SdkVerify;
import com.gz.gamecity.login.service.GameServerService;
import com.gz.gamecity.login.service.PlayerLoginCache;
import com.gz.gamecity.login.service.PlayerLoginService;
import com.gz.gamecity.protocol.Protocols;
import com.gz.http.HttpServer;
import com.gz.util.Config;
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
		Config.instance().init();
	}
	
	public void startLogic()
	{
		PlayerLoginCache.getInstance();
		
		LSMsgReceiver.getInstance().registHandler(Protocols.C2l_login.mainCode_value, PlayerLoginService.getInstance());
		LSMsgReceiver.getInstance().registHandler(Protocols.G2l_login.mainCode_value, GameServerService.getInstance());
		LSMsgReceiver.getInstance().start();
	}
	public void initDB(){
		JedisConnectionPool.init(Config.instance().getSValue(ConfigField.DB_HOST),
				Config.instance().getIValue(ConfigField.DB_PORT),
				Config.instance().getSValue(""));
	}
	
	public void initWebsocket(){
		
		GameServerMsgSender.getInstance().start();
		
		PlayerMsgSender.getInstance().start();
		
		SdkVerify.getInstance().start();
		
		final HttpServer httpServer = new HttpServer();
		httpServer.addHandler(new PlayerLoginHandler());
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
