package cn.wenat.form;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

/**
 * 父类
 */
public class BaseForm extends JFrame {

    private static final long serialVersionUID = -8446051052250946428L;

    public BaseForm() {

        getContentPane().setLayout(null);
        getContentPane().setBackground(new Color(248, 251, 253));
        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {

                System.exit(0);
                super.windowClosing(e);
            }
        });
        ImageIcon imageIcon = new ImageIcon(getClass().getResource("/logo.png"));

        this.setIconImage(imageIcon.getImage());
    }
}
