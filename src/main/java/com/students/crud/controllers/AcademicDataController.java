package com.students.crud.controllers;

import com.students.crud.Services.AcademicDataService;
import com.students.crud.Services.StudentService;
import com.students.crud.dto.AcademicData;
import com.students.crud.dto.Student;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/academic")
public class AcademicDataController {

    @Autowired
    private AcademicDataService academicDataService;

    @PostMapping("/add")
    public ResponseEntity<AcademicData> addAcademicData(@RequestBody AcademicData academicData) {
        AcademicData savedAcademicData = academicDataService.saveAcademicData(academicData);
        return ResponseEntity.ok(savedAcademicData);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AcademicData> getAcademicDataById(@PathVariable Long id) {
        Optional<AcademicData> academicData = academicDataService.getAcademicDataById(id);
        return academicData.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<AcademicData>> getAllAcademicData() {
        List<AcademicData> academicDataList = academicDataService.getAllAcademicData();
        return ResponseEntity.ok(academicDataList);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAcademicData(@PathVariable Long id) {
        academicDataService.deleteAcademicData(id);
        return ResponseEntity.noContent().build();
    }
}
