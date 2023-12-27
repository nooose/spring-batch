package com.example.batch.chunk;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

public class CustomItemWriter implements ItemWriter<String> {

    @Override
    public void write(Chunk<? extends String> chunk) throws Exception {
        System.out.println("chunk size: " + chunk.size());
        List<? extends String> items = chunk.getItems();
        items.forEach(System.out::println);
    }
}
