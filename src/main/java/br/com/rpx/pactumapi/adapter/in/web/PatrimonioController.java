package br.com.rpx.pactumapi.adapter.in.web;

import br.com.rpx.pactumapi.application.dto.request.CadastrarPatrimonioRequest;
import br.com.rpx.pactumapi.application.dto.response.ListaPatrimonioResponse;
import br.com.rpx.pactumapi.application.dto.response.PatrimonioResponse;
import br.com.rpx.pactumapi.application.mapper.PatrimonioMapper;
import br.com.rpx.pactumapi.config.security.UsuarioAutenticadoResolver;
import br.com.rpx.pactumapi.domain.port.in.CadastrarPatrimonioUseCase;
import br.com.rpx.pactumapi.domain.port.in.ConsultarPatrimonioUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/patrimonio")
@Tag(name = "Patrimônio", description = "Gestão de patrimônio guardado")
public class PatrimonioController {

    private final CadastrarPatrimonioUseCase cadastrarUseCase;
    private final ConsultarPatrimonioUseCase consultarUseCase;
    private final UsuarioAutenticadoResolver usuarioAutenticadoResolver;

    public PatrimonioController(CadastrarPatrimonioUseCase cadastrarUseCase,
                                ConsultarPatrimonioUseCase consultarUseCase,
                                UsuarioAutenticadoResolver usuarioAutenticadoResolver) {
        this.cadastrarUseCase = cadastrarUseCase;
        this.consultarUseCase = consultarUseCase;
        this.usuarioAutenticadoResolver = usuarioAutenticadoResolver;
    }

    @Operation(summary = "Cadastrar item de patrimônio")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Patrimônio criado"),
            @ApiResponse(responseCode = "422", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<PatrimonioResponse> cadastrar(@Valid @RequestBody CadastrarPatrimonioRequest request) {
        UUID usuarioId = usuarioAutenticadoResolver.getUsuarioId();
        var patrimonio = cadastrarUseCase.cadastrar(PatrimonioMapper.toDomain(request), usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(PatrimonioMapper.toResponse(patrimonio));
    }

    @Operation(summary = "Consultar patrimônio por competência")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada"),
            @ApiResponse(responseCode = "400", description = "Parâmetro competencia ausente ou inválido")
    })
    @GetMapping
    public ResponseEntity<ListaPatrimonioResponse> consultar(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth competencia) {
        UUID usuarioId = usuarioAutenticadoResolver.getUsuarioId();
        var itens = consultarUseCase.consultar(competencia, usuarioId);
        return ResponseEntity.ok(PatrimonioMapper.toListResponse(itens));
    }
}
