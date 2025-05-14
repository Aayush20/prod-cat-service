package org.example.prodcatservice.dtos.product.requestDtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.example.prodcatservice.models.Category;
import org.example.prodcatservice.models.Product;

@Getter
@Setter
public class UpdateProductRequestDto {
    @Schema(description = "Updated product title", example = "Samsung S24 Ultra")
    private String title;

    @Schema(description = "Updated product description", example = "Next-gen Galaxy smartphone")
    private String description;

    @Schema(description = "Updated product price", example = "1199.99")
    private double price;

    @Schema(description = "Updated category name", example = "mobiles")
    private String categoryName;

    @Schema(description = "Updated stock quantity", example = "25")
    private int stock;

    @Schema(description = "Updated seller name", example = "Samsung Official")
    private String seller;

    @Schema(description = "Updated product image URL", example = "https://cdn.example.com/products/s24ultra.png")
    private String imageUrl;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Product toProduct() {
        Product product = new Product();
        product.setTitle(this.title);
        product.setDescription(this.description);
        product.setPrice(this.price);
        product.setStock(this.stock);
        product.setSeller(this.seller);
        product.setImageUrl(this.imageUrl);

        Category category = new Category();
        category.setName(this.categoryName);
        product.setCategory(category);

        return product;
    }
}