package ir.ac.iust.comp.sa.repository.rowmapper;

import io.r2dbc.spi.Row;
import ir.ac.iust.comp.sa.domain.ModificationType;
import ir.ac.iust.comp.sa.domain.enumeration.EnumModType;
import ir.ac.iust.comp.sa.service.ColumnConverter;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link ModificationType}, with proper type conversions.
 */
@Service
public class ModificationTypeRowMapper implements BiFunction<Row, String, ModificationType> {

    private final ColumnConverter converter;

    public ModificationTypeRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link ModificationType} stored in the database.
     */
    @Override
    public ModificationType apply(Row row, String prefix) {
        ModificationType entity = new ModificationType();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setType(converter.fromRow(row, prefix + "_type", EnumModType.class));
        return entity;
    }
}
