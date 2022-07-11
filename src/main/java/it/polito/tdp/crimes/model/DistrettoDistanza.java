package it.polito.tdp.crimes.model;

public class DistrettoDistanza implements Comparable<DistrettoDistanza>{
	


	private Distretto d;
	private Double distanza;
	
	public DistrettoDistanza(Distretto d, Double distanza) {
		super();
		this.d = d;
		this.distanza = distanza;
	}

	public Distretto getD() {
		return d;
	}

	public void setD(Distretto d) {
		this.d = d;
	}

	public Double getDistanza() {
		return distanza;
	}

	public void setDistanza(Double distanza) {
		this.distanza = distanza;
	}

	@Override
	public int compareTo(DistrettoDistanza o) {
		return this.distanza.compareTo(o.distanza);
	}
	
	@Override
	public String toString() {
		return d.getId() + " --- " + distanza;
	}




	
	

}
