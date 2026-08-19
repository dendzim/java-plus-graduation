package ru.practicum.service.compilation;

import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.CompilationSearchFilter;
import ru.practicum.dto.compilation.CompilationUpdateDto;
import ru.practicum.dto.compilation.NewCompilationDto;

import java.util.List;

public interface CompilationService {

	CompilationDto getById(Long compilationId);

	void delById(Long compilationId);

	CompilationDto addCompilation(NewCompilationDto compilation);

	CompilationDto updateCompilation(Long compilationId, CompilationUpdateDto compilation);

	List<CompilationDto> getByFilter(CompilationSearchFilter filter);
}
