package com.students.crud.Services;

import com.students.crud.dto.ExtracurricularActivityData;
import com.students.crud.repository.ExtracurricularActivityDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExtracurricularActivityDataService {

    @Autowired
    private ExtracurricularActivityDataRepository extracurricularActivityDataRepository;

    public ExtracurricularActivityData saveExtracurricularActivityData(ExtracurricularActivityData extracurricularActivityData) {
        return extracurricularActivityDataRepository.save(extracurricularActivityData);
    }

    public Optional<ExtracurricularActivityData> getExtracurricularActivityDataById(Long id) {
        return extracurricularActivityDataRepository.findById(id);
    }

    public List<ExtracurricularActivityData> getAllExtracurricularActivityData() {
        return extracurricularActivityDataRepository.findAll();
    }

    public void deleteExtracurricularActivityData(Long id) {
        extracurricularActivityDataRepository.deleteById(id);
    }
}
