package ir.ac.iust.comp.sa.web.rest;

import ir.ac.iust.comp.sa.domain.Application;
import ir.ac.iust.comp.sa.repository.ApplicationRepository;
import ir.ac.iust.comp.sa.repository.search.ApplicationSearchRepository;
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
 * REST controller for managing {@link ir.ac.iust.comp.sa.domain.Application}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class ApplicationResource {

    private final Logger log = LoggerFactory.getLogger(ApplicationResource.class);

    private static final String ENTITY_NAME = "gameserviceApplication";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ApplicationRepository applicationRepository;

    private final ApplicationSearchRepository applicationSearchRepository;

    public ApplicationResource(ApplicationRepository applicationRepository, ApplicationSearchRepository applicationSearchRepository) {
        this.applicationRepository = applicationRepository;
        this.applicationSearchRepository = applicationSearchRepository;
    }

    /**
     * {@code POST  /applications} : Create a new application.
     *
     * @param application the application to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new application, or with status {@code 400 (Bad Request)} if the application has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/applications")
    public Mono<ResponseEntity<Application>> createApplication(@RequestBody Application application) throws URISyntaxException {
        log.debug("REST request to save Application : {}", application);
        if (application.getId() != null) {
            throw new BadRequestAlertException("A new application cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return applicationRepository
            .save(application)
            .flatMap(applicationSearchRepository::save)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/applications/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /applications/:id} : Updates an existing application.
     *
     * @param id the id of the application to save.
     * @param application the application to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated application,
     * or with status {@code 400 (Bad Request)} if the application is not valid,
     * or with status {@code 500 (Internal Server Error)} if the application couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/applications/{id}")
    public Mono<ResponseEntity<Application>> updateApplication(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Application application
    ) throws URISyntaxException {
        log.debug("REST request to update Application : {}, {}", id, application);
        if (application.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, application.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return applicationRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return applicationRepository
                    .save(application)
                    .flatMap(applicationSearchRepository::save)
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
     * {@code PATCH  /applications/:id} : Partial updates given fields of an existing application, field will ignore if it is null
     *
     * @param id the id of the application to save.
     * @param application the application to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated application,
     * or with status {@code 400 (Bad Request)} if the application is not valid,
     * or with status {@code 404 (Not Found)} if the application is not found,
     * or with status {@code 500 (Internal Server Error)} if the application couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/applications/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<Application>> partialUpdateApplication(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Application application
    ) throws URISyntaxException {
        log.debug("REST request to partial update Application partially : {}, {}", id, application);
        if (application.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, application.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return applicationRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<Application> result = applicationRepository
                    .findById(application.getId())
                    .map(existingApplication -> {
                        if (application.getWidth() != null) {
                            existingApplication.setWidth(application.getWidth());
                        }
                        if (application.getHeight() != null) {
                            existingApplication.setHeight(application.getHeight());
                        }
                        if (application.getScreenBuffer() != null) {
                            existingApplication.setScreenBuffer(application.getScreenBuffer());
                        }
                        if (application.getScreenBufferContentType() != null) {
                            existingApplication.setScreenBufferContentType(application.getScreenBufferContentType());
                        }

                        return existingApplication;
                    })
                    .flatMap(applicationRepository::save)
                    .flatMap(savedApplication -> {
                        applicationSearchRepository.save(savedApplication);

                        return Mono.just(savedApplication);
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
     * {@code GET  /applications} : get all the applications.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of applications in body.
     */
    @GetMapping("/applications")
    public Mono<List<Application>> getAllApplications() {
        log.debug("REST request to get all Applications");
        return applicationRepository.findAll().collectList();
    }

    /**
     * {@code GET  /applications} : get all the applications as a stream.
     * @return the {@link Flux} of applications.
     */
    @GetMapping(value = "/applications", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<Application> getAllApplicationsAsStream() {
        log.debug("REST request to get all Applications as a stream");
        return applicationRepository.findAll();
    }

    /**
     * {@code GET  /applications/:id} : get the "id" application.
     *
     * @param id the id of the application to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the application, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/applications/{id}")
    public Mono<ResponseEntity<Application>> getApplication(@PathVariable Long id) {
        log.debug("REST request to get Application : {}", id);
        Mono<Application> application = applicationRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(application);
    }

    /**
     * {@code DELETE  /applications/:id} : delete the "id" application.
     *
     * @param id the id of the application to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/applications/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Void>> deleteApplication(@PathVariable Long id) {
        log.debug("REST request to delete Application : {}", id);
        return applicationRepository
            .deleteById(id)
            .then(applicationSearchRepository.deleteById(id))
            .map(result ->
                ResponseEntity
                    .noContent()
                    .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
                    .build()
            );
    }

    /**
     * {@code SEARCH  /_search/applications?query=:query} : search for the application corresponding
     * to the query.
     *
     * @param query the query of the application search.
     * @return the result of the search.
     */
    @GetMapping("/_search/applications")
    public Mono<List<Application>> searchApplications(@RequestParam String query) {
        log.debug("REST request to search Applications for query {}", query);
        return applicationSearchRepository.search(query).collectList();
    }
}
