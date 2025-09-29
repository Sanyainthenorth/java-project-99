package hexlet.code.service;

import hexlet.code.dto.LabelDTO;
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

    public LabelDTO createLabel(LabelDTO labelDTO) {
        if (labelRepository.existsByName(labelDTO.getName())) {
            throw new ResourceConflictException("Label with name '" + labelDTO.getName() + "' already exists");
        }

        Label label = labelMapper.toEntity(labelDTO);
        Label saved = labelRepository.save(label);
        return labelMapper.toDto(saved);
    }

    public LabelDTO updateLabel(Long id, LabelDTO labelDTO) {
        Label label = labelRepository.findById(id)
                                     .orElseThrow(() -> new ResourceNotFoundException("Label not found with id: " + id));

        // Проверяем уникальность имени
        Optional<Label> existingWithName = labelRepository.findByName(labelDTO.getName());
        if (existingWithName.isPresent() && !existingWithName.get().getId().equals(id)) {
            throw new ResourceConflictException("Label with name '" + labelDTO.getName() + "' already exists");
        }

        label.setName(labelDTO.getName());
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