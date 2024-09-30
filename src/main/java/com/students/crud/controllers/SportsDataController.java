package com.students.crud.controllers;

import com.students.crud.Services.SportsDataService;
import com.students.crud.dto.SportsData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/sports")
public class SportsDataController {

    @Autowired
    private SportsDataService sportsDataService;

    @PostMapping("/add")
    public ResponseEntity<SportsData> addSportsData(@RequestBody SportsData sportsData) {
        SportsData savedSportsData = sportsDataService.saveSportsData(sportsData);
        return ResponseEntity.ok(savedSportsData);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SportsData> getSportsDataById(@PathVariable Long id) {
        Optional<SportsData> sportsData = sportsDataService.getSportsDataById(id);
        return sportsData.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<SportsData>> getAllSportsData() {
        List<SportsData> sportsDataList = sportsDataService.getAllSportsData();
        return ResponseEntity.ok(sportsDataList);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSportsData(@PathVariable Long id) {
        sportsDataService.deleteSportsData(id);
        return ResponseEntity.noContent().build();
    }
}