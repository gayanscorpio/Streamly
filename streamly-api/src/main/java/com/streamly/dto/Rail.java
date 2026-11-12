package com.streamly.dto;
//WHAT: Entity for a row of movies on the home page. WHY: lets the catalog be data, not hard-coded UI.

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Rail {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long id; // IDENTITY = MySQL AUTO_INCREMENT, no *_seq table needed
	public String title; // heading shown above the row
	public int position; // sort order on the page (0 = top)

	// many movies can be in many rails; EAGER = load movies together with the rail
	// (avoids "lazy loading" errors when GraphQL reads them later)
	@ManyToMany(fetch = FetchType.EAGER)
	public List<Movie> movies;
}