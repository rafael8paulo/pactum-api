package br.com.rpx.pactumapi.adapter.in.web;

import br.com.rpx.pactumapi.application.dto.request.AtualizarStatusDespesaRequest;
import br.com.rpx.pactumapi.application.dto.request.CadastrarDespesaRequest;
import br.com.rpx.pactumapi.application.dto.request.EditarDespesaRequest;
import br.com.rpx.pactumapi.application.dto.response.DespesaResponse;
import br.com.rpx.pactumapi.application.dto.response.ListaDespesasResponse;
import br.com.rpx.pactumapi.application.mapper.DespesaMapper;
import br.com.rpx.pactumapi.config.security.UsuarioAutenticadoResolver;
import br.com.rpx.pactumapi.domain.model.CategoriaDespesa;
import br.com.rpx.pactumapi.domain.model.StatusDespesa;
import br.com.rpx.pactumapi.domain.port.in.AtualizarStatusDespesaUseCase;
import br.com.rpx.pactumapi.domain.port.in.CadastrarDespesaUseCase;
import br.com.rpx.pactumapi.domain.port.in.EditarDespesaUseCase;
import br.com.rpx.pactumapi.domain.port.in.ListarDespesasUseCase;
import br.com.rpx.pactumapi.domain.port.in.RemoverDespesaUseCase;
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
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping("/api/v1/despesas")
@Tag(name = "Despesas", description = "Gestão de despesas pessoais")
public class DespesaController {

    private final CadastrarDespesaUseCase cadastrarUseCase;
    private final ListarDespesasUseCase listarUseCase;
    private final AtualizarStatusDespesaUseCase atualizarStatusUseCase;
    private final EditarDespesaUseCase editarUseCase;
    private final RemoverDespesaUseCase removerUseCase;
    private final UsuarioAutenticadoResolver usuarioAutenticadoResolver;

    public DespesaController(CadastrarDespesaUseCase cadastrarUseCase,
                             ListarDespesasUseCase listarUseCase,
                             AtualizarStatusDespesaUseCase atualizarStatusUseCase,
                             EditarDespesaUseCase editarUseCase,
                             RemoverDespesaUseCase removerUseCase,
                             UsuarioAutenticadoResolver usuarioAutenticadoResolver) {
        this.cadastrarUseCase = cadastrarUseCase;
        this.listarUseCase = listarUseCase;
        this.atualizarStatusUseCase = atualizarStatusUseCase;
        this.editarUseCase = editarUseCase;
        this.removerUseCase = removerUseCase;
        this.usuarioAutenticadoResolver = usuarioAutenticadoResolver;
    }

    @Operation(summary = "Cadastrar despesa")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Despesa criada"),
            @ApiResponse(responseCode = "422", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<DespesaResponse> cadastrar(@Valid @RequestBody CadastrarDespesaRequest request) {
        UUID usuarioId = usuarioAutenticadoResolver.getUsuarioId();
        var despesa = cadastrarUseCase.cadastrar(DespesaMapper.toDomain(request), usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(DespesaMapper.toResponse(despesa));
    }

    @Operation(summary = "Listar despesas por competência")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos")
    })
    @GetMapping
    public ResponseEntity<ListaDespesasResponse> listar(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth competencia,
            @RequestParam(required = false) CategoriaDespesa categoria,
            @RequestParam(required = false) StatusDespesa status) {
        UUID usuarioId = usuarioAutenticadoResolver.getUsuarioId();
        var despesas = listarUseCase.listar(competencia, categoria, status, usuarioId);
        return ResponseEntity.ok(DespesaMapper.toListResponse(despesas));
    }

    @Operation(summary = "Atualizar status de despesa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status atualizado"),
            @ApiResponse(responseCode = "404", description = "Despesa não encontrada"),
            @ApiResponse(responseCode = "422", description = "Dados inválidos")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<DespesaResponse> atualizarStatus(
            @PathVariable UUID id,
            @Valid @RequestBody AtualizarStatusDespesaRequest request) {
        UUID usuarioId = usuarioAutenticadoResolver.getUsuarioId();
        var despesa = atualizarStatusUseCase.atualizar(id, request.status(), usuarioId);
        return ResponseEntity.ok(DespesaMapper.toResponse(despesa));
    }

    @Operation(summary = "Editar despesa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Despesa editada"),
            @ApiResponse(responseCode = "404", description = "Despesa não encontrada"),
            @ApiResponse(responseCode = "422", description = "Dados inválidos")
    })
    @PutMapping("/{id}")
    public ResponseEntity<DespesaResponse> editar(
            @PathVariable UUID id,
            @Valid @RequestBody EditarDespesaRequest request) {
        UUID usuarioId = usuarioAutenticadoResolver.getUsuarioId();
        var despesa = editarUseCase.editar(id, DespesaMapper.toDomain(request), usuarioId);
        return ResponseEntity.ok(DespesaMapper.toResponse(despesa));
    }

    @Operation(summary = "Remover despesa")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Despesa removida"),
            @ApiResponse(responseCode = "404", description = "Despesa não encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        UUID usuarioId = usuarioAutenticadoResolver.getUsuarioId();
        removerUseCase.remover(id, usuarioId);
        return ResponseEntity.noContent().build();
    }
}
