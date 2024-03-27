package hr.fer.oprpp2.chat.predavanja;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;


public class UDPKlijent {

	public static void main(String[] args) 
		throws SocketException, UnknownHostException {
		
		if(args.length!=4) {
			System.out.println("Očekivao sam host port broj1 broj2");
			return;
		}
		
		String hostname = args[0];
		int port = Integer.parseInt(args[1]);

		short broj1 = Short.parseShort(args[2]);
		short broj2 = Short.parseShort(args[3]);

		// Pripremi podatke upita koje treba poslati:
		byte[] podatci = new byte[4];
		ShortUtil.shortToBytes(broj1, podatci, 0);
		ShortUtil.shortToBytes(broj2, podatci, 2);
		
		// Stvori pristupnu točku klijenta:
		DatagramSocket dSocket = new DatagramSocket();

		// Umetni podatke u paket i paketu postavi adresu i 
		// port poslužitelja
		DatagramPacket packet = new DatagramPacket(
			podatci, podatci.length
		);
		packet.setSocketAddress(new InetSocketAddress(
			InetAddress.getByName(hostname),
			port
		));

		
		
		
		
		
		
		
		
		
		
		// Postavi timeout od 4 sekunde na primitak odgovora:
		dSocket.setSoTimeout(4000);

		// Šalji upite sve dok ne dobiješ odgovor:
		boolean answerReceived = false;
		while(!answerReceived) {
			
			// Pošalji upit poslužitelju:
			System.out.println("Šaljem upit...");
			try {
				dSocket.send(packet);
			} catch(IOException e) {
				System.out.println("Ne mogu poslati upit.");
				break;
			}
		
			
			
			
			
			
			
			
			
			
			
			
			
			// Pripremi prazan paket za primitak odgovora:
			byte[] recvBuffer = new byte[4];
			DatagramPacket recvPacket = new DatagramPacket(
				recvBuffer, recvBuffer.length
			);
			
			// Čekaj na odgovor:
			try {
				dSocket.receive(recvPacket);
			} catch(SocketTimeoutException ste) {
				// Ako je isteko timeout, ponovno šalji upit
				continue;
			} catch(IOException e) {
				// U slučaju drugih pogrešaka - dogovoriti se što dalje...
				// (mi opet radimo retransmisiju)
				continue;
			}

			
			
			
			
			
			
			
			
			
			
			// Analiziraj sadržaj paketa:
			if(recvPacket.getLength() != 2) {
				System.out.println(
					"Primljen je odgovor neočekivane duljine."
				);
				break;
			}
			
			// Ispiši rezultat, i prekini slanje retransmisija:
			System.out.println(
				"Rezultat je: " + 
				ShortUtil.bytesToShort(
					recvPacket.getData(), recvPacket.getOffset()
				)
			);
			answerReceived = true;
			//break;
		}

		// Obavezno zatvori pristupnu točku:
		dSocket.close();
	}
	
}




