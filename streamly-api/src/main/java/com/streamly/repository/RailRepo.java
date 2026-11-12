package com.streamly.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.streamly.dto.Rail;

public interface RailRepo extends JpaRepository<Rail, Long> {
	// Spring reads the method NAME and builds: SELECT ... ORDER BY position ASC
	List<Rail> findAllByOrderByPositionAsc();
}
