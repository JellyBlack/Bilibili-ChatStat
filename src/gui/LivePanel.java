package gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import script.Config;
import script.LiveChat;
import script.LiveRoomStatus;
import script.OutputManager;

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
	private JButton button_delay;
	private JCheckBox check;
	private int times = 0;
	private int count = 0;
	private int failure = 0;
	private JScrollPane scrollPane_live;
	private JList<String> list;
	private static LivePanel instance;
	private ArrayList<String> logs;
	private Thread thread;
	private boolean long_clicked = false;
	private JCheckBox checkbox;
	private Thread auto = null;
	private boolean auto_stopped = false;
	private JPanel panel;
	private JCheckBox prevent;

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
				if (long_clicked) {
					return;
				}
				logs.clear();
				list.setListData(logs.toArray(new String[0]));

			}
		});
		button.addMouseListener(new MouseAdapter() {
			boolean thread_started = false;
			Thread thread2;
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						return;
					}
					long_clicked = true;
					log("��ǿ��ˢ����־��ʾ��");
					refreshUi();
				}
			};

			@Override
			public void mousePressed(MouseEvent e) {
				if (!thread_started) {
					thread2 = new Thread(runnable);
					thread2.start();
					thread_started = true;
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				thread2.interrupt();
				try {
					thread2.join();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				thread_started = false;
				long_clicked = false;
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

		panel = new JPanel();
		panel_right.add(panel);

		check = new JCheckBox("���ܵ�����ȡ���");
		panel.add(check);
		check.setSelected(Config.live_config.AUTO_DELAY);

		prevent = new JCheckBox("���ֺ��������ؼ���");
		prevent.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (prevent.isSelected()) {
					check.setEnabled(false);
					field_delay.setEditable(false);
					button_delay.setEnabled(false);
					button_choose.setEnabled(false);
					button_status.setEnabled(false);
					checkbox.setEnabled(false);
				} else {
					check.setEnabled(true);
					field_delay.setEditable(true);
					button_delay.setEnabled(true);
					button_choose.setEnabled(true);
					button_status.setEnabled(true);
					checkbox.setEnabled(true);
				}
			}
		});
		prevent.setEnabled(false);
		panel.add(prevent);
		check.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				Config.live_config.AUTO_DELAY = check.isSelected();
			}
		});

		JPanel panel_delay = new JPanel();
		panel_right.add(panel_delay);

		JLabel label_delay = new JLabel("��ȡ�����ms��");
		panel_delay.add(label_delay);

		field_delay = new JTextField();
		field_delay.setText(Config.live_config.DELAY + "");
		panel_delay.add(field_delay);
		field_delay.setColumns(10);

		button_delay = new JButton("ע��");
		button_delay.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (long_clicked) {
					return;
				}
				try {
					int delay = Integer.parseInt(field_delay.getText());
					if (delay < 0) {
						log("�����桿�������ʧ�ܣ���������");
						return;
					}
					if (Config.live_config.DELAY != delay) {
						Config.live_config.DELAY = delay;
						log("���������Ϊ" + delay + "ms");
					}
					if (thread != null) {
						thread.interrupt();
					}
				} catch (NumberFormatException err) {
					log("�����桿�������ʧ�ܣ���������");
				} finally {
					field_delay.setText(Config.live_config.DELAY + "");
					refreshUi();
				}
			}
		});
		button_delay.addMouseListener(new MouseAdapter() {
			boolean auto_delay = false;
			int delay = 0;
			boolean thread_started = false;
			boolean successful = false;
			Thread thread2;
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						return;
					}
					long_clicked = true;
					if (!Config.live_config.STATUS) {
						log("��δ������ȡ�����ܿ�ʼ��ʱ����ģʽ");
						refreshUi();
						return;
					}
					successful = true;
					log("###  ��ʱ����ģʽ������  ###");
					refreshUi();
					field_delay.setText("0");
					Config.live_config.DELAY = 0;
					Config.live_config.AUTO_DELAY = false;
					thread.interrupt();
				}
			};

			@Override
			public void mousePressed(MouseEvent e) {
				if (!thread_started) {
					thread2 = new Thread(runnable);
					thread2.start();
					auto_delay = Config.live_config.AUTO_DELAY;
					delay = Config.live_config.DELAY;
					thread_started = true;
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				thread2.interrupt();
				try {
					thread2.join();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				thread_started = false;
				long_clicked = false;
				if (!successful) {
					return;
				}
				successful = false;
				Config.live_config.DELAY = delay;
				Config.live_config.AUTO_DELAY = auto_delay;
				field_delay.setText(delay + "");
				log("###  ��ʱ����ģʽ��ֹͣ  ###");
				refreshUi();
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
					if (Config.live_config.AUTO_STOP) {
						Config.live_config.AUTO_STOP = false;
						auto.interrupt();
						try {
							auto.join();
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
						log("###  �����ֶ�ֹͣ����  ###");
						refreshUi();
					}
					Config.live_config.STATUS = false;
					if (!auto_stopped) {
						button_status.setText("�ȴ�ץȡ����");
						button_choose.setEnabled(false);
						button_status.setEnabled(false);
						thread.interrupt();
						try {
							thread.join();
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
					Config.live_config.STATUS = false;
					auto_stopped = false;
					button_choose.setText("�������");
					OutputManager.saveToJson(LiveChat.getJSONArray());
					new Dialog("����ɹ�", "�������json�ļ���").setVisible(true);
					;
					thread = null;
					reset();
					checkbox.setText("����������ʱ�Զ���������");
					checkbox.setEnabled(true);
					checkbox.setSelected(false);
					button_choose.setEnabled(true);
					button_status.setEnabled(true);
					prevent.setEnabled(false);
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
					log("###  ����ļ����  ###");
					refreshUi();
				}
			}
		});
		panel_control.add(button_choose);

		button_status.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!Config.live_config.STATUS) {
					if (!Config.live_config.AUTO_START) {
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
					}
					boolean b = false;
					if (Config.live_config.AUTO_START) {
						button_choose.setEnabled(true);
						Config.live_config.AUTO_START = false;
						auto.interrupt();
						try {
							auto.join();
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
						log("###  �����ֶ���������  ###");
						b = true;
						refreshUi();
					}
					prevent.setEnabled(true);
					Config.live_config.START_TIME = new Date().getTime();
					Config.live_config.STATUS = true;
					checkbox.setText("�������ز�ʱ�Զ�ֹͣ����");
					checkbox.setSelected(false);
					setEnabled(false);
					Config.ALLOW_MODIFY = false;
					MainGui.getInstance().setEnabled(false);
					Config.ALLOW_MODIFY = true;
					button_choose.setText("���������Ϊjson");
					button_status.setText("������ģ�����xml");
					thread = new Thread(new LiveChat(OutputManager.getFile(), b));
					thread.start();
				} else {
					if (Config.live_config.AUTO_STOP) {
						Config.live_config.AUTO_STOP = false;
						auto.interrupt();
						try {
							auto.join();
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
						log("###  �����ֶ�ֹͣ����  ###");
						refreshUi();
					}
					Config.live_config.STATUS = false;
					if (!auto_stopped) {
						button_status.setText("�ȴ�ץȡ����");
						button_choose.setEnabled(false);
						button_status.setEnabled(false);
						thread.interrupt();
						try {
							thread.join();
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
					Config.live_config.STATUS = false;
					auto_stopped = false;
					button_status.setText("�������");
					OutputManager.saveToXml(LiveChat.getChat(), LiveChat.getChatColor());
					new Dialog("����ɹ�",
							"�������xml�ļ���\n������xml�ļ��ɹ�ChatStatͳ��ʹ�ã���������������������xml��Ļ�ļ���\n1.2.0�汾֮��������ʹ�õ�����xml��Ļת��Ļ����ת�������xml�ļ������ұ�֤��Ļ��ɫ��ʾ������")
									.setVisible(true);
					;
					thread = null;
					reset();
					checkbox.setText("����������ʱ�Զ���������");
					checkbox.setEnabled(true);
					checkbox.setSelected(false);
					button_choose.setEnabled(true);
					button_status.setEnabled(true);
					prevent.setEnabled(false);
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
					log("###  ����ļ����  ###");
					refreshUi();
				}
			}
		});
		panel_control.add(button_status);

		checkbox = new JCheckBox("����������ʱ�Զ���������");
		checkbox.setSelected(false);
		checkbox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!Config.live_config.STATUS && !Config.live_config.AUTO_START) {
					int result = JOptionPane.showConfirmDialog(null,
							"�ù��ܿ��԰����㾫׼�ؿ������濪ʼ���е�ʱ�䡣\n��ѡ��ѡ��󣬳��򽫲���ϵ������������ֱ����״̬��һ�������������������档\n��ѡ����Ҳ�����ֶ��������档\nΪ���������������������ݣ������ڿ���ǰһ������ʱ������ѡ�\n������ĻԾ������־�����Ҵ��������������Ψ�ҷ�Ŷ����������棩\n��ȷ��Ҫ������",
							"����������ʱ�Զ���������", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if (result == JOptionPane.YES_OPTION) {
						if (OutputManager.getFile() == null) {
							new Dialog("���ò�����", "����ѡ�����Ŀ¼��").setVisible(true);
							checkbox.setSelected(false);
							return;
						}
						if (field_room.getText().isEmpty()) {
							new Dialog("���ò�����", "������ֱ���䡣").setVisible(true);
							checkbox.setSelected(false);
							return;
						}
						try {
							Config.live_config.ROOM = Integer.parseInt(field_room.getText());
						} catch (NumberFormatException err) {
							new Dialog("ֱ�������ô���", "�޷���ȡ����š�����ֱ���䷿��š�").setVisible(true);
							checkbox.setSelected(false);
							return;
						}
						if (Config.live_config.ROOM <= 0) {
							new Dialog("ֱ�������ô���", "ֱ���䷿��ű����Ǵ���0��������").setVisible(true);
							checkbox.setSelected(false);
							return;
						}
						setEnabled(false);
						button_choose.setEnabled(false);
						Config.ALLOW_MODIFY = false;
						MainGui.getInstance().setEnabled(false);
						Config.ALLOW_MODIFY = true;
						Config.live_config.AUTO_START = true;
						auto = new Thread(new LiveRoomStatus(1));
						auto.start();
					} else {
						checkbox.setSelected(false);
					}
				} else if (!Config.live_config.STATUS && Config.live_config.AUTO_START) {
					if (auto != null) {
						auto.interrupt();
						try {
							auto.join();
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
						log("###  ��ֹͣ�Զ�����  ###");
						refreshUi();
						setEnabled(true);
						button_choose.setEnabled(true);
						Config.ALLOW_MODIFY = false;
						MainGui.getInstance().setEnabled(true);
						Config.ALLOW_MODIFY = true;
						Config.live_config.AUTO_START = false;
					}
				} else if (Config.live_config.STATUS && !Config.live_config.AUTO_STOP) {
					int result = JOptionPane.showConfirmDialog(null,
							"�ù��ܿ��԰����㾫׼�ؿ�������ֹͣ���е�ʱ�䡣\n��ѡ��ѡ��󣬳��򽫲���ϵ������������ֱ����״̬��һ��ͣ��������ֹͣ���档\nֹͣ�󲻻��Զ�������ļ������ֶ�������ֹͣ���ܼ�����ȡ��\n��ѡ����Ҳ�����ֶ�ֹͣ���档\n�����������ȡ��ֱ���������ڹز����ֲ�״̬����ô��ѡ����������ֹͣ��\nΪ���������������������ݣ������ڹز�ǰһ������ʱ������ѡ�\n�����������ʱ�²�����Ҫ��ѡ��ѡ��������潫����;ֹͣ��\n������ĻԾ������־�����Ҵ��������������Ψ�ҷ�Ŷ����������棩\n��ȷ��Ҫ������",
							"�������ز�ʱ�Զ�ֹͣ����", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if (result == JOptionPane.YES_OPTION) {
						Config.live_config.AUTO_STOP = true;
						auto = new Thread(new LiveRoomStatus(0));
						auto.start();
					} else {
						checkbox.setSelected(false);
					}
				} else if (Config.live_config.STATUS && Config.live_config.AUTO_STOP) {
					if (auto != null) {
						auto.interrupt();
						try {
							auto.join();
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
						log("###  ��ֹͣ�Զ�ֹͣ  ###");
						refreshUi();
						Config.live_config.AUTO_STOP = false;
					}
				}
			}
		});
		panel_right.add(checkbox);

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

	public void onLiveStart() {
		Config.live_config.START_TIME = new Date().getTime();
		Config.live_config.STATUS = true;
		setEnabled(false);
		prevent.setEnabled(true);
		button_choose.setEnabled(true);
		Config.ALLOW_MODIFY = false;
		MainGui.getInstance().setEnabled(false);
		Config.ALLOW_MODIFY = true;
		button_choose.setText("���������Ϊjson");
		button_status.setText("������ģ�����xml");
		checkbox.setText("�������ز�ʱ�Զ�ֹͣ����");
		checkbox.setSelected(false);
		Config.live_config.AUTO_START = false;
		thread = new Thread(new LiveChat(OutputManager.getFile(), true));
		thread.start();
	}

	public void onLiveStop() {
		Config.live_config.STATUS = false;
		checkbox.setText("������ȡ��ɣ���������ļ�");
		checkbox.setEnabled(false);
		check.setEnabled(true);
		field_delay.setEditable(true);
		button_delay.setEnabled(true);
		button_choose.setEnabled(true);
		button_status.setEnabled(true);
		checkbox.setSelected(false);
		prevent.setEnabled(false);
		Config.live_config.AUTO_STOP = false;
		auto_stopped = true;
		thread.interrupt();
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Config.live_config.STATUS = true;// Ϊ��֤����ļ�ʱ�����ִ����ȷ��������ʱ�޸�״̬
		// Ϊ��ֹ��־������ʾ�������������ˢ��һ��UI
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				refreshUi();
			}
		}).start();
	}
}
