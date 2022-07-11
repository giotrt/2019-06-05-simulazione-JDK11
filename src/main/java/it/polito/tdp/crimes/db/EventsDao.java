package it.polito.tdp.crimes.db;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.javadocmd.simplelatlng.LatLng;

import it.polito.tdp.crimes.model.Distretto;
import it.polito.tdp.crimes.model.Event;



public class EventsDao {
	
	public List<Event> listAllEventsByDate(int anno, int mese, int giorno){
		String sql = "SELECT * FROM events "
				+ "WHERE YEAR(reported_date)=? "
				+ "AND MONTH(reported_date)=? AND DAY(reported_date)=?" ;
		try {
			Connection conn = DBConnect.getConnection() ;

			PreparedStatement st = conn.prepareStatement(sql) ;
			
			st.setInt(1, anno);
			st.setInt(2, mese);
			st.setInt(3, giorno);
			
			List<Event> list = new ArrayList<>() ;
			
			ResultSet res = st.executeQuery() ;
			
			while(res.next()) {
				try {
					list.add(new Event(res.getLong("incident_id"),
							res.getInt("offense_code"),
							res.getInt("offense_code_extension"), 
							res.getString("offense_type_id"), 
							res.getString("offense_category_id"),
							res.getTimestamp("reported_date").toLocalDateTime(),
							res.getString("incident_address"),
							res.getDouble("geo_lon"),
							res.getDouble("geo_lat"),
							res.getInt("district_id"),
							res.getInt("precinct_id"), 
							res.getString("neighborhood_id"),
							res.getInt("is_crime"),
							res.getInt("is_traffic")));
				} catch (Throwable t) {
					t.printStackTrace();
					System.out.println(res.getInt("id"));
				}
			}
			
			conn.close();
			return list ;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null ;
		}
	}

	public List<Integer> getAnni() {
		String sql = "SELECT distinct YEAR(reported_date) AS anno "
				+ "FROM `events` "
				+ "ORDER BY anno";
		try {
			Connection conn = DBConnect.getConnection() ;

			PreparedStatement st = conn.prepareStatement(sql) ;
			
			List<Integer> list = new ArrayList<>() ;
			
			ResultSet res = st.executeQuery() ;
			
			while(res.next()) {
				
				list.add(res.getInt("anno"));
			}
			
			conn.close();
			return list ;

		} catch (SQLException e) {
			e.printStackTrace();
			return null ;
		}	
	}

	public List<Distretto> getVertici(int anno, Map<Integer, Distretto> idMap) {
		String sql = "SELECT district_id AS id, AVG(geo_lat) AS lat, AVG(geo_lon) AS lon "
				+ "FROM `events` "
				+ "WHERE YEAR(reported_date) = ? "
				+ "GROUP BY id";
		try {
			Connection conn = DBConnect.getConnection() ;

			PreparedStatement st = conn.prepareStatement(sql) ;
			
			st.setInt(1, anno);
			
			List<Distretto> list = new ArrayList<>() ;
			
			ResultSet res = st.executeQuery() ;
			
			while(res.next()) {
				
				Distretto d = new Distretto(res.getInt("id"), new LatLng(res.getDouble("lat"), res.getDouble("lon")));
				idMap.put(res.getInt("id"), d);
				list.add(d);
			}
			
			conn.close();
			return list ;

		} catch (SQLException e) {
			e.printStackTrace();
			return null ;
		}	
	}

	public List<Integer> getMesi() {
		String sql = "SELECT DISTINCT MONTH(reported_date) AS mese "
				+ "FROM `events` "
				+ "ORDER BY mese";
		try {
			Connection conn = DBConnect.getConnection() ;

			PreparedStatement st = conn.prepareStatement(sql) ;
			
			List<Integer> list = new ArrayList<>() ;
			
			ResultSet res = st.executeQuery() ;
			
			while(res.next()) {
				
				list.add(res.getInt("mese"));
			}
			
			conn.close();
			return list ;

		} catch (SQLException e) {
			e.printStackTrace();
			return null ;
		}		
	}

	public List<Integer> getGiorni(int mese, int anno) {
		String sql = "SELECT DISTINCT DAY(reported_date) AS giorno "
				+ "FROM `events` "
				+ "WHERE YEAR(reported_date) = ? AND MONTH(reported_date) = ? "
				+ "ORDER BY giorno";
		try {
			Connection conn = DBConnect.getConnection() ;

			PreparedStatement st = conn.prepareStatement(sql) ;
			
			st.setInt(1, anno);
			st.setInt(2, mese);
			
			List<Integer> list = new ArrayList<>() ;
			
			ResultSet res = st.executeQuery() ;
			
			while(res.next()) {
				
				list.add(res.getInt("giorno"));
			}
			
			conn.close();
			return list ;

		} catch (SQLException e) {
			e.printStackTrace();
			return null ;
		}		
	}

	public Integer getDistrettoMin(int anno) {
		String sql = "SELECT  district_id AS id "
				+ "FROM `events` "
				+ "WHERE YEAR(reported_date) = ? "
				+ "GROUP BY district_id "
				+ "ORDER BY COUNT(*) ASC "
				+ "LIMIT 1";
		try {
			Connection conn = DBConnect.getConnection() ;

			PreparedStatement st = conn.prepareStatement(sql) ;
			
			st.setInt(1, anno);
			
			Integer id = null;
			ResultSet res = st.executeQuery() ;
			
			if(res.first()) {
				id = res.getInt("id");
			}
			
			conn.close();
			return id ;

		} catch (SQLException e) {
			e.printStackTrace();
			return null ;
		}		
	}

}
