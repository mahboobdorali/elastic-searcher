package com.example.elasticsearcher.controller;

import com.example.elasticsearcher.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {
    private final SearchService service;

    @PostMapping("/{index}")
    public List<Map<String, Object>> search(@PathVariable String index
            , @RequestBody Map<String, Object> searchParam) throws IOException {
        return service.search(index, searchParam);
    }
}
