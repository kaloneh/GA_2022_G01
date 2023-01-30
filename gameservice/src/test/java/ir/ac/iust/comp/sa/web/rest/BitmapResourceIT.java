package ir.ac.iust.comp.sa.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import ir.ac.iust.comp.sa.IntegrationTest;
import ir.ac.iust.comp.sa.domain.Bitmap;
import ir.ac.iust.comp.sa.repository.BitmapRepository;
import ir.ac.iust.comp.sa.repository.search.BitmapSearchRepository;
import ir.ac.iust.comp.sa.service.EntityManager;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.Base64Utils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Integration tests for the {@link BitmapResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient
@WithMockUser
class BitmapResourceIT {

    private static final byte[] DEFAULT_BLOB = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_BLOB = TestUtil.createByteArray(1, "1");
    private static final String DEFAULT_BLOB_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_BLOB_CONTENT_TYPE = "image/png";

    private static final String ENTITY_API_URL = "/api/bitmaps";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/bitmaps";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private BitmapRepository bitmapRepository;

    /**
     * This repository is mocked in the ir.ac.iust.comp.sa.repository.search test package.
     *
     * @see ir.ac.iust.comp.sa.repository.search.BitmapSearchRepositoryMockConfiguration
     */
    @Autowired
    private BitmapSearchRepository mockBitmapSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Bitmap bitmap;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Bitmap createEntity(EntityManager em) {
        Bitmap bitmap = new Bitmap().blob(DEFAULT_BLOB).blobContentType(DEFAULT_BLOB_CONTENT_TYPE);
        return bitmap;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Bitmap createUpdatedEntity(EntityManager em) {
        Bitmap bitmap = new Bitmap().blob(UPDATED_BLOB).blobContentType(UPDATED_BLOB_CONTENT_TYPE);
        return bitmap;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Bitmap.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
    }

    @AfterEach
    public void cleanup() {
        deleteEntities(em);
    }

    @BeforeEach
    public void initTest() {
        deleteEntities(em);
        bitmap = createEntity(em);
    }

    @Test
    void createBitmap() throws Exception {
        int databaseSizeBeforeCreate = bitmapRepository.findAll().collectList().block().size();
        // Configure the mock search repository
        when(mockBitmapSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Create the Bitmap
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(bitmap))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the Bitmap in the database
        List<Bitmap> bitmapList = bitmapRepository.findAll().collectList().block();
        assertThat(bitmapList).hasSize(databaseSizeBeforeCreate + 1);
        Bitmap testBitmap = bitmapList.get(bitmapList.size() - 1);
        assertThat(testBitmap.getBlob()).isEqualTo(DEFAULT_BLOB);
        assertThat(testBitmap.getBlobContentType()).isEqualTo(DEFAULT_BLOB_CONTENT_TYPE);

        // Validate the Bitmap in Elasticsearch
        verify(mockBitmapSearchRepository, times(1)).save(testBitmap);
    }

    @Test
    void createBitmapWithExistingId() throws Exception {
        // Create the Bitmap with an existing ID
        bitmap.setId(1L);

        int databaseSizeBeforeCreate = bitmapRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(bitmap))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Bitmap in the database
        List<Bitmap> bitmapList = bitmapRepository.findAll().collectList().block();
        assertThat(bitmapList).hasSize(databaseSizeBeforeCreate);

        // Validate the Bitmap in Elasticsearch
        verify(mockBitmapSearchRepository, times(0)).save(bitmap);
    }

    @Test
    void getAllBitmapsAsStream() {
        // Initialize the database
        bitmapRepository.save(bitmap).block();

        List<Bitmap> bitmapList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(Bitmap.class)
            .getResponseBody()
            .filter(bitmap::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(bitmapList).isNotNull();
        assertThat(bitmapList).hasSize(1);
        Bitmap testBitmap = bitmapList.get(0);
        assertThat(testBitmap.getBlob()).isEqualTo(DEFAULT_BLOB);
        assertThat(testBitmap.getBlobContentType()).isEqualTo(DEFAULT_BLOB_CONTENT_TYPE);
    }

    @Test
    void getAllBitmaps() {
        // Initialize the database
        bitmapRepository.save(bitmap).block();

        // Get all the bitmapList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(bitmap.getId().intValue()))
            .jsonPath("$.[*].blobContentType")
            .value(hasItem(DEFAULT_BLOB_CONTENT_TYPE))
            .jsonPath("$.[*].blob")
            .value(hasItem(Base64Utils.encodeToString(DEFAULT_BLOB)));
    }

    @Test
    void getBitmap() {
        // Initialize the database
        bitmapRepository.save(bitmap).block();

        // Get the bitmap
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, bitmap.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(bitmap.getId().intValue()))
            .jsonPath("$.blobContentType")
            .value(is(DEFAULT_BLOB_CONTENT_TYPE))
            .jsonPath("$.blob")
            .value(is(Base64Utils.encodeToString(DEFAULT_BLOB)));
    }

    @Test
    void getNonExistingBitmap() {
        // Get the bitmap
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewBitmap() throws Exception {
        // Configure the mock search repository
        when(mockBitmapSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Initialize the database
        bitmapRepository.save(bitmap).block();

        int databaseSizeBeforeUpdate = bitmapRepository.findAll().collectList().block().size();

        // Update the bitmap
        Bitmap updatedBitmap = bitmapRepository.findById(bitmap.getId()).block();
        updatedBitmap.blob(UPDATED_BLOB).blobContentType(UPDATED_BLOB_CONTENT_TYPE);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedBitmap.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedBitmap))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Bitmap in the database
        List<Bitmap> bitmapList = bitmapRepository.findAll().collectList().block();
        assertThat(bitmapList).hasSize(databaseSizeBeforeUpdate);
        Bitmap testBitmap = bitmapList.get(bitmapList.size() - 1);
        assertThat(testBitmap.getBlob()).isEqualTo(UPDATED_BLOB);
        assertThat(testBitmap.getBlobContentType()).isEqualTo(UPDATED_BLOB_CONTENT_TYPE);

        // Validate the Bitmap in Elasticsearch
        verify(mockBitmapSearchRepository).save(testBitmap);
    }

    @Test
    void putNonExistingBitmap() throws Exception {
        int databaseSizeBeforeUpdate = bitmapRepository.findAll().collectList().block().size();
        bitmap.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, bitmap.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(bitmap))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Bitmap in the database
        List<Bitmap> bitmapList = bitmapRepository.findAll().collectList().block();
        assertThat(bitmapList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Bitmap in Elasticsearch
        verify(mockBitmapSearchRepository, times(0)).save(bitmap);
    }

    @Test
    void putWithIdMismatchBitmap() throws Exception {
        int databaseSizeBeforeUpdate = bitmapRepository.findAll().collectList().block().size();
        bitmap.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(bitmap))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Bitmap in the database
        List<Bitmap> bitmapList = bitmapRepository.findAll().collectList().block();
        assertThat(bitmapList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Bitmap in Elasticsearch
        verify(mockBitmapSearchRepository, times(0)).save(bitmap);
    }

    @Test
    void putWithMissingIdPathParamBitmap() throws Exception {
        int databaseSizeBeforeUpdate = bitmapRepository.findAll().collectList().block().size();
        bitmap.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(bitmap))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Bitmap in the database
        List<Bitmap> bitmapList = bitmapRepository.findAll().collectList().block();
        assertThat(bitmapList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Bitmap in Elasticsearch
        verify(mockBitmapSearchRepository, times(0)).save(bitmap);
    }

    @Test
    void partialUpdateBitmapWithPatch() throws Exception {
        // Initialize the database
        bitmapRepository.save(bitmap).block();

        int databaseSizeBeforeUpdate = bitmapRepository.findAll().collectList().block().size();

        // Update the bitmap using partial update
        Bitmap partialUpdatedBitmap = new Bitmap();
        partialUpdatedBitmap.setId(bitmap.getId());

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedBitmap.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedBitmap))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Bitmap in the database
        List<Bitmap> bitmapList = bitmapRepository.findAll().collectList().block();
        assertThat(bitmapList).hasSize(databaseSizeBeforeUpdate);
        Bitmap testBitmap = bitmapList.get(bitmapList.size() - 1);
        assertThat(testBitmap.getBlob()).isEqualTo(DEFAULT_BLOB);
        assertThat(testBitmap.getBlobContentType()).isEqualTo(DEFAULT_BLOB_CONTENT_TYPE);
    }

    @Test
    void fullUpdateBitmapWithPatch() throws Exception {
        // Initialize the database
        bitmapRepository.save(bitmap).block();

        int databaseSizeBeforeUpdate = bitmapRepository.findAll().collectList().block().size();

        // Update the bitmap using partial update
        Bitmap partialUpdatedBitmap = new Bitmap();
        partialUpdatedBitmap.setId(bitmap.getId());

        partialUpdatedBitmap.blob(UPDATED_BLOB).blobContentType(UPDATED_BLOB_CONTENT_TYPE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedBitmap.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedBitmap))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Bitmap in the database
        List<Bitmap> bitmapList = bitmapRepository.findAll().collectList().block();
        assertThat(bitmapList).hasSize(databaseSizeBeforeUpdate);
        Bitmap testBitmap = bitmapList.get(bitmapList.size() - 1);
        assertThat(testBitmap.getBlob()).isEqualTo(UPDATED_BLOB);
        assertThat(testBitmap.getBlobContentType()).isEqualTo(UPDATED_BLOB_CONTENT_TYPE);
    }

    @Test
    void patchNonExistingBitmap() throws Exception {
        int databaseSizeBeforeUpdate = bitmapRepository.findAll().collectList().block().size();
        bitmap.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, bitmap.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(bitmap))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Bitmap in the database
        List<Bitmap> bitmapList = bitmapRepository.findAll().collectList().block();
        assertThat(bitmapList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Bitmap in Elasticsearch
        verify(mockBitmapSearchRepository, times(0)).save(bitmap);
    }

    @Test
    void patchWithIdMismatchBitmap() throws Exception {
        int databaseSizeBeforeUpdate = bitmapRepository.findAll().collectList().block().size();
        bitmap.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(bitmap))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Bitmap in the database
        List<Bitmap> bitmapList = bitmapRepository.findAll().collectList().block();
        assertThat(bitmapList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Bitmap in Elasticsearch
        verify(mockBitmapSearchRepository, times(0)).save(bitmap);
    }

    @Test
    void patchWithMissingIdPathParamBitmap() throws Exception {
        int databaseSizeBeforeUpdate = bitmapRepository.findAll().collectList().block().size();
        bitmap.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(bitmap))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Bitmap in the database
        List<Bitmap> bitmapList = bitmapRepository.findAll().collectList().block();
        assertThat(bitmapList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Bitmap in Elasticsearch
        verify(mockBitmapSearchRepository, times(0)).save(bitmap);
    }

    @Test
    void deleteBitmap() {
        // Configure the mock search repository
        when(mockBitmapSearchRepository.deleteById(anyLong())).thenReturn(Mono.empty());
        // Initialize the database
        bitmapRepository.save(bitmap).block();

        int databaseSizeBeforeDelete = bitmapRepository.findAll().collectList().block().size();

        // Delete the bitmap
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, bitmap.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<Bitmap> bitmapList = bitmapRepository.findAll().collectList().block();
        assertThat(bitmapList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Bitmap in Elasticsearch
        verify(mockBitmapSearchRepository, times(1)).deleteById(bitmap.getId());
    }

    @Test
    void searchBitmap() {
        // Configure the mock search repository
        when(mockBitmapSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Initialize the database
        bitmapRepository.save(bitmap).block();
        when(mockBitmapSearchRepository.search("id:" + bitmap.getId())).thenReturn(Flux.just(bitmap));

        // Search the bitmap
        webTestClient
            .get()
            .uri(ENTITY_SEARCH_API_URL + "?query=id:" + bitmap.getId())
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(bitmap.getId().intValue()))
            .jsonPath("$.[*].blobContentType")
            .value(hasItem(DEFAULT_BLOB_CONTENT_TYPE))
            .jsonPath("$.[*].blob")
            .value(hasItem(Base64Utils.encodeToString(DEFAULT_BLOB)));
    }
}
