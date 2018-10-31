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
import java.util.Arrays;
import jade.core.AID;
import java.util.Random;



public class PeerNode extends Agent {
	
	private int chunksNumberN, chunkSizeX, peersNum;
	private short thresholdT;
	private HashMap<BitVector, int[]> hmap;

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


	public int[] generateFakeRandomData() {
		int[] returnArray = new int[chunkSizeX];
		Random r = new Random();
		int low = -10;
		int high = 10;
		for (int i=0; i<chunkSizeX; i++) {
			returnArray[i] = r.nextInt(high-low) + low;
		}
		return returnArray;
	}

	public int[] searchData(String data) {
		BitVector bitString = new BitVector(data.length());
		int[] sumResult = new int[bitString.size()]; // -> to send back  - sum of all values of counters within threshold
		
		// iterate through hashmap -> 
		Iterator<Map.Entry<BitVector, int[]>> it = hmap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<BitVector,int[]> pairs = it.next();
			BitVector currentAddress = (BitVector)pairs.getKey();
			int[] countersArray;
			
			if(thresholdT >= hammingDistance(bitString, currentAddress)) {
				countersArray = (int[])pairs.getValue();
				for(int i=0; i<bitString.size(); i++ ) {
					sumResult[i] += countersArray[i];
				}
			}
		}
		return sumResult;
	}
	
	public int storeData(String data) {
		//System.out.println("Data received: "+data);
		BitVector datatoStore = new BitVector(data.length());
		int storedChunksCounter = 0;
		
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
				storedChunksCounter++;
			}
			//System.out.println("address: "+getBitVector((BitVector)pairs.getKey())+" value is: "+ pairs.getValue());
		}
		
		
		//System.out.println("\nPeer said he was: "+getAID().getName() + " and stored: [" + storedChunksCounter +"/"+chunksNumberN+"] chunks");
//		Iterator<Map.Entry<BitVector, int[]>> it2 = hmap.entrySet().iterator();
//		while (it2.hasNext()) {
//			Map.Entry<BitVector, int[]> pairs2 = it2.next();
//			int[] counters = (int[])pairs2.getValue();
//			System.out.print("\naddress: "+getBitVector((BitVector)pairs2.getKey())+" value is: ");
//			for(int i=0; i<datatoStore.size(); i++ ) {
//				System.out.print(counters[i]);
//			} 
//			System.out.println();
//		}
		return storedChunksCounter;
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
			int[] searchResult = new int[chunkSizeX];
			int counterStoredChunks;

			ACLMessage msg = receive();
			if (msg != null) {
				counterStoredChunks=0;
				String command = msg.getContent();
				if(msg.getPerformative() == ACLMessage.REQUEST) {
					counterStoredChunks = storeData(command);
					ACLMessage replyToStore = createMessage(ACLMessage.CONFIRM, counterStoredChunks+"", msg.getSender());
					send(replyToStore);
				}
				else if (msg.getPerformative() == ACLMessage.PROPOSE) {
					//System.out.println("Peer received command to search for: " + command);
					searchResult = searchData(command);
					ACLMessage replyToSearchQuery = createMessage(ACLMessage.INFORM, Arrays.toString(searchResult), msg.getSender());
					send(replyToSearchQuery);
					//System.out.println("data to search: "+ command);
				}
	
			} else {
				block();
			}
		}


		private ACLMessage createMessage (int mp, String content, AID dest) {
			ACLMessage msgACL;
			msgACL = new ACLMessage(mp);
			msgACL.setContent(content);
			msgACL.addReceiver(dest);
			
			return msgACL;
		}

	}
}
