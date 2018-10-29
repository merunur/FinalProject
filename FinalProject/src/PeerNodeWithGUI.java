import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class PeerNodeWithGUI extends Agent{
	
	private PeerNodeGUI peerNodeGUI;
	
	
	//launch agent automatically
	protected void setup() {
		System.out.println("Helper Agent "+getAID().getName()+" is ready to create peer agents");
		 
		peerNodeGUI = new PeerNodeGUI(this);
		peerNodeGUI.showGUI();
		 
		   // Register the master agent service in the yellow pages
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			ServiceDescription sd = new ServiceDescription();
			sd.setType("peer-agent");
			sd.setName(getLocalName()+"-Peer Agent");
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
			
			if (peerNodeGUI != null) { // Dispose the GUI if it is there
				peerNodeGUI.dispose();
			}
			
			// Deregister agent from the Directory Facilitator 
			try {
				DFService.deregister(this);
				System.out.println("Peer-agent "+getAID().getName()+" has been signed off.");
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
			}
			// Printout a dismissal message
			System.out.println("Peer-agent "+getAID().getName()+"terminated.");
		}

		
	public void generateData() {}
	
	public void searchData() {}
	
	public void storeData() {}
}
