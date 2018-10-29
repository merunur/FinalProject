import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

public class HelperNode extends Agent {
	
	private HelperNodeGUI helperNodeGUI;
	private int peersNumber = 0;
	
	//launch agent automatically
	protected void setup() {
		System.out.println("Helper Agent "+getAID().getName()+" is ready to create peer agents");
		 
		helperNodeGUI = new HelperNodeGUI(this);
		helperNodeGUI.showGUI();
		 
		// Register the master agent service in the yellow pages
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			ServiceDescription sd = new ServiceDescription();
			sd.setType("helper-agent");
			sd.setName(getLocalName()+"-Helper Agent");
			dfd.addServices(sd);
			try {
				DFService.register(this, dfd);
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
			}
		
	}
	
	// called to delete the agent
	protected void takeDown() {
		
		if (helperNodeGUI != null) { // Dispose the GUI if it is there
			helperNodeGUI.dispose();
		}
		
		// Deregister agent from the Directory Facilitator 
		try {
			DFService.deregister(this);
			System.out.println("Helper-agent "+getAID().getName()+" has been signed off.");
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		// Printout a dismissal message
		System.out.println("Helper-agent "+getAID().getName()+"terminated.");
	}
	
	
	
	public void CreatePeerAgents(String peersNum,String chunksNum,String chunkSize,String threshold) throws ControllerException {
		
		helperNodeGUI.startButton.setEnabled(false);
		helperNodeGUI.logTA.setText("System started");
		helperNodeGUI.stopButton.setEnabled(true);
	
//		jade.core.Runtime runtime = jade.core.Runtime.instance();
//		Profile profile = new ProfileImpl();
//		profile.setParameter(Profile.MAIN_HOST, "localhost");
//		AgentContainer container = runtime.createAgentContainer(profile);
//        AgentController ag;
//        
        
//        for (int i = 0; i < Integer.parseInt(peersNum); i++) {
//			try {
//				ag = container.createNewAgent("PeerAgent" + (peersNumber+i), 
//				                               "PeerAgent", 
//				                               new Object[] {chunksNum, chunkSize, threshold});
//				ag.start();
//		
//			} catch (StaleProxyException e) {
//				e.printStackTrace();
//			}
//        }
       
        peersNumber += Integer.parseInt(peersNum);
	}
	
	public void stopSystem() {
		
		helperNodeGUI.stopButton.setEnabled(false);
		helperNodeGUI.logTA.setText("System stopped");
		helperNodeGUI.startButton.setEnabled(true);
		
	}

}
