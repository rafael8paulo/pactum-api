package br.com.rpx.pactumapi.adapter.in.web;

import br.com.rpx.pactumapi.application.dto.request.CadastrarReceitaRequest;
import br.com.rpx.pactumapi.application.dto.request.EditarReceitaRequest;
import br.com.rpx.pactumapi.application.dto.response.ListaReceitasResponse;
import br.com.rpx.pactumapi.application.dto.response.ReceitaResponse;
import br.com.rpx.pactumapi.application.mapper.ReceitaMapper;
import br.com.rpx.pactumapi.config.security.UsuarioAutenticadoResolver;
import br.com.rpx.pactumapi.domain.model.CategoriaReceita;
import br.com.rpx.pactumapi.domain.port.in.CadastrarReceitaUseCase;
import br.com.rpx.pactumapi.domain.port.in.EditarReceitaUseCase;
import br.com.rpx.pactumapi.domain.port.in.ListarReceitasUseCase;
import br.com.rpx.pactumapi.domain.port.in.RemoverReceitaUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/receitas")
@Tag(name = "Receitas", description = "Gestão de receitas pessoais")
public class ReceitaController {

    private final CadastrarReceitaUseCase cadastrarUseCase;
    private final ListarReceitasUseCase listarUseCase;
    private final EditarReceitaUseCase editarUseCase;
    private final RemoverReceitaUseCase removerUseCase;
    private final UsuarioAutenticadoResolver usuarioAutenticadoResolver;

    public ReceitaController(CadastrarReceitaUseCase cadastrarUseCase,
                             ListarReceitasUseCase listarUseCase,
                             EditarReceitaUseCase editarUseCase,
                             RemoverReceitaUseCase removerUseCase,
                             UsuarioAutenticadoResolver usuarioAutenticadoResolver) {
        this.cadastrarUseCase = cadastrarUseCase;
        this.listarUseCase = listarUseCase;
        this.editarUseCase = editarUseCase;
        this.removerUseCase = removerUseCase;
        this.usuarioAutenticadoResolver = usuarioAutenticadoResolver;
    }

    @Operation(summary = "Cadastrar receita")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Receita criada"),
            @ApiResponse(responseCode = "422", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<ReceitaResponse> cadastrar(@Valid @RequestBody CadastrarReceitaRequest request) {
        UUID usuarioId = usuarioAutenticadoResolver.getUsuarioId();
        var receita = cadastrarUseCase.cadastrar(ReceitaMapper.toDomain(request), usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ReceitaMapper.toResponse(receita));
    }

    @Operation(summary = "Listar receitas por competência")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos")
    })
    @GetMapping
    public ResponseEntity<ListaReceitasResponse> listar(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth competencia,
            @RequestParam(required = false) CategoriaReceita categoria) {
        UUID usuarioId = usuarioAutenticadoResolver.getUsuarioId();
        var receitas = listarUseCase.listar(competencia, categoria, usuarioId);
        return ResponseEntity.ok(ReceitaMapper.toListResponse(receitas));
    }

    @Operation(summary = "Editar receita")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Receita editada"),
            @ApiResponse(responseCode = "404", description = "Receita não encontrada"),
            @ApiResponse(responseCode = "422", description = "Dados inválidos")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ReceitaResponse> editar(
            @PathVariable UUID id,
            @Valid @RequestBody EditarReceitaRequest request) {
        UUID usuarioId = usuarioAutenticadoResolver.getUsuarioId();
        var receita = editarUseCase.editar(id, ReceitaMapper.toDomain(request), usuarioId);
        return ResponseEntity.ok(ReceitaMapper.toResponse(receita));
    }

    @Operation(summary = "Remover receita")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Receita removida"),
            @ApiResponse(responseCode = "404", description = "Receita não encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        UUID usuarioId = usuarioAutenticadoResolver.getUsuarioId();
        removerUseCase.remover(id, usuarioId);
        return ResponseEntity.noContent().build();
    }
}
