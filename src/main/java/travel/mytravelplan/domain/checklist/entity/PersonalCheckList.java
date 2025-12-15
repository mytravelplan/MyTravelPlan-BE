package travel.mytravelplan.domain.checklist.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.trip.entity.Trip;
import travel.mytravelplan.domain.trip.entity.TripJoin;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@DiscriminatorValue("PERSONAL_CHECKLIST")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PersonalCheckList extends CheckList {

    @OneToMany(mappedBy = "personalCheckList")
    private List<PersonalCheckListItem> personalCheckListItems = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private TripJoin tripJoin;

    @Builder(access = AccessLevel.PRIVATE)
    private PersonalCheckList(String name, Trip trip, TripJoin tripJoin) {
        super(name, trip);
        this.tripJoin = tripJoin;
    }

    public static PersonalCheckList createPersonalCheckList(String name, Trip trip, TripJoin tripJoin) {
        return PersonalCheckList.builder()
                .name(name)
                .trip(trip)
                .tripJoin(tripJoin)
                .build();
    }

    public void addPersonalCheckListItems(PersonalCheckListItem personalCheckListItem) {
        personalCheckListItems.add(personalCheckListItem);
        personalCheckListItem.setPersonalCheckList(this);
    }

    public void update(String name) {
        super.update(name);
    }
}
