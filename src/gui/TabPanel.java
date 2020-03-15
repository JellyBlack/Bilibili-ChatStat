package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import script.Config;

/**
 * ÿһ��ѡ������
 */
public class TabPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private JCheckBox check_ignore_cases;
	private JCheckBox check_to_sbc;
	private JCheckBox check_ignore_spaces;
	private JCheckBox check_split_chats;
	private JCheckBox check_advanced_match;
	private JCheckBox check_mark_once;
	private JTextField field_rank_limit;
	private JTextField field_length2;
	private JTextField field_start_time2;
	private JTextField field_end_time2;
	private JCheckBox check_crc32;
	private JTextField field_length6;
	private JTextField field_start_time6;
	private JTextField field_end_time6;
	private JLabel label_output;
	private JButton button_output;
	private JTextArea textArea;
	private JPanel panel;

	/**
	 * �������
	 * 
	 * @param tab ѡ���ѡ���1��ʼ
	 */
	public TabPanel(int tab) {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 512, 0 };
		gridBagLayout.rowHeights = new int[] { 24, 284, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		textArea = new JTextArea();
		textArea.setBackground(new Color(240, 240, 240));
		textArea.setLineWrap(true);
		GridBagConstraints gbc_textArea = new GridBagConstraints();
		gbc_textArea.fill = GridBagConstraints.HORIZONTAL;
		gbc_textArea.anchor = GridBagConstraints.NORTH;
		gbc_textArea.insets = new Insets(0, 0, 5, 0);
		gbc_textArea.gridx = 0;
		gbc_textArea.gridy = 0;
		add(textArea, gbc_textArea);

		GridLayout gl_panel = new GridLayout();
		gl_panel.setColumns(1);
		gl_panel.setRows(0);
		panel = new JPanel(gl_panel);
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 1;
		add(panel, gbc_panel);

		check_ignore_cases = new JCheckBox("���Դ�Сд");
		panel.add(check_ignore_cases);

		check_to_sbc = new JCheckBox("ת��Ϊȫ���ַ�");
		panel.add(check_to_sbc);

		check_ignore_spaces = new JCheckBox("ɾ����β�ո�");
		panel.add(check_ignore_spaces);

		check_split_chats = new JCheckBox("���Բ�ֵ�Ļ���硰awsl awsl awsl������Ϊ��awsl��");
		panel.add(check_split_chats);

		check_advanced_match = new JCheckBox("�߼���Ļƥ�俪�أ�ƥ����������ѡ���");
		panel.add(check_advanced_match);

		check_mark_once = new JCheckBox("һ���˷��Ķ�����ͬ��Ļֻ��һ�Σ�����ͬ��Ļ���ж�λ��ǰ���ѡ�����֮��");
		panel.add(check_mark_once);

		JPanel panel_output = new JPanel();
		panel_output.setLayout(new GridLayout(0, 2, 0, 0));

		label_output = new JLabel("�����ʽ����ǰΪ��swing���չʾ��");
		panel_output.add(label_output);

		button_output = new JButton("�л�����csv�ļ�չʾ��");
		button_output.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				if (Config.public_config.OUTPUT_STYLE == 0) {
					Config.public_config.OUTPUT_STYLE = 1;
				} else {
					Config.public_config.OUTPUT_STYLE = 0;
				}
				if (Config.public_config.OUTPUT_STYLE == 0) {
					label_output.setText("�����ʽ����ǰΪ��swing���չʾ��");
					button_output.setText("�л�����csv�ļ�չʾ��");
				} else {
					label_output.setText("�����ʽ����ǰΪ��csv�ļ�չʾ��");
					button_output.setText("�л�����swing���չʾ��");
				}
			}
		});
		panel_output.add(button_output);
		check_mark_once.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				Config.public_config.MARK_ONCE = check_mark_once.isSelected();
			}
		});
		check_advanced_match.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				Config.public_config.ADVANCED_MATCH = check_advanced_match.isSelected();
			}
		});
		check_split_chats.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				Config.public_config.SPLIT_CHATS = check_split_chats.isSelected();
			}
		});
		check_ignore_spaces.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				Config.public_config.IGNORE_SPACES = check_ignore_spaces.isSelected();
			}
		});
		check_to_sbc.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				Config.public_config.TO_SBC = check_to_sbc.isSelected();
			}
		});
		check_ignore_cases.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				Config.public_config.IGNORE_CASES = check_ignore_cases.isSelected();
			}
		});

		JPanel panel_rank_limit = new JPanel();
		if (tab == 1) {
			panel.add(panel_rank_limit);
		}
		panel_rank_limit.setLayout(new BorderLayout(0, 0));

		JLabel label_rank_limit = new JLabel("�������������Ļ��������չʾǰ��������������Ϊ-1��ȫ������");
		panel_rank_limit.add(label_rank_limit, BorderLayout.CENTER);

		field_rank_limit = new JTextField();
		panel_rank_limit.add(field_rank_limit, BorderLayout.EAST);
		field_rank_limit.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				try {
					Config.tab1.RANK_LIMIT = Integer.parseInt(field_rank_limit.getText());
				} catch (NumberFormatException e) {
				}
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				try {
					Config.tab1.RANK_LIMIT = Integer.parseInt(field_rank_limit.getText());
				} catch (NumberFormatException e) {
				}
			}

			@Override
			public void changedUpdate(DocumentEvent arg0) {
			}
		});
		field_rank_limit.setColumns(20);

		JPanel panel_length2 = new JPanel();

		panel_length2.setLayout(new BorderLayout(0, 0));

		JLabel label_length2 = new JLabel("�������ȣ���λΪ��");
		panel_length2.add(label_length2, BorderLayout.CENTER);

		field_length2 = new JTextField();
		panel_length2.add(field_length2, BorderLayout.EAST);
		field_length2.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				try {
					Config.tab2.LENGTH = Float.parseFloat(field_length2.getText());
				} catch (NumberFormatException e) {
				}
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				try {
					Config.tab2.LENGTH = Float.parseFloat(field_length2.getText());
				} catch (NumberFormatException e) {
				}
			}

			@Override
			public void changedUpdate(DocumentEvent arg0) {
			}
		});
		field_length2.setColumns(20);

		JPanel panel_start_time2 = new JPanel();

		panel_start_time2.setLayout(new BorderLayout(0, 0));

		JLabel label_start_time2 = new JLabel("��ʼʱ�䣨������������Ϊ0����ͷ��ʼ");
		panel_start_time2.add(label_start_time2, BorderLayout.CENTER);

		field_start_time2 = new JTextField();
		panel_start_time2.add(field_start_time2, BorderLayout.EAST);
		field_start_time2.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				try {
					Config.tab2.START_TIME = Float.parseFloat(field_start_time2.getText());
				} catch (NumberFormatException e) {
				}
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				try {
					Config.tab2.START_TIME = Float.parseFloat(field_start_time2.getText());
				} catch (NumberFormatException e) {
				}
			}

			@Override
			public void changedUpdate(DocumentEvent arg0) {
			}
		});
		field_start_time2.setColumns(20);

		JPanel panel_end_time2 = new JPanel();

		if (tab == 2) {
			panel.add(panel_length2);
			panel.add(panel_start_time2);
			panel.add(panel_end_time2);
		}
		panel_end_time2.setLayout(new BorderLayout(0, 0));

		JLabel label_end_time2 = new JLabel("��ֹʱ�䣨������������Ϊ-1������Ƶĩβ");
		panel_end_time2.add(label_end_time2, BorderLayout.CENTER);

		field_end_time2 = new JTextField();
		panel_end_time2.add(field_end_time2, BorderLayout.EAST);
		field_end_time2.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				try {
					Config.tab2.END_TIME = Float.parseFloat(field_end_time2.getText());
				} catch (NumberFormatException e) {
				}
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				try {
					Config.tab2.END_TIME = Float.parseFloat(field_end_time2.getText());
				} catch (NumberFormatException e) {
				}
			}

			@Override
			public void changedUpdate(DocumentEvent arg0) {
			}
		});
		field_end_time2.setColumns(20);

		check_crc32 = new JCheckBox("����CRC32���㣨ʮ������ʱ�䣬�����Ƽ���");
		check_crc32.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				Config.tab3.CRC32 = check_crc32.isSelected();
			}
		});
		if (tab == 3) {
			panel.add(check_crc32);
			panel.add(panel_rank_limit);
		}

		JPanel panel_length6 = new JPanel();

		panel_length6.setLayout(new BorderLayout(0, 0));

		JLabel label_length6 = new JLabel("�������ȣ���λΪ��");
		panel_length6.add(label_length6, BorderLayout.CENTER);

		field_length6 = new JTextField();
		panel_length6.add(field_length6, BorderLayout.EAST);
		field_length6.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				try {
					Config.tab6.LENGTH = Integer.parseInt(field_length6.getText());
				} catch (NumberFormatException e) {
				}
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				try {
					Config.tab6.LENGTH = Integer.parseInt(field_length6.getText());
				} catch (NumberFormatException e) {
				}
			}

			@Override
			public void changedUpdate(DocumentEvent arg0) {
			}
		});
		field_length6.setColumns(20);

		JPanel panel_start_time6 = new JPanel();

		panel_start_time6.setLayout(new BorderLayout(0, 0));

		JLabel label_start_time6 = new JLabel("��ʼʱ�䣨������������Ϊ0����00:00:00��ʼ");
		panel_start_time6.add(label_start_time6);

		field_start_time6 = new JTextField();
		panel_start_time6.add(field_start_time6, BorderLayout.EAST);
		field_start_time6.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				try {
					Config.tab6.START_TIME = Integer.parseInt(field_start_time6.getText());
				} catch (NumberFormatException e) {
				}
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				try {
					Config.tab6.START_TIME = Integer.parseInt(field_start_time6.getText());
				} catch (NumberFormatException e) {
				}
			}

			@Override
			public void changedUpdate(DocumentEvent arg0) {
			}
		});
		field_start_time6.setColumns(20);

		JPanel panel_end_time6 = new JPanel();

		if (tab == 6) {
			panel.add(panel_length6);
			panel.add(panel_start_time6);
			panel.add(panel_end_time6);
		}
		panel_end_time6.setLayout(new BorderLayout(0, 0));

		JLabel label_end_time6 = new JLabel("��ֹʱ�䣨������������Ϊ-1����һ�����");
		panel_end_time6.add(label_end_time6, BorderLayout.CENTER);

		field_end_time6 = new JTextField();
		panel_end_time6.add(field_end_time6, BorderLayout.EAST);
		field_end_time6.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				try {
					Config.tab6.END_TIME = Integer.parseInt(field_end_time6.getText());
				} catch (NumberFormatException e) {
				}
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				if (!Config.ALLOW_MODIFY) {
					return;
				}
				try {
					Config.tab6.END_TIME = Integer.parseInt(field_end_time6.getText());
				} catch (NumberFormatException e) {
				}
			}

			@Override
			public void changedUpdate(DocumentEvent arg0) {
			}
		});
		panel.add(panel_output);
		field_end_time6.setColumns(20);
		textArea.setEditable(false);
		switch (tab) {
		case 1:
			textArea.setText(
					"���ظ���Ļͳ�ơ�\n�ù��ܿɲ鿴���ڷ������ĵ�Ļ��ʲô��\n1.2.0�������ܣ���ChatStat.csv�ļ��С��滻�ַ�����һ������д <@ignore> �� <@only> ��ǩ��ǰ�߿�����ͳ��ʱ����ƥ���Ӧ������ʽ�ĵ�Ļ�����߿�����ͳ��ʱֻͳ��ƥ���Ӧ������ʽ�ĵ�Ļ���ù��ܶ�����ͳ��������ã���򿪡��߼���Ļƥ�俪�ء��������Github�ֿ⡣");
			break;
		case 2:
			textArea.setText(
					"����ĻƵ������ͳ�ơ�\n�ù��ܿɲ鿴��Ƶ�ĵ�Ļ�ܼ���������������������ҳ�˲������ĸ��ܽ�����������������ָÿ�β�����ʱ�䳤�̡�������Ϊ15s���������15sΪһ�飬ͳ��ÿ���ڵĵ�Ļ����\n�鿴���ߣ���ѡ��csv�ļ�չʾ����Ȼ��ʹ�õ��ӱ�񴴽�ͼ��");
			break;
		case 3:
			textArea.setText(
					"����Ļˢ��ͳ�ơ�\n�ù��ܿɲ鿴���͵�Ļ�����û���\n�翪��CRC32���㣬���򽫱����ƽ�CRC32������û���UID�����Ƽ�����\n����Ҫ����ֱ����Ļץȡ�������ģ��xml�ļ�ֱ�Ӱ����û���UID��Ϣ��������봦�����xml�ļ�������ѡ��CRC32���㡱��������̽�������");
			break;
		case 4:
			textArea.setText(
					"���µ�Ļ��������ͳ�ơ�\n�ù��ܿɲ鿴��Ļ�������·ݱ仯�������\n����������Ļ�������ǹ̶��ģ���˿��ܳ���һ��ʱ��û��������Ļ�������\n�鿴���ߣ���ѡ��csv�ļ�չʾ����Ȼ��ʹ�õ��ӱ�񴴽�ͼ��");
			break;
		case 5:
			textArea.setText(
					"���յ�Ļ��������ͳ�ơ�\n�ù��ܿɲ鿴��Ļ���������ڱ仯�������\n����������Ļ�������ǹ̶��ģ���˿��ܳ���һ��ʱ��û��������Ļ�������\n�鿴���ߣ���ѡ��csv�ļ�չʾ����Ȼ��ʹ�õ��ӱ�񴴽�ͼ��");
			break;
		case 6:
			textArea.setText(
					"���վ���Ļ��Ծʱ��ͳ�ơ�\n�ù��ܿɲ鿴������һ���ڵ��ĸ�ʱ��η��͵�Ļ��ࡣ��������ָÿ�β�����ʱ�䳤�̡�������Ϊ1800s���������1800sΪһ�飬ͳ��ÿ���ڵĵ�Ļ����\n�鿴���ߣ���ѡ��csv�ļ�չʾ����Ȼ��ʹ�õ��ӱ�񴴽�ͼ��");
			break;
		}
	}

	/**
	 * ˢ�����
	 */
	public void refresh() {
		check_ignore_cases.setSelected(Config.public_config.IGNORE_CASES);
		check_to_sbc.setSelected(Config.public_config.TO_SBC);
		check_ignore_spaces.setSelected(Config.public_config.IGNORE_SPACES);
		check_split_chats.setSelected(Config.public_config.SPLIT_CHATS);
		check_advanced_match.setSelected(Config.public_config.ADVANCED_MATCH);
		check_mark_once.setSelected(Config.public_config.MARK_ONCE);
		field_rank_limit.setText(Integer.toString(Config.tab1.RANK_LIMIT));
		field_length2.setText(Float.toString(Config.tab2.LENGTH));
		field_start_time2.setText(Float.toString(Config.tab2.START_TIME));
		field_end_time2.setText(Float.toString(Config.tab2.END_TIME));
		check_crc32.setSelected(Config.tab3.CRC32);
		field_length6.setText(Integer.toString(Config.tab6.LENGTH));
		field_start_time6.setText(Integer.toString(Config.tab6.START_TIME));
		field_end_time6.setText(Integer.toString(Config.tab6.END_TIME));
		if (Config.public_config.OUTPUT_STYLE == 0) {
			label_output.setText("�����ʽ����ǰΪ��swing���չʾ��");
			button_output.setText("�л�����csv�ļ�չʾ��");
		} else {
			label_output.setText("�����ʽ����ǰΪ��csv�ļ�չʾ��");
			button_output.setText("�л�����swing���չʾ��");
		}
	}

	/**
	 * ��������Ƿ����
	 * 
	 * @param b �Ƿ����
	 */
	@Override
	public void setEnabled(boolean b) {
		super.setEnabled(b);
		check_ignore_cases.setEnabled(b);
		check_to_sbc.setEnabled(b);
		check_ignore_spaces.setEnabled(b);
		check_split_chats.setEnabled(b);
		check_advanced_match.setEnabled(b);
		check_mark_once.setEnabled(b);
		field_rank_limit.setEnabled(b);
		field_length2.setEnabled(b);
		field_start_time2.setEnabled(b);
		field_end_time2.setEnabled(b);
		check_crc32.setEnabled(b);
		field_length6.setEnabled(b);
		field_start_time6.setEnabled(b);
		field_end_time6.setEnabled(b);
		button_output.setEnabled(b);
	}
}
