package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper
public interface ShoppingItemMapper {

    @Mapping(target = "householdId", source = "household.id")
    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "createdByEmail", source = "createdBy.email")
    @Mapping(target = "createdByFirstName", source = "createdBy.firstName")
    @Mapping(target = "createdByLastName", source = "createdBy.lastName")
    @Mapping(target = "targetUserId", source = "targetUser.id")
    @Mapping(target = "targetUserEmail", source = "targetUser.email")
    @Mapping(target = "targetUserFirstName", source = "targetUser.firstName")
    @Mapping(target = "targetUserLastName", source = "targetUser.lastName")
    ShoppingItemDto entityToDto(ShoppingItem item);

    List<ShoppingItemDto> entityToDto(List<ShoppingItem> items);
}
