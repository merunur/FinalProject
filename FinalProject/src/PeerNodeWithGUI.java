import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import javax.swing.JOptionPane;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import cern.colt.bitvector.BitVector;
import java.io.*;
import java.util.Arrays;

public class PeerNodeWithGUI extends Agent{
	
	private PeerNodeGUI peerNodeGUI;
	
	private int chunksNumberN, chunkSizeX, peersNum;
	private short thresholdT;
	private HashMap<BitVector, ArrayList<Integer>> hmap = new HashMap<BitVector, ArrayList<Integer>>();
	private BitVector genData;
	private Vector peerAgents = new Vector();
	private String genDataString="";
	private int searchIterateCounter=0;

	
	//launch agent automatically
	protected void setup() {
		System.out.println("Peer Agent with GUI"+getAID().getName()+" is ready");
		 
		peerNodeGUI = new PeerNodeGUI(this);
		peerNodeGUI.showGUI();
		
		//get arguments from helper node
		Object[] args = getArguments();
		if (args !=null && args.length >= 0) {
		   peersNum = Integer.parseInt((String) args[0]);
		   chunksNumberN = Integer.parseInt((String) args[1]);
		   chunkSizeX = Integer.parseInt((String) args[2]);
		   thresholdT = Short.parseShort((String) args[3]);
	    }
		genData = new BitVector(chunkSizeX);
		//create N storage locations
		for (int i=0; i<chunksNumberN; i++) {
			BitVector randAddresses = new BitVector(chunkSizeX);
			ArrayList<Integer> counters= new ArrayList<Integer>();
			
			//generate random addresses and 0 counters
			for (int j = 0; j < chunkSizeX; j++) {
				randAddresses.putQuick(j, Math.random() > .5);
				counters.add(0);	
			}
			//put generated data into hashmap
			hmap.put(randAddresses, counters);				
		}
			 
	   // Register the peer agent service in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("peerAgent");
		sd.setName(getLocalName()+"-Peer Agent with GUI");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		
		
		addBehaviour(new ReceiveMessage());
		
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

		
	public void generateData() {
		int next_int;
		genDataString="";
		
		//generate data chunk
		for (int i = 0; i < chunkSizeX; i++) {
			genData.putQuick(i, Math.random() > .5);
			if (genData.get(i)) 
				genDataString+="1";                
		      else 
		    	genDataString+="0"; 
		} 
		
		if(genDataString.length()<=65) {
			peerNodeGUI.dataTF.setText(genDataString);
		}
		else {
			peerNodeGUI.dataTF.setText(genDataString.substring(0, 65)+"...");
			
		}
		
		
	
		
		
        //peerNodeGUI.logTA.append("map size is : "+hmap.size()+"\n");      //just to check the hashmap size	
	}
	
	
	public void searchData() {
		addBehaviour(new OneShotBehaviour() {

			@Override
			public void action() {
				searchIterateCounter=0;
				if (0 != genData.size()) {
					int counter = peersNum;
					for (int i=1; i<counter; i++) {
						ACLMessage msgACL;
						String guiagent = "PeerAgentWithGUI";
						if (i==1 && getAID().getName()==guiagent) {
							msgACL = createMessage(ACLMessage.PROPOSE, genDataString, new AID("PeerAgentWithGUI", AID.ISLOCALNAME));
							// send(msgACL);
						}
						else {
							msgACL = createMessage(ACLMessage.PROPOSE, genDataString, new AID("PeerAgent" + (counter-i), AID.ISLOCALNAME));
							send(msgACL);
					
							}	
					}
					peerNodeGUI.logTA.append("\nMessage sent\n");
						
						
				} 
				else {
		
					JOptionPane.showMessageDialog(peerNodeGUI, "No data to store!", "WARNING", JOptionPane.WARNING_MESSAGE);
				}
				
			}
				
			private ACLMessage createMessage (int mp, String content, AID dest) {
				ACLMessage msgACL;
				msgACL = new ACLMessage(mp);
				msgACL.setContent(content);
				msgACL.addReceiver(dest);
				
				return msgACL;
			}
		});
	}
	
	
	public void storeData() {
		//store genData
		addBehaviour(new OneShotBehaviour() {

			@Override
			public void action() {
				//String s = genData.toString() ;
				//genDataString = peerNodeGUI.dataTF.getText();
		
				if (0 != genData.size()) {
					int counter = peersNum;
					
					for (int i=1; i<counter; i++) {
						ACLMessage msgACL;
						String guiagent = "PeerAgentWithGUI";
						if (i==1 && getAID().getName()==guiagent) {
							 msgACL = createMessage(ACLMessage.REQUEST, genDataString, new AID("PeerAgentWithGUI", AID.ISLOCALNAME));
							// send(msgACL);
						}
						else {
							 msgACL = createMessage(ACLMessage.REQUEST, genDataString, new AID("PeerAgent" + (counter-i), AID.ISLOCALNAME));
							 send(msgACL);
					 
							}	
					}
					peerNodeGUI.logTA.append(genDataString+"\n");
					
				
					
				} 
				else {
		
					JOptionPane.showMessageDialog(peerNodeGUI, "No data to store!", "WARNING", JOptionPane.WARNING_MESSAGE);
				}
				
			}
				
			private ACLMessage createMessage (int mp, String content, AID dest) {
				ACLMessage msgACL;
				msgACL = new ACLMessage(mp);
				msgACL.setContent(content);
				msgACL.addReceiver(dest);
				
				return msgACL;
				}
		});	
	}
	
	public class ReceiveMessage extends CyclicBehaviour {
		private int step = 0;
		private int repliesCounter = 0;	
		private int[] replyIntArray = new int[chunkSizeX];
		private int[] sumVector = new int[chunkSizeX];
		private int storedChunksCounter;
		

		public void action() {
			BitVector binary = new BitVector(chunkSizeX);
			BitVector bitStrSearch = genData;
			String bitToString;
			
			switch (step) {
				case 0:
					ACLMessage msg = receive();
					if (msg != null) {
						String content = msg.getContent();
						//System.out.println("Peer["+repliesCounter+"] sent:\t"+content);
						
						if (msg.getPerformative() == ACLMessage.INFORM) {
							
							replyIntArray = convertStringToArray(content);
							//System.out.println("Convert to int and add to SumVector: ");
							for (int i=0; i<sumVector.length; i++) {
								sumVector[i] += replyIntArray[i];
								//System.out.print(sumVector[i]+"\t");
							}
							repliesCounter++;
							if (repliesCounter >= peersNum-1) {
								step = 1;
							}
							//System.out.println("\nProgress to step 1");
							
							
						} else if (msg.getPerformative() == ACLMessage.CONFIRM){
							repliesCounter++;
							storedChunksCounter += Integer.parseInt(content);
							if (repliesCounter >= peersNum-1) {
								peerNodeGUI.logTA.append("Stored: ["+storedChunksCounter+"/"+(peersNum-1)*chunksNumberN+"]\n");
								step = 0;
								repliesCounter = 0;
								storedChunksCounter = 0;
							}
						}
				
					} else {
						block();
					}
					break;
				case 1:
					//System.out.println("All replies received, time to process them!");
					
					// convert sumvector to binary format:
					//System.out.print("SUMVECTOR in binary: ");
					for (int i=0; i< sumVector.length; i++) {
						if (sumVector[i] > 0) {
							binary.set(i);
						} else{
							binary.clear(i);
						}
						
					}
					//System.out.println(getBitVectorStr(binary));
					
					//System.out.println("\nHalf of Chunksize is "+chunkSizeX/2+" HammingDist is: " + hammingDistance(binary, bitStrSearch));
					if (hammingDistance(binary, bitStrSearch) <= (chunkSizeX/2) && hammingDistance(binary, bitStrSearch)>0 && searchIterateCounter < chunksNumberN ) {
						
						peerNodeGUI.logTA.append(".");
						// sent msg to all peers
						int counter = peersNum;
						for (int i=1; i<counter; i++) {
							ACLMessage msgACL;
							String guiagent = "PeerAgentWithGUI";
							bitToString = getBitVectorStr(binary);
							if (i==1 && getAID().getName()==guiagent) {
								msgACL = createMessage(ACLMessage.PROPOSE, bitToString, new AID("PeerAgentWithGUI", AID.ISLOCALNAME));
								// send(msgACL);
							}
							else {
								msgACL = createMessage(ACLMessage.PROPOSE, bitToString, new AID("PeerAgent" + (counter-i), AID.ISLOCALNAME));
								send(msgACL);
						
								}	
						}
						bitStrSearch = binary;
						searchIterateCounter++;
					}
					else if(hammingDistance(binary, bitStrSearch) == 0) {
						peerNodeGUI.logTA.append("yes, we have your data\n");

						//System.out.println("sorry, we couldnt find your data");
						
					}
					else {
						//System.out.println("yes, we have your data");
						peerNodeGUI.logTA.append("\nsorry, we couldnt find your data\n");

					}
					
					bitStrSearch = genData;
					for (int j = 0; j<sumVector.length; j++) {
						sumVector[j] = 0;
					}
					step = 0;
					repliesCounter = 0;
					break;
			}
			
		}
		public String getBitVectorStr(BitVector v) {
			String s="";
			for (int i= 0; i < v.size(); i++) {
				if (v.get(i)) 
					s+="1";                
				else 
					s+="0";  
			}
			return s;
		}
		private ACLMessage createMessage (int mp, String content, AID dest) {
			ACLMessage msgACL;
			msgACL = new ACLMessage(mp);
			msgACL.setContent(content);
			msgACL.addReceiver(dest);
			
			return msgACL;
			}
		
		public int hammingDistance(BitVector toStore, BitVector address) {
			BitVector v1 = toStore.copy();
			BitVector v2 = address.copy();
			v1.xor(v2);	
			return  v1.cardinality();
		}


		public int[] convertStringToArray(String text) {
			String cleanTxt = cleanString(text);
			String[] splitStrings = cleanTxt.split(",");
			int counter = 0;
			
			int[] intarray = new int[splitStrings.length];
			int i=0;
			for(String str:splitStrings){
				try {
					intarray[i]=Integer.parseInt(str);
					i++;
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException("Not a number: " + str + " at index " + i, e);
				}
			}
			return intarray;
			
		}

		public String cleanString(String toClean) {
			String newStr = "";
			for (int i=0; i<toClean.length(); i++) {
				if ((toClean.charAt(i) != '[') && (toClean.charAt(i) != ']') && (toClean.charAt(i) != ' ')){
					newStr += toClean.charAt(i);
				}
			}
			return newStr;
		}
		
	}

}
 