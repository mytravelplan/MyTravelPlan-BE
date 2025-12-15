package travel.mytravelplan.global.common.entity;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseEntity {
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    protected BaseEntity(LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    public void prePersist() {
        createdAt = truncateToMicros(createdAt);
        updatedAt = truncateToMicros(updatedAt);
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = truncateToMicros(updatedAt);
    }

    public void updateTimestamp() {
        updatedAt = truncateToMicros(LocalDateTime.now());
    }

    private LocalDateTime truncateToMicros(LocalDateTime dateTime) {
        return dateTime.truncatedTo(ChronoUnit.MICROS);
    }
}
