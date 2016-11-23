package com.gz.gamecity.login;

import java.util.concurrent.LinkedBlockingQueue;

import com.gz.websocket.msg.BaseMsg;
import com.gz.websocket.msg.ProtocolMsg;

public class GameServerMsgSender extends Thread{
	private static GameServerMsgSender instance;

	public static synchronized GameServerMsgSender getInstance() {
		if(instance==null) instance=new GameServerMsgSender();
		return instance;
	}
	private GameServerMsgSender(){
		
	}
	
	private static LinkedBlockingQueue<ProtocolMsg> queue=new LinkedBlockingQueue<ProtocolMsg>();
	
	public void addMsg(ProtocolMsg msg){
		try {
			queue.put(msg);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		while(true){
			try {
				BaseMsg msg = queue.take();
				msg.sendSelf();
			} catch (InterruptedException e) {
				e.printStackTrace();
				// TODO 异常处理,应该需要断开连接
			}
		}
	}
	
	
}
