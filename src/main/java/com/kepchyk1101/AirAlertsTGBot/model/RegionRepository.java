package com.kepchyk1101.AirAlertsTGBot.model;

import org.springframework.data.repository.CrudRepository;

public interface RegionRepository extends CrudRepository<Region, Long> {
    Region findByregionName (String regionName);
}