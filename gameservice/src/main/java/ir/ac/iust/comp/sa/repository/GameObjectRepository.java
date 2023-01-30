package ir.ac.iust.comp.sa.repository;

import ir.ac.iust.comp.sa.domain.GameObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive repository for the GameObject entity.
 */
@SuppressWarnings("unused")
@Repository
public interface GameObjectRepository extends R2dbcRepository<GameObject, Long>, GameObjectRepositoryInternal {
    // just to avoid having unambigous methods
    @Override
    Flux<GameObject> findAll();

    @Override
    Mono<GameObject> findById(Long id);

    @Override
    <S extends GameObject> Mono<S> save(S entity);
}

interface GameObjectRepositoryInternal {
    <S extends GameObject> Mono<S> insert(S entity);
    <S extends GameObject> Mono<S> save(S entity);
    Mono<Integer> update(GameObject entity);

    Flux<GameObject> findAll();
    Mono<GameObject> findById(Long id);
    Flux<GameObject> findAllBy(Pageable pageable);
    Flux<GameObject> findAllBy(Pageable pageable, Criteria criteria);
}
