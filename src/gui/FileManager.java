package gui;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.filechooser.FileNameExtensionFilter;

import script.OutputManager;

/**
 * ���ڿ����ļ�ѡ��������
 */
public class FileManager {
	ListModel<String> listModel;
	ArrayList<String> arrayList;
	private static File defaultDir;
	private static boolean dirExists = true;// ����ѡ���ļ��У�ָʾ�ļ���ԭ���Ƿ���ڣ����ԭ�������ڣ���ȡ��ѡ�����ɾ���ļ���

	/**
	 * չʾ���ļ��б�ĶԻ���
	 * 
	 * @param parent ���ؼ�
	 * @param list   JList����
	 */
	public void showFileOpenDialog(Component parent, JList<String> list) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File("."));
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setMultiSelectionEnabled(true);
		fileChooser.setFileFilter(new FileNameExtensionFilter("xml������������Ļ�ļ���", "xml"));
		int result = fileChooser.showOpenDialog(parent);
		listModel = list.getModel();
		arrayList = new ArrayList<>();
		for (int i = 0; i < listModel.getSize(); i++) {
			arrayList.add(listModel.getElementAt(i));
		}
		if (result == JFileChooser.APPROVE_OPTION) {
			File[] files = fileChooser.getSelectedFiles();
			tick: for (int i = 0; i < files.length; i++) {
				String name = files[i].getName();
				String[] strArray = name.split("\\.");
				int suffixIndex = strArray.length - 1;// ��׺��
				if (!strArray[suffixIndex].matches("[Xx][Mm][Ll]")) {
					new Dialog("����xml�ļ�����", files[i].getPath() + " ����xml�ļ���\n����ֹ���ε��룬��ѡ��xml��ʽ����������Ļ�ļ���")
							.setVisible(true);
					return;
				}
				// �ж���ӵ��ļ��Ƿ������б��д���
				for (int j = 0; j < listModel.getSize(); j++) {
					if (files[i].getPath().equals(listModel.getElementAt(j))) {
						continue tick;
					}
				}
				// ������ļ�
				arrayList.add(files[i].getPath());
			}
			list.setListData(arrayList.toArray(new String[0]));
		}
	}

	/**
	 * ���ļ�ѡ���������ڶ�ȡtxt�ļ�
	 * 
	 * @param parent ���ؼ�
	 */
	public void showFileOpenDialogForTxt(Component parent) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File("."));
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setFileFilter(new FileNameExtensionFilter(".txt", "txt"));
		int result = fileChooser.showOpenDialog(parent);
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			MainGui.getInstance().setTxtFile(file);
			MainGui.getInstance().log("��ѡ��txt�ļ��� " + file.getPath());
		}
	}

	/**
	 * չʾ����ֱ����Ļ�ĶԻ���
	 * 
	 * @param parent   ���ؼ�
	 * @param fileName �ļ�������������׺��
	 * @param mode     ģʽ��0�����Ϊcsv�ļ���1��ֱ����Ļ�����2������Ƶ��Ļ�������P����3�������Ŀ¼
	 */
	public static void showFileSaveDialog(Component parent, String fileName, int mode) {
		JFileChooser fileChooser = new JFileChooser();
		if (mode == 0) {
			fileChooser.setSelectedFile(new File(replaceFileName(fileName) + ".csv"));
			fileChooser.setFileFilter(new FileNameExtensionFilter("*.csv", "csv"));
		} else if (mode == 1) {
			fileChooser.setSelectedFile(new File(replaceFileName(fileName)));
			fileChooser.setFileFilter(new FileNameExtensionFilter("*.*�����ʱ�Զ������׺����", ".*"));
		} else if (mode == 2) {
			fileChooser.setSelectedFile(new File(replaceFileName(fileName) + ".xml"));
			fileChooser.setFileFilter(new FileNameExtensionFilter("*.xml", "xml"));
		} else if (mode == 3) {
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			File dir = new File(fileChooser.getCurrentDirectory() + "\\" + replaceFileName(fileName) + "\\");
			defaultDir = dir;
			dirExists = dir.exists();
			dir.mkdir();
			fileChooser.setCurrentDirectory(dir);
		}
		int result = fileChooser.showSaveDialog(parent);

		if (result == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			OutputManager.setFile(file);
			// ɾ����ʱ�������ļ���
			if (OutputManager.getFile() == null) {
				if (!dirExists) {
					deleteTempDir();
				}
			}
		}
	}

	/**
	 * �滻���Ϸ����ַ�
	 * 
	 * @param fileName �ļ���
	 * @return �Ϸ����ļ���
	 */
	public static String replaceFileName(String fileName) {
		Pattern pattern = Pattern.compile("[\\\\/:\\*\\?\\\"<>\\|]");
		Matcher matcher = pattern.matcher(fileName);
		return matcher.replaceAll("");
	}

	/**
	 * ɾ����ʱ�������ļ���
	 */
	public static void deleteTempDir() {
		if (defaultDir != null && !dirExists) {
			defaultDir.delete();
			dirExists = true;
			defaultDir = null;
		}
	}

}
