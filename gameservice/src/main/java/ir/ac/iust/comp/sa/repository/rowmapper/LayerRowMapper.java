package ir.ac.iust.comp.sa.repository.rowmapper;

import io.r2dbc.spi.Row;
import ir.ac.iust.comp.sa.domain.Layer;
import ir.ac.iust.comp.sa.service.ColumnConverter;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Layer}, with proper type conversions.
 */
@Service
public class LayerRowMapper implements BiFunction<Row, String, Layer> {

    private final ColumnConverter converter;

    public LayerRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Layer} stored in the database.
     */
    @Override
    public Layer apply(Row row, String prefix) {
        Layer entity = new Layer();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setX(converter.fromRow(row, prefix + "_x", Float.class));
        entity.setY(converter.fromRow(row, prefix + "_y", Float.class));
        entity.setBufferContentType(converter.fromRow(row, prefix + "_buffer_content_type", String.class));
        entity.setBuffer(converter.fromRow(row, prefix + "_buffer", byte[].class));
        entity.setIsEnabled(converter.fromRow(row, prefix + "_is_enabled", Boolean.class));
        return entity;
    }
}
