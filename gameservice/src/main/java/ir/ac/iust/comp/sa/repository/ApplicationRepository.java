package ir.ac.iust.comp.sa.repository;

import ir.ac.iust.comp.sa.domain.Application;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive repository for the Application entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ApplicationRepository extends R2dbcRepository<Application, Long>, ApplicationRepositoryInternal {
    // just to avoid having unambigous methods
    @Override
    Flux<Application> findAll();

    @Override
    Mono<Application> findById(Long id);

    @Override
    <S extends Application> Mono<S> save(S entity);
}

interface ApplicationRepositoryInternal {
    <S extends Application> Mono<S> insert(S entity);
    <S extends Application> Mono<S> save(S entity);
    Mono<Integer> update(Application entity);

    Flux<Application> findAll();
    Mono<Application> findById(Long id);
    Flux<Application> findAllBy(Pageable pageable);
    Flux<Application> findAllBy(Pageable pageable, Criteria criteria);
}
