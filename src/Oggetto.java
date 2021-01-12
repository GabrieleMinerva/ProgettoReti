
import java.util.ArrayList;

public class Oggetto{
	
	String nome , venditore;		//nome oggetto(univoco) , nome del venditore
	int prezzo;						//prezzo
	boolean valido;					//disponibilita' oggetto
	
	public Oggetto(String nom, String vend, int prezz, boolean disp) { 	nome = nom; prezzo=prezz; valido = disp; venditore = vend;	}
	public void indisponibile()			 {	this.valido = false;	}		//rende oggetto indisponibile
	public void disponibile() 			 {	this.valido = true;		}		// rende oggetto disponibile
	public boolean equals(Oggetto g)	 {	return this.nome.equals(g.nome);	}
	public String getNome() 			 {	return nome;	}			// ritorna nome oggetto
	public String getVenditore() 		 {	return venditore;	}		//ritorna nome venditore
	public String toString() 			 {	return nome+" "+prezzo+" "+venditore+" "+((valido)? "disponibile\n\r" : "non disponibile\n\r");	}
	public boolean checkDisp() {	return valido;	}		//true se disponibile false altrimenti
	public int presente(ArrayList<Oggetto> lista) {		
		//ritorna -1 , 0 , 1
		for(Oggetto g: lista)  if(g.getNome().equals(this.getNome()) && g.valido) return 1;		//prodotto presente e disponibile nel catalogo
		for(Oggetto g: lista)  if(g.getNome().equals(this.getNome()) && !g.valido) return 0; 	//prodotto presente ma non disponibile
		return -1;																			//prodotto non presente
	}
}
