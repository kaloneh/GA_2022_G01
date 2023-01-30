package ir.ac.iust.comp.sa.repository.rowmapper;

import io.r2dbc.spi.Row;
import ir.ac.iust.comp.sa.domain.GameObject;
import ir.ac.iust.comp.sa.service.ColumnConverter;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link GameObject}, with proper type conversions.
 */
@Service
public class GameObjectRowMapper implements BiFunction<Row, String, GameObject> {

    private final ColumnConverter converter;

    public GameObjectRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link GameObject} stored in the database.
     */
    @Override
    public GameObject apply(Row row, String prefix) {
        GameObject entity = new GameObject();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setX(converter.fromRow(row, prefix + "_x", Float.class));
        entity.setY(converter.fromRow(row, prefix + "_y", Float.class));
        entity.setBitmapContentType(converter.fromRow(row, prefix + "_bitmap_content_type", String.class));
        entity.setBitmap(converter.fromRow(row, prefix + "_bitmap", byte[].class));
        entity.setIsEnabled(converter.fromRow(row, prefix + "_is_enabled", Boolean.class));
        return entity;
    }
}
