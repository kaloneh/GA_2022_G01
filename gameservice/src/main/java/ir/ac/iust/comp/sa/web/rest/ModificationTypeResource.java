package ir.ac.iust.comp.sa.web.rest;

import ir.ac.iust.comp.sa.domain.ModificationType;
import ir.ac.iust.comp.sa.repository.ModificationTypeRepository;
import ir.ac.iust.comp.sa.repository.search.ModificationTypeSearchRepository;
import ir.ac.iust.comp.sa.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link ir.ac.iust.comp.sa.domain.ModificationType}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class ModificationTypeResource {

    private final Logger log = LoggerFactory.getLogger(ModificationTypeResource.class);

    private static final String ENTITY_NAME = "gameserviceModificationType";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ModificationTypeRepository modificationTypeRepository;

    private final ModificationTypeSearchRepository modificationTypeSearchRepository;

    public ModificationTypeResource(
        ModificationTypeRepository modificationTypeRepository,
        ModificationTypeSearchRepository modificationTypeSearchRepository
    ) {
        this.modificationTypeRepository = modificationTypeRepository;
        this.modificationTypeSearchRepository = modificationTypeSearchRepository;
    }

    /**
     * {@code POST  /modification-types} : Create a new modificationType.
     *
     * @param modificationType the modificationType to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new modificationType, or with status {@code 400 (Bad Request)} if the modificationType has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/modification-types")
    public Mono<ResponseEntity<ModificationType>> createModificationType(@RequestBody ModificationType modificationType)
        throws URISyntaxException {
        log.debug("REST request to save ModificationType : {}", modificationType);
        if (modificationType.getId() != null) {
            throw new BadRequestAlertException("A new modificationType cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return modificationTypeRepository
            .save(modificationType)
            .flatMap(modificationTypeSearchRepository::save)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/modification-types/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /modification-types/:id} : Updates an existing modificationType.
     *
     * @param id the id of the modificationType to save.
     * @param modificationType the modificationType to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated modificationType,
     * or with status {@code 400 (Bad Request)} if the modificationType is not valid,
     * or with status {@code 500 (Internal Server Error)} if the modificationType couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/modification-types/{id}")
    public Mono<ResponseEntity<ModificationType>> updateModificationType(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody ModificationType modificationType
    ) throws URISyntaxException {
        log.debug("REST request to update ModificationType : {}, {}", id, modificationType);
        if (modificationType.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, modificationType.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return modificationTypeRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return modificationTypeRepository
                    .save(modificationType)
                    .flatMap(modificationTypeSearchRepository::save)
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
     * {@code PATCH  /modification-types/:id} : Partial updates given fields of an existing modificationType, field will ignore if it is null
     *
     * @param id the id of the modificationType to save.
     * @param modificationType the modificationType to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated modificationType,
     * or with status {@code 400 (Bad Request)} if the modificationType is not valid,
     * or with status {@code 404 (Not Found)} if the modificationType is not found,
     * or with status {@code 500 (Internal Server Error)} if the modificationType couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/modification-types/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<ModificationType>> partialUpdateModificationType(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody ModificationType modificationType
    ) throws URISyntaxException {
        log.debug("REST request to partial update ModificationType partially : {}, {}", id, modificationType);
        if (modificationType.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, modificationType.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return modificationTypeRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<ModificationType> result = modificationTypeRepository
                    .findById(modificationType.getId())
                    .map(existingModificationType -> {
                        if (modificationType.getType() != null) {
                            existingModificationType.setType(modificationType.getType());
                        }

                        return existingModificationType;
                    })
                    .flatMap(modificationTypeRepository::save)
                    .flatMap(savedModificationType -> {
                        modificationTypeSearchRepository.save(savedModificationType);

                        return Mono.just(savedModificationType);
                    });

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
     * {@code GET  /modification-types} : get all the modificationTypes.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of modificationTypes in body.
     */
    @GetMapping("/modification-types")
    public Mono<List<ModificationType>> getAllModificationTypes() {
        log.debug("REST request to get all ModificationTypes");
        return modificationTypeRepository.findAll().collectList();
    }

    /**
     * {@code GET  /modification-types} : get all the modificationTypes as a stream.
     * @return the {@link Flux} of modificationTypes.
     */
    @GetMapping(value = "/modification-types", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<ModificationType> getAllModificationTypesAsStream() {
        log.debug("REST request to get all ModificationTypes as a stream");
        return modificationTypeRepository.findAll();
    }

    /**
     * {@code GET  /modification-types/:id} : get the "id" modificationType.
     *
     * @param id the id of the modificationType to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the modificationType, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/modification-types/{id}")
    public Mono<ResponseEntity<ModificationType>> getModificationType(@PathVariable Long id) {
        log.debug("REST request to get ModificationType : {}", id);
        Mono<ModificationType> modificationType = modificationTypeRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(modificationType);
    }

    /**
     * {@code DELETE  /modification-types/:id} : delete the "id" modificationType.
     *
     * @param id the id of the modificationType to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/modification-types/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Void>> deleteModificationType(@PathVariable Long id) {
        log.debug("REST request to delete ModificationType : {}", id);
        return modificationTypeRepository
            .deleteById(id)
            .then(modificationTypeSearchRepository.deleteById(id))
            .map(result ->
                ResponseEntity
                    .noContent()
                    .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
                    .build()
            );
    }

    /**
     * {@code SEARCH  /_search/modification-types?query=:query} : search for the modificationType corresponding
     * to the query.
     *
     * @param query the query of the modificationType search.
     * @return the result of the search.
     */
    @GetMapping("/_search/modification-types")
    public Mono<List<ModificationType>> searchModificationTypes(@RequestParam String query) {
        log.debug("REST request to search ModificationTypes for query {}", query);
        return modificationTypeSearchRepository.search(query).collectList();
    }
}
