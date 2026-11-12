package com.streamly.controller;

//WHAT: Everything about users: login, plans, subscribe, and the protected watch(). WHY: this is where
//the business rule lives: trailers are free, full movies need an active subscription.

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Controller;

import com.streamly.dto.AppUser;
import com.streamly.dto.Movie;
import com.streamly.dto.Plan;
import com.streamly.exception.ApiException;
import com.streamly.repository.MovieRepo;
import com.streamly.repository.UserRepo;

@Controller
class AccountController {
	// records = tiny immutable data holders; their fields match the GraphQL types
	record AuthPayload(String token, AppUser user) {
	}

	record PlanInfo(Plan plan, double priceMonthly, String description) {
	}

	record Playback(String movieId, String url) {
	}

	private final UserRepo users;
	private final MovieRepo movies;

	AccountController(UserRepo users, MovieRepo movies) {
		this.users = users;
		this.movies = movies;
	}

	// Helper: turn a token into a user, or fail with 401-style errors
	private AppUser require(String token) {
		if (token == null)
			throw new ApiException(ErrorType.UNAUTHORIZED, "Sign in first.");
		return users.findByToken(token)
				.orElseThrow(() -> new ApiException(ErrorType.UNAUTHORIZED, "Session expired. Sign in again."));
	}

	// Query.plans: convert every enum value into a PlanInfo for the pricing cards
	@QueryMapping
	List<PlanInfo> plans() {
		return Arrays.stream(Plan.values()).map(p -> new PlanInfo(p, p.price, p.description)).toList();
	}

	// Query.me: @ContextValue reads the token put there by TokenInterceptor;
	// required=false = may be absent
	@QueryMapping
	AppUser me(@ContextValue(name = "token", required = false) String token) {
		return token == null ? null : users.findByToken(token).orElse(null); // null = "not signed in", not an error
	}

	// Query.watch: THE protected action
	@QueryMapping
	Playback watch(@Argument Long movieId, @ContextValue(name = "token", required = false) String token) {
		AppUser u = require(token); // must be signed in
		if (!u.isActive()) // must have a valid plan
			throw new ApiException(ErrorType.FORBIDDEN, "Subscribe to watch the full movie. Trailers are free.");
		Movie m = movies.findById(movieId) // movie must exist
				.orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND, "Movie not found."));
		return new Playback(String.valueOf(m.id), m.streamUrl); // only now do we reveal the stream URL
	}

	/** Demo login: creates the user on first use. No password, do not ship this. */
	@MutationMapping
	AuthPayload login(@Argument String email) {
		AppUser u = users.findByEmail(email.trim().toLowerCase()).orElseGet(() -> { // find existing user...
			AppUser n = new AppUser(); // ...or create a new one
			n.email = email.trim().toLowerCase(); // normalise the email
			return n;
		});
		u.token = UUID.randomUUID().toString(); // new random token for this session
		return new AuthPayload(u.token, users.save(u)); // save to DB and return token + user
	}

	@MutationMapping
	AppUser subscribe(@Argument Plan plan, @ContextValue(name = "token", required = false) String token) {
		AppUser u = require(token); // must be signed in
		u.plan = plan; // remember chosen plan
		u.subscribedUntil = Instant.now().plus(30, ChronoUnit.DAYS); // valid for 30 days (no real payment)
		return users.save(u);
	}

	@MutationMapping
	AppUser cancelSubscription(@ContextValue(name = "token", required = false) String token) {
		AppUser u = require(token);
		u.plan = null; // remove plan
		u.subscribedUntil = null;
		return users.save(u);
	}

	// GraphQL type User needs a String "subscribedUntil"; our entity stores an
	// Instant, so convert here
	@SchemaMapping(typeName = "User", field = "subscribedUntil")
	String subscribedUntil(AppUser u) {
		return u.subscribedUntil == null ? null : u.subscribedUntil.toString();
	}

	// GraphQL field User.active is computed using the entity's isActive() rule
	@SchemaMapping(typeName = "User", field = "active")
	boolean active(AppUser u) {
		return u.isActive();
	}
}