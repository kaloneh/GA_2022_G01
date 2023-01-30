package ir.ac.iust.comp.sa.repository;

import ir.ac.iust.comp.sa.domain.MyApplication;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive repository for the MyApplication entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MyApplicationRepository extends R2dbcRepository<MyApplication, Long>, MyApplicationRepositoryInternal {
    // just to avoid having unambigous methods
    @Override
    Flux<MyApplication> findAll();

    @Override
    Mono<MyApplication> findById(Long id);

    @Override
    <S extends MyApplication> Mono<S> save(S entity);
}

interface MyApplicationRepositoryInternal {
    <S extends MyApplication> Mono<S> insert(S entity);
    <S extends MyApplication> Mono<S> save(S entity);
    Mono<Integer> update(MyApplication entity);

    Flux<MyApplication> findAll();
    Mono<MyApplication> findById(Long id);
    Flux<MyApplication> findAllBy(Pageable pageable);
    Flux<MyApplication> findAllBy(Pageable pageable, Criteria criteria);
}
