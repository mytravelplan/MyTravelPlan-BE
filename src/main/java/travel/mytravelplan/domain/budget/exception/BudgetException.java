package travel.mytravelplan.domain.budget.exception;

import travel.mytravelplan.global.error.code.BudgetErrorCode;
import travel.mytravelplan.global.error.exception.BusinessException;

public class BudgetException extends BusinessException {
    public BudgetException(BudgetErrorCode errorCode) {
        super(errorCode);
    }
}
