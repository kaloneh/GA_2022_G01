package ir.ac.iust.comp.sa.repository;

import ir.ac.iust.comp.sa.domain.ModificationType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive repository for the ModificationType entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ModificationTypeRepository extends R2dbcRepository<ModificationType, Long>, ModificationTypeRepositoryInternal {
    // just to avoid having unambigous methods
    @Override
    Flux<ModificationType> findAll();

    @Override
    Mono<ModificationType> findById(Long id);

    @Override
    <S extends ModificationType> Mono<S> save(S entity);
}

interface ModificationTypeRepositoryInternal {
    <S extends ModificationType> Mono<S> insert(S entity);
    <S extends ModificationType> Mono<S> save(S entity);
    Mono<Integer> update(ModificationType entity);

    Flux<ModificationType> findAll();
    Mono<ModificationType> findById(Long id);
    Flux<ModificationType> findAllBy(Pageable pageable);
    Flux<ModificationType> findAllBy(Pageable pageable, Criteria criteria);
}
