package travel.mytravelplan.domain.checklist.entity;

import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.global.common.entity.BaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@MappedSuperclass
public abstract class CheckListItem extends BaseEntity {
    private String text;

    protected CheckListItem(String text) {
        this.text = text;
    }

    protected void update(String text) {
        this.text = text;
    }
}
