package dev.magadiflo.app.service.impl;

import dev.magadiflo.app.dao.DepartmentDao;
import dev.magadiflo.app.dto.DepartmentRequest;
import dev.magadiflo.app.dto.DepartmentResponse;
import dev.magadiflo.app.dto.EmployeeResponse;
import dev.magadiflo.app.entity.Department;
import dev.magadiflo.app.exception.DepartmentAlreadyExistsException;
import dev.magadiflo.app.exception.DepartmentNotFoundException;
import dev.magadiflo.app.mapper.DepartmentMapper;
import dev.magadiflo.app.mapper.EmployeeMapper;
import dev.magadiflo.app.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentDao departmentDao;
    private final DepartmentMapper departmentMapper;
    private final EmployeeMapper employeeMapper;

    @Override
    public Flux<DepartmentResponse> getAllDepartments() {
        return this.departmentDao.findAll()
                .map(this.departmentMapper::toBasicDepartmentResponse);
    }

    @Override
    public Mono<DepartmentResponse> showDepartment(Long departmentId) {
        return this.departmentDao.findById(departmentId)
                .switchIfEmpty(Mono.error(() -> new DepartmentNotFoundException(departmentId)))
                .map(this.departmentMapper::toBasicDepartmentResponse);
    }

    @Override
    public Mono<DepartmentResponse> showDepartmentWithManagerAndEmployees(Long departmentId) {
        return this.departmentDao.findDepartmentWithManagerAndEmployees(departmentId)
                .switchIfEmpty(Mono.error(() -> new DepartmentNotFoundException(departmentId)))
                .map(this.departmentMapper::toDepartmentResponse);
    }

    @Override
    @Transactional
    public Mono<DepartmentResponse> createDepartment(DepartmentRequest request) {
        return this.departmentDao.findByName(request.name())
                .flatMap(department -> Mono.<Department>error(new DepartmentAlreadyExistsException(department.getName())))
                .switchIfEmpty(Mono.fromSupplier(() -> this.departmentMapper.toDepartment(request)))
                .flatMap(this.departmentDao::save)
                .map(this.departmentMapper::toBasicDepartmentResponse);
    }

    @Override
    @Transactional
    public Mono<DepartmentResponse> updateDepartment(Long departmentId, Department department) {
        return this.departmentDao.findDepartmentWithManagerAndEmployees(departmentId)
                .switchIfEmpty(Mono.error(() -> new DepartmentNotFoundException(departmentId)))
                .map(departmentDB -> {
                    departmentDB.setName(department.getName());
                    if (department.getManager().isPresent()) {
                        departmentDB.setManager(department.getManager().get());
                    }
                    departmentDB.setEmployees(department.getEmployees());
                    return departmentDB;
                })
                .flatMap(this.departmentDao::save)
                .map(this.departmentMapper::toDepartmentResponse);
    }

    @Override
    @Transactional
    public Mono<Void> deleteDepartment(Long departmentId) {
        return this.departmentDao.findById(departmentId)
                .switchIfEmpty(Mono.error(() -> new DepartmentNotFoundException(departmentId)))
                .flatMap(this.departmentDao::delete);
    }

    @Override
    public Flux<EmployeeResponse> getEmployeesFromDepartment(Long departmentId, Boolean isFullTime) {
        if (isFullTime != null) {
            return this.departmentDao.findDepartmentWithManagerAndEmployees(departmentId)
                    .switchIfEmpty(Mono.error(() -> new DepartmentNotFoundException(departmentId)))
                    .flatMapMany(department -> Flux.fromStream(
                                    department.getEmployees().stream()
                                            .filter(employee -> employee.getFullTime().equals(isFullTime))
                            )
                    )
                    .map(this.employeeMapper::toEmployeeResponse);
        }
        return this.departmentDao.findDepartmentWithManagerAndEmployees(departmentId)
                .switchIfEmpty(Mono.error(() -> new DepartmentNotFoundException(departmentId)))
                .flatMapMany(department -> Flux.fromIterable(department.getEmployees()))
                .map(this.employeeMapper::toEmployeeResponse);
    }
}
