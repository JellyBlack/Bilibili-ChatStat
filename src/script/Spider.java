package script;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
 * 哔哩哔哩小黑屋看守员提醒您： 视频千万条， 版权第一条。 爬虫不规范， 游客两行泪。
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
		MainGui.getInstance().setButtonText("正在准备，请稍候");
		MainGui.getInstance().log("准备工作正在进行，请稍候......");
		int failures = 0;// 对于不直接返回的异常，每发生一次该变量加一
		MainGui.getInstance().setEnabled(false);
		if (Config.spider_config.mode == 0) {
			confirmed = false;
			try {
				JSONObject data = getVideoInfoJson(Config.spider_config.avs[0] + "");
				MainGui.getInstance().log("获取视频JSON成功");
				String title = getVideoTitle(data);
				MainGui.getInstance().log("获取视频标题成功： " + title);
				pages = getVideoPages(data);
				MainGui.getInstance().log("获取视频分P信息成功，总P数： " + pages.length);
				if (pages.length == 0) {
					MainGui.getInstance().log("【警告】弹幕爬取失败，视频分P数为0。");
					MainGui.getInstance().reset();
					MainGui.getInstance().setEnabled(true);
					return;
				}
				if (pages.length == 1) {
					if (Config.spider_config.HISTORICAL) {
						FileManager.showFileSaveDialog(null, "[历史弹幕] " + getUpName(data) + " - " + title, 2);
					}
					if (OutputManager.getFile() == null) {
						MainGui.getInstance().log("您未选择输出文件，爬取已终止");
						MainGui.getInstance().setEnabled(true);
						MainGui.getInstance().reset();
						return;
					}
					MainGui.getInstance().log("您已选择输出文件：" + OutputManager.getFile().getPath());
					new Reminding().setVisible(true);
					try {
						Thread.sleep(Long.MAX_VALUE);
					} catch (InterruptedException e) {
						// 继续执行操作
						if (!confirmed) {
							MainGui.getInstance().log("您已取消爬取");
							OutputManager.setFile(null);
							MainGui.getInstance().reset();
							MainGui.getInstance().setEnabled(true);
							return;
						}
						MainGui.getInstance().log("视频弹幕爬取开始");
						confirmed = false;
						String chatStr = null;
						try {
							chatStr = getChatByCid(pages[0], data);
							MainGui.getInstance().log("弹幕文件获取完毕");
						} catch (ParseException e1) {
							MainGui.getInstance().log("【警告】弹幕爬取失败： " + e1.toString());
							MainGui.getInstance().reset();
							e1.printStackTrace();
							MainGui.getInstance().setEnabled(true);
							MainGui.getInstance().log("爬取完毕，共1处错误，您可以查看爬取日志");
							return;
						}
						OutputManager.saveToXml(chatStr);
						MainGui.getInstance().log("爬取成功，一切顺利！");
						MainGui.getInstance().reset();
						MainGui.getInstance().setEnabled(true);
						OutputManager.setFile(null);
					}
				} else {
					FileManager.showFileSaveDialog(null, "", 3);
					if (OutputManager.getFile() == null) {
						MainGui.getInstance().log("您未选择输出目录，爬取已终止");
						MainGui.getInstance().setEnabled(true);
						MainGui.getInstance().reset();
						return;
					}
					MainGui.getInstance().log("您已选择输出目录：" + OutputManager.getFile().getPath());
					new Reminding().setVisible(true);
					try {
						Thread.sleep(Long.MAX_VALUE);
					} catch (InterruptedException e) {
						// 继续执行操作
						if (!confirmed) {
							MainGui.getInstance().log("您已取消爬取");
							OutputManager.setFile(null);
							MainGui.getInstance().setEnabled(true);
							MainGui.getInstance().reset();
							return;
						}
						MainGui.getInstance().log("视频弹幕爬取开始");
						MainGui.getInstance().setButtonText("正在爬取中...");
						confirmed = false;
						String name = getUpName(data);
						for (int i = 0; i < pages.length; i++) {
							String chatStr = null;
							try {
								chatStr = getChatByCid(pages[i], data);
								MainGui.getInstance().log("P" + (i + 1) + " （共" + pages.length + "P）弹幕爬取完毕");
							} catch (ParseException e1) {
								MainGui.getInstance()
										.log("【警告】P" + (i + 1) + " （共" + pages.length + "P）弹幕爬取失败： " + e1.toString());
								failures++;
								e1.printStackTrace();
								randomSleep();
							}
							if (Config.spider_config.HISTORICAL) {
								OutputManager.saveToXml(chatStr,
										OutputManager.getFile().getPath() + File.separator + "[历史弹幕] "
												+ OutputManager.replaceFileName(name + " - " + title + " （P" + (i + 1)
														+ "：" + getPageName(data, i) + "）.xml"));
							} else {
								OutputManager.saveToXml(chatStr,
										OutputManager.getFile().getPath() + File.separator
												+ OutputManager.replaceFileName(name + " - " + title + " （P" + (i + 1)
														+ "：" + getPageName(data, i) + "）.xml"));
							}
							randomSleep();
						}
						OutputManager.setFile(null);
						if (failures == 0) {
							MainGui.getInstance().log("爬取成功，一切顺利！");
						} else {
							MainGui.getInstance().log("爬取完毕，共" + failures + "处错误，您可以查看爬取日志");
						}
						MainGui.getInstance().reset();
						MainGui.getInstance().setEnabled(true);
					}
				}
			} catch (IOException e) {
				MainGui.getInstance().log("【警告】弹幕爬取失败： " + e.toString());
				MainGui.getInstance().reset();
				MainGui.getInstance().setEnabled(true);
				e.printStackTrace();
				return;
			} catch (JSONException e) {
				MainGui.getInstance().log("【警告】在获取" + Config.spider_config.avs[0] + "的弹幕时发生错误，视频可能不存在");
				MainGui.getInstance().setEnabled(true);
				MainGui.getInstance().reset();
				e.printStackTrace();
				return;
			}
		} else if (Config.spider_config.mode == 1) {
			confirmed = false;
			try {
				FileManager.showFileSaveDialog(null, "", 3);
				if (OutputManager.getFile() == null) {
					MainGui.getInstance().log("您未选择输出目录，爬取已终止");
					MainGui.getInstance().setEnabled(true);
					MainGui.getInstance().reset();
					return;
				}
				MainGui.getInstance().log("您已选择输出目录：" + OutputManager.getFile().getPath());
				new Reminding().setVisible(true);
				try {
					Thread.sleep(Long.MAX_VALUE);
				} catch (InterruptedException err) {
					// 继续执行操作
					if (!confirmed) {
						FileManager.deleteTempDir();
						MainGui.getInstance().log("您已取消爬取");
						OutputManager.setFile(null);
						MainGui.getInstance().setEnabled(true);
						MainGui.getInstance().reset();
						return;
					}
					String[] avs = Config.spider_config.avs;
					MainGui.getInstance().log("视频弹幕爬取开始，总计视频数为 " + avs.length);
					MainGui.getInstance().setButtonText("正在爬取中...");
					confirmed = false;
					for (int i = 0; i < avs.length; i++) {
						JSONObject data = null;
						try {
							data = getVideoInfoJson(avs[i] + "");
						} catch (JSONException e) {
							MainGui.getInstance().log("【警告】在获取" + Config.spider_config.avs[i] + "的弹幕时发生错误，视频可能不存在");
							failures++;
							randomSleep();
							continue;
						}
						try {
							String name = getUpName(data);
							String title = getVideoTitle(data);
							pages = getVideoPages(data);
							for (int j = 0; j < pages.length; j++) {
								String chatStr = null;
								try {
									chatStr = getChatByCid(pages[j], data);
								} catch (ParseException e1) {
									MainGui.getInstance().log(
											"【警告】P" + (j + 1) + " （共" + pages.length + "P）弹幕爬取失败： " + e1.toString());
									e1.printStackTrace();
									failures++;
									randomSleep();
									continue;
								}
								if (pages.length == 1) {
									if (Config.spider_config.HISTORICAL) {
										OutputManager.saveToXml(chatStr,
												OutputManager.getFile().getPath() + File.separator + "[历史弹幕] "
														+ OutputManager.replaceFileName(name + " - " + title + ".xml"));
									} else {
										OutputManager.saveToXml(chatStr, OutputManager.getFile().getPath() + File.separator
												+ OutputManager.replaceFileName(name + " - " + title + ".xml"));
									}
									MainGui.getInstance().log("第 " + (i + 1) + " / " + avs.length + " 个视频爬取完毕");
								} else {
									if (Config.spider_config.HISTORICAL) {
										OutputManager.saveToXml(chatStr,
												OutputManager.getFile().getPath() + File.separator + "[历史弹幕] "
														+ OutputManager.replaceFileName(name + " - " + title + " （P"
																+ (j + 1) + "：" + getPageName(data, j) + "）.xml"));
									} else {
										OutputManager.saveToXml(chatStr,
												OutputManager.getFile().getPath() + File.separator
														+ OutputManager.replaceFileName(name + " - " + title + " （P"
																+ (j + 1) + "：" + getPageName(data, j) + "）.xml"));
									}
									MainGui.getInstance().log("第 " + (i + 1) + " / " + avs.length + " 个视频（P" + (j + 1)
											+ " / 共" + pages.length + "P）爬取完毕");
								}
								randomSleep();
							}
						} catch (JSONException e) {
							MainGui.getInstance()
									.log("【警告】P" + (i + 1) + " （共" + pages.length + "P）弹幕爬取失败： " + e.toString());
							failures++;
							e.printStackTrace();
							randomSleep();
						}
					}
					OutputManager.setFile(null);
					if (failures == 0) {
						MainGui.getInstance().log("爬取成功，一切顺利！");
					} else {
						MainGui.getInstance().log("爬取完毕，共" + failures + "处错误，您可以查看爬取日志");
					}
					MainGui.getInstance().reset();
					MainGui.getInstance().setEnabled(true);
				}
			} catch (IOException e) {
				MainGui.getInstance().log("【警告】弹幕爬取失败： " + e.toString());
				MainGui.getInstance().reset();
				MainGui.getInstance().setEnabled(true);
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
					MainGui.getInstance().log("【警告】无法获取UP主信息，UID可能不存在");
					MainGui.getInstance().reset();
					MainGui.getInstance().setEnabled(true);
					e.printStackTrace();
					return;
				}
				MainGui.getInstance().log("获取UP主信息完毕");
				new UpInfo(up).setVisible(true);
				try {
					Thread.sleep(Long.MAX_VALUE);
				} catch (InterruptedException e) {
					// 继续执行操作
					if (!confirmed) {
						MainGui.getInstance().log("您已取消爬取");
						OutputManager.setFile(null);
						MainGui.getInstance().reset();
						MainGui.getInstance().setEnabled(true);
						return;
					}
					MainGui.getInstance().log("您已确认UP主信息");
					confirmed = false;
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					String date = format.format(new Date());
					if (Config.spider_config.HISTORICAL) {
						FileManager.showFileSaveDialog(null, up.name + " - " + date + "全部视频历史弹幕", 3);
					} else {
						FileManager.showFileSaveDialog(null, up.name + " - " + date + "全部视频实时弹幕", 3);
					}
					if (OutputManager.getFile() == null) {
						FileManager.deleteTempDir();
						MainGui.getInstance().log("您未选择输出目录，爬取已终止");
						MainGui.getInstance().reset();
						MainGui.getInstance().setEnabled(true);
						return;
					}
					MainGui.getInstance().log("您已选择输出目录：" + OutputManager.getFile().getPath());
					new Reminding().setVisible(true);
					try {
						Thread.sleep(Long.MAX_VALUE);
					} catch (InterruptedException err) {
						// 继续执行操作
						if (!confirmed) {
							FileManager.deleteTempDir();
							MainGui.getInstance().log("您已取消爬取");
							OutputManager.setFile(null);
							MainGui.getInstance().setEnabled(true);
							MainGui.getInstance().reset();
							return;
						}
						MainGui.getInstance().log("视频弹幕爬取开始");
						MainGui.getInstance().setButtonText("正在爬取中...");
						String[] avs = null;
						try {
							avs = getAvsOfUp(up.uid);
						} catch (JSONException error) {
							MainGui.getInstance().log("【警告】无法获取UP主的视频列表：" + err.toString());
							MainGui.getInstance().reset();
							MainGui.getInstance().setEnabled(true);
							e.printStackTrace();
							return;
						}
						MainGui.getInstance().log("已获取UP主的所有视频，总数为 " + avs.length);
						confirmed = false;
						for (int i = 0; i < up.videos; i++) {
							JSONObject data = null;
							try {
								data = getVideoInfoJson(avs[i] + "");
							} catch (JSONException error) {
								MainGui.getInstance().log("【警告】在获取" + avs[i] + "的弹幕时发生错误，视频可能不存在");
								failures++;
								randomSleep();
								continue;
							}
							String title = null;
							try {
								title = getVideoTitle(data);
								pages = getVideoPages(data);
							} catch (JSONException error) {
								MainGui.getInstance().log("【警告】无法获取av" + avs[i] + "的标题或分P列表：" + error.toString());
								error.printStackTrace();
								failures++;
								randomSleep();
								continue;
							}
							for (int j = 0; j < pages.length; j++) {
								String chatStr = null;
								try {
									chatStr = getChatByCid(pages[j], data);
								} catch (ParseException e1) {
									MainGui.getInstance().log(
											"【警告】P" + (j + 1) + " （共" + pages.length + "P）弹幕爬取失败： " + e1.toString());
									e1.printStackTrace();
									failures++;
									randomSleep();
									continue;
								}
								if (pages.length == 1) {
									OutputManager.saveToXml(chatStr, OutputManager.getFile().getPath() + File.separator
											+ OutputManager.replaceFileName(up.name + " - " + title + ".xml"));
									MainGui.getInstance().log("第 " + (i + 1) + " / " + avs.length + " 个视频爬取完毕");
								} else {
									OutputManager.saveToXml(chatStr,
											OutputManager.getFile().getPath() + File.separator
													+ OutputManager.replaceFileName(up.name + " - " + title + " （P"
															+ (j + 1) + "：" + getPageName(data, j) + "）.xml"));
									MainGui.getInstance().log("第 " + (i + 1) + " / " + avs.length + " 个视频（P" + (j + 1)
											+ " / 共" + pages.length + "P）爬取完毕");
								}
								randomSleep();
							}
						}
						OutputManager.setFile(null);
						if (failures == 0) {
							MainGui.getInstance().log("爬取成功，一切顺利！");
						} else {
							MainGui.getInstance().log("爬取完毕，共" + failures + "处错误，您可以查看爬取日志");
						}
						MainGui.getInstance().reset();
						MainGui.getInstance().setEnabled(true);
					}
				}
			} catch (IOException e) {
				MainGui.getInstance().log("【警告】弹幕爬取失败： " + e.toString());
				MainGui.getInstance().reset();
				MainGui.getInstance().setEnabled(true);
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
				avs.add("AV" + video.getInt("aid"));
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
		conn.disconnect();
		return sb.toString();
	}

	private int getUpVideosCount(String uid) throws IOException, JSONException {
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
		String text;
		if (av.startsWith("AV")) {
			text = getDataFromServer("https://api.bilibili.com/x/web-interface/view?aid=" + av.replace("AV", ""));
		} else {
			text = getDataFromServer("https://api.bilibili.com/x/web-interface/view?bvid=" + av);
		}
		return new JSONObject(text).getJSONObject("data");
	}

	private String getVideoTitle(JSONObject json) throws JSONException {
		return json.getString("title");
	}

	private String getPageName(JSONObject json, int page) throws JSONException {
		return json.getJSONArray("pages").getJSONObject(page).getString("part");
	}

	private String getUpName(JSONObject json) throws JSONException {
		return json.getJSONObject("owner").getString("name");
	}

	private String[] getVideoPages(JSONObject json) throws IOException, JSONException {
		ArrayList<String> str = new ArrayList<>();
		JSONArray pages = json.getJSONArray("pages");
		for (int i = 0; i < pages.length(); i++) {
			str.add(pages.getJSONObject(i).getInt("cid") + "");
		}
		return str.toArray(new String[0]);
	}

	private void randomSleep() {
		try {
			Thread.sleep(1000 + (int) (Math.random() * 1000));
		} catch (InterruptedException e) {
		}
	}

	private Calendar getPubDate(JSONObject json) throws JSONException {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(json.getLong("pubdate") * 1000);
		return calendar;
	}

	/**
	 * 不建议使用这个方法获取日期列表，因为频繁请求服务器不返回数据。事实上不在列表中的日期也可以请求到弹幕。
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private String[] getDateList(String cid, Calendar pubDate) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
		String today = simpleDateFormat.format(new Date());
		ArrayList<String> list = new ArrayList<>();
		boolean retry = true;
		int delay = 100;
		while (true) {
			int code = -1000;
			try {
				CloseableHttpClient client = HttpClients.createDefault();
				HttpGet httpGet = new HttpGet("https://api.bilibili.com/x/v2/dm/history/index?type=1&oid=" + cid
						+ "&month=" + simpleDateFormat.format(new Date(pubDate.getTimeInMillis())));
				httpGet.addHeader("Host", "api.bilibili.com");
				httpGet.addHeader("Connection", "keep-alive");
				httpGet.addHeader("Accept", "application/json, text/javascript, */*; q=0.01");
				httpGet.addHeader("Sec-Fetch-Dest", "empty");
				httpGet.addHeader("User-Agent",
						"Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.132 Safari/537.36");
				httpGet.addHeader("Origin", "https://www.bilibili.com");
				httpGet.addHeader("Sec-Fetch-Site", "same-site");
				httpGet.addHeader("Sec-Fetch-Mode", "cors");
				httpGet.addHeader("Referer", "https://www.bilibili.com/video/");
				httpGet.addHeader("Accept-Encoding", "gzip, deflate, br");
				httpGet.addHeader("Accept-Language", "zh-CN,zh;q=0.9");
				httpGet.addHeader("Cookie", Config.spider_config.COOKIE);
				CloseableHttpResponse response = client.execute(httpGet);
				HttpEntity entity = response.getEntity();
				String string = EntityUtils.toString(entity);
				response.close();
				JSONObject jsonObject = new JSONObject(string);
				code = jsonObject.getInt("code");
				JSONArray array = jsonObject.getJSONArray("data");
				for (int i = 0; i < array.length(); i++) {
					list.add(array.getString(i));
				}
				retry = true;
			} catch (Exception e) {
				e.printStackTrace();
				if (code == 0) {
					// code为0表示请求成功，该月没有可爬取的日期，自动跳过
				} else if (code == -503 || code == -509) {
					if (delay < 10000) {
						delay += 100;
						MainGui.getInstance().log("（错误码" + code + "）请求过于频繁，已调整请求间隔");
					} else {
						MainGui.getInstance().log("（错误码" + code + "）请求过于频繁，将在10s后重试");
					}
					pubDate.add(Calendar.MONTH, -1);
				} else {
					if (retry) {
						MainGui.getInstance().log("（错误码" + code + "）"
								+ simpleDateFormat.format(new Date(pubDate.getTimeInMillis())) + "的可爬取日期列表请求失败，将重试一次");
						pubDate.add(Calendar.MONTH, -1);
						retry = false;
						try {
							Thread.sleep(1900);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					} else {
						retry = true;
						MainGui.getInstance().log("（错误码" + code + "）"
								+ simpleDateFormat.format(new Date(pubDate.getTimeInMillis())) + "的可爬取日期列表请求失败，已放弃");
					}
				}
			} finally {
				if (simpleDateFormat.format(new Date(pubDate.getTimeInMillis())).equals(today)) {
					break;
				}
				pubDate.add(Calendar.MONTH, 1);
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		return list.toArray(new String[0]);
	}

	/*
	 * 开发者诉苦： 哔哩哔哩弹幕xml是压缩过了的，浏览器访问xml文件会自动解压从而获得正确的文件；而跑程序则不会。
	 * TMD这个问题困扰我整整两天，敲代码到半夜，我甚至想过直接搞解压算法，直到最后一刻才想起用第三方库，成功爬取的时候（爷）真是老泪纵横23333
	 */
	private String getChatByCid(String cid, JSONObject json) throws ParseException, IOException {
		if (Config.spider_config.HISTORICAL) {
			Calendar calendar = getPubDate(json);
			ArrayList<String> arrayList = new ArrayList<>();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String today = simpleDateFormat.format(new Date());
			while (true) {
				String date = simpleDateFormat.format(new Date(calendar.getTimeInMillis()));
				arrayList.add(date);
				if (date.equals(today)) {
					break;
				}
				calendar.add(Calendar.DATE, 1);
			}
			String[] date_list = arrayList.toArray(new String[0]);
			MainGui.getInstance().log("已获取日期列表，可爬取的天数为" + date_list.length);
			if (date_list.length == 0) {
				MainGui.getInstance().log("【警告】无法请求到任何弹幕");
				return "<i><chatserver>chat.bilibili.com</chatserver><chatid>" + cid
						+ "</chatid><mission>0</mission><maxlimit>2147483647</maxlimit><state>0</state><real_name>0</real_name></i>";
			} else if (date_list.length == 1) {
				MainGui.getInstance().log("视频是今天发布的，已自动转为爬取实时弹幕");
				CloseableHttpClient client = HttpClients.createDefault();
				HttpGet httpGet = new HttpGet("https://comment.bilibili.com/" + cid + ".xml");
				CloseableHttpResponse response = client.execute(httpGet);
				HttpEntity entity = response.getEntity();
				String string = EntityUtils.toString(entity);
				response.close();
				HistoricalChat chat = new HistoricalChat();
				try {
					chat.append(string);
				} catch (NumberFormatException | InterruptedException e) {
					e.printStackTrace();
					MainGui.getInstance().log("【警告】实时弹幕获取失败");
					return "<i><chatserver>chat.bilibili.com</chatserver><chatid>" + cid
							+ "</chatid><mission>0</mission><maxlimit>2147483647</maxlimit><state>0</state><real_name>0</real_name></i>";
				}
				StringBuilder sb = new StringBuilder("<i><chatserver>chat.bilibili.com</chatserver><chatid>" + cid
						+ "</chatid><mission>0</mission><maxlimit>2147483647</maxlimit><state>0</state><real_name>0</real_name>");
				for (int i = 0; i < chat.getIds().length; i++) {
					sb.append(chat.getChats()[i]);
				}
				MainGui.getInstance().log("成功，历史弹幕总数为" + chat.getIds().length);
				sb.append("</i>");
				return sb.toString();
			}
			HistoricalChat[] historicalChats = new HistoricalChat[date_list.length];
			handle(cid, date_list, historicalChats, 0, date_list.length - 1);
			CloseableHttpClient client = HttpClients.createDefault();
			HttpGet httpGet = new HttpGet("https://comment.bilibili.com/" + cid + ".xml");
			CloseableHttpResponse response = client.execute(httpGet);
			HttpEntity entity = response.getEntity();
			String string = EntityUtils.toString(entity);
			response.close();
			HistoricalChat chat = new HistoricalChat();
			try {
				chat.append(string);
				MainGui.getInstance().log("实时弹幕请求成功");
			} catch (NumberFormatException | InterruptedException e) {
				e.printStackTrace();
				MainGui.getInstance().log("【警告】实时弹幕获取失败");
				chat = null;
			}
			MainGui.getInstance().log("历史弹幕爬取完毕，准备合并弹幕");
			StringBuilder sb = new StringBuilder("<i><chatserver>chat.bilibili.com</chatserver><chatid>" + cid
					+ "</chatid><mission>0</mission><maxlimit>2147483647</maxlimit><state>0</state><real_name>0</real_name>");
			ArrayList<Long> temp = new ArrayList<>();
			for (int i = 0; i < historicalChats.length; i++) {
				if (historicalChats[i] == null) {
					continue;
				}
				tick: for (int j = historicalChats[i].getIds().length - 1; j > -1; j--) {
					for (int k = 0; k < temp.size(); k++) {
						if (historicalChats[i].getIds()[j] == temp.get(k)) {
							continue tick;
						}
					}
					temp.add(historicalChats[i].getIds()[j]);
					sb.append(historicalChats[i].getChats()[j]);
				}
			}
			if (chat != null) {
				tick: for (int j = 0; j < chat.getIds().length; j++) {
					for (int k = 0; k < temp.size(); k++) {
						if (chat.getIds()[j] == temp.get(k)) {
							continue tick;
						}
					}
					temp.add(chat.getIds()[j]);
					sb.append(chat.getChats()[j]);
				}
			}
			MainGui.getInstance().log("成功，历史弹幕总数为" + temp.size());
			sb.append("</i>");
			return sb.toString();
		} else {
			CloseableHttpClient client = HttpClients.createDefault();
			HttpGet httpGet = new HttpGet("https://comment.bilibili.com/" + cid + ".xml");
			CloseableHttpResponse response = client.execute(httpGet);
			HttpEntity entity = response.getEntity();
			String string = EntityUtils.toString(entity);
			response.close();
			return string;
		}
	}

	private void handle(String cid, String[] date, HistoricalChat[] historicalChats, int min_index, int max_index) {
		if (historicalChats[min_index] == null) {
			HistoricalChat chat = new HistoricalChat();
			MainGui.getInstance().log("正在请求" + date[min_index] + "的历史弹幕");
			try {
				chat.append(requestWithCookie(cid, date[min_index]));
			} catch (Exception e) {
				e.printStackTrace();
				MainGui.getInstance().log("【警告】" + date[min_index] + "的历史弹幕请求失败，将在10秒后重试一次");
				try {
					Thread.sleep(10000);
					chat.append(requestWithCookie(cid, date[min_index]));
				} catch (Exception e1) {
					e1.printStackTrace();
					MainGui.getInstance().log("【警告】" + date[min_index] + "的历史弹幕请求失败，已放弃");
					return;
				}
			}
			historicalChats[min_index] = chat;
		}
		if (historicalChats[max_index] == null) {
			HistoricalChat chat = new HistoricalChat();
			MainGui.getInstance().log("正在请求" + date[max_index] + "的历史弹幕");
			try {
				chat.append(requestWithCookie(cid, date[max_index]));
			} catch (Exception e) {
				e.printStackTrace();
				MainGui.getInstance().log("【警告】" + date[max_index] + "的历史弹幕请求失败，将在10秒后重试一次");
				try {
					Thread.sleep(10000);
					chat.append(requestWithCookie(cid, date[max_index]));
				} catch (Exception e1) {
					e1.printStackTrace();
					MainGui.getInstance().log("【警告】" + date[max_index] + "的历史弹幕请求失败，已放弃");
					return;
				}
			}
			historicalChats[max_index] = chat;
		}
		if (historicalChats[max_index].getIds().length == 0) {
			return;
		}
		if (historicalChats[min_index].isLowerThan(historicalChats[max_index])) {
			if (max_index - min_index > 1) {
				handle(cid, date, historicalChats, (min_index + max_index) / 2, max_index);
				handle(cid, date, historicalChats, min_index, (min_index + max_index) / 2);
			}
		}
	}

	private String requestWithCookie(String cid, String date) throws IOException, ParseException {
		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet("https://api.bilibili.com/x/v2/dm/history?type=1&oid=" + cid + "&date=" + date);
		httpGet.addHeader("Host", "api.bilibili.com");
		httpGet.addHeader("Connection", "keep-alive");
		httpGet.addHeader("Accept", "application/json, text/javascript, */*; q=0.01");
		httpGet.addHeader("Sec-Fetch-Dest", "empty");
		httpGet.addHeader("User-Agent",
				"Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.132 Safari/537.36");
		httpGet.addHeader("Origin", "https://www.bilibili.com");
		httpGet.addHeader("Sec-Fetch-Site", "same-site");
		httpGet.addHeader("Sec-Fetch-Mode", "cors");
		httpGet.addHeader("Referer", "https://www.bilibili.com/video/");
		httpGet.addHeader("Accept-Encoding", "gzip, deflate, br");
		httpGet.addHeader("Accept-Language", "zh-CN,zh;q=0.9");
		httpGet.addHeader("Cookie", Config.spider_config.COOKIE);
		CloseableHttpResponse response = client.execute(httpGet);
		HttpEntity entity = response.getEntity();
		String string = EntityUtils.toString(entity);
		response.close();
		return string;
	}

	public static class Up {
		public String uid;// uid
		public String name;// UP主的名字
		public int level;// 等级
		public String sex;// 性别
		public URL face_url;// 头像的url
		public String sign;// 介绍
		public int role;// 认证信息，0为无认证，1或2为黄色闪电，3及以上为蓝色闪电
		public int videos;// UP主的视频数
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
