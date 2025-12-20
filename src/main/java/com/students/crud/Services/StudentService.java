package com.students.crud.Services;

import com.students.crud.DAO.Student;
import com.students.crud.DTO.StudentSimpleDto;
import com.students.crud.mapper.StudentMapper;
import com.students.crud.repository.StudentRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Collections;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.SliceImpl;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;

    public StudentService(StudentRepository studentRepository, StudentMapper studentMapper) {
        this.studentRepository = studentRepository;
        this.studentMapper = studentMapper;
    }

    @CacheEvict(value = "studentSimple", allEntries = true)
    public Student registerStudent(Student student) {
        return studentRepository.save(student);
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Page<Student> getStudents(Pageable pageable) {
        return studentRepository.findAll(pageable);
    }

    public Page<StudentSimpleDto> getStudentsSimple(Pageable pageable) {
        List<StudentSimpleDto> all = getAllSimple(); // cached list
        int pageSize = pageable.getPageSize();
        int pageNumber = pageable.getPageNumber();
        int start = pageNumber * pageSize;
        int end = Math.min(start + pageSize, all.size());
        List<StudentSimpleDto> content = (start >= all.size() || start < 0) ? Collections.emptyList() : all.subList(start, end);
        return new PageImpl<>(content, pageable, all.size());
    }

    @CacheEvict(value = "studentSimple", allEntries = true)
    public void deleteStudent(String registrationNo) {
        studentRepository.deleteById(registrationNo);
    }

    // Cache the full simple list in Redis under cache name 'studentSimple'
    @Cacheable(value = "studentSimple")
    public List<StudentSimpleDto> getAllSimple() {
        return studentMapper.toSimpleDtoList(studentRepository.findAll());
    }

    public Optional<StudentSimpleDto> getSimpleByRegistrationNo(String registrationNo) {
        return studentRepository.findById(registrationNo).map(studentMapper::toSimpleDto);
    }

    // Cache the page content (List) rather than the Slice object which Jackson can't deserialize.
    @Cacheable(value = "studentSimple", key = "'sliceContent:' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort")
    public List<StudentSimpleDto> getStudentsSimpleSliceContent(Pageable pageable) {
        List<StudentSimpleDto> all = getAllSimple();
        int pageSize = pageable.getPageSize();
        int pageNumber = pageable.getPageNumber();
        int start = pageNumber * pageSize;
        if (start >= all.size() || start < 0) {
            return Collections.emptyList();
        }
        int endExclusive = Math.min(start + pageSize, all.size());
        return all.subList(start, endExclusive);
    }

    // Public method returns a Slice built from cached page content and the cached full list for hasNext calculation.
    public org.springframework.data.domain.Slice<StudentSimpleDto> getStudentsSimpleSlice(Pageable pageable) {
        List<StudentSimpleDto> content = getStudentsSimpleSliceContent(pageable); // cached list per page
        int pageSize = pageable.getPageSize();
        int pageNumber = pageable.getPageNumber();
        int start = pageNumber * pageSize;
        boolean hasNext = (start + content.size()) < getAllSimple().size();
        return new SliceImpl<>(content, pageable, hasNext);
    }
}
