package travel.mytravelplan.domain.schedule.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import travel.mytravelplan.domain.schedule.entity.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule, Long>, ScheduleRepositoryCustom {

}
