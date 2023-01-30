package ir.ac.iust.comp.sa.service.impl;

import static org.elasticsearch.index.query.QueryBuilders.*;

import ir.ac.iust.comp.sa.domain.Layer;
import ir.ac.iust.comp.sa.repository.LayerRepository;
import ir.ac.iust.comp.sa.repository.search.LayerSearchRepository;
import ir.ac.iust.comp.sa.service.LayerService;
import ir.ac.iust.comp.sa.service.dto.LayerDTO;
import ir.ac.iust.comp.sa.service.mapper.LayerMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link Layer}.
 */
@Service
@Transactional
public class LayerServiceImpl implements LayerService {

    private final Logger log = LoggerFactory.getLogger(LayerServiceImpl.class);

    private final LayerRepository layerRepository;

    private final LayerMapper layerMapper;

    private final LayerSearchRepository layerSearchRepository;

    public LayerServiceImpl(LayerRepository layerRepository, LayerMapper layerMapper, LayerSearchRepository layerSearchRepository) {
        this.layerRepository = layerRepository;
        this.layerMapper = layerMapper;
        this.layerSearchRepository = layerSearchRepository;
    }

    @Override
    public Mono<LayerDTO> save(LayerDTO layerDTO) {
        log.debug("Request to save Layer : {}", layerDTO);
        return layerRepository.save(layerMapper.toEntity(layerDTO)).flatMap(layerSearchRepository::save).map(layerMapper::toDto);
    }

    @Override
    public Mono<LayerDTO> partialUpdate(LayerDTO layerDTO) {
        log.debug("Request to partially update Layer : {}", layerDTO);

        return layerRepository
            .findById(layerDTO.getId())
            .map(existingLayer -> {
                layerMapper.partialUpdate(existingLayer, layerDTO);

                return existingLayer;
            })
            .flatMap(layerRepository::save)
            .flatMap(savedLayer -> {
                layerSearchRepository.save(savedLayer);

                return Mono.just(savedLayer);
            })
            .map(layerMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<LayerDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Layers");
        return layerRepository.findAllBy(pageable).map(layerMapper::toDto);
    }

    public Mono<Long> countAll() {
        return layerRepository.count();
    }

    public Mono<Long> searchCount() {
        return layerSearchRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<LayerDTO> findOne(Long id) {
        log.debug("Request to get Layer : {}", id);
        return layerRepository.findById(id).map(layerMapper::toDto);
    }

    @Override
    public Mono<Void> delete(Long id) {
        log.debug("Request to delete Layer : {}", id);
        return layerRepository.deleteById(id).then(layerSearchRepository.deleteById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<LayerDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Layers for query {}", query);
        return layerSearchRepository.search(query, pageable).map(layerMapper::toDto);
    }
}
