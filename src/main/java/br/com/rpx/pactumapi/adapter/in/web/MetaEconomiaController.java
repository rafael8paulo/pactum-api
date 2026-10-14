package br.com.rpx.pactumapi.adapter.in.web;

import br.com.rpx.pactumapi.application.dto.request.DefinirMetaRequest;
import br.com.rpx.pactumapi.application.dto.response.HistoricoMetasResponse;
import br.com.rpx.pactumapi.application.dto.response.MetaEconomiaResponse;
import br.com.rpx.pactumapi.application.dto.response.ProgressoMetaResponse;
import br.com.rpx.pactumapi.application.mapper.MetaEconomiaMapper;
import br.com.rpx.pactumapi.config.security.UsuarioAutenticadoResolver;
import br.com.rpx.pactumapi.domain.port.in.ConsultarHistoricoMetasUseCase;
import br.com.rpx.pactumapi.domain.port.in.ConsultarProgressoMetaUseCase;
import br.com.rpx.pactumapi.domain.port.in.DefinirMetaEconomiaUseCase;
import br.com.rpx.pactumapi.domain.port.in.RemoverMetaEconomiaUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/metas-economia")
@RequiredArgsConstructor
@Tag(name = "Metas de Economia", description = "Meta mensal de economia, progresso e histórico")
public class MetaEconomiaController {

    private final DefinirMetaEconomiaUseCase definirUseCase;
    private final ConsultarProgressoMetaUseCase progressoUseCase;
    private final RemoverMetaEconomiaUseCase removerUseCase;
    private final ConsultarHistoricoMetasUseCase historicoUseCase;
    private final UsuarioAutenticadoResolver usuarioAutenticadoResolver;

    @Operation(summary = "Definir ou ajustar a meta de uma competência")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Meta definida"),
            @ApiResponse(responseCode = "400", description = "Competência anterior ao mês corrente"),
            @ApiResponse(responseCode = "422", description = "Valor inválido")
    })
    @PutMapping("/{competencia}")
    public ResponseEntity<MetaEconomiaResponse> definir(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM") YearMonth competencia,
            @Valid @RequestBody DefinirMetaRequest request) {
        UUID usuarioId = usuarioAutenticadoResolver.getUsuarioId();
        return ResponseEntity.ok(MetaEconomiaMapper.toResponse(
                definirUseCase.definir(competencia, request.valor(), usuarioId)));
    }

    @Operation(summary = "Consultar progresso da meta de uma competência")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Progresso retornado"),
            @ApiResponse(responseCode = "404", description = "Sem meta para a competência")
    })
    @GetMapping("/{competencia}")
    public ResponseEntity<ProgressoMetaResponse> progresso(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM") YearMonth competencia) {
        UUID usuarioId = usuarioAutenticadoResolver.getUsuarioId();
        return ResponseEntity.ok(MetaEconomiaMapper.toResponse(progressoUseCase.consultar(competencia, usuarioId)));
    }

    @Operation(summary = "Remover a meta de uma competência")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Meta removida"),
            @ApiResponse(responseCode = "400", description = "Competência anterior ao mês corrente"),
            @ApiResponse(responseCode = "404", description = "Sem meta para a competência")
    })
    @DeleteMapping("/{competencia}")
    public ResponseEntity<Void> remover(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM") YearMonth competencia) {
        UUID usuarioId = usuarioAutenticadoResolver.getUsuarioId();
        removerUseCase.remover(competencia, usuarioId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Consultar histórico de desempenho das metas de um ano")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico retornado"),
            @ApiResponse(responseCode = "400", description = "Parâmetro ano ausente ou inválido")
    })
    @GetMapping("/historico")
    public ResponseEntity<HistoricoMetasResponse> historico(@RequestParam int ano) {
        UUID usuarioId = usuarioAutenticadoResolver.getUsuarioId();
        return ResponseEntity.ok(MetaEconomiaMapper.toHistoricoResponse(ano, historicoUseCase.consultar(ano, usuarioId)));
    }
}
