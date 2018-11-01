import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

import java.util.ArrayList;

import jade.core.AID;
import jade.lang.acl.ACLMessage;


public class HelperNode extends Agent {
	
	// variable for storing the global GUI
	private HelperNodeGUI helperNodeGUI;
	private int peersNumber = 0;
	public ArrayList<AID> helperList;
	private String ip = "172.20.10.2";
	private int localAgentsNum = 2000;
	
	
	//launch agent automatically
	protected void setup() {
		System.out.println("Helper Agent "+getAID().getName()+" is ready to create peer agents");
		 
		helperNodeGUI = new HelperNodeGUI(this);
		helperNodeGUI.showGUI();
		 
		// Register the master agent service in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("helperAgent");
		sd.setName(getLocalName()+"-Helper Agent");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		helperList = new ArrayList<AID>();
		
		   addBehaviour(new TickerBehaviour( this, 5000 ) {
    	protected void onTick() {
    		refreshActiveAgents();
           // System.out.println("number of Helper Agents in remote platform: "+helperList.size());
        }
    	
    });
		
		
		
		
		
	}
	
	// called to delete the agent
	protected void takeDown() {
		// Dispose the GUI if it is there
		if (helperNodeGUI != null) { 
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
		int peersNumInt = Integer.parseInt(peersNum);
	
		jade.core.Runtime runtime = jade.core.Runtime.instance();
		Profile profile = new ProfileImpl();
		profile.setParameter(Profile.MAIN_HOST, "localhost");
		AgentContainer container = runtime.createAgentContainer(profile);
        AgentController ag;
        
        if(peersNumInt < localAgentsNum) {
       	 for (int i = 1; i < peersNumInt; i++) {
    			try {
    				ag = container.createNewAgent("PeerAgent" +i, "PeerNode", new Object[] {peersNum, chunksNum, chunkSize, threshold});
    				ag.start();
    		
    			} catch (StaleProxyException ex) {
    				ex.printStackTrace();
    			}
            }
            
            try {
    			ag = container.createNewAgent("PeerAgentWithGUI", "PeerNodeWithGUI", new Object[] {peersNum, chunksNum, chunkSize, threshold});
    			ag.start();
    	
    		} catch (StaleProxyException e) {
    			e.printStackTrace();
    		}
       	
       }
       else {
       	for (int i = 1; i < localAgentsNum; i++) {
    			try {
    				ag = container.createNewAgent("PeerAgent" + i, "PeerNode", new Object[] {peersNum, chunksNum, chunkSize, threshold});
    				ag.start();
    		
    			} catch (StaleProxyException ex) {
    				ex.printStackTrace();
    			}
            }
            
            try {
    			ag = container.createNewAgent("PeerAgentWithGUI", "PeerNodeWithGUI", new Object[] {peersNum, chunksNum, chunkSize, threshold});
    			ag.start();
    	
    		} catch (StaleProxyException e) {
    			e.printStackTrace();
    		}
            
            ACLMessage msgACL = new ACLMessage(ACLMessage.CFP);
  
					AID remoteAgent = new AID();
					remoteAgent.setName("Flo@172.20.10.2:1099/JADE");
					remoteAgent.addAddresses("http://172.20.10.2:7778/acc");  
					msgACL.setContent((peersNumInt-localAgentsNum)+":"+chunksNum+":"+chunkSize+":"+threshold);
					msgACL.addReceiver(remoteAgent);
					send(msgACL);
				
    		
            
            
       }
       
        peersNumber += Integer.parseInt(peersNum);
	}
	
	 public void refreshActiveAgents(){
		 //  System.out.println("scanning remote host");
		 
	        //clearing list in GUI
		    helperList.clear();
		    
		 // Update the list of seller agents
	        DFAgentDescription templateFirst = new DFAgentDescription();
	        ServiceDescription sdFirst = new ServiceDescription();
	        sdFirst.setType("peerAgent");
	        templateFirst.addServices(sdFirst);
	        try {
	          DFAgentDescription[] result = DFService.search(this, templateFirst);
	          for (int i = 0; i < result.length; ++i) {
	        	  AID agentID = result[i].getName();
	              helperList.add(agentID);
	          }
	        }
	        catch (FIPAException fe) {
	          fe.printStackTrace();
	        }

	        DFAgentDescription template = new DFAgentDescription();

	        AID otherPlatform = new AID();
	        otherPlatform.setName("df@"+ip+":1099/JADE");
	        otherPlatform.addAddresses("http://"+ip+":7778/acc");

	        ServiceDescription sd = new ServiceDescription();
	        sd.setType("peerAgent");
	        template.addServices(sd);

					
	        try {
	            DFAgentDescription[] result = DFService.search(this, otherPlatform, template);
	            for (int i = 0; i < result.length; i++) {
	                AID agentID = result[i].getName();
	                helperList.add(agentID);
	            }
	        } catch (FIPAException e) {
	            e.printStackTrace();
	        }
	    }

	public void stopSystem() {
		
		// broadcast messages to all peers to shut down gracefully
		for (int i = 0; i < helperList.size(); i++) {
			ACLMessage msgACL = new ACLMessage(ACLMessage.FAILURE);
           //  System.out.println("helperListItem"+i+" "+helperList.get(i));
			msgACL.addReceiver(helperList.get(i));
			send(msgACL);
		}
		
		// send the shutdown to the peer with GUI as well
	//	msgACL.addReceiver(new AID("PeerAgentWithGUI", AID.ISLOCALNAME));
		//send(msgACL);

		// fix buttons
		helperNodeGUI.stopButton.setEnabled(false);
		helperNodeGUI.logTA.setText("System stopped");
		helperNodeGUI.startButton.setEnabled(true);

		peersNumber = 0;
	}
	

}
