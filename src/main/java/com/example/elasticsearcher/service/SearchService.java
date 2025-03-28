package com.example.elasticsearcher.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.json.JsonData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ElasticsearchClient elasticsearchClient;

    public List<Map<String, Object>> search(String index, Map<String, Object> searchParams) throws IOException {
        var queries = new ArrayList<Query>();
        searchParams.forEach((field, value) -> {
            Query query = switch (value) {
                case String s -> MatchQuery.of(m -> m.field(field).query(s))._toQuery();
                case Long l -> TermQuery.of(t -> t.field(field).value(l))._toQuery();
                case Integer i -> TermQuery.of(t -> t.field(field).value(i))._toQuery();
                case Boolean b -> TermQuery.of(t -> t.field(field).value(b))._toQuery();
                case Map<?, ?> mapValue when mapValue.containsKey("gte") || mapValue.containsKey("lte") ->
                        RangeQuery.of(r -> r.field(field)
                                .gte(mapValue.containsKey("gte") ? JsonData.of(mapValue.get("gte")) : null)
                                .lte(mapValue.containsKey("lte") ? JsonData.of(mapValue.get("lte")) : null)
                        )._toQuery();
                default -> throw new IllegalArgumentException("Invalid search parameter: " + value);
            };
            queries.add(query);
        });
        var response = elasticsearchClient.search(s -> s.index(index)
                .query(q -> q.bool(b -> b.must(queries))), Map.class);
        return response.hits().hits().stream()
                .map(hit -> (Map<String, Object>) hit.source()).toList();

    }
}
