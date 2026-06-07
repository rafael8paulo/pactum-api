package br.com.rpx.pactumapi.adapter.in.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:authit;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.session.store-type=none"
})
class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    private static final String EMAIL = "integracao@test.com";
    private static final String SENHA = "senha123";

    private void cadastrar(String email, String senha) throws Exception {
        mockMvc.perform(post("/api/v1/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Usuario Teste","email":"%s","senha":"%s"}
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

    @Test
    void deve_cadastrar_usuario_e_retornar_201() throws Exception {
        mockMvc.perform(post("/api/v1/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Novo","email":"novo@test.com","senha":"senha123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("novo@test.com"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void deve_retornar_409_quando_email_ja_cadastrado() throws Exception {
        cadastrar("duplicado@test.com", SENHA);

        mockMvc.perform(post("/api/v1/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Outro","email":"duplicado@test.com","senha":"outrasenha"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void deve_fazer_login_e_retornar_200_com_sessao() throws Exception {
        cadastrar(EMAIL, SENHA);

        mockMvc.perform(post("/api/v1/auth/login")
                        .param("email", EMAIL)
                        .param("senha", SENHA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL));
    }

    @Test
    void deve_retornar_401_em_login_com_credenciais_invalidas() throws Exception {
        cadastrar("valido@test.com", SENHA);

        mockMvc.perform(post("/api/v1/auth/login")
                        .param("email", "valido@test.com")
                        .param("senha", "senhaerrada"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deve_acessar_me_com_sessao_valida() throws Exception {
        cadastrar("me@test.com", SENHA);
        MockHttpSession session = login("me@test.com", SENHA);

        mockMvc.perform(get("/api/v1/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("me@test.com"));
    }

    @Test
    void deve_retornar_401_em_me_sem_autenticacao() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deve_fazer_logout_e_retornar_204() throws Exception {
        cadastrar("logout@test.com", SENHA);
        MockHttpSession session = login("logout@test.com", SENHA);

        mockMvc.perform(post("/api/v1/auth/logout").session(session))
                .andExpect(status().isNoContent());
    }

    @Test
    void deve_retornar_401_apos_logout() throws Exception {
        cadastrar("poslogout@test.com", SENHA);
        MockHttpSession session = login("poslogout@test.com", SENHA);

        mockMvc.perform(post("/api/v1/auth/logout").session(session))
                .andExpect(status().isNoContent());

        session.invalidate();

        mockMvc.perform(get("/api/v1/auth/me").session(session))
                .andExpect(status().isUnauthorized());
    }
}
