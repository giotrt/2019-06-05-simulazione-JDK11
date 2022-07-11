package it.polito.tdp.crimes.model;

import java.time.LocalDateTime;

public class Evento implements Comparable<Evento>{
	
	public enum EventType{
		CRIMINE,
		ARRIVA_AGENTE, 
		GESTITO
	}
	
	private EventType tipo;
	private LocalDateTime data;
	private Event crimine;
	
	public Evento(EventType tipo, LocalDateTime data, Event crimine) {
		super();
		this.tipo = tipo;
		this.data = data;
		this.crimine = crimine;
	}

	public EventType getTipo() {
		return tipo;
	}

	public LocalDateTime getData() {
		return data;
	}

	public Event getCrimine() {
		return crimine;
	}

	@Override
	public int compareTo(Evento o) {
		return this.data.compareTo(o.getData());
	}
	
	
	
	
	
}
