package travel.mytravelplan.global.config;

/*
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;

import java.util.List;

@Configuration
public class SwaggerConfig {
    final String securitySchemeName = "bearerAuth";

    @Bean
    public OpenAPI OpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MyTravelPlan API 문서")
                        .description("MyTravelPlan 프로젝트의 Swagger API 문서입니다.")
                )
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName)) // 모든 요청에 기본 적용
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("로컬 서버")
                ));
    }
}
*/
