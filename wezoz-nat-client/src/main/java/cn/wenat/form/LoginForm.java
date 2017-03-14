package com.wezoz.nat.form;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;

import javax.swing.JButton;
import javax.swing.JTextField;

public class LoginForm extends BaseForm {

	private static final long serialVersionUID = -8577615925651575124L;

	private JTextField txtUsername;

	private JTextField txtPassword;

	public LoginForm() {
		setResizable(false);
		

		txtUsername = new JTextField();
		txtUsername.setForeground(new Color(104, 104, 104));
		txtUsername.setBounds(69, 66, 235, 41);

		getContentPane().add(txtUsername);
		txtUsername.setColumns(10);

		txtPassword = new JTextField();
		txtPassword.setToolTipText("密码");
		txtPassword.setColumns(10);
		txtPassword.setForeground(new Color(104, 104, 104));
		txtPassword.setBounds(69, 122, 235, 41);
		getContentPane().add(txtPassword);

		JButton btnLogin = new JButton("登录");
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new MainForm().setVisible(true);
			}
		});
		btnLogin.setBounds(187, 185, 117, 47);
		getContentPane().add(btnLogin);

		JButton btnReg = new JButton("注册");
		btnReg.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (java.awt.Desktop.isDesktopSupported()) {
					try {
						// 创建一个URI实例
						URI uri = URI.create("https://www.wezoz.com/register/");
						// 获取当前系统桌面扩展
						java.awt.Desktop dp = java.awt.Desktop.getDesktop();
						// 判断系统桌面是否支持要执行的功能
						if (dp.isSupported(java.awt.Desktop.Action.BROWSE)) {
							// 获取系统默认浏览器打开链接
							dp.browse(uri);
						}
					} catch (NullPointerException e1) {
						// 此为uri为空时抛出异常
					} catch (IOException e2) {
						// 此为无法获取系统默认浏览器
					}
				}
			}
		});
		btnReg.setBounds(69, 185, 117, 47);
		getContentPane().add(btnReg);
		this.setSize(400, 300);
		this.setLocationRelativeTo(null);
		this.setTitle("Wezoz NAT 登录");
	}

	public static void main(String[] args) {
		new LoginForm().setVisible(true);
	}
}
