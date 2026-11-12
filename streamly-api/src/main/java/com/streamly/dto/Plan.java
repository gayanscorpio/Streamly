package com.streamly.dto;

public enum Plan {
	BASIC(5.99, "720p, 1 screen"), // each constant calls the constructor below
	STANDARD(9.99, "1080p, 2 screens"), PREMIUM(14.99, "4K, 4 screens");

	public final double price; // monthly price
	public final String description; // short text for the pricing card

	Plan(double price, String description) { // constructor: stores the values for each constant
		this.price = price;
		this.description = description;
	}
}
