import java.io.*;
import java.net.Socket;
import java.util.*;

public class Principale implements Runnable {
	

	private Socket channel;
	private static ArrayList<Oggetto> oggetti;
	static BufferedReader br;
	static BufferedWriter bw = null;
	
	public Principale(Socket soc, ArrayList<Oggetto> o) {	
		oggetti = o;
		channel = soc;
	}
	
	// aggiungere clone nel sync
	
	//Metodi Statici
	

	public static String trovaNomeOgg(String line) throws ArrayIndexOutOfBoundsException{				
		//estrapola il nome dell'oggetto dalla string acquista da BufferReader con protocollo GET
		return line.split("nomeOggetto=")[1].split("&").length != 1? line.split("nomeOggetto=")[1].split("&")[0] : line.split("nomeOggetto=")[1].split(" ")[0];
	} 
	public static String trovaPrezzo(String line) throws ArrayIndexOutOfBoundsException{				
		//estrapola il prezzo dalla string acquista da BufferReader con protocollo GET
		return line.split("prezzo=")[1].split("&").length != 1? line.split("prezzo=")[1].split("&")[0] : line.split("prezzo=")[1].split(" ")[0];
	}	
	public static String trovaNomeUtente(String line) throws ArrayIndexOutOfBoundsException{		
		//estrapola il nome dell'utente dalla string acquista da BufferReader con protocollo GET
		return line.split("nomeUtente=")[1].split("&").length != 1? line.split("nomeUtente=")[1].split("&")[0] : line.split("nomeUtente=")[1].split(" ")[0];
	}
	public static ArrayList<String> popolaAmici(){							
		//lettura file txt amicizie
		ArrayList <String> amici = new ArrayList<String>(); 
		try{																			
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("amicizie.txt")));
			String sc=in.readLine();
			while(sc!=null) {		
				amici.add(sc.split("-")[0]);	
				amici.add(sc.split("-")[1]);	
				sc=in.readLine();
			}in.close();
		} 	
		catch (FileNotFoundException e) {	e.printStackTrace();	}				
		catch (IOException e) {		e.printStackTrace();		}
		return amici;
	}
	public static boolean isFriend(ArrayList <String> amicizie, String utente1, String utenete2) {		//determina le amicizie
		for(int i = 0; i < amicizie.size(); i++) {
			if( amicizie.get(i).equals(utente1) && i%2 == 0 && amicizie.get(i+1).equals(utenete2))	return true;
			if( amicizie.get(i).equals(utente1) && i%2 != 0 && amicizie.get(i-1).equals(utenete2))	return true;
		}
		return false;
	}
	public static boolean utenteRegistrato(String s, ArrayList <String> lista) {		//true se l'utente è registrato (quindi abilitato)
		return lista.indexOf(s) != -1;
	}
	public static Oggetto getOggDaNome(String nome, ArrayList <Oggetto> lista) {	
		//estrapola da un array di tipo Oggetto un elemento dal nome.
		
		for(Oggetto x: lista)	if(x.getNome().equals(nome))	return x;
		return new Oggetto("","",0,false);			
		//nel caso in cui l'oggetto richiesto non è presente nell'array in cui lo si sta cercando
		//il ritorno del metodo sarà un tipo Oggetto con prezzo 0	

	}
	public void vendi(String s) {
		//controlla se non ci sono altri oggetti uguali (qualche dubbio)
		synchronized(oggetti) {
			try {
				if(new Oggetto(trovaNomeOgg(s) , "" , 0 , false).presente(oggetti) == 1) { 		
					stamp("HTTP/1.0 400 Errore: \n\n\rprodotto già presente nel catalogo" , bw);
					return;
				}	
			}
			catch(Exception e) {System.out.println("Errore: \n\n\rinserimento dati");}
			try {
				oggetti.add(new Oggetto(trovaNomeOgg(s), trovaNomeUtente(s), Integer.parseInt(trovaPrezzo(s)), true));
			}catch(NumberFormatException e) {
				stamp("Errore: \n\n\rPrezzo non valido", bw);
			}
			stamp("HTTP/1.0 200 OK: \n\n\rprodotto aggiunto al catalogo",bw);
		}
	}	
	public void compra(String s, ArrayList <String> amicizie) {
		synchronized (oggetti) {
			if(isFriend( amicizie , trovaNomeUtente(s) , ( getOggDaNome(trovaNomeOgg(s),oggetti).getVenditore() ) )) {	
				stamp("HTTP/1.0 400 Errore: \n\n\rprodotto non disponibile" , bw);																	
				return;
			}
			Oggetto g = getOggDaNome(trovaNomeOgg(s),oggetti);
			switch(g.presente(oggetti)) {
				case 1: 
					stamp("HTTP/1.0 200 OK: \n\n\rprodotto acquistato" , bw);
				//rende indisponibile l'oggetto appena acquistato
					oggetti.get(oggetti.indexOf(g)).indisponibile();		
					break;			
				case  0:	stamp("HTTP/1.0 400 Errore: \n\n\roggetto non disponibile" , bw);	break;
				case -1:	stamp("HTTP/1.0 400 Errore: \n\n\rnon presente nel catalogo" , bw);	break;
			}
		}
	}	
	public void vediVendite(String s , ArrayList <String> amicizie) {
		synchronized (oggetti) {
			for(Oggetto x : oggetti)
				//se l'utente che richiede la lista vendite viene fatto un controllo sul venditore di ogni oggetto
				//(se è amico di chi effettua la richiesta non stampa)e sulla non disponibilità dello stesso.
				if( x.getVenditore().equals(trovaNomeUtente(s)) && !( x.checkDisp() ) ) 
					stamp(x.toString() , bw);
		}
	}
	public void vediProdotti(String s, ArrayList<String> amicizie) {
		synchronized(oggetti) {
			for(Oggetto x : oggetti) 	
				//se l'utente che richiede il catalogo viene fatto un controllo sul venditore di ogni oggetto
				//(se è amico di chi effettua la richiesta non stampa)e sulla disponibilità dello stesso.
				if( ! (isFriend( amicizie, trovaNomeUtente(s), x.getVenditore() )) && x.checkDisp()) 
					stamp(x.toString() , bw);
		}
	}	
	public static void stamp(String line ,BufferedWriter bw ) {		
		//abbreviazione BufferWriter per stampare su terminale
		try {
			bw.write(line);
			bw.flush();
		}
		catch(Exception e) {}
	}
	
	
	
	//Metodo run

	
	public void run() {
		ArrayList <String> richiesta=new ArrayList<String>();
		try {
			br = new BufferedReader(new InputStreamReader(channel.getInputStream()));
			bw = new BufferedWriter(new OutputStreamWriter(channel.getOutputStream()));
			String linea;
				while((linea=br.readLine()) != null ) {
					richiesta.add(linea);
					if(linea.equals("") | linea == null)
						break;
				}
		}
		catch(Exception e)	{
			System.out.println("errore: \n\n\rrichiesta sul canale non valida");
		}
		String s = richiesta.get(0);
		/*creazione insieme di utenti, i cui, in base all'ordine vengono determinatate le varie
		amicizie tra utenti, questi ultimi sono identificati dal proprio nome contenuto in una variabile String
		*/
		ArrayList <String> amicizie = popolaAmici();
		//controllo richiesta consona
		if(s.startsWith("GET /") && s.substring(s.length()-8, s.length()).equals("HTTP/1.0")) {	
			//controlla se utente è registrato, non verrà fatto controllo successivo
			if (utenteRegistrato(trovaNomeUtente(s), amicizie)){
				//determina che tipo di richiesta è stata fatta e quindi determina quale metodo utilizzare
				String c = s.split("\\?")[0].split("/")[1];			
				switch(c) {							
					case "vendi":
						if(s.split("nomeOggetto=").length==2 && s.split("prezzo=").length==2 && s.split("nomeUtente=").length==2)
							vendi(s);
						else stamp("HTTP/1.0 400 Errore: \n\n\rrichiesta vendi non ben formata.\n\n\rDati non sufficienti" , bw);
						break;
					case "compra":
						//determina se gli acquirenti sono amici		   
						if(s.split("nomeOggetto=").length==2 && s.split("nomeUtente=").length==2)
							compra(s , amicizie);
						else stamp("HTTP/1.0 400 Errore: \n\n\rrichiesta compra non ben formata.\n\n\rDati non sufficienti" , bw);
						break;
					case "vediVendite":
						if(s.split("nomeUtente=").length==2)
							vediVendite(s , amicizie);
						else stamp("HTTP/1.0 400 Errore: \n\n\rrichiesta vediVendite non ben formata.\n\n\rDati non sufficienti" , bw); 
						break;
					case "vediProdotti":
						if(s.split("nomeUtente=").length==2) {
							vediProdotti(s , amicizie);
						}else stamp("HTTP/1.0 400 Errore: \n\n\rrichiesta vediProdotti non ben formata.\n\n\rDati non sufficienti" , bw);
						break;
					default:	stamp("HTTP/1.0 400 Errore:\n\n\rerrore generico" , bw);
				}
			}else stamp("HTTP/1.0 400 Errore: \n\n\rutente non registrato" , bw);	
		}else stamp("HTTP/1.0 400 Errore: \n\n\rprotocollo non valido" , bw);	
		
		try {
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}