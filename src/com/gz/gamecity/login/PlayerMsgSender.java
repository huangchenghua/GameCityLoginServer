package com.gz.gamecity.login;

import java.util.concurrent.LinkedBlockingQueue;

import com.gz.gamecity.login.msg.ClientMsg;
import com.gz.websocket.msg.BaseMsg;

public class PlayerMsgSender extends Thread{
	private static PlayerMsgSender instance;

	public static synchronized PlayerMsgSender getInstance() {
		if(instance==null) instance=new PlayerMsgSender();
		return instance;
	}
	private PlayerMsgSender(){
		
	}
	
	private static LinkedBlockingQueue<ClientMsg> queue=new LinkedBlockingQueue<ClientMsg>();
	
	public void addMsg(ClientMsg msg){
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
