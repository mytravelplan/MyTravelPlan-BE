package travel.mytravelplan.domain.checklist.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.trip.entity.Trip;

@Getter
@Entity
@DiscriminatorValue("SHARED_CHECKLIST")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SharedCheckList extends CheckList {

    @Builder(access = AccessLevel.PRIVATE)
    private SharedCheckList(String name, Trip trip) {
        super(name, trip);
    }

    public static SharedCheckList createSharedCheckList(String name, Trip trip) {
        return SharedCheckList.builder()
                .name(name)
                .trip(trip)
                .build();
    }

    public void update(String name) {
        super.update(name);
    }
}
