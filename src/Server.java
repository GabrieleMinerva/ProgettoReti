import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
public class Server extends Thread {
	private Socket toClient;
	static ArrayList <Oggetto> oggetti = new ArrayList<Oggetto>();
	public static void main(String agr[]){
		try{
			//costruzione delle social network
			ServerSocket server = new ServerSocket(6000);
			for(;;){
				Principale pr = new Principale(server.accept(), oggetti);
				Thread t = new Thread(pr);
				t.start();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
}