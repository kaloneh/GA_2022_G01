package ir.ac.iust.comp.sa.domain;

import ir.ac.iust.comp.sa.domain.enumeration.EnumModType;
import java.io.Serializable;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A ModificationType.
 */
@Table("modification_type")
@org.springframework.data.elasticsearch.annotations.Document(indexName = "modificationtype")
public class ModificationType implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @Column("type")
    private EnumModType type;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public ModificationType id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EnumModType getType() {
        return this.type;
    }

    public ModificationType type(EnumModType type) {
        this.setType(type);
        return this;
    }

    public void setType(EnumModType type) {
        this.type = type;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ModificationType)) {
            return false;
        }
        return id != null && id.equals(((ModificationType) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ModificationType{" +
            "id=" + getId() +
            ", type='" + getType() + "'" +
            "}";
    }
}
