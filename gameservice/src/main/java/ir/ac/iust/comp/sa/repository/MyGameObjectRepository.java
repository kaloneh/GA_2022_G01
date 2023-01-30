package ir.ac.iust.comp.sa.repository;

import ir.ac.iust.comp.sa.domain.MyGameObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive repository for the MyGameObject entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MyGameObjectRepository extends R2dbcRepository<MyGameObject, Long>, MyGameObjectRepositoryInternal {
    // just to avoid having unambigous methods
    @Override
    Flux<MyGameObject> findAll();

    @Override
    Mono<MyGameObject> findById(Long id);

    @Override
    <S extends MyGameObject> Mono<S> save(S entity);
}

interface MyGameObjectRepositoryInternal {
    <S extends MyGameObject> Mono<S> insert(S entity);
    <S extends MyGameObject> Mono<S> save(S entity);
    Mono<Integer> update(MyGameObject entity);

    Flux<MyGameObject> findAll();
    Mono<MyGameObject> findById(Long id);
    Flux<MyGameObject> findAllBy(Pageable pageable);
    Flux<MyGameObject> findAllBy(Pageable pageable, Criteria criteria);
}
