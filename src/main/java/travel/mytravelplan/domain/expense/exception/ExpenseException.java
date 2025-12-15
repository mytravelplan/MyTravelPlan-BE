package travel.mytravelplan.domain.expense.exception;

import travel.mytravelplan.global.error.code.ExpenseErrorCode;
import travel.mytravelplan.global.error.exception.BusinessException;

public class ExpenseException extends BusinessException {
    public ExpenseException(ExpenseErrorCode errorCode) {
        super(errorCode);
    }
}
