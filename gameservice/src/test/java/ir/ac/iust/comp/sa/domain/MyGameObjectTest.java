package ir.ac.iust.comp.sa.domain;

import static org.assertj.core.api.Assertions.assertThat;

import ir.ac.iust.comp.sa.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MyGameObjectTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(MyGameObject.class);
        MyGameObject myGameObject1 = new MyGameObject();
        myGameObject1.setId(1L);
        MyGameObject myGameObject2 = new MyGameObject();
        myGameObject2.setId(myGameObject1.getId());
        assertThat(myGameObject1).isEqualTo(myGameObject2);
        myGameObject2.setId(2L);
        assertThat(myGameObject1).isNotEqualTo(myGameObject2);
        myGameObject1.setId(null);
        assertThat(myGameObject1).isNotEqualTo(myGameObject2);
    }
}
