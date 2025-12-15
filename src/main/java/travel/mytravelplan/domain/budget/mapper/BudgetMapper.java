package travel.mytravelplan.domain.budget.mapper;

import org.mapstruct.*;
import travel.mytravelplan.domain.budget.dto.*;
import travel.mytravelplan.domain.budget.entity.Budget;
import travel.mytravelplan.domain.budget.entity.BudgetParticipant;
import travel.mytravelplan.domain.budget.entity.PersonalBudget;
import travel.mytravelplan.domain.budget.entity.SharedBudget;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class BudgetMapper {
    public BudgetDto toDto(Budget budget) {
        if (budget instanceof SharedBudget) {
            return toDto((SharedBudget) budget);
        } else if (budget instanceof PersonalBudget) {
            return toDto((PersonalBudget) budget);
        }
        return null;
    }

    @Mapping(target = "budgetParticipants", source = "budgetParticipants")
    abstract protected SharedBudgetDto toDto(SharedBudget sharedBudget);

    abstract protected PersonalBudgetDto toDto(PersonalBudget personalBudget);

    @Mapping(source = "tripJoin.user.id", target = "id")
    @Mapping(source = "amount", target = "amount")
    abstract protected BudgetParticipantRequestDto toBudgetParticipantRequestDto(BudgetParticipant budgetParticipant);
}
