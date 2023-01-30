package ir.ac.iust.comp.sa.web.rest;

import ir.ac.iust.comp.sa.repository.LayerRepository;
import ir.ac.iust.comp.sa.service.LayerService;
import ir.ac.iust.comp.sa.service.dto.LayerDTO;
import ir.ac.iust.comp.sa.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link ir.ac.iust.comp.sa.domain.Layer}.
 */
@RestController
@RequestMapping("/api")
public class LayerResource {

    private final Logger log = LoggerFactory.getLogger(LayerResource.class);

    private static final String ENTITY_NAME = "gameserviceLayer";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final LayerService layerService;

    private final LayerRepository layerRepository;

    public LayerResource(LayerService layerService, LayerRepository layerRepository) {
        this.layerService = layerService;
        this.layerRepository = layerRepository;
    }

    /**
     * {@code POST  /layers} : Create a new layer.
     *
     * @param layerDTO the layerDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new layerDTO, or with status {@code 400 (Bad Request)} if the layer has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/layers")
    public Mono<ResponseEntity<LayerDTO>> createLayer(@RequestBody LayerDTO layerDTO) throws URISyntaxException {
        log.debug("REST request to save Layer : {}", layerDTO);
        if (layerDTO.getId() != null) {
            throw new BadRequestAlertException("A new layer cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return layerService
            .save(layerDTO)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/layers/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /layers/:id} : Updates an existing layer.
     *
     * @param id the id of the layerDTO to save.
     * @param layerDTO the layerDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated layerDTO,
     * or with status {@code 400 (Bad Request)} if the layerDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the layerDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/layers/{id}")
    public Mono<ResponseEntity<LayerDTO>> updateLayer(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody LayerDTO layerDTO
    ) throws URISyntaxException {
        log.debug("REST request to update Layer : {}, {}", id, layerDTO);
        if (layerDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, layerDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return layerRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return layerService
                    .save(layerDTO)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(result ->
                        ResponseEntity
                            .ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
                            .body(result)
                    );
            });
    }

    /**
     * {@code PATCH  /layers/:id} : Partial updates given fields of an existing layer, field will ignore if it is null
     *
     * @param id the id of the layerDTO to save.
     * @param layerDTO the layerDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated layerDTO,
     * or with status {@code 400 (Bad Request)} if the layerDTO is not valid,
     * or with status {@code 404 (Not Found)} if the layerDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the layerDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/layers/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<LayerDTO>> partialUpdateLayer(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody LayerDTO layerDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update Layer partially : {}, {}", id, layerDTO);
        if (layerDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, layerDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return layerRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<LayerDTO> result = layerService.partialUpdate(layerDTO);

                return result
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(res ->
                        ResponseEntity
                            .ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, res.getId().toString()))
                            .body(res)
                    );
            });
    }

    /**
     * {@code GET  /layers} : get all the layers.
     *
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of layers in body.
     */
    @GetMapping("/layers")
    public Mono<ResponseEntity<List<LayerDTO>>> getAllLayers(Pageable pageable, ServerHttpRequest request) {
        log.debug("REST request to get a page of Layers");
        return layerService
            .countAll()
            .zipWith(layerService.findAll(pageable).collectList())
            .map(countWithEntities -> {
                return ResponseEntity
                    .ok()
                    .headers(
                        PaginationUtil.generatePaginationHttpHeaders(
                            UriComponentsBuilder.fromHttpRequest(request),
                            new PageImpl<>(countWithEntities.getT2(), pageable, countWithEntities.getT1())
                        )
                    )
                    .body(countWithEntities.getT2());
            });
    }

    /**
     * {@code GET  /layers/:id} : get the "id" layer.
     *
     * @param id the id of the layerDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the layerDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/layers/{id}")
    public Mono<ResponseEntity<LayerDTO>> getLayer(@PathVariable Long id) {
        log.debug("REST request to get Layer : {}", id);
        Mono<LayerDTO> layerDTO = layerService.findOne(id);
        return ResponseUtil.wrapOrNotFound(layerDTO);
    }

    /**
     * {@code DELETE  /layers/:id} : delete the "id" layer.
     *
     * @param id the id of the layerDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/layers/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Void>> deleteLayer(@PathVariable Long id) {
        log.debug("REST request to delete Layer : {}", id);
        return layerService
            .delete(id)
            .map(result ->
                ResponseEntity
                    .noContent()
                    .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
                    .build()
            );
    }

    /**
     * {@code SEARCH  /_search/layers?query=:query} : search for the layer corresponding
     * to the query.
     *
     * @param query the query of the layer search.
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @return the result of the search.
     */
    @GetMapping("/_search/layers")
    public Mono<ResponseEntity<Flux<LayerDTO>>> searchLayers(@RequestParam String query, Pageable pageable, ServerHttpRequest request) {
        log.debug("REST request to search for a page of Layers for query {}", query);
        return layerService
            .searchCount()
            .map(total -> new PageImpl<>(new ArrayList<>(), pageable, total))
            .map(page -> PaginationUtil.generatePaginationHttpHeaders(UriComponentsBuilder.fromHttpRequest(request), page))
            .map(headers -> ResponseEntity.ok().headers(headers).body(layerService.search(query, pageable)));
    }
}
