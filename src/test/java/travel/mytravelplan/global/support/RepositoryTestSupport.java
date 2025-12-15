package travel.mytravelplan.global.support;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;
import travel.mytravelplan.global.config.QueryDSLConfig;

@ActiveProfiles("test")
@EnableJpaAuditing
@DataJpaTest
@Import(QueryDSLConfig.class)
public abstract class RepositoryTestSupport {
    @Autowired
    protected EntityManager em;
}
