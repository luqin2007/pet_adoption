package com.example.backend.component;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.backend.entity.Breading;
import com.example.backend.entity.Location;
import com.example.backend.entity.LostPet;
import com.example.backend.entity.Pet;
import com.example.backend.mapper.BreadingMapper;
import com.example.backend.mapper.LocationMapper;
import com.example.backend.mapper.LostPetMapper;
import com.example.backend.mapper.PetMapper;
import com.example.backend.service.InformationService;
import com.example.backend.util.CityHelper;
import com.example.backend.util.ServiceException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@DependsOn("startupInitializer")
public class StartupValidator implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(StartupValidator.class);

    private final PetMapper petMapper;
    private final LostPetMapper lostPetMapper;
    private final BreadingMapper breadingMapper;
    private final LocationMapper locationMapper;
    private final InformationService informationService;

    private final CityHelper cityHelper;

    @Override
    public void run(ApplicationArguments args) {
        int petTypeInsertions = loadPetTypes();
        int validatedLocations = validateLocations();
        LOGGER.info(
                "Startup data integrity check completed. syncedPetTypes={}, validatedLocations={}",
                petTypeInsertions,
                validatedLocations);
    }

    private int loadPetTypes() {
        int inserted = 0;
        for (Pet pet : petMapper.selectList(Wrappers.lambdaQuery(Pet.class)
                .select(Pet::getType, Pet::getBreed))) {
            inserted += recordPetTypeIfMissing(pet.getType(), pet.getBreed());
        }
        for (LostPet lostPet : lostPetMapper.selectList(Wrappers.lambdaQuery(LostPet.class)
                .select(LostPet::getType, LostPet::getBreed))) {
            inserted += recordPetTypeIfMissing(lostPet.getType(), lostPet.getBreed());
        }
        for (Breading breading : breadingMapper.selectList(Wrappers.lambdaQuery(Breading.class)
                .select(Breading::getPetType, Breading::getPetBreed))) {
            inserted += recordPetTypeIfMissing(breading.getPetType(), breading.getPetBreed());
        }
        return inserted;
    }

    private int recordPetTypeIfMissing(String type, String breed) {
        return informationService.addPetType(type, breed) ? 1 : 0;
    }

    private int validateLocations() {
        int validated = 0;
        boolean error = false;
        for (Location location : locationMapper.selectList(Wrappers.lambdaQuery(Location.class)
                .select(Location::getId, Location::getProvince, Location::getCity, Location::getDistrict))) {
            if (!cityHelper.isValid(location.getProvince(), location.getCity(), location.getDistrict())) {
                LOGGER.error(
                        "Invalid location data found during startup. id={}, province={}, city={}, district={}",
                        location.getId(),
                        location.getProvince(),
                        location.getCity(),
                        location.getDistrict());
                error = true;
            }
            validated++;
        }
        if (error) {
            throw ServiceException.system("exception.system.location_data_invalid");
        }
        return validated;
    }
}
