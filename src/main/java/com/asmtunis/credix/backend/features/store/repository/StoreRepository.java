package com.asmtunis.credix.backend.features.store.repository;

import com.asmtunis.credix.backend.features.store.entity.Store;
import com.asmtunis.credix.backend.features.store.entity.StoreType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
	List<Store> findByTypeAndActiveTrue(StoreType type);

	List<Store> findByActiveTrue();

	@Query("SELECT s FROM Store s WHERE s.active = true AND " +
				 "(6371 * acos(cos(radians(:latitude)) * cos(radians(s.latitude)) * " +
				 "cos(radians(s.longitude) - radians(:longitude)) + " +
				 "sin(radians(:latitude)) * sin(radians(s.latitude)))) < :radiusKm " +
				 "ORDER BY (6371 * acos(cos(radians(:latitude)) * cos(radians(s.latitude)) * " +
				 "cos(radians(s.longitude) - radians(:longitude)) + " +
				 "sin(radians(:latitude)) * sin(radians(s.latitude))))")
	List<Store> findNearbyStores(
			@Param("latitude") Double latitude,
			@Param("longitude") Double longitude,
			@Param("radiusKm") Double radiusKm
	);
}
