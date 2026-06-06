package br.com.rpx.pactumapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    private final OpenApiConfig config = new OpenApiConfig();

    @Test
    void deve_criar_bean_openapi_com_titulo_e_versao_corretos() {
        OpenAPI openAPI = config.openAPI();

        assertThat(openAPI.getInfo().getTitle()).isEqualTo("Pactum API");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("1.0.0");
        assertThat(openAPI.getInfo().getDescription()).isNotBlank();
    }
}
