package com.space.service;

import com.space.model.Ship;
import com.space.model.ShipType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface ShipService {

    Ship createShip(Ship ship);
    Ship editShip(Long id, Ship ship);
    void deleteByID(Long id);
    Ship getShip(Long id);

    List<Ship> getAllExistingShip(Specification<Ship> spec);
    Page<Ship> getAllExistingShip(Specification<Ship> spec, Pageable sortByName);

    Specification<Ship> nameFilter(String name);
    Specification<Ship> planetFilter(String name);
    Specification<Ship> typeFilter(ShipType type);
    Specification<Ship> dateFilter(Long after, Long before);
    Specification<Ship> isUsedFilter(Boolean bool);
    Specification<Ship> speedFilter(Double min, Double max);
    Specification<Ship> crewSizeFilter(Integer min, Integer max);
    Specification<Ship> ratingFilter(Double min, Double max);

    Long idChecker(String id);



}
