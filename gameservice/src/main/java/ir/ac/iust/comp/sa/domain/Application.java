package ir.ac.iust.comp.sa.domain;

import java.io.Serializable;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A Application.
 */
@Table("application")
@org.springframework.data.elasticsearch.annotations.Document(indexName = "application")
public class Application implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @Column("width")
    private Float width;

    @Column("height")
    private Float height;

    @Column("screen_buffer")
    private byte[] screenBuffer;

    @Column("screen_buffer_content_type")
    private String screenBufferContentType;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Application id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Float getWidth() {
        return this.width;
    }

    public Application width(Float width) {
        this.setWidth(width);
        return this;
    }

    public void setWidth(Float width) {
        this.width = width;
    }

    public Float getHeight() {
        return this.height;
    }

    public Application height(Float height) {
        this.setHeight(height);
        return this;
    }

    public void setHeight(Float height) {
        this.height = height;
    }

    public byte[] getScreenBuffer() {
        return this.screenBuffer;
    }

    public Application screenBuffer(byte[] screenBuffer) {
        this.setScreenBuffer(screenBuffer);
        return this;
    }

    public void setScreenBuffer(byte[] screenBuffer) {
        this.screenBuffer = screenBuffer;
    }

    public String getScreenBufferContentType() {
        return this.screenBufferContentType;
    }

    public Application screenBufferContentType(String screenBufferContentType) {
        this.screenBufferContentType = screenBufferContentType;
        return this;
    }

    public void setScreenBufferContentType(String screenBufferContentType) {
        this.screenBufferContentType = screenBufferContentType;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Application)) {
            return false;
        }
        return id != null && id.equals(((Application) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Application{" +
            "id=" + getId() +
            ", width=" + getWidth() +
            ", height=" + getHeight() +
            ", screenBuffer='" + getScreenBuffer() + "'" +
            ", screenBufferContentType='" + getScreenBufferContentType() + "'" +
            "}";
    }
}
