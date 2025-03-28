package org.example.prodcatservice.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.data.repository.cdi.Eager;

import java.util.List;

@Getter
@Setter
@Entity
public class Category extends BaseModel {
    private String name;
    @Basic(fetch = FetchType.LAZY)
    private String description;

    @OneToMany(mappedBy = "category",cascade= {CascadeType.PERSIST, CascadeType.REMOVE})
    private List<Product> products;

    @OneToMany(fetch = FetchType.EAGER)
    @Fetch(FetchMode.JOIN)
    private List<Product> featuredProducts;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public List<Product> getFeaturedProducts() {
        return featuredProducts;
    }

    public void setFeaturedProducts(List<Product> featuredProducts) {
        this.featuredProducts = featuredProducts;
    }
}
