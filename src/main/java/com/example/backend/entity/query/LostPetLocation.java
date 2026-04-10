package com.example.backend.entity.query;

import com.example.backend.entity.Location;
import com.example.backend.entity.LostPet;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LostPetLocation extends LostPet {

    private Location location;
}
