package ir.ac.iust.comp.sa.web.rest;

import ir.ac.iust.comp.sa.domain.MyApplication;
import ir.ac.iust.comp.sa.repository.MyApplicationRepository;
import ir.ac.iust.comp.sa.repository.search.MyApplicationSearchRepository;
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
 * REST controller for managing {@link ir.ac.iust.comp.sa.domain.MyApplication}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class MyApplicationResource {

    private final Logger log = LoggerFactory.getLogger(MyApplicationResource.class);

    private static final String ENTITY_NAME = "gameserviceMyApplication";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final MyApplicationRepository myApplicationRepository;

    private final MyApplicationSearchRepository myApplicationSearchRepository;

    public MyApplicationResource(
        MyApplicationRepository myApplicationRepository,
        MyApplicationSearchRepository myApplicationSearchRepository
    ) {
        this.myApplicationRepository = myApplicationRepository;
        this.myApplicationSearchRepository = myApplicationSearchRepository;
    }

    /**
     * {@code POST  /my-applications} : Create a new myApplication.
     *
     * @param myApplication the myApplication to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new myApplication, or with status {@code 400 (Bad Request)} if the myApplication has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/my-applications")
    public Mono<ResponseEntity<MyApplication>> createMyApplication(@RequestBody MyApplication myApplication) throws URISyntaxException {
        log.debug("REST request to save MyApplication : {}", myApplication);
        if (myApplication.getId() != null) {
            throw new BadRequestAlertException("A new myApplication cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return myApplicationRepository
            .save(myApplication)
            .flatMap(myApplicationSearchRepository::save)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/my-applications/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /my-applications/:id} : Updates an existing myApplication.
     *
     * @param id the id of the myApplication to save.
     * @param myApplication the myApplication to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated myApplication,
     * or with status {@code 400 (Bad Request)} if the myApplication is not valid,
     * or with status {@code 500 (Internal Server Error)} if the myApplication couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/my-applications/{id}")
    public Mono<ResponseEntity<MyApplication>> updateMyApplication(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody MyApplication myApplication
    ) throws URISyntaxException {
        log.debug("REST request to update MyApplication : {}, {}", id, myApplication);
        if (myApplication.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, myApplication.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return myApplicationRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return myApplicationRepository
                    .save(myApplication)
                    .flatMap(myApplicationSearchRepository::save)
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
     * {@code PATCH  /my-applications/:id} : Partial updates given fields of an existing myApplication, field will ignore if it is null
     *
     * @param id the id of the myApplication to save.
     * @param myApplication the myApplication to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated myApplication,
     * or with status {@code 400 (Bad Request)} if the myApplication is not valid,
     * or with status {@code 404 (Not Found)} if the myApplication is not found,
     * or with status {@code 500 (Internal Server Error)} if the myApplication couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/my-applications/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<MyApplication>> partialUpdateMyApplication(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody MyApplication myApplication
    ) throws URISyntaxException {
        log.debug("REST request to partial update MyApplication partially : {}, {}", id, myApplication);
        if (myApplication.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, myApplication.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return myApplicationRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<MyApplication> result = myApplicationRepository
                    .findById(myApplication.getId())
                    .map(existingMyApplication -> {
                        return existingMyApplication;
                    })
                    .flatMap(myApplicationRepository::save)
                    .flatMap(savedMyApplication -> {
                        myApplicationSearchRepository.save(savedMyApplication);

                        return Mono.just(savedMyApplication);
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
     * {@code GET  /my-applications} : get all the myApplications.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of myApplications in body.
     */
    @GetMapping("/my-applications")
    public Mono<List<MyApplication>> getAllMyApplications() {
        log.debug("REST request to get all MyApplications");
        return myApplicationRepository.findAll().collectList();
    }

    /**
     * {@code GET  /my-applications} : get all the myApplications as a stream.
     * @return the {@link Flux} of myApplications.
     */
    @GetMapping(value = "/my-applications", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<MyApplication> getAllMyApplicationsAsStream() {
        log.debug("REST request to get all MyApplications as a stream");
        return myApplicationRepository.findAll();
    }

    /**
     * {@code GET  /my-applications/:id} : get the "id" myApplication.
     *
     * @param id the id of the myApplication to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the myApplication, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/my-applications/{id}")
    public Mono<ResponseEntity<MyApplication>> getMyApplication(@PathVariable Long id) {
        log.debug("REST request to get MyApplication : {}", id);
        Mono<MyApplication> myApplication = myApplicationRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(myApplication);
    }

    /**
     * {@code DELETE  /my-applications/:id} : delete the "id" myApplication.
     *
     * @param id the id of the myApplication to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/my-applications/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Void>> deleteMyApplication(@PathVariable Long id) {
        log.debug("REST request to delete MyApplication : {}", id);
        return myApplicationRepository
            .deleteById(id)
            .then(myApplicationSearchRepository.deleteById(id))
            .map(result ->
                ResponseEntity
                    .noContent()
                    .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
                    .build()
            );
    }

    /**
     * {@code SEARCH  /_search/my-applications?query=:query} : search for the myApplication corresponding
     * to the query.
     *
     * @param query the query of the myApplication search.
     * @return the result of the search.
     */
    @GetMapping("/_search/my-applications")
    public Mono<List<MyApplication>> searchMyApplications(@RequestParam String query) {
        log.debug("REST request to search MyApplications for query {}", query);
        return myApplicationSearchRepository.search(query).collectList();
    }
}
