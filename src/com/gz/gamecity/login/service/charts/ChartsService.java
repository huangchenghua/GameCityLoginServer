package com.gz.gamecity.login.service.charts;

import java.security.KeyStore.Entry;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.gz.gamecity.bean.Player;
import com.gz.gamecity.delay.DelayMsg;
import com.gz.gamecity.delay.InnerDelayManager;

import com.gz.gamecity.login.LSMsgReceiver;
import com.gz.gamecity.login.logic.LogicHandler;
import com.gz.gamecity.login.service.db.DBService;
import com.gz.gamecity.protocol.Protocols;
import com.gz.util.DateUtil;
import com.gz.websocket.msg.BaseMsg;
import com.gz.websocket.msg.ClientMsg;
import com.gz.websocket.msg.ProtocolMsg;

public class ChartsService implements LogicHandler {

	public static enum ChartsType{
		CHARM_WEEK(0, "charm"),
		COIN_DAY(1, "coin"),
		PAY_TODAY(2, "pay"),
		PAY_YESTODAY(3, "pay"),
		COIN_TOTAL(4, "coin");
		
		private int m_nValue;
		private String m_strColumn;
		
		ChartsType(int nValue, String strColumn) {
			m_nValue = nValue;
			m_strColumn = strColumn;
		}
		
		public int value() {
			return m_nValue;
		}
		
		public String column() {
			return m_strColumn;
		}
		
		ChartsType nameOfValue(int nValue) {
			for(ChartsType e : ChartsType.values() ) {
				if (e.value() == nValue)
					return e;
			}
			return CHARM_WEEK;
		}
	}
	
	
	class PlayerRecord {

		TreeMap<Integer, Long> mapValue = new TreeMap<Integer, Long>();
		
		String strUuid;
		String strName;
		byte bySex;
		int nHead;
		int nLvl;
		int nVip;
		int nFinance;
	}
	
	class PlayerInfo {
		int nRank;
		long nValue;
		
		String strUuid;
		String strName;
		byte bySex;
		int nHead;
		int nLvl;
		int nVip;
		int nFinance;
		
	}
	
	// private 
	private final static Logger log = Logger.getLogger(ChartsService.class);
	
	private final static int N_CHARTS_TOP_LEN = 10;
	private final static long N_UPDATE_DELAY = 1 * 1000l;
	
	private TreeMap<String, PlayerRecord> m_mapRecord = new TreeMap<String, PlayerRecord>(); // map<uuid, ...>
	
	private TreeMap<ChartsType, LinkedList<PlayerInfo>> m_mapChartsTopList = new TreeMap<ChartsType, LinkedList<PlayerInfo>>(); 
	
	private static ChartsService m_instance = null;
	
	private int m_nNowDayOfYear = 0;
	private int m_nNowWeekOfYear = 0;
	private int m_nNowYear = 0;
	
	private void addDelayEvent() {
		DelayMsg delayMsg = new DelayMsg(N_UPDATE_DELAY) {
			@Override
			public void onTimeUp() {
				ProtocolMsg msg = new ProtocolMsg();
				msg.setMainCode(Protocols.Inner_login_charts_update.mainCode_value);
				msg.put(Protocols.MAINCODE, Protocols.Inner_login_charts_update.mainCode_value);
				msg.put(Protocols.SUBCODE, Protocols.Inner_login_charts_update.subCode_value);

				msg.setInner(true);
				LSMsgReceiver.getInstance().addMsg(msg);
//				log.debug("add msg-------------------------");
			}
		};
		InnerDelayManager.getInstance().addDelayItem(delayMsg);
//		log.debug("charts add delay event");
	}
	
	private ChartsService() {
		// update now time
		m_nNowDayOfYear = getNowDayOfYear();
		
		m_nNowWeekOfYear = getNowWeekOfYear();
		
		m_nNowYear = getNowYear();
		
		addDelayEvent();

	}
	
	private int getNowYear() {
		Calendar c = Calendar.getInstance();
		return c.get(Calendar.YEAR);
	}
	
	private int getNowDayOfYear() {
		Calendar c = Calendar.getInstance();

		return c.get(Calendar.DAY_OF_YEAR);
	}
	
	private int getNowWeekOfYear() {
		Calendar c = Calendar.getInstance();
		c.setFirstDayOfWeek(Calendar.MONDAY);
		return c.get(Calendar.WEEK_OF_YEAR);
	}
	
	private int getNowTime() {
		return m_nNowYear * 100000 + m_nNowWeekOfYear * 1000 + m_nNowDayOfYear;
	}
	
	public static synchronized ChartsService getInstance() {
		if (m_instance == null)
			m_instance = new ChartsService();
		
		return m_instance;
	}
	
	@Override
	public void handleMsg(BaseMsg msg) {
		
		ProtocolMsg pMsg = (ProtocolMsg)msg;
		int subCode = pMsg.getJson().getIntValue(Protocols.SUBCODE);
		switch (subCode){
			case Protocols.Inner_login_charts_update.subCode_value:
				handleInnerUpdate(pMsg);
				break;
			
			default :
				log.error("error charts sub_code[sub_code=" + subCode + "]");
		}
	}

	@Override
	public int getMainCode() {
		
		return Protocols.MainCode.CHARTS;
	}
	
	private void handleInnerUpdate(ProtocolMsg msg) {
//		log.debug("inner charts update[time=" + DateUtil.getCurDateTime() + "]");
		
		addDelayEvent();
	}
	
	private void addValue(String strUuid, ChartsType eType, long nValue) {
		/*
		PlayerRecord record = m_mapRecord.get(strUuid);
		if (record == null) {
			record = new PlayerRecord();
			m_mapRecord.put(strUuid, record);
		}
		//record.szValue[nOffset] += nValue;
		Long nowValue = record.mapValue.get(eType.value());
		if (nowValue == null) {
			nowValue = new Long(0);
			record.mapValue.put(eType.value(), nowValue);
		}
		nowValue += nValue;
		*/
		
		JSONObject jo = new JSONObject();
		
		jo.put(Protocols.MAINCODE, Protocols.DB_charts_update_record.mainCode_value);
		jo.put(Protocols.SUBCODE, Protocols.DB_charts_update_record.subCode_value);
		jo.put(Protocols.DB_charts_update_record.TIME, getNowTime());
		jo.put(Protocols.DB_charts_update_record.UUID, strUuid);
		jo.put(Protocols.DB_charts_update_record.COLUMN_NAME, eType.column());
		jo.put(Protocols.DB_charts_update_record.COLUMN_VALUE, nValue);
		
		DBService.getInstance().addMsg(jo);
//		log.debug("charts add value[uuid=" + strUuid + " type=" + eType.value() + " column=" + eType.column() + " value=" + nValue);
	}
	

	
	public void addCharmWeek(String strUuid, long nCharm) {
		addValue(strUuid, ChartsType.CHARM_WEEK, nCharm);
	}
	
	public void addCoinDay(String strUuid, long nCoin) {
		addValue(strUuid, ChartsType.COIN_DAY, nCoin);
	}
	
	public void addPayToday(String strUuid, long nPay) {
		addValue(strUuid, ChartsType.PAY_TODAY, nPay);
	}
	

	
	

	
	
}
