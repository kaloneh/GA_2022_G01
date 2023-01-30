package ir.ac.iust.comp.sa.service.dto;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Lob;

/**
 * A DTO for the {@link ir.ac.iust.comp.sa.domain.Layer} entity.
 */
public class LayerDTO implements Serializable {

    private Long id;

    private Float x;

    private Float y;

    @Lob
    private byte[] buffer;

    private String bufferContentType;
    private Boolean isEnabled;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Float getX() {
        return x;
    }

    public void setX(Float x) {
        this.x = x;
    }

    public Float getY() {
        return y;
    }

    public void setY(Float y) {
        this.y = y;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }

    public String getBufferContentType() {
        return bufferContentType;
    }

    public void setBufferContentType(String bufferContentType) {
        this.bufferContentType = bufferContentType;
    }

    public Boolean getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(Boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LayerDTO)) {
            return false;
        }

        LayerDTO layerDTO = (LayerDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, layerDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "LayerDTO{" +
            "id=" + getId() +
            ", x=" + getX() +
            ", y=" + getY() +
            ", buffer='" + getBuffer() + "'" +
            ", isEnabled='" + getIsEnabled() + "'" +
            "}";
    }
}
