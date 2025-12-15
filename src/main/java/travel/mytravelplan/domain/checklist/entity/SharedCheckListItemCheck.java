package travel.mytravelplan.domain.checklist.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.trip.entity.TripJoin;
import travel.mytravelplan.global.common.entity.BaseEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SharedCheckListItemCheck extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_join_id")
    private TripJoin tripJoin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_checklist_item_id")
    private SharedCheckListItem sharedCheckListItem;

    @Builder(access = AccessLevel.PRIVATE)
    private SharedCheckListItemCheck(TripJoin tripJoin, SharedCheckListItem sharedCheckListItem) {
        this.tripJoin = tripJoin;
        this.sharedCheckListItem = sharedCheckListItem;
    }

    public static SharedCheckListItemCheck createSharedCheckListItemCheck(TripJoin tripJoin, SharedCheckListItem sharedCheckListItem) {
        return SharedCheckListItemCheck.builder()
                .tripJoin(tripJoin)
                .sharedCheckListItem(sharedCheckListItem)
                .build();
    }
}
