package ir.ac.iust.comp.sa.repository.search;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import ir.ac.iust.comp.sa.domain.Layer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import reactor.core.publisher.Flux;

/**
 * Spring Data Elasticsearch repository for the {@link Layer} entity.
 */
public interface LayerSearchRepository extends ReactiveElasticsearchRepository<Layer, Long>, LayerSearchRepositoryInternal {}

interface LayerSearchRepositoryInternal {
    Flux<Layer> search(String query, Pageable pageable);
}

class LayerSearchRepositoryInternalImpl implements LayerSearchRepositoryInternal {

    private final ReactiveElasticsearchTemplate reactiveElasticsearchTemplate;

    LayerSearchRepositoryInternalImpl(ReactiveElasticsearchTemplate reactiveElasticsearchTemplate) {
        this.reactiveElasticsearchTemplate = reactiveElasticsearchTemplate;
    }

    @Override
    public Flux<Layer> search(String query, Pageable pageable) {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryStringQuery(query));
        nativeSearchQuery.setPageable(pageable);
        return reactiveElasticsearchTemplate.search(nativeSearchQuery, Layer.class).map(SearchHit::getContent);
    }
}
