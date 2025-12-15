package travel.mytravelplan.domain.category.exception;

import travel.mytravelplan.global.error.code.CategoryErrorCode;
import travel.mytravelplan.global.error.exception.BusinessException;

public class CategoryException extends BusinessException {
    public CategoryException(CategoryErrorCode errorCode) {
        super(errorCode);
    }
}
