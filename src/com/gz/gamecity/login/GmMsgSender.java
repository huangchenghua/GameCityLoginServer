package com.gz.gamecity.login;

import java.util.concurrent.LinkedBlockingQueue;

import com.gz.websocket.msg.BaseMsg;
import com.gz.websocket.msg.ClientMsg;

public class GmMsgSender extends Thread{
	private static GmMsgSender instance;

	public static synchronized GmMsgSender getInstance() {
		if(instance==null) instance=new GmMsgSender();
		return instance;
	}
	private GmMsgSender(){
		
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
			}
		}
	}
	
	
}
