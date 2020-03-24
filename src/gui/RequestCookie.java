package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import script.Config;

public class RequestCookie extends JDialog {

	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JTextField textField;

	public RequestCookie() {
		setTitle("����Cookie");
		setModal(true);
		setBounds(100, 100, 500, 350);
		setLocationRelativeTo(null);
		getContentPane().setLayout(new BorderLayout());

		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));

		JTextArea textArea = new JTextArea();
		textArea.setWrapStyleWord(true);
		contentPanel.add(textArea, BorderLayout.CENTER);
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setBackground(new Color(240, 240, 240));
		textArea.setText("������������ȫ���Ե�Ӱ�죬��ȡ��ʷ��Ļ��Ҫ�����û��ĵ�¼��Ϣ��\n" + "�밴�����·�����ȡCookie��\n" + "1. �����������������ȷ���ѵ�¼�˺ţ���û��¼���¼��\n"
				+ "2. ������½�һ����ǩҳ����F12�򿪿����߹��ߣ��������������F12�򲻿������߹��ߣ���ٶ��������뿪���߹��ߵķ�ʽ����\n"
				+ "3. �ڸñ�ǩҳ����http://comment.bilibili.com/279786.xml\n"
				+ "4. �ڿ����߹��������ε����Network/���硱����279786.xml�����ұ���ʾ��Headers/ͷ�������������棬�ڡ�Request Headers/����ͷ���п��Կ�����Cookie���ֶΣ��ԡ�_uuid=����ͷ�����ѡ�Cookie�������ݸ���������ճ�������档\n"
				+ "Cookie������������ʷ��Ļ�������߲��ռ��κ���Ϣ�����뿪Դ����û���ռ���ȫ���Կ��õ�����\n" + "�����桿��ò�Ҫ�ô�ţ��ô�ŵĻ��˺�û�˲�Ҫ���ң�");

		textField = new JTextField();
		contentPanel.add(textField, BorderLayout.SOUTH);
		textField.setText(Config.spider_config.COOKIE);
		textField.setColumns(10);

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);

		JButton okButton = new JButton("ȷ��");
		okButton.setActionCommand("OK");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String str = textField.getText();
				if (str.isEmpty()) {
					new Dialog("������Cookie", "������Cookie��").setVisible(true);
					return;
				}
				Config.spider_config.COOKIE = str.replaceFirst("^Cookie:", "");
				Config.spider_config.HISTORICAL = true;
				MainGui.getInstance().setCheckSelected(Config.spider_config.HISTORICAL);
				dispose();
			}
		});
		buttonPane.add(okButton);
		JButton cancelButton = new JButton("ȡ��");
		cancelButton.setActionCommand("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MainGui.getInstance().setCheckSelected(Config.spider_config.HISTORICAL);
				dispose();
			}
		});
		buttonPane.add(cancelButton);
		setVisible(true);
	}
}
