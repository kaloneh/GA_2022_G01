package ir.ac.iust.comp.sa.repository.search;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;

/**
 * Configure a Mock version of {@link MyApplicationSearchRepository} to test the
 * application without starting Elasticsearch.
 */
@Configuration
public class MyApplicationSearchRepositoryMockConfiguration {

    @MockBean
    private MyApplicationSearchRepository mockMyApplicationSearchRepository;
}
