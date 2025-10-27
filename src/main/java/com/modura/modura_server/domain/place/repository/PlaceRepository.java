package com.modura.modura_server.domain.place.repository;

import com.modura.modura_server.domain.place.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceRepository extends JpaRepository<Place, Long> {

}
