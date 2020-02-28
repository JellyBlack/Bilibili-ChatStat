package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import script.Spider;

/**
 * չʾUP����Ϣ�ĶԻ���
 */
public class UpInfo extends JDialog implements Runnable {

	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JLabel label_faceImage;
	public Spider.Up up;

	/**
	 * �����Ի���
	 * 
	 * @param up up����ʵ�������
	 */
	public UpInfo(Spider.Up up) {
		setModal(true);
		setTitle("ȷ��UP����Ϣ");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.up = up;
		setBounds(100, 100, 700, 300);
		setLocationRelativeTo(null);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));

		JTextArea textArea = new JTextArea();
		textArea.setText("��������ȡ��UP����������Ƶ��Ļ�������Ƿ�����");
		textArea.setBackground(new Color(240, 240, 240));
		textArea.setLineWrap(true);
		textArea.setEditable(false);
		contentPanel.add(textArea, BorderLayout.NORTH);

		label_faceImage = new JLabel();
		contentPanel.add(label_faceImage, BorderLayout.WEST);

		JPanel panel_info = new JPanel();
		contentPanel.add(panel_info, BorderLayout.CENTER);
		panel_info.setLayout(new GridLayout(0, 1, 0, 0));

		JLabel label_name = new JLabel();
		panel_info.add(label_name);

		JLabel label_sexAndUid = new JLabel();
		panel_info.add(label_sexAndUid);
		JLabel label_sign = new JLabel();
		panel_info.add(label_sign);

		JLabel label_role = new JLabel();
		panel_info.add(label_role);

		JLabel label_videos = new JLabel();
		contentPanel.add(label_videos, BorderLayout.SOUTH);

		JPanel buttonPane = new JPanel();
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		buttonPane.setLayout(new GridLayout(0, 2, 0, 0));

		JButton okButton = new JButton("ȷ��������һ��");
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);

		JButton cancelButton = new JButton("ȡ��");
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(cancelButton);

		Thread thread = new Thread(this);
		thread.start();

		label_name.setText("    UP���� " + up.name);
		label_sexAndUid.setText("    �Ա� " + up.sex + "           UID: " + up.uid + "           �ȼ��� " + up.level);
		label_sign.setText("    ��飺 " + up.sign);
		switch (up.role) {
		case 0:
			label_role.setText("    ��֤��Ϣ�� ����֤");
			break;
		case 1:
			label_role.setText("    ��֤��Ϣ�� ������֤����ɫ���磩");
			break;
		case 2:
			label_role.setText("    ��֤��Ϣ�� ��ҵ/������֤����ɫ���磩");
			break;
		}
		label_videos.setText("    ��Ƶ������ " + up.videos + "   ��һ����Ƶ���ֶܷ�P,ÿP�ĵ�Ļ�Ƕ����ģ�");
		okButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Spider.getInstance().confirm();
				dispose();
			}
		});
		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		addWindowListener(new WindowListener() {

			@Override
			public void windowOpened(WindowEvent e) {
			}

			@Override
			public void windowIconified(WindowEvent e) {
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
			}

			@Override
			public void windowClosing(WindowEvent e) {
				Spider.getInstance().getThread().interrupt();
			}

			@Override
			public void windowClosed(WindowEvent e) {
				Spider.getInstance().getThread().interrupt();
			}

			@Override
			public void windowActivated(WindowEvent e) {
			}
		});
	}

	/**
	 * ����һ���̼߳���UP����ͷ��
	 */
	@Override
	public void run() {
		ImageIcon icon = new ImageIcon(up.face_url);
		icon.setImage(icon.getImage().getScaledInstance(160, 160, java.awt.Image.SCALE_DEFAULT));
		label_faceImage.setIcon(icon);
	}
}
