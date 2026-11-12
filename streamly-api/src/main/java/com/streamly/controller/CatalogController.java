package com.streamly.controller;

//WHAT: Answers the PUBLIC catalog queries. WHY: keeps browsing code separate from account code.

import java.util.List;
import java.util.Optional;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.stereotype.Controller;

import com.streamly.dto.Movie;
import com.streamly.dto.Rail;
import com.streamly.repository.MovieRepo;
import com.streamly.repository.RailRepo;

@Controller // marks this class as a request handler
class CatalogController {

	private final RailRepo rails; // database access for rails
	private final MovieRepo movies; // database access for movies

	// Spring injects the repositories automatically (constructor injection)
	CatalogController(RailRepo rails, MovieRepo movies) {
		this.rails = rails;
		this.movies = movies;
	}

	// @QueryMapping links this method to "Query.rails" in schema.graphqls (same
	// name)
	@QueryMapping
	List<Rail> rails() {
		return rails.findAllByOrderByPositionAsc();
	}

	// @Argument pulls the "id" argument out of the query; Optional empty => GraphQL
	// returns null
	@QueryMapping
	Optional<Movie> movie(@Argument Long id) {
		return movies.findById(id);
	}
}
