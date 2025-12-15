package travel.mytravelplan.domain.expense.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import travel.mytravelplan.domain.expense.dto.ExpenseDto;
import travel.mytravelplan.domain.expense.dto.ExpenseParticipantDto;
import travel.mytravelplan.domain.expense.dto.PersonalExpenseDto;
import travel.mytravelplan.domain.expense.dto.SharedExpenseDto;
import travel.mytravelplan.domain.expense.entity.Expense;
import travel.mytravelplan.domain.expense.entity.ExpenseParticipant;
import travel.mytravelplan.domain.expense.entity.PersonalExpense;
import travel.mytravelplan.domain.expense.entity.SharedExpense;

@Mapper(componentModel = "spring")
public abstract class ExpenseMapper {

    public ExpenseDto toDto(Expense expense) {
       if(expense instanceof SharedExpense) {
              return toDto((SharedExpense) expense);
       } else if(expense instanceof PersonalExpense) {
              return toDto((PersonalExpense) expense);
       }
       return null;
    }

    @Mapping(target = "expenseParticipants", source = "expenseParticipants")
    abstract protected SharedExpenseDto toDto(SharedExpense sharedExpense);

    abstract protected PersonalExpenseDto toDto(PersonalExpense personalExpense);

    @Mapping(source = "tripJoin.user.id", target = "userId")
    @Mapping(source = "amount", target = "amount")
    abstract protected ExpenseParticipantDto toDto(ExpenseParticipant expenseParticipant);
}
