package com.streamly.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.streamly.dto.Movie;

public interface MovieRepo extends JpaRepository<Movie, Long> {
}
