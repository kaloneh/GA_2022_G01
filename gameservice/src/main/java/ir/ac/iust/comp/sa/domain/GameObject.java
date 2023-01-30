package ir.ac.iust.comp.sa.domain;

import java.io.Serializable;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A GameObject.
 */
@Table("game_object")
@org.springframework.data.elasticsearch.annotations.Document(indexName = "gameobject")
public class GameObject implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @Column("x")
    private Float x;

    @Column("y")
    private Float y;

    @Column("bitmap")
    private byte[] bitmap;

    @Column("bitmap_content_type")
    private String bitmapContentType;

    @Column("is_enabled")
    private Boolean isEnabled;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public GameObject id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Float getX() {
        return this.x;
    }

    public GameObject x(Float x) {
        this.setX(x);
        return this;
    }

    public void setX(Float x) {
        this.x = x;
    }

    public Float getY() {
        return this.y;
    }

    public GameObject y(Float y) {
        this.setY(y);
        return this;
    }

    public void setY(Float y) {
        this.y = y;
    }

    public byte[] getBitmap() {
        return this.bitmap;
    }

    public GameObject bitmap(byte[] bitmap) {
        this.setBitmap(bitmap);
        return this;
    }

    public void setBitmap(byte[] bitmap) {
        this.bitmap = bitmap;
    }

    public String getBitmapContentType() {
        return this.bitmapContentType;
    }

    public GameObject bitmapContentType(String bitmapContentType) {
        this.bitmapContentType = bitmapContentType;
        return this;
    }

    public void setBitmapContentType(String bitmapContentType) {
        this.bitmapContentType = bitmapContentType;
    }

    public Boolean getIsEnabled() {
        return this.isEnabled;
    }

    public GameObject isEnabled(Boolean isEnabled) {
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
        if (!(o instanceof GameObject)) {
            return false;
        }
        return id != null && id.equals(((GameObject) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "GameObject{" +
            "id=" + getId() +
            ", x=" + getX() +
            ", y=" + getY() +
            ", bitmap='" + getBitmap() + "'" +
            ", bitmapContentType='" + getBitmapContentType() + "'" +
            ", isEnabled='" + getIsEnabled() + "'" +
            "}";
    }
}
