package org.example.prodcatservice.dtos.product.requestDtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.example.prodcatservice.models.Category;
import org.example.prodcatservice.models.Product;

@Getter
@Setter
public class CreateProductRequestDto {
    @Schema(description = "Product title", example = "iPhone 15 Pro")
    private String title;

    @Schema(description = "Product description", example = "Latest Apple smartphone with A17 chip")
    private String description;

    @Schema(description = "Price of product", example = "1299.99")
    private double price;

    @Schema(description = "Name of category", example = "electronics")
    private String categoryName;

    @Schema(description = "Available stock", example = "20")
    private int stock;

    @Schema(description = "Image URL", example = "https://cdn.example.com/products/iphone15pro.png")
    private String imageUrl;

    @Schema(description = "Seller name", example = "Apple Store")
    private String seller;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Product toProduct() {
        Product product = new Product();
        product.setTitle(this.title);
        product.setDescription(this.description);
        product.setPrice(price);
        product.setStock(stock);
        product.setSeller(seller);
        product.setImageUrl(imageUrl);
        Category category = new Category();
        category.setName(categoryName);
        product.setCategory(category);
        return product;
    }
}
