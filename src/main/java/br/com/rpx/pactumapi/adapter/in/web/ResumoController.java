package br.com.rpx.pactumapi.adapter.in.web;

import br.com.rpx.pactumapi.application.dto.response.HistoricoAnualResponse;
import br.com.rpx.pactumapi.application.dto.response.ResumoMensalResponse;
import br.com.rpx.pactumapi.application.mapper.ResumoMapper;
import br.com.rpx.pactumapi.config.security.UsuarioAutenticadoResolver;
import br.com.rpx.pactumapi.domain.port.in.ConsultarHistoricoAnualUseCase;
import br.com.rpx.pactumapi.domain.port.in.ConsultarResumoMensalUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/resumo")
@Tag(name = "Resumo", description = "Consolidação mensal e histórico anual")
public class ResumoController {

    private final ConsultarResumoMensalUseCase resumoMensalUseCase;
    private final ConsultarHistoricoAnualUseCase historicoAnualUseCase;
    private final UsuarioAutenticadoResolver usuarioAutenticadoResolver;

    public ResumoController(ConsultarResumoMensalUseCase resumoMensalUseCase,
                            ConsultarHistoricoAnualUseCase historicoAnualUseCase,
                            UsuarioAutenticadoResolver usuarioAutenticadoResolver) {
        this.resumoMensalUseCase = resumoMensalUseCase;
        this.historicoAnualUseCase = historicoAnualUseCase;
        this.usuarioAutenticadoResolver = usuarioAutenticadoResolver;
    }

    @Operation(summary = "Consultar resumo mensal")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resumo retornado"),
            @ApiResponse(responseCode = "400", description = "Parâmetro competencia ausente ou inválido")
    })
    @GetMapping
    public ResponseEntity<ResumoMensalResponse> resumoMensal(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth competencia) {
        UUID usuarioId = usuarioAutenticadoResolver.getUsuarioId();
        return ResponseEntity.ok(ResumoMapper.toResponse(resumoMensalUseCase.consultar(competencia, usuarioId)));
    }

    @Operation(summary = "Consultar histórico anual")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico retornado"),
            @ApiResponse(responseCode = "400", description = "Parâmetro ano ausente ou inválido")
    })
    @GetMapping("/anual")
    public ResponseEntity<HistoricoAnualResponse> historicoAnual(@RequestParam int ano) {
        UUID usuarioId = usuarioAutenticadoResolver.getUsuarioId();
        return ResponseEntity.ok(ResumoMapper.toHistoricoResponse(ano, historicoAnualUseCase.consultar(ano, usuarioId)));
    }
}
