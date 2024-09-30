package com.students.crud.Services;

import com.students.crud.dto.SportsData;
import com.students.crud.repository.SportsDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SportsDataService {

    @Autowired
    private SportsDataRepository sportsDataRepository;

    public SportsData saveSportsData(SportsData sportsData) {
        return sportsDataRepository.save(sportsData);
    }

    public Optional<SportsData> getSportsDataById(Long id) {
        return sportsDataRepository.findById(id);
    }

    public List<SportsData> getAllSportsData() {
        return sportsDataRepository.findAll();
    }

    public void deleteSportsData(Long id) {
        sportsDataRepository.deleteById(id);
    }
}