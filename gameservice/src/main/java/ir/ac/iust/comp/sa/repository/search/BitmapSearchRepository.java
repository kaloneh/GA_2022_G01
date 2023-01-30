package ir.ac.iust.comp.sa.repository.search;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import ir.ac.iust.comp.sa.domain.Bitmap;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import reactor.core.publisher.Flux;

/**
 * Spring Data Elasticsearch repository for the {@link Bitmap} entity.
 */
public interface BitmapSearchRepository extends ReactiveElasticsearchRepository<Bitmap, Long>, BitmapSearchRepositoryInternal {}

interface BitmapSearchRepositoryInternal {
    Flux<Bitmap> search(String query);
}

class BitmapSearchRepositoryInternalImpl implements BitmapSearchRepositoryInternal {

    private final ReactiveElasticsearchTemplate reactiveElasticsearchTemplate;

    BitmapSearchRepositoryInternalImpl(ReactiveElasticsearchTemplate reactiveElasticsearchTemplate) {
        this.reactiveElasticsearchTemplate = reactiveElasticsearchTemplate;
    }

    @Override
    public Flux<Bitmap> search(String query) {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryStringQuery(query));
        return reactiveElasticsearchTemplate.search(nativeSearchQuery, Bitmap.class).map(SearchHit::getContent);
    }
}
