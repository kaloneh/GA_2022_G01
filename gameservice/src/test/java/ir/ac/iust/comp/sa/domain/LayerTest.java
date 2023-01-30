package ir.ac.iust.comp.sa.domain;

import static org.assertj.core.api.Assertions.assertThat;

import ir.ac.iust.comp.sa.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class LayerTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Layer.class);
        Layer layer1 = new Layer();
        layer1.setId(1L);
        Layer layer2 = new Layer();
        layer2.setId(layer1.getId());
        assertThat(layer1).isEqualTo(layer2);
        layer2.setId(2L);
        assertThat(layer1).isNotEqualTo(layer2);
        layer1.setId(null);
        assertThat(layer1).isNotEqualTo(layer2);
    }
}
