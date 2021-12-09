package test;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class KeyPreGenerators extends JFrame implements Runnable{
	
	public static void main(String args[]){
		Thread mainThread = new Thread( new KeyPreGenerators() );
		mainThread.setDaemon( false );
		mainThread.start();
	}
	
	private JTextField maxNodeNumber = new JTextField("1000");
	private JTextField prime = new JTextField("997");
	private JButton generateBt = new JButton("Generate");
	
	
	public void run(){
		JPanel panel = new JPanel( new FlowLayout(FlowLayout.LEFT));
		panel.add( new JLabel("Maximum number of nodes k:") );
		panel.add(maxNodeNumber);
		panel.add( new JLabel("prime p:") );
		panel.add(prime);
		panel.add(generateBt);
		
		
		generateBt.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				keyGen();
				
			}
		});
		
		add(panel);
		setVisible(true);
		setTitle("Key Predistributor");
		setSize(400,400);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	private static void keyGen(){
		//System.out.println("Hello Keygen");
		
	}

}
