package ir.ac.iust.comp.sa.domain;

import java.io.Serializable;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A Layer.
 */
@Table("layer")
@org.springframework.data.elasticsearch.annotations.Document(indexName = "layer")
public class Layer implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @Column("x")
    private Float x;

    @Column("y")
    private Float y;

    @Column("buffer")
    private byte[] buffer;

    @Column("buffer_content_type")
    private String bufferContentType;

    @Column("is_enabled")
    private Boolean isEnabled;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Layer id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Float getX() {
        return this.x;
    }

    public Layer x(Float x) {
        this.setX(x);
        return this;
    }

    public void setX(Float x) {
        this.x = x;
    }

    public Float getY() {
        return this.y;
    }

    public Layer y(Float y) {
        this.setY(y);
        return this;
    }

    public void setY(Float y) {
        this.y = y;
    }

    public byte[] getBuffer() {
        return this.buffer;
    }

    public Layer buffer(byte[] buffer) {
        this.setBuffer(buffer);
        return this;
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }

    public String getBufferContentType() {
        return this.bufferContentType;
    }

    public Layer bufferContentType(String bufferContentType) {
        this.bufferContentType = bufferContentType;
        return this;
    }

    public void setBufferContentType(String bufferContentType) {
        this.bufferContentType = bufferContentType;
    }

    public Boolean getIsEnabled() {
        return this.isEnabled;
    }

    public Layer isEnabled(Boolean isEnabled) {
        this.setIsEnabled(isEnabled);
        return this;
    }

    public void setIsEnabled(Boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Layer)) {
            return false;
        }
        return id != null && id.equals(((Layer) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Layer{" +
            "id=" + getId() +
            ", x=" + getX() +
            ", y=" + getY() +
            ", buffer='" + getBuffer() + "'" +
            ", bufferContentType='" + getBufferContentType() + "'" +
            ", isEnabled='" + getIsEnabled() + "'" +
            "}";
    }
}
