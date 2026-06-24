package com.school.teacher.service;

import com.school.teacher.dto.PagedResponseDTO;
import com.school.teacher.dto.TeacherRequestDTO;
import com.school.teacher.dto.TeacherResponseDTO;
import com.school.teacher.entity.Teacher;
import com.school.teacher.entity.Teacher.TeacherStatus;
import com.school.teacher.exception.DuplicateEmailException;
import com.school.teacher.exception.TeacherNotFoundException;
import com.school.teacher.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TeacherServiceImpl implements TeacherService {

    private final TeacherRepository teacherRepository;
    private final TeacherMapper teacherMapper;

    @Override
    public TeacherResponseDTO createTeacher(TeacherRequestDTO requestDTO) {
        log.info("Creating teacher with email: {}", requestDTO.getEmail());
        if (teacherRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateEmailException(requestDTO.getEmail());
        }
        Teacher teacher = teacherMapper.toEntity(requestDTO);
        Teacher savedTeacher = teacherRepository.save(teacher);
        log.info("Teacher created with id: {}", savedTeacher.getId());
        return teacherMapper.toResponseDTO(savedTeacher);
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherResponseDTO getTeacherById(Long id) {
        log.info("Fetching teacher with id: {}", id);
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new TeacherNotFoundException(id));
        return teacherMapper.toResponseDTO(teacher);
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherResponseDTO getTeacherByEmail(String email) {
        log.info("Fetching teacher with email: {}", email);
        Teacher teacher = teacherRepository.findByEmail(email)
                .orElseThrow(() -> new TeacherNotFoundException("Teacher not found with email: " + email));
        return teacherMapper.toResponseDTO(teacher);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<TeacherResponseDTO> getAllTeachers(Pageable pageable) {
        log.info("Fetching all teachers - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Teacher> teacherPage = teacherRepository.findAll(pageable);
        return toPagedResponse(teacherPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<TeacherResponseDTO> getTeachersByDepartment(String department, Pageable pageable) {
        log.info("Fetching teachers by department: {}", department);
        Page<Teacher> teacherPage = teacherRepository.findByDepartment(department, pageable);
        return toPagedResponse(teacherPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<TeacherResponseDTO> getTeachersByStatus(TeacherStatus status, Pageable pageable) {
        log.info("Fetching teachers by status: {}", status);
        Page<Teacher> teacherPage = teacherRepository.findByStatus(status, pageable);
        return toPagedResponse(teacherPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<TeacherResponseDTO> searchTeachers(String keyword, Pageable pageable) {
        log.info("Searching teachers with keyword: {}", keyword);
        Page<Teacher> teacherPage = teacherRepository.searchTeachers(keyword, pageable);
        return toPagedResponse(teacherPage);
    }

    @Override
    public TeacherResponseDTO updateTeacher(Long id, TeacherRequestDTO requestDTO) {
        log.info("Updating teacher with id: {}", id);
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new TeacherNotFoundException(id));

        if (teacherRepository.existsByEmailAndIdNot(requestDTO.getEmail(), id)) {
            throw new DuplicateEmailException(requestDTO.getEmail());
        }

        teacherMapper.updateEntityFromDTO(teacher, requestDTO);
        Teacher updatedTeacher = teacherRepository.save(teacher);
        log.info("Teacher updated with id: {}", updatedTeacher.getId());
        return teacherMapper.toResponseDTO(updatedTeacher);
    }

    @Override
    public TeacherResponseDTO updateTeacherStatus(Long id, TeacherStatus status) {
        log.info("Updating status of teacher id: {} to: {}", id, status);
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new TeacherNotFoundException(id));
        teacher.setStatus(status);
        return teacherMapper.toResponseDTO(teacherRepository.save(teacher));
    }

    @Override
    public void deleteTeacher(Long id) {
        log.info("Deleting teacher with id: {}", id);
        if (!teacherRepository.existsById(id)) {
            throw new TeacherNotFoundException(id);
        }
        teacherRepository.deleteById(id);
        log.info("Teacher deleted with id: {}", id);
    }

    private PagedResponseDTO<TeacherResponseDTO> toPagedResponse(Page<Teacher> page) {
        List<TeacherResponseDTO> content = page.getContent().stream()
                .map(teacherMapper::toResponseDTO)
                .collect(Collectors.toList());
        return PagedResponseDTO.<TeacherResponseDTO>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
