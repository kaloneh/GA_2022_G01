package ir.ac.iust.comp.sa.web.rest;

import ir.ac.iust.comp.sa.domain.MyGameObject;
import ir.ac.iust.comp.sa.repository.MyGameObjectRepository;
import ir.ac.iust.comp.sa.repository.search.MyGameObjectSearchRepository;
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
 * REST controller for managing {@link ir.ac.iust.comp.sa.domain.MyGameObject}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class MyGameObjectResource {

    private final Logger log = LoggerFactory.getLogger(MyGameObjectResource.class);

    private static final String ENTITY_NAME = "gameserviceMyGameObject";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final MyGameObjectRepository myGameObjectRepository;

    private final MyGameObjectSearchRepository myGameObjectSearchRepository;

    public MyGameObjectResource(MyGameObjectRepository myGameObjectRepository, MyGameObjectSearchRepository myGameObjectSearchRepository) {
        this.myGameObjectRepository = myGameObjectRepository;
        this.myGameObjectSearchRepository = myGameObjectSearchRepository;
    }

    /**
     * {@code POST  /my-game-objects} : Create a new myGameObject.
     *
     * @param myGameObject the myGameObject to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new myGameObject, or with status {@code 400 (Bad Request)} if the myGameObject has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/my-game-objects")
    public Mono<ResponseEntity<MyGameObject>> createMyGameObject(@RequestBody MyGameObject myGameObject) throws URISyntaxException {
        log.debug("REST request to save MyGameObject : {}", myGameObject);
        if (myGameObject.getId() != null) {
            throw new BadRequestAlertException("A new myGameObject cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return myGameObjectRepository
            .save(myGameObject)
            .flatMap(myGameObjectSearchRepository::save)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/my-game-objects/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /my-game-objects/:id} : Updates an existing myGameObject.
     *
     * @param id the id of the myGameObject to save.
     * @param myGameObject the myGameObject to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated myGameObject,
     * or with status {@code 400 (Bad Request)} if the myGameObject is not valid,
     * or with status {@code 500 (Internal Server Error)} if the myGameObject couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/my-game-objects/{id}")
    public Mono<ResponseEntity<MyGameObject>> updateMyGameObject(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody MyGameObject myGameObject
    ) throws URISyntaxException {
        log.debug("REST request to update MyGameObject : {}, {}", id, myGameObject);
        if (myGameObject.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, myGameObject.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return myGameObjectRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return myGameObjectRepository
                    .save(myGameObject)
                    .flatMap(myGameObjectSearchRepository::save)
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
     * {@code PATCH  /my-game-objects/:id} : Partial updates given fields of an existing myGameObject, field will ignore if it is null
     *
     * @param id the id of the myGameObject to save.
     * @param myGameObject the myGameObject to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated myGameObject,
     * or with status {@code 400 (Bad Request)} if the myGameObject is not valid,
     * or with status {@code 404 (Not Found)} if the myGameObject is not found,
     * or with status {@code 500 (Internal Server Error)} if the myGameObject couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/my-game-objects/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<MyGameObject>> partialUpdateMyGameObject(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody MyGameObject myGameObject
    ) throws URISyntaxException {
        log.debug("REST request to partial update MyGameObject partially : {}, {}", id, myGameObject);
        if (myGameObject.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, myGameObject.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return myGameObjectRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<MyGameObject> result = myGameObjectRepository
                    .findById(myGameObject.getId())
                    .map(existingMyGameObject -> {
                        return existingMyGameObject;
                    })
                    .flatMap(myGameObjectRepository::save)
                    .flatMap(savedMyGameObject -> {
                        myGameObjectSearchRepository.save(savedMyGameObject);

                        return Mono.just(savedMyGameObject);
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
     * {@code GET  /my-game-objects} : get all the myGameObjects.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of myGameObjects in body.
     */
    @GetMapping("/my-game-objects")
    public Mono<List<MyGameObject>> getAllMyGameObjects() {
        log.debug("REST request to get all MyGameObjects");
        return myGameObjectRepository.findAll().collectList();
    }

    /**
     * {@code GET  /my-game-objects} : get all the myGameObjects as a stream.
     * @return the {@link Flux} of myGameObjects.
     */
    @GetMapping(value = "/my-game-objects", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<MyGameObject> getAllMyGameObjectsAsStream() {
        log.debug("REST request to get all MyGameObjects as a stream");
        return myGameObjectRepository.findAll();
    }

    /**
     * {@code GET  /my-game-objects/:id} : get the "id" myGameObject.
     *
     * @param id the id of the myGameObject to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the myGameObject, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/my-game-objects/{id}")
    public Mono<ResponseEntity<MyGameObject>> getMyGameObject(@PathVariable Long id) {
        log.debug("REST request to get MyGameObject : {}", id);
        Mono<MyGameObject> myGameObject = myGameObjectRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(myGameObject);
    }

    /**
     * {@code DELETE  /my-game-objects/:id} : delete the "id" myGameObject.
     *
     * @param id the id of the myGameObject to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/my-game-objects/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Void>> deleteMyGameObject(@PathVariable Long id) {
        log.debug("REST request to delete MyGameObject : {}", id);
        return myGameObjectRepository
            .deleteById(id)
            .then(myGameObjectSearchRepository.deleteById(id))
            .map(result ->
                ResponseEntity
                    .noContent()
                    .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
                    .build()
            );
    }

    /**
     * {@code SEARCH  /_search/my-game-objects?query=:query} : search for the myGameObject corresponding
     * to the query.
     *
     * @param query the query of the myGameObject search.
     * @return the result of the search.
     */
    @GetMapping("/_search/my-game-objects")
    public Mono<List<MyGameObject>> searchMyGameObjects(@RequestParam String query) {
        log.debug("REST request to search MyGameObjects for query {}", query);
        return myGameObjectSearchRepository.search(query).collectList();
    }
}
