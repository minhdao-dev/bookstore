package com.bookstore.warehouse.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@Entity
@Table(name = "warehouse")
@Getter
@Setter
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private @Nullable UUID id;

    @Column(nullable = false)
    private String name;

    private @Nullable String address;

    @Column(name = "ghn_district_id")
    private @Nullable Integer ghnDistrictId;

    @Column(name = "ghn_ward_code", length = 20)
    private @Nullable String ghnWardCode;

    protected Warehouse() {
    }

    public Warehouse(String name, @Nullable String address,
                     @Nullable Integer ghnDistrictId, @Nullable String ghnWardCode) {
        this.name = name;
        this.address = address;
        this.ghnDistrictId = ghnDistrictId;
        this.ghnWardCode = ghnWardCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Warehouse other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}