package ir.ac.iust.comp.sa.repository.rowmapper;

import io.r2dbc.spi.Row;
import ir.ac.iust.comp.sa.domain.Application;
import ir.ac.iust.comp.sa.service.ColumnConverter;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Application}, with proper type conversions.
 */
@Service
public class ApplicationRowMapper implements BiFunction<Row, String, Application> {

    private final ColumnConverter converter;

    public ApplicationRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Application} stored in the database.
     */
    @Override
    public Application apply(Row row, String prefix) {
        Application entity = new Application();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setWidth(converter.fromRow(row, prefix + "_width", Float.class));
        entity.setHeight(converter.fromRow(row, prefix + "_height", Float.class));
        entity.setScreenBufferContentType(converter.fromRow(row, prefix + "_screen_buffer_content_type", String.class));
        entity.setScreenBuffer(converter.fromRow(row, prefix + "_screen_buffer", byte[].class));
        return entity;
    }
}
