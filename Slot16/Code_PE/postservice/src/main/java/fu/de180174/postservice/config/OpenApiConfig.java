package fu.de180174.postservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Post Service API")
                        .version("1.0")
                        .description("MSS301 Practical Exam - Post Service API Documentation"))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Via Media Gateway (Port 8080)"),
                        new Server().url("http://localhost:8082").description("Direct Post Service (Port 8082)")
                ));
    }
}

