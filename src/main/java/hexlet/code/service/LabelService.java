package hexlet.code.service;

import hexlet.code.dto.LabelCreateDTO;
import hexlet.code.dto.LabelDTO;
import hexlet.code.dto.LabelUpdateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.exception.ResourceConflictException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.repository.LabelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import hexlet.code.model.Label;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class LabelService {
    private final LabelRepository labelRepository;
    private final LabelMapper labelMapper;

    public List<LabelDTO> getAllLabels() {
        return labelRepository.findAll().stream()
                              .map(labelMapper::toDto)
                              .toList();
    }

    public LabelDTO getLabelById(Long id) {
        Label label = labelRepository.findById(id)
                                     .orElseThrow(() -> new ResourceNotFoundException("Label not found with id: " + id));
        return labelMapper.toDto(label);
    }

    public LabelDTO createLabel(LabelCreateDTO labelCreateDTO) {
        if (labelRepository.existsByName(labelCreateDTO.getName())) {
            throw new ResourceConflictException("Label with name '" + labelCreateDTO.getName() + "' already exists");
        }

        Label label = labelMapper.toEntity(labelCreateDTO);
        Label saved = labelRepository.save(label);
        return labelMapper.toDto(saved);
    }

    public LabelDTO updateLabel(Long id, LabelUpdateDTO updateDTO) {
        Label label = labelRepository.findById(id)
                                     .orElseThrow(() -> new ResourceNotFoundException("Label not found with id: " + id));

        // Проверяем уникальность имени только если имя передано
        if (updateDTO.getName() != null && !updateDTO.getName().equals(label.getName())) {
            Optional<Label> existingWithName = labelRepository.findByName(updateDTO.getName());
            if (existingWithName.isPresent() && !existingWithName.get().getId().equals(id)) {
                throw new ResourceConflictException("Label with name '" + updateDTO.getName() + "' already exists");
            }
        }

        labelMapper.update(updateDTO, label);
        Label updated = labelRepository.save(label);
        return labelMapper.toDto(updated);
    }

    public void deleteLabel(Long id) {
        Label label = labelRepository.findById(id)
                                     .orElseThrow(() -> new ResourceNotFoundException("Label not found with id: " + id));

        if (!label.getTasks().isEmpty()) {
            throw new ResourceConflictException("Cannot delete label with id: " + id + " because it has associated tasks");
        }

        labelRepository.delete(label);
    }
}