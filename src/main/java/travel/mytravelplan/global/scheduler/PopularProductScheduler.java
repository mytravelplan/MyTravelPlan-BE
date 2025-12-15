package travel.mytravelplan.global.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import travel.mytravelplan.domain.product.service.ProductService;

@Component
@Slf4j
@RequiredArgsConstructor
public class PopularProductScheduler {
    private final ProductService productService;

    // 매일 자정 배치 연산 수행
    @Scheduled(cron = "${scheduler.batch.start-time}", zone = "Asia/Seoul")
    public void updateRanking() {

    }
}
