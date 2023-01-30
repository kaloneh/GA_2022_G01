package ir.ac.iust.comp.sa.repository.rowmapper;

import io.r2dbc.spi.Row;
import ir.ac.iust.comp.sa.domain.MyGameObject;
import ir.ac.iust.comp.sa.service.ColumnConverter;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link MyGameObject}, with proper type conversions.
 */
@Service
public class MyGameObjectRowMapper implements BiFunction<Row, String, MyGameObject> {

    private final ColumnConverter converter;

    public MyGameObjectRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link MyGameObject} stored in the database.
     */
    @Override
    public MyGameObject apply(Row row, String prefix) {
        MyGameObject entity = new MyGameObject();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        return entity;
    }
}
