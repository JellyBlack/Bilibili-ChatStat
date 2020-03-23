package script;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gui.LivePanel;

/**
 * ֱ����Ļ��ȡ
 */
public class LiveChat implements Runnable, MultiThreadOperation {

	File file;
	private static String stringBuff = "";
	private static boolean liveRoomHandled;
	private static LiveChatUtil liveChatUtil;
	private Thread util_thread;
	private OnChatUtilFinished class_;

	/**
	 * ʵ����
	 * 
	 * @param file ������ļ�
	 */
	public LiveChat(File file, boolean liveRoomHandled, OnChatUtilFinished class_) {
		this.file = file;
		LiveChat.liveRoomHandled = liveRoomHandled;
		this.class_ = class_;
	}

	/**
	 * ����
	 */
	@Override
	public void run() {
		liveChatUtil = new LiveChatUtil(2048);
		util_thread = new Thread(liveChatUtil);
		util_thread.start();
		if (!liveRoomHandled) {
			LivePanel.getInstance().log("###  ���ڴ������  ###");
			LivePanel.getInstance().refreshUi();
			if (!utilRoomNumber()) {
				LivePanel.getInstance().onUtilRoomNumberFailed(2);
				return;
			}
			LivePanel.getInstance().log("###  ������ϣ�ץȡ�ѿ�ʼ  ###");
			LivePanel.getInstance().refreshUi();
		}
		if (Config.live_config.MULTI_THREAD) {
			MultiThreadUtil multiThreadUtil = new MultiThreadUtil();
			multiThreadUtil.init(this, (int) (Config.live_config.MAX_DELAY * 1.5));
			multiThreadUtil.start();
		} else {
			while (Config.live_config.STATUS) {
				try {
					liveChatUtil.push(request());
				} catch (IOException e) {
					e.printStackTrace();
					LivePanel.getInstance().log("###############");
					LivePanel.getInstance().log(e.getMessage());
					LivePanel.getInstance().log("�������쳣��" + e.getClass().getName());
					LivePanel.getInstance().log("###############");
					LivePanel.getInstance().refreshUi();
				}
				try {
					Thread.sleep(Config.live_config.DELAY);
				} catch (InterruptedException e) {
					if (!Config.live_config.STATUS) {
						LivePanel.getInstance().log("###  ץȡ�ѽ���  ###");
						LivePanel.getInstance().refreshUi();
					}
				}
			}
		}
		// ����ж�״̬
		Thread.interrupted();
		try {
			util_thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		class_.onChatUtilFinish();
	}

	/**
	 * ����HTML����
	 * 
	 * @return ���������ص��ַ���
	 * @throws IOException IO�쳣
	 */
	public static String request() throws IOException {
		try {
			URL url = new URL("https://api.live.bilibili.com/ajax/msg");
			byte[] postDataBytes = new String("roomid=" + Config.live_config.ROOM).getBytes("UTF-8");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setConnectTimeout(500);
			conn.setReadTimeout(500);
			conn.getOutputStream().write(postDataBytes);
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

	/**
	 * �ӷ�������ȡ�ַ���
	 * 
	 * @param url_ url
	 * @return �ַ���
	 * @throws IOException IO�쳣
	 */
	private static String getDataFromServer(String url_) throws IOException {
		URL url = new URL(url_);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setDoOutput(true);
		conn.setConnectTimeout(20000);
		conn.setReadTimeout(20000);
		Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
		StringBuilder sb = new StringBuilder();
		for (int c; (c = in.read()) >= 0;) {
			sb.append((char) c);
		}
		conn.disconnect();
		return sb.toString();
	}

	/**
	 * ��ȡ��Ļʵ�������
	 * 
	 * @return ��Ļ
	 */
	public static Chat getChat() {
		return LiveChatUtil.getChat();
	}

	/**
	 * ��ȡ��Ļ��ɫ����
	 * 
	 * @return ��ɫ
	 */
	public static int[] getChatColor() {
		return LiveChatUtil.getChatColor();
	}

	/**
	 * ��ȡ�洢��json
	 * 
	 * @return json
	 */
	public static JSONArray getJSONArray() {
		return LiveChatUtil.getJSONArray();
	}

	public static boolean utilRoomNumber() {
		try {
			String json = getDataFromServer(
					"https://api.live.bilibili.com/room_ex/v1/RoomNews/get?roomid=" + Config.live_config.ROOM);
			String room = new JSONObject(json).getJSONObject("data").getString("roomid");
			Config.live_config.ROOM = Integer.parseInt(room);
			LivePanel.getInstance().setRoomNumber();
		} catch (IOException | JSONException | NumberFormatException e) {
			try {
				String json = getDataFromServer(
						"https://api.live.bilibili.com/room_ex/v1/RoomNews/get?roomid=" + Config.live_config.ROOM);
				int room = new JSONObject(json).getJSONObject("data").getInt("roomid");
				Config.live_config.ROOM = room;
				LivePanel.getInstance().setRoomNumber();
			} catch (IOException | JSONException | NumberFormatException err) {
				LivePanel.getInstance().log("###############");
				LivePanel.getInstance().log(e.getMessage());
				LivePanel.getInstance().log("�������쳣��" + err.getClass().getName());
				LivePanel.getInstance().log("###############");
				LivePanel.getInstance().refreshUi();
				return false;
			}
		}
		return true;
	}

	@Override
	public void callback(String json) {
		liveChatUtil.push(json);
	}

	public static boolean isLiveRoomHandled() {
		return liveRoomHandled;
	}

	public static void setLiveRoomHandled(boolean liveRoomHandled) {
		LiveChat.liveRoomHandled = liveRoomHandled;
	}

	public static void saveToXml(OnXmlUtilFinished c) {
		boolean successful = OutputManager.saveToXml(LiveChatUtil.getChat(), LiveChatUtil.getChatColor());
		c.onXmlUtilFinish(successful);
	}

	public static void saveToJson(OnJsonUtilFinished c) {
		boolean successful = OutputManager.saveToJson(LiveChatUtil.getJSONArray());
		c.onJsonUtilFinish(successful);
	}
}
