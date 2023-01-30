package ir.ac.iust.comp.sa.repository.search;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;

/**
 * Configure a Mock version of {@link ModificationTypeSearchRepository} to test the
 * application without starting Elasticsearch.
 */
@Configuration
public class ModificationTypeSearchRepositoryMockConfiguration {

    @MockBean
    private ModificationTypeSearchRepository mockModificationTypeSearchRepository;
}
