package ir.ac.iust.comp.sa.repository;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import ir.ac.iust.comp.sa.domain.Layer;
import ir.ac.iust.comp.sa.repository.rowmapper.LayerRowMapper;
import ir.ac.iust.comp.sa.service.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiFunction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Select;
import org.springframework.data.relational.core.sql.SelectBuilder.SelectFromAndJoin;
import org.springframework.data.relational.core.sql.Table;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive custom repository implementation for the Layer entity.
 */
@SuppressWarnings("unused")
class LayerRepositoryInternalImpl implements LayerRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final LayerRowMapper layerMapper;

    private static final Table entityTable = Table.aliased("layer", EntityManager.ENTITY_ALIAS);

    public LayerRepositoryInternalImpl(R2dbcEntityTemplate template, EntityManager entityManager, LayerRowMapper layerMapper) {
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.layerMapper = layerMapper;
    }

    @Override
    public Flux<Layer> findAllBy(Pageable pageable) {
        return findAllBy(pageable, null);
    }

    @Override
    public Flux<Layer> findAllBy(Pageable pageable, Criteria criteria) {
        return createQuery(pageable, criteria).all();
    }

    RowsFetchSpec<Layer> createQuery(Pageable pageable, Criteria criteria) {
        List<Expression> columns = LayerSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        SelectFromAndJoin selectFrom = Select.builder().select(columns).from(entityTable);

        String select = entityManager.createSelect(selectFrom, Layer.class, pageable, criteria);
        String alias = entityTable.getReferenceName().getReference();
        String selectWhere = Optional
            .ofNullable(criteria)
            .map(crit ->
                new StringBuilder(select)
                    .append(" ")
                    .append("WHERE")
                    .append(" ")
                    .append(alias)
                    .append(".")
                    .append(crit.toString())
                    .toString()
            )
            .orElse(select); // TODO remove once https://github.com/spring-projects/spring-data-jdbc/issues/907 will be fixed
        return db.sql(selectWhere).map(this::process);
    }

    @Override
    public Flux<Layer> findAll() {
        return findAllBy(null, null);
    }

    @Override
    public Mono<Layer> findById(Long id) {
        return createQuery(null, where("id").is(id)).one();
    }

    private Layer process(Row row, RowMetadata metadata) {
        Layer entity = layerMapper.apply(row, "e");
        return entity;
    }

    @Override
    public <S extends Layer> Mono<S> insert(S entity) {
        return entityManager.insert(entity);
    }

    @Override
    public <S extends Layer> Mono<S> save(S entity) {
        if (entity.getId() == null) {
            return insert(entity);
        } else {
            return update(entity)
                .map(numberOfUpdates -> {
                    if (numberOfUpdates.intValue() <= 0) {
                        throw new IllegalStateException("Unable to update Layer with id = " + entity.getId());
                    }
                    return entity;
                });
        }
    }

    @Override
    public Mono<Integer> update(Layer entity) {
        //fixme is this the proper way?
        return r2dbcEntityTemplate.update(entity).thenReturn(1);
    }
}

class LayerSqlHelper {

    static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("x", table, columnPrefix + "_x"));
        columns.add(Column.aliased("y", table, columnPrefix + "_y"));
        columns.add(Column.aliased("buffer", table, columnPrefix + "_buffer"));
        columns.add(Column.aliased("buffer_content_type", table, columnPrefix + "_buffer_content_type"));
        columns.add(Column.aliased("is_enabled", table, columnPrefix + "_is_enabled"));

        return columns;
    }
}
