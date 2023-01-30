package ir.ac.iust.comp.sa.repository;

import ir.ac.iust.comp.sa.domain.Layer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive repository for the Layer entity.
 */
@SuppressWarnings("unused")
@Repository
public interface LayerRepository extends R2dbcRepository<Layer, Long>, LayerRepositoryInternal {
    Flux<Layer> findAllBy(Pageable pageable);

    // just to avoid having unambigous methods
    @Override
    Flux<Layer> findAll();

    @Override
    Mono<Layer> findById(Long id);

    @Override
    <S extends Layer> Mono<S> save(S entity);
}

interface LayerRepositoryInternal {
    <S extends Layer> Mono<S> insert(S entity);
    <S extends Layer> Mono<S> save(S entity);
    Mono<Integer> update(Layer entity);

    Flux<Layer> findAll();
    Mono<Layer> findById(Long id);
    Flux<Layer> findAllBy(Pageable pageable);
    Flux<Layer> findAllBy(Pageable pageable, Criteria criteria);
}
