package ir.ac.iust.comp.sa.domain;

import static org.assertj.core.api.Assertions.assertThat;

import ir.ac.iust.comp.sa.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class BitmapTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Bitmap.class);
        Bitmap bitmap1 = new Bitmap();
        bitmap1.setId(1L);
        Bitmap bitmap2 = new Bitmap();
        bitmap2.setId(bitmap1.getId());
        assertThat(bitmap1).isEqualTo(bitmap2);
        bitmap2.setId(2L);
        assertThat(bitmap1).isNotEqualTo(bitmap2);
        bitmap1.setId(null);
        assertThat(bitmap1).isNotEqualTo(bitmap2);
    }
}
