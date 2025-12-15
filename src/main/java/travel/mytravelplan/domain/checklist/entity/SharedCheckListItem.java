package travel.mytravelplan.domain.checklist.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SharedCheckListItem extends CheckListItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_checklist_id")
    private SharedCheckList sharedCheckList;

    @Builder(access = AccessLevel.PRIVATE)
    private SharedCheckListItem(String text, SharedCheckList sharedCheckList) {
        super(text);
        this.sharedCheckList = sharedCheckList;
    }

    public static SharedCheckListItem createSharedCheckListItem(String text, SharedCheckList sharedCheckList) {
        return SharedCheckListItem.builder()
                .text(text)
                .sharedCheckList(sharedCheckList)
                .build();
    }

    public void update(String text) {
        super.update(text);
    }
}
