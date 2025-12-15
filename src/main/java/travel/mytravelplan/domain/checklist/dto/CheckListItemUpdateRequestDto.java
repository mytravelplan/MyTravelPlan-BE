package travel.mytravelplan.domain.checklist.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import travel.mytravelplan.domain.checklist.enums.CheckListType;

@Getter
@NoArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "checkListType",
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PersonalCheckListItemUpdateRequestDto.class, name = "PERSONAL"),
        @JsonSubTypes.Type(value = SharedCheckListItemUpdateRequestDto.class, name = "SHARED"),
})
public abstract class CheckListItemUpdateRequestDto {
    private CheckListType checkListType;
    private String text;
    private boolean checked;

    protected CheckListItemUpdateRequestDto(CheckListType checkListType, String text, boolean checked) {
        this.checkListType = checkListType;
        this.text = text;
        this.checked = checked;
    }
}
