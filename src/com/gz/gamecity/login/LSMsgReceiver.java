package com.gz.gamecity.login;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import com.gz.gamecity.login.logic.LogicHandler;
import com.gz.websocket.msg.BaseMsg;

public class LSMsgReceiver extends Thread {
	private static final Logger log=Logger.getLogger(LSMsgReceiver.class);
	private static LSMsgReceiver instance;
	
	private LinkedBlockingQueue<BaseMsg> queue = new LinkedBlockingQueue<BaseMsg>();
	
	private Map<Integer, LogicHandler> handlers=new HashMap<Integer, LogicHandler>();
	
	public static synchronized LSMsgReceiver getInstance(){
		if(instance ==null){
			instance = new LSMsgReceiver();
		}
		return instance;
	}
	
	private LSMsgReceiver(){
		
	}
	
	public void addMsg(BaseMsg msg){
		try {
			queue.put(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		while(true){
			try {
				BaseMsg msg = queue.take();
				int mainCode=msg.getMainCode();
				LogicHandler handler =handlers.get(mainCode);
				if(handler!=null)
					handler.handleMsg(msg);
				else
				{
					log.warn("无法识别的协议:"+mainCode);
				}
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}
		}
	}
	
	public void registHandler(int mainCode,LogicHandler handler){
		handlers.put(mainCode, handler);
	}
}
