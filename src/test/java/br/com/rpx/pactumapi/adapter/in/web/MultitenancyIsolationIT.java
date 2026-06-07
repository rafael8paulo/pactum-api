package br.com.rpx.pactumapi.adapter.in.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:isolationit;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.session.store-type=none"
})
class MultitenancyIsolationIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private void cadastrar(String email, String senha) throws Exception {
        mockMvc.perform(post("/api/v1/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Usuario","email":"%s","senha":"%s"}
                                """.formatted(email, senha)))
                .andExpect(status().isCreated());
    }

    private MockHttpSession login(String email, String senha) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .param("email", email)
                        .param("senha", senha))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private String criarDespesa(MockHttpSession session) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/despesas")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "descricao":"Despesa do usuario A",
                                  "valor":100.00,
                                  "status":"PENDENTE",
                                  "competencia":"2025-07",
                                  "categoria":"OUTROS"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asText();
    }

    @Test
    void usuario_b_nao_deve_atualizar_despesa_do_usuario_a() throws Exception {
        cadastrar("a@isolation.com", "senha123");
        cadastrar("b@isolation.com", "senha123");

        MockHttpSession sessionA = login("a@isolation.com", "senha123");
        String despesaId = criarDespesa(sessionA);

        MockHttpSession sessionB = login("b@isolation.com", "senha123");

        mockMvc.perform(patch("/api/v1/despesas/{id}/status", despesaId)
                        .session(sessionB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"PAGA"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void usuario_b_nao_deve_remover_despesa_do_usuario_a() throws Exception {
        cadastrar("a2@isolation.com", "senha123");
        cadastrar("b2@isolation.com", "senha123");

        MockHttpSession sessionA = login("a2@isolation.com", "senha123");
        String despesaId = criarDespesa(sessionA);

        MockHttpSession sessionB = login("b2@isolation.com", "senha123");

        mockMvc.perform(delete("/api/v1/despesas/{id}", despesaId).session(sessionB))
                .andExpect(status().isNotFound());
    }

    @Test
    void usuario_so_ve_suas_proprias_despesas_na_listagem() throws Exception {
        cadastrar("a3@isolation.com", "senha123");
        cadastrar("b3@isolation.com", "senha123");

        MockHttpSession sessionA = login("a3@isolation.com", "senha123");
        criarDespesa(sessionA);

        MockHttpSession sessionB = login("b3@isolation.com", "senha123");

        MvcResult result = mockMvc.perform(get("/api/v1/despesas")
                        .session(sessionB)
                        .param("competencia", "2025-07"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        Assertions.assertThat(json.get("despesas").size()).isZero();
    }
}
