package com.example.batch.chunk;

import org.springframework.batch.item.ItemProcessor;

public class CustomItemProcessor implements ItemProcessor<String, String> {
    @Override
    public String process(String item) throws Exception {
        return item.toUpperCase();
    }
}
