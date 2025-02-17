package aitrader.ui.forms;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class FrmSplash extends JFrame
{
	private static final long serialVersionUID = 1L;

	private static FrmSplash frame;

	private JPanel contentPane;
	private JLabel lblLogo;
	private JLabel lblTitle;

	public FrmSplash()
	{
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 380, 240);
		setUndecorated(true);

		// 尝试使用根路径
		URL resourceUrl = FrmSplash.class.getResource("/logo.png");

		if (resourceUrl == null) {
			// 尝试使用相对路径
			resourceUrl = FrmSplash.class.getResource("logo.png");
		}

		if (resourceUrl == null) {
			// 使用 ClassLoader
			resourceUrl = FrmSplash.class.getClassLoader().getResource("logo.png");
		}

		if (resourceUrl != null) {
			Image image = Toolkit.getDefaultToolkit().getImage(resourceUrl);
			setIconImage(image);
		} else {
			System.out.println("Logo image not found.");
		}

		setIconImage(Toolkit.getDefaultToolkit().getImage(FrmSplash.class.getResource("/logo.png")));
		setLocationRelativeTo(null);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setBackground(new Color(20, 29, 45));

		setContentPane(contentPane);
		contentPane.setLayout(null);

		lblLogo = new JLabel(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/aitrader.png"))));
		lblLogo.setBounds(60, 72, 260, 80);
		lblLogo.setForeground(new Color(89, 221, 255));
		lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(lblLogo);

		lblTitle = new JLabel("market");
		lblTitle.setBounds(199, 160, 120, 24);
		lblTitle.setBorder(new EmptyBorder(0, 0, 5, 10));
		lblTitle.setForeground(new Color(200, 200, 200));
		lblTitle.setFont(new Font("Arial", Font.ITALIC, 18));
		lblTitle.setHorizontalAlignment(SwingConstants.RIGHT);
		contentPane.add(lblTitle);
	}

	public static void close()
	{
		frame.setVisible(false);
		frame.dispose();
	}

	public static void launch() throws InvocationTargetException, InterruptedException
	{
		// EventQueue.invokeLater(new Runnable()
		EventQueue.invokeAndWait(new Runnable()
		{
			public void run()
			{
				try
				{
					frame = new FrmSplash();
					frame.lblTitle.setText("unknown");
					frame.setVisible(true);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

}
