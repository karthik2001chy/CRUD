package com.students.crud.controllers;

import com.students.crud.Services.ExtracurricularActivityDataService;
import com.students.crud.dto.ExtracurricularActivityData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/extracurricular")
public class ExtracurricularActivityDataController {

    @Autowired
    private ExtracurricularActivityDataService extracurricularActivityDataService;

    @PostMapping("/add")
    public ResponseEntity<ExtracurricularActivityData> addExtracurricularActivityData(@RequestBody ExtracurricularActivityData extracurricularActivityData) {
        ExtracurricularActivityData savedData = extracurricularActivityDataService.saveExtracurricularActivityData(extracurricularActivityData);
        return ResponseEntity.ok(savedData);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExtracurricularActivityData> getExtracurricularActivityDataById(@PathVariable Long id) {
        Optional<ExtracurricularActivityData> data = extracurricularActivityDataService.getExtracurricularActivityDataById(id);
        return data.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<ExtracurricularActivityData>> getAllExtracurricularActivityData() {
        List<ExtracurricularActivityData> dataList = extracurricularActivityDataService.getAllExtracurricularActivityData();
        return ResponseEntity.ok(dataList);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExtracurricularActivityData(@PathVariable Long id) {
        extracurricularActivityDataService.deleteExtracurricularActivityData(id);
        return ResponseEntity.noContent().build();
    }
}