package pl.pb.tas.teaching_allocation_system.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI teachingAllocationOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Teaching Allocation System API")
                .version("v1")
                .description("REST API for managing academic teaching workloads."));
    }
}