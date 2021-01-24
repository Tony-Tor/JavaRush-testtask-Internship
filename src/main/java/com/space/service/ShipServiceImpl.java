package com.space.service;

import com.space.exception.BadRequestException;
import com.space.exception.NotFoundException;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class ShipServiceImpl implements ShipService{

    private ShipRepository repository;

    @Autowired
    public void setRepository(ShipRepository repository) {
        this.repository = repository;
    }

    @Override
    public Ship createShip(Ship ship) {
        paramsNullValidator(ship);
        paramsValidator(ship);

        if(ship.getUsed() == null){
            ship.setUsed(false);
        }

        ship.setRating(calculateRating(ship));

        return repository.saveAndFlush(ship);
    }

    @Override
    public Ship editShip(Long id, Ship ship) {

        if (!repository.existsById(id)){
            throw new NotFoundException("Ship is not found");
        }

        Ship changedShip = repository.findById(id).get();

        if(ship.getName() != null) {
            nameValidator(ship);
            changedShip.setName(ship.getName());
        }
        if(ship.getPlanet() != null) {
            planetValidator(ship);
            changedShip.setPlanet(ship.getPlanet());
        }
        if(ship.getShipType() != null) {
            changedShip.setShipType(ship.getShipType());
        }
        if(ship.getProdDate() != null) {
            prodDateValidator(ship);
            changedShip.setProdDate(ship.getProdDate());
        }
        if(ship.getSpeed() != null) {
            speedValidator(ship);
            changedShip.setSpeed(ship.getSpeed());
        }
        if(ship.getUsed() != null) {
            changedShip.setUsed(ship.getUsed());
        }
        if(ship.getCrewSize() != null) {
            crewSizeValidator(ship);
            changedShip.setCrewSize(ship.getCrewSize());
        }

        changedShip.setRating(calculateRating(changedShip));

        return repository.save(changedShip);
    }

    @Override
    public void deleteByID(Long id) {
        if (!repository.existsById(id)){
            throw new NotFoundException("Ship is not found");
        }

        repository.deleteById(id);
    }

    @Override
    public Ship getShip(Long id) {
        if (!repository.existsById(id)){
            throw new NotFoundException("Ship is not found");
        }

        return repository.findById(id).get();
    }

    @Override
    public List<Ship> getAllExistingShip(Specification<Ship> spec) {
        return repository.findAll(spec);
    }

    @Override
    public Page<Ship> getAllExistingShip(Specification<Ship> spec, Pageable sortByName) {
        return repository.findAll(spec, sortByName);
    }

    private Double calculateRating(Ship ship){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(ship.getProdDate());

        int yearProd = calendar.get(Calendar.YEAR);
        int yearCurrent = 3019;
        double k = ship.getUsed() ? 0.5 : 1;
        double speed = ship.getSpeed();

        double res = (80*speed*k)/(yearCurrent-yearProd+1);

        BigDecimal result = new BigDecimal(res)
                .setScale(2, RoundingMode.HALF_UP);

        return result.doubleValue();
    }

    @Override
    public Specification<Ship> nameFilter(String name) {
        return new Specification<Ship>() {
            @Override
            public Predicate toPredicate(Root<Ship> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                if (name == null) return null;
                return criteriaBuilder.like(root.get("name"), "%" + name +"%");
            }
        };
    }

    @Override
    public Specification<Ship> planetFilter(String name) {
        return new Specification<Ship>() {
            @Override
            public Predicate toPredicate(Root<Ship> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                if (name == null) return null;
                return criteriaBuilder.like(root.get("planet"), "%" + name +"%");
            }
        };
    }

    @Override
    public Specification<Ship> typeFilter(ShipType type) {
        return new Specification<Ship>() {
            @Override
            public Predicate toPredicate(Root<Ship> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                if (type == null) return null;
                return criteriaBuilder.equal(root.get("shipType"), type);
            }
        };
    }

    @Override
    public Specification<Ship> dateFilter(Long after, Long before) {
        return new Specification<Ship>() {
            @Override
            public Predicate toPredicate(Root<Ship> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                if(after == null && before == null){
                    return null;
                }

                if (after == null){
                    return criteriaBuilder.lessThanOrEqualTo(root.get("profDate"),
                            new Date(before));
                }
                if (before == null){
                    return criteriaBuilder.greaterThanOrEqualTo(root.get("profDate"),
                            new Date(before));
                }

                return criteriaBuilder.between(root.get("profDate"),
                        new Date(after),
                        new Date(before)); //TODO
            }
        };
    }

    @Override
    public Specification<Ship> isUsedFilter(Boolean bool) {
        return new Specification<Ship>() {
            @Override
            public Predicate toPredicate(Root<Ship> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                if(bool == null)return null;
                if(bool) return criteriaBuilder.isTrue(root.get("isUsed"));
                else return criteriaBuilder.isFalse(root.get("isUsed"));
            }
        };
    }

    @Override
    public Specification<Ship> speedFilter(Double min, Double max) {
        return new Specification<Ship>() {
            @Override
            public Predicate toPredicate(Root<Ship> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                if(min == null && max == null){
                    return null;
                }

                if (min == null){
                    return criteriaBuilder.lessThanOrEqualTo(root.get("profDate"), min);
                }
                if (max == null){
                    return criteriaBuilder.greaterThanOrEqualTo(root.get("profDate"), max);
                }

                return criteriaBuilder.between(root.get("profDate"), min, max);
            }
        };
    }

    @Override
    public Specification<Ship> crewSizeFilter(Integer min, Integer max) {
        return new Specification<Ship>() {
            @Override
            public Predicate toPredicate(Root<Ship> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                if(min == null && max == null){
                    return null;
                }

                if (min == null){
                    return criteriaBuilder.lessThanOrEqualTo(root.get("profDate"), min);
                }
                if (max == null){
                    return criteriaBuilder.greaterThanOrEqualTo(root.get("profDate"), max);
                }

                return criteriaBuilder.between(root.get("profDate"), min, max);
            }
        };
    }

    @Override
    public Specification<Ship> ratingFilter(Double min, Double max) {
        return new Specification<Ship>() {
            @Override
            public Predicate toPredicate(Root<Ship> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                if(min == null && max == null){
                    return null;
                }

                if (min == null){
                    return criteriaBuilder.lessThanOrEqualTo(root.get("profDate"), min);
                }
                if (max == null){
                    return criteriaBuilder.greaterThanOrEqualTo(root.get("profDate"), max);
                }

                return criteriaBuilder.between(root.get("profDate"), min, max);
            }
        };
    }

    private void paramsValidator(Ship ship){
        nameValidator(ship);
        planetValidator(ship);
        speedValidator(ship);
        crewSizeValidator(ship);
        prodDateValidator(ship);
    }

    private void paramsNullValidator(Ship ship){
        nameNullValidator(ship);
        planetNullValidator(ship);
        speedNullValidator(ship);
        crewSizeNullValidator(ship);
        prodDateNullValidator(ship);
        shipTypeNullValidator(ship);
    }

    private void nameValidator(Ship ship){
        if (ship.getName().length() < 1 || ship.getName().length() > 50){
            throw new BadRequestException("Name is too long or empty");
        }
    }

    private void planetValidator(Ship ship){
        if (ship.getPlanet().length() < 1 || ship.getPlanet().length() > 50){
            throw new BadRequestException("Planet is too long or empty");
        }
    }

    private void speedValidator(Ship ship) {
        if (ship.getSpeed() < 0.01 || ship.getSpeed() > 0.99){
            throw new BadRequestException("Speed is out of range");
        }
    }

    private void crewSizeValidator(Ship ship) {
        if (ship.getCrewSize() < 1 || ship.getCrewSize() > 9999){
            throw new BadRequestException("Crew size is out of range");
        }
    }

    private void prodDateValidator(Ship ship) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(ship.getProdDate());

        if (calendar.get(Calendar.YEAR) < 2800 || calendar.get(Calendar.YEAR) > 3019){
            throw new BadRequestException("Production date is out of range");
        }
    }

    private void nameNullValidator(Ship ship){
        if (ship.getName() == null){
            throw new BadRequestException("Name is NULL");
        }
    }

    private void planetNullValidator(Ship ship){
        if (ship.getPlanet() == null){
            throw new BadRequestException("Planet is NULL");
        }
    }

    private void speedNullValidator(Ship ship) {
        if (ship.getSpeed() == null){
            throw new BadRequestException("Speed is NULL");
        }
    }

    private void crewSizeNullValidator(Ship ship) {
        if (ship.getCrewSize() == null){
            throw new BadRequestException("Crew size is NULL");
        }
    }

    private void prodDateNullValidator(Ship ship) {
        if (ship.getProdDate() == null){
            throw new BadRequestException("ProdDate is NULL");
        }
    }

    private void shipTypeNullValidator(Ship ship) {
        if (ship.getShipType() == null){
            throw new BadRequestException("ProdDate is NULL");
        }
    }
}
