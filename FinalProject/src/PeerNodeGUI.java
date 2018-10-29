import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;

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
			dataLabel.setPreferredSize(new Dimension(200, 30));
			
			// generate data chunk
			generatePanel = new JPanel();
			BoxLayout boxlayoutX = new BoxLayout(generatePanel, BoxLayout.X_AXIS);
			generatePanel.setLayout(boxlayoutX);
			
			dataTF = new JTextField("100");
			dataTF.setPreferredSize(new Dimension(200, 40));
			
			generateButton = new JButton("GENERATE");
			generateButton.setPreferredSize(new Dimension(100, 40));
			
			generatePanel.add(dataTF);
			generatePanel.add(generateButton);
			
			//buttons to search and store
			buttonsPanel = new JPanel();
			BoxLayout boxlayoutX2 = new BoxLayout(generatePanel, BoxLayout.X_AXIS);
			generatePanel.setLayout(boxlayoutX2);
			
			searchButton = new JButton("SEARCH");
			searchButton.setPreferredSize(new Dimension(200, 40));
			
			storeButton = new JButton("STORE");
			storeButton.setPreferredSize(new Dimension(200, 40));
			
			logTA = new JTextArea();
			logTA.setEditable(false);
			logTA.setPreferredSize(new Dimension(400, 40));
			logTA.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
			
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
		    setSize(500, 200); 					
			
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
