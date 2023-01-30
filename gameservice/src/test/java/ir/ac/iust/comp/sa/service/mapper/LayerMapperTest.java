package ir.ac.iust.comp.sa.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LayerMapperTest {

    private LayerMapper layerMapper;

    @BeforeEach
    public void setUp() {
        layerMapper = new LayerMapperImpl();
    }
}
