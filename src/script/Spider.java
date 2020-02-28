package script;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gui.FileManager;
import gui.MainGui;
import gui.Reminding;
import gui.UpInfo;

/**
 * ��������С���ݿ���Ա�������� ��Ƶǧ������ ��Ȩ��һ���� ���治�淶�� �ο������ᡣ
 */
public class Spider implements Runnable {
	private static Spider instance;
	private Thread currentThread;
	private boolean confirmed = false;
	private String[] pages;

	@Override
	public void run() {
		instance = this;
		currentThread = Thread.currentThread();
		MainGui.getInstance().setButtonText("����׼�������Ժ�");
		MainGui.getInstance().log("׼���������ڽ��У����Ժ�......");
		if (Config.spider_config.mode == 0) {
			confirmed = false;
			try {
				JSONObject data = getVideoInfoJson(Config.spider_config.avs[0] + "");
				MainGui.getInstance().log("��ȡ��ƵJSON�ɹ�");
				String title = getVideoTitle(data);
				MainGui.getInstance().log("��ȡ��Ƶ����ɹ��� " + title);
				pages = getVideoPages(data);
				MainGui.getInstance().log("��ȡ��Ƶ��P��Ϣ�ɹ�����P���� " + pages.length);
				if (pages.length == 0) {
					MainGui.getInstance().log("�����桿��Ļ��ȡʧ�ܣ���Ƶ��P��Ϊ0��");
					MainGui.getInstance().reset();
					return;
				}
				if (pages.length == 1) {
					FileManager.showFileSaveDialog(null, getUpName(data) + " - " + title, 2);
					if (OutputManager.getFile() == null) {
						MainGui.getInstance().log("��δѡ������ļ�����ȡ����ֹ");
						MainGui.getInstance().reset();
						return;
					}
					MainGui.getInstance().log("����ѡ������ļ���" + OutputManager.getFile().getPath());
					new Reminding().setVisible(true);
					try {
						Thread.sleep(Long.MAX_VALUE);
					} catch (InterruptedException e) {
						// ����ִ�в���
						if (!confirmed) {
							MainGui.getInstance().log("����ȡ����ȡ");
							MainGui.getInstance().reset();
							return;
						}
						MainGui.getInstance().log("��Ƶ��Ļ��ȡ��ʼ");
						confirmed = false;
						String chatStr = null;
						try {
							chatStr = getChatByCid(pages[0]);
							MainGui.getInstance().log("��Ļ�ļ���ȡ���");
						} catch (ParseException e1) {
							MainGui.getInstance().log("�����桿��Ļ��ȡʧ�ܣ� " + e1.toString());
							MainGui.getInstance().reset();
							e1.printStackTrace();
							return;
						}
						OutputManager.saveToXml(chatStr);
						MainGui.getInstance().log("��ȡ�ɹ���");
						MainGui.getInstance().reset();
						OutputManager.setFile(null);
					}
				} else {
					FileManager.showFileSaveDialog(null, "", 3);
					if (OutputManager.getFile() == null) {
						MainGui.getInstance().log("��δѡ�����Ŀ¼����ȡ����ֹ");
						MainGui.getInstance().reset();
						return;
					}
					MainGui.getInstance().log("����ѡ�����Ŀ¼��" + OutputManager.getFile().getPath());
					new Reminding().setVisible(true);
					try {
						Thread.sleep(Long.MAX_VALUE);
					} catch (InterruptedException e) {
						// ����ִ�в���
						if (!confirmed) {
							MainGui.getInstance().log("����ȡ����ȡ");
							MainGui.getInstance().reset();
							return;
						}
						MainGui.getInstance().log("��Ƶ��Ļ��ȡ��ʼ");
						MainGui.getInstance().setButtonText("������ȡ��...");
						confirmed = false;
						String name = getUpName(data);
						for (int i = 0; i < pages.length; i++) {
							String chatStr = null;
							try {
								chatStr = getChatByCid(pages[i]);
								MainGui.getInstance().log("��P�� " + (i + 1) + "P��Ļ��ȡ���");
							} catch (ParseException e1) {
								MainGui.getInstance().log("�����桿��Ļ��ȡʧ�ܣ� " + e1.toString());
								e1.printStackTrace();
							}
							OutputManager.saveToXml(chatStr,
									OutputManager.getFile().getPath() + "\\" + OutputManager.replaceFileName(name
											+ " - " + title + " ��" + (i + 1) + "P��" + getPageName(data, i) + "��.xml"));
							randomSleep();
						}
						OutputManager.setFile(null);
						MainGui.getInstance().log("��ȡ�ɹ���");
						MainGui.getInstance().reset();
					}
				}
			} catch (IOException e) {
				MainGui.getInstance().log("�����桿��Ļ��ȡʧ�ܣ� " + e.toString());
				MainGui.getInstance().reset();
				e.printStackTrace();
				return;
			} catch (JSONException e) {
				MainGui.getInstance().log("�����桿�ڻ�ȡav" + Config.spider_config.avs[0] + "�ĵ�Ļʱ����������Ƶ���ܲ�����");
				MainGui.getInstance().reset();
				e.printStackTrace();
				return;
			}
		} else if (Config.spider_config.mode == 1) {
			confirmed = false;
			try {
				FileManager.showFileSaveDialog(null, "", 3);
				if (OutputManager.getFile() == null) {
					MainGui.getInstance().log("��δѡ�����Ŀ¼����ȡ����ֹ");
					MainGui.getInstance().reset();
					return;
				}
				MainGui.getInstance().log("����ѡ�����Ŀ¼��" + OutputManager.getFile().getPath());
				new Reminding().setVisible(true);
				try {
					Thread.sleep(Long.MAX_VALUE);
				} catch (InterruptedException err) {
					// ����ִ�в���
					if (!confirmed) {
						FileManager.deleteTempDir();
						MainGui.getInstance().log("����ȡ����ȡ");
						MainGui.getInstance().reset();
						return;
					}
					int[] avs = Config.spider_config.avs;
					MainGui.getInstance().log("��Ƶ��Ļ��ȡ��ʼ���ܼ���Ƶ��Ϊ " + avs.length);
					MainGui.getInstance().setButtonText("������ȡ��...");
					confirmed = false;
					for (int i = 0; i < avs.length; i++) {
						JSONObject data = null;
						try {
							data = getVideoInfoJson(avs[i] + "");
						} catch (JSONException e) {
							MainGui.getInstance().log("�����桿�ڻ�ȡav" + Config.spider_config.avs[i] + "�ĵ�Ļʱ����������Ƶ���ܲ�����");
							continue;
						}
						String name = getUpName(data);
						String title = getVideoTitle(data);
						pages = getVideoPages(data);
						for (int j = 0; j < pages.length; j++) {
							String chatStr = null;
							try {
								chatStr = getChatByCid(pages[j]);
							} catch (ParseException e1) {
								MainGui.getInstance().log("�����桿��Ļ��ȡʧ�ܣ� " + e1.toString());
								e1.printStackTrace();
							}
							if (pages.length == 1) {
								OutputManager.saveToXml(chatStr, OutputManager.getFile().getPath() + "\\"
										+ OutputManager.replaceFileName(name + " - " + title + ".xml"));
								MainGui.getInstance().log("�� " + (i + 1) + " ����Ƶ��ȡ���");
							} else {
								OutputManager.saveToXml(chatStr,
										OutputManager.getFile().getPath() + "\\"
												+ OutputManager.replaceFileName(name + " - " + title + " ��" + (j + 1)
														+ "P��" + getPageName(data, j) + "��.xml"));
								MainGui.getInstance().log("�� " + (i + 1) + " ����Ƶ��" + (j + 1) + "P����ȡ���");
							}
							randomSleep();
						}
					}
					OutputManager.setFile(null);
					MainGui.getInstance().log("��ȡ�ɹ���");
					MainGui.getInstance().reset();
				}
			} catch (IOException e) {
				MainGui.getInstance().log("�����桿��Ļ��ȡʧ�ܣ� " + e.toString());
				MainGui.getInstance().reset();
				e.printStackTrace();
				return;
			} catch (JSONException e) {
				MainGui.getInstance().log("�����桿�ڻ�ȡ��Ƶ��Ļʱ��������" + e.toString());
				MainGui.getInstance().reset();
				e.printStackTrace();
				return;
			}
		} else if (Config.spider_config.mode == 2) {
			confirmed = false;
			try {
				Up up = null;
				try {
					up = getUpInfo(Config.spider_config.uid + "");
				} catch (JSONException e) {
					MainGui.getInstance().log("�����桿�޷���ȡUP����Ϣ��UID���ܲ�����");
					MainGui.getInstance().reset();
					e.printStackTrace();
					return;
				}
				MainGui.getInstance().log("��ȡUP����Ϣ���");
				new UpInfo(up).setVisible(true);
				try {
					Thread.sleep(Long.MAX_VALUE);
				} catch (InterruptedException e) {
					// ����ִ�в���
					if (!confirmed) {
						MainGui.getInstance().log("����ȡ����ȡ");
						MainGui.getInstance().reset();
						return;
					}
					MainGui.getInstance().log("����ȷ��UP����Ϣ");
					confirmed = false;
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					String date = format.format(new Date());
					FileManager.showFileSaveDialog(null, up.name + " - " + date + "ȫ����Ƶ��Ļ", 3);
					if (OutputManager.getFile() == null) {
						FileManager.deleteTempDir();
						MainGui.getInstance().log("��δѡ�����Ŀ¼����ȡ����ֹ");
						MainGui.getInstance().reset();
						return;
					}
					MainGui.getInstance().log("����ѡ�����Ŀ¼��" + OutputManager.getFile().getPath());
					new Reminding().setVisible(true);
					try {
						Thread.sleep(Long.MAX_VALUE);
					} catch (InterruptedException err) {
						// ����ִ�в���
						if (!confirmed) {
							FileManager.deleteTempDir();
							MainGui.getInstance().log("����ȡ����ȡ");
							MainGui.getInstance().reset();
							return;
						}
						MainGui.getInstance().log("��Ƶ��Ļ��ȡ��ʼ");
						MainGui.getInstance().setButtonText("������ȡ��...");
						String[] avs = getAvsOfUp(up.uid);
						MainGui.getInstance().log("�ѻ�ȡUP����������Ƶ������Ϊ " + avs.length);
						confirmed = false;
						for (int i = 0; i < up.videos; i++) {
							JSONObject data = null;
							try {
								data = getVideoInfoJson(avs[i] + "");
							} catch (JSONException error) {
								MainGui.getInstance().log("�����桿�ڻ�ȡav" + avs[i] + "�ĵ�Ļʱ����������Ƶ���ܲ�����");
								continue;
							}
							String title = getVideoTitle(data);
							pages = getVideoPages(data);
							for (int j = 0; j < pages.length; j++) {
								String chatStr = null;
								try {
									chatStr = getChatByCid(pages[j]);
								} catch (ParseException e1) {
									MainGui.getInstance().log("�����桿��Ļ��ȡʧ�ܣ� " + e1.toString());
									MainGui.getInstance().reset();
									e1.printStackTrace();
									return;
								}
								if (pages.length == 1) {
									OutputManager.saveToXml(chatStr, OutputManager.getFile().getPath() + "\\"
											+ OutputManager.replaceFileName(up.name + " - " + title + ".xml"));
									MainGui.getInstance().log("�� " + (i + 1) + " ����Ƶ��ȡ���");
								} else {
									OutputManager.saveToXml(chatStr,
											OutputManager.getFile().getPath() + "\\"
													+ OutputManager.replaceFileName(up.name + " - " + title + " ��"
															+ (j + 1) + "P��" + getPageName(data, j) + "��.xml"));
									MainGui.getInstance().log("�� " + (i + 1) + " ����Ƶ��" + (j + 1) + "P����ȡ���");
								}
								randomSleep();
							}
						}
						OutputManager.setFile(null);
						MainGui.getInstance().log("��ȡ�ɹ���");
						MainGui.getInstance().reset();
					}
				}
			} catch (IOException e) {
				MainGui.getInstance().log("�����桿��Ļ��ȡʧ�ܣ� " + e.toString());
				MainGui.getInstance().reset();
				e.printStackTrace();
				return;
			}
		}
	}

	private String[] getAvsOfUp(String uid) throws IOException, JSONException {
		int count;
		ArrayList<String> avs = new ArrayList<>();
		String first = getDataFromServer(
				"https://api.bilibili.com/x/space/arc/search?mid=" + uid + "&pn=1&ps=1&jsonp=jsonp");
		JSONObject first_root = new JSONObject(first);
		JSONObject first_data = first_root.getJSONObject("data");
		JSONObject first_page = first_data.getJSONObject("page");
		count = first_page.getInt("count");
		int currentPage = 1;
		while (avs.size() != count) {
			String json = getDataFromServer("https://api.bilibili.com/x/space/arc/search?mid=" + uid + "&pn="
					+ currentPage + "&ps=100&jsonp=jsonp");
			JSONArray array = new JSONObject(json).getJSONObject("data").getJSONObject("list").getJSONArray("vlist");
			for (int i = 0; i < array.length(); i++) {
				JSONObject video = array.getJSONObject(i);
				avs.add(video.getInt("aid") + "");
			}
			currentPage++;
			randomSleep();
		}
		return avs.toArray(new String[0]);
	}

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

	private int getUpVideosCount(String uid) throws IOException {
		int count;
		String first = getDataFromServer(
				"https://api.bilibili.com/x/space/arc/search?mid=" + uid + "&pn=1&ps=1&jsonp=jsonp");
		JSONObject first_root = new JSONObject(first);
		JSONObject first_data = first_root.getJSONObject("data");
		JSONObject first_page = first_data.getJSONObject("page");
		count = first_page.getInt("count");
		return count;
	}

	private Up getUpInfo(String uid) throws IOException, JSONException {
		Up up = new Up();
		String info = getDataFromServer("https://api.bilibili.com/x/space/acc/info?mid=" + uid + "&jsonp=jsonp");
		JSONObject data = new JSONObject(info).getJSONObject("data");
		up.uid = uid;
		up.name = data.getString("name");
		up.level = data.getInt("level");
		up.sex = data.getString("sex");
		up.face_url = new URL(data.getString("face"));
		up.sign = data.getString("sign");
		up.role = data.getJSONObject("official").getInt("role");
		up.videos = getUpVideosCount(uid);
		return up;
	}

	private JSONObject getVideoInfoJson(String av) throws IOException, JSONException {
		String text = getDataFromServer("https://api.bilibili.com/x/web-interface/view?aid=" + av);
		return new JSONObject(text).getJSONObject("data");
	}

	private String getVideoTitle(JSONObject json) {
		return json.getString("title");
	}

	private String getPageName(JSONObject json, int page) {
		return json.getJSONArray("pages").getJSONObject(page).getString("part");
	}

	private String getUpName(JSONObject json) {
		return json.getJSONObject("owner").getString("name");
	}

	private String[] getVideoPages(JSONObject json) throws IOException {
		ArrayList<String> str = new ArrayList<>();
		JSONArray pages = json.getJSONArray("pages");
		for (int i = 0; i < pages.length(); i++) {
			str.add(pages.getJSONObject(i).getInt("cid") + "");
		}
		return str.toArray(new String[0]);
	}

	private void randomSleep() {
		try {
			Thread.sleep(2000 + (int) Math.random() * 2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
		}
	}

	/*
	 * �������߿ࣺ ����������Ļxml��ѹ�����˵ģ����������xml�ļ����Զ���ѹ�Ӷ������ȷ���ļ������ܳ����򲻻ᡣ
	 * TMD��������������������죬�ô��뵽��ҹ�����������ֱ�Ӹ��ѹ�㷨��ֱ�����һ�̲������õ������⣬�ɹ���ȡ��ʱ��ү�����������ݺ�23333
	 */
	private String getChatByCid(String cid) throws ParseException, IOException {
		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet("https://comment.bilibili.com/" + cid + ".xml");
		CloseableHttpResponse response = client.execute(httpGet);
		HttpEntity entity = response.getEntity();
		String string = EntityUtils.toString(entity);
		response.close();
		return string;
	}

	public static class Up {
		public String uid;// uid
		public String name;// UP��������
		public int level;// �ȼ�
		public String sex;// �Ա�
		public URL face_url;// ͷ���url
		public String sign;// ����
		public int role;// ��֤��Ϣ��0Ϊ����֤��1Ϊ��ɫ���磬2Ϊ��ɫ����
		public int videos;// UP������Ƶ��
	}

	public static Spider getInstance() {
		return instance;
	}

	public Thread getThread() {
		return currentThread;
	}

	public void confirm() {
		confirmed = true;
	}

}
