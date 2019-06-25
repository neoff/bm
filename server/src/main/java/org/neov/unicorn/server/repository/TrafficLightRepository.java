package org.neov.unicorn.server.repository;

import org.neov.unicorn.common.models.TrafficLight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrafficLightRepository extends JpaRepository<TrafficLight, Long> {
	@Query(value = "SELECT seq_name.nextval FROM dual", nativeQuery = true)
	Long getNextSeriesId();

	List<TrafficLight> findByNameStartsWithIgnoreCase(String lastName);
}
