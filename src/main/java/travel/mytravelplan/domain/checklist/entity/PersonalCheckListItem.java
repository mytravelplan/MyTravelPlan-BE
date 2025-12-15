package travel.mytravelplan.domain.checklist.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PersonalCheckListItem extends CheckListItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_checklist_id")
    private PersonalCheckList personalCheckList;

    private boolean checked;

    @Builder(access = AccessLevel.PRIVATE)
    private PersonalCheckListItem(String text, PersonalCheckList personalCheckList) {
        super(text);
        this.personalCheckList = personalCheckList;
    }

    public static PersonalCheckListItem createPersonalCheckListItem(String text, PersonalCheckList personalCheckList) {
        return PersonalCheckListItem.builder()
                .text(text)
                .personalCheckList(personalCheckList)
                .build();
    }

    public void update(String text, boolean checked) {
        super.update(text);
        this.checked = checked;
    }
}
