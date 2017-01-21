package com.gz.gamecity.login;

import java.util.Calendar;
import java.util.Date;

import com.alibaba.fastjson.JSONObject;
import com.gz.gamecity.delay.DelayMsg;
import com.gz.gamecity.delay.InnerDelayManager;
import com.gz.gamecity.login.service.db.DBService;
import com.gz.gamecity.protocol.Protocols;
import com.gz.util.DateUtil;

public class MaintainManager {
	private static MaintainManager instance;

	public static synchronized MaintainManager getInstance() {
		if(instance == null)
			instance=new MaintainManager();
		return instance;
	}
	
	private MaintainManager(){
		
	}
	
	public void startMaintain(){
		startMailMaintain();
	}
	
	private void startMailMaintain(){
		Date date_now = Calendar.getInstance().getTime();
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, 1);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 1);
		c.set(Calendar.SECOND, 0);
		int diff_secondes = DateUtil.dateDiff("S", date_now, c.getTime());
		DelayMsg msg = new DelayMsg(diff_secondes*1000){
			@Override
			public void onTimeUp() {
				reqCleanMail();
				continueMailMaintain();
			}
		};
		InnerDelayManager.getInstance().addDelayItem(msg);
	}
	
	private void continueMailMaintain(){
		DelayMsg msg = new DelayMsg(24*60*60*1000){
			@Override
			public void onTimeUp() {
				reqCleanMail();
				continueMailMaintain();
			}
		};
		InnerDelayManager.getInstance().addDelayItem(msg);
	}
	
	private void reqCleanMail(){
		JSONObject j = new JSONObject();
		j.put(Protocols.MAINCODE, Protocols.DB_clean_mail.mainCode_value);
		j.put(Protocols.SUBCODE, Protocols.DB_clean_mail.subCode_value);
		DBService.getInstance().addMsg(j);
	}
	
	public static void main(String[] args) {
//		Date date_now = Calendar.getInstance().getTime();
//		Calendar c = Calendar.getInstance();
//		c.add(Calendar.DATE, -1);
//		c.set(Calendar.HOUR_OF_DAY, 0);
//		c.set(Calendar.MINUTE, 1);
//		c.set(Calendar.SECOND, 0);
//		System.out.println(date_now);
//		System.out.println(c.getTime());
//		int diff = DateUtil.dateDiff("S", date_now, c.getTime());
//		System.out.println("dateDiff=" + diff);
		
		System.out.println(DateUtil.getBeforeToday(1));
		Calendar c = DateUtil.parse(DateUtil.getBeforeToday(1),"yyyy-MM-dd");
		c.set(Calendar.HOUR_OF_DAY, 23);
		c.set(Calendar.MINUTE, 59);
		c.set(Calendar.SECOND, 59);
		Date d = c.getTime();
		
		System.out.println(DateUtil.getDateTime(DateUtil.DEFAULT_PATTERN, c));
	}
	
	public static String getMailCleanTime(){
		Calendar c = DateUtil.parse(DateUtil.getBeforeToday(31),"yyyy-MM-dd");
		c.set(Calendar.HOUR_OF_DAY, 23);
		c.set(Calendar.MINUTE, 59);
		c.set(Calendar.SECOND, 59);
		return DateUtil.getDateTime(DateUtil.DEFAULT_PATTERN, c);
	}
	
	
}
