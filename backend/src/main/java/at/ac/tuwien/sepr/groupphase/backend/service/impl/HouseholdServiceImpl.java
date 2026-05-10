package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.HouseholdCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.HouseholdDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RoommateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;
import at.ac.tuwien.sepr.groupphase.backend.entity.HouseholdRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.HouseholdRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.HouseholdService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HouseholdServiceImpl implements HouseholdService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final HouseholdRepository householdRepository;
    private final RoommateRepository roommateRepository;

    @Autowired
    public HouseholdServiceImpl(HouseholdRepository householdRepository, RoommateRepository roommateRepository) {
        this.householdRepository = householdRepository;
        this.roommateRepository = roommateRepository;
    }

    @Override
    @Transactional
    public HouseholdDto createHousehold(HouseholdCreateDto dto, ApplicationUser currentUser) {
        LOGGER.debug("Creating household {} for user {}", dto.name(), currentUser.getEmail());

        if (roommateRepository.existsByUserIdAndActiveTrue(currentUser.getId())) {
            throw new ConflictException("User already belongs to a household.");
        }

        Household household = new Household();
        household.setName(dto.name());
        household.setAddress(dto.address());
        household.setCreatedBy(currentUser);
        household = householdRepository.save(household);

        Roommate roommate = new Roommate();
        roommate.setUser(currentUser);
        roommate.setHousehold(household);
        roommate.setRole(HouseholdRole.MAIN_TENANT);
        roommate.setActive(true);
        roommateRepository.save(roommate);

        return new HouseholdDto(
            household.getId(),
            household.getName(),
            household.getAddress(),
            household.getCreatedAt(),
            HouseholdRole.MAIN_TENANT.name()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public HouseholdDto getMyHousehold(ApplicationUser currentUser) {
        LOGGER.debug("Getting household for user {}", currentUser.getEmail());

        Roommate roommate = roommateRepository.findByUserIdAndActiveTrue(currentUser.getId())
            .orElseThrow(() -> new NotFoundException("User does not belong to any household."));

        Household household = roommate.getHousehold();

        return new HouseholdDto(
            household.getId(),
            household.getName(),
            household.getAddress(),
            household.getCreatedAt(),
            roommate.getRole().name()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoommateDto> getMembers(Long householdId, ApplicationUser currentUser) {
        LOGGER.debug("Getting members for household {} for user {}", householdId, currentUser.getEmail());

        if (!roommateRepository.existsByUserIdAndHouseholdIdAndActiveTrue(currentUser.getId(), householdId)) {
            LOGGER.warn("Access denied: User {} is not an active member of household {}", currentUser.getId(), householdId);
            throw new ForbiddenException("You are not a member of this household.");
        }

        return roommateRepository.findAllByHouseholdIdAndActiveTrue(householdId).stream()
            .map(r -> new RoommateDto(
                r.getId(),
                r.getUser().getId(),
                r.getUser().getFirstName(),
                r.getUser().getLastName(),
                r.getUser().getEmail(),
                r.getRole().name(),
                r.getJoinedAt()
            ))
            .collect(Collectors.toList());
    }
}
