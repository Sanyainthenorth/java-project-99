package hexlet.code.controller;

import hexlet.code.dto.LabelDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.exception.ResourceConflictException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.model.Label;
import hexlet.code.service.LabelService;
import hexlet.code.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/labels")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class LabelController {

    private final LabelService labelService;

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public LabelDTO getLabel(@PathVariable Long id) {
        return labelService.getLabelById(id);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<LabelDTO>> getAllLabels() {
        List<LabelDTO> labels = labelService.getAllLabels();
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(labels.size()));
        return new ResponseEntity<>(labels, headers, HttpStatus.OK);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LabelDTO createLabel(@Valid @RequestBody LabelDTO labelDTO) {
        return labelService.createLabel(labelDTO);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public LabelDTO updateLabel(@PathVariable Long id, @Valid @RequestBody LabelDTO labelDTO) {
        return labelService.updateLabel(id, labelDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLabel(@PathVariable Long id) {
        labelService.deleteLabel(id);
    }
}