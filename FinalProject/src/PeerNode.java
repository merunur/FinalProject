import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cern.colt.bitvector.BitVector;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class PeerNode extends Agent {
	
	private int chunksNumberN, chunkSizeX, peersNum;
	private short thresholdT;
	private HashMap<BitVector, int[]> hmap;
//	private HashMap<BitVector, int[]> hmap2;
		
	//launch agent automatically
	protected void setup() {
		System.out.println("Peer Agent "+getAID().getName()+" is ready to create peer agents");
		
		//get arguments from helper node
		Object[] args = getArguments();
		if (args !=null && args.length >= 0) {
			   peersNum = Integer.parseInt((String) args[0]);
			   chunksNumberN = Integer.parseInt((String) args[1]);
			   chunkSizeX = Integer.parseInt((String) args[2]);
			   thresholdT = Short.parseShort((String) args[3]);
	    }
		hmap = new HashMap<BitVector, int[]>(chunksNumberN);
		
		//create N storage locations
		for (int i=0; i<chunksNumberN; i++) {
			BitVector randAddresses = new BitVector(chunkSizeX);
			int counters[]= new int[chunkSizeX];
			
			//generate random addresses and 0 counters
			
			for (int j = 0; j < chunkSizeX; j++) {
				randAddresses.putQuick(j, Math.random() > .5);
				counters[j]=0;
	
			}
			
			//put generated data into hashmap
			hmap.put(randAddresses, counters);	
		}
		
		
	   // Register the master agent service in the yellow pages
//		DFAgentDescription dfd = new DFAgentDescription();
//		dfd.setName(getAID());
//		ServiceDescription sd = new ServiceDescription();
//		sd.setType("peerAgent");
//		sd.setName(getLocalName()+"-Peer Agent");
//		dfd.addServices(sd);
//		try {
//			DFService.register(this, dfd);
//		}
//		catch (FIPAException fe) {
//			fe.printStackTrace();
//		}
		
		addBehaviour(new ReceiveMessage());
		
	}
	
	// called to delete the agent
	protected void takeDown() {

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
	
	public void searchData(String data) {
		System.out.println("we will soon search your data");
		BitVector datatoSearch = new BitVector(data.length());
		int sumResult[] = (int[])new int[datatoSearch.size()];
		// int [] sumResult -> to send back  - sum of all values of counters within threshold
		// iterate through hashmap -> 
		
		Iterator<Map.Entry<BitVector, int[]>> it = hmap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<BitVector, int[]> pairs = it.next();
			int placeHolder[] = (int[])pairs.getValue();
			
			if(thresholdT >= hammingDistance(datatoSearch, (BitVector)pairs.getKey())) {
				for(int i=0; i<datatoSearch.size(); i++ ) {
				//	sumResult[i] 
				}
				pairs.setValue(placeHolder);
			}
			//System.out.println("address: "+getBitVector((BitVector)pairs.getKey())+" value is: "+ pairs.getValue());
		}
		
	}
	
	public void storeData(String data) {
		System.out.println("Data received: "+data);
		BitVector datatoStore = new BitVector(data.length());
		
		for(int i=0; i< data.length(); i++) {
			if(data.charAt(i) == '1') {
				datatoStore.set(i); 
			}
		}
		
		Iterator<Map.Entry<BitVector, int[]>> it = hmap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<BitVector, int[]> pairs = it.next();
			int placeHolder[] = (int[])pairs.getValue();
			
			if(thresholdT >= hammingDistance(datatoStore, (BitVector)pairs.getKey())) {
				for(int i=0; i<datatoStore.size(); i++ ) {
					if(datatoStore.get(i)==true) {
						placeHolder[i]++;
					}
					else {
						placeHolder[i]--;
					}
				}
				pairs.setValue(placeHolder);
			}
			//System.out.println("address: "+getBitVector((BitVector)pairs.getKey())+" value is: "+ pairs.getValue());
		}
		
		
		System.out.println("\n"+getAID().getName());

		Iterator<Map.Entry<BitVector, int[]>> it2 = hmap.entrySet().iterator();
		while (it2.hasNext()) {
			Map.Entry<BitVector, int[]> pairs2 = it2.next();
			int[] counters = (int[])pairs2.getValue();
			System.out.print("\naddress: "+getBitVector((BitVector)pairs2.getKey())+" value is: ");
			for(int i=0; i<datatoStore.size(); i++ ) {
				System.out.print(counters[i]);
			} 
			System.out.println();
		}
		
		
		
	}
	
	public String getBitVector(BitVector v) {
	String s="";
	for (int i= 0; i < v.size(); i++) {
        if (v.get(i)) 
            s+="1";                
        else 
            s+="0";  
    }
	return s;
}
	public int hammingDistance(BitVector toStore, BitVector address) {
		BitVector v1 = toStore.copy();
		BitVector v2 = address.copy();
		v1.xor(v2);	
		return  v1.cardinality();
	}
	
	public class ReceiveMessage extends CyclicBehaviour {

		private String Message_Content;
		private String SenderName;

		public void action() {
			ACLMessage msg = receive();
			if (msg != null) {
				String command = msg.getContent();
				if(msg.getPerformative() == ACLMessage.REQUEST) {
					storeData(command);	
				}
				else {
					searchData(command);
				}
				
				
				//System.out.println("data received at: "+getAID().getLocalName());
				
			} else {
				block();
			}
		}
	}
}
