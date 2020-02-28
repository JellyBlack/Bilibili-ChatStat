package script;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gui.LivePanel;

/**
 * ֱ����Ļ��ȡ
 */
public class LiveChat implements Runnable {

	File file;
	private static Chat chat;
	private static JSONArray total_json;
	private static String stringBuff = "";

	/**
	 * ʵ����
	 * 
	 * @param file ������ļ�
	 */
	public LiveChat(File file) {
		this.file = file;
	}

	/**
	 * ����
	 */
	@Override
	public void run() {
		chat = new Chat(new ArrayList<String>(), new ArrayList<String>(), new ArrayList<Float>(),
				new ArrayList<Long>());
		JSONArray jsonArray_old = new JSONArray();
		total_json = new JSONArray();
		boolean first_run = true;
		int[] stat = new int[10];
		int stat_index = 0;
		for (int i = 0; i < stat.length; i++) {
			stat[i] = 0;
		}
		LivePanel.getInstance().log("###  ץȡ�ѿ�ʼ  ###");
		LivePanel.getInstance().refreshUi();
		while (Config.live_config.STATUS) {
			JSONObject jsonObject = null;
			int buffer = 0;
			int new_chat_count = 0;
			try {
				jsonObject = new JSONObject(request());
			} catch (JSONException | IOException e) {
				LivePanel.getInstance().log("###############");
				LivePanel.getInstance().log(e.getMessage());
				LivePanel.getInstance().log("�������쳣��" + e.getClass().getName());
				LivePanel.getInstance().log("###############");
			}
			JSONObject data = jsonObject.getJSONObject("data");
			JSONArray room = data.getJSONArray("room");
			LivePanel.getInstance().addTime();
			tick: for (int i = 0; i < room.length(); i++) {
				for (int j = 0; j < jsonArray_old.length(); j++) {
					if ((((JSONObject) room.get(i)).get("rnd"))
							.equals(((JSONObject) jsonArray_old.get(j)).get("rnd"))) {
						buffer++;
						continue tick;
					}
				}
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				long time = 0;
				try {
					time = (format.parse(((JSONObject) room.get(i)).getString("timeline"))).getTime();
				} catch (JSONException | ParseException e) {
					LivePanel.getInstance().log("###############");
					LivePanel.getInstance().log(e.getMessage());
					LivePanel.getInstance().log("�������쳣��" + e.getClass().getName());
					LivePanel.getInstance().log("###############");
				}
				// �����Ļ�ķ���ʱ�������趨�Ŀ�ʼʱ�䣬��õ�Ļ����ʷ��Ļ����ץȡ
				if (time < Config.live_config.START_TIME) {
					LivePanel.getInstance().log("[��ʷ��Ļ������¼]  " + ((JSONObject) room.get(i)).getString("text"));
					continue tick;
				}
				new_chat_count++;
				String text = ((JSONObject) room.get(i)).getString("text");
				text = text.replace("<", "&lt;");
				text = text.replace("&", "&amp;");
				chat.append(text, ((JSONObject) room.get(i)).getInt("uid") + "",
						(float) ((time - Config.live_config.START_TIME) / 1000), time / 1000);
				total_json.put(room.getJSONObject(i));
				if (first_run) {
					LivePanel.getInstance().log("[�����������]  " + ((JSONObject) room.get(i)).getString("text"));
				} else if (buffer == 0) {
					LivePanel.getInstance()
							.log("������ - ������" + buffer + "��  " + ((JSONObject) room.get(i)).getString("text"));
				} else {
					LivePanel.getInstance().log("[������" + buffer + "]  " + ((JSONObject) room.get(i)).getString("text"));
				}
			}
			if (!first_run && buffer == 0) {
				if (Config.live_config.AUTO_DELAY && !first_run) {
					if (Config.live_config.DELAY >= 4000) {
						Config.live_config.DELAY -= 2000;
						LivePanel.getInstance().log("###  ��ʱ�����ܵ���2000ms  ###");
						LivePanel.getInstance().refreshDelayField();
					} else if (Config.live_config.DELAY >= 2000) {
						Config.live_config.DELAY -= 1000;
						LivePanel.getInstance().log("###  ��ʱ�����ܵ���1000ms  ###");
						LivePanel.getInstance().refreshDelayField();
					} else if (Config.live_config.DELAY >= 1000) {
						Config.live_config.DELAY -= 600;
						LivePanel.getInstance().log("###  ��ʱ�����ܵ���600ms  ###");
						LivePanel.getInstance().refreshDelayField();
					} else if (Config.live_config.DELAY >= 200) {
						Config.live_config.DELAY -= 200;
						LivePanel.getInstance().log("###  ��ʱ�����ܵ���2000ms  ###");
						LivePanel.getInstance().refreshDelayField();
					} else {
						Config.live_config.DELAY = 0;
						LivePanel.getInstance().log("###  ��ʱ����������Ϊ0  ###");
						LivePanel.getInstance().refreshDelayField();
					}
				}
			}

			jsonArray_old = room;
			if (stat_index != stat.length - 1) {
				stat[stat_index] = buffer;
				stat_index++;
			} else {
				stat[stat_index] = buffer;
				stat_index = 0;
				if (Config.live_config.AUTO_DELAY && !first_run) {
					int sum = 0;
					for (int k = 0; k < stat.length; k++) {
						sum += stat[k];
					}
					double average = (double) sum / stat.length;
					if (average < 4.0) {
						if (Config.live_config.DELAY >= 4000) {
							Config.live_config.DELAY -= 1000;
							LivePanel.getInstance().log("###  ��ʱ�����ܵ���1000ms  ###");
							LivePanel.getInstance().refreshDelayField();
						} else if (Config.live_config.DELAY >= 2000) {
							Config.live_config.DELAY -= 500;
							LivePanel.getInstance().log("###  ��ʱ�����ܵ���500ms  ###");
							LivePanel.getInstance().refreshDelayField();
						} else if (Config.live_config.DELAY >= 1000) {
							Config.live_config.DELAY -= 300;
							LivePanel.getInstance().log("###  ��ʱ�����ܵ���300ms  ###");
							LivePanel.getInstance().refreshDelayField();
						} else if (Config.live_config.DELAY >= 200) {
							Config.live_config.DELAY -= 100;
							LivePanel.getInstance().log("###  ��ʱ�����ܵ���100ms  ###");
							LivePanel.getInstance().refreshDelayField();
						} else {
							Config.live_config.DELAY = 0;
							LivePanel.getInstance().log("###  ��ʱ����������Ϊ0  ###");
							LivePanel.getInstance().refreshDelayField();
						}
					} else if (average > 7.5) {
						Config.live_config.DELAY += (int) (Config.live_config.DELAY * 0.25 + 50);
						LivePanel.getInstance().log("###  ��ʱ�����ܵ��ߣ�50ms + 25%��  ###");
						LivePanel.getInstance().refreshDelayField();
					}
				}
			}
			LivePanel.getInstance().refreshLabel(new_chat_count, buffer, first_run);
			buffer = 0;
			if (chat.getCount() > 9) {
				first_run = false;
			}
			LivePanel.getInstance().refreshUi();
			try {
				Thread.sleep(Config.live_config.DELAY);
			} catch (InterruptedException e) {
				LivePanel.getInstance().log("###  ץȡ�ѽ���  ###");
				LivePanel.getInstance().refreshUi();
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
			URL url = new URL("https://api.live.bilibili.com/ajax/msg");
			byte[] postDataBytes = new String("roomid=" + Config.live_config.ROOM).getBytes("UTF-8");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			conn.getOutputStream().write(postDataBytes);
			Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			StringBuilder sb = new StringBuilder();
			for (int c; (c = in.read()) >= 0;) {
				sb.append((char) c);
			}
			stringBuff = sb.toString();
			return sb.toString();
		} catch (SocketTimeoutException e) {
			LivePanel.getInstance().log("�����桿HTTP���ӳ�ʱ");
			return stringBuff;
		}
	}

	/**
	 * ��ȡ��Ļʵ�������
	 * 
	 * @return ��Ļ
	 */
	public static Chat getChat() {
		return chat;
	}

	/**
	 * ��ȡ�洢��json
	 * 
	 * @return json
	 */
	public static JSONArray getJSONArray() {
		return total_json;
	}
}
