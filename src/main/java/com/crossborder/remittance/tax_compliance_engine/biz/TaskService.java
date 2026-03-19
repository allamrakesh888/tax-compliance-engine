package com.crossborder.remittance.tax_compliance_engine.biz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crossborder.remittance.tax_compliance_engine.dal.Task;
import com.crossborder.remittance.tax_compliance_engine.dal.TaskRepository;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
    }
}
