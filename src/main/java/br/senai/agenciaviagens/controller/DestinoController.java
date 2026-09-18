package br.senai.agenciaviagens.controller;

import br.senai.agenciaviagens.dto.AvaliacaoRequestDTO;
import br.senai.agenciaviagens.dto.DestinoRequestDTO;
import br.senai.agenciaviagens.dto.DestinoResponseDTO;
import br.senai.agenciaviagens.service.DestinoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/destinos")
public class DestinoController {

    private final DestinoService destinoService;

    public DestinoController(DestinoService destinoService) {
        this.destinoService = destinoService;
    }

    /** Publico. */
    @GetMapping
    public ResponseEntity<Page<DestinoResponseDTO>> listar(
            @PageableDefault(size = 10, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(required = false) String nome,
            @RequestParam(defaultValue = "false") boolean somenteAtivos) {

        if (nome != null && !nome.isBlank()) {
            return ResponseEntity.ok(destinoService.buscarPorNome(nome, pageable));
        }
        if (somenteAtivos) {
            return ResponseEntity.ok(destinoService.listarAtivos(pageable));
        }
        return ResponseEntity.ok(destinoService.listar(pageable));
    }

    /** Publico. */
    @GetMapping("/{id}")
    public ResponseEntity<DestinoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(destinoService.buscarPorId(id));
    }

    /** Publico. */
    @GetMapping("/pais/{pais}")
    public ResponseEntity<List<DestinoResponseDTO>> buscarPorPais(@PathVariable String pais) {
        return ResponseEntity.ok(destinoService.buscarPorPais(pais));
    }

    /** Publico. */
    @GetMapping("/melhores-avaliados")
    public ResponseEntity<List<DestinoResponseDTO>> melhoresAvaliados(
            @RequestParam(defaultValue = "4.0") Double notaMinima) {
        return ResponseEntity.ok(destinoService.buscarPorAvaliacaoMinima(notaMinima));
    }

    /** Somente ADMIN. */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DestinoResponseDTO> cadastrar(@RequestBody @Valid DestinoRequestDTO dto,
                                                        UriComponentsBuilder uriBuilder) {
        DestinoResponseDTO criado = destinoService.cadastrar(dto);
        var uri = uriBuilder.path("/api/destinos/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(uri).body(criado);
    }

    /** Somente ADMIN. */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DestinoResponseDTO> atualizar(@PathVariable Long id,
                                                        @RequestBody @Valid DestinoRequestDTO dto) {
        return ResponseEntity.ok(destinoService.atualizar(id, dto));
    }

    /** Somente ADMIN. */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DestinoResponseDTO> alternarStatus(@PathVariable Long id,
                                                             @RequestParam boolean ativo) {
        return ResponseEntity.ok(destinoService.alternarStatus(id, ativo));
    }

    /** Somente ADMIN. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        destinoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    /** Autenticado: USER ou ADMIN. */
    @PostMapping("/{id}/avaliacoes")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<DestinoResponseDTO> avaliar(@PathVariable Long id,
                                                      @RequestBody @Valid AvaliacaoRequestDTO dto) {
        return ResponseEntity.ok(destinoService.avaliar(id, dto));
    }
}
