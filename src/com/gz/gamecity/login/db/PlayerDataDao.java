package com.gz.gamecity.login.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gz.gamecity.bean.Mail;
import com.gz.gamecity.bean.Player;
import com.gz.gamecity.login.service.charts.ChartsService;
import com.gz.gamecity.protocol.Protocols;
import com.gz.util.DateUtil;

public class PlayerDataDao extends BaseDao{

	private final static Logger log = Logger.getLogger(PlayerDataDao.class);
	
	public Player getPlayer(String uuid) {
		Connection conn = null;
		PreparedStatement pstmt=null;
		ResultSet rs = null;
		try {
			conn=getConn();
			String sql = "select * from player where uuid=?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, uuid);
			rs = pstmt.executeQuery();
			if(rs.next()){
				Player player = new  Player();
				player.setUuid(uuid);
				player.setName(rs.getString("name"));
				player.setSex(rs.getByte("sex"));
				player.setCoin(rs.getLong("coin"));
				player.setHead(rs.getInt("head"));
				player.setLvl(rs.getInt("lvl"));
				player.setFinance(rs.getInt("finance"));
				player.setVip(rs.getInt("vip"));
				player.setCharm(rs.getInt("charm"));
				player.setSign(rs.getString("sign"));
				player.setCharge_total(rs.getLong("charge_total"));
				player.setFrozen(rs.getInt("frozen")==1);
				player.setSilent(rs.getInt("silent")==1);
				player.setLastSignDate(rs.getString("last_sign_date"));
				player.setSignDays(rs.getInt("sign_days"));
				player.setExp(rs.getInt("exp"));
				player.setAlmsCnt(rs.getByte("alms_cnt"));
				player.setAlmsTime(rs.getString("alms_time"));
				if(player.getLastSignDate()!=null){
					if(player.getLastSignDate().equals(DateUtil.getCurDateTime("yyyy-MM-dd"))){
						player.setSigned(true);
					}
				}
				
				String lastday = player.getLastSignDate();
				String curDate=DateUtil.getCurDateTime("yyyy-MM-dd");
				if(lastday!=null){
					long date_diff = 0;
					try {
						date_diff = DateUtil.dateDays(lastday,curDate);
					} catch (Exception e) {
					}
					if(date_diff != 1 && !lastday.equals(curDate)){
						player.setSignDays(0);
					}
				}
				
				String[] heads_str = rs.getString("heads").split("~");
				int[] heads = new int[heads_str.length];
				for (int i = 0; i < heads.length; i++) {
					heads[i] = Integer.parseInt(heads_str[i]);
				}
				player.setHeads(heads);
				
				return player;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(rs, pstmt, conn);
		}
		return null;
	}
	
	
	public void insertPlayer(Player player) {
		Connection conn = null;
		PreparedStatement pstmt=null;
		try {
			conn=getConn();
			String sql = "insert into player (uuid,name,sex,coin,head,lvl,finance,vip,charm,sign,charge_total,create_time,last_time,heads) values(?,?,?,?,?,?,?,?,?,?,?,now(),now(),?)";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, player.getUuid());
			pstmt.setString(2, player.getName());
			pstmt.setInt(3, player.getSex());
			pstmt.setLong(4, player.getCoin());
			pstmt.setInt(5, player.getHead());
			pstmt.setInt(6, player.getLvl());
			pstmt.setInt(7, player.getFinance());
			pstmt.setInt(8, player.getVip());
			pstmt.setInt(9, player.getCharm());
			pstmt.setString(10, player.getSign());
			pstmt.setLong(11, player.getCharge_total());
			StringBuffer sb=new StringBuffer("");
			for(int i=0;i<player.getHeads().length;i++){
				sb.append(player.getHeads()[i]).append("~");
			}
			sb.deleteCharAt(sb.length()-1);
			pstmt.setString(12, sb.toString());
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(null, pstmt, conn);
		}
	}
	
	public void updatePlayerCoin(String uuid,long coin){
		Connection conn = null;
		PreparedStatement pstmt=null;
		try {
			conn=getConn();
			String sql = "update player set coin=? where uuid=?";
			pstmt=conn.prepareStatement(sql);
			pstmt.setLong(1, coin);
			pstmt.setString(2, uuid);
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(null, pstmt, conn);
		}
	}
	
	public void recordCoinChange(String player_uuid,long coin,long change,int type,String uuid_log){
		Connection conn=null;
		PreparedStatement pstmt = null;
		try {
			String sql="insert into player_coin_log values(?,?,?,?,?,now())";
			conn=getConn();
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, player_uuid);
			pstmt.setString(2, uuid_log);
			pstmt.setLong(3, coin);
			pstmt.setLong(4, change);
			pstmt.setInt(5, type);
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			close(null,pstmt,conn);
		}
	}
	
	public void updatePlayerData(JSONObject j){
		Connection conn=null;
		PreparedStatement pstmt = null;
		String uuid = j.getString(Protocols.G2l_coinChange.UUID);
		StringBuffer sb=new StringBuffer("update player set ");
		
		String name = j.getString(Protocols.G2l_data_change.NAME);
		if(name!=null){
			sb.append("name='").append(name).append("',");
		}
		
		String head_str = j.getString(Protocols.G2l_data_change.HEAD);
		if(head_str!=null){
			int head = j.getIntValue(Protocols.G2l_data_change.HEAD);
			sb.append("head='").append(head).append("',");
		}
		
		String vip_str = j.getString(Protocols.G2l_data_change.VIP);
		if(vip_str!=null){
			int vip = j.getIntValue(Protocols.G2l_data_change.VIP);
			sb.append("vip=").append(vip).append(",");
		}
		
		String charge_total_str = j.getString(Protocols.G2l_data_change.CHARGE_TOTAL);
		if(charge_total_str!=null){
			long charge_total = j.getLongValue(Protocols.G2l_data_change.CHARGE_TOTAL);
			sb.append("charge_total=").append(charge_total).append(",");
		}
		
		String sex_str = j.getString(Protocols.G2l_data_change.SEX);
		if(sex_str!=null){
			byte sex = j.getByteValue(Protocols.G2l_data_change.SEX);
			sb.append("sex=").append(sex).append(",");
		}
		
		String lvl_str = j.getString(Protocols.G2l_data_change.LVL);
		if(lvl_str!=null){
			int lvl = j.getIntValue(Protocols.G2l_data_change.LVL);
			sb.append("lvl=").append(lvl).append(",");
		}
		
		String exp_str = j.getString(Protocols.G2l_data_change.EXP);
		if(exp_str!=null){
			int exp = j.getIntValue(Protocols.G2l_data_change.EXP);
			sb.append("exp=").append(exp).append(",");
		}
		
		String finance_str = j.getString(Protocols.G2l_data_change.FINANCE);
		if(finance_str!=null){
			int finance = j.getIntValue(Protocols.G2l_data_change.FINANCE);
			sb.append("finance=").append(finance).append(",");
		}
		
		String sign = j.getString(Protocols.G2l_data_change.SIGN);
		if(sign!=null){
			sb.append("sign='").append(sign).append("',");
		}
		
		String charm_str = j.getString(Protocols.G2l_data_change.CHARM);
		if(charm_str!=null){
			int charm = j.getIntValue(Protocols.G2l_data_change.CHARM);
			sb.append("charm=").append(charm).append(",");
		}
		

		if (j.containsKey(Protocols.G2l_data_change.ALMS_CNT)) {
			byte nAlmsCnt = j.getByteValue(Protocols.G2l_data_change.ALMS_CNT);
			sb.append("alms_cnt=").append(nAlmsCnt).append(",");
		}
		
		if (j.containsKey(Protocols.G2l_data_change.ALMS_TIME)) {
			String strAlmsTime = j.getString(Protocols.G2l_data_change.ALMS_TIME);
			sb.append("alms_time='").append(strAlmsTime).append("',");
		}
		
		String heads_str = j.getString(Protocols.G2l_data_change.HEADS);
		if(heads_str!=null){
			JSONArray arr = j.getJSONArray(Protocols.G2l_data_change.HEADS);
			sb.append("heads='");
			for(int i=0;i<arr.size();i++){
				sb.append(arr.getIntValue(i)).append("~");
			}
			sb.deleteCharAt(sb.length()-1);
			sb.append("',");
		}
		

		sb.deleteCharAt(sb.length()-1);
		sb.append(" where uuid=?");
		
		try {
			conn = getConn();
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setString(1, uuid);
			pstmt.executeUpdate();
		} catch (Exception e) {
			System.out.println(sb.toString());
			e.printStackTrace();
		} finally {
			close(null,pstmt,conn);
		}
	}
	
	public JSONObject[] getPlayerMailList(String uuid){
		Connection conn=null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ArrayList<JSONObject> list=new ArrayList<>();
		JSONObject[] arr=null;
		try {
			conn = getConn();
			pstmt = conn.prepareStatement("SELECT * FROM mail WHERE player_uuid=? ORDER BY `read`  ,send_time DESC");
			pstmt.setString(1, uuid);
			rs = pstmt.executeQuery();
			while(rs.next()){
				JSONObject j = new JSONObject();
				j.put(Protocols.L2g_mail_list.Mail_list.MAILID, rs.getString("mail_uuid"));
				j.put(Protocols.L2g_mail_list.Mail_list.TITLE, rs.getString("title"));
				j.put(Protocols.L2g_mail_list.Mail_list.CONTENT, rs.getString("content"));
				j.put(Protocols.L2g_mail_list.Mail_list.SEND_TIME, rs.getString("send_time"));
				j.put(Protocols.L2g_mail_list.Mail_list.READ, rs.getByte("read")==1);
				j.put(Protocols.L2g_mail_list.Mail_list.TAKEN, rs.getByte("taken")==1);
				j.put(Protocols.L2g_mail_list.Mail_list.ATTACHMENTS, rs.getString("attachments"));
				j.put(Protocols.L2g_mail_list.Mail_list.MAIL_TYPE,rs.getInt("mail_type"));
				j.put(Protocols.L2g_mail_list.Mail_list.SENDER,rs.getString("sender"));
				Calendar c1 = DateUtil.parse(rs.getString("send_time"));
				c1.add(Calendar.DATE, 31);
				c1.set(Calendar.HOUR_OF_DAY, 0);
				c1.set(Calendar.MINUTE, 1);
				c1.set(Calendar.SECOND, 0);
				Calendar c2 = Calendar.getInstance();
				long remain = DateUtil.dateDiff("S", c2.getTime(), c1.getTime()); 
				j.put(Protocols.L2g_mail_list.Mail_list.REMAIN, remain);
				list.add(j);
			}
			
			arr = new JSONObject[list.size()];
			for(int i=0;i<arr.length;i++){
				arr[i] = list.get(i);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(rs, pstmt, conn);
		}
		return arr;
	}
	
	public void addNewMail(JSONObject j){
		Connection conn=null;
		PreparedStatement pstmt = null;
		try {
			conn = getConn();
			pstmt = conn.prepareStatement("insert into mail(mail_uuid,player_uuid,title,content,send_time,attachments,mail_type,sender) values(?,?,?,?,?,?,?,?)");
			pstmt.setString(1, j.getString(Protocols.DB_login_new_mail.MAILID));
			pstmt.setString(2, j.getString(Protocols.DB_login_new_mail.UUID));
			pstmt.setString(3, j.getString(Protocols.DB_login_new_mail.TITLE));
			pstmt.setString(4, j.getString(Protocols.DB_login_new_mail.CONTENT));
			pstmt.setString(5, j.getString(Protocols.DB_login_new_mail.SEND_TIME));
			pstmt.setString(6, j.getString(Protocols.DB_login_new_mail.ATTACHMENTS));
			pstmt.setInt(7, j.getIntValue(Protocols.DB_login_new_mail.MAIL_TYPE));
			pstmt.setString(8, j.getString(Protocols.DB_login_new_mail.SENDER));
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(null, pstmt, conn);
		}
	}
	
	public void openMail(String mailId){
		Connection conn=null;
		PreparedStatement pstmt = null;
		try {
			conn = getConn();
			pstmt = conn.prepareStatement("update mail set `read` = 1 where mail_uuid = ?");
			pstmt.setString(1, mailId);
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(null, pstmt, conn);
		}
	}
	
	public Mail getMail(String mailId){
		Connection conn=null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Mail mail=null;
		try {
			conn = getConn();
			pstmt = conn.prepareStatement("select * from mail where mail_uuid=?");
			pstmt.setString(1, mailId);
			rs= pstmt.executeQuery();
			if(rs.next()){
				mail =new Mail();
				mail.setAttachments(rs.getString("attachments"));
				mail.setContent(rs.getString("content"));
				mail.setMailId(rs.getString("mail_uuid"));;
				mail.setPlayer_uuid(rs.getString("player_uuid"));
				mail.setRead(rs.getInt("read")==1);
				mail.setTaken(rs.getInt("taken")==1);
				mail.setTitle(rs.getString("title"));
				mail.setMail_type(rs.getInt("mail_type"));
				mail.setSender(rs.getString("sender"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(rs, pstmt, conn);
		}
		return mail;
	}
	
	public void takeMail(String mailId){
		Connection conn=null;
		PreparedStatement pstmt = null;
		try {
			conn = getConn();
			pstmt = conn.prepareStatement("update mail set `taken` = 1 where mail_uuid = ?");
			pstmt.setString(1, mailId);
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(null, pstmt, conn);
		}
	}
	
	public void delMail(String ids){
		Connection conn=null;
		PreparedStatement pstmt = null;
		try {
			conn = getConn();
			StringBuffer sb =new StringBuffer("delete from mail where mail_uuid in (");
			String[] par=ids.split(",");
			for(int i=0;i<par.length;i++){
				sb.append("'").append(par[i]).append("',");
			}
			sb.deleteCharAt(sb.length()-1);
			sb.append(")");
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(null, pstmt, conn);
		}
	}
	
	public int[] getPlayerGiftList(String uuid){
		int[] info=null;
		Connection conn=null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = getConn();
			pstmt=conn.prepareStatement("select * from player_gift where player_uuid=?");
			pstmt.setString(1, uuid);
			rs = pstmt.executeQuery();
			if(rs.next()){
				info=new int[8];
				for(int i=0;i<8;i++){
					info[i] = rs.getInt("count_"+(i+1));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(rs, pstmt, conn);
		}
		return info;
	}
	
	public void insertPlayerGiftList(String uuid){
		Connection conn=null;
		PreparedStatement pstmt = null;
		try {
			conn = getConn();
			pstmt=conn.prepareStatement("insert into player_gift(player_uuid) values(?)");
			pstmt.setString(1, uuid);
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(null, pstmt, conn);
		}
	}

	
	// friend
	public int insertFriend(JSONObject j ) {
		Connection conn=null;
		PreparedStatement pstmt = null;
		int nCnt = 0;
		try {
			conn = getConn();
			pstmt = conn.prepareStatement("insert into friend(uuid, uuid_friend, create_time) values(?,?,?)");
			pstmt.setString(1, j.getString(Protocols.G2l_friend_add.UUID_MY));
			pstmt.setString(2, j.getString(Protocols.G2l_friend_add.UUID_OTHER));
			pstmt.setString(3, j.getString(Protocols.G2l_friend_add.DATE_TIME));

			nCnt = pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(null, pstmt, conn);
		}
		return nCnt;
	}
	
	public int deleteFriend(String strUuid, String strUuidOther) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		int nCnt = 0;
		try {
			conn = getConn();
			pstmt = conn.prepareStatement("delete from friend where uuid=? and uuid_friend=? limit 1");
			pstmt.setString(1, strUuid);
			pstmt.setString(2, strUuidOther);
			
			nCnt = pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(null, pstmt, conn);
		}
		return nCnt;
	}

	public JSONObject[] getFriendList(String strUuid) {
		/*select uuid,name,coin,head from player where uuid in (select uuid_friend from friend where uuid=1);
		<field name="uuid" type="string"/>
		<field name="lv" type="int"/>
		<field name="name" type="string"/>
		<field name="head" type="int"/>
		<field name="vip" type="int"/>
		<field name="finance" type="int"/>
		<field name="coin" type="long"/>
		*/
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ArrayList<JSONObject> list = new ArrayList<>();
		JSONObject[] szObj = null;
		try {
			conn = getConn();
			pstmt = conn.prepareStatement("select uuid, lvl, name, head, vip, finance, coin from player where uuid in (select uuid_friend from friend where uuid=?)");
			pstmt.setString(1, strUuid);
			rs = pstmt.executeQuery();
			while(rs.next()){
				JSONObject j = new JSONObject();
				j.put(Protocols.L2g_friend_list.Player_list.UUID, rs.getString("uuid"));
				j.put(Protocols.L2g_friend_list.Player_list.LV, rs.getInt("lvl"));
				j.put(Protocols.L2g_friend_list.Player_list.NAME, rs.getString("name"));
				j.put(Protocols.L2g_friend_list.Player_list.HEAD, rs.getInt("head"));
				j.put(Protocols.L2g_friend_list.Player_list.VIP, rs.getInt("vip"));
				j.put(Protocols.L2g_friend_list.Player_list.FINANCE, rs.getInt("finance"));
				j.put(Protocols.L2g_friend_list.Player_list.COIN, rs.getLong("coin"));
				
				list.add(j);
			}
			
			szObj = new JSONObject[list.size()];
			for(int i = 0; i < szObj.length; ++i){
				szObj[i] = list.get(i);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(rs, pstmt, conn);
		}
		return szObj;
	}

	public List<Player> searchPlayer(String name){
		List<Player> list=new ArrayList<>();
		Connection conn=null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn=getConn();
			pstmt=conn.prepareStatement("select * from player where name like '%"+name+"%'");
			rs=pstmt.executeQuery();
			while(rs.next()){
				Player player = new  Player();
				player.setUuid(rs.getString("uuid"));
				player.setName(rs.getString("name"));
				player.setSex(rs.getByte("sex"));
				player.setCoin(rs.getLong("coin"));
				player.setHead(rs.getInt("head"));
				player.setLvl(rs.getInt("lvl"));
				player.setFinance(rs.getInt("finance"));
				player.setVip(rs.getInt("vip"));
				player.setCharm(rs.getInt("charm"));
				player.setSign(rs.getString("sign"));
				player.setCharge_total(rs.getLong("charge_total"));
				player.setFrozen(rs.getInt("frozen")==1);
				player.setSilent(rs.getInt("silent")==1);
				list.add(player);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(rs, pstmt, conn);
		}
		return list;
	}
	
	public void freezePlayer(String uuid){
		Connection conn=null;
		PreparedStatement pstmt = null;
		try {
			conn=getConn();
			pstmt=conn.prepareStatement("update player set frozen = 1 where uuid=?");
			pstmt.setString(1, uuid);
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(null, pstmt, conn);
		}
	}
	
	public void unfreezePlayer(String uuid){
		Connection conn=null;
		PreparedStatement pstmt = null;
		try {
			conn=getConn();
			pstmt=conn.prepareStatement("update player set frozen = 0 where uuid=?");
			pstmt.setString(1, uuid);
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(null, pstmt, conn);
		}
	}
	
	public void silentPlayer(String uuid){
		Connection conn=null;
		PreparedStatement pstmt = null;
		try {
			conn=getConn();
			pstmt=conn.prepareStatement("update player set silent = 1 where uuid=?");
			pstmt.setString(1, uuid);
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(null, pstmt, conn);
		}
	}
	
	public void unsilentPlayer(String uuid){
		Connection conn=null;
		PreparedStatement pstmt = null;
		try {
			conn=getConn();
			pstmt=conn.prepareStatement("update player set silent = 0 where uuid=?");
			pstmt.setString(1, uuid);
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(null, pstmt, conn);
		}
	}
	
	public void cleanMail(String datetime){
		Connection conn=null;
		PreparedStatement pstmt = null;
		try {
			conn=getConn();
			pstmt=conn.prepareStatement("delete from mail where send_time between '2010-01-01' and '"+datetime+"'");
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(null, pstmt, conn);
		}
	}
	
	public void playerSignin(String uuid,String date,int days){
		Connection conn=null;
		PreparedStatement pstmt = null;
		try {
			conn=getConn();
			pstmt=conn.prepareStatement("update player set last_sign_date = ?,sign_days=? where uuid=?");
			pstmt.setString(1, date);
			pstmt.setInt(2, days);
			pstmt.setString(3, uuid);
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(null, pstmt, conn);
		}
	}

	public void recordCharge(JSONObject j){
		Connection conn=null;
		PreparedStatement pstmt = null;
		try {
			conn=getConn();
			pstmt=conn.prepareStatement("insert into charge values(?,?,?,?,?,?,?,?,now())");
			pstmt.setString(1, j.getString(Protocols.DB_charge_record.UUID));
			pstmt.setInt(2, j.getIntValue(Protocols.DB_charge_record.AMOUNT));
			pstmt.setString(3, j.getString(Protocols.DB_charge_record.APPID));
			pstmt.setString(4, j.getString(Protocols.DB_charge_record.NOTIFYPARAMETERS));
			pstmt.setString(5, j.getString(Protocols.DB_charge_record.ORDERID));
			pstmt.setString(6, j.getString(Protocols.DB_charge_record.PAYWAY));
			pstmt.setString(7, j.getString(Protocols.DB_charge_record.STATUS));
			pstmt.setString(8, j.getString(Protocols.DB_charge_record.SIGN));
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(null, pstmt, conn);
		}
	}
	
	public JSONArray getChargeRecordList(String uuid){
		JSONArray arr = new JSONArray();
		Connection conn=null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = getConn();
			pstmt=conn.prepareStatement("select * from charge where uuid=?");
			pstmt.setString(1, uuid);
			rs = pstmt.executeQuery();
			while(rs.next()){
				JSONObject j = new JSONObject();
				j.put(Protocols.L2gm_req_charge.List.AMOUNT, rs.getInt("amount"));
				j.put(Protocols.L2gm_req_charge.List.CHARGE_TIME, rs.getString("charge_time"));
				j.put(Protocols.L2gm_req_charge.List.ORDERID, rs.getString("order_id"));
				j.put(Protocols.L2gm_req_charge.List.PAYWAY, rs.getString("pay_way"));
				j.put(Protocols.L2gm_req_charge.List.STATUS, rs.getString("status"));
				arr.add(j);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(rs, pstmt, conn);
		}
		
		return arr;
	}
	
	public void updatePlayerGift(JSONObject j){
		Connection conn=null;
		PreparedStatement pstmt = null;
		try {
			conn = getConn();
			String uuid = j.getString(Protocols.DB_player_gift_change.UUID);
			int id = j.getIntValue(Protocols.DB_player_gift_change.ID);
			int count = j.getIntValue(Protocols.DB_player_gift_change.COUNT);
			String col_name = "count_"+id;
			pstmt = conn.prepareStatement("update player_gift set "+col_name+" = "+col_name + "+"+count+" where player_uuid=?");
			pstmt.setString(1, uuid);
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(null, pstmt, conn);
		}
		
	}
	
	public List<Player> searchFrozenPlayer(){
		List<Player> list=new ArrayList<>();
		Connection conn=null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn=getConn();
			pstmt=conn.prepareStatement("select * from player where frozen = 1");
			rs=pstmt.executeQuery();
			while(rs.next()){
				Player player = new  Player();
				player.setUuid(rs.getString("uuid"));
				player.setName(rs.getString("name"));
				player.setSex(rs.getByte("sex"));
				player.setCoin(rs.getLong("coin"));
				player.setHead(rs.getInt("head"));
				player.setLvl(rs.getInt("lvl"));
				player.setFinance(rs.getInt("finance"));
				player.setVip(rs.getInt("vip"));
				player.setCharm(rs.getInt("charm"));
				player.setSign(rs.getString("sign"));
				player.setCharge_total(rs.getLong("charge_total"));
				player.setFrozen(rs.getInt("frozen")==1);
				player.setSilent(rs.getInt("silent")==1);
				list.add(player);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(rs, pstmt, conn);
		}
		return list;
	}
	
	public List<Player> searchSilentPlayer(){
		List<Player> list=new ArrayList<>();
		Connection conn=null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn=getConn();
			pstmt=conn.prepareStatement("select * from player where silent = 1");
			rs=pstmt.executeQuery();
			while(rs.next()){
				Player player = new  Player();
				player.setUuid(rs.getString("uuid"));
				player.setName(rs.getString("name"));
				player.setSex(rs.getByte("sex"));
				player.setCoin(rs.getLong("coin"));
				player.setHead(rs.getInt("head"));
				player.setLvl(rs.getInt("lvl"));
				player.setFinance(rs.getInt("finance"));
				player.setVip(rs.getInt("vip"));
				player.setCharm(rs.getInt("charm"));
				player.setSign(rs.getString("sign"));
				player.setCharge_total(rs.getLong("charge_total"));
				player.setFrozen(rs.getInt("frozen")==1);
				player.setSilent(rs.getInt("silent")==1);
				list.add(player);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(rs, pstmt, conn);
		}
		return list;
	}
	
	public void chartsRecordAdd(JSONObject j) {
		int nTime = j.getIntValue(Protocols.DB_charts_update_record.TIME);
		String strUuid = j.getString(Protocols.DB_charts_update_record.UUID);
		String strColumnName = j.getString(Protocols.DB_charts_update_record.COLUMN_NAME);
		long nColumnValue = j.getLongValue(Protocols.DB_charts_update_record.COLUMN_VALUE);
		
		Connection conn=null;
		PreparedStatement pstmt = null;
		try {
			conn=getConn();
			StringBuffer buffer = new StringBuffer();
			buffer.append("insert into charts_record(time, uuid, ");
			buffer.append(strColumnName);
			buffer.append(") values(");
			buffer.append(nTime);
			buffer.append(", '");
			buffer.append(strUuid);
			buffer.append("',");
			buffer.append(nColumnValue);
			buffer.append(") on duplicate key update ");
			buffer.append(strColumnName);
			buffer.append("=");
			buffer.append(strColumnName);
			buffer.append("+");
			buffer.append(nColumnValue);
			
			pstmt=conn.prepareStatement(buffer.toString());
			pstmt.executeUpdate();
			
			//log.debug("sql=" + buffer.toString() + " ]");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(null, pstmt, conn);
		}
	}
	
	public void playerCharge(String uuid,long coin,int amount){
		Connection conn=null;
		PreparedStatement pstmt = null;
		try {
			conn=getConn();
			pstmt=conn.prepareStatement("update player set coin=coin+"+coin+",charge_total=charge_total+"+amount+" where uuid=?");
			pstmt.setString(1, uuid);
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(null, pstmt, conn);
		}
	}
}
