package com.school.teacher.controller;

import com.school.teacher.dto.ApiResponseDTO;
import com.school.teacher.dto.PagedResponseDTO;
import com.school.teacher.dto.TeacherRequestDTO;
import com.school.teacher.dto.TeacherResponseDTO;
import com.school.teacher.entity.Teacher.TeacherStatus;
import com.school.teacher.service.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/teachers")
@RequiredArgsConstructor
@Tag(name = "Teacher Management", description = "APIs for managing school teachers")
public class TeacherController {

    private final TeacherService teacherService;

    @PostMapping
    @Operation(summary = "Create a new teacher", description = "Adds a new teacher record to the system")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Teacher created successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failed"),
        @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    public ResponseEntity<ApiResponseDTO<TeacherResponseDTO>> createTeacher(
            @Valid @RequestBody TeacherRequestDTO requestDTO) {
        log.info("POST /api/v1/teachers - Creating teacher");
        TeacherResponseDTO response = teacherService.createTeacher(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success("Teacher created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get teacher by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Teacher found"),
        @ApiResponse(responseCode = "404", description = "Teacher not found")
    })
    public ResponseEntity<ApiResponseDTO<TeacherResponseDTO>> getTeacherById(
            @PathVariable Long id) {
        log.info("GET /api/v1/teachers/{}", id);
        TeacherResponseDTO response = teacherService.getTeacherById(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Teacher fetched successfully", response));
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get teacher by email")
    public ResponseEntity<ApiResponseDTO<TeacherResponseDTO>> getTeacherByEmail(
            @PathVariable String email) {
        log.info("GET /api/v1/teachers/email/{}", email);
        TeacherResponseDTO response = teacherService.getTeacherByEmail(email);
        return ResponseEntity.ok(ApiResponseDTO.success("Teacher fetched successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all teachers (paginated)")
    public ResponseEntity<ApiResponseDTO<PagedResponseDTO<TeacherResponseDTO>>> getAllTeachers(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponseDTO<TeacherResponseDTO> response = teacherService.getAllTeachers(pageable);
        return ResponseEntity.ok(ApiResponseDTO.success("Teachers fetched successfully", response));
    }

    @GetMapping("/department/{department}")
    @Operation(summary = "Get teachers by department")
    public ResponseEntity<ApiResponseDTO<PagedResponseDTO<TeacherResponseDTO>>> getTeachersByDepartment(
            @PathVariable String department,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PagedResponseDTO<TeacherResponseDTO> response = teacherService.getTeachersByDepartment(department, pageable);
        return ResponseEntity.ok(ApiResponseDTO.success("Teachers fetched by department", response));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get teachers by status")
    public ResponseEntity<ApiResponseDTO<PagedResponseDTO<TeacherResponseDTO>>> getTeachersByStatus(
            @PathVariable TeacherStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PagedResponseDTO<TeacherResponseDTO> response = teacherService.getTeachersByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponseDTO.success("Teachers fetched by status", response));
    }

    @GetMapping("/search")
    @Operation(summary = "Search teachers by keyword")
    public ResponseEntity<ApiResponseDTO<PagedResponseDTO<TeacherResponseDTO>>> searchTeachers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PagedResponseDTO<TeacherResponseDTO> response = teacherService.searchTeachers(keyword, pageable);
        return ResponseEntity.ok(ApiResponseDTO.success("Search results", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update teacher details")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Teacher updated successfully"),
        @ApiResponse(responseCode = "404", description = "Teacher not found"),
        @ApiResponse(responseCode = "400", description = "Validation failed"),
        @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    public ResponseEntity<ApiResponseDTO<TeacherResponseDTO>> updateTeacher(
            @PathVariable Long id,
            @Valid @RequestBody TeacherRequestDTO requestDTO) {
        log.info("PUT /api/v1/teachers/{}", id);
        TeacherResponseDTO response = teacherService.updateTeacher(id, requestDTO);
        return ResponseEntity.ok(ApiResponseDTO.success("Teacher updated successfully", response));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update teacher status only")
    public ResponseEntity<ApiResponseDTO<TeacherResponseDTO>> updateTeacherStatus(
            @PathVariable Long id,
            @RequestParam TeacherStatus status) {
        log.info("PATCH /api/v1/teachers/{}/status - {}", id, status);
        TeacherResponseDTO response = teacherService.updateTeacherStatus(id, status);
        return ResponseEntity.ok(ApiResponseDTO.success("Teacher status updated", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a teacher")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Teacher deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Teacher not found")
    })
    public ResponseEntity<ApiResponseDTO<Void>> deleteTeacher(@PathVariable Long id) {
        log.info("DELETE /api/v1/teachers/{}", id);
        teacherService.deleteTeacher(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Teacher deleted successfully"));
    }
}
