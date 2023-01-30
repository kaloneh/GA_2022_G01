package ir.ac.iust.comp.sa.domain;

import static org.assertj.core.api.Assertions.assertThat;

import ir.ac.iust.comp.sa.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MyApplicationTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(MyApplication.class);
        MyApplication myApplication1 = new MyApplication();
        myApplication1.setId(1L);
        MyApplication myApplication2 = new MyApplication();
        myApplication2.setId(myApplication1.getId());
        assertThat(myApplication1).isEqualTo(myApplication2);
        myApplication2.setId(2L);
        assertThat(myApplication1).isNotEqualTo(myApplication2);
        myApplication1.setId(null);
        assertThat(myApplication1).isNotEqualTo(myApplication2);
    }
}
