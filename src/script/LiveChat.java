package script;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
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
	private static ArrayList<Integer> color = null;

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
		color = new ArrayList<>();
		String[] ct_old = null;
		total_json = new JSONArray();
		boolean first_run = true;
		boolean prepared = false;
		int[] stat = new int[10];
		int stat_index = 0;
		for (int i = 0; i < stat.length; i++) {
			stat[i] = 0;
		}
		LivePanel.getInstance().log("###  ���ڴ������  ###");
		LivePanel.getInstance().refreshUi();
		try {
			String json = getDataFromServer(
					"https://api.live.bilibili.com/room_ex/v1/RoomNews/get?roomid=" + Config.live_config.ROOM);
			String room = new JSONObject(json).getJSONObject("data").getString("roomid");
			Config.live_config.ROOM = Integer.parseInt(room);
		} catch (IOException | JSONException | NumberFormatException e) {
			try {
				String json = getDataFromServer(
						"https://api.live.bilibili.com/room_ex/v1/RoomNews/get?roomid=" + Config.live_config.ROOM);
				int room = new JSONObject(json).getJSONObject("data").getInt("roomid");
				Config.live_config.ROOM = room;
			} catch (IOException | JSONException | NumberFormatException err) {
				LivePanel.getInstance().log("###############");
				LivePanel.getInstance().log(e.getMessage());
				LivePanel.getInstance().log("�������쳣��" + err.getClass().getName());
				LivePanel.getInstance().log("###############");
				LivePanel.getInstance().refreshUi();
				return;
			}
		}
		LivePanel.getInstance().log("###  ������ϣ�ץȡ�ѿ�ʼ  ###");
		LivePanel.getInstance().refreshUi();
		while (Config.live_config.STATUS) {
			JSONObject jsonObject = null;
			String[] ct_temp = null;
			int buffer = 0;
			int new_chat_count = 0;
			try {
				jsonObject = new JSONObject(request());
			} catch (JSONException | IOException e) {
				LivePanel.getInstance().log("###############");
				LivePanel.getInstance().log(e.getMessage());
				LivePanel.getInstance().log("�������쳣��" + e.getClass().getName());
				LivePanel.getInstance().log("###############");
				LivePanel.getInstance().refreshUi();
			}
			JSONObject data = jsonObject.getJSONObject("data");
			JSONArray room = data.getJSONArray("room");
			LivePanel.getInstance().addTime();
			tick: for (int i = 0; i < room.length(); i++) {
				JSONObject chat_json = room.getJSONObject(i);
				JSONObject check_info = chat_json.getJSONObject("check_info");
				String ct = check_info.getString("ct");
				String text = chat_json.getString("text");
				if (i == 0) {
					ct_temp = new String[room.length()];
				}
				if (prepared) {
					for (int j = 0; j < ct_old.length; j++) {
						// �жϵ�Ļ�Ƿ����
						if (ct.equals(ct_old[j])) {
							buffer++;
							ct_temp[i] = ct;
							continue tick;
						}
					}
				}
				if (i == 0) {
					ct_old = new String[room.length()];
				}
				ct_temp[i] = ct;
				Long time = check_info.getLong("ts");
				// �����Ļ�ķ���ʱ�������趨�Ŀ�ʼʱ�䣬��õ�Ļ����ʷ��Ļ����ץȡ
				if (time * 1000 < Config.live_config.START_TIME) {
					LivePanel.getInstance().log("[��ʷ��Ļ������¼]  " + text);
					continue tick;
				}
				new_chat_count++;
				String text2 = text.replace("<", "&lt;").replace("&", "&amp;");
				// ������ɫ����Ļת������Github��һ��һ���
				// ��ʱֻ֧���⼸�֣���Ϊ�����߷���F12Ҳ�Ҳ������Ƶ�Ļ��ɫ���ֶ�������ᣩ
				// ���ȼ�����Ա > �����ү > �·���ү > ��ͨ
				color_block: {
					if (chat_json.getInt("guard_level") > 0) {
						color.add(0xe33fff);// ��ɫ
						break color_block;
					}
					if (chat_json.getInt("svip") == 1) {
						color.add(0x66ccff);// ��ɫ������ɫŶ������DNA���RGB�ţ�
						break color_block;
					}
					if (chat_json.getInt("vip") == 1) {
						color.add(0xff6868);// ��ɫ
						break color_block;
					}
					color.add(0xffffff);// ��ɫ
				}
				chat.append(text2, chat_json.getInt("uid") + "", (float) (time - Config.live_config.START_TIME / 1000),
						time);
				total_json.put(chat_json);
				if (first_run) {
					LivePanel.getInstance().log("[�����������]  " + text);
				} else if (buffer == 0) {
					LivePanel.getInstance().log("������ - ������" + buffer + "��  " + text);
				} else {
					LivePanel.getInstance().log("[������" + buffer + "]  " + text);
				}
			}
			for (int i = 0; i < room.length(); i++) {
				ct_old[i] = ct_temp[i];
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
			prepared = true;
			if (chat.getCount() > 9) {
				first_run = false;
			}
			LivePanel.getInstance().refreshUi();
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
	private String getDataFromServer(String url_) throws IOException {
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
		return sb.toString();
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
	 * ��ȡ��Ļ��ɫ����
	 * 
	 * @return ��ɫ
	 */
	public static int[] getChatColor() {
		int[] c = new int[color.size()];
		for (int i = 0; i < c.length; i++) {
			c[i] = color.get(i);
		}
		return c;
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
