package com.example.batch.chunk;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import java.util.ArrayList;
import java.util.List;

public class CustomItemReader implements ItemReader<String> {

    private List<String> strings;

    public CustomItemReader(List<String> strings) {
        this.strings = new ArrayList<>(strings);
    }

    @Override
    public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        System.out.println("read...");
        if (!strings.isEmpty()) {
            return strings.remove(0);
        }

        return null;
    }
}
