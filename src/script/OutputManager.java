package script;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;

import gui.Dialog;
import gui.MainGui;

/**
 * �ļ����������
 */
public class OutputManager {
	private static File file;

	/**
	 * ��ȡ���úõ��ļ�
	 * 
	 * @return �ļ�
	 */
	public static File getFile() {
		return file;
	}

	/**
	 * �����ļ�
	 * 
	 * @param file �ļ�
	 */
	public static void setFile(File file) {
		OutputManager.file = file;
	}

	/**
	 * ���浽csv�ļ�
	 * 
	 * @param titles   ��ͷ����Ϊnull
	 * @param lists    �����б�
	 * @param autoOpen �Ƿ��Զ��򿪱�����ļ�
	 * @throws InterruptedException
	 */
	public static void saveToCsv(String[] titles, ArrayList<?>[] lists, boolean autoOpen) throws InterruptedException {
		FileOutputStream out = null;
		OutputStreamWriter osw = null;
		BufferedWriter bw = null;
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				MainGui.getInstance().notifyXmlHandlingException(e);
				throw new InterruptedException();
			}
		}
		try {
			out = new FileOutputStream(file);
			osw = new OutputStreamWriter(out);
			bw = new BufferedWriter(osw);
			if (titles != null) {
				for (int i = 0; i < titles.length - 1; i++) {
					bw.append(titles[i] + ",");
				}
				bw.append(titles[titles.length - 1]).append("\n");
			}
			for (int i = 0; i < lists[0].size(); i++) {
				if (autoOpen) {
					MainGui.getInstance().refreshProgressBar(i);
				}
				for (int j = 0; j < lists.length - 1; j++) {
					if (!(lists[j].get(0) instanceof String)) {
						bw.append(lists[j].get(i).toString() + ",");
					} else if (lists[j].get(i).toString().contains(",") || lists[j].get(i).toString().contains(",")) {
						String string = lists[j].get(i).toString();
						string = string.replace("\"", "\"\"");
						bw.append("\"" + string + "\",");
					} else {
						bw.append(lists[j].get(i) + ",");
					}
				}
				if (!(lists[lists.length - 1].get(0) instanceof String)) {
					bw.append(lists[lists.length - 1].get(i).toString() + "\n");
				} else if (lists[lists.length - 1].get(i).toString().contains(",")
						|| lists[lists.length - 1].get(i).toString().contains(",")) {
					String string = lists[lists.length - 1].get(i).toString();
					string = string.replace("\"", "\"\"");
					bw.append("\"" + string + "\"\n");
				} else {
					bw.append(lists[lists.length - 1].get(i) + "\n");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			MainGui.getInstance().notifyXmlHandlingException(e);
			throw new InterruptedException();
		} finally {
			if (bw != null) {
				try {
					bw.close();
					bw = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (osw != null) {
				try {
					osw.close();
					osw = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (out != null) {
				try {
					out.close();
					out = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if (autoOpen) {
			try {
				Desktop.getDesktop().open(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * ���浽json�ļ�
	 * 
	 * @param jsonArray Ҫ�����json����
	 */
	public static boolean saveToJson(JSONArray jsonArray) {
		try {
			file = new File(OutputManager.getFile().getPath() + ".json");
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
			Writer writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
			writer.write(jsonArray.toString());
			writer.flush();
			writer.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			new Dialog("���ʧ��", "�ܱ�Ǹ�����Ϊjsonʧ�ܡ�\n\n" + e.getClass().getName() + "\n" + e.getMessage()).setVisible(true);
			return false;
		}
	}

	/**
	 * ���浽xml�ļ�
	 * 
	 * @param chat  ��Ļʵ�������
	 * @param color ��ɫ����
	 */
	public static boolean saveToXml(Chat chat, int[] color) {
		return saveToXml(chat, color, file.getPath() + ".xml");
	}

	/**
	 * ���浽xml�ļ�
	 * 
	 * @param chat     ��Ļʵ�������
	 * @param color    ��ɫ����
	 * @param filePath ָ���ļ�·��
	 */
	public static boolean saveToXml(Chat chat, int[] color, String filePath) {
		try {
			file = new File(filePath);
			file.mkdirs();
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
			DecimalFormat df = new DecimalFormat("#.00000");
			Writer writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
			writer.write(
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?><i><chatserver>chat.bilibili.com</chatserver><chatid>0</chatid><mission>0</mission><maxlimit>2147483647</maxlimit><state>0</state><real_name>0</real_name><source>n-a</source>");
			for (int i = 0; i < chat.getCount(); i++) {
				writer.write("<d p=\"");
				if (df.format(chat.getTime().get(i)).toString().charAt(0) == '.') {
					writer.write("0");
				}
				writer.write(df.format(chat.getTime().get(i)));
				writer.write(",1,25,");
				writer.write(color[i] + "");
				writer.write(",");
				writer.write(chat.getDate().get(i).toString());
				writer.write(",0,");
				writer.write(chat.getUsers().get(i));
				writer.write(",0\">");
				writer.write(chat.getChats().get(i));
				writer.write("</d>");
			}
			writer.write("</i>");
			writer.flush();
			writer.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			new Dialog("���ʧ��", "�ܱ�Ǹ�����Ϊxmlʧ�ܡ�\n\n" + e.getClass().getName() + "\n" + e.getMessage()).setVisible(true);
			return false;
		}
	}

	/**
	 * ���浽xml�ļ�
	 * 
	 * @param chatStr �ַ�����ʽ�ĵ�Ļ
	 */
	public static boolean saveToXml(String chatStr) {
		return saveToXml(chatStr, file.getPath());
	}

	/**
	 * ���浽xml�ļ�
	 * 
	 * @param chatStr  �ַ�����ʽ�ĵ�Ļ
	 * @param filePath �ļ�·��
	 */
	public static boolean saveToXml(String chatStr, String filePath) {
		File file = new File(filePath);
		try {
			file.mkdirs();
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(filePath);
			fos.write(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
			BufferedWriter bw = new BufferedWriter(osw);
			bw.write(chatStr);
			bw.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			new Dialog("���ʧ��", "�ܱ�Ǹ�����Ϊxmlʧ�ܡ�\n\n" + e.getClass().getName() + "\n" + e.getMessage()).setVisible(true);
			return false;
		}
	}

	/**
	 * ���浽xml�ļ�(��������)
	 * 
	 * @param data      ��Ļ����
	 * @param fileNames �ļ�·������
	 */
	@Deprecated
	public static void saveToXmls(String[] data, String[] fileNames) {
		file.mkdirs();
		for (int i = 0; i < data.length; i++) {
			try {
				File subFile = new File(file.getPath() + File.separator + fileNames[i]);
				FileOutputStream fos = new FileOutputStream(subFile);
				fos.write(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
				FileWriter fileWriter = new FileWriter(subFile.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fileWriter);
				bw.write(data[i]);
				fos.close();
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
				new Dialog("���ʧ��", "�ܱ�Ǹ�����Ϊxmlʧ�ܡ�\n\n" + e.getClass().getName() + "\n" + e.getMessage())
						.setVisible(true);
			}
		}
	}

	/**
	 * �滻�Ƿ����ļ��ַ�
	 * 
	 * @param fileName �ļ���
	 * @return �Ϸ����ļ���
	 */
	public static String replaceFileName(String fileName) {
		Pattern pattern = Pattern.compile("[\\\\/:\\*\\?\\\"<>\\|]");
		Matcher matcher = pattern.matcher(fileName);
		return matcher.replaceAll("");
	}
}
