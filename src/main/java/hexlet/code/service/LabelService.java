package hexlet.code.service;

import hexlet.code.dto.LabelDTO;
import hexlet.code.dto.TaskDTO;
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

    public List<Label> findAll() {
        return labelRepository.findAll();
    }

    public List<LabelDTO> getAllLabels() {
        return labelRepository.findAll().stream()
                              .map(labelMapper::toDto)
                              .toList();
    }

    public Optional<Label> findById(Long id) {
        return labelRepository.findById(id);
    }

    public Optional<Label> findByName(String name) {
        return labelRepository.findByName(name);
    }

    public Label create(LabelDTO labelDTO) {
        Label label = labelMapper.toEntity(labelDTO);
        return labelRepository.save(label);
    }

    public Optional<Label> update(Long id, LabelDTO labelDTO) {
        return labelRepository.findById(id)
                              .map(existingLabel -> {
                                  // Исправленное название метода
                                  labelMapper.updateEntityFromDto(labelDTO, existingLabel);
                                  return labelRepository.save(existingLabel);
                              });
    }

    public boolean delete(Long id) {
        Optional<Label> labelOpt = labelRepository.findById(id);
        if (labelOpt.isPresent()) {
            Label label = labelOpt.get();

            // Проверяем, есть ли связанные задачи
            if (label.getTasks() != null && !label.getTasks().isEmpty()) {
                return false; // Нельзя удалить - есть связанные задачи
            }

            labelRepository.delete(label);
            return true;
        }
        return false;
    }

    public boolean existsByName(String name) {
        return labelRepository.existsByName(name);
    }

    // Публичный метод для преобразования Label в LabelDTO
    public LabelDTO toDto(Label label) {
        return labelMapper.toDto(label);
    }
}