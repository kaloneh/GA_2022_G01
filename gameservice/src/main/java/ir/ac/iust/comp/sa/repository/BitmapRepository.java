package ir.ac.iust.comp.sa.repository;

import ir.ac.iust.comp.sa.domain.Bitmap;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive repository for the Bitmap entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BitmapRepository extends R2dbcRepository<Bitmap, Long>, BitmapRepositoryInternal {
    // just to avoid having unambigous methods
    @Override
    Flux<Bitmap> findAll();

    @Override
    Mono<Bitmap> findById(Long id);

    @Override
    <S extends Bitmap> Mono<S> save(S entity);
}

interface BitmapRepositoryInternal {
    <S extends Bitmap> Mono<S> insert(S entity);
    <S extends Bitmap> Mono<S> save(S entity);
    Mono<Integer> update(Bitmap entity);

    Flux<Bitmap> findAll();
    Mono<Bitmap> findById(Long id);
    Flux<Bitmap> findAllBy(Pageable pageable);
    Flux<Bitmap> findAllBy(Pageable pageable, Criteria criteria);
}
