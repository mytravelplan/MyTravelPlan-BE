package travel.mytravelplan.domain.currency.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import travel.mytravelplan.domain.currency.entity.Currency;
import travel.mytravelplan.domain.currency.enums.CurrencyType;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    Optional<Currency> findByCurrencyType(CurrencyType currencyType);
    List<Currency> findByCurrencyTypeIn(Set<CurrencyType> currencyTypes);
}
