import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class PeerNodeGUI extends JFrame{
		
		private PeerNodeWithGUI peerAgent;
		
		JPanel mainPanel, generatePanel, buttonsPanel;
		JButton generateButton, storeButton, searchButton;
		JTextField dataTF;
		JTextArea logTA;
		JLabel dataLabel;
		
		public PeerNodeGUI(PeerNodeWithGUI myAgent) {
			super(myAgent.getLocalName());
			
			peerAgent = myAgent;
			
			mainPanel = new JPanel();
			BoxLayout boxlayoutY = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);
			mainPanel.setLayout(boxlayoutY);
			
			dataLabel = new JLabel("Enter data or generate");
			dataLabel.setPreferredSize(new Dimension(400, 30));
			
			// generate data chunk
			generatePanel = new JPanel();
			BoxLayout boxlayoutX = new BoxLayout(generatePanel, BoxLayout.X_AXIS);
			generatePanel.setLayout(boxlayoutX);
			
			dataTF = new JTextField("data chunk");
			dataTF.setEditable(true);
			dataTF.setPreferredSize(new Dimension(400, 40));
			
			generateButton = new JButton("GENERATE");
			generateButton.setPreferredSize(new Dimension(200, 40));
			
			generateButton.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent evt) {
	             	peerAgent.generateData();
	            }
	        });
			
			generatePanel.add(dataTF);
			generatePanel.add(generateButton);
			
			//buttons to search and store
			buttonsPanel = new JPanel();
			BoxLayout boxlayoutX2 = new BoxLayout(generatePanel, BoxLayout.X_AXIS);
			generatePanel.setLayout(boxlayoutX2);
			
			searchButton = new JButton("SEARCH");
			searchButton.setPreferredSize(new Dimension(200, 40));
			
			searchButton.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent evt) {
	             	peerAgent.searchData();
	            }
	        });
			
			storeButton = new JButton("STORE");
			storeButton.setPreferredSize(new Dimension(200, 40));
			
			storeButton.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent evt) {
	             	peerAgent.storeData();
	            }
	        });
			
			logTA = new JTextArea();
			logTA.setEditable(true);
			logTA.setPreferredSize(new Dimension(800, 600));
			logTA.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
			
			buttonsPanel.add(searchButton);
			buttonsPanel.add(storeButton);
			
			mainPanel.add(dataLabel);
			mainPanel.add(generatePanel);
			mainPanel.add(buttonsPanel);
			mainPanel.add(logTA);
			

			
			// "super" Frame sets to FlowLayout
			setLayout(new FlowLayout());  
			add(mainPanel);
			setTitle("Peer Agent"); 
		    setSize(850, 800); 					
			
		}
		
		public void showGUI() {
			//	pack();
				Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
				int centerX = (int)screenSize.getWidth() / 2;
				int centerY = (int)screenSize.getHeight() / 2;
				setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
				super.setVisible(true);
			
			}
}
