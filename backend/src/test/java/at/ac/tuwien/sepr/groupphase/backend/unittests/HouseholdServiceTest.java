package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.HouseholdCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.HouseholdDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException;
import at.ac.tuwien.sepr.groupphase.backend.repository.HouseholdRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.HouseholdService;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.HouseholdServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HouseholdServiceTest {

    @Mock
    private HouseholdRepository householdRepository;

    @Mock
    private RoommateRepository roommateRepository;

    private HouseholdService householdService;

    private ApplicationUser user;

    @BeforeEach
    public void setUp() {
        householdService = new HouseholdServiceImpl(householdRepository, roommateRepository);
        user = new ApplicationUser();
        user.setId(1L);
        user.setEmail("test@user.com");
    }

    @Test
    public void givenUserWithoutWG_whenCreateHousehold_thenSuccess() {
        // Arrange
        when(roommateRepository.existsByUserIdAndActiveTrue(1L)).thenReturn(false);
        when(householdRepository.save(any(Household.class))).thenAnswer(i -> {
            Household h = i.getArgument(0);
            h.setId(1L);
            return h;
        });

        HouseholdCreateDto dto = new HouseholdCreateDto("My WG", "Address 1");

        // Act
        HouseholdDto result = householdService.createHousehold(dto, user);

        // Assert
        assertNotNull(result);
        assertEquals("My WG", result.name());
        assertEquals("MAIN_TENANT", result.currentUserRole());
        verify(householdRepository, times(1)).save(any(Household.class));
        verify(roommateRepository, times(1)).save(any(Roommate.class));
    }

    @Test
    public void givenUserWithWG_whenCreateHousehold_thenConflict() {
        // Arrange
        when(roommateRepository.existsByUserIdAndActiveTrue(1L)).thenReturn(true);

        HouseholdCreateDto dto = new HouseholdCreateDto("My WG", "Address 1");

        // Act & Assert
        assertThrows(ConflictException.class, () -> 
            householdService.createHousehold(dto, user)
        );
    }

    @Test
    public void givenNonMember_whenGetMembers_thenForbidden() {
        // Arrange
        when(roommateRepository.existsByUserIdAndHouseholdIdAndActiveTrue(1L, 1L)).thenReturn(false);

        // Act & Assert
        assertThrows(ForbiddenException.class, () -> 
            householdService.getMembers(1L, user)
        );
    }
}
