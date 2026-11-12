package com.streamly.dto;
//WHAT: JPA entity = one row of the "movie" table. WHY: lets us save/load movies as plain Java objects.

import jakarta.persistence.*; // JPA annotations (@Entity, @Id, ...)

@Entity // "map this class to a database table"
public class Movie {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long id; // IDENTITY = MySQL AUTO_INCREMENT, no *_seq table needed // primary key, the
					// database generates it
	public String title; // each public field becomes a column
	@Column(length = 1000)
	public String synopsis; // allow longer text than the default 255
	public int year;
	public String genre;
	public String posterUrl; // image shown on the rail
	public String trailerUrl; // FREE video, exposed in GraphQL
	public String streamUrl; // full movie: NOT in schema.graphqls, only watch() returns it
}