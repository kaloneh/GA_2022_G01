package ir.ac.iust.comp.sa.service;

import ir.ac.iust.comp.sa.service.dto.LayerDTO;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Interface for managing {@link ir.ac.iust.comp.sa.domain.Layer}.
 */
public interface LayerService {
    /**
     * Save a layer.
     *
     * @param layerDTO the entity to save.
     * @return the persisted entity.
     */
    Mono<LayerDTO> save(LayerDTO layerDTO);

    /**
     * Partially updates a layer.
     *
     * @param layerDTO the entity to update partially.
     * @return the persisted entity.
     */
    Mono<LayerDTO> partialUpdate(LayerDTO layerDTO);

    /**
     * Get all the layers.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<LayerDTO> findAll(Pageable pageable);

    /**
     * Returns the number of layers available.
     * @return the number of entities in the database.
     *
     */
    Mono<Long> countAll();

    /**
     * Returns the number of layers available in search repository.
     *
     */
    Mono<Long> searchCount();

    /**
     * Get the "id" layer.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<LayerDTO> findOne(Long id);

    /**
     * Delete the "id" layer.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    Mono<Void> delete(Long id);

    /**
     * Search for the layer corresponding to the query.
     *
     * @param query the query of the search.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<LayerDTO> search(String query, Pageable pageable);
}
