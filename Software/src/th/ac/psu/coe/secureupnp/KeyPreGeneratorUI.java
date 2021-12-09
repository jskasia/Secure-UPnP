package th.ac.psu.coe.secureupnp;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import test.KeyPreGenerators;

public class KeyPreGeneratorUI extends JFrame implements Runnable {

	private static int k = 100;
	private static int p = 3571;
	private static String path = "./res/matrix/";

	public static void main(String args[]) {
		Thread mainThread = new Thread(new KeyPreGeneratorUI());
		mainThread.setDaemon(false);
		mainThread.start();
	}

	private JTextField maxNodeNumber = new JTextField("" + k);
	private JTextField prime = new JTextField("" + p);
	private JTextField pathText = new JTextField(path);
	private JButton generateBt = new JButton("Generate");

	public void run() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Maximum number of nodes k:"));
		panel.add(maxNodeNumber);
		
		panel.add(new JLabel("prime p:"));
		panel.add(prime);
		
		panel.add(new JLabel("path: "));
		panel.add(pathText);
		
		panel.add(generateBt);

		generateBt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try{
					keyGen();
				}catch(Exception ex){ ex.printStackTrace(); }

			}
		});
		
		
		
		add(panel);
		setVisible(true);
		setTitle("Key Predistributor");
		setSize(300, 200);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private void keyGen() throws Exception {
		KeyPreGeneratorUI.k = Integer.parseInt(maxNodeNumber.getText());
		System.out.println("Max node number is set to "+KeyPreGeneratorUI.k);
		
		KeyPreGeneratorUI.p = Integer.parseInt(prime.getText());
		System.out.println("Prime number is set to "+KeyPreGeneratorUI.p);
		
		KeyPreGeneratorUI.path = pathText.getText();
		System.out.println("Path is "+KeyPreGeneratorUI.path);
		
		
		Trend t = new Trend(k, p, path);
		t.generateD();
		t.generatePublic("test");
		t.generateSecret("test");
		
		System.out.println("Key generated");
	}

}
