package ir.ac.iust.comp.sa.web.rest;

import ir.ac.iust.comp.sa.domain.Bitmap;
import ir.ac.iust.comp.sa.repository.BitmapRepository;
import ir.ac.iust.comp.sa.repository.search.BitmapSearchRepository;
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
 * REST controller for managing {@link ir.ac.iust.comp.sa.domain.Bitmap}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class BitmapResource {

    private final Logger log = LoggerFactory.getLogger(BitmapResource.class);

    private static final String ENTITY_NAME = "gameserviceBitmap";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final BitmapRepository bitmapRepository;

    private final BitmapSearchRepository bitmapSearchRepository;

    public BitmapResource(BitmapRepository bitmapRepository, BitmapSearchRepository bitmapSearchRepository) {
        this.bitmapRepository = bitmapRepository;
        this.bitmapSearchRepository = bitmapSearchRepository;
    }

    /**
     * {@code POST  /bitmaps} : Create a new bitmap.
     *
     * @param bitmap the bitmap to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new bitmap, or with status {@code 400 (Bad Request)} if the bitmap has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/bitmaps")
    public Mono<ResponseEntity<Bitmap>> createBitmap(@RequestBody Bitmap bitmap) throws URISyntaxException {
        log.debug("REST request to save Bitmap : {}", bitmap);
        if (bitmap.getId() != null) {
            throw new BadRequestAlertException("A new bitmap cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return bitmapRepository
            .save(bitmap)
            .flatMap(bitmapSearchRepository::save)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/bitmaps/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /bitmaps/:id} : Updates an existing bitmap.
     *
     * @param id the id of the bitmap to save.
     * @param bitmap the bitmap to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated bitmap,
     * or with status {@code 400 (Bad Request)} if the bitmap is not valid,
     * or with status {@code 500 (Internal Server Error)} if the bitmap couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/bitmaps/{id}")
    public Mono<ResponseEntity<Bitmap>> updateBitmap(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Bitmap bitmap
    ) throws URISyntaxException {
        log.debug("REST request to update Bitmap : {}, {}", id, bitmap);
        if (bitmap.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, bitmap.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return bitmapRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return bitmapRepository
                    .save(bitmap)
                    .flatMap(bitmapSearchRepository::save)
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
     * {@code PATCH  /bitmaps/:id} : Partial updates given fields of an existing bitmap, field will ignore if it is null
     *
     * @param id the id of the bitmap to save.
     * @param bitmap the bitmap to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated bitmap,
     * or with status {@code 400 (Bad Request)} if the bitmap is not valid,
     * or with status {@code 404 (Not Found)} if the bitmap is not found,
     * or with status {@code 500 (Internal Server Error)} if the bitmap couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/bitmaps/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<Bitmap>> partialUpdateBitmap(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Bitmap bitmap
    ) throws URISyntaxException {
        log.debug("REST request to partial update Bitmap partially : {}, {}", id, bitmap);
        if (bitmap.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, bitmap.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return bitmapRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<Bitmap> result = bitmapRepository
                    .findById(bitmap.getId())
                    .map(existingBitmap -> {
                        if (bitmap.getBlob() != null) {
                            existingBitmap.setBlob(bitmap.getBlob());
                        }
                        if (bitmap.getBlobContentType() != null) {
                            existingBitmap.setBlobContentType(bitmap.getBlobContentType());
                        }

                        return existingBitmap;
                    })
                    .flatMap(bitmapRepository::save)
                    .flatMap(savedBitmap -> {
                        bitmapSearchRepository.save(savedBitmap);

                        return Mono.just(savedBitmap);
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
     * {@code GET  /bitmaps} : get all the bitmaps.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of bitmaps in body.
     */
    @GetMapping("/bitmaps")
    public Mono<List<Bitmap>> getAllBitmaps() {
        log.debug("REST request to get all Bitmaps");
        return bitmapRepository.findAll().collectList();
    }

    /**
     * {@code GET  /bitmaps} : get all the bitmaps as a stream.
     * @return the {@link Flux} of bitmaps.
     */
    @GetMapping(value = "/bitmaps", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<Bitmap> getAllBitmapsAsStream() {
        log.debug("REST request to get all Bitmaps as a stream");
        return bitmapRepository.findAll();
    }

    /**
     * {@code GET  /bitmaps/:id} : get the "id" bitmap.
     *
     * @param id the id of the bitmap to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the bitmap, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/bitmaps/{id}")
    public Mono<ResponseEntity<Bitmap>> getBitmap(@PathVariable Long id) {
        log.debug("REST request to get Bitmap : {}", id);
        Mono<Bitmap> bitmap = bitmapRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(bitmap);
    }

    /**
     * {@code DELETE  /bitmaps/:id} : delete the "id" bitmap.
     *
     * @param id the id of the bitmap to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/bitmaps/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Void>> deleteBitmap(@PathVariable Long id) {
        log.debug("REST request to delete Bitmap : {}", id);
        return bitmapRepository
            .deleteById(id)
            .then(bitmapSearchRepository.deleteById(id))
            .map(result ->
                ResponseEntity
                    .noContent()
                    .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
                    .build()
            );
    }

    /**
     * {@code SEARCH  /_search/bitmaps?query=:query} : search for the bitmap corresponding
     * to the query.
     *
     * @param query the query of the bitmap search.
     * @return the result of the search.
     */
    @GetMapping("/_search/bitmaps")
    public Mono<List<Bitmap>> searchBitmaps(@RequestParam String query) {
        log.debug("REST request to search Bitmaps for query {}", query);
        return bitmapSearchRepository.search(query).collectList();
    }
}
