package ir.ac.iust.comp.sa.repository.rowmapper;

import io.r2dbc.spi.Row;
import ir.ac.iust.comp.sa.domain.Bitmap;
import ir.ac.iust.comp.sa.service.ColumnConverter;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Bitmap}, with proper type conversions.
 */
@Service
public class BitmapRowMapper implements BiFunction<Row, String, Bitmap> {

    private final ColumnConverter converter;

    public BitmapRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Bitmap} stored in the database.
     */
    @Override
    public Bitmap apply(Row row, String prefix) {
        Bitmap entity = new Bitmap();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setBlobContentType(converter.fromRow(row, prefix + "_blob_content_type", String.class));
        entity.setBlob(converter.fromRow(row, prefix + "_blob", byte[].class));
        return entity;
    }
}
