package br.com.rpx.pactumapi.adapter.in.web;

import br.com.rpx.pactumapi.application.dto.request.AtualizarStatusContaRecorrenteRequest;
import br.com.rpx.pactumapi.application.dto.request.CadastrarContaRecorrenteRequest;
import br.com.rpx.pactumapi.application.dto.request.EditarContaRecorrenteRequest;
import br.com.rpx.pactumapi.application.dto.response.ContaRecorrenteResponse;
import br.com.rpx.pactumapi.application.dto.response.ListaContasRecorrentesResponse;
import br.com.rpx.pactumapi.application.dto.response.ListaDespesasResponse;
import br.com.rpx.pactumapi.application.mapper.ContaRecorrenteMapper;
import br.com.rpx.pactumapi.application.mapper.DespesaMapper;
import br.com.rpx.pactumapi.config.security.UsuarioAutenticadoResolver;
import br.com.rpx.pactumapi.domain.model.StatusContaRecorrente;
import br.com.rpx.pactumapi.domain.port.in.AtualizarStatusContaRecorrenteUseCase;
import br.com.rpx.pactumapi.domain.port.in.CadastrarContaRecorrenteUseCase;
import br.com.rpx.pactumapi.domain.port.in.EditarContaRecorrenteUseCase;
import br.com.rpx.pactumapi.domain.port.in.GerarLancamentosRecorrentesUseCase;
import br.com.rpx.pactumapi.domain.port.in.GerarLoteLancamentosRecorrentesUseCase;
import br.com.rpx.pactumapi.domain.port.in.ListarContasRecorrentesUseCase;
import br.com.rpx.pactumapi.domain.port.in.RemoverContaRecorrenteUseCase;
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
@RequestMapping("/api/v1/contas-recorrentes")
@Tag(name = "Contas Recorrentes", description = "Gestão de contas recorrentes e geração de lançamentos")
public class ContaRecorrenteController {

    private final CadastrarContaRecorrenteUseCase cadastrarUseCase;
    private final ListarContasRecorrentesUseCase listarUseCase;
    private final EditarContaRecorrenteUseCase editarUseCase;
    private final AtualizarStatusContaRecorrenteUseCase atualizarStatusUseCase;
    private final RemoverContaRecorrenteUseCase removerUseCase;
    private final GerarLancamentosRecorrentesUseCase gerarLancamentosUseCase;
    private final GerarLoteLancamentosRecorrentesUseCase gerarLoteLancamentosUseCase;
    private final UsuarioAutenticadoResolver usuarioAutenticadoResolver;

    public ContaRecorrenteController(CadastrarContaRecorrenteUseCase cadastrarUseCase,
                                     ListarContasRecorrentesUseCase listarUseCase,
                                     EditarContaRecorrenteUseCase editarUseCase,
                                     AtualizarStatusContaRecorrenteUseCase atualizarStatusUseCase,
                                     RemoverContaRecorrenteUseCase removerUseCase,
                                     GerarLancamentosRecorrentesUseCase gerarLancamentosUseCase,
                                     GerarLoteLancamentosRecorrentesUseCase gerarLoteLancamentosUseCase,
                                     UsuarioAutenticadoResolver usuarioAutenticadoResolver) {
        this.cadastrarUseCase = cadastrarUseCase;
        this.listarUseCase = listarUseCase;
        this.editarUseCase = editarUseCase;
        this.atualizarStatusUseCase = atualizarStatusUseCase;
        this.removerUseCase = removerUseCase;
        this.gerarLancamentosUseCase = gerarLancamentosUseCase;
        this.gerarLoteLancamentosUseCase = gerarLoteLancamentosUseCase;
        this.usuarioAutenticadoResolver = usuarioAutenticadoResolver;
    }

    @Operation(summary = "Cadastrar conta recorrente")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Conta recorrente criada"),
            @ApiResponse(responseCode = "422", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<ContaRecorrenteResponse> cadastrar(@Valid @RequestBody CadastrarContaRecorrenteRequest request) {
        UUID usuarioId = usuarioAutenticadoResolver.getUsuarioId();
        var contaRecorrente = cadastrarUseCase.cadastrar(ContaRecorrenteMapper.toDomain(request), usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ContaRecorrenteMapper.toResponse(contaRecorrente));
    }

    @Operation(summary = "Listar contas recorrentes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada")
    })
    @GetMapping
    public ResponseEntity<ListaContasRecorrentesResponse> listar(
            @RequestParam(required = false) StatusContaRecorrente status) {
        UUID usuarioId = usuarioAutenticadoResolver.getUsuarioId();
        var contasRecorrentes = listarUseCase.listar(status, usuarioId);
        return ResponseEntity.ok(ContaRecorrenteMapper.toListResponse(contasRecorrentes));
    }

    @Operation(summary = "Editar conta recorrente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conta recorrente editada"),
            @ApiResponse(responseCode = "404", description = "Conta recorrente não encontrada"),
            @ApiResponse(responseCode = "422", description = "Dados inválidos")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ContaRecorrenteResponse> editar(
            @PathVariable UUID id,
            @Valid @RequestBody EditarContaRecorrenteRequest request) {
        UUID usuarioId = usuarioAutenticadoResolver.getUsuarioId();
        var contaRecorrente = editarUseCase.editar(id, ContaRecorrenteMapper.toDomain(request), usuarioId);
        return ResponseEntity.ok(ContaRecorrenteMapper.toResponse(contaRecorrente));
    }

    @Operation(summary = "Atualizar status da conta recorrente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status atualizado"),
            @ApiResponse(responseCode = "404", description = "Conta recorrente não encontrada")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<ContaRecorrenteResponse> atualizarStatus(
            @PathVariable UUID id,
            @Valid @RequestBody AtualizarStatusContaRecorrenteRequest request) {
        UUID usuarioId = usuarioAutenticadoResolver.getUsuarioId();
        var contaRecorrente = atualizarStatusUseCase.atualizar(id, request.status(), usuarioId);
        return ResponseEntity.ok(ContaRecorrenteMapper.toResponse(contaRecorrente));
    }

    @Operation(summary = "Remover conta recorrente")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Conta recorrente removida"),
            @ApiResponse(responseCode = "404", description = "Conta recorrente não encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        UUID usuarioId = usuarioAutenticadoResolver.getUsuarioId();
        removerUseCase.remover(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Gerar lançamentos do mês a partir das contas recorrentes ativas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Despesas geradas (pode ser uma lista vazia se já geradas anteriormente)"),
            @ApiResponse(responseCode = "400", description = "Competência ausente ou inválida")
    })
    @PostMapping("/gerar")
    public ResponseEntity<ListaDespesasResponse> gerar(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth competencia) {
        UUID usuarioId = usuarioAutenticadoResolver.getUsuarioId();
        var despesasGeradas = gerarLancamentosUseCase.gerar(competencia, usuarioId);
        return ResponseEntity.ok(DespesaMapper.toListResponse(despesasGeradas));
    }

    @Operation(summary = "Gerar todos os lançamentos de uma conta recorrente entre suas competências de início e fim")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Despesas geradas (pode ser uma lista vazia se já geradas anteriormente ou conta não ativa)"),
            @ApiResponse(responseCode = "404", description = "Conta recorrente não encontrada")
    })
    @PostMapping("/{id}/gerar-todos")
    public ResponseEntity<ListaDespesasResponse> gerarTodos(@PathVariable UUID id) {
        UUID usuarioId = usuarioAutenticadoResolver.getUsuarioId();
        var despesasGeradas = gerarLoteLancamentosUseCase.gerarTodos(id, usuarioId);
        return ResponseEntity.ok(DespesaMapper.toListResponse(despesasGeradas));
    }
}
