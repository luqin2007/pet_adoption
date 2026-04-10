package com.example.backend.entity.query;

import com.example.backend.entity.Location;
import com.example.backend.entity.Pet;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class PetLocations extends Pet {

    private List<Location> locations;
}
