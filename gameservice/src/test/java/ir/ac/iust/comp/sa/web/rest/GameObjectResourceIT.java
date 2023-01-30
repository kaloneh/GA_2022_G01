package ir.ac.iust.comp.sa.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import ir.ac.iust.comp.sa.IntegrationTest;
import ir.ac.iust.comp.sa.domain.GameObject;
import ir.ac.iust.comp.sa.repository.GameObjectRepository;
import ir.ac.iust.comp.sa.repository.search.GameObjectSearchRepository;
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
 * Integration tests for the {@link GameObjectResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient
@WithMockUser
class GameObjectResourceIT {

    private static final Float DEFAULT_X = 1F;
    private static final Float UPDATED_X = 2F;

    private static final Float DEFAULT_Y = 1F;
    private static final Float UPDATED_Y = 2F;

    private static final byte[] DEFAULT_BITMAP = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_BITMAP = TestUtil.createByteArray(1, "1");
    private static final String DEFAULT_BITMAP_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_BITMAP_CONTENT_TYPE = "image/png";

    private static final Boolean DEFAULT_IS_ENABLED = false;
    private static final Boolean UPDATED_IS_ENABLED = true;

    private static final String ENTITY_API_URL = "/api/game-objects";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/game-objects";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private GameObjectRepository gameObjectRepository;

    /**
     * This repository is mocked in the ir.ac.iust.comp.sa.repository.search test package.
     *
     * @see ir.ac.iust.comp.sa.repository.search.GameObjectSearchRepositoryMockConfiguration
     */
    @Autowired
    private GameObjectSearchRepository mockGameObjectSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private GameObject gameObject;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static GameObject createEntity(EntityManager em) {
        GameObject gameObject = new GameObject()
            .x(DEFAULT_X)
            .y(DEFAULT_Y)
            .bitmap(DEFAULT_BITMAP)
            .bitmapContentType(DEFAULT_BITMAP_CONTENT_TYPE)
            .isEnabled(DEFAULT_IS_ENABLED);
        return gameObject;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static GameObject createUpdatedEntity(EntityManager em) {
        GameObject gameObject = new GameObject()
            .x(UPDATED_X)
            .y(UPDATED_Y)
            .bitmap(UPDATED_BITMAP)
            .bitmapContentType(UPDATED_BITMAP_CONTENT_TYPE)
            .isEnabled(UPDATED_IS_ENABLED);
        return gameObject;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(GameObject.class).block();
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
        gameObject = createEntity(em);
    }

    @Test
    void createGameObject() throws Exception {
        int databaseSizeBeforeCreate = gameObjectRepository.findAll().collectList().block().size();
        // Configure the mock search repository
        when(mockGameObjectSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Create the GameObject
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(gameObject))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the GameObject in the database
        List<GameObject> gameObjectList = gameObjectRepository.findAll().collectList().block();
        assertThat(gameObjectList).hasSize(databaseSizeBeforeCreate + 1);
        GameObject testGameObject = gameObjectList.get(gameObjectList.size() - 1);
        assertThat(testGameObject.getX()).isEqualTo(DEFAULT_X);
        assertThat(testGameObject.getY()).isEqualTo(DEFAULT_Y);
        assertThat(testGameObject.getBitmap()).isEqualTo(DEFAULT_BITMAP);
        assertThat(testGameObject.getBitmapContentType()).isEqualTo(DEFAULT_BITMAP_CONTENT_TYPE);
        assertThat(testGameObject.getIsEnabled()).isEqualTo(DEFAULT_IS_ENABLED);

        // Validate the GameObject in Elasticsearch
        verify(mockGameObjectSearchRepository, times(1)).save(testGameObject);
    }

    @Test
    void createGameObjectWithExistingId() throws Exception {
        // Create the GameObject with an existing ID
        gameObject.setId(1L);

        int databaseSizeBeforeCreate = gameObjectRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(gameObject))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the GameObject in the database
        List<GameObject> gameObjectList = gameObjectRepository.findAll().collectList().block();
        assertThat(gameObjectList).hasSize(databaseSizeBeforeCreate);

        // Validate the GameObject in Elasticsearch
        verify(mockGameObjectSearchRepository, times(0)).save(gameObject);
    }

    @Test
    void getAllGameObjectsAsStream() {
        // Initialize the database
        gameObjectRepository.save(gameObject).block();

        List<GameObject> gameObjectList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(GameObject.class)
            .getResponseBody()
            .filter(gameObject::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(gameObjectList).isNotNull();
        assertThat(gameObjectList).hasSize(1);
        GameObject testGameObject = gameObjectList.get(0);
        assertThat(testGameObject.getX()).isEqualTo(DEFAULT_X);
        assertThat(testGameObject.getY()).isEqualTo(DEFAULT_Y);
        assertThat(testGameObject.getBitmap()).isEqualTo(DEFAULT_BITMAP);
        assertThat(testGameObject.getBitmapContentType()).isEqualTo(DEFAULT_BITMAP_CONTENT_TYPE);
        assertThat(testGameObject.getIsEnabled()).isEqualTo(DEFAULT_IS_ENABLED);
    }

    @Test
    void getAllGameObjects() {
        // Initialize the database
        gameObjectRepository.save(gameObject).block();

        // Get all the gameObjectList
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
            .value(hasItem(gameObject.getId().intValue()))
            .jsonPath("$.[*].x")
            .value(hasItem(DEFAULT_X.doubleValue()))
            .jsonPath("$.[*].y")
            .value(hasItem(DEFAULT_Y.doubleValue()))
            .jsonPath("$.[*].bitmapContentType")
            .value(hasItem(DEFAULT_BITMAP_CONTENT_TYPE))
            .jsonPath("$.[*].bitmap")
            .value(hasItem(Base64Utils.encodeToString(DEFAULT_BITMAP)))
            .jsonPath("$.[*].isEnabled")
            .value(hasItem(DEFAULT_IS_ENABLED.booleanValue()));
    }

    @Test
    void getGameObject() {
        // Initialize the database
        gameObjectRepository.save(gameObject).block();

        // Get the gameObject
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, gameObject.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(gameObject.getId().intValue()))
            .jsonPath("$.x")
            .value(is(DEFAULT_X.doubleValue()))
            .jsonPath("$.y")
            .value(is(DEFAULT_Y.doubleValue()))
            .jsonPath("$.bitmapContentType")
            .value(is(DEFAULT_BITMAP_CONTENT_TYPE))
            .jsonPath("$.bitmap")
            .value(is(Base64Utils.encodeToString(DEFAULT_BITMAP)))
            .jsonPath("$.isEnabled")
            .value(is(DEFAULT_IS_ENABLED.booleanValue()));
    }

    @Test
    void getNonExistingGameObject() {
        // Get the gameObject
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewGameObject() throws Exception {
        // Configure the mock search repository
        when(mockGameObjectSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Initialize the database
        gameObjectRepository.save(gameObject).block();

        int databaseSizeBeforeUpdate = gameObjectRepository.findAll().collectList().block().size();

        // Update the gameObject
        GameObject updatedGameObject = gameObjectRepository.findById(gameObject.getId()).block();
        updatedGameObject
            .x(UPDATED_X)
            .y(UPDATED_Y)
            .bitmap(UPDATED_BITMAP)
            .bitmapContentType(UPDATED_BITMAP_CONTENT_TYPE)
            .isEnabled(UPDATED_IS_ENABLED);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedGameObject.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedGameObject))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the GameObject in the database
        List<GameObject> gameObjectList = gameObjectRepository.findAll().collectList().block();
        assertThat(gameObjectList).hasSize(databaseSizeBeforeUpdate);
        GameObject testGameObject = gameObjectList.get(gameObjectList.size() - 1);
        assertThat(testGameObject.getX()).isEqualTo(UPDATED_X);
        assertThat(testGameObject.getY()).isEqualTo(UPDATED_Y);
        assertThat(testGameObject.getBitmap()).isEqualTo(UPDATED_BITMAP);
        assertThat(testGameObject.getBitmapContentType()).isEqualTo(UPDATED_BITMAP_CONTENT_TYPE);
        assertThat(testGameObject.getIsEnabled()).isEqualTo(UPDATED_IS_ENABLED);

        // Validate the GameObject in Elasticsearch
        verify(mockGameObjectSearchRepository).save(testGameObject);
    }

    @Test
    void putNonExistingGameObject() throws Exception {
        int databaseSizeBeforeUpdate = gameObjectRepository.findAll().collectList().block().size();
        gameObject.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, gameObject.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(gameObject))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the GameObject in the database
        List<GameObject> gameObjectList = gameObjectRepository.findAll().collectList().block();
        assertThat(gameObjectList).hasSize(databaseSizeBeforeUpdate);

        // Validate the GameObject in Elasticsearch
        verify(mockGameObjectSearchRepository, times(0)).save(gameObject);
    }

    @Test
    void putWithIdMismatchGameObject() throws Exception {
        int databaseSizeBeforeUpdate = gameObjectRepository.findAll().collectList().block().size();
        gameObject.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(gameObject))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the GameObject in the database
        List<GameObject> gameObjectList = gameObjectRepository.findAll().collectList().block();
        assertThat(gameObjectList).hasSize(databaseSizeBeforeUpdate);

        // Validate the GameObject in Elasticsearch
        verify(mockGameObjectSearchRepository, times(0)).save(gameObject);
    }

    @Test
    void putWithMissingIdPathParamGameObject() throws Exception {
        int databaseSizeBeforeUpdate = gameObjectRepository.findAll().collectList().block().size();
        gameObject.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(gameObject))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the GameObject in the database
        List<GameObject> gameObjectList = gameObjectRepository.findAll().collectList().block();
        assertThat(gameObjectList).hasSize(databaseSizeBeforeUpdate);

        // Validate the GameObject in Elasticsearch
        verify(mockGameObjectSearchRepository, times(0)).save(gameObject);
    }

    @Test
    void partialUpdateGameObjectWithPatch() throws Exception {
        // Initialize the database
        gameObjectRepository.save(gameObject).block();

        int databaseSizeBeforeUpdate = gameObjectRepository.findAll().collectList().block().size();

        // Update the gameObject using partial update
        GameObject partialUpdatedGameObject = new GameObject();
        partialUpdatedGameObject.setId(gameObject.getId());

        partialUpdatedGameObject
            .y(UPDATED_Y)
            .bitmap(UPDATED_BITMAP)
            .bitmapContentType(UPDATED_BITMAP_CONTENT_TYPE)
            .isEnabled(UPDATED_IS_ENABLED);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedGameObject.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedGameObject))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the GameObject in the database
        List<GameObject> gameObjectList = gameObjectRepository.findAll().collectList().block();
        assertThat(gameObjectList).hasSize(databaseSizeBeforeUpdate);
        GameObject testGameObject = gameObjectList.get(gameObjectList.size() - 1);
        assertThat(testGameObject.getX()).isEqualTo(DEFAULT_X);
        assertThat(testGameObject.getY()).isEqualTo(UPDATED_Y);
        assertThat(testGameObject.getBitmap()).isEqualTo(UPDATED_BITMAP);
        assertThat(testGameObject.getBitmapContentType()).isEqualTo(UPDATED_BITMAP_CONTENT_TYPE);
        assertThat(testGameObject.getIsEnabled()).isEqualTo(UPDATED_IS_ENABLED);
    }

    @Test
    void fullUpdateGameObjectWithPatch() throws Exception {
        // Initialize the database
        gameObjectRepository.save(gameObject).block();

        int databaseSizeBeforeUpdate = gameObjectRepository.findAll().collectList().block().size();

        // Update the gameObject using partial update
        GameObject partialUpdatedGameObject = new GameObject();
        partialUpdatedGameObject.setId(gameObject.getId());

        partialUpdatedGameObject
            .x(UPDATED_X)
            .y(UPDATED_Y)
            .bitmap(UPDATED_BITMAP)
            .bitmapContentType(UPDATED_BITMAP_CONTENT_TYPE)
            .isEnabled(UPDATED_IS_ENABLED);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedGameObject.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedGameObject))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the GameObject in the database
        List<GameObject> gameObjectList = gameObjectRepository.findAll().collectList().block();
        assertThat(gameObjectList).hasSize(databaseSizeBeforeUpdate);
        GameObject testGameObject = gameObjectList.get(gameObjectList.size() - 1);
        assertThat(testGameObject.getX()).isEqualTo(UPDATED_X);
        assertThat(testGameObject.getY()).isEqualTo(UPDATED_Y);
        assertThat(testGameObject.getBitmap()).isEqualTo(UPDATED_BITMAP);
        assertThat(testGameObject.getBitmapContentType()).isEqualTo(UPDATED_BITMAP_CONTENT_TYPE);
        assertThat(testGameObject.getIsEnabled()).isEqualTo(UPDATED_IS_ENABLED);
    }

    @Test
    void patchNonExistingGameObject() throws Exception {
        int databaseSizeBeforeUpdate = gameObjectRepository.findAll().collectList().block().size();
        gameObject.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, gameObject.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(gameObject))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the GameObject in the database
        List<GameObject> gameObjectList = gameObjectRepository.findAll().collectList().block();
        assertThat(gameObjectList).hasSize(databaseSizeBeforeUpdate);

        // Validate the GameObject in Elasticsearch
        verify(mockGameObjectSearchRepository, times(0)).save(gameObject);
    }

    @Test
    void patchWithIdMismatchGameObject() throws Exception {
        int databaseSizeBeforeUpdate = gameObjectRepository.findAll().collectList().block().size();
        gameObject.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(gameObject))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the GameObject in the database
        List<GameObject> gameObjectList = gameObjectRepository.findAll().collectList().block();
        assertThat(gameObjectList).hasSize(databaseSizeBeforeUpdate);

        // Validate the GameObject in Elasticsearch
        verify(mockGameObjectSearchRepository, times(0)).save(gameObject);
    }

    @Test
    void patchWithMissingIdPathParamGameObject() throws Exception {
        int databaseSizeBeforeUpdate = gameObjectRepository.findAll().collectList().block().size();
        gameObject.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(gameObject))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the GameObject in the database
        List<GameObject> gameObjectList = gameObjectRepository.findAll().collectList().block();
        assertThat(gameObjectList).hasSize(databaseSizeBeforeUpdate);

        // Validate the GameObject in Elasticsearch
        verify(mockGameObjectSearchRepository, times(0)).save(gameObject);
    }

    @Test
    void deleteGameObject() {
        // Configure the mock search repository
        when(mockGameObjectSearchRepository.deleteById(anyLong())).thenReturn(Mono.empty());
        // Initialize the database
        gameObjectRepository.save(gameObject).block();

        int databaseSizeBeforeDelete = gameObjectRepository.findAll().collectList().block().size();

        // Delete the gameObject
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, gameObject.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<GameObject> gameObjectList = gameObjectRepository.findAll().collectList().block();
        assertThat(gameObjectList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the GameObject in Elasticsearch
        verify(mockGameObjectSearchRepository, times(1)).deleteById(gameObject.getId());
    }

    @Test
    void searchGameObject() {
        // Configure the mock search repository
        when(mockGameObjectSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Initialize the database
        gameObjectRepository.save(gameObject).block();
        when(mockGameObjectSearchRepository.search("id:" + gameObject.getId())).thenReturn(Flux.just(gameObject));

        // Search the gameObject
        webTestClient
            .get()
            .uri(ENTITY_SEARCH_API_URL + "?query=id:" + gameObject.getId())
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(gameObject.getId().intValue()))
            .jsonPath("$.[*].x")
            .value(hasItem(DEFAULT_X.doubleValue()))
            .jsonPath("$.[*].y")
            .value(hasItem(DEFAULT_Y.doubleValue()))
            .jsonPath("$.[*].bitmapContentType")
            .value(hasItem(DEFAULT_BITMAP_CONTENT_TYPE))
            .jsonPath("$.[*].bitmap")
            .value(hasItem(Base64Utils.encodeToString(DEFAULT_BITMAP)))
            .jsonPath("$.[*].isEnabled")
            .value(hasItem(DEFAULT_IS_ENABLED.booleanValue()));
    }
}
