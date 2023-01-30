package ir.ac.iust.comp.sa.domain;

import static org.assertj.core.api.Assertions.assertThat;

import ir.ac.iust.comp.sa.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class GameObjectTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(GameObject.class);
        GameObject gameObject1 = new GameObject();
        gameObject1.setId(1L);
        GameObject gameObject2 = new GameObject();
        gameObject2.setId(gameObject1.getId());
        assertThat(gameObject1).isEqualTo(gameObject2);
        gameObject2.setId(2L);
        assertThat(gameObject1).isNotEqualTo(gameObject2);
        gameObject1.setId(null);
        assertThat(gameObject1).isNotEqualTo(gameObject2);
    }
}
