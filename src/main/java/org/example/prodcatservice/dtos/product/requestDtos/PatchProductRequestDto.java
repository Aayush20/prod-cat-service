package org.example.prodcatservice.dtos.product.requestDtos;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.prodcatservice.models.Category;
import org.example.prodcatservice.models.Product;

public class PatchProductRequestDto {
    @Schema(description = "Updated product title", example = "Samsung S24 Ultra")
    private String title;

    @Schema(description = "Updated description", example = "Next-gen Galaxy phone")
    private String description;

    @Schema(description = "Updated price", example = "1099.99")
    private double price;

    @Schema(description = "Updated category", example = "mobiles")
    private String categoryName;

    @Schema(description = "Updated stock", example = "25")
    private int stock;

    @Schema(description = "Updated image URL", example = "https://cdn.example.com/samsung.png")
    private String imageUrl;

    @Schema(description = "Updated seller", example = "Samsung Store")
    private String seller;


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
