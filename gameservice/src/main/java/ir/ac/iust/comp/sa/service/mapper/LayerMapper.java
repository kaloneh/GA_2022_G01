package ir.ac.iust.comp.sa.service.mapper;

import ir.ac.iust.comp.sa.domain.*;
import ir.ac.iust.comp.sa.service.dto.LayerDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Layer} and its DTO {@link LayerDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface LayerMapper extends EntityMapper<LayerDTO, Layer> {}
