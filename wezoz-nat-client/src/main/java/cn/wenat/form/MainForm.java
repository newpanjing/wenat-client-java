package cn.wenat.form;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import cn.wenat.CallListener;
import cn.wenat.LocalServer;

public class MainForm extends BaseForm {

	private static final long serialVersionUID = -8577615925651575124L;

	private JTextField txtHost;

	private JTextArea txtConsole;

	private JButton btnAction;

	private JLabel lblStatus;

	private LocalServer server;

	private JPanel panelSetting;

	private JScrollPane panelConsole;

	private JLabel label_1;

	private JTextField txtDomain;

	private JLabel lblwezozcom;

	private JLabel lablTraffic;

	private JLabel label_3;

	private JLabel lblSpeed;
	private JLabel lblPing;
	
	public MainForm() {
		setTitle("Wezoz NAT");
		this.setSize(546, 432);
		setLocationRelativeTo(null);

		JLabel label = new JLabel("状态：");
		label.setBounds(20, 27, 61, 16);
		getContentPane().add(label);

		lblStatus = new JLabel("服务停止");
		lblStatus.setBounds(60, 27, 280, 16);
		getContentPane().add(lblStatus);

		btnAction = new JButton("启动服务");
		btnAction.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (btnAction.getText().equals("启动服务")) {
					try {
						String host = txtHost.getText();
						server = new LocalServer();
						server.setServer("http://127.0.0.1:3001");
						server.setForward(host);
						server.setDomain(txtDomain.getText());
						server.setCallListener(new CallListener() {

							@Override
							public void statusCall(String info) {
								lblStatus.setText(info);
							}

							@Override
							public void eventCall(String info) {
								txtConsole.append(info + "\n");
							}

							@Override
							public void onClose() {
								server.stop();
								lblStatus.setText("服务停止");
								btnAction.setText("启动服务");
							}

							@Override
							public void trafficCall(long traffic) {

								lablTraffic.setText(formatNumber(traffic));
							}

							@Override
							public void speedCall(long speed) {
								lblSpeed.setText(formatNumber(speed)+"/s");
								
							}

							@Override
							public void ping(long ms) {
								lblPing.setText(ms+"ms");
							}
						});
						server.start();
						btnAction.setText("停止服务");
					} catch (Exception ex) {
						lblStatus.setText("启动失败！请检查地址是否正确");
					}
				} else {
					server.stop();
					lblStatus.setText("服务停止");
					btnAction.setText("启动服务");
				}
			}
		});
		btnAction.setBounds(339, 22, 117, 29);
		getContentPane().add(btnAction);

		panelSetting = new JPanel();
		panelSetting.setBackground(new Color(248, 251, 253));
		panelSetting.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "\u7F51\u7EDC\u53C2\u6570", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panelSetting.setBounds(19, 55, 506, 119);
		getContentPane().add(panelSetting);
		panelSetting.setLayout(null);

		JLabel lblip = new JLabel("转发地址：");
		lblip.setBounds(18, 64, 65, 16);
		panelSetting.add(lblip);

		txtHost = new JTextField();
		txtHost.setText("http://127.0.0.1:8080");
		txtHost.setBounds(82, 59, 331, 26);
		panelSetting.add(txtHost);
		txtHost.setColumns(10);

		label_1 = new JLabel("绑定域名：");
		label_1.setBounds(18, 30, 65, 16);
		panelSetting.add(label_1);

		txtDomain = new JTextField();
		txtDomain.setColumns(10);
		txtDomain.setBounds(82, 25, 108, 26);
		panelSetting.add(txtDomain);

		lblwezozcom = new JLabel(".wezoz.com");
		lblwezozcom.setBounds(189, 30, 90, 16);
		panelSetting.add(lblwezozcom);

		JLabel label_2 = new JLabel("流出流量：");
		label_2.setBounds(18, 92, 65, 16);
		panelSetting.add(label_2);

		lablTraffic = new JLabel("0KB");
		lablTraffic.setBounds(82, 92, 96, 16);
		panelSetting.add(lablTraffic);

		label_3 = new JLabel("速度：");
		label_3.setBounds(162, 92, 39, 16);
		panelSetting.add(label_3);

		lblSpeed = new JLabel("0KB");
		lblSpeed.setBounds(199, 92, 73, 16);
		panelSetting.add(lblSpeed);
		
		JLabel label_4 = new JLabel("延迟：");
		label_4.setBounds(305, 92, 39, 16);
		panelSetting.add(label_4);
		
		lblPing = new JLabel("0ms");
		lblPing.setBounds(342, 92, 123, 16);
		panelSetting.add(lblPing);

		txtConsole = new JTextArea() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 8749801166570350982L;

			@Override
			public void append(String str) {
				this.setCaretPosition(this.getDocument().getLength());
				str = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()) + " - " + str;
				if (this.getText().length() > 100000) {
					this.setText("");
				}
				super.append(str);
			}
		};
		txtConsole.setText("准备就绪\n");
		txtConsole.setBounds(19, 181, 437, 129);
		panelConsole = new JScrollPane(txtConsole, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		panelConsole.setLocation(20, 210);
		panelConsole.setSize(431, 150);

		getContentPane().add(panelConsole);
		this.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				change();
				super.componentResized(e);
			}
		});
		change();
	}

	private String formatNumber(long traffic) {
		String text = null;
		DecimalFormat format = new DecimalFormat("#.##");
		Double value = traffic * 0.0001221d;
		if (value <= 1024) {
			text = format.format(value) + "KB";
		} else if (value / 1024 > 1) {
			text = format.format((value / 1024)) + "MB";
		} else if (value / 1024 / 1024 > 1) {
			text = format.format((value / 1024 / 1024)) + "GB";
		} else if (value / 1024 / 1024 / 1024 > 1) {
			text = format.format((value / 1024 / 1024 / 1024)) + "TB";
		}
		return text;
	}

	private void change() {
		int width = MainForm.this.getWidth();
		int height = MainForm.this.getHeight();
		int wv = width - 40;
		panelSetting.setSize(wv, panelSetting.getHeight());
		panelConsole.setSize(wv, height - panelConsole.getY() - 40);
		btnAction.setLocation(width - 20 - btnAction.getWidth(), btnAction.getY());
	}

	public static void main(String[] args) {
		new MainForm().setVisible(true);
	}
}
