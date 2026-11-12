package com.streamly.dto;

//WHAT: Entity for a customer. WHY: we must remember who is signed in and what plan they have.
//Named AppUser because "User" is a reserved word in some databases.

import jakarta.persistence.*;
import java.time.Instant; // a precise point in time

@Entity
public class AppUser {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long id; // IDENTITY = MySQL AUTO_INCREMENT, no *_seq table needed
	@Column(unique = true)
	public String email; // no two users may share an email
	public String token; // random session key sent by the browser
	@Enumerated(EnumType.STRING)
	public Plan plan; // save plan as text ("BASIC"), null = no plan
	public Instant subscribedUntil; // when the plan expires

	// Business rule in one place: active = has a plan AND it has not expired yet
	public boolean isActive() {
		return plan != null && subscribedUntil != null && subscribedUntil.isAfter(Instant.now());
	}
}