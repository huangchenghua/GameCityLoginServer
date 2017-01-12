package com.gz.gamecity.login.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gz.gamecity.bean.Mail;
import com.gz.gamecity.bean.Player;
import com.gz.gamecity.protocol.Protocols;

public class PlayerDataDao extends BaseDao{

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
			String sql = "insert into player (uuid,name,sex,coin,head,lvl,finance,vip,charm,sign,charge_total,create_time,last_time) values(?,?,?,?,?,?,?,?,?,?,?,now(),now())";
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
			sb.append("head=").append(vip).append(",");
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
		
		String finance_str = j.getString(Protocols.G2l_data_change.FINANCE);
		if(finance_str!=null){
			int finance = j.getIntValue(Protocols.G2l_data_change.FINANCE);
			sb.append("finance=").append(finance).append(",");
		}
		
		String sign = j.getString(Protocols.G2l_data_change.FINANCE);
		if(sign!=null){
			sb.append("sign='").append(sign).append("',");
		}
		
		String charm_str = j.getString(Protocols.G2l_data_change.CHARM);
		if(charm_str!=null){
			int charm = j.getIntValue(Protocols.G2l_data_change.CHARM);
			sb.append("charm=").append(charm).append(",");
		}
		
		sb.deleteCharAt(sb.length()-1);
		sb.append(" where uuid=?");
		
		try {
			conn = getConn();
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setString(1, uuid);
			pstmt.executeUpdate();
		} catch (Exception e) {
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
			pstmt = conn.prepareStatement("select * from mail where player_uuid=?");
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
			pstmt = conn.prepareStatement("insert into mail(mail_uuid,player_uuid,title,content,send_time,attachments) values(?,?,?,?,?,?)");
			pstmt.setString(1, j.getString(Protocols.DB_login_new_mail.MAILID));
			pstmt.setString(2, j.getString(Protocols.DB_login_new_mail.UUID));
			pstmt.setString(3, j.getString(Protocols.DB_login_new_mail.TITLE));
			pstmt.setString(4, j.getString(Protocols.DB_login_new_mail.CONTENT));
			pstmt.setString(5, j.getString(Protocols.DB_login_new_mail.SEND_TIME));
			pstmt.setString(6, j.getString(Protocols.DB_login_new_mail.ATTACHMENTS));
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
			pstmt = conn.prepareStatement("update mail set read = 1 where mail_uuid = ?");
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
				mail.setAttachments(rs.getString(""));
				mail.setContent(rs.getString(""));
				mail.setMailId(rs.getString(""));;
				mail.setPlayer_uuid(rs.getString(""));
				mail.setRead(rs.getInt("")==1);
				mail.setTaken(rs.getInt("")==1);
				mail.setTitle(rs.getString(""));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(rs, pstmt, conn);
		}
		return mail;
	}
	
	public void delMail(String ids){
		Connection conn=null;
		PreparedStatement pstmt = null;
		try {
			conn = getConn();
			pstmt = conn.prepareStatement("delete from mail where mail_uuid in ?");
			pstmt.setString(1, ids);
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(null, pstmt, conn);
		}
	}
	
	public int[][] getPlayerGiftList(String uuid){
		int[][] info=null;
		Connection conn=null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = getConn();
			pstmt=conn.prepareStatement("select * from player_gift where player_uuid=?");
			pstmt.setString(1, uuid);
			rs = pstmt.executeQuery();
			if(rs.next()){
				info=new int[2][8];
				for(int i=0;i<8;i++){
					info[0][i] = rs.getInt("id_"+(i+1));
					info[1][i] = rs.getInt("count_"+(i+1));
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
}
