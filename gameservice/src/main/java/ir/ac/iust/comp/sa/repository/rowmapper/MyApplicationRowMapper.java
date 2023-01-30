package ir.ac.iust.comp.sa.repository.rowmapper;

import io.r2dbc.spi.Row;
import ir.ac.iust.comp.sa.domain.MyApplication;
import ir.ac.iust.comp.sa.service.ColumnConverter;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link MyApplication}, with proper type conversions.
 */
@Service
public class MyApplicationRowMapper implements BiFunction<Row, String, MyApplication> {

    private final ColumnConverter converter;

    public MyApplicationRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link MyApplication} stored in the database.
     */
    @Override
    public MyApplication apply(Row row, String prefix) {
        MyApplication entity = new MyApplication();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        return entity;
    }
}
