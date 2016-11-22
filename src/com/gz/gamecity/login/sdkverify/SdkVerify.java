package com.gz.gamecity.login.sdkverify;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

public class SdkVerify {
	
	
	
	public static String http(String url, Map<String, String> params) throws Exception{

		URL u = null;

		HttpURLConnection con = null;

		// 构建请求参数

		StringBuffer sb = new StringBuffer();

		if (params != null) {

			for (Entry<String, String> e : params.entrySet()) {

				sb.append(e.getKey());

				sb.append("=");

				sb.append(e.getValue());

				sb.append("&");

			}

			sb.substring(0, sb.length() - 1);

		}

//		System.out.println("send_url:" + url);

//		System.out.println("send_data:" + sb.toString());

		// 尝试发送请求

		try {

			u = new URL(url);

			con = (HttpURLConnection) u.openConnection();

			con.setRequestMethod("POST");

			con.setDoOutput(true);

			con.setDoInput(true);

			con.setUseCaches(false);

			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream(), "UTF-8");

			osw.write(sb.toString());

			osw.flush();

			osw.close();

		} catch (Exception e) {

			throw e;

		} finally {

			if (con != null) {

				con.disconnect();

			}

		}

		// 读取返回内容

		StringBuffer buffer = new StringBuffer();
		InputStream in=null;
		try {
			in = con.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));

			String temp;

			while ((temp = br.readLine()) != null) {

				buffer.append(temp);

				buffer.append("\n");

			}

		} catch (Exception e) {

			throw e;

		} finally {
			if(in!=null)try {
				in.close();
			} catch (Exception e2) {
			}
				
		}

		return buffer.toString();

	}
}
