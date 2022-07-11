package it.polito.tdp.crimes.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import it.polito.tdp.crimes.db.EventsDao;

public class Model {
	
	private List<Integer> anni;
	
	private EventsDao dao;
	
	private Graph<Distretto, DefaultWeightedEdge> grafo;
	
	private List<Distretto> vertici;
	
	public Model() {
		
		this.dao = new EventsDao();
		
		this.anni = this.dao.getAnni();
	}
	
	public String creaGrafo(int anno) {
		
		this.grafo = new SimpleWeightedGraph<Distretto, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		this.vertici = this.dao.getVertici(anno);
		
		Graphs.addAllVertices(this.grafo, this.vertici);
		
		for(Distretto d1 : this.vertici) {
			for(Distretto d2 : this.vertici) {
				if(d1.getId() > d2.getId()) {
					Graphs.addEdgeWithVertices(this.grafo, d1, d2, LatLngTool.distance(d1.getCentro(), d2.getCentro(), LengthUnit.KILOMETER));
				}
			}
		}

		String result = "GRAFO CREATO!\n" + "#VERTICI: " + this.grafo.vertexSet().size() + "\n" + "#ARCHI: " + this.grafo.edgeSet().size()+"\n";
		return result;
	}
	
	public Map<Distretto, List<DistrettoDistanza>>getDistrettiAdiacenti(){
		Map<Distretto, List<DistrettoDistanza>> result = new TreeMap<Distretto, List<DistrettoDistanza>>();
		
		for(Distretto d : this.vertici) {
			List<DistrettoDistanza> adiacenti = new ArrayList<DistrettoDistanza>();
			for(Distretto a : Graphs.neighborListOf(this.grafo, d)) {
				double peso = this.grafo.getEdgeWeight(this.grafo.getEdge(d, a));
				DistrettoDistanza dd = new DistrettoDistanza(a, peso);
				adiacenti.add(dd);
			}
			Collections.sort(adiacenti);
			result.put(d, adiacenti);
		}
		return result;
	}


	public boolean isCreato() {
		if(this.grafo == null)
			return false;
		else
			return true;
	}
	
	public List<Integer> getAnni() {
		return anni;
	}
	
}
