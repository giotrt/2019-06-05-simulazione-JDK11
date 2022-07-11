package it.polito.tdp.crimes.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

import it.polito.tdp.crimes.db.EventsDao;
import it.polito.tdp.crimes.model.Evento.EventType;

public class Simulatore {
	
//	Tipi di evento
//	1. Evento criminoso
//		1.1 La centrale selezione l'agente più vicino
//		1.2 Calcolo il cammino minino tra tutti i vertici che ganno degli agenti liberi
//			e il vertici in cui è avvenuto il crimine
//		1.3 Setta l'agente occupato
//	2. L'agente arriva dove c'è il crimine
//		2.1 Definisco quanto durerà l'intervento
//		2.2 Controlla se è mal gestito
//	3. Crimine terminato
//		3.1 Libero l'agente
	
	
//	OSSERVAZIONE PUNTO 1.2:
//		DATO CHE TUTTI I VERTICI SONO COLLEGATI TRA DI LORO (TUTTI CON TUTTI)
//		VALE LA DISUGUAGLIANZA TRIANGOLARE ED E' QUINDI SEMPRE PIU' CONVENIENTE
//		SFRUTTARE L'ARCO DIRETTO PER ANDARE DA UN DISTRETTO AD UN ALTRO. QUINDI 
//		NON E' NECESSARIO CALCOLAR IL CAMMINO MINIMO MA BASTA VEDERE TUTTI I DISTREII ADIACENTI
//		E SCEGLIERE QUELLO CON PESO MINIMO OGNI VOLTA

//	Strutture dati che ci servono
	
	private PriorityQueue<Evento> coda;
	
//	output
	private int nMalGestiti;
	
//	input
	private int N;
	private int anno;
	private int mese;
	private int giorno;
	
//	parametri
	private Map<Integer, Integer> agenti; // mappa distretto-#agenti
	private Graph<Distretto, DefaultWeightedEdge> grafo;
	private Map<Integer, Distretto> idMap;
	
	
	public Simulatore(Graph<Distretto, DefaultWeightedEdge> grafo, Map<Integer, Distretto> idMap) {
		 this.grafo = grafo;
		 this.idMap = idMap;
	}
	
	public void init(int N, int anno, int mese, int giorno) {
		
		
		this.coda = new PriorityQueue<Evento>();
		
		this.N = N;
		this.anno = anno;
		this.mese = mese;
		this.giorno = giorno;
		
		this.nMalGestiti = 0;
		
		this.agenti = new HashMap<Integer, Integer>();
		
//		metto a zero tutti gli agenti nei distretti 
		for(Distretto d : this.grafo.vertexSet()) {
			this.agenti.put(d.getId(), 0);
		}
		
//		trovo il distretto con minor criminalità e cosi trovo la centrale dove poter mettere inizialmente
//		tutti gli agenti.
//		Il distretto con minor criminalità è quello dove ci sono stati meno crimini nell'anno:
//			in pratica selezione gli id dei distretti filtrati per l'anno in questione,li ragruppo, faccio
//			il COUNT(*), ordino per count ascendente e faccio LIMIT 1 --> mi prende solo la prima
		
		EventsDao dao = new EventsDao();
		
		Integer minID = dao.getDistrettoMin(this.anno);
		
//		ci metto tutti gli agenti ora
		
		this.agenti.put(minID, this.N);
		
//		Imposto gli eventi iniziali ovvero tutti i crimini di quell'anno
		
		for(Event e : dao.listAllEventsByDate(this.anno, this.mese, this.giorno)) {
			coda.add(new Evento(EventType.CRIMINE, e.getReported_date(), e));
		}
	}
	
	public void run() {
		while(!coda.isEmpty()) {
			Evento e = coda.poll();
			processEvent(e);
		}
		
	}

	private void processEvent(Evento e) {
		LocalDateTime data = e.getData();
		EventType tipo = e.getTipo();
		Event crimine = e.getCrimine();
		switch(tipo) {
		
			case CRIMINE:
				
				System.out.println("NUOVO CRIMINE: "+ crimine.getIncident_id());

				
				Integer partenza = null;
				
				partenza = cercaAgente(crimine.getDistrict_id());
				
				if(partenza != null) {
					//c'è un agente libero
					if(partenza.equals(crimine.getDistrict_id())) {
						//tempo di arrivo = 0
						System.out.println("AGENTE ARRIVA PER CRIMINE: "+ crimine.getIncident_id());
						if(crimine.getOffense_category_id().compareTo("all_other_crimes")==0) {
							int ore = (int)(Math.random()+1);
							this.coda.add(new Evento(EventType.GESTITO, crimine.getReported_date().plusHours(ore), crimine));
						}else {
							this.coda.add(new Evento(EventType.GESTITO, crimine.getReported_date().plusHours(2), crimine));
						}
					}else {
						//schedulo evento ARRIVA_AGENTE
						double distanza = this.grafo.getEdgeWeight(this.grafo.getEdge(idMap.get(partenza), idMap.get(crimine.getDistrict_id())));
						Long secondi = (long)((distanza*100)/(60/3.6)); //da km/h a m/s
						this.coda.add(new Evento(EventType.ARRIVA_AGENTE, data.plusSeconds(secondi), crimine));
					}
				}else{
					//nessuno libero
					System.out.println("CRIMINE: " + crimine.getIncident_id() + " MAL GESTITO");
					this.nMalGestiti++;
				}
				//CASO I: se non c'è nessun agente libero allora crimine mal gestito
				
				//CASO II: c'è l'agente libero nello stesso distretto del crimine e quindi ci mette 0 e quindi
				//non serve schedulare l'evento ARRIVA_AGENTE
				
				//CASO II: normale
				
				break;
			case ARRIVA_AGENTE:
				System.out.println("AFENTE ARRIVA PER CRIMINE: "+ crimine.getIncident_id());
				if(crimine.getOffense_category_id().compareTo("all_other_crimes")==0) {
					int ore = (int)(Math.random()+1);
					this.coda.add(new Evento(EventType.GESTITO, crimine.getReported_date().plusHours(ore), crimine));
				}else {
					this.coda.add(new Evento(EventType.GESTITO, crimine.getReported_date().plusHours(2), crimine));
				}
				
				//controllo se l'evento è mal gestito
				if(data.isAfter(crimine.getReported_date().plusMinutes(15))) {
					System.out.println("CRIMINE: " + crimine.getIncident_id() + " MAL GESTITO");
					this.nMalGestiti++;
					
				}
				break;
			case GESTITO:
				// quando un crimine è stato gestito semplicemente liberiamo l'agente andando ad aumentare di uno 
				// il numero di agenti in quel distretto.
				// se il primo valore era zero è perchè fino a quel momento non c'erano agenti che erano andati in
				// quel distretto. Dato che uno è andato e ha gestito il crimine aumentiamo di uno.
				// Non sappiamo se è stato mal gestito o ben gestito.
				System.out.println("CRIMINE " +crimine.getIncident_id() + " GESTITO!");
				this.agenti.put(crimine.getDistrict_id(), this.agenti.get(crimine.getDistrict_id())+1);
				break;
		}
	}

	private Integer cercaAgente(Integer district_id) {
		Double distanza = Double.MAX_VALUE;
		Integer distretto = null;
		
		for(Integer d : this.agenti.keySet()) {
			if(this.agenti.get(d)>0) {
				if(district_id.equals(d)) {
					distanza = Double.valueOf(0);
					distretto = d;
				}
				if(this.grafo.getEdgeWeight(this.grafo.getEdge(this.idMap.get(district_id), this.idMap.get(d))) < distanza) {
					distanza = this.grafo.getEdgeWeight(this.grafo.getEdge(this.idMap.get(district_id), this.idMap.get(d)));
					distretto = d;
				}
			}
		}
		return distretto;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
