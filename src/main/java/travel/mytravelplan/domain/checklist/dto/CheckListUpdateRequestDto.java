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
        @JsonSubTypes.Type(value = SharedCheckListUpdateRequestDto.class, name = "SHARED"),
        @JsonSubTypes.Type(value = PersonalCheckListUpdateRequestDto.class, name = "PERSONAL"),
})
public abstract class CheckListUpdateRequestDto {
    private CheckListType checkListType;
    private String name;

    protected CheckListUpdateRequestDto(CheckListType checkListType, String name) {
        this.checkListType = checkListType;
        this.name = name;
    }
}
