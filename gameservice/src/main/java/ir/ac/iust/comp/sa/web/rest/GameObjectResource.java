package ir.ac.iust.comp.sa.web.rest;

import ir.ac.iust.comp.sa.domain.GameObject;
import ir.ac.iust.comp.sa.repository.GameObjectRepository;
import ir.ac.iust.comp.sa.repository.search.GameObjectSearchRepository;
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
 * REST controller for managing {@link ir.ac.iust.comp.sa.domain.GameObject}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class GameObjectResource {

    private final Logger log = LoggerFactory.getLogger(GameObjectResource.class);

    private static final String ENTITY_NAME = "gameserviceGameObject";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final GameObjectRepository gameObjectRepository;

    private final GameObjectSearchRepository gameObjectSearchRepository;

    public GameObjectResource(GameObjectRepository gameObjectRepository, GameObjectSearchRepository gameObjectSearchRepository) {
        this.gameObjectRepository = gameObjectRepository;
        this.gameObjectSearchRepository = gameObjectSearchRepository;
    }

    /**
     * {@code POST  /game-objects} : Create a new gameObject.
     *
     * @param gameObject the gameObject to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new gameObject, or with status {@code 400 (Bad Request)} if the gameObject has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/game-objects")
    public Mono<ResponseEntity<GameObject>> createGameObject(@RequestBody GameObject gameObject) throws URISyntaxException {
        log.debug("REST request to save GameObject : {}", gameObject);
        if (gameObject.getId() != null) {
            throw new BadRequestAlertException("A new gameObject cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return gameObjectRepository
            .save(gameObject)
            .flatMap(gameObjectSearchRepository::save)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/game-objects/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /game-objects/:id} : Updates an existing gameObject.
     *
     * @param id the id of the gameObject to save.
     * @param gameObject the gameObject to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated gameObject,
     * or with status {@code 400 (Bad Request)} if the gameObject is not valid,
     * or with status {@code 500 (Internal Server Error)} if the gameObject couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/game-objects/{id}")
    public Mono<ResponseEntity<GameObject>> updateGameObject(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody GameObject gameObject
    ) throws URISyntaxException {
        log.debug("REST request to update GameObject : {}, {}", id, gameObject);
        if (gameObject.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, gameObject.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return gameObjectRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return gameObjectRepository
                    .save(gameObject)
                    .flatMap(gameObjectSearchRepository::save)
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
     * {@code PATCH  /game-objects/:id} : Partial updates given fields of an existing gameObject, field will ignore if it is null
     *
     * @param id the id of the gameObject to save.
     * @param gameObject the gameObject to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated gameObject,
     * or with status {@code 400 (Bad Request)} if the gameObject is not valid,
     * or with status {@code 404 (Not Found)} if the gameObject is not found,
     * or with status {@code 500 (Internal Server Error)} if the gameObject couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/game-objects/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<GameObject>> partialUpdateGameObject(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody GameObject gameObject
    ) throws URISyntaxException {
        log.debug("REST request to partial update GameObject partially : {}, {}", id, gameObject);
        if (gameObject.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, gameObject.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return gameObjectRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<GameObject> result = gameObjectRepository
                    .findById(gameObject.getId())
                    .map(existingGameObject -> {
                        if (gameObject.getX() != null) {
                            existingGameObject.setX(gameObject.getX());
                        }
                        if (gameObject.getY() != null) {
                            existingGameObject.setY(gameObject.getY());
                        }
                        if (gameObject.getBitmap() != null) {
                            existingGameObject.setBitmap(gameObject.getBitmap());
                        }
                        if (gameObject.getBitmapContentType() != null) {
                            existingGameObject.setBitmapContentType(gameObject.getBitmapContentType());
                        }
                        if (gameObject.getIsEnabled() != null) {
                            existingGameObject.setIsEnabled(gameObject.getIsEnabled());
                        }

                        return existingGameObject;
                    })
                    .flatMap(gameObjectRepository::save)
                    .flatMap(savedGameObject -> {
                        gameObjectSearchRepository.save(savedGameObject);

                        return Mono.just(savedGameObject);
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
     * {@code GET  /game-objects} : get all the gameObjects.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of gameObjects in body.
     */
    @GetMapping("/game-objects")
    public Mono<List<GameObject>> getAllGameObjects() {
        log.debug("REST request to get all GameObjects");
        return gameObjectRepository.findAll().collectList();
    }

    /**
     * {@code GET  /game-objects} : get all the gameObjects as a stream.
     * @return the {@link Flux} of gameObjects.
     */
    @GetMapping(value = "/game-objects", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<GameObject> getAllGameObjectsAsStream() {
        log.debug("REST request to get all GameObjects as a stream");
        return gameObjectRepository.findAll();
    }

    /**
     * {@code GET  /game-objects/:id} : get the "id" gameObject.
     *
     * @param id the id of the gameObject to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the gameObject, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/game-objects/{id}")
    public Mono<ResponseEntity<GameObject>> getGameObject(@PathVariable Long id) {
        log.debug("REST request to get GameObject : {}", id);
        Mono<GameObject> gameObject = gameObjectRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(gameObject);
    }

    /**
     * {@code DELETE  /game-objects/:id} : delete the "id" gameObject.
     *
     * @param id the id of the gameObject to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/game-objects/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Void>> deleteGameObject(@PathVariable Long id) {
        log.debug("REST request to delete GameObject : {}", id);
        return gameObjectRepository
            .deleteById(id)
            .then(gameObjectSearchRepository.deleteById(id))
            .map(result ->
                ResponseEntity
                    .noContent()
                    .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
                    .build()
            );
    }

    /**
     * {@code SEARCH  /_search/game-objects?query=:query} : search for the gameObject corresponding
     * to the query.
     *
     * @param query the query of the gameObject search.
     * @return the result of the search.
     */
    @GetMapping("/_search/game-objects")
    public Mono<List<GameObject>> searchGameObjects(@RequestParam String query) {
        log.debug("REST request to search GameObjects for query {}", query);
        return gameObjectSearchRepository.search(query).collectList();
    }
}
