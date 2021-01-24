package com.space.controller;

import com.space.exception.BadRequestException;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest")
public class ShipRestController {

    private ShipService service;

    @Autowired
    public void setService(ShipService service) {
        this.service = service;
    }

    @GetMapping("/ships")
    @ResponseStatus(HttpStatus.OK)
    public List<Ship> getAllExistingShips(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "planet", required = false) String planet,
            @RequestParam(value = "shipType", required = false) ShipType type,
            @RequestParam(value = "after", required = false) Long after,
            @RequestParam(value = "before", required = false) Long before,
            @RequestParam(value = "isUsed", required = false) Boolean bool,
            @RequestParam(value = "minSpeed", required = false) Double minSpeed,
            @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
            @RequestParam(value = "minCrewSize", required = false) Integer minSize,
            @RequestParam(value = "maxCrewSize", required = false) Integer maxSize,
            @RequestParam(value = "minRating", required = false) Double minRating,
            @RequestParam(value = "maxRating", required = false) Double maxRating,
            @RequestParam(value = "order", required = false, defaultValue = "ID") ShipOrder order,
            @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "3") Integer size
            ){

        Pageable pageable = PageRequest.of(page, size, Sort.by(order.getFieldName()));

        Specification<Ship> spec = Specification.where(
                service.nameFilter(name)
                .and(service.planetFilter(planet))
                .and(service.typeFilter(type))
                .and(service.dateFilter(after, before))
                .and(service.isUsedFilter(bool))
                .and(service.speedFilter(minSpeed, maxSpeed))
                .and(service.crewSizeFilter(minSize, maxSize))
                .and(service.ratingFilter(minRating, maxRating))
        );

        return service.getAllExistingShip(spec, pageable).getContent();
    }

    @GetMapping("/ships/count")
    @ResponseStatus(HttpStatus.OK)
    public Integer getCount(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "planet", required = false) String planet,
            @RequestParam(value = "shipType", required = false) ShipType type,
            @RequestParam(value = "after", required = false) Long after,
            @RequestParam(value = "before", required = false) Long before,
            @RequestParam(value = "isUsed", required = false) Boolean bool,
            @RequestParam(value = "minSpeed", required = false) Double minSpeed,
            @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
            @RequestParam(value = "minCrewSize", required = false) Integer minSize,
            @RequestParam(value = "maxCrewSize", required = false) Integer maxSize,
            @RequestParam(value = "minRating", required = false) Double minRating,
            @RequestParam(value = "maxRating", required = false) Double maxRating
    ){
        Specification<Ship> spec = Specification.where(
                service.nameFilter(name)
                        .and(service.planetFilter(planet))
                        .and(service.typeFilter(type))
                        .and(service.dateFilter(after, before))
                        .and(service.isUsedFilter(bool))
                        .and(service.speedFilter(minSpeed, maxSpeed))
                        .and(service.crewSizeFilter(minSize, maxSize))
                        .and(service.ratingFilter(minRating, maxRating))
        );

        return service.getAllExistingShip(spec).size();
    }

    @GetMapping("/ships/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Ship getShip(@PathVariable("id") String id){
        return service.getShip(idChecker(id));
    }

    @PostMapping("/ships")
    @ResponseStatus(HttpStatus.OK)
    public Ship createShip(@RequestBody Ship ship){
        return service.createShip(ship);
    }

    @PostMapping("/ships/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Ship editShip(@PathVariable("id") String id, @RequestBody Ship ship){
        return service.editShip(idChecker(id), ship);
    }

    @DeleteMapping("/ships/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteShip(@PathVariable("id") String id){
        service.deleteByID(idChecker(id));
    }

    private Long idChecker(String id) {
        if(id == null || id.equals("")){
            throw new BadRequestException("ID is not a number");
        }

        try {
            Long idLong = Long.parseLong(id);
            if(idLong <= 0) throw new BadRequestException("ID is less then or equal zero");
            return idLong;
        } catch (NumberFormatException e) {
            throw new  BadRequestException("ID is not a number");
        }
    }
}
