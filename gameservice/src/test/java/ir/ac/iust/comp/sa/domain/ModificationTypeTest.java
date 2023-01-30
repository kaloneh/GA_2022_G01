package ir.ac.iust.comp.sa.domain;

import static org.assertj.core.api.Assertions.assertThat;

import ir.ac.iust.comp.sa.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ModificationTypeTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ModificationType.class);
        ModificationType modificationType1 = new ModificationType();
        modificationType1.setId(1L);
        ModificationType modificationType2 = new ModificationType();
        modificationType2.setId(modificationType1.getId());
        assertThat(modificationType1).isEqualTo(modificationType2);
        modificationType2.setId(2L);
        assertThat(modificationType1).isNotEqualTo(modificationType2);
        modificationType1.setId(null);
        assertThat(modificationType1).isNotEqualTo(modificationType2);
    }
}
