package ir.ac.iust.comp.sa.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import ir.ac.iust.comp.sa.IntegrationTest;
import ir.ac.iust.comp.sa.domain.Layer;
import ir.ac.iust.comp.sa.repository.LayerRepository;
import ir.ac.iust.comp.sa.repository.search.LayerSearchRepository;
import ir.ac.iust.comp.sa.service.EntityManager;
import ir.ac.iust.comp.sa.service.dto.LayerDTO;
import ir.ac.iust.comp.sa.service.mapper.LayerMapper;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.Base64Utils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Integration tests for the {@link LayerResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient
@WithMockUser
class LayerResourceIT {

    private static final Float DEFAULT_X = 1F;
    private static final Float UPDATED_X = 2F;

    private static final Float DEFAULT_Y = 1F;
    private static final Float UPDATED_Y = 2F;

    private static final byte[] DEFAULT_BUFFER = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_BUFFER = TestUtil.createByteArray(1, "1");
    private static final String DEFAULT_BUFFER_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_BUFFER_CONTENT_TYPE = "image/png";

    private static final Boolean DEFAULT_IS_ENABLED = false;
    private static final Boolean UPDATED_IS_ENABLED = true;

    private static final String ENTITY_API_URL = "/api/layers";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/layers";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private LayerRepository layerRepository;

    @Autowired
    private LayerMapper layerMapper;

    /**
     * This repository is mocked in the ir.ac.iust.comp.sa.repository.search test package.
     *
     * @see ir.ac.iust.comp.sa.repository.search.LayerSearchRepositoryMockConfiguration
     */
    @Autowired
    private LayerSearchRepository mockLayerSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Layer layer;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Layer createEntity(EntityManager em) {
        Layer layer = new Layer()
            .x(DEFAULT_X)
            .y(DEFAULT_Y)
            .buffer(DEFAULT_BUFFER)
            .bufferContentType(DEFAULT_BUFFER_CONTENT_TYPE)
            .isEnabled(DEFAULT_IS_ENABLED);
        return layer;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Layer createUpdatedEntity(EntityManager em) {
        Layer layer = new Layer()
            .x(UPDATED_X)
            .y(UPDATED_Y)
            .buffer(UPDATED_BUFFER)
            .bufferContentType(UPDATED_BUFFER_CONTENT_TYPE)
            .isEnabled(UPDATED_IS_ENABLED);
        return layer;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Layer.class).block();
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
        layer = createEntity(em);
    }

    @Test
    void createLayer() throws Exception {
        int databaseSizeBeforeCreate = layerRepository.findAll().collectList().block().size();
        // Configure the mock search repository
        when(mockLayerSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Create the Layer
        LayerDTO layerDTO = layerMapper.toDto(layer);
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(layerDTO))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the Layer in the database
        List<Layer> layerList = layerRepository.findAll().collectList().block();
        assertThat(layerList).hasSize(databaseSizeBeforeCreate + 1);
        Layer testLayer = layerList.get(layerList.size() - 1);
        assertThat(testLayer.getX()).isEqualTo(DEFAULT_X);
        assertThat(testLayer.getY()).isEqualTo(DEFAULT_Y);
        assertThat(testLayer.getBuffer()).isEqualTo(DEFAULT_BUFFER);
        assertThat(testLayer.getBufferContentType()).isEqualTo(DEFAULT_BUFFER_CONTENT_TYPE);
        assertThat(testLayer.getIsEnabled()).isEqualTo(DEFAULT_IS_ENABLED);

        // Validate the Layer in Elasticsearch
        verify(mockLayerSearchRepository, times(1)).save(testLayer);
    }

    @Test
    void createLayerWithExistingId() throws Exception {
        // Create the Layer with an existing ID
        layer.setId(1L);
        LayerDTO layerDTO = layerMapper.toDto(layer);

        int databaseSizeBeforeCreate = layerRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(layerDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Layer in the database
        List<Layer> layerList = layerRepository.findAll().collectList().block();
        assertThat(layerList).hasSize(databaseSizeBeforeCreate);

        // Validate the Layer in Elasticsearch
        verify(mockLayerSearchRepository, times(0)).save(layer);
    }

    @Test
    void getAllLayers() {
        // Initialize the database
        layerRepository.save(layer).block();

        // Get all the layerList
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
            .value(hasItem(layer.getId().intValue()))
            .jsonPath("$.[*].x")
            .value(hasItem(DEFAULT_X.doubleValue()))
            .jsonPath("$.[*].y")
            .value(hasItem(DEFAULT_Y.doubleValue()))
            .jsonPath("$.[*].bufferContentType")
            .value(hasItem(DEFAULT_BUFFER_CONTENT_TYPE))
            .jsonPath("$.[*].buffer")
            .value(hasItem(Base64Utils.encodeToString(DEFAULT_BUFFER)))
            .jsonPath("$.[*].isEnabled")
            .value(hasItem(DEFAULT_IS_ENABLED.booleanValue()));
    }

    @Test
    void getLayer() {
        // Initialize the database
        layerRepository.save(layer).block();

        // Get the layer
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, layer.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(layer.getId().intValue()))
            .jsonPath("$.x")
            .value(is(DEFAULT_X.doubleValue()))
            .jsonPath("$.y")
            .value(is(DEFAULT_Y.doubleValue()))
            .jsonPath("$.bufferContentType")
            .value(is(DEFAULT_BUFFER_CONTENT_TYPE))
            .jsonPath("$.buffer")
            .value(is(Base64Utils.encodeToString(DEFAULT_BUFFER)))
            .jsonPath("$.isEnabled")
            .value(is(DEFAULT_IS_ENABLED.booleanValue()));
    }

    @Test
    void getNonExistingLayer() {
        // Get the layer
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewLayer() throws Exception {
        // Configure the mock search repository
        when(mockLayerSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Initialize the database
        layerRepository.save(layer).block();

        int databaseSizeBeforeUpdate = layerRepository.findAll().collectList().block().size();

        // Update the layer
        Layer updatedLayer = layerRepository.findById(layer.getId()).block();
        updatedLayer
            .x(UPDATED_X)
            .y(UPDATED_Y)
            .buffer(UPDATED_BUFFER)
            .bufferContentType(UPDATED_BUFFER_CONTENT_TYPE)
            .isEnabled(UPDATED_IS_ENABLED);
        LayerDTO layerDTO = layerMapper.toDto(updatedLayer);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, layerDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(layerDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Layer in the database
        List<Layer> layerList = layerRepository.findAll().collectList().block();
        assertThat(layerList).hasSize(databaseSizeBeforeUpdate);
        Layer testLayer = layerList.get(layerList.size() - 1);
        assertThat(testLayer.getX()).isEqualTo(UPDATED_X);
        assertThat(testLayer.getY()).isEqualTo(UPDATED_Y);
        assertThat(testLayer.getBuffer()).isEqualTo(UPDATED_BUFFER);
        assertThat(testLayer.getBufferContentType()).isEqualTo(UPDATED_BUFFER_CONTENT_TYPE);
        assertThat(testLayer.getIsEnabled()).isEqualTo(UPDATED_IS_ENABLED);

        // Validate the Layer in Elasticsearch
        verify(mockLayerSearchRepository).save(testLayer);
    }

    @Test
    void putNonExistingLayer() throws Exception {
        int databaseSizeBeforeUpdate = layerRepository.findAll().collectList().block().size();
        layer.setId(count.incrementAndGet());

        // Create the Layer
        LayerDTO layerDTO = layerMapper.toDto(layer);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, layerDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(layerDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Layer in the database
        List<Layer> layerList = layerRepository.findAll().collectList().block();
        assertThat(layerList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Layer in Elasticsearch
        verify(mockLayerSearchRepository, times(0)).save(layer);
    }

    @Test
    void putWithIdMismatchLayer() throws Exception {
        int databaseSizeBeforeUpdate = layerRepository.findAll().collectList().block().size();
        layer.setId(count.incrementAndGet());

        // Create the Layer
        LayerDTO layerDTO = layerMapper.toDto(layer);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(layerDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Layer in the database
        List<Layer> layerList = layerRepository.findAll().collectList().block();
        assertThat(layerList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Layer in Elasticsearch
        verify(mockLayerSearchRepository, times(0)).save(layer);
    }

    @Test
    void putWithMissingIdPathParamLayer() throws Exception {
        int databaseSizeBeforeUpdate = layerRepository.findAll().collectList().block().size();
        layer.setId(count.incrementAndGet());

        // Create the Layer
        LayerDTO layerDTO = layerMapper.toDto(layer);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(layerDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Layer in the database
        List<Layer> layerList = layerRepository.findAll().collectList().block();
        assertThat(layerList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Layer in Elasticsearch
        verify(mockLayerSearchRepository, times(0)).save(layer);
    }

    @Test
    void partialUpdateLayerWithPatch() throws Exception {
        // Initialize the database
        layerRepository.save(layer).block();

        int databaseSizeBeforeUpdate = layerRepository.findAll().collectList().block().size();

        // Update the layer using partial update
        Layer partialUpdatedLayer = new Layer();
        partialUpdatedLayer.setId(layer.getId());

        partialUpdatedLayer.buffer(UPDATED_BUFFER).bufferContentType(UPDATED_BUFFER_CONTENT_TYPE).isEnabled(UPDATED_IS_ENABLED);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedLayer.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedLayer))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Layer in the database
        List<Layer> layerList = layerRepository.findAll().collectList().block();
        assertThat(layerList).hasSize(databaseSizeBeforeUpdate);
        Layer testLayer = layerList.get(layerList.size() - 1);
        assertThat(testLayer.getX()).isEqualTo(DEFAULT_X);
        assertThat(testLayer.getY()).isEqualTo(DEFAULT_Y);
        assertThat(testLayer.getBuffer()).isEqualTo(UPDATED_BUFFER);
        assertThat(testLayer.getBufferContentType()).isEqualTo(UPDATED_BUFFER_CONTENT_TYPE);
        assertThat(testLayer.getIsEnabled()).isEqualTo(UPDATED_IS_ENABLED);
    }

    @Test
    void fullUpdateLayerWithPatch() throws Exception {
        // Initialize the database
        layerRepository.save(layer).block();

        int databaseSizeBeforeUpdate = layerRepository.findAll().collectList().block().size();

        // Update the layer using partial update
        Layer partialUpdatedLayer = new Layer();
        partialUpdatedLayer.setId(layer.getId());

        partialUpdatedLayer
            .x(UPDATED_X)
            .y(UPDATED_Y)
            .buffer(UPDATED_BUFFER)
            .bufferContentType(UPDATED_BUFFER_CONTENT_TYPE)
            .isEnabled(UPDATED_IS_ENABLED);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedLayer.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedLayer))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Layer in the database
        List<Layer> layerList = layerRepository.findAll().collectList().block();
        assertThat(layerList).hasSize(databaseSizeBeforeUpdate);
        Layer testLayer = layerList.get(layerList.size() - 1);
        assertThat(testLayer.getX()).isEqualTo(UPDATED_X);
        assertThat(testLayer.getY()).isEqualTo(UPDATED_Y);
        assertThat(testLayer.getBuffer()).isEqualTo(UPDATED_BUFFER);
        assertThat(testLayer.getBufferContentType()).isEqualTo(UPDATED_BUFFER_CONTENT_TYPE);
        assertThat(testLayer.getIsEnabled()).isEqualTo(UPDATED_IS_ENABLED);
    }

    @Test
    void patchNonExistingLayer() throws Exception {
        int databaseSizeBeforeUpdate = layerRepository.findAll().collectList().block().size();
        layer.setId(count.incrementAndGet());

        // Create the Layer
        LayerDTO layerDTO = layerMapper.toDto(layer);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, layerDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(layerDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Layer in the database
        List<Layer> layerList = layerRepository.findAll().collectList().block();
        assertThat(layerList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Layer in Elasticsearch
        verify(mockLayerSearchRepository, times(0)).save(layer);
    }

    @Test
    void patchWithIdMismatchLayer() throws Exception {
        int databaseSizeBeforeUpdate = layerRepository.findAll().collectList().block().size();
        layer.setId(count.incrementAndGet());

        // Create the Layer
        LayerDTO layerDTO = layerMapper.toDto(layer);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(layerDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Layer in the database
        List<Layer> layerList = layerRepository.findAll().collectList().block();
        assertThat(layerList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Layer in Elasticsearch
        verify(mockLayerSearchRepository, times(0)).save(layer);
    }

    @Test
    void patchWithMissingIdPathParamLayer() throws Exception {
        int databaseSizeBeforeUpdate = layerRepository.findAll().collectList().block().size();
        layer.setId(count.incrementAndGet());

        // Create the Layer
        LayerDTO layerDTO = layerMapper.toDto(layer);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(layerDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Layer in the database
        List<Layer> layerList = layerRepository.findAll().collectList().block();
        assertThat(layerList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Layer in Elasticsearch
        verify(mockLayerSearchRepository, times(0)).save(layer);
    }

    @Test
    void deleteLayer() {
        // Configure the mock search repository
        when(mockLayerSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(mockLayerSearchRepository.deleteById(anyLong())).thenReturn(Mono.empty());
        // Initialize the database
        layerRepository.save(layer).block();

        int databaseSizeBeforeDelete = layerRepository.findAll().collectList().block().size();

        // Delete the layer
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, layer.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<Layer> layerList = layerRepository.findAll().collectList().block();
        assertThat(layerList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Layer in Elasticsearch
        verify(mockLayerSearchRepository, times(1)).deleteById(layer.getId());
    }

    @Test
    void searchLayer() {
        // Configure the mock search repository
        when(mockLayerSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(mockLayerSearchRepository.count()).thenReturn(Mono.just(1L));
        // Initialize the database
        layerRepository.save(layer).block();
        when(mockLayerSearchRepository.search("id:" + layer.getId(), PageRequest.of(0, 20))).thenReturn(Flux.just(layer));

        // Search the layer
        webTestClient
            .get()
            .uri(ENTITY_SEARCH_API_URL + "?query=id:" + layer.getId())
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(layer.getId().intValue()))
            .jsonPath("$.[*].x")
            .value(hasItem(DEFAULT_X.doubleValue()))
            .jsonPath("$.[*].y")
            .value(hasItem(DEFAULT_Y.doubleValue()))
            .jsonPath("$.[*].bufferContentType")
            .value(hasItem(DEFAULT_BUFFER_CONTENT_TYPE))
            .jsonPath("$.[*].buffer")
            .value(hasItem(Base64Utils.encodeToString(DEFAULT_BUFFER)))
            .jsonPath("$.[*].isEnabled")
            .value(hasItem(DEFAULT_IS_ENABLED.booleanValue()));
    }
}
