package gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import script.Config;
import script.LiveChat;
import script.OutputManager;
import javax.swing.JCheckBox;

/**
 * ֱ����Ļ��ȡ�Ŀؼ���
 */
public class LivePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private JTextField field_delay;
	private JTextField field_room;
	private JLabel label_times;
	private JLabel label_total;
	private JButton button_choose;
	private JButton button_status;
	private JCheckBox check;
	private int times = 0;
	private int count = 0;
	private int failure = 0;
	private JScrollPane scrollPane_live;
	private JList<String> list;
	private static LivePanel instance;
	private ArrayList<String> logs;
	private Thread thread;

	/**
	 * �����ؼ�
	 */
	public LivePanel() {
		LivePanel.instance = this;
		logs = new ArrayList<>();
		list = new JList<>();
		list.setListData(logs.toArray(new String[0]));
		setLayout(new BorderLayout(0, 0));
		scrollPane_live = new JScrollPane(list);
		scrollPane_live.setMaximumSize(getSize());
		scrollPane_live.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane_live.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(scrollPane_live, BorderLayout.CENTER);
		JPanel panel_right = new JPanel();
		add(panel_right, BorderLayout.EAST);
		panel_right.setLayout(new GridLayout(0, 1, 0, 0));

		JPanel panel_room = new JPanel();
		panel_right.add(panel_room);

		JButton button = new JButton("�����־");
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				logs.clear();
				list.setListData(logs.toArray(new String[0]));
			}
		});
		panel_room.add(button);

		JLabel label_room = new JLabel("ֱ����");
		panel_room.add(label_room);

		field_room = new JTextField();
		panel_room.add(field_room);
		field_room.setColumns(10);

		label_times = new JLabel("��ȡ�ɹ��Ĵ�����δ��ʼ");
		panel_right.add(label_times);

		label_total = new JLabel("����ȡ��Ļ����δ��ʼ");
		panel_right.add(label_total);

		check = new JCheckBox("���ܵ�����ȡ���");
		check.setSelected(Config.live_config.AUTO_DELAY);
		check.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				Config.live_config.AUTO_DELAY = check.isSelected();
			}
		});
		panel_right.add(check);

		JPanel panel_delay = new JPanel();
		panel_right.add(panel_delay);

		JLabel label_delay = new JLabel("��ȡ�����ms��");
		panel_delay.add(label_delay);

		field_delay = new JTextField();
		field_delay.setText(Config.live_config.DELAY + "");
		panel_delay.add(field_delay);
		field_delay.setColumns(10);

		JButton button_delay = new JButton("ע��");
		button_delay.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Config.live_config.DELAY = Integer.parseInt(field_delay.getText());
			}
		});
		panel_delay.add(button_delay);

		JPanel panel_control = new JPanel();
		panel_right.add(panel_control);

		button_choose = new JButton("ѡ�����Ŀ¼");
		button_status = new JButton("��ʼ��ȡ");
		button_choose.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!Config.live_config.STATUS) {
					FileManager.showFileSaveDialog(instance, "ֱ����Ļ", 1);
					if (OutputManager.getFile() != null) {
						log("###  ��ѡ�����Ŀ¼  ###");
						refreshUi();
					}
				} else {
					button_choose.setText("�ȴ�ץȡ����");
					button_choose.setEnabled(false);
					button_status.setEnabled(false);
					Config.live_config.STATUS = false;
					thread.interrupt();
					button_choose.setText("�������");
					OutputManager.saveToJson(LiveChat.getJSONArray());
					new Dialog("����ɹ�", "�������json�ļ���").setVisible(true);
					;
					reset();
					button_choose.setEnabled(true);
					button_status.setEnabled(true);
					button_choose.setText("ѡ�����Ŀ¼");
					button_status.setText("��ʼ��ȡ");
					label_times.setText("��ȡ�ɹ��Ĵ�����δ��ʼ");
					label_total.setText("����ȡ��Ļ����δ��ʼ");
					Config.ALLOW_MODIFY = false;
					setEnabled(true);
					MainGui.getInstance().setEnabled(true);
					field_room.setEnabled(true);
					Config.ALLOW_MODIFY = true;
					OutputManager.setFile(null);
				}
			}
		});
		panel_control.add(button_choose);

		button_status.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!Config.live_config.STATUS) {
					if (OutputManager.getFile() == null) {
						new Dialog("���ò�����", "����ѡ�����Ŀ¼��").setVisible(true);
						return;
					}
					if (field_room.getText().isEmpty()) {
						new Dialog("���ò�����", "������ֱ���䡣").setVisible(true);
						return;
					}
					try {
						Config.live_config.ROOM = Integer.parseInt(field_room.getText());
					} catch (NumberFormatException err) {
						new Dialog("ֱ�������ô���", "�޷���ȡ����š�����ֱ���䷿��š�").setVisible(true);
						return;
					}
					if (Config.live_config.ROOM <= 0) {
						new Dialog("ֱ�������ô���", "ֱ���䷿��ű����Ǵ���0��������").setVisible(true);
						return;
					}
					Config.live_config.START_TIME = new Date().getTime();
					Config.live_config.STATUS = true;
					setEnabled(false);
					Config.ALLOW_MODIFY = false;
					MainGui.getInstance().setEnabled(false);
					Config.ALLOW_MODIFY = true;
					button_choose.setText("���������Ϊjson");
					button_status.setText("������ģ�����xml");
					thread = new Thread(new LiveChat(OutputManager.getFile()));
					thread.start();
				} else {
					button_status.setText("�ȴ�ץȡ����");
					button_choose.setEnabled(false);
					button_status.setEnabled(false);
					Config.live_config.STATUS = false;
					thread.interrupt();
					button_status.setText("�������");
					OutputManager.saveToXml(LiveChat.getChat());
					new Dialog("����ɹ�", "�������xml�ļ���\n��ע�⡿������xml�ļ�����ChatStatͳ��ʹ�ã���������������������xml��Ļ�ļ���").setVisible(true);
					;
					reset();
					button_choose.setEnabled(true);
					button_status.setEnabled(true);
					button_choose.setText("ѡ�����Ŀ¼");
					button_status.setText("��ʼ��ȡ");
					label_times.setText("��ȡ�ɹ��Ĵ�����δ��ʼ");
					label_total.setText("����ȡ��Ļ����δ��ʼ");
					Config.ALLOW_MODIFY = false;
					setEnabled(true);
					MainGui.getInstance().setEnabled(true);
					field_room.setEnabled(true);
					Config.ALLOW_MODIFY = true;
					OutputManager.setFile(null);
				}
			}
		});
		panel_control.add(button_status);

	}

	/**
	 * ��ȡʾ��
	 * 
	 * @return ʾ��
	 */
	public static LivePanel getInstance() {
		return instance;
	}

	/**
	 * �����־
	 * 
	 * @param log ��־����
	 */
	public void log(String log) {
		logs.add(0, log);
	}

	/**
	 * ������־չʾ��UI
	 */
	public void refreshUi() {
		list.setListData(logs.toArray(new String[0]));
	}

	/**
	 * ���һ����ȡ�ɹ��Ĵ���
	 */
	public void addTime() {
		times++;
		label_times.setText("��ȡ�ɹ��Ĵ�����" + times + "     �㻺�������" + failure);
	}

	/**
	 * ˢ��״̬չʾ��
	 * 
	 * @param new_chat_count �µ�Ļ��������������һ����ȡ�Ľ�����ظ��ĵ�Ļ����
	 * @param buffer         ������
	 * @param first_run      �Ƿ��ǵ�һ����ȡ
	 */
	public void refreshLabel(int new_chat_count, int buffer, boolean first_run) {
		count += new_chat_count;
		if (first_run) {
			label_total.setText("����ȡ��Ļ����" + count + "     [�����������]");
		} else if (buffer == 0) {
			failure++;
			label_total.setText("����ȡ��Ļ����" + count + "     ������ - ��������" + buffer + "��");
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("����ȡ��Ļ����" + count + "     ��������" + buffer + " ");
			for (int i = 0; i < buffer; i++) {
				sb.append("+");
			}
			label_total.setText(sb.toString());
		}
	}

	/**
	 * ���ÿؼ��Ƿ����
	 * 
	 * @param b �Ƿ����
	 */
	@Override
	public void setEnabled(boolean b) {
		super.setEnabled(b);
		if (!Config.ALLOW_MODIFY) {
			return;
		}
		field_room.setEnabled(b);
	}

	/**
	 * ����״̬չʾ��
	 */
	public void reset() {
		times = 0;
		count = 0;
		failure = 0;
	}

	/**
	 * ������ȡ��ʱ
	 */
	public void refreshDelayField() {
		field_delay.setText(Config.live_config.DELAY + "");
	}

}
