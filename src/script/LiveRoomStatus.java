package script;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import gui.LivePanel;

public class LiveRoomStatus implements Runnable {

	private static String stringBuff;
	private int expectedStatus;

	public LiveRoomStatus(int expectedStatus) {
		this.expectedStatus = expectedStatus;
	}

	@Override
	public void run() {
		if (expectedStatus == 1) {
			if(!LiveChat.isLiveRoomHandled()) {
				LivePanel.getInstance().log("###  ���ڴ������  ###");
				LivePanel.getInstance().refreshUi();
				if (!LiveChat.utilRoomNumber()) {
					LivePanel.getInstance().onUtilRoomNumberFailed(1);
					return;
				}
				LiveChat.setLiveRoomHandled(true);
				LivePanel.getInstance().log("###  ������ϣ������ѱ���  ###");
				LivePanel.getInstance().refreshUi();
			}
			int i = 0;
			while (!Thread.interrupted()) {
				try {
					String str = request();
					JSONObject json = new JSONObject(str);
					int status = json.getJSONObject("data").getJSONObject("room_info").getInt("live_status");
					/*
					 * 0��δ���� 1��ֱ���� 2���ֲ���
					 */
					if (status == 1) {
						LivePanel.getInstance().log("###  �����ѿ��������Զ���������  ###");
						LivePanel.getInstance().refreshUi();
						LivePanel.getInstance().onLiveStart();
						return;
					}
					i++;
					LivePanel.getInstance().log("###  �ѳɹ���ȡֱ����״̬" + i + "��  ###");
					LivePanel.getInstance().refreshUi();
				} catch (IOException | JSONException e) {
					LivePanel.getInstance().log("###############");
					LivePanel.getInstance().log(e.getMessage());
					LivePanel.getInstance().log("�������쳣��" + e.getClass().getName());
					LivePanel.getInstance().log("###############");
					LivePanel.getInstance().refreshUi();
				}
			}
		} else {
			int i = 0;
			while (!Thread.interrupted()) {
				try {
					String str = request();
					JSONObject json = new JSONObject(str);
					int status = json.getJSONObject("data").getJSONObject("room_info").getInt("live_status");
					/*
					 * 0��δ���� 1��ֱ���� 2���ֲ���
					 */
					if (status != 1) {
						LivePanel.getInstance().log("###  �������²������Զ�ֹͣ����  ###");
						LivePanel.getInstance().refreshUi();
						LivePanel.getInstance().onLiveStop();
						return;
					}
					i++;
					LivePanel.getInstance().log("###  �ѳɹ���ȡֱ����״̬" + i + "��  ###");
					LivePanel.getInstance().refreshUi();
				} catch (IOException | JSONException e) {
					LivePanel.getInstance().log("###############");
					LivePanel.getInstance().log(e.getMessage());
					LivePanel.getInstance().log("�������쳣��" + e.getClass().getName());
					LivePanel.getInstance().log("###############");
					LivePanel.getInstance().refreshUi();
				}
			}
		}
	}

	/**
	 * ����HTML����
	 * 
	 * @return ���������ص��ַ���
	 * @throws IOException IO�쳣
	 */
	public static String request() throws IOException {
		try {
			URL url = new URL("https://api.live.bilibili.com/xlive/web-room/v1/index/getInfoByRoom?room_id="
					+ +Config.live_config.ROOM);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setDoOutput(true);
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			StringBuilder sb = new StringBuilder();
			for (int c; (c = in.read()) >= 0;) {
				sb.append((char) c);
			}
			stringBuff = sb.toString();
			conn.disconnect();
			return sb.toString();
		} catch (SocketTimeoutException e) {
			LivePanel.getInstance().log("�����桿HTTP���ӳ�ʱ");
			LivePanel.getInstance().refreshUi();
			return stringBuff;
		}
	}
}
