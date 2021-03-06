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
import script.MultiThreadUtil;
import script.OnChatUtilFinished;
import script.OnJsonUtilFinished;
import script.OnXmlUtilFinished;
import script.OutputManager;

/**
 * 直播弹幕爬取的控件类
 */
public class LivePanel extends JPanel implements OnChatUtilFinished, OnXmlUtilFinished, OnJsonUtilFinished {
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
	private JPanel panel;
	private JCheckBox prevent;
	private JCheckBox check_multiThread;
	private JPanel panel_1;
	private int style = 0;

	/**
	 * 创建控件
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

		JButton button = new JButton("清空日志");
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
					log("已强制刷新日志显示区");
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

		JLabel label_room = new JLabel("直播间");
		panel_room.add(label_room);

		field_room = new JTextField();
		panel_room.add(field_room);
		field_room.setColumns(10);

		label_times = new JLabel("爬取成功的次数：未开始");
		panel_right.add(label_times);

		label_total = new JLabel("已爬取弹幕数：未开始");
		panel_right.add(label_total);

		panel = new JPanel();
		panel_right.add(panel);

		check = new JCheckBox("智能调整爬取间隔");
		panel.add(check);
		check.setSelected(Config.live_config.AUTO_DELAY);

		prevent = new JCheckBox("防手胡（锁定控件）");
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

		JLabel label_delay = new JLabel("爬取间隔（ms）");
		panel_delay.add(label_delay);

		field_delay = new JTextField();
		field_delay.setText(Config.live_config.DELAY + "");
		panel_delay.add(field_delay);
		field_delay.setColumns(10);

		button_delay = new JButton("注册");
		button_delay.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (long_clicked) {
					return;
				}
				try {
					int delay = Integer.parseInt(field_delay.getText());
					if (delay < 0) {
						log("【警告】间隔设置失败，请检查输入");
						return;
					}
					if (Config.live_config.MULTI_THREAD) {
						if (delay < 50) {
							if (Config.live_config.I_DO_NOT_FEAR_BANNING) {
								Config.live_config.DELAY = delay;
								log("尊贵的封  号  斗  罗大人，间隔已设置为" + delay + "ms");
							} else if (Config.live_config.DELAY != delay) {
								Config.live_config.DELAY = 50;
								log("为了您的账号安全，多线程下间隔不允许小于50ms");
								log("已将间隔设置为50ms");
							}
						} else if (Config.live_config.DELAY != delay) {
							Config.live_config.DELAY = delay;
							log("已将间隔设置为" + delay + "ms");
						}
					} else if (Config.live_config.DELAY != delay) {
						Config.live_config.DELAY = delay;
						log("已将间隔设置为" + delay + "ms");
					}
					if (thread != null) {
						thread.interrupt();
					}
				} catch (NumberFormatException err) {
					log("【警告】间隔设置失败，请检查输入");
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
						log("您未启动爬取，不能开始临时零间隔模式");
						refreshUi();
						return;
					}
					successful = true;
					log("###  临时零间隔模式已启动  ###");
					refreshUi();
					if (Config.live_config.MULTI_THREAD && (!Config.live_config.I_DO_NOT_FEAR_BANNING)) {
						field_delay.setText("50");
						Config.live_config.DELAY = 50;
						log("多线程下不能设置为0间隔，已将其设置为50ms");
					} else {
						field_delay.setText("0");
						Config.live_config.DELAY = 0;
					}
					Config.live_config.AUTO_DELAY = false;
					thread.interrupt();
				}
			};

			@Override
			public void mousePressed(MouseEvent e) {
				if (!button_delay.isEnabled()) {
					return;
				}
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
				if (!button_delay.isEnabled()) {
					return;
				}
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
				log("###  临时零间隔模式已停止  ###");
				refreshUi();
			}
		});
		panel_delay.add(button_delay);

		JPanel panel_control = new JPanel();
		panel_right.add(panel_control);

		button_choose = new JButton("选择输出目录");
		button_status = new JButton("开始爬取");
		button_choose.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!Config.live_config.STATUS) {
					FileManager.showFileSaveDialog(instance, "直播弹幕", 1);
					if (OutputManager.getFile() != null) {
						log("###  已选择输出目录  ###");
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
						log("###  您已手动停止爬虫  ###");
					}
					log("###  正在等待弹幕处理完毕，请稍候  ###");
					refreshUi();
					button_choose.setEnabled(false);
					button_choose.setText("等待弹幕处理完毕");
					button_status.setEnabled(false);
					checkbox.setEnabled(false);
					if (style == 3) {
						new Thread(new Runnable() {
							@Override
							public void run() {
								onChatUtilFinish();
							}
						}).start();
					}
					Config.live_config.STATUS = false;
					style = 1;
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
							new Dialog("配置不完整", "请先选择输出目录。").setVisible(true);
							return;
						}
						if (field_room.getText().isEmpty()) {
							new Dialog("配置不完整", "请输入直播间。").setVisible(true);
							return;
						}
						try {
							Config.live_config.ROOM = Integer.parseInt(field_room.getText());
						} catch (NumberFormatException err) {
							new Dialog("直播间配置错误", "无法读取房间号。请检查直播间房间号。").setVisible(true);
							return;
						}
						if (Config.live_config.ROOM <= 0) {
							new Dialog("直播间配置错误", "直播间房间号必须是大于0的整数。").setVisible(true);
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
						log("###  您已手动启动爬虫  ###");
						b = true;
						refreshUi();
					}
					prevent.setEnabled(true);
					Config.live_config.START_TIME = new Date().getTime();
					Config.live_config.STATUS = true;
					checkbox.setText("当主播关播时自动停止爬虫");
					checkbox.setSelected(false);
					setEnabled(false);
					Config.ALLOW_MODIFY = false;
					MainGui.getInstance().setEnabled(false);
					Config.ALLOW_MODIFY = true;
					button_choose.setText("结束并输出为json");
					button_status.setText("结束并模拟输出xml");
					thread = new Thread(new LiveChat(OutputManager.getFile(), b, LivePanel.this));
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
						log("###  您已手动停止爬虫  ###");
					}
					log("###  正在等待弹幕处理完毕，请稍候  ###");
					refreshUi();
					button_choose.setEnabled(false);
					button_status.setEnabled(false);
					button_status.setText("等待弹幕处理完毕");
					checkbox.setEnabled(false);
					if (style == 3) {
						new Thread(new Runnable() {
							@Override
							public void run() {
								onChatUtilFinish();
							}
						}).start();
					}
					style = 2;
					Config.live_config.STATUS = false;
				}
			}
		});
		panel_control.add(button_status);

		panel_1 = new JPanel();
		panel_right.add(panel_1);

		checkbox = new JCheckBox("当主播开播时自动启动爬虫");
		panel_1.add(checkbox);
		checkbox.setSelected(false);

		check_multiThread = new JCheckBox("使用多线程");
		check_multiThread.addActionListener(new ActionListener() {
			Thread multi = null;
			boolean thread_started = false;

			@Override
			public void actionPerformed(ActionEvent e) {
				if ((!Config.live_config.MULTI_THREAD) && (!thread_started)) {
					int result = JOptionPane.showConfirmDialog(null,
							"多线程爬取可以有效加快爬取速度。\n开启后，程序需要先向服务器请求50次弹幕，以估计延迟，所以如果主播即将开播，请勿勾选。\n注意：为保护您的账号安全，若开启多线程，延迟不能小于50ms。\n确认开启吗？",
							"使用多线程", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if (result == JOptionPane.YES_OPTION) {
						if (field_room.getText().isEmpty()) {
							new Dialog("配置不完整", "请输入直播间。").setVisible(true);
							check_multiThread.setSelected(false);
							return;
						}
						try {
							Config.live_config.ROOM = Integer.parseInt(field_room.getText());
						} catch (NumberFormatException err) {
							new Dialog("直播间配置错误", "无法读取房间号。请检查直播间房间号。").setVisible(true);
							check_multiThread.setSelected(false);
							return;
						}
						if (Config.live_config.ROOM <= 0) {
							new Dialog("直播间配置错误", "直播间房间号必须是大于0的整数。").setVisible(true);
							check_multiThread.setSelected(false);
							return;
						}
						field_room.setEnabled(false);
						button_status.setEnabled(false);
						button_delay.setEnabled(false);
						checkbox.setEnabled(false);
						Config.ALLOW_MODIFY = false;
						MainGui.getInstance().setEnabled(false);
						Config.ALLOW_MODIFY = true;
						multi = new Thread(new Runnable() {
							@Override
							public void run() {
								thread_started = true;
								log("延迟测试已开始");
								refreshUi();
								int delay = new MultiThreadUtil().getMaxDelay();
								if (delay == 0) {
									return;
								}
								Config.live_config.MAX_DELAY = delay;
								Config.live_config.MULTI_THREAD = true;
								thread_started = false;
								log("开启成功，最大延迟为" + delay + "ms，已保存配置");
								if (delay > 1000) {
									log("【警告】最大延迟超过1000ms，可能会影响后续超时判断");
									log("【警告】建议重新测延迟");
								}
								log("注意：如果取消选择“使用多线程”，则下次需要重新测延迟");
								if (Config.live_config.DELAY < 50 && (!Config.live_config.I_DO_NOT_FEAR_BANNING)) {
									Config.live_config.DELAY = 50;
									log("发现间隔小于50ms，已调整至50ms");
								}
								thread_started = false;
								field_room.setEnabled(false);
								button_status.setEnabled(true);
								button_delay.setEnabled(true);
								checkbox.setEnabled(true);
								Config.ALLOW_MODIFY = false;
								MainGui.getInstance().setEnabled(true);
								Config.ALLOW_MODIFY = true;
								field_delay.setText(Config.live_config.DELAY + "");
								refreshUi();
							}
						});
						multi.start();
					} else {
						check_multiThread.setSelected(false);
					}
				} else {
					if (multi != null) {
						multi.interrupt();
					}
					log("已取消多线程模式");
					refreshUi();
					LiveChat.setLiveRoomHandled(false);
					thread_started = false;
					field_room.setEnabled(true);
					button_status.setEnabled(true);
					button_delay.setEnabled(true);
					checkbox.setEnabled(true);
					Config.ALLOW_MODIFY = false;
					MainGui.getInstance().setEnabled(true);
					Config.ALLOW_MODIFY = true;
					Config.live_config.MULTI_THREAD = false;
					check_multiThread.setSelected(false);
				}
			}
		});
		panel_1.add(check_multiThread);
		checkbox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!Config.live_config.STATUS && !Config.live_config.AUTO_START) {
					int result = JOptionPane.showConfirmDialog(null,
							"该功能可以帮助你精准地控制爬虫开始运行的时间。\n勾选该选项后，程序将不间断地向服务器请求直播间状态，一旦开播将立即启动爬虫。\n勾选后您也可以手动启动爬虫。\n为避免向服务器请求过多数据，建议在开播前一两分钟时启动此选项。\n（你屏幕跃动的日志，是我此生不灭的信仰，唯我封号斗罗永世长存）\n你确认要开启吗？",
							"当主播开播时自动启动爬虫", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if (result == JOptionPane.YES_OPTION) {
						if (OutputManager.getFile() == null) {
							new Dialog("配置不完整", "请先选择输出目录。").setVisible(true);
							checkbox.setSelected(false);
							return;
						}
						if (field_room.getText().isEmpty()) {
							new Dialog("配置不完整", "请输入直播间。").setVisible(true);
							checkbox.setSelected(false);
							return;
						}
						try {
							Config.live_config.ROOM = Integer.parseInt(field_room.getText());
						} catch (NumberFormatException err) {
							new Dialog("直播间配置错误", "无法读取房间号。请检查直播间房间号。").setVisible(true);
							checkbox.setSelected(false);
							return;
						}
						if (Config.live_config.ROOM <= 0) {
							new Dialog("直播间配置错误", "直播间房间号必须是大于0的整数。").setVisible(true);
							checkbox.setSelected(false);
							return;
						}
						setEnabled(false);
						button_choose.setEnabled(false);
						check_multiThread.setEnabled(false);
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
						log("###  已停止自动启动  ###");
						refreshUi();
						setEnabled(true);
						check_multiThread.setEnabled(true);
						if (Config.live_config.MULTI_THREAD) {
							field_room.setEnabled(false);
						}
						button_choose.setEnabled(true);
						Config.ALLOW_MODIFY = false;
						MainGui.getInstance().setEnabled(true);
						Config.ALLOW_MODIFY = true;
						Config.live_config.AUTO_START = false;
					}
				} else if (Config.live_config.STATUS && !Config.live_config.AUTO_STOP) {
					int result = JOptionPane.showConfirmDialog(null,
							"该功能可以帮助你精准地控制爬虫停止运行的时间。\n勾选该选项后，程序将不间断地向服务器请求直播间状态，一旦停播将立即停止爬虫。\n停止后不会自动输出到文件，需手动操作；停止后不能继续爬取。\n勾选后您也可以手动停止爬虫。\n如果你正在爬取的直播间正处于关播或轮播状态，那么勾选后爬虫立即停止。\n为避免向服务器请求过多数据，建议在关播前一两分钟时启动此选项。\n如果主播是临时下播，不要勾选此选项，否则爬虫将在中途停止。\n（你屏幕跃动的日志，是我此生不灭的信仰，唯我封号斗罗永世长存）\n你确认要开启吗？",
							"当主播关播时自动停止爬虫", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
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
						log("###  已停止自动停止  ###");
						refreshUi();
						Config.live_config.AUTO_STOP = false;
					}
				}
			}
		});

	}

	/**
	 * 获取示例
	 * 
	 * @return 示例
	 */
	public static LivePanel getInstance() {
		return instance;
	}

	/**
	 * 输出日志
	 * 
	 * @param log 日志内容
	 */
	public void log(String log) {
		logs.add(0, log);
	}

	/**
	 * 更新日志展示的UI
	 */
	public void refreshUi() {
		list.setListData(logs.toArray(new String[0]));
	}

	/**
	 * 添加一次爬取成功的次数
	 */
	public void addTime() {
		times++;
		label_times.setText("爬取成功的次数：" + times + "     零缓冲次数：" + failure);
	}

	/**
	 * 刷新状态展示区
	 * 
	 * @param new_chat_count 新弹幕的数量，即与上一次爬取的结果不重复的弹幕数量
	 * @param buffer         缓冲区
	 * @param first_run      是否是第一次爬取
	 */
	public void refreshLabel(int new_chat_count, int buffer, boolean first_run) {
		count += new_chat_count;
		if (first_run) {
			label_total.setText("已爬取弹幕数：" + count + "     [缓冲区填充中]");
		} else if (buffer == 0) {
			failure++;
			label_total.setText("已爬取弹幕数：" + count + "     【警告 - 缓冲区：" + buffer + "】");
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("已爬取弹幕数：" + count + "     缓冲区：" + buffer + " ");
			for (int i = 0; i < buffer; i++) {
				sb.append("+");
			}
			label_total.setText(sb.toString());
		}
	}

	/**
	 * 设置控件是否可用
	 * 
	 * @param b 是否可用
	 */
	@Override
	public void setEnabled(boolean b) {
		super.setEnabled(b);
		field_room.setEnabled(b);
		check_multiThread.setEnabled(b);
	}

	public void reset() {
		times = 0;
		count = 0;
		failure = 0;
		checkbox.setText("当主播开播时自动启动爬虫");
		checkbox.setEnabled(true);
		checkbox.setSelected(false);
		button_delay.setEnabled(true);
		button_choose.setEnabled(true);
		button_status.setEnabled(true);
		prevent.setEnabled(false);
		button_choose.setText("选择输出目录");
		button_status.setText("开始爬取");
		label_times.setText("爬取成功的次数：未开始");
		label_total.setText("已爬取弹幕数：未开始");
		Config.ALLOW_MODIFY = false;
		setEnabled(true);
		MainGui.getInstance().setEnabled(true);
		field_room.setEnabled(true);
		Config.ALLOW_MODIFY = true;
	}

	/**
	 * 更新爬取延时
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
		button_status.setEnabled(true);
		Config.ALLOW_MODIFY = false;
		MainGui.getInstance().setEnabled(false);
		Config.ALLOW_MODIFY = true;
		button_delay.setEnabled(true);
		button_choose.setText("结束并输出为json");
		button_status.setText("结束并模拟输出xml");
		checkbox.setText("当主播关播时自动停止爬虫");
		checkbox.setSelected(false);
		Config.live_config.AUTO_START = false;
		thread = new Thread(new LiveChat(OutputManager.getFile(), true, this));
		thread.start();
	}

	public void onLiveStop() {
		style = 3;
		Config.live_config.STATUS = false;
		checkbox.setText("本次爬取完成，请输出到文件");
		checkbox.setEnabled(false);
		check.setEnabled(true);
		field_delay.setEditable(true);
		button_delay.setEnabled(true);
		button_choose.setEnabled(true);
		button_status.setEnabled(true);
		checkbox.setSelected(false);
		prevent.setEnabled(false);
		Config.live_config.AUTO_STOP = false;
	}

	public void onUtilRoomNumberFailed(int from) {
		if (from == 0) {
			reset();
		} else if (from == 1) {
			times = 0;
			count = 0;
			failure = 0;
			Config.live_config.MULTI_THREAD = false;
			checkbox.setText("当主播开播时自动启动爬虫");
			check_multiThread.setEnabled(true);
			check_multiThread.setSelected(false);
			button_delay.setEnabled(true);
			button_choose.setEnabled(true);
			button_status.setEnabled(true);
			prevent.setEnabled(false);
			button_choose.setText("选择输出目录");
			button_status.setText("开始爬取");
			label_times.setText("爬取成功的次数：未开始");
			label_total.setText("已爬取弹幕数：未开始");
			Config.ALLOW_MODIFY = false;
			setEnabled(true);
			MainGui.getInstance().setEnabled(true);
			field_room.setEnabled(true);
			Config.ALLOW_MODIFY = true;
		} else {
			reset();
		}
	}

	@Override
	public void onJsonUtilFinish(boolean successful) {
		if (successful) {
			new Dialog("输出成功", "已输出到json文件。").setVisible(true);
			;
			thread = null;
			reset();
			if (Config.live_config.MULTI_THREAD) {
				field_room.setEnabled(false);
			}
			OutputManager.setFile(null);
			log("###  输出文件完毕  ###");
			refreshUi();
		} else {
			thread = null;
			reset();
			if (Config.live_config.MULTI_THREAD) {
				field_room.setEnabled(false);
			}
			OutputManager.setFile(null);
			log("###  输出文件失败  ###");
			refreshUi();
		}
	}

	@Override
	public void onXmlUtilFinish(boolean successful) {
		if (successful) {
			new Dialog("输出成功",
					"已输出到xml文件。\n导出的xml文件可供ChatStat统计使用，并不是真正的哔哩哔哩xml弹幕文件。\n1.2.0版本之后，您可以使用第三方xml弹幕转字幕工具转换输出的xml文件，并且保证弹幕颜色显示正常。")
							.setVisible(true);
			;
			thread = null;
			reset();
			if (Config.live_config.MULTI_THREAD) {
				field_room.setEnabled(false);
			}
			OutputManager.setFile(null);
			log("###  输出文件完毕  ###");
			refreshUi();
		} else {
			thread = null;
			reset();
			if (Config.live_config.MULTI_THREAD) {
				field_room.setEnabled(false);
			}
			OutputManager.setFile(null);
			log("###  输出文件失败  ###");
			refreshUi();
		}
	}

	@Override
	public void onChatUtilFinish() {
		if (style == 1) {
			Config.live_config.STATUS = false;
			log("###  正在输出文件  ###");
			refreshUi();
			button_choose.setText("正在输出");
			new Thread(new Runnable() {
				@Override
				public void run() {
					LiveChat.saveToJson(LivePanel.this);
				}
			}).start();
		} else if (style == 2) {
			Config.live_config.STATUS = false;
			log("###  正在输出文件  ###");
			refreshUi();
			button_status.setText("正在输出");
			new Thread(new Runnable() {
				@Override
				public void run() {
					LiveChat.saveToXml(LivePanel.this);
				}
			}).start();
		} else if (style == 3) {
			Config.live_config.STATUS = true;// 为保证输出文件时代码块执行正确，这里临时修改状态
			// 为防止日志区不显示的情况，这里再刷新一下UI
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

	public void setRoomNumber() {
		field_room.setText(Config.live_config.ROOM + "");
	}
}
