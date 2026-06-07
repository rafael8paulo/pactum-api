package br.com.rpx.pactumapi.adapter.in.web;

import br.com.rpx.pactumapi.application.dto.request.CadastroUsuarioRequest;
import br.com.rpx.pactumapi.application.dto.response.UsuarioResponse;
import br.com.rpx.pactumapi.application.mapper.UsuarioMapper;
import br.com.rpx.pactumapi.config.security.UsuarioAutenticadoResolver;
import br.com.rpx.pactumapi.domain.model.Usuario;
import br.com.rpx.pactumapi.domain.port.in.CadastrarUsuarioUseCase;
import br.com.rpx.pactumapi.domain.port.out.BuscarUsuarioPorEmailPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticação", description = "Cadastro, login e perfil do usuário")
public class AuthController {

    private final CadastrarUsuarioUseCase cadastrarUseCase;
    private final BuscarUsuarioPorEmailPort buscarUsuarioPorEmailPort;
    private final UsuarioAutenticadoResolver usuarioAutenticadoResolver;

    public AuthController(CadastrarUsuarioUseCase cadastrarUseCase,
                          BuscarUsuarioPorEmailPort buscarUsuarioPorEmailPort,
                          UsuarioAutenticadoResolver usuarioAutenticadoResolver) {
        this.cadastrarUseCase = cadastrarUseCase;
        this.buscarUsuarioPorEmailPort = buscarUsuarioPorEmailPort;
        this.usuarioAutenticadoResolver = usuarioAutenticadoResolver;
    }

    @Operation(summary = "Cadastrar novo usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário criado"),
            @ApiResponse(responseCode = "409", description = "Email já cadastrado"),
            @ApiResponse(responseCode = "422", description = "Dados inválidos")
    })
    @PostMapping("/cadastro")
    public ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody CadastroUsuarioRequest request) {
        Usuario usuario = cadastrarUseCase.cadastrar(UsuarioMapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioMapper.toResponse(usuario));
    }

    @Operation(summary = "Obter perfil do usuário autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil retornado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> me(@AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = buscarUsuarioPorEmailPort.buscarPorEmail(userDetails.getUsername())
                .orElseThrow();
        return ResponseEntity.ok(UsuarioMapper.toResponse(usuario));
    }
}
