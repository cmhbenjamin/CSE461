package chatroom.serverless;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * 
 * @author Benjamin Chan, Nicholas Johnson
 *
 */
public class MulticastClient implements Runnable {
	// TODO: keep track of clients on the same network
	private MulticastSocket mcs;
	private Map<Integer,Message> messages;
	
	public MulticastClient(MulticastSocket mcs) {
		if(mcs != null){
			this.mcs = mcs;
			messages = new TreeMap<Integer,Message>();
		}else{
			System.err.println("Error: socket not found");
		}
	}

	@Override
	public void run() {
		// TODO: create a multicastPacket and display the content
		try {
			DatagramPacket packet = null;
			
			while(ClientRunner.runThreads) {
				byte[] buf = new byte[Packet.MAX_SIZE+Packet.HEADER_LENGTH];
				packet = new DatagramPacket(buf, buf.length);
				mcs.receive(packet);
				//process packet
				Packet received = new Packet(packet.getData());
				// for debugging
				/*System.out.println("Received packet from: " + packet.getAddress().toString() +
						":" + packet.getPort() + " with length: " +
						packet.getLength() +
						" id= " + received.getID()+
						" count = "+ received.getCount()
						);
				*/
				
				// TODO: Find sender
				String sender = "";
				
				if(received.getType() == 0){
					// peer online, add to list
					System.out.println(received.getText()+" has join the chatrooom");
				}else if(received.getType() == 1){
					// peer offline, remove from list
					System.out.println(received.getText()+" has left the chatroom");
				}else if(received.getType() == 2){
					// received message packet, arrange the packet into corresponding message
					if(messages.containsKey(received.getID())){
						messages.get(received.getID()).addPacket(received);

						//check if the message arrived completely
						//assume packet arrives in order
						if(received.getCount()==1){
							ConsoleUI.printReceive(sender,messages.get(received.getID()) );
							messages.remove(received.getID());
						}	
					}else{
						// print out directly
						if(received.getCount() == 1){
							ConsoleUI.printReceive(sender, received.getText());
						}else{
							messages.put(received.getID(), new Message(received));
						}
					}					
				}else if ( received.getType() == 3){
					if(messages.containsKey(received.getID())){
						messages.get(received.getID()).addPacket(received);

						//check if the message arrived completely
						//assume packet arrives in order
						if(received.getCount()==1){
							FileProcessor.write(messages.get(received.getID()));
							messages.remove(received.getID());
						}
						
					}else{
						messages.put(received.getID(), new Message(received));
					}					
				}
			}	
		} catch (IOException e) {
			System.out.println("IOException while receiving: " + e.getMessage());
			e.printStackTrace();
		}	
	}

}
