package com.example.app.batch.chunk.processor;

import com.example.app.batch.domain.Product;
import com.example.app.batch.domain.ProductDto;
import org.springframework.batch.item.ItemProcessor;

public class FileItemProcessor implements ItemProcessor<ProductDto, Product> {
    @Override
    public Product process(ProductDto item) throws Exception {
        return new Product(item.name(), item.price(), item.type());
    }
}
